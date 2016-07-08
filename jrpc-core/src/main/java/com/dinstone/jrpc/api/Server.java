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
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceRegistry;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.AcceptanceFactory;
import com.dinstone.jrpc.transport.NetworkAddressUtil;
import com.dinstone.jrpc.transport.TransportConfig;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private EndpointConfig endpointConfig = new EndpointConfig();

    private RegistryConfig registryConfig = new RegistryConfig();

    private TransportConfig transportConfig = new TransportConfig();

    private Map<String, AcceptanceFactory> acceptanceFactoryMap = new HashMap<String, AcceptanceFactory>();

    private Map<String, RegistryFactory> registryFactoryMap = new HashMap<String, RegistryFactory>();

    private ServiceExporter serviceExporter;

    private DefaultImplementBinding implementBinding;

    private InetSocketAddress serviceAddress;

    private Acceptance acceptance;

    public Server(String host, int port) {
        try {
            serviceAddress = new InetSocketAddress(resolveHost(host), port);
        } catch (SocketException e) {
            throw new RuntimeException("host is invalid", e);
        }

        loadProviders();
    }

    public Server(String address) {
        if (address == null || address.isEmpty()) {
            throw new RuntimeException("address is empty");
        }

        serviceAddress = parseServiceAddress(address);
        if (serviceAddress == null) {
            throw new RuntimeException("address is invalid");
        }

        loadProviders();
    }

    public Server(InetSocketAddress serviceAddress) {
        this.serviceAddress = serviceAddress;

        loadProviders();
    }

    private void loadProviders() {
        ServiceLoader<AcceptanceFactory> afServiceLoader = ServiceLoader.load(AcceptanceFactory.class);
        for (AcceptanceFactory acceptanceFactory : afServiceLoader) {
            LOG.info("load acceptance provider for schema : {}", acceptanceFactory.getSchema());
            acceptanceFactoryMap.put(acceptanceFactory.getSchema(), acceptanceFactory);
        }

        ServiceLoader<RegistryFactory> rfServiceLoader = ServiceLoader.load(RegistryFactory.class);
        for (RegistryFactory registryFactory : rfServiceLoader) {
            LOG.info("load registry provider for schema : {}", registryFactory.getSchema());
            registryFactoryMap.put(registryFactory.getSchema(), registryFactory);
        }
    }

    private InetSocketAddress parseServiceAddress(String address) {
        InetSocketAddress providerAddress = null;
        try {
            String[] hpParts = address.split(":", 2);
            if (hpParts.length == 2) {
                String host = hpParts[0];
                int port = Integer.parseInt(hpParts[1]);
                host = resolveHost(host);
                providerAddress = new InetSocketAddress(host, port);
            }
        } catch (Exception e) {
            LOG.warn("parse service address error", e);
        }

        return providerAddress;
    }

    protected String resolveHost(String host) throws SocketException {
        if (host == null || "-".equals(host)) {
            host = NetworkAddressUtil.getPrivateInetInetAddress().get(0).getHostAddress();
        } else if ("+".equals(host)) {
            host = NetworkAddressUtil.getPublicInetInetAddress().get(0).getHostAddress();
        } else if ("*".equals(host)) {
            host = "0.0.0.0";
        }
        return host;
    }

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

        for (AcceptanceFactory acceptanceFactory : acceptanceFactoryMap.values()) {
            acceptanceFactory.destroy();
        }

        for (RegistryFactory registryFactory : registryFactoryMap.values()) {
            registryFactory.destroy();
        }

        LOG.info("JRPC server is stopped", implementBinding.getServiceAddress());
    }

    public synchronized ServiceExporter getServiceExporter() {
        if (serviceExporter == null) {
            createServiceExporter();
        }
        return serviceExporter;
    }

    private void createServiceExporter() {
        ServiceRegistry serviceRegistry = null;
        String registrySchema = registryConfig.getSchema();
        if (registrySchema != null && !registrySchema.isEmpty()) {
            RegistryFactory registryFactory = registryFactoryMap.get(registrySchema);
            if (registryFactory != null) {
                registryFactory.getRegistryConfig().merge(registryConfig);
                serviceRegistry = registryFactory.createServiceRegistry();
            } else {
                throw new RuntimeException("can't find regitry provider for schema : " + registrySchema);
            }
        }

        this.implementBinding = new DefaultImplementBinding(serviceAddress, serviceRegistry);
        this.serviceExporter = new DefaultServiceExporter(endpointConfig, implementBinding);

        AcceptanceFactory acceptanceFactory = acceptanceFactoryMap.get(transportConfig.getSchema());
        if (acceptanceFactory == null) {
            throw new RuntimeException("can't find transport provider for schema : " + transportConfig.getSchema());
        }
        acceptanceFactory.getTransportConfig().merge(transportConfig);
        acceptance = acceptanceFactory.create(implementBinding);

        acceptance.bind();
        LOG.info("JRPC server is started");
    }

    public InetSocketAddress getServiceAddress() {
        return serviceAddress;
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

}
