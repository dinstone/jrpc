
package com.dinstone.jrpc.protocol;

import com.dinstone.jrpc.demo.DemoService;
import com.dinstone.jrpc.serializer.SerializeType;

public class MessageCodecTest {

    public static void main(String[] args) throws Exception {
        test(SerializeType.JACKSON);
        test(SerializeType.PROTOBUFF);
    }

    protected static void test(SerializeType st) throws Exception {
        Request request = new Request(12345, st, new Call(DemoService.class.getName(), "", 3000, "hello", new Object[] {
                "guojinfei", 34 }, new Class<?>[] { String.class, int.class }));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byte[] pd = MessageCodec.encodeMessage(request);
            MessageCodec.decodeMessage(pd);
        }
        long end = System.currentTimeMillis();
        System.out.println(st + " hot take's " + (end - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byte[] pd = MessageCodec.encodeMessage(request);
            MessageCodec.decodeMessage(pd);
        }
        end = System.currentTimeMillis();
        System.out.println(st + " take's " + (end - start) + "ms");
    }

}
