
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
        loadModule();
    }

    public Client(String host, int port) {
        serviceAddresses.add(new InetSocketAddress(host, port));

        loadModule();
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

        loadModule();
    }

    private void loadModule() {
        ServiceLoader<ConnectionFactory> cfServiceLoader = ServiceLoader.load(ConnectionFactory.class);
        for (ConnectionFactory connectionFactory : cfServiceLoader) {
            LOG.info("load connection factory : {}", connectionFactory.getSchema());
            connectionFactoryMap.put(connectionFactory.getSchema(), connectionFactory);
        }

        ServiceLoader<RegistryFactory> rfServiceLoader = ServiceLoader.load(RegistryFactory.class);
        for (RegistryFactory registryFactory : rfServiceLoader) {
            LOG.info("load registry factory : {}", registryFactory.getSchema());
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
            throw new RuntimeException("unsportted transport schema : " + transportSchema);
        }
        connectionFactory.getTransportConfig().merge(transportConfig);

        ServiceDiscovery serviceDiscovery = null;
        String registrySchema = registryConfig.getSchema();
        RegistryFactory registryFactory = registryFactoryMap.get(registrySchema);
        if (registryFactory != null) {
            registryFactory.getRegistryConfig().merge(registryConfig);
            serviceDiscovery = registryFactory.createServiceDiscovery();
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
