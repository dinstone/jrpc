
package com.dinstone.jrpc.example.spring;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dinstone.jrpc.example.HelloService;

public class ServiceWithSpring {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "application-jrpc-example.xml");

        jackson(applicationContext);
        System.out.println("===========================================================");
        protobuff(applicationContext);

        try {
            System.in.read();
        } catch (IOException e) {
        }

        applicationContext.close();
    }

    protected static void jackson(ClassPathXmlApplicationContext applicationContext) {
        HelloService service = (HelloService) applicationContext.getBean("rhsv1-1");
        System.out.println("jackson");
        try {
            testHot(service);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            testSend1k(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void protobuff(ClassPathXmlApplicationContext applicationContext) {
        HelloService service = (HelloService) applicationContext.getBean("rhsv1");
        System.out.println("protobuff");
        try {
            testHot(service);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            testSend1k(service);
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void testSend1k(HelloService service) throws IOException {
        byte[] mb = new byte[1 * 1024];
        for (int i = 0; i < mb.length; i++) {
            mb[i] = 65;
        }

        String name = new String(mb);

        long st = System.currentTimeMillis();

        int count = 100000;
        for (int i = 0; i < count; i++) {
            service.sayHello(name);
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("it takes " + et + "ms, 1k : " + (count * 1000 / et) + " tps");
    }
}
