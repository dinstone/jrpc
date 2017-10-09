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
package com.dinstone.jrpc.endpoint;

import com.dinstone.jrpc.Configuration;

public class EndpointConfig extends Configuration {

    private static final String ENDPOINT_ID = "endpoint.id";

    private static final String ENDPOINT_NAME = "endpoint.name";

    private static final String DEFAULT_TIMEOUT_KEY = "default.timeout";

    private static final int DEFAULT_TIMEOUT = 3000;

    public EndpointConfig() {
        super();
    }

    public EndpointConfig(Configuration config) {
        super(config);
    }

    public EndpointConfig(String configLocation) {
        super(configLocation);
    }

    public EndpointConfig setDefaultTimeout(int defaultTimeout) {
        setInt(DEFAULT_TIMEOUT_KEY, defaultTimeout);
        return this;
    }

    public int getDefaultTimeout() {
        return getInt(DEFAULT_TIMEOUT_KEY, DEFAULT_TIMEOUT);
    }

    public EndpointConfig setEndpointId(String id) {
        set(ENDPOINT_ID, id);
        return this;
    }

    public EndpointConfig setEndpointName(String name) {
        set(ENDPOINT_NAME, name);
        return this;
    }

    public String getEndpointId() {
        return get(ENDPOINT_ID);
    }

    public String getEndpointName() {
        return get(ENDPOINT_NAME);
    }

}
