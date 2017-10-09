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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dinstone.jrpc.RpcException;
import com.dinstone.jrpc.cases.HelloService;
import com.dinstone.jrpc.cases.HelloServiceImpl;
import com.dinstone.jrpc.cases.SuperInterface;
import com.dinstone.jrpc.mina.MinaClient;
import com.dinstone.jrpc.mina.MinaServer;

/**
 * @author guojf
 * @version 1.0.0.2013-5-2
 */
public class ClientTest {

    private static MinaServer server;

    private static MinaClient client;

    @BeforeClass
    public static void startServer() {
        server = new MinaServer("localhost", 1234);
        server.regist(HelloService.class, new HelloServiceImpl());
        server.start();

        client = new MinaClient("localhost", 1234).setDefaultTimeout(5000);
    }

    @AfterClass
    public static void stopServer() {
        client.destroy();

        if (server != null) {
            server.stop();
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = RpcException.class)
    public void testSendException() {
        SuperInterface service = client.getService(HelloService.class);
        service.sayHello("dinstone", 31);
    }

    @Test
    public void testSend() throws IOException {
        HelloService service = client.getService(HelloService.class);
        service.sayHello("dinstone");
    }

    @Test
    public void testAsyncInvoke() throws Throwable {
        // client.asyncInvoke("com.dinstone.jrpc.cases.HelloService.sayHello", new Object[] { "dddd" }).get();
        //
        // client.asyncInvoke("com.dinstone.jrpc.service.ServiceStats.serviceList", null).get();
    }

    @Test
    public void testHot() throws IOException {
        HelloService service = client.getService(HelloService.class);
        long st = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            service.sayHello("dinstone");
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("hot takes " + et + "ms, " + (100000 * 1000 / et) + " tps");
    }

    @Test
    public void testSend100B() throws IOException {
        byte[] mb = new byte[100];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        HelloService service = client.getService(HelloService.class);

        long st = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 100B : " + (10000 * 1000 / et) + " tps");
    }

    @Test
    public void testSend1k() throws IOException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        HelloService service = client.getService(HelloService.class);

        long st = System.currentTimeMillis();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }

    @Test
    public void testSend2k() throws IOException {
        byte[] mb = new byte[2 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 66;
        }

        String name = new String(mb);

        HelloService service = client.getService(HelloService.class);

        long st = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 2k : " + (10000 * 1000 / et) + " tps");
    }

    @Test
    public void testSend5k() throws IOException {
        byte[] mb = new byte[5 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 67;
        }

        String name = new String(mb);

        HelloService service = client.getService(HelloService.class);

        long st = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 5k : " + (10000 * 1000 / et) + " tps");
    }

    @Test
    public void testSend1k4t() throws IOException, InterruptedException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }
        final String name = new String(mb);

        int count = 4;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Thread t = new Thread() {

                /**
                 * {@inheritDoc}
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    HelloService service = client.getService(HelloService.class);
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    try {
                        long st = System.currentTimeMillis();

                        for (int i = 0; i < 10000; i++) {
                            service.sayHello(name);
                        }

                        long et = System.currentTimeMillis() - st;
                        System.out.println("it takes " + et + "ms, 1k4t : " + (10000 * 1000 / et) + " tps");
                    } finally {
                        end.countDown();

                    }
                }
            };
            t.start();
        }

        start.countDown();
        long st = System.currentTimeMillis();
        end.await();
        long et = System.currentTimeMillis() - st;

        System.out.println("it takes " + et + "ms, avg 1k4t : " + (count * 10000 * 1000 / et) + " tps");
    }

    @Test
    public void testSend2k4t() throws IOException, InterruptedException {
        byte[] mb = new byte[2 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }
        final String name = new String(mb);

        final HelloService service = client.getService(HelloService.class);

        int count = 4;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Thread t = new Thread() {

                /**
                 * {@inheritDoc}
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    try {
                        long st = System.currentTimeMillis();

                        for (int i = 0; i < 10000; i++) {
                            service.sayHello(name);
                        }

                        long et = System.currentTimeMillis() - st;
                        System.out.println("it takes " + et + "ms, 2k4t : " + (10000 * 1000 / et) + "  tps");
                    } finally {
                        end.countDown();

                    }
                }
            };
            t.start();
        }

        start.countDown();
        long st = System.currentTimeMillis();
        end.await();
        long et = System.currentTimeMillis() - st;

        System.out.println("it takes " + et + "ms, avg 2k4t : " + (count * 10000 * 1000 / et) + " tps");
    }

    @Test
    public void testSend1k8t() throws IOException, InterruptedException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }
        final String name = new String(mb);

        final int count = 8;
        final HelloService[] services = new HelloService[count];

        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            services[i] = client.getService(HelloService.class);
            Thread t = new Thread() {

                /**
                 * {@inheritDoc}
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    try {
                        long st = System.currentTimeMillis();

                        for (int i = 0; i < 10000; i++) {
                            getService(i).sayHello(name);
                        }

                        long et = System.currentTimeMillis() - st;
                        System.out.println("it takes " + et + "ms, 1k8t : " + (10000 * 1000 / et) + "  tps");
                    } finally {
                        end.countDown();

                    }
                }

                private HelloService getService(int i) {
                    return services[i % count];
                }
            };
            t.start();
        }

        start.countDown();
        long st = System.currentTimeMillis();
        end.await();
        long et = System.currentTimeMillis() - st;

        System.out.println("it takes " + et + "ms, avg 1k8t : " + (count * 10000 * 1000 / et) + " tps");
    }
}
