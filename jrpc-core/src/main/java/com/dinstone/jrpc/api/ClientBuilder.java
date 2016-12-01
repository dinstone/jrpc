
package com.dinstone.jrpc.api;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.TransportConfig;

public class ClientBuilder {

    private EndpointConfig endpointConfig = new EndpointConfig();

    private RegistryConfig registryConfig = new RegistryConfig();

    private TransportConfig transportConfig = new TransportConfig();

    private List<InetSocketAddress> serviceAddresses = new ArrayList<InetSocketAddress>();

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

    public Client build() {
        return new Client(endpointConfig, registryConfig, transportConfig, serviceAddresses);
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

}
