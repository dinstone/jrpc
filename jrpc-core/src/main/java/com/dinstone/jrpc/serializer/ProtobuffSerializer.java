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
package com.dinstone.jrpc.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtobuffSerializer implements Serializer {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T data) throws Exception {
        Schema<T> schema = (Schema<T>) getSchema(data.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate();
        byte[] protostuff = null;
        try {
            protostuff = ProtostuffIOUtil.toByteArray(data, schema, buffer);
        } finally {
            // buffer.clear();
        }
        return protostuff;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) throws Exception {
        return deserialize(bytes, 0, bytes.length, targetClass);
    }

    @Override
    public <T> T deserialize(byte[] bytes, int offset, int length, Class<T> targetClass) throws Exception {
        Schema<T> schema = getSchema(targetClass);
        T instance = targetClass.newInstance();
        ProtostuffIOUtil.mergeFrom(bytes, offset, length, instance, schema);
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

}
