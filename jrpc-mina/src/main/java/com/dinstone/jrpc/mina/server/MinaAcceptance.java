/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
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
package com.dinstone.jrpc.mina.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.invoker.SkelectonServiceInvoker;
import com.dinstone.jrpc.mina.TransportProtocolDecoder;
import com.dinstone.jrpc.mina.TransportProtocolEncoder;
import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.transport.AbstractAcceptance;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.TransportConfig;

public class MinaAcceptance extends AbstractAcceptance {

    private static final Logger LOG = LoggerFactory.getLogger(MinaAcceptance.class);

    private SocketAcceptor acceptor;

    private ExecutorService executorService;

    private TransportConfig transportConfig;

    public MinaAcceptance(TransportConfig transportConfig, ImplementBinding implementBinding) {
        super(implementBinding, new SkelectonServiceInvoker());
        this.transportConfig = transportConfig;
    }

    @Override
    public MinaAcceptance bind() {
        // This socket acceptor will handle incoming connections
        acceptor = new NioSocketAcceptor();
        acceptor.setReuseAddress(true);

        SocketSessionConfig sessionConfig = acceptor.getSessionConfig();

        // set read buffer size
        sessionConfig.setReceiveBufferSize(8 * 1024);
        sessionConfig.setSendBufferSize(8 * 1024);

        // get filter chain builder
        DefaultIoFilterChainBuilder chainBuilder = acceptor.getFilterChain();

        // add message codec filter
        final TransportProtocolEncoder encoder = new TransportProtocolEncoder();
        final TransportProtocolDecoder decoder = new TransportProtocolDecoder();

        int maxSize = transportConfig.getMaxSize();
        encoder.setMaxObjectSize(maxSize);
        decoder.setMaxObjectSize(maxSize);
        chainBuilder.addLast("codec", new ProtocolCodecFilter(new ProtocolCodecFactory() {

            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                return encoder;
            }

            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                return decoder;
            }
        }));

        executorService = Executors.newFixedThreadPool(transportConfig.getParallelCount());
        chainBuilder.addLast("threadPool", new ExecutorFilter(executorService, IoEventType.MESSAGE_RECEIVED));

        // add keep alive filter
        KeepAliveFilter kaFilter = new KeepAliveFilter(new PassiveKeepAliveMessageFactory(), IdleStatus.BOTH_IDLE);
        kaFilter.setForwardEvent(true);
        chainBuilder.addLast("keepAlive", kaFilter);

        // add business handler
        acceptor.setHandler(new MinaIoHandler(this));

        InetSocketAddress serviceAddress = implementBinding.getServiceAddress();
        try {
            acceptor.bind(serviceAddress);
        } catch (Exception e) {
            throw new RuntimeException("can't bind service on " + serviceAddress, e);
        }
        LOG.info("jrpc service start on {}", serviceAddress);

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

        LOG.info("jrpc service stop on {}", implementBinding.getServiceAddress());
    }

    private class MinaIoHandler extends IoHandlerAdapter {

        private Acceptance acceptance;

        /**
         * @param acceptance
         */
        public MinaIoHandler(Acceptance acceptance) {
            this.acceptance = acceptance;
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            if (message instanceof Request) {
                Response response = acceptance.handle((Request) message);
                session.write(response);
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache.mina.core.session.IoSession)
         */
        @Override
        public void sessionClosed(IoSession session) throws Exception {
            long id = session.getId();
            LOG.debug("Session[{}] is closed", id);
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

        public boolean isResponse(IoSession session, Object message) {
            return false;
        }

        public Object getRequest(IoSession session) {
            return null;
        }

        public boolean isRequest(IoSession session, Object message) {
            if (message instanceof Heartbeat) {
                return true;
            }
            return false;
        }

        public Object getResponse(IoSession session, Object request) {
            Heartbeat heartbeat = (Heartbeat) request;
            heartbeat.getContent().increase();
            return heartbeat;
        }
    }

}
