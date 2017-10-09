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
package com.dinstone.jrpc.transport.netty5;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.protocol.Tick;
import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.TransportConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultExecutorServiceFactory;

public class NettyConnector {

    private static final Logger LOG = LoggerFactory.getLogger(NettyConnector.class);

    private int refCount;

    private NioEventLoopGroup workerGroup;

    private Bootstrap clientBoot;

    public NettyConnector(InetSocketAddress isa, final TransportConfig transportConfig) {
        workerGroup = new NioEventLoopGroup(1, new DefaultExecutorServiceFactory("N5C-Work"));
        clientBoot = new Bootstrap().group(workerGroup).channel(NioSocketChannel.class);
        clientBoot.option(ChannelOption.TCP_NODELAY, true);
        clientBoot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, transportConfig.getConnectTimeout());
        clientBoot.option(ChannelOption.SO_RCVBUF, 8 * 1024).option(ChannelOption.SO_SNDBUF, 8 * 1024);
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
                ch.pipeline().addLast("IdleStateHandler", new IdleStateHandler(0, intervalSeconds, 0));
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
        Channel channel = clientBoot.connect().awaitUninterruptibly().channel();
        LOG.debug("session connect {} to {}", channel.localAddress(), channel.remoteAddress());
        return channel;
    }

    public class NettyClientHandler extends ChannelHandlerAdapter {

        private Heartbeat heartbeat = new Heartbeat(0, SerializeType.JACKSON, new Tick());

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.WRITER_IDLE) {
                    heartbeat.getContent().increase();
                    ctx.writeAndFlush(heartbeat);
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.netty.channel.ChannelHandlerContext)
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Map<Integer, ResultFuture> futureMap = SessionUtil.getResultFutureMap(ctx.channel());
            for (ResultFuture future : futureMap.values()) {
                future.setResult(new Result(400, "connection is closed"));
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Response) {
                Response response = (Response) msg;
                Map<Integer, ResultFuture> cfMap = SessionUtil.getResultFutureMap(ctx.channel());
                ResultFuture future = cfMap.remove(response.getMessageId());
                if (future != null) {
                    future.setResult(response.getResult());
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            LOG.error("Unhandled Exception", cause);
            ctx.close();
        }

    }
}
