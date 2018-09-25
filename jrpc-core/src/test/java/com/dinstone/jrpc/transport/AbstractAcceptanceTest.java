/*
 * Copyright (C) 2013~2017 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.transport;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.demo.DemoService;
import com.dinstone.jrpc.demo.DemoServiceImpl;
import com.dinstone.jrpc.invoker.LocalInvocationHandler;
import com.dinstone.jrpc.invoker.ServiceInvoker;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.serializer.SerializeType;

public class AbstractAcceptanceTest {

    private AbstractAcceptance acceptance;

    @Before
    public void setUp() throws Exception {
        ImplementBinding iBinding = new DefaultImplementBinding(null, null, new InetSocketAddress("localhost", 0));

        LocalInvocationHandler invocationHandler = new LocalInvocationHandler(iBinding);
        ServiceInvoker serviceInvoker = new ServiceInvoker(invocationHandler);
        ServiceProxyFactory factory = new ServiceProxyFactory(serviceInvoker);

        ServiceProxy<DemoService> wrapper = factory.create(DemoService.class, "", 3000, new DemoServiceImpl());
        iBinding.bind(wrapper, null);

        acceptance = new AbstractAcceptance(serviceInvoker, null, null) {

            @Override
            public void destroy() {
                // TODO Auto-generated method stub

            }

            @Override
            public Acceptance bind() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testHandle() {
        Request request = new Request(1234, SerializeType.JACKSON, new Call(DemoService.class.getName(), "", 3000,
            "hello", new Object[] { "dinstone" }, new Class<?>[] { String.class }));
        Response response = acceptance.handle(request);

        System.out.println(response);
        assertEquals(response.getResult().getCode(), 200);
    }

    @Test
    public void testHandle1() {
        Request request = new Request(12345, SerializeType.JACKSON, new Call(DemoService.class.getName(), "", 3000,
            "hello", new Object[] { "guojinfei", 34 }, new Class<?>[] { String.class, int.class }));
        Response response = acceptance.handle(request);

        System.out.println(response);
        assertEquals(response.getResult().getCode(), 200);
    }

    @Test
    public void testHandle2() {
        Request request = new Request(12345, SerializeType.JACKSON,
            new Call(DemoService.class.getName(), "", 3000, "hello", new Object[] { "guojinfei", (int) 34 }, null));
        Response response = acceptance.handle(request);

        System.out.println(response);
        assertEquals(response.getResult().getCode(), 405);
    }

    @Test
    public void testHandle3() {
        Request request = new Request(12345, SerializeType.JACKSON,
            new Call(DemoService.class.getName(), "", 3000, "hello", null, null));
        Response response = acceptance.handle(request);

        System.out.println(response);
        assertEquals(response.getResult().getCode(), 405);
    }

}
