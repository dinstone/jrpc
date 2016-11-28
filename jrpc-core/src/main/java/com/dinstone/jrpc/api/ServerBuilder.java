
package com.dinstone.jrpc.api;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.endpoint.DefaultServiceExporter;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceRegistry;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.AcceptanceFactory;
import com.dinstone.jrpc.transport.NetworkAddressUtil;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ServerBuilder.class);

    private Map<String, AcceptanceFactory> acceptanceFactoryMap = new HashMap<String, AcceptanceFactory>();

    private Map<String, RegistryFactory> registryFactoryMap = new HashMap<String, RegistryFactory>();

    private EndpointConfig endpointConfig = new EndpointConfig();

    private RegistryConfig registryConfig = new RegistryConfig();

    private TransportConfig transportConfig = new TransportConfig();

    private InetSocketAddress serviceAddress;

    public EndpointConfig endpointConfig() {
        return endpointConfig;
    }

    public RegistryConfig registryConfig() {
        return registryConfig;
    }

    public TransportConfig transportConfig() {
        return transportConfig;
    }

    public Server build() {
        loadProviders();

        if (serviceAddress == null) {
            throw new RuntimeException("server not bind service address");
        }

        AcceptanceFactory acceptanceFactory = acceptanceFactoryMap.get(transportConfig.getSchema());
        if (acceptanceFactory == null) {
            throw new RuntimeException("can't find transport provider for schema : " + transportConfig.getSchema());
        }

        ServiceRegistry serviceRegistry = null;
        String registrySchema = registryConfig.getSchema();
        if (registrySchema != null && !registrySchema.isEmpty()) {
            RegistryFactory registryFactory = registryFactoryMap.get(registrySchema);
            if (registryFactory != null) {
                serviceRegistry = registryFactory.createServiceRegistry(registryConfig);
            } else {
                throw new RuntimeException("can't find regitry provider for schema : " + registrySchema);
            }
        }

        DefaultImplementBinding implementBinding = new DefaultImplementBinding(serviceAddress, serviceRegistry);
        DefaultServiceExporter serviceExporter = new DefaultServiceExporter(endpointConfig, implementBinding);

        Acceptance acceptance = acceptanceFactory.create(transportConfig, implementBinding);

        return new Server(serviceExporter, implementBinding, acceptance);
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

    public ServerBuilder bind(InetSocketAddress serviceAddress) {
        this.serviceAddress = serviceAddress;
        return this;
    }

    public ServerBuilder bind(String host, int port) {
        try {
            return bind(new InetSocketAddress(resolveHost(host), port));
        } catch (SocketException e) {
            throw new RuntimeException("host is invalid", e);
        }
    }

    public ServerBuilder bind(String address) {
        if (address == null || address.isEmpty()) {
            throw new RuntimeException("address is empty");
        }

        InetSocketAddress socketAddress = parseServiceAddress(address);
        if (socketAddress == null) {
            throw new RuntimeException("address is invalid");
        }

        return bind(socketAddress);
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
}
