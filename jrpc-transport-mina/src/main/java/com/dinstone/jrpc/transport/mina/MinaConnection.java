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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.TransportConfig;

public class MinaConnection implements Connection {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private MinaConnector connector;

    private IoSession ioSession;

    private SerializeType serializeType;

    public MinaConnection(String host, int port, TransportConfig config) {
        this(new InetSocketAddress(host, port), config);
    }

    public MinaConnection(InetSocketAddress isa, TransportConfig config) {
        serializeType = config.getSerializeType();

        try {
            connector = new MinaConnector(isa, config);
            ioSession = connector.createSession();
            SessionUtil.setResultFutureMap(ioSession);
        } catch (RuntimeException e) {
            destroy();

            throw e;
        }
    }

    @Override
    public ResultFuture call(Call call) {
        final int id = ID_GENERATOR.incrementAndGet();
        Map<Integer, ResultFuture> futureMap = SessionUtil.getResultFutureMap(ioSession);
        final ResultFuture resultFuture = new ResultFuture();
        futureMap.put(id, resultFuture);

        WriteFuture wf = ioSession.write(new Request(id, serializeType, call));
        wf.addListener(new IoFutureListener<WriteFuture>() {

            @Override
            public void operationComplete(WriteFuture future) {
                if (!future.isWritten()) {
                    resultFuture.setResult(new Result(500, "can't write request"));
                }
            }

        });

        return resultFuture;
    }

    @Override
    public boolean isAlive() {
        return ioSession.isConnected() && !ioSession.isClosing();
    }

    @Override
    public void destroy() {
        if (ioSession != null) {
            ioSession.close(true);
        }

        if (connector != null) {
            connector.dispose();
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return connector.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) ioSession.getLocalAddress();
    }

}
