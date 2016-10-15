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

package com.dinstone.jrpc.transport.netty4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Heartbeat;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.protocol.Tick;
import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.transport.ResultFuture;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    private Heartbeat heartbeat = new Heartbeat(0, SerializeType.JACKSON, new Tick());

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
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
