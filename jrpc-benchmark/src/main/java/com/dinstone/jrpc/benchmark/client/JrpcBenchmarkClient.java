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
package com.dinstone.jrpc.benchmark.client;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.ClientBuilder;
import com.dinstone.jrpc.benchmark.BenchmarkService;
import com.dinstone.jrpc.transport.TransportConfig;

public class JrpcBenchmarkClient extends AbstractBenchmarkClient {

    private BenchmarkService benchmarkService;

    private Client client;

    public JrpcBenchmarkClient(CaseConfig caseConfig) {
        super(caseConfig);
    }

    @Override
    protected void init() {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setSchema(caseConfig.transportSchema).setConnectPoolSize(caseConfig.connectPoolSize);

        client = new ClientBuilder().bind("localhost", 4444).transportConfig(transportConfig).build();
        benchmarkService = client.importService(BenchmarkService.class);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected CaseRunnable getCaseRunnable(CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime) {
        Class[] parameterTypes = new Class[] { BenchmarkService.class, CaseConfig.class, CyclicBarrier.class,
                CountDownLatch.class, long.class, long.class };
        Object[] parameters = new Object[] { benchmarkService, caseConfig, barrier, latch, startTime, endTime };

        CaseRunnable clientRunnable = null;
        try {
            clientRunnable = (CaseRunnable) Class.forName(caseConfig.caseClassName).getConstructor(parameterTypes)
                .newInstance(parameters);
        } catch (InstantiationException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getTargetException();
        }

        return clientRunnable;
    }

    @Override
    protected void destory() {
        client.destroy();
    }

    public static void main(String[] args) {
        System.out.println("Usage:[TransportSchema] [Concurrents]");

        CaseConfig caseConfig = new CaseConfig();
        if (args.length == 2) {
            caseConfig.transportSchema = args[0];
            caseConfig.concurrents = Integer.parseInt(args[1]);
        }

        caseConfig.dataLength = 1024;
        new JrpcBenchmarkClient(caseConfig).execute();

        caseConfig.dataLength = 5 * 1024;
        new JrpcBenchmarkClient(caseConfig).execute();

        caseConfig.dataLength = 10 * 1024;
        new JrpcBenchmarkClient(caseConfig).execute();

        caseConfig.dataLength = 20 * 1024;
        new JrpcBenchmarkClient(caseConfig).execute();

        caseConfig.dataLength = 30 * 1024;
        new JrpcBenchmarkClient(caseConfig).execute();

        caseConfig.dataLength = 50 * 1024;
        new JrpcBenchmarkClient(caseConfig).execute();
    }
}
