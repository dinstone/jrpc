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
package com.dinstone.jrpc.example.common;

import java.util.concurrent.CountDownLatch;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.ClientBuilder;
import com.dinstone.jrpc.example.HelloService;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServiceConsumer {

    public static void main(String[] args) throws Exception {
        ClientBuilder builder = new ClientBuilder().bind("localhost", 4444);
        builder.transportConfig(new TransportConfig().setSchema("netty").setConnectPoolSize(2));

        Client client = builder.build();
        HelloService helloService = client.importService(HelloService.class);

        try {
            testHot(helloService);

            testMultiThread(helloService, 500, 1);

            testMultiThread(helloService, 500, 10);

            testMultiThread(helloService, 500, 20);

            testMultiThread(helloService, 500, 32);

            testMultiThread(helloService, 1 * 1024, 1);

            testMultiThread(helloService, 1 * 1024, 10);

            testMultiThread(helloService, 1 * 1024, 20);

            testMultiThread(helloService, 1 * 1024, 32);

            testMultiThread(helloService, 1 * 1024, 40);

            testMultiThread(helloService, 5 * 1024, 1);

            testMultiThread(helloService, 5 * 1024, 10);

            testMultiThread(helloService, 5 * 1024, 20);

            testMultiThread(helloService, 5 * 1024, 32);

            testMultiThread(helloService, 5 * 1024, 40);

        } finally {
            client.destroy();
        }
    }

    protected static void testHot(HelloService service) {
        long st = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            service.sayHello("dinstone");
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("hot takes " + et + "ms, " + (100000 * 1000 / et) + " tps");
    }

    public static void testMultiThread(final HelloService service, final int dataLength, int threadCount)
            throws Exception {
        // int dataLength = 8 * 1024;
        byte[] mb = new byte[dataLength];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }
        final String name = new String(mb);

        final int loopCount = 10000;

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread() {

                /**
                 * {@inheritDoc}
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    try {
                        // long st = System.currentTimeMillis();

                        for (int i = 0; i < loopCount; i++) {
                            service.sayHello(name);
                        }

                        // long et = System.currentTimeMillis() - st;
                        // System.out.println(et + " ms, " + dataLength + " B : " + (loopCount * 1000 / et) + " tps");
                    } finally {
                        endLatch.countDown();
                    }
                }
            };
            t.start();
        }

        startLatch.countDown();
        long st = System.currentTimeMillis();
        endLatch.await();
        long et = System.currentTimeMillis() - st;

        System.out.println(threadCount + " Threads,\t" + dataLength + " Bytes,\t" + et + " ms,\t" + "AVG: "
                + (threadCount * loopCount * 1000 / et) + " tps");
    }

}
