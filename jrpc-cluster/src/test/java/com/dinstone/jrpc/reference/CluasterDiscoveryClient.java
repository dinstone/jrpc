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
package com.dinstone.jrpc.reference;

import java.io.IOException;

import com.dinstone.jrpc.api.DefaultServiceImporter;
import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.cluster.DiscoveryReferenceBinding;
import com.dinstone.jrpc.demo.HelloService;
import com.dinstone.jrpc.mina.transport.MinaConnectionFactory;
import com.dinstone.jrpc.srd.RegistryDiscoveryConfig;
import com.dinstone.jrpc.transport.ConnectionFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class CluasterDiscoveryClient {

    public static void main(String[] args) {
        RegistryDiscoveryConfig discoveryConfig = new RegistryDiscoveryConfig();
        discoveryConfig.set("zookeeper.node.list", "localhost:2181");

        ReferenceBinding referenceBinding = new DiscoveryReferenceBinding(discoveryConfig);
        ConnectionFactory connectionFactory = new MinaConnectionFactory(new TransportConfig());
        DefaultServiceImporter serviceImporter = new DefaultServiceImporter(referenceBinding, connectionFactory);

        try {
            testHot(serviceImporter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            testSend1k(serviceImporter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        serviceImporter.destroy();
        referenceBinding.destroy();
        connectionFactory.destroy();
    }

    protected static void testHot(DefaultServiceImporter serviceImporter) {
        HelloService service = serviceImporter.importService(HelloService.class);
        long st = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            service.sayHello("dinstone");
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("hot takes " + et + "ms, " + (100000 * 1000 / et) + " tps");
    }

    public static void testSend1k(DefaultServiceImporter serviceImporter) throws IOException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        HelloService service = serviceImporter.importService(HelloService.class);

        long st = System.currentTimeMillis();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }
}
