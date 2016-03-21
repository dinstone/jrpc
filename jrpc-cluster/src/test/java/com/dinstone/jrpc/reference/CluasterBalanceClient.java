
package com.dinstone.jrpc.reference;

import java.io.IOException;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.client.Client;
import com.dinstone.jrpc.cluster.ClusterServiceInvoker;
import com.dinstone.jrpc.demo.HelloService;
import com.dinstone.jrpc.invoker.ServiceInvoker;
import com.dinstone.jrpc.mina.client.MinaClient;
import com.dinstone.jrpc.mina.client.MinaConnectionManager;

public class CluasterBalanceClient {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.set("zookeeper.node.list", "localhost:2181");

        MinaConnectionManager connectionManager = new MinaConnectionManager();
        ServiceInvoker serviceInvoker = new ClusterServiceInvoker(config, connectionManager);
        MinaClient client = new MinaClient(serviceInvoker);

        System.out.println("start");

        try {
            testHot(client);
            testSend1k(client);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.in.read();
        } catch (IOException e) {
        }

        System.out.println("stop");
        client.destroy();
        serviceInvoker.destroy();
        connectionManager.destroy();
    }

    protected static void testHot(Client client) {
        HelloService service = client.getService(HelloService.class);
        long st = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            service.sayHello("dinstone");
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("hot takes " + et + "ms, " + (100000 * 1000 / et) + " tps");
    }

    public static void testSend1k(Client client) throws IOException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        HelloService service = client.getService(HelloService.class);

        long st = System.currentTimeMillis();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }
}
