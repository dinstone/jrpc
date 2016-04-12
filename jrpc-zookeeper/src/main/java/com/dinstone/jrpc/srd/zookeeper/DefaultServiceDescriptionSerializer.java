/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.srd.zookeeper;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import com.dinstone.jrpc.srd.ServiceDescription;

public class DefaultServiceDescriptionSerializer implements ServiceDescriptionSerializer {

    private final ObjectMapper mapper;

    private final JavaType type;

    public DefaultServiceDescriptionSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        type = mapper.getTypeFactory().constructType(ServiceDescription.class);
    }

    @Override
    public byte[] serialize(ServiceDescription service) throws Exception {
        return mapper.writeValueAsBytes(service);
    }

    @Override
    public ServiceDescription deserialize(byte[] bytes) throws Exception {
        return mapper.readValue(bytes, type);
    }

}
