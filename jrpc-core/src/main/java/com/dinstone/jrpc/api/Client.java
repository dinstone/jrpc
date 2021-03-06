/*
 * Copyright (C) 2013~2017 dinstone<dinstone@163.com>
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
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.endpoint.ServiceImporter;
import com.dinstone.jrpc.invoker.InvocationHandler;
import com.dinstone.jrpc.invoker.LocationInvocationHandler;
import com.dinstone.jrpc.invoker.RemoteInvocationHandler;
import com.dinstone.jrpc.invoker.ServiceInvoker;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.ConnectionManager;

public class Client implements ServiceImporter {

    private EndpointConfig endpointConfig;

    private ServiceDiscovery serviceDiscovery;

    private ReferenceBinding referenceBinding;

    private ConnectionManager connectionManager;

    private ServiceProxyFactory serviceProxyFactory;

    Client(EndpointConfig endpointConfig, List<InetSocketAddress> serviceAddresses) {
        checkAndInit(endpointConfig, serviceAddresses);
    }

    private void checkAndInit(EndpointConfig endpointConfig, List<InetSocketAddress> serviceAddresses) {
        if (endpointConfig == null) {
            throw new IllegalArgumentException("endpointConfig is null");
        }
        this.endpointConfig = endpointConfig;

        // check transport provider
        this.connectionManager = new ConnectionManager(endpointConfig.getTransportConfig());

        // check registry provider
        String registrySchema = endpointConfig.getRegistryConfig().getSchema();
        if (registrySchema != null && !registrySchema.isEmpty()) {
            SchemaFactoryLoader<RegistryFactory> rfLoader = SchemaFactoryLoader.getInstance(RegistryFactory.class);
            RegistryFactory registryFactory = rfLoader.getSchemaFactory(registrySchema);
            if (registryFactory == null) {
                throw new RuntimeException("can't find regitry provider for schema : " + registrySchema);
            } else {
                this.serviceDiscovery = registryFactory.createServiceDiscovery(endpointConfig.getRegistryConfig());
            }
        }

        this.referenceBinding = new DefaultReferenceBinding(endpointConfig, serviceDiscovery);

        InvocationHandler invocationHandler = createInvocationHandler(serviceAddresses);
        this.serviceProxyFactory = new ServiceProxyFactory(new ServiceInvoker(invocationHandler));
    }

    private InvocationHandler createInvocationHandler(List<InetSocketAddress> serviceAddresses) {
        RemoteInvocationHandler rpcInvocationHandler = new RemoteInvocationHandler(connectionManager);
        return new LocationInvocationHandler(rpcInvocationHandler, referenceBinding, serviceAddresses);
    }

    @Override
    public void destroy() {
        connectionManager.destroy();
        referenceBinding.destroy();

        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
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
        if (group == null) {
            group = "";
        }
        if (timeout <= 0) {
            timeout = endpointConfig.getDefaultTimeout();
        }

        try {
            ServiceProxy<T> wrapper = serviceProxyFactory.create(sic, group, timeout, null);
            referenceBinding.bind(wrapper);
            return wrapper.getProxy();
        } catch (Exception e) {
            throw new RuntimeException("can't import service", e);
        }
    }

}
