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
import java.util.List;

import com.dinstone.jrpc.SchemaFactoryLoader;
import com.dinstone.jrpc.binding.DefaultReferenceBinding;
import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.endpoint.DefaultServiceImporter;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.endpoint.ServiceImporter;
import com.dinstone.jrpc.invoker.StubServiceInvoker;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.ConnectionFactory;
import com.dinstone.jrpc.transport.ConnectionManager;
import com.dinstone.jrpc.transport.TransportConfig;

public class Client implements ServiceImporter {

    private ServiceImporter serviceImporter;

    private ServiceDiscovery serviceDiscovery;

    private ConnectionManager connectionManager;

    Client(EndpointConfig endpointConfig, RegistryConfig registryConfig, TransportConfig transportConfig,
            List<InetSocketAddress> serviceAddresses) {
        checkAndInit(endpointConfig, registryConfig, transportConfig, serviceAddresses);
    }

    protected void checkAndInit(EndpointConfig endpointConfig, RegistryConfig registryConfig,
            TransportConfig transportConfig, List<InetSocketAddress> serviceAddresses) {
        // check transport provider
        String transportSchema = transportConfig.getSchema();
        SchemaFactoryLoader<ConnectionFactory> cfLoader = SchemaFactoryLoader.getInstance(ConnectionFactory.class);
        ConnectionFactory connectionFactory = cfLoader.getSchemaFactory(transportSchema);
        if (connectionFactory == null) {
            throw new RuntimeException("can't find transport provider for schema : " + transportSchema);
        } else {
            this.connectionManager = new ConnectionManager(transportConfig, connectionFactory);
        }

        // check regitry provider
        String registrySchema = registryConfig.getSchema();
        if (registrySchema != null && !registrySchema.isEmpty()) {
            SchemaFactoryLoader<RegistryFactory> rfLoader = SchemaFactoryLoader.getInstance(RegistryFactory.class);
            RegistryFactory registryFactory = rfLoader.getSchemaFactory(registrySchema);
            if (registryFactory == null) {
                throw new RuntimeException("can't find regitry provider for schema : " + registrySchema);
            } else {
                this.serviceDiscovery = registryFactory.createServiceDiscovery(registryConfig);
            }
        }

        ReferenceBinding referenceBinding = new DefaultReferenceBinding(endpointConfig, serviceDiscovery);
        StubServiceInvoker serviceInvoker = new StubServiceInvoker(connectionManager, referenceBinding,
            serviceAddresses);
        this.serviceImporter = new DefaultServiceImporter(endpointConfig, referenceBinding, serviceInvoker);
    }

    @Override
    public void destroy() {
        serviceImporter.destroy();
        connectionManager.destroy();
        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
    }

    @Override
    public <T> T importService(Class<T> sic) {
        return serviceImporter.importService(sic);
    }

    @Override
    public <T> T importService(Class<T> sic, String group) {
        return serviceImporter.importService(sic, group);
    }

    @Override
    public <T> T importService(Class<T> sic, String group, int timeout) {
        return serviceImporter.importService(sic, group, timeout);
    }

}
