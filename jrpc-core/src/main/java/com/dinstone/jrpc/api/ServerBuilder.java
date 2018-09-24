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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.NetworkInterfaceUtil;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServerBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(ServerBuilder.class);

	private EndpointConfig endpointConfig = new EndpointConfig();

	private InetSocketAddress serviceAddress;

	public Server build() {
		return new Server(endpointConfig, serviceAddress);
	}

	public ServerBuilder bind(InetSocketAddress socketAddress) {
		if (socketAddress != null) {
			this.serviceAddress = socketAddress;
		}
		return this;
	}

	public ServerBuilder bind(String host, int port) {
		try {
			List<InetSocketAddress> resolveAddress = resolveAddress(host, port);
			if (!resolveAddress.isEmpty()) {
				bind(resolveAddress.get(0));
			}
		} catch (SocketException e) {
			throw new RuntimeException("host is invalid", e);
		}
		return this;
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

	public ServerBuilder endpointConfig(EndpointConfig endpointConfig) {
		if (endpointConfig != null) {
			this.endpointConfig.setEndpointId(endpointConfig.getEndpointId());
			this.endpointConfig.setEndpointName(endpointConfig.getEndpointName());
			this.endpointConfig.setDefaultTimeout(endpointConfig.getDefaultTimeout());
			this.endpointConfig.setTransportConfig(endpointConfig.getTransportConfig());
			this.endpointConfig.setRegistryConfig(endpointConfig.getRegistryConfig());
		}

		return this;
	}

	public ServerBuilder transportConfig(TransportConfig transportConfig) {
		this.endpointConfig.setTransportConfig(transportConfig);
		return this;
	}

	public ServerBuilder registryConfig(RegistryConfig registryConfig) {
		this.endpointConfig.setRegistryConfig(registryConfig);
		return this;
	}

	private InetSocketAddress parseServiceAddress(String address) {
		try {
			String[] hpParts = address.split(":", 2);
			if (hpParts.length == 2) {
				List<InetSocketAddress> resolveAddress = resolveAddress(hpParts[0], Integer.parseInt(hpParts[1]));
				if (!resolveAddress.isEmpty()) {
					return resolveAddress.get(0);
				}
			}
		} catch (Exception e) {
			LOG.warn("parse service address error", e);
		}

		return null;
	}

	private List<InetSocketAddress> resolveAddress(String host, int port) throws SocketException {
		List<InetSocketAddress> addresses = new ArrayList<>();
		if (host == null || "-".equals(host)) {
			for (InetAddress inetAddress : NetworkInterfaceUtil.getPrivateAddresses()) {
				addresses.add(new InetSocketAddress(inetAddress, port));
			}
		} else if ("+".equals(host)) {
			for (InetAddress inetAddress : NetworkInterfaceUtil.getPublicAddresses()) {
				addresses.add(new InetSocketAddress(inetAddress, port));
			}
		} else if ("*".equals(host)) {
			addresses.add(new InetSocketAddress("0.0.0.0", port));
		}
		return addresses;
	}

}
