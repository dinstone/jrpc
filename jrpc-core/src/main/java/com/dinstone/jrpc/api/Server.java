
package com.dinstone.jrpc.api;

import java.net.InetSocketAddress;
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
        this(new InetSocketAddress(host, port));
    }

    public Server(String address) {
        if (address == null || address.isEmpty()) {
            throw new RuntimeException("address is empty");
        }

        serviceAddress = parseServiceAddress(address);
        if (serviceAddress == null) {
            throw new RuntimeException("address is invalid");
        }

        loadModule();
    }

    public Server(InetSocketAddress serviceAddress) {
        this.serviceAddress = serviceAddress;

        loadModule();
    }

    private void loadModule() {
        ServiceLoader<AcceptanceFactory> afServiceLoader = ServiceLoader.load(AcceptanceFactory.class);
        for (AcceptanceFactory acceptanceFactory : afServiceLoader) {
            LOG.info("load acceptance factory : {}", acceptanceFactory.getSchema());
            acceptanceFactoryMap.put(acceptanceFactory.getSchema(), acceptanceFactory);
        }

        ServiceLoader<RegistryFactory> rfServiceLoader = ServiceLoader.load(RegistryFactory.class);
        for (RegistryFactory registryFactory : rfServiceLoader) {
            LOG.info("load registry factory : {}", registryFactory.getSchema());
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
                if (host == null || "-".equals(host)) {
                    host = NetworkAddressUtil.getPrivateInetInetAddress().get(0).getHostAddress();
                } else if ("+".equals(host)) {
                    host = NetworkAddressUtil.getPublicInetInetAddress().get(0).getHostAddress();
                } else if ("*".equals(host)) {
                    host = "0.0.0.0";
                }
                providerAddress = new InetSocketAddress(host, port);
            }
        } catch (Exception e) {
            LOG.warn("parse service address error", e);
        }

        return providerAddress;
    }

    public void destroy() {
        acceptance.destroy();
        serviceExporter.destroy();
        implementBinding.destroy();

        for (AcceptanceFactory acceptanceFactory : acceptanceFactoryMap.values()) {
            acceptanceFactory.destroy();
        }

        for (RegistryFactory registryFactory : registryFactoryMap.values()) {
            registryFactory.destroy();
        }

        LOG.info("jrpc server stop on {}", implementBinding.getServiceAddress());
    }

    public synchronized ServiceExporter getServiceExporter() {
        if (serviceExporter == null) {
            createServiceExporter();
        }
        return serviceExporter;
    }

    private void createServiceExporter() {
        ServiceRegistry serviceRegistry = null;
        RegistryFactory registryFactory = registryFactoryMap.get(registryConfig.getSchema());
        if (registryFactory != null) {
            registryFactory.getRegistryConfig().merge(registryConfig);
            serviceRegistry = registryFactory.createServiceRegistry();
        }

        this.implementBinding = new DefaultImplementBinding(serviceAddress, serviceRegistry);
        this.serviceExporter = new DefaultServiceExporter(endpointConfig, implementBinding);

        AcceptanceFactory acceptanceFactory = acceptanceFactoryMap.get(transportConfig.getSchema());
        if (acceptanceFactory == null) {
            throw new RuntimeException("unsportted transport schema : " + transportConfig.getSchema());
        }
        acceptanceFactory.getTransportConfig().merge(transportConfig);
        acceptance = acceptanceFactory.create(implementBinding);

        acceptance.bind();
        LOG.info("jrpc server start on {}", serviceAddress);
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
