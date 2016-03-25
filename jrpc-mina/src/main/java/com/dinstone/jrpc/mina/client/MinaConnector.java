/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.mina.client;

import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.mina.TransportProtocolDecoder;
import com.dinstone.jrpc.mina.TransportProtocolEncoder;
import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Tick;
import com.dinstone.jrpc.serialize.SerializeType;
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

        public boolean isResponse(IoSession session, Object message) {
            if (message instanceof Heartbeat) {
                return true;
            }
            return false;
        }

        public Object getRequest(IoSession session) {
            return new Heartbeat(0, serializeType, new Tick());
        }

        public boolean isRequest(IoSession session, Object message) {
            return false;
        }

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

    /**
     * @param port2
     * @param host2
     * @param config
     */
    private void initConnector(InetSocketAddress isa, TransportConfig config) {
        // create connector
        ioConnector = new NioSocketConnector();
        ioConnector.setConnectTimeoutMillis(config.getConnectTimeout());
        SocketSessionConfig sessionConfig = ioConnector.getSessionConfig();

        // set read buffer size
        sessionConfig.setReceiveBufferSize(4 * 1024);

        DefaultIoFilterChainBuilder chainBuilder = ioConnector.getFilterChain();

        int maxLen = config.getMaxSize();
        LOG.debug("rpc.protocol.maxlength is {}", maxLen);

        final TransportProtocolEncoder encoder = new TransportProtocolEncoder();
        final TransportProtocolDecoder decoder = new TransportProtocolDecoder();
        encoder.setMaxObjectSize(maxLen);
        decoder.setMaxObjectSize(maxLen);
        // add filter
        chainBuilder.addLast("codec", new ProtocolCodecFilter(new ProtocolCodecFactory() {

            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                return encoder;
            }

            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                return decoder;
            }
        }));

        // add keep alive filter
        ActiveKeepAliveMessageFactory messageFactory = new ActiveKeepAliveMessageFactory(config.getSerializeType());
        KeepAliveFilter kaFilter = new KeepAliveFilter(messageFactory, IdleStatus.BOTH_IDLE);
        kaFilter.setForwardEvent(true);
        chainBuilder.addLast("keepAlive", kaFilter);

        // set handler
        ioConnector.setHandler(new MinaClientHandler());

        ioConnector.setDefaultRemoteAddress(isa);
    }

    /**
     * @return
     */
    public IoSession createSession() {
        // create session
        LOG.debug("create session on {} ", ioConnector.getDefaultRemoteAddress());
        // long s = System.currentTimeMillis();
        ConnectFuture future = ioConnector.connect().awaitUninterruptibly();
        IoSession session = future.getSession();
        // long t = System.currentTimeMillis() - s;
        // LOG.debug("create session on {} takes {}ms", ioConnector.getDefaultRemoteAddress(), t);
        return session;
    }

    public void dispose() {
        ioConnector.dispose(false);
    }

}
