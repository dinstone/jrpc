
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
