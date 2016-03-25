
package com.dinstone.jrpc.reference;

import java.io.IOException;

import com.dinstone.jrpc.api.DefaultServiceExporter;
import com.dinstone.jrpc.cluster.RegistryImplementBinding;
import com.dinstone.jrpc.demo.HelloService;
import com.dinstone.jrpc.demo.HelloServiceImpl;
import com.dinstone.jrpc.mina.server.MinaAcceptance;
import com.dinstone.jrpc.processor.ImplementBinding;
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
