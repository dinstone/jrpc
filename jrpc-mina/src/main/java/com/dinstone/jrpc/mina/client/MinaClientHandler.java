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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.RpcException;
import com.dinstone.jrpc.client.CallFuture;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.protocol.Response;

public class MinaClientHandler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(MinaClientHandler.class);

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.mina.core.service.IoHandlerAdapter#sessionCreated(org.apache.mina.core.session.IoSession)
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        SessionUtil.setCallFutureMap(session);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LOG.debug("Session[{}] is closed", session.getId());
        Map<Integer, CallFuture> futureMap = SessionUtil.getCallFutureMap(session);
        for (CallFuture future : futureMap.values()) {
            future.setException(new RuntimeException("connection is closed"));
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.error("Unhandled Exception", cause);
        session.close(true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession,
     *      java.lang.Object)
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        handle(session, (Response) message);
    }

    private void handle(IoSession session, Response response) {
        Map<Integer, CallFuture> cfMap = SessionUtil.getCallFutureMap(session);
        int id = response.getMessageId();
        CallFuture future = cfMap.remove(id);
        if (future != null) {
            try {
                Result result = response.getResult();
                if (result.getCode() != 200) {
                    Throwable fault = (Throwable) result.getData();
                    if (fault == null) {
                        fault = new RpcException(result.getCode(), result.getMessage());
                    }
                    future.setException(fault);
                } else {
                    future.setResult(result.getData());
                }
            } catch (Exception e) {
                LOG.error("Unhandled Exception", e);
                future.setException(new RpcException(400, e.getMessage()));
            }
        }
    }

}
