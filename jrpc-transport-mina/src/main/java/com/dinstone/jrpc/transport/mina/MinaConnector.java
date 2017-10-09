/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.jrpc.transport.mina;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.protocol.Tick;
import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.TransportConfig;

/**
 * @author guojf
 * @version 1.0.0.2013-4-11
 */
public class MinaConnector {

    private final class ActiveKeepAliveMessageFactory implements KeepAliveMessageFactory {

        private SerializeType serializeType;

        public ActiveKeepAliveMessageFactory(SerializeType serializeType) {
            this.serializeType = serializeType;
        }

        @Override
        public boolean isResponse(IoSession session, Object message) {
            if (message instanceof Heartbeat) {
                return true;
            }
            return false;
        }

        @Override
        public Object getRequest(IoSession session) {
            return new Heartbeat(0, serializeType, new Tick());
        }

        @Override
        public boolean isRequest(IoSession session, Object message) {
            return false;
        }

        @Override
        public Object getResponse(IoSession session, Object request) {
            // HeartbeatPing ping = (HeartbeatPing) request;
            // return new HeartbeatPong(ping.getMessageId(), ping.getSerializeType(), new Pong());
            return null;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(MinaConnector.class);

    private NioSocketConnector ioConnector;

    public MinaConnector(InetSocketAddress isa, TransportConfig config) {
        initConnector(isa, config);
    }

    private void initConnector(InetSocketAddress isa, TransportConfig config) {
        // create connector
        ioConnector = new NioSocketConnector(1);
        ioConnector.setConnectTimeoutMillis(config.getConnectTimeout());

        SocketSessionConfig sessionConfig = ioConnector.getSessionConfig();
        sessionConfig.setTcpNoDelay(true);
        sessionConfig.setReceiveBufferSize(8 * 1024);
        sessionConfig.setSendBufferSize(8 * 1024);

        DefaultIoFilterChainBuilder chainBuilder = ioConnector.getFilterChain();

        final TransportProtocolEncoder encoder = new TransportProtocolEncoder();
        final TransportProtocolDecoder decoder = new TransportProtocolDecoder();
        encoder.setMaxObjectSize(config.getMaxSize());
        decoder.setMaxObjectSize(config.getMaxSize());
        // add filter
        chainBuilder.addLast("codec", new ProtocolCodecFilter(encoder, decoder));

        // add keep alive filter
        ActiveKeepAliveMessageFactory messageFactory = new ActiveKeepAliveMessageFactory(config.getSerializeType());
        KeepAliveFilter kaFilter = new KeepAliveFilter(messageFactory, IdleStatus.BOTH_IDLE);
        kaFilter.setRequestInterval(config.getHeartbeatIntervalSeconds());
        kaFilter.setForwardEvent(true);
        chainBuilder.addLast("keepAlive", kaFilter);

        // set handler
        ioConnector.setHandler(new MinaIoHandler());

        ioConnector.setDefaultRemoteAddress(isa);
    }

    /**
     * @return
     */
    public IoSession createSession() {
        // create session
        // LOG.debug("create session to {} ", ioConnector.getDefaultRemoteAddress());
        // long s = System.currentTimeMillis();
        IoSession session = ioConnector.connect().awaitUninterruptibly().getSession();
        // long t = System.currentTimeMillis() - s;
        LOG.debug("session connect {} to {}", session.getLocalAddress(), session.getRemoteAddress());
        return session;
    }

    public void dispose() {
        ioConnector.dispose(false);
    }

    private class MinaIoHandler extends IoHandlerAdapter {

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            // LOG.debug("Session[{}] is closed", session.getId());
            Map<Integer, ResultFuture> futureMap = SessionUtil.getResultFutureMap(session);
            if (futureMap != null) {
                for (ResultFuture future : futureMap.values()) {
                    future.setResult(new Result(400, "connection is closed"));
                }
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            LOG.error("Unhandled Exception", cause);
            session.close(true);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            handle(session, (Response) message);
        }

        private void handle(IoSession session, Response response) {
            Map<Integer, ResultFuture> cfMap = SessionUtil.getResultFutureMap(session);
            ResultFuture future = cfMap.remove(response.getMessageId());
            if (future != null) {
                future.setResult(response.getResult());
            }
        }

    }

    public InetSocketAddress getRemoteAddress() {
        return ioConnector.getDefaultRemoteAddress();
    }
}
