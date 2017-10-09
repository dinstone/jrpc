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
package com.dinstone.jrpc.example.registry;

import java.util.Properties;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.ClientBuilder;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.example.HelloService;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServiceConsumer {

    public static void main(String[] args) {
        ClientBuilder builder = new ClientBuilder();
        EndpointConfig endpointConfig = new EndpointConfig();
        endpointConfig.setEndpointId("consumer-1").setEndpointName("example-registry-consumer");
        builder.endpointConfig(endpointConfig);

        RegistryConfig registryConfig = new RegistryConfig();
        Properties props = new Properties();
        props.setProperty("zookeeper.node.list", "localhost:2181");
        registryConfig.setSchema("zookeeper").setProperties(props);
        builder.registryConfig(registryConfig);

        TransportConfig transportConfig = new TransportConfig();
        props = new Properties();
        props.setProperty("rpc.handler.count", "2");
        transportConfig.setSchema("netty").setProperties(props);
        builder.transportConfig(transportConfig);

        Client client = builder.build();

        try {
            HelloService helloService = client.importService(HelloService.class);

            testHot(helloService);

            testSend1k(helloService);
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

    public static void testSend1k(HelloService service) {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        long st = System.currentTimeMillis();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }

}
