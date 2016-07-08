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

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.transport.ResultFuture;

public class NettyClientHandler extends ChannelHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * {@inheritDoc}
     * 
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SessionUtil.setResultFutureMap(ctx.channel());
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
        Map<Integer, ResultFuture> cfMap = SessionUtil.getResultFutureMap(ctx.channel());
        Response response = (Response) msg;
        ResultFuture future = cfMap.remove(response.getMessageId());
        if (future != null) {
            future.setResult(response.getResult());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        LOG.error("Unhandled Exception", cause);
        ctx.close();
    }

}
