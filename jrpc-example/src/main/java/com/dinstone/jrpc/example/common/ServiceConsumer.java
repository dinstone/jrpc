
package com.dinstone.jrpc.example.common;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.ServiceImporter;
import com.dinstone.jrpc.example.HelloService;

public class ServiceConsumer {

    public static void main(String[] args) {
        Client client = new Client("localhost", 4444);
        ServiceImporter serviceImporter = client.getServiceImporter();
        HelloService helloService = serviceImporter.importService(HelloService.class);

        testHot(helloService);

        testSend1k(helloService);

        client.destroy();
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
