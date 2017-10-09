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
package com.dinstone.jrpc.api;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.SchemaFactoryLoader;
import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.endpoint.ServiceExporter;
import com.dinstone.jrpc.invoker.SkelectonServiceInvoker;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.StubServiceProxyFactory;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceRegistry;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.AcceptanceFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class Server implements ServiceExporter {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private Acceptance acceptance;

    private EndpointConfig endpointConfig;

    private InetSocketAddress serviceAddress;

    private ServiceRegistry serviceRegistry;

    private ImplementBinding implementBinding;

    private ServiceProxyFactory serviceProxyFactory;

    Server(EndpointConfig endpointConfig, RegistryConfig registryConfig, TransportConfig transportConfig,
            InetSocketAddress serviceAddress) {
        checkAndInit(endpointConfig, registryConfig, transportConfig, serviceAddress);
    }

    private void checkAndInit(EndpointConfig endpointConfig, RegistryConfig registryConfig,
            TransportConfig transportConfig, InetSocketAddress serviceAddress) {
        if (endpointConfig == null) {
            throw new IllegalArgumentException("endpointConfig is null");
        }
        this.endpointConfig = endpointConfig;

        // check bind service address
        if (serviceAddress == null) {
            throw new RuntimeException("server not bind service address");
        }
        this.serviceAddress = serviceAddress;

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
            } else {
                this.serviceRegistry = registryFactory.createServiceRegistry(registryConfig);
            }
        }

        this.serviceProxyFactory = new StubServiceProxyFactory(new SkelectonServiceInvoker());
        this.implementBinding = new DefaultImplementBinding(endpointConfig, serviceRegistry, serviceAddress);
        this.acceptance = acceptanceFactory.create(transportConfig, implementBinding, serviceAddress);
    }

    public synchronized Server start() {
        acceptance.bind();

        LOG.info("JRPC server is started on {}", serviceAddress);

        return this;
    }

    public synchronized Server stop() {
        destroy();

        LOG.info("JRPC server is stopped on {}", serviceAddress);

        return this;
    }

    public InetSocketAddress getServiceAddress() {
        return serviceAddress;
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, T serviceImplement) {
        exportService(serviceInterface, "", endpointConfig.getDefaultTimeout(), serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, T serviceImplement) {
        exportService(serviceInterface, group, endpointConfig.getDefaultTimeout(), serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        if (group == null) {
            group = "";
        }
        if (timeout <= 0) {
            timeout = endpointConfig.getDefaultTimeout();
        }

        try {
            ServiceProxy<T> wrapper = serviceProxyFactory.create(serviceInterface, group, timeout, serviceImplement);
            implementBinding.bind(wrapper, endpointConfig);
        } catch (Exception e) {
            throw new RuntimeException("can't export service", e);
        }
    }

    @Override
    public void destroy() {
        if (implementBinding != null) {
            implementBinding.destroy();
        }
        if (serviceRegistry != null) {
            serviceRegistry.destroy();
        }
        if (acceptance != null) {
            acceptance.destroy();
        }
    }

}
