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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.DefaultReferenceBinding;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.ConnectionFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private EndpointConfig endpointConfig = new EndpointConfig();

    private RegistryConfig registryConfig = new RegistryConfig();

    private TransportConfig transportConfig = new TransportConfig();

    private List<InetSocketAddress> serviceAddresses = new ArrayList<InetSocketAddress>();

    private Map<String, ConnectionFactory> connectionFactoryMap = new HashMap<String, ConnectionFactory>();

    private Map<String, RegistryFactory> registryFactoryMap = new HashMap<String, RegistryFactory>();

    private ServiceImporter serviceImporter;

    private DefaultReferenceBinding referenceBinding;

    public Client() {
        loadProviders();
    }

    public Client(String host, int port) {
        serviceAddresses.add(new InetSocketAddress(host, port));

        loadProviders();
    }

    public Client(String addresses) {
        if (addresses == null || addresses.length() == 0) {
            throw new IllegalArgumentException("serviceAddresses is empty");
        }

        String[] addressArrays = addresses.split(",");
        for (String address : addressArrays) {
            int pidx = address.lastIndexOf(':');
            if (pidx > 0 && (pidx < address.length() - 1)) {
                String host = address.substring(0, pidx);
                int port = Integer.parseInt(address.substring(pidx + 1));

                serviceAddresses.add(new InetSocketAddress(host, port));
            }
        }

        loadProviders();
    }

    private void loadProviders() {
        ServiceLoader<ConnectionFactory> cfServiceLoader = ServiceLoader.load(ConnectionFactory.class);
        for (ConnectionFactory connectionFactory : cfServiceLoader) {
            LOG.info("load connection provider for schema : {}", connectionFactory.getSchema());
            connectionFactoryMap.put(connectionFactory.getSchema(), connectionFactory);
        }

        ServiceLoader<RegistryFactory> rfServiceLoader = ServiceLoader.load(RegistryFactory.class);
        for (RegistryFactory registryFactory : rfServiceLoader) {
            LOG.info("load registry provider for schema : {}", registryFactory.getSchema());
            registryFactoryMap.put(registryFactory.getSchema(), registryFactory);
        }
    }

    public EndpointConfig getEndpointConfig() {
        return endpointConfig;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public TransportConfig getTransportConfig() {
        return transportConfig;
    }

    public List<InetSocketAddress> getServiceAddresses() {
        return serviceAddresses;
    }

    public synchronized ServiceImporter getServiceImporter() {
        if (serviceImporter == null) {
            creatServiceImporter();
        }

        return serviceImporter;
    }

    private void creatServiceImporter() {
        String transportSchema = transportConfig.getSchema();
        ConnectionFactory connectionFactory = connectionFactoryMap.get(transportSchema);
        if (connectionFactory == null) {
            throw new RuntimeException("can't find transport provider for schema : " + transportSchema);
        }
        connectionFactory.getTransportConfig().merge(transportConfig);

        ServiceDiscovery serviceDiscovery = null;
        String registrySchema = registryConfig.getSchema();
        RegistryFactory registryFactory = registryFactoryMap.get(registrySchema);
        if (registrySchema != null && !registrySchema.isEmpty()) {
            if (registryFactory != null) {
                registryFactory.getRegistryConfig().merge(registryConfig);
                serviceDiscovery = registryFactory.createServiceDiscovery();
            } else {
                throw new RuntimeException("can't find regitry provider for schema : " + registrySchema);
            }
        }

        this.referenceBinding = new DefaultReferenceBinding(serviceAddresses, serviceDiscovery);
        this.serviceImporter = new DefaultServiceImporter(endpointConfig, referenceBinding, connectionFactory);
    }

    public void destroy() {
        serviceImporter.destroy();
        referenceBinding.destroy();

        for (ConnectionFactory connectionFactory : connectionFactoryMap.values()) {
            connectionFactory.destroy();
        }

        for (RegistryFactory registryFactory : registryFactoryMap.values()) {
            registryFactory.destroy();
        }
    }

}
