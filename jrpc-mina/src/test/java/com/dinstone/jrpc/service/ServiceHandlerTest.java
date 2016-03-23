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

package com.dinstone.jrpc.service;

import org.junit.Test;

import com.dinstone.jrpc.cases.HelloService;
import com.dinstone.jrpc.cases.HelloServiceImpl;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.serialize.SerializeType;

public class ServiceHandlerTest {

    @Test
    public void handle() throws Exception {
        ServiceHandler serviceHandler = new DefaultServiceHandler();
        serviceHandler.regist(HelloService.class, new HelloServiceImpl());

        Request request = getRequest();

        long st = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            serviceHandler.handle(request);
        }
        long et = System.currentTimeMillis() - st;

        System.out.println("it takes " + et + "ms, " + (10000 * 1000 / et) + " tps");
    }

    private Request getRequest() {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }
        final String name = new String(mb);

        return new Request(1, SerializeType.JACKSON, new Call("com.dinstone.jrpc.cases.HelloService", "", 3000,
            "sayHello", new Object[] { name }));
    }
}
