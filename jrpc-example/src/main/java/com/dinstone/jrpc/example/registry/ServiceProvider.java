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

package com.dinstone.jrpc.example.registry;

import java.io.IOException;
import java.util.Properties;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServiceExporter;
import com.dinstone.jrpc.example.HelloService;
import com.dinstone.jrpc.example.HelloServiceImpl;

public class ServiceProvider {

    public static void main(String[] args) throws IOException {
        // Server server = new Server("-:4444");
        // Server server = new Server("-", 4444);
        Server server = new Server("localhost", 4444);
        server.getEndpointConfig().setEndpointId("provider-1");
        server.getEndpointConfig().setEndpointName("example-registry-provider");

        server.getRegistryConfig().setSchema("zookeeper");
        Properties other = new Properties();
        other.setProperty("zookeeper.node.list", "localhost:2181");
        server.getRegistryConfig().setProperties(other);

        server.getTransportConfig().setSchema("mina");
        other = new Properties();
        other.setProperty("rpc.parallel.count", "2");
        server.getTransportConfig().setProperties(other);

        try {
            ServiceExporter serviceExporter = server.getServiceExporter();
            serviceExporter.exportService(HelloService.class, new HelloServiceImpl());

            System.in.read();
        } finally {
            server.destroy();
        }
    }

}
