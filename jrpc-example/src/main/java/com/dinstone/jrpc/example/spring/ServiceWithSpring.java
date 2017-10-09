/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
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
package com.dinstone.jrpc.example.spring;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dinstone.jrpc.example.HelloService;

public class ServiceWithSpring {

    public static void main(String[] args) {
        case01();
        // case02();
        // case03();
    }

    protected static void case03() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "jrpc-example-case3.xml");

        HelloService rhsv1 = (HelloService) applicationContext.getBean("rhsv1");

        try {
            testHot(rhsv1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            testSend1k(rhsv1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.in.read();
        } catch (IOException e) {
        }

        applicationContext.close();
    }

    private static void case02() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "jrpc-example-case2.xml");

        HelloService rhsv1Netty = (HelloService) applicationContext.getBean("rhsv1-netty");
        System.out.println("rhsv1Netty");
        try {
            testHot(rhsv1Netty);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            testSend1k(rhsv1Netty);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HelloService rhsv1Mina = (HelloService) applicationContext.getBean("rhsv1-mina");
        System.out.println("rhsv1Mina");
        try {
            testHot(rhsv1Mina);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            testSend1k(rhsv1Mina);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.in.read();
        } catch (IOException e) {
        }

        applicationContext.close();
    }

    protected static void case01() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "jrpc-example-case1.xml");

        HelloService rhsv1 = (HelloService) applicationContext.getBean("rhsv1");

        try {
            testHot(rhsv1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            testSend1k(rhsv1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("please press any key to continue");
            System.in.read();
        } catch (IOException e) {
        }

        applicationContext.close();
    }

    protected static void jackson(ClassPathXmlApplicationContext applicationContext) {
        HelloService service = (HelloService) applicationContext.getBean("rhsv1-1");
        if (service == null) {
            return;
        }

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
        if (service == null) {
            return;
        }

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

        int count = 100000;
        for (int i = 0; i < count; i++) {
            service.sayHello("dinstone");
        }

        long et = System.currentTimeMillis() - st;
        System.out.println("hot takes " + et + "ms, " + (count * 1000 / et) + " tps");
    }

    public static void testSend1k(HelloService service) throws IOException {
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
