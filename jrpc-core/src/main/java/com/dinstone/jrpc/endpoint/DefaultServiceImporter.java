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

package com.dinstone.jrpc.endpoint;

import java.net.InetSocketAddress;
import java.util.List;

import com.dinstone.jrpc.SchemaFactoryLoader;
import com.dinstone.jrpc.binding.DefaultReferenceBinding;
import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.invoker.StubServiceInvoker;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.StubProxyFactory;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.ConnectionManager;
import com.dinstone.jrpc.transport.TransportConfig;

public class DefaultServiceImporter implements ServiceImporter {

    private final EndpointConfig endpointConfig;

    private final ReferenceBinding referenceBinding;

    private final ConnectionManager connectionManager;

    private final ServiceProxyFactory serviceProxyFactory;

    private ServiceDiscovery serviceDiscovery;

    public DefaultServiceImporter(EndpointConfig endpointConfig, TransportConfig transportConfig,
            RegistryConfig registryConfig, List<InetSocketAddress> serviceAddresses) {
        if (endpointConfig == null) {
            throw new IllegalArgumentException("endpointConfig is null");
        }
        this.endpointConfig = endpointConfig;

        if (transportConfig == null) {
            throw new IllegalArgumentException("transportConfig is null");
        }
        this.connectionManager = new ConnectionManager(transportConfig);

        this.referenceBinding = new DefaultReferenceBinding(registryConfig, serviceAddresses);

        String registrySchema = registryConfig.getSchema();
        if (registrySchema != null && !registrySchema.isEmpty()) {
            SchemaFactoryLoader<RegistryFactory> rfLoader = SchemaFactoryLoader.getInstance(RegistryFactory.class);
            RegistryFactory registryFactory = rfLoader.getSchemaFactory(registrySchema);
            if (registryFactory != null) {
                this.serviceDiscovery = registryFactory.createServiceDiscovery(registryConfig);
            }
        }

        StubServiceInvoker serviceInvoker = new StubServiceInvoker(connectionManager, serviceDiscovery,
            serviceAddresses);
        this.serviceProxyFactory = new StubProxyFactory(serviceInvoker);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#importService(java.lang.Class)
     */
    @Override
    public <T> T importService(Class<T> sic) {
        return importService(sic, "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#importService(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T importService(Class<T> sic, String group) {
        return importService(sic, group, endpointConfig.getDefaultTimeout());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#importService(java.lang.Class, java.lang.String, int)
     */
    @Override
    public <T> T importService(Class<T> sic, String group, int timeout) {
        try {
            ServiceProxy<T> wrapper = serviceProxyFactory.create(sic, group, timeout, null);
            referenceBinding.bind(wrapper, endpointConfig);
            return wrapper.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("can't import service", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#destroy()
     */
    @Override
    public void destroy() {
        connectionManager.destroy();
        referenceBinding.destroy();

        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
    }

}
