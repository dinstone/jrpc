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

package com.dinstone.jrpc.benchmark.server;

import java.io.IOException;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServerBuilder;
import com.dinstone.jrpc.benchmark.BenchmarkService;
import com.dinstone.jrpc.example.MetricService;

public class JrpcBenchmarkServer {

    public static void main(String[] args) throws IOException {
        System.out.println("Usage:[TransportSchema]");
        System.out.println("Usage:[NioProcessorCount] [BusinessProcessorCount]");
        System.out.println("Usage:[NioProcessorCount] [BusinessProcessorCount] [TransportSchema]");

        String schema = "netty";
        int businessCount = 0;
        int nioCount = Runtime.getRuntime().availableProcessors();
        if (args.length == 1) {
            schema = args[0];
        } else if (args.length == 2) {
            nioCount = Integer.parseInt(args[0]);
            businessCount = Integer.parseInt(args[1]);
        } else if (args.length == 3) {
            nioCount = Integer.parseInt(args[0]);
            businessCount = Integer.parseInt(args[1]);
            schema = args[2];
        }

        System.out.println("NioProcessorCount=" + nioCount + ",BusinessProcessorCount=" + businessCount
                + ",TransportSchema=" + schema);

        ServerBuilder builder = new ServerBuilder().bind("localhost", 4444);
        builder.transportConfig().setSchema(schema).setNioProcessorCount(nioCount)
            .setBusinessProcessorCount(businessCount);
        Server server = builder.build().start();

        MetricService metricService = new MetricService();
        server.exportService(BenchmarkService.class, new BenchmarkServiceImpl(metricService));

        System.in.read();

        server.stop();
        metricService.destory();
    }
}
