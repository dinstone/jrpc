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

package com.dinstone.jrpc.mina.transport;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.serialize.SerializeType;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.TransportConfig;

public class MinaConnection implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(MinaConnection.class);

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
        } catch (RuntimeException e) {
            destroy();
            throw e;
        }
    }

    public ResultFuture call(Call call) {
        final int id = ID_GENERATOR.incrementAndGet();
        Map<Integer, ResultFuture> futureMap = SessionUtil.getResultFutureMap(ioSession);
        final ResultFuture callFuture = new ResultFuture();
        futureMap.put(id, callFuture);

        WriteFuture wf = ioSession.write(new Request(id, serializeType, call));
        wf.addListener(new IoFutureListener<WriteFuture>() {

            public void operationComplete(WriteFuture future) {
                if (!future.isWritten()) {
                    callFuture.setResult(new Result(500, "connection is closed"));
                }
            }

        });

        return callFuture;
    }

    public void close() {
        if (ioSession != null) {
            ioSession.close(true);
            LOG.info("session {} closed", ioSession.getId());
        }
    }

    public boolean isAlive() {
        return ioSession.isConnected() && !ioSession.isClosing();
    }

    @Override
    public void destroy() {
        if (ioSession != null) {
            ioSession.close(true);
            LOG.info("session {} closed", ioSession.getId());
            ioSession = null;
        }

        if (connector != null) {
            connector.dispose();
            connector = null;
        }

    }

}
