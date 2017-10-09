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
package com.dinstone.jrpc.serialize;

import com.dinstone.jrpc.demo.DemoService;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.serializer.JacksonSerializer;
import com.dinstone.jrpc.serializer.ProtobuffSerializer;
import com.dinstone.jrpc.serializer.Serializer;

public class SerializerTest {

    public static void main(String[] args) throws Exception {
        productTest();

        System.out.println("=====================================");

        callTest();

    }

    protected static void productTest() throws Exception {
        Product p = new Product();
        p.setService(DemoService.class.getName());
        p.setGroup("");
        p.setTimeout(1000);
        p.setMethod("hello");
        p.setParams(new Object[] { "dinstone", 30 });
        p.setParamTypes(new Class[] { String.class, int.class });

        comTest(p, new ProtobuffSerializer());
        comTest(p, new JacksonSerializer());
    }

    protected static void callTest() throws Exception {
        Call call = new Call(DemoService.class.getName(), "", 1000, "hello", new Object[] { "dinstone", 30 },
            new Class[] { String.class, int.class });

        comTest(call, new ProtobuffSerializer());
        comTest(call, new JacksonSerializer());

        // comTest(call, new HessianSerializer());
    }

    protected static void comTest(Object obj, Serializer serializer) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byte[] pd = serializer.serialize(obj);
            serializer.deserialize(pd, obj.getClass());
        }
        long end = System.currentTimeMillis();
        System.out.println(serializer.getClass() + " hot take's " + (end - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byte[] pd = serializer.serialize(obj);
            serializer.deserialize(pd, obj.getClass());
        }
        end = System.currentTimeMillis();
        System.out.println(serializer.getClass() + " take's " + (end - start) + "ms");
    }
}
