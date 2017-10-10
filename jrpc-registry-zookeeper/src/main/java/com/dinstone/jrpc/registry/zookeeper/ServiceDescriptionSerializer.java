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

package com.dinstone.jrpc.registry.zookeeper;

import com.dinstone.jrpc.registry.ServiceDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceDescriptionSerializer {

    private final ObjectMapper mapper;

    private final JavaType type;

    public ServiceDescriptionSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        type = mapper.getTypeFactory().constructType(ServiceDescription.class);
    }

    public byte[] serialize(ServiceDescription service) throws Exception {
        return mapper.writeValueAsBytes(service);
    }

    public ServiceDescription deserialize(byte[] bytes) throws Exception {
        return mapper.readValue(bytes, type);
    }

}
