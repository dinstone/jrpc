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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.NamedThreadFactory;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.transport.AbstractAcceptance;
import com.dinstone.jrpc.transport.NetworkAddressUtil;
import com.dinstone.jrpc.transport.TransportConfig;

public class MinaAcceptance extends AbstractAcceptance {

    private static final Logger LOG = LoggerFactory.getLogger(MinaAcceptance.class);

    private SocketAcceptor acceptor;

    private ExecutorService executorService;

    public MinaAcceptance(TransportConfig transportConfig, ImplementBinding implementBinding,
            InetSocketAddress serviceAddress) {
        super(transportConfig, implementBinding, serviceAddress);
    }

    @Override
    public MinaAcceptance bind() {
        // This socket acceptor will handle incoming connections
        acceptor = new NioSocketAcceptor(transportConfig.getNioProcessorCount());
        acceptor.setReuseAddress(true);
        acceptor.setBacklog(128);

        SocketSessionConfig sessionConfig = acceptor.getSessionConfig();
        sessionConfig.setTcpNoDelay(true);

        // set read buffer size
        sessionConfig.setReceiveBufferSize(16 * 1024);
        sessionConfig.setSendBufferSize(16 * 1024);

        // get filter chain builder
        DefaultIoFilterChainBuilder chainBuilder = acceptor.getFilterChain();

        // add message codec filter
        final TransportProtocolEncoder encoder = new TransportProtocolEncoder();
        final TransportProtocolDecoder decoder = new TransportProtocolDecoder();

        encoder.setMaxObjectSize(transportConfig.getMaxSize());
        decoder.setMaxObjectSize(transportConfig.getMaxSize());
        chainBuilder.addLast("codec", new ProtocolCodecFilter(encoder, decoder));

        // add keep alive filter
        KeepAliveFilter kaFilter = new KeepAliveFilter(new PassiveKeepAliveMessageFactory(), IdleStatus.BOTH_IDLE);
        kaFilter.setRequestInterval(transportConfig.getHeartbeatIntervalSeconds());
        kaFilter.setForwardEvent(true);
        chainBuilder.addLast("keepAlive", kaFilter);

        // add business handler
        acceptor.setHandler(new MinaIoHandler());

        try {
            acceptor.bind(serviceAddress);

            int processorCount = transportConfig.getBusinessProcessorCount();
            if (processorCount > 0) {
                NamedThreadFactory threadFactory = new NamedThreadFactory("Mina-BusinssProcessor");
                executorService = Executors.newFixedThreadPool(processorCount, threadFactory);
            }
        } catch (Exception e) {
            throw new RuntimeException("can't bind service on " + serviceAddress, e);
        }
        LOG.info("mina acceptance bind on {}", serviceAddress);

        return this;
    }

    @Override
    public void destroy() {
        if (acceptor != null) {
            acceptor.dispose(true);
        }

        if (executorService != null) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }

        LOG.info("mina acceptance unbind on {}", serviceAddress);
    }

    private class MinaIoHandler extends IoHandlerAdapter {

        private static final String LOCAL_REMOTE_ADDRESS_KEY = "local-remote-address-key";

        private final int maxConnectionCount = transportConfig.getMaxConnectionCount();

        private ConcurrentMap<String, IoSession> connectionMap = new ConcurrentHashMap<>();

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            int currentConnectioncount = connectionMap.size();
            if (currentConnectioncount >= maxConnectionCount) {
                session.close(true);
                LOG.warn("connection count is too big: limit={},current={}", maxConnectionCount,
                    currentConnectioncount);
            } else {
                String addressLabel = NetworkAddressUtil.addressLabel(session.getRemoteAddress(),
                    session.getLocalAddress());
                session.setAttribute(LOCAL_REMOTE_ADDRESS_KEY, addressLabel);
                connectionMap.put(addressLabel, session);
            }
        }

        @Override
        public void messageReceived(final IoSession session, final Object message) throws Exception {
            if (message instanceof Request) {
                if (executorService != null) {
                    executorService.execute(new Runnable() {

                        @Override
                        public void run() {
                            process(session, message);
                        }

                    });
                } else {
                    process(session, message);
                }
            }
        }

        protected void process(IoSession session, Object message) {
            Response response = handle((Request) message);
            session.write(response);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache.mina.core.session.IoSession)
         */
        @Override
        public void sessionClosed(IoSession session) throws Exception {
            String connectionKey = (String) session.getAttribute(LOCAL_REMOTE_ADDRESS_KEY);
            if (connectionKey != null) {
                connectionMap.remove(connectionKey);
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            if (cause instanceof ProtocolDecoderException) {
                session.close(true);
            }
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            LOG.debug("Session[{}] is idle with status[{}]", session.getId(), status);
        }

    }

    private final class PassiveKeepAliveMessageFactory implements KeepAliveMessageFactory {

        @Override
        public boolean isResponse(IoSession session, Object message) {
            return false;
        }

        @Override
        public Object getRequest(IoSession session) {
            return null;
        }

        @Override
        public boolean isRequest(IoSession session, Object message) {
            if (message instanceof Heartbeat) {
                return true;
            }
            return false;
        }

        @Override
        public Object getResponse(IoSession session, Object request) {
            Heartbeat heartbeat = (Heartbeat) request;
            heartbeat.getContent().increase();
            return heartbeat;
        }
    }

}
