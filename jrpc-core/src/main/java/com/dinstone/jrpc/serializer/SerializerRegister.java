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

import java.util.EnumMap;
import java.util.Map;

public class SerializerRegister {

    private static SerializerRegister INSTANCE = new SerializerRegister();

    private Map<SerializeType, Serializer> serializerMap = new EnumMap<>(SerializeType.class);

    public static SerializerRegister getInstance() {
        return INSTANCE;
    }

    protected SerializerRegister() {
        regist(SerializeType.JACKSON, new JacksonSerializer());
        regist(SerializeType.PROTOBUFF, new ProtobuffSerializer());
    }

    public void regist(SerializeType type, Serializer serializer) {
        serializerMap.put(type, serializer);
    }

    public Serializer find(SerializeType serializeType) {
        return serializerMap.get(serializeType);
    }
}
