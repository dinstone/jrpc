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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.transport.AbstractAcceptance;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.TransportConfig;

public class NettyAcceptance extends AbstractAcceptance {

    private static final Logger LOG = LoggerFactory.getLogger(NettyAcceptance.class);

    private TransportConfig transportConfig;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    public NettyAcceptance(TransportConfig transportConfig, ImplementBinding implementBinding) {
        super(implementBinding);
        this.transportConfig = transportConfig;
    }

    @Override
    public Acceptance bind() {
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(transportConfig.getHandlerCount());

        ServerBootstrap boot = new ServerBootstrap();
        boot.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    TransportProtocolDecoder decoder = new TransportProtocolDecoder();
                    decoder.setMaxObjectSize(transportConfig.getMaxSize());
                    TransportProtocolEncoder encoder = new TransportProtocolEncoder();
                    encoder.setMaxObjectSize(transportConfig.getMaxSize());
                    ch.pipeline().addLast("TransportProtocolDecoder", decoder);
                    ch.pipeline().addLast("TransportProtocolEncoder", encoder);

                    int intervalSeconds = transportConfig.getHeartbeatIntervalSeconds();
                    ch.pipeline().addLast("IdleStateHandler", new IdleStateHandler(0, 0, intervalSeconds * 2));
                    ch.pipeline().addLast("NettyServerHandler", new NettyServerHandler());
                }
            });
        boot.option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_BACKLOG, 128);
        boot.childOption(ChannelOption.SO_RCVBUF, 8 * 1024).childOption(ChannelOption.SO_SNDBUF, 8 * 1024);

        InetSocketAddress serviceAddress = implementBinding.getServiceAddress();
        try {
            boot.bind(serviceAddress).sync();
        } catch (Exception e) {
            throw new RuntimeException("can't bind service on " + serviceAddress, e);
        }
        LOG.info("netty5 acceptance bind on {}", serviceAddress);

        return this;
    }

    @Override
    public void destroy() {
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }

    public class NettyServerHandler extends ChannelHandlerAdapter {

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.ALL_IDLE) {
                    ctx.close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object message) {
            if (message instanceof Request) {
                Response response = handle((Request) message);
                ctx.writeAndFlush(response);
            } else if (message instanceof Heartbeat) {
                // ((Heartbeat) message).getContent().increase();
                ctx.writeAndFlush(message);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("untreated exception", cause);
            ctx.close();
        }

    }

}
