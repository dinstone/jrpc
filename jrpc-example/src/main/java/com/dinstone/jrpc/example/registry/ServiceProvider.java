
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
