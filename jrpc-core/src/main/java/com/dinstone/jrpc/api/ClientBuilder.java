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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.TransportConfig;

public class ClientBuilder {

	private EndpointConfig endpointConfig = new EndpointConfig();

	private List<InetSocketAddress> serviceAddresses = new ArrayList<>();

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
		return new Client(endpointConfig, serviceAddresses);
	}

	public ClientBuilder endpointConfig(EndpointConfig endpointConfig) {
		if (endpointConfig != null) {
			this.endpointConfig.setEndpointId(endpointConfig.getEndpointId());
			this.endpointConfig.setEndpointName(endpointConfig.getEndpointName());
			this.endpointConfig.setDefaultTimeout(endpointConfig.getDefaultTimeout());
			this.endpointConfig.setTransportConfig(endpointConfig.getTransportConfig());
			this.endpointConfig.setRegistryConfig(endpointConfig.getRegistryConfig());
		}

		return this;
	}

	public ClientBuilder transportConfig(TransportConfig transportConfig) {
		this.endpointConfig.setTransportConfig(transportConfig);
		return this;
	}

	public ClientBuilder registryConfig(RegistryConfig registryConfig) {
		this.endpointConfig.setRegistryConfig(registryConfig);
		return this;
	}

}
