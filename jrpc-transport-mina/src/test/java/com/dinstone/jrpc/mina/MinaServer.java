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

package com.dinstone.jrpc.mina;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.endpoint.DefaultServiceExporter;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.endpoint.ServiceExporter;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.TransportConfig;
import com.dinstone.jrpc.transport.mina.MinaAcceptance;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class MinaServer {

    private static final Logger LOG = LoggerFactory.getLogger(MinaServer.class);

    private ImplementBinding implementBinding;

    private ServiceExporter serviceExporter;

    private MinaAcceptance acceptance;

    public MinaServer(String host, int port) {
        this(host, port, new TransportConfig());
    }

    public MinaServer(String host, int port, TransportConfig transportConfig) {
        this(new InetSocketAddress(host, port), transportConfig, null, null);
    }

    public MinaServer(InetSocketAddress providerAddress, TransportConfig transportConfig,
            RegistryConfig registryConfig, EndpointConfig endpointConfig) {
        this.implementBinding = new DefaultImplementBinding(registryConfig, providerAddress);
        this.serviceExporter = new DefaultServiceExporter(endpointConfig, implementBinding);

        this.acceptance = new MinaAcceptance(transportConfig, implementBinding);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.Server#start()
     */
    public void start() {
        acceptance.bind();
        LOG.info("jrpc server start on {}", implementBinding.getServiceAddress());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.Server#stop()
     */
    public void stop() {
        acceptance.destroy();
        serviceExporter.destroy();
        implementBinding.destroy();

        LOG.info("jrpc server stop on {}", implementBinding.getServiceAddress());
    }

    public <T> void regist(Class<T> serviceInterface, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, serviceImplement);
    }

    public <T> void regist(Class<T> serviceInterface, String group, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, serviceImplement);
    }

    public <T> void regist(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, timeout, serviceImplement);
    }

}
