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

package com.dinstone.jrpc.transport;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.serializer.SerializeType;

public class TransportConfig extends Configuration {

    /** max size */
    private static final String MAX_SIZE = "rpc.max.size";

    /** serialize type */
    private static final String SERIALIZE_TYPE = "rpc.serialize.type";

    /** Connect Timeout */
    private static final String CONNECT_TIMEOUT = "rpc.connect.timeout";

    /** parallel count */
    private static final String HANDLER_COUNT = "rpc.handler.count";

    /** transport schema, default is 'mina' */
    private String schema = "mina";

    public TransportConfig() {
    }

    public TransportConfig(Configuration config) {
        super(config);
    }

    public TransportConfig(String configLocation) {
        super(configLocation);
    }

    public int getMaxSize() {
        return getInt(MAX_SIZE, Integer.MAX_VALUE);
    }

    public void setMaxSize(int maxSize) {
        setInt(MAX_SIZE, maxSize);
    }

    public void setSerializeType(SerializeType type) {
        set(SERIALIZE_TYPE, type.name());
    }

    public SerializeType getSerializeType() {
        String name = get(SERIALIZE_TYPE, SerializeType.JACKSON.name()).toUpperCase();
        return SerializeType.valueOf(name);
    }

    public int getConnectTimeout() {
        return getInt(CONNECT_TIMEOUT, 30000);
    }

    public void setConnectTimeout(int timeout) {
        setInt(CONNECT_TIMEOUT, timeout);
    }

    public int getHandlerCount() {
        return getInt(HANDLER_COUNT, Runtime.getRuntime().availableProcessors());
    }

    public void setHandlerCount(int count) {
        setInt(HANDLER_COUNT, count);
    }

    public void setSchema(String schema) {
        if (schema != null && !schema.isEmpty()) {
            this.schema = schema;
        }
    }

    public String getSchema() {
        return schema;
    }

}
