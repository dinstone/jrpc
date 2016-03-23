
package com.dinstone.jrpc.reference;

import java.io.IOException;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.cluster.ClusterServiceProcessor;
import com.dinstone.jrpc.demo.HelloService;
import com.dinstone.jrpc.demo.HelloServiceImpl;
import com.dinstone.jrpc.mina.server.MinaServer;
import com.dinstone.jrpc.processor.ServiceProcessor;

public class ClusterRegistryServer {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.set("zookeeper.node.list", "localhost:2181");
        config.setServiceHost("127.0.0.1");
        config.setServicePort(1234);

        ServiceProcessor serviceProcessor = new ClusterServiceProcessor(config);
        MinaServer server = new MinaServer(config, serviceProcessor);
        server.regist(HelloService.class, new HelloServiceImpl());
        server.start();

        System.out.println("server start");

        try {
            System.in.read();
        } catch (IOException e) {
        }

        server.shutdown();
        serviceProcessor.destroy();
        
        System.out.println("server stop");
    }

}
