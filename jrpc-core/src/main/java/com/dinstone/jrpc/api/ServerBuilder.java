
package com.dinstone.jrpc.api;

import java.net.InetSocketAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.NetworkAddressUtil;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ServerBuilder.class);

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
        return new Server(endpointConfig, registryConfig, transportConfig, serviceAddress);
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
