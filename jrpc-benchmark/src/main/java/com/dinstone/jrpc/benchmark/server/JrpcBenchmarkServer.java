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

package com.dinstone.jrpc.benchmark.server;

import java.io.IOException;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServerBuilder;
import com.dinstone.jrpc.benchmark.BenchmarkService;
import com.dinstone.jrpc.transport.TransportConfig;

public class JrpcBenchmarkServer {

    public static void main(String[] args) throws IOException {
        System.out.println("Usage:[TransportSchema]");
        System.out.println("Usage:[TransportSchema] [NioProcessorCount] [BusinessProcessorCount]");

        int businessCount = 0;
        int nioCount = Runtime.getRuntime().availableProcessors();
        String transportSchema = "netty";
        if (args.length == 1) {
            transportSchema = args[0];
        } else if (args.length == 3) {
            transportSchema = args[0];
            nioCount = Integer.parseInt(args[1]);
            businessCount = Integer.parseInt(args[2]);
        }

        System.out.println("NioProcessorCount=" + nioCount + ",BusinessProcessorCount=" + businessCount
                + ",TransportSchema=" + transportSchema);

        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setSchema(transportSchema).setNioProcessorCount(nioCount);
        transportConfig.setBusinessProcessorCount(businessCount);

        ServerBuilder builder = new ServerBuilder().bind("localhost", 4444);
        Server server = builder.transportConfig(transportConfig).build().start();

        MetricService metricService = new MetricService();
        server.exportService(BenchmarkService.class, new BenchmarkServiceImpl(metricService));

        System.out.println("please press any key to continue");
        System.in.read();

        server.stop();
        metricService.destory();
    }
}
