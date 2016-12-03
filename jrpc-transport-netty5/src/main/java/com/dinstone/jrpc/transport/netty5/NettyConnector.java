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

package com.dinstone.jrpc.transport.netty5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultExecutorServiceFactory;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.transport.TransportConfig;

public class NettyConnector {

    private static final Logger LOG = LoggerFactory.getLogger(NettyConnector.class);

    private int refCount;

    private NioEventLoopGroup workerGroup;

    private Bootstrap clientBoot;

    public NettyConnector(InetSocketAddress isa, final TransportConfig transportConfig) {
        workerGroup = new NioEventLoopGroup(1, new DefaultExecutorServiceFactory("N5CWork"));
        clientBoot = new Bootstrap().group(workerGroup).channel(NioSocketChannel.class);
        clientBoot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, transportConfig.getConnectTimeout());
        clientBoot.option(ChannelOption.SO_RCVBUF, 8 * 1024);
        clientBoot.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                TransportProtocolDecoder decoder = new TransportProtocolDecoder();
                decoder.setMaxObjectSize(transportConfig.getMaxSize());
                TransportProtocolEncoder encoder = new TransportProtocolEncoder();
                encoder.setMaxObjectSize(transportConfig.getMaxSize());
                ch.pipeline().addLast("TransportProtocolDecoder", decoder);
                ch.pipeline().addLast("TransportProtocolEncoder", encoder);

                int intervalSeconds = transportConfig.getHeartbeatIntervalSeconds();
                ch.pipeline().addLast("IdleStateHandler", new IdleStateHandler(0, 0, intervalSeconds));
                ch.pipeline().addLast("NettyClientHandler", new NettyClientHandler());
            }
        });

        clientBoot.remoteAddress(isa);
    }

    /**
    *
    */
    public void incrementRefCount() {
        ++refCount;
    }

    /**
    *
    */
    public void decrementRefCount() {
        if (refCount > 0) {
            --refCount;
        }
    }

    /**
     * @return
     */
    public boolean isZeroRefCount() {
        return refCount == 0;
    }

    public void dispose() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public Channel createSession() {
        ChannelFuture cf = clientBoot.connect().awaitUninterruptibly();
        Channel channel = cf.channel();
        LOG.debug("session connect {} to {}", channel.localAddress(), channel.remoteAddress());
        return channel;
    }
}
