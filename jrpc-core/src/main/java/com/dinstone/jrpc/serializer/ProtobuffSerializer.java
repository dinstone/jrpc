
package com.dinstone.jrpc.serializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtobuffSerializer implements Serializer {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

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
        Schema<T> schema = getSchema(targetClass);
        T instance = targetClass.newInstance();
        ProtostuffIOUtil.mergeFrom(bytes, instance, schema);
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
