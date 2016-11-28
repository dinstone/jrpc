
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
import com.dinstone.jrpc.endpoint.DefaultServiceImporter;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.ConnectionFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class ClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ClientBuilder.class);

    private EndpointConfig endpointConfig = new EndpointConfig();

    private RegistryConfig registryConfig = new RegistryConfig();

    private TransportConfig transportConfig = new TransportConfig();

    private List<InetSocketAddress> serviceAddresses = new ArrayList<InetSocketAddress>();

    private Map<String, ConnectionFactory> connectionFactoryMap = new HashMap<String, ConnectionFactory>();

    private Map<String, RegistryFactory> registryFactoryMap = new HashMap<String, RegistryFactory>();

    public ClientBuilder bind(String addresses) {
        if (addresses == null || addresses.length() == 0) {
            return this;
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

        return this;
    }

    public ClientBuilder bind(String host, int port) {
        serviceAddresses.add(new InetSocketAddress(host, port));

        return this;
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

    public EndpointConfig endpointConfig() {
        return endpointConfig;
    }

    public RegistryConfig registryConfig() {
        return registryConfig;
    }

    public TransportConfig transportConfig() {
        return transportConfig;
    }

    public Client build() {
        loadProviders();

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
                serviceDiscovery = registryFactory.createServiceDiscovery(registryConfig);
            } else {
                throw new RuntimeException("can't find regitry provider for schema : " + registrySchema);
            }
        }

        DefaultReferenceBinding referenceBinding = new DefaultReferenceBinding(serviceAddresses, serviceDiscovery);
        DefaultServiceImporter serviceImporter = new DefaultServiceImporter(endpointConfig, referenceBinding,
            connectionFactory);

        return new Client(referenceBinding, serviceImporter);
    }

}
