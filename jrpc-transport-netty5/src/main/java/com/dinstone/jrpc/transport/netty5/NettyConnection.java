/*
 * Copyright (C) 2012~2014 dinstone<dinstone@163.com>
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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.TransportConfig;

public class NettyConnection implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(NettyConnection.class);

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private SerializeType serializeType;

    private NettyConnector connector;

    private Channel ioSession;

    public NettyConnection(String host, int port, TransportConfig config) {
        this(new InetSocketAddress(host, port), config);
    }

    public NettyConnection(InetSocketAddress isa, TransportConfig config) {
        connector = new NettyConnector(isa, config);
        ioSession = connector.createSession();
        serializeType = config.getSerializeType();
    }

    public ResultFuture call(Call call) {
        final int id = ID_GENERATOR.incrementAndGet();
        Map<Integer, ResultFuture> futureMap = SessionUtil.getResultFutureMap(ioSession);
        final ResultFuture resultFuture = new ResultFuture();
        futureMap.put(id, resultFuture);

        ChannelFuture wf = ioSession.writeAndFlush(new Request(id, serializeType, call));
        wf.addListener(new GenericFutureListener<ChannelFuture>() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    resultFuture.setResult(new Result(500, "can't write request"));
                }
            }

        });

        return resultFuture;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) ioSession.remoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) ioSession.localAddress();
    }

    @Override
    public boolean isAlive() {
        return ioSession.isActive();
    }

    @Override
    public void destroy() {
        if (ioSession != null) {
            ioSession.close();
            LOG.debug("session closed {} to {}", ioSession.localAddress(), ioSession.remoteAddress());
        }

        if (connector != null) {
            connector.dispose();
        }
    }

}
