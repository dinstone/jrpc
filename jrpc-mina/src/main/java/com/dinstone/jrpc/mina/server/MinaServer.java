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

package com.dinstone.jrpc.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.RpcException;
import com.dinstone.jrpc.mina.TransportProtocolDecoder;
import com.dinstone.jrpc.mina.TransportProtocolEncoder;
import com.dinstone.jrpc.processor.DefaultServiceProcessor;
import com.dinstone.jrpc.processor.ServiceProcessor;
import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.server.AbstractServer;
import com.dinstone.jrpc.server.Server;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class MinaServer extends AbstractServer implements Server {

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

    private static final Logger LOG = LoggerFactory.getLogger(MinaServer.class);

    private SocketAcceptor acceptor;

    private ExecutorService executorService;

    public MinaServer(Configuration config, ServiceProcessor serviceProcessor) {
        this.config.setServiceHost(config.getServiceHost());
        this.config.setServicePort(config.getServicePort());

        init(serviceProcessor);
    }

    public MinaServer(String host, int port) {
        this.config.setServiceHost(host);
        this.config.setServicePort(port);

        init(new DefaultServiceProcessor());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.server.Server#shutdown()
     */
    @Override
    public Server shutdown() {
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

        if (serviceProcessor != null) {
            serviceProcessor.destroy();
        }

        LOG.info("jrpc service shutdown");

        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.server.Server#start()
     */
    @Override
    public Server start() {
        String serviceHost = getServiceHost();
        int servicePort = getServicePort();
        String localAddress = serviceHost + ":" + servicePort;
        try {
            bind0(serviceHost, servicePort);

            LOG.info("jrpc service works on " + localAddress);
        } catch (IOException e) {
            LOG.error("jrpc can't bind to the specified local address " + localAddress, e);
            throw new RpcException(500, "jrpc can't bind to the specified local address " + localAddress, e);
        }

        return this;
    }

    private void bind0(String host, int port) throws IOException {
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

        int maxSize = config.getMaxSize();
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

        executorService = Executors.newFixedThreadPool(config.getParallelCount());
        chainBuilder.addLast("threadPool", new ExecutorFilter(executorService, IoEventType.MESSAGE_RECEIVED));

        // add keep alive filter
        KeepAliveFilter kaFilter = new KeepAliveFilter(new PassiveKeepAliveMessageFactory(), IdleStatus.BOTH_IDLE);
        kaFilter.setForwardEvent(true);
        chainBuilder.addLast("keepAlive", kaFilter);

        // add business handler
        acceptor.setHandler(new MinaServerHandler(acceptance));

        if (host != null) {
            InetSocketAddress localAddress = new InetSocketAddress(host, port);
            acceptor.bind(localAddress);
        } else {
            InetSocketAddress localAddress = new InetSocketAddress(port);
            acceptor.bind(localAddress);
        }
    }

    public MinaServer setMaxObjectSize(int maxSize) {
        config.setMaxSize(maxSize);
        return this;
    }

    public MinaServer setParallelCount(int count) {
        config.setParallelCount(count);
        return this;
    }
}
