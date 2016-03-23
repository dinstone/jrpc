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

import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.transport.ResultFuture;

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
        Map<Integer, ResultFuture> futureMap = SessionUtil.getCallFutureMap(session);
        for (ResultFuture future : futureMap.values()) {
            future.setResult(new Result(400, "connection is closed"));
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
        Map<Integer, ResultFuture> cfMap = SessionUtil.getCallFutureMap(session);
        ResultFuture future = cfMap.remove(response.getMessageId());
        if (future != null) {
            future.setResult(response.getResult());
        }
    }

}
