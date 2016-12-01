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

package com.dinstone.jrpc.api;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.SchemaFactoryLoader;
import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.endpoint.DefaultServiceExporter;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.endpoint.ServiceExporter;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.AcceptanceFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class Server implements ServiceExporter {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private ServiceExporter serviceExporter;

    private ImplementBinding implementBinding;

    private Acceptance acceptance;

    Server(EndpointConfig endpointConfig, RegistryConfig registryConfig, TransportConfig transportConfig,
            InetSocketAddress serviceAddress) {
        checkAndInit(endpointConfig, registryConfig, transportConfig, serviceAddress);
    }

    private void checkAndInit(EndpointConfig endpointConfig, RegistryConfig registryConfig,
            TransportConfig transportConfig, InetSocketAddress serviceAddress) {
        // check bind service address
        if (serviceAddress == null) {
            throw new RuntimeException("server not bind service address");
        }

        // check transport provider
        SchemaFactoryLoader<AcceptanceFactory> afLoader = SchemaFactoryLoader.getInstance(AcceptanceFactory.class);
        AcceptanceFactory acceptanceFactory = afLoader.getSchemaFactory(transportConfig.getSchema());
        if (acceptanceFactory == null) {
            throw new RuntimeException("can't find transport provider for schema : " + transportConfig.getSchema());
        }

        // check registry provider
        String registrySchema = registryConfig.getSchema();
        if (registrySchema != null && !registrySchema.isEmpty()) {
            SchemaFactoryLoader<RegistryFactory> rfLoader = SchemaFactoryLoader.getInstance(RegistryFactory.class);
            RegistryFactory registryFactory = rfLoader.getSchemaFactory(registrySchema);
            if (registryFactory == null) {
                throw new RuntimeException("can't find registry provider for schema : " + registrySchema);
            }
        }

        this.implementBinding = new DefaultImplementBinding(registryConfig, serviceAddress);
        this.serviceExporter = new DefaultServiceExporter(endpointConfig, implementBinding);
        this.acceptance = acceptanceFactory.create(transportConfig, implementBinding);
    }

    public synchronized Server start() {
        acceptance.bind();

        LOG.info("JRPC server is started", implementBinding.getServiceAddress());

        return this;
    }

    public synchronized Server stop() {
        destroy();

        LOG.info("JRPC server is stopped", implementBinding.getServiceAddress());

        return this;
    }

    public InetSocketAddress getServiceAddress() {
        return implementBinding.getServiceAddress();
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, timeout, serviceImplement);
    }

    @Override
    public void destroy() {
        if (acceptance != null) {
            acceptance.destroy();
        }
        if (serviceExporter != null) {
            serviceExporter.destroy();
        }
        if (implementBinding != null) {
            implementBinding.destroy();
        }
    }

}
