
package com.dinstone.jrpc.reference;

import java.io.IOException;

import com.dinstone.jrpc.api.ServiceImporter;
import com.dinstone.jrpc.cluster.DiscoveryReferenceBinding;
import com.dinstone.jrpc.demo.HelloService;
import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.mina.client.MinaConnectionFactory;
import com.dinstone.jrpc.srd.RegistryDiscoveryConfig;
import com.dinstone.jrpc.transport.ConnectionFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class CluasterDiscoveryClient {

    public static void main(String[] args) {
        RegistryDiscoveryConfig discoveryConfig = new RegistryDiscoveryConfig();
        discoveryConfig.set("zookeeper.node.list", "localhost:2181");

        ReferenceBinding referenceBinding = new DiscoveryReferenceBinding(discoveryConfig);
        ConnectionFactory connectionFactory = new MinaConnectionFactory(new TransportConfig());
        ServiceImporter serviceImporter = new ServiceImporter(referenceBinding, connectionFactory);

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

    protected static void testHot(ServiceImporter serviceImporter) {
        HelloService service = serviceImporter.getService(HelloService.class);
        long st = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            service.sayHello("dinstone");
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("hot takes " + et + "ms, " + (100000 * 1000 / et) + " tps");
    }

    public static void testSend1k(ServiceImporter serviceImporter) throws IOException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        HelloService service = serviceImporter.getService(HelloService.class);

        long st = System.currentTimeMillis();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }
}
