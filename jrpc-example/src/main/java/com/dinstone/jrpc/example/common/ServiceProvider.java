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

import java.io.IOException;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServerBuilder;
import com.dinstone.jrpc.example.HelloService;
import com.dinstone.jrpc.example.HelloServiceImpl;
import com.dinstone.jrpc.example.MetricService;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServiceProvider {

    public static void main(String[] args) throws IOException {

        TransportConfig transportConfig = new TransportConfig().setSchema("netty").setMaxConnectionCount(10)
            .setBusinessProcessorCount(5);
        Server server = new ServerBuilder().transportConfig(transportConfig).bind("localhost", 4444).build();

        server.start();

        MetricService metricService = new MetricService();
        server.exportService(HelloService.class, new HelloServiceImpl(metricService));

        System.in.read();

        server.stop();
        metricService.destory();
    }

}
