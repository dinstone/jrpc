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

import com.dinstone.jrpc.api.DefaultServiceExporter;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.cluster.RegistryImplementBinding;
import com.dinstone.jrpc.demo.HelloService;
import com.dinstone.jrpc.demo.HelloServiceImpl;
import com.dinstone.jrpc.mina.server.MinaAcceptance;
import com.dinstone.jrpc.srd.RegistryDiscoveryConfig;
import com.dinstone.jrpc.transport.TransportConfig;

public class ClusterRegistryServer {

    public static void main(String[] args) {

        RegistryDiscoveryConfig registryConfig = new RegistryDiscoveryConfig();
        registryConfig.set("zookeeper.node.list", "localhost:2181");
        ImplementBinding implementBinding = new RegistryImplementBinding("localhost", 9090, registryConfig);

        DefaultServiceExporter exporter = new DefaultServiceExporter(implementBinding);
        exporter.exportService(HelloService.class, "", 2000, new HelloServiceImpl());

        MinaAcceptance acceptance = new MinaAcceptance(new TransportConfig(), implementBinding);
        acceptance.bind();
        
        System.out.println("server start");

        try {
            System.in.read();
        } catch (IOException e) {
        }

        exporter.destroy();
        implementBinding.destroy();
        acceptance.destroy();

        System.out.println("server stop");
    }

}
