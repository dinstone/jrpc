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

package com.dinstone.jrpc.spring.spi;

import com.dinstone.jrpc.SchemaConfig;
import com.dinstone.jrpc.transport.TransportConfig;

public class TransportBean {

    private static final String DEFAULT_TRANSPORT = "netty";

    private String type = DEFAULT_TRANSPORT;

    private String host;

    private int port;

    private String address;

    private TransportConfig config = new TransportConfig();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TransportConfig getConfig() {
        return config;
    }

    public void setConfig(SchemaConfig<?> config) {
        if (config != null) {
            this.config = new TransportConfig(config);
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
