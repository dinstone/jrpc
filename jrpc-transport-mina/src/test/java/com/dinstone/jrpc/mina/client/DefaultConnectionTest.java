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
package com.dinstone.jrpc.mina.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dinstone.jrpc.cases.HelloService;
import com.dinstone.jrpc.cases.HelloServiceImpl;
import com.dinstone.jrpc.mina.MinaServer;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ResultFuture;
import com.dinstone.jrpc.transport.ResultFutureListener;
import com.dinstone.jrpc.transport.TransportConfig;
import com.dinstone.jrpc.transport.mina.MinaConnectionFactory;

/**
 * @author guojf
 * @version 1.0.0.2013-11-5
 */
public class DefaultConnectionTest {

    private static MinaServer server;

    private Connection connect;

    @BeforeClass
    public static void startServer() {
        server = new MinaServer("localhost", 1234);
        server.regist(HelloService.class, new HelloServiceImpl());
        server.start();
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MinaConnectionFactory connectionFactory = new MinaConnectionFactory();
        connect = connectionFactory.create(new TransportConfig(), new InetSocketAddress("localhost", 1234));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        connect.destroy();
    }

    /**
     * Test method for
     * {@link com.dinstone.jrpc.mina.transport.rpc.mina.client.MinaConnection#call(java.lang.String, java.lang.Object[], java.lang.Class)}
     * .
     */
    @Test
    public void testCall() {
        long st = System.currentTimeMillis();

        ResultFuture cf = connect.call(
            new Call("com.dinstone.jrpc.cases.HelloService", "", 3000, "sayHello", new Object[] { "dddd" }, null));
        try {
            cf.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, " + (1 * 1000 / et) + " tps");
    }

    @Test
    public void testCall01() throws InterruptedException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }
        String name = new String(mb);

        long st = System.currentTimeMillis();

        final Semaphore s = new Semaphore(0);
        ResultFutureListener listener = new ResultFutureListener() {

            @Override
            public void complete(ResultFuture future) {
                s.release();
            }
        };

        int count = 10000;
        for (int i = 0; i < count; i++) {
            ResultFuture f = connect.call(
                new Call("com.dinstone.jrpc.cases.HelloService", "", 3000, "sayHello", new Object[] { name }, null));
            f.addListener(listener);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("write time " + et + " ms");

        s.acquire(count);
        et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }
}
