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
package com.dinstone.jrpc.mina;

import java.net.InetSocketAddress;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServerBuilder;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.TransportConfig;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class MinaServer {

    private Server server;

    public MinaServer(String host, int port) {
        this(host, port, new TransportConfig());
    }

    public MinaServer(String host, int port, TransportConfig transportConfig) {
        this(new InetSocketAddress(host, port), transportConfig, null, null);
    }

    public MinaServer(InetSocketAddress providerAddress, TransportConfig transportConfig, RegistryConfig registryConfig,
            EndpointConfig endpointConfig) {
        transportConfig.setSchema("mina");
        server = new ServerBuilder().bind(providerAddress).endpointConfig(endpointConfig).registryConfig(registryConfig)
            .transportConfig(transportConfig).build();

    }

    /**
     * {@inheritDoc}
     *
     * @see com.dinstone.jrpc.api.Server#start()
     */
    public void start() {
        server.start();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.dinstone.jrpc.api.Server#stop()
     */
    public void stop() {
        server.stop();
    }

    public <T> void regist(Class<T> serviceInterface, T serviceImplement) {
        server.exportService(serviceInterface, serviceImplement);
    }

    public <T> void regist(Class<T> serviceInterface, String group, T serviceImplement) {
        server.exportService(serviceInterface, group, serviceImplement);
    }

    public <T> void regist(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        server.exportService(serviceInterface, group, timeout, serviceImplement);
    }

}
