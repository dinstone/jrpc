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

package com.dinstone.jrpc.mina.client;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.client.CallFuture;
import com.dinstone.jrpc.client.Connection;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.serialize.SerializeType;

public class MinaConnection implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(MinaConnection.class);

    private static final AtomicInteger IDGEN = new AtomicInteger();

    private IoSession ioSession;

    private SerializeType serializeType;

    public MinaConnection(IoSession ioSession, Configuration config) {
        this.ioSession = ioSession;
        this.serializeType = config.getSerializeType();

        LOG.info("session {} created", ioSession.getId());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.client.Connection#call(com.dinstone.jrpc.protocol.Call)
     */
    public CallFuture call(Call call) {
        final int id = getId();
        Map<Integer, CallFuture> futureMap = SessionUtil.getCallFutureMap(ioSession);
        final CallFuture callFuture = new CallFuture();
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

    /**
     * @return
     */
    private int getId() {
        return IDGEN.incrementAndGet();
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

}
