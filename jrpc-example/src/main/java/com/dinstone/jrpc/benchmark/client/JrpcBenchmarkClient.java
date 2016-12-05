/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
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

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.ClientBuilder;
import com.dinstone.jrpc.benchmark.BenchmarkService;

public class JrpcBenchmarkClient extends AbstractBenchmarkClient {

    private Configuration caseConfig;

    private BenchmarkService service;

    private Client client;

    public JrpcBenchmarkClient(Configuration caseConfig) {
        this.caseConfig = caseConfig;
    }

    @Override
    protected void init() {
        concurrents = caseConfig.getInt("concurrents", 10);
        params = caseConfig.get("params", "1");
        runTime = caseConfig.getInt("runTimeSeconds", 90);
        classname = caseConfig.get("caseClassName", StringCaseRunnable.class.getName());

        String schema = caseConfig.get("schema", "netty");
        int conPollSize = caseConfig.getInt("connectPoolSize", 10);
        ClientBuilder builder = new ClientBuilder().bind("localhost", 4444);
        builder.transportConfig().setSchema(schema).setConnectPoolSize(conPollSize);

        client = builder.build();
        service = client.importService(BenchmarkService.class);
    }

    @Override
    protected CaseRunnable getCaseRunnable(CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime) {
        Class[] parameterTypes = new Class[] { BenchmarkService.class, String.class, CyclicBarrier.class,
                CountDownLatch.class, long.class, long.class };
        Object[] parameters = new Object[] { service, params, barrier, latch, startTime, endTime };

        CaseRunnable clientRunnable = null;
        try {
            clientRunnable = (CaseRunnable) Class.forName(classname).getConstructor(parameterTypes)
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
        System.out.println("Usage:[Config-File]");
        System.out.println("Usage:[Concurrents] [Params]");
        System.out.println("Usage:[Concurrents] [Params] [TransportSchema]");

        Configuration caseConfig = new Configuration();
        if (args.length == 1) {
            caseConfig = new Configuration(args[0]);
        } else if (args.length == 2) {
            caseConfig.setInt("concurrents", Integer.parseInt(args[0]));
            caseConfig.setInt("params", Integer.parseInt(args[1]));
        } else if (args.length == 3) {
            caseConfig.setInt("concurrents", Integer.parseInt(args[0]));
            caseConfig.setInt("params", Integer.parseInt(args[1]));
            caseConfig.set("schema", args[2]);
        }

        JrpcBenchmarkClient benchmarkClient = new JrpcBenchmarkClient(caseConfig);
        benchmarkClient.execute();
    }
}
