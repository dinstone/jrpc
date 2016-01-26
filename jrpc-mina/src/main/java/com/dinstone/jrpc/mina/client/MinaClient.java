/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.mina.client;

import com.dinstone.jrpc.client.AbstractClient;
import com.dinstone.jrpc.client.Client;

/**
 * @author guojf
 * @version 1.0.0.2013-5-2
 */
public class MinaClient extends AbstractClient implements Client {

    public MinaClient(String host, int port) {
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("host is null");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than zero");
        }
        config.setServiceHost(host);
        config.setServicePort(port);
    }

    public MinaClient setCallTimeout(int timeout) {
        config.setCallTimeout(timeout);
        return this;
    }

    public MinaClient setMaxObjectSize(int maxSize) {
        config.setMaxSize(maxSize);
        return this;
    }

    public MinaClient setParallelCount(int count) {
        config.setParallelCount(count);
        return this;
    }

    public Client build() {
        build(new MinaConnectionFactory(config));
        return this;
    }

    @Override
    public String toString() {
        return "MinaClient [host=" + config.getServiceHost() + ", port=" + config.getServicePort() + "]";
    }

}
