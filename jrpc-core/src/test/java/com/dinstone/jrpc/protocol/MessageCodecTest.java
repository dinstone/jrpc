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
package com.dinstone.jrpc.protocol;

import com.dinstone.jrpc.demo.DemoService;
import com.dinstone.jrpc.serializer.SerializeType;

public class MessageCodecTest {

    public static void main(String[] args) throws Exception {
        test(SerializeType.JACKSON);
        test(SerializeType.PROTOBUFF);
    }

    protected static void test(SerializeType st) throws Exception {
        Request request = new Request(12345, st, new Call(DemoService.class.getName(), "", 3000, "hello",
            new Object[] { "guojinfei", 34 }, new Class<?>[] { String.class, int.class }));
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
