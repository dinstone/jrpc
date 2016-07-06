
package com.dinstone.jrpc.example.common;

import java.io.IOException;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServiceExporter;
import com.dinstone.jrpc.example.HelloService;
import com.dinstone.jrpc.example.HelloServiceImpl;

public class ServiceProvider {

    public static void main(String[] args) throws IOException {
        // Server server = new Server("-:4444");
        // Server server = new Server("-", 4444);
        Server server = new Server("localhost", 4444);
        ServiceExporter serviceExporter = server.getServiceExporter();
        serviceExporter.exportService(HelloService.class, new HelloServiceImpl());

        System.in.read();

        server.destroy();
    }

}
