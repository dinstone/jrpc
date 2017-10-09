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
package com.dinstone.jrpc.example;

import java.util.concurrent.CountDownLatch;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.ClientBuilder;
import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServerBuilder;
import com.dinstone.jrpc.transport.TransportConfig;

public class JrpcStressTest {

    public static void main(String[] args) throws Exception {
        int dataLength = 1024;
        int parallel = 32;
        int conPollSize = 2;
        int nioSize = 2;
        int businessSize = 4;
        if (args.length == 5) {
            dataLength = Integer.parseInt(args[0]);
            parallel = Integer.parseInt(args[1]);
            conPollSize = Integer.parseInt(args[2]);
            nioSize = Integer.parseInt(args[3]);
            businessSize = Integer.parseInt(args[4]);
        } else if (args.length > 0 && args.length < 5) {
            System.out.println("Usage: data-length parallel connection-poll-size server-nio-size server-business-size");
            System.out.println("example: 1024 32 2 2 4");

            System.exit(-1);
        }

        caseTemplate("mina", "mina", dataLength, parallel, conPollSize, nioSize, businessSize);
        // caseTemplate("netty", "netty", dataLength, parallel, conPollSize, nioSize, businessSize);
        // caseTemplate("mina", "mina");
        // caseTemplate(nettySchema, "mina");
        // caseTemplate("mina", nettySchema);
    }

    protected static void caseTemplate(String serverSchema, String clientSchema, int dataLength, int parallel,
            int conPollSize, int nioSize, int businessSize) throws Exception {
        MetricService metricService = new MetricService();

        Server server = createServer(serverSchema, nioSize, businessSize);
        server.exportService(HelloService.class, new HelloServiceImpl(metricService));

        Client client = createClient(clientSchema, conPollSize);
        HelloService helloService = client.importService(HelloService.class);

        try {
            testHot(helloService);

            System.out.println("Case S/C[" + serverSchema + ":" + clientSchema + "] dataLength[" + dataLength
                    + "] parallel[" + parallel + "] start");

            testMultiThread(helloService, dataLength, parallel);

            System.out.println("Case S/C[" + serverSchema + ":" + clientSchema + "] dataLength[" + dataLength
                    + "] parallel[" + parallel + "] end");

        } finally {
            client.destroy();
        }

        server.stop();

        metricService.destory();
    }

    protected static Client createClient(String schema, int conPollSize) {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setSchema(schema).setConnectPoolSize(conPollSize);

        ClientBuilder builder = new ClientBuilder().bind("localhost", 4444);
        Client client = builder.transportConfig(transportConfig).build();

        return client;
    }

    protected static Server createServer(String schema, int nioSize, int businessSize) {
        TransportConfig config = new TransportConfig().setSchema(schema).setNioProcessorCount(nioSize)
            .setBusinessProcessorCount(businessSize);

        return new ServerBuilder().transportConfig(config).bind("localhost", 4444).build().start();
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
