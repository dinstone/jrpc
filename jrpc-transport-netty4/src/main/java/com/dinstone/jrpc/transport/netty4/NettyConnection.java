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

package com.dinstone.jrpc.transport.netty4;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.TransportConfig;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyConnection implements Connection {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private SerializeType serializeType;

    private Channel channel;

    public NettyConnection(Channel channel, TransportConfig transportConfig) {
        this.channel = channel;
        SessionUtil.setResultFutureMap(channel);

        this.serializeType = transportConfig.getSerializeType();
    }

    @Override
    public ResultFuture call(Call call) {
        final int id = ID_GENERATOR.incrementAndGet();
        final ResultFuture resultFuture = new ResultFuture();
        SessionUtil.getResultFutureMap(channel).put(id, resultFuture);

        ChannelFuture wf = channel.writeAndFlush(new Request(id, serializeType, call));
        wf.addListener(new GenericFutureListener<ChannelFuture>() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    resultFuture.setResult(new Result(500, "can't write request"));
                    SessionUtil.getResultFutureMap(channel).remove(id);
                }
            }

        });

        return resultFuture;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public boolean isAlive() {
        return channel.isActive();
    }

    @Override
    public void destroy() {
        if (channel != null) {
            channel.close();
        }
    }

}
