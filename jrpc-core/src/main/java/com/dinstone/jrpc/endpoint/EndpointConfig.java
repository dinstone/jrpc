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
package com.dinstone.jrpc.endpoint;

import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.TransportConfig;

public class EndpointConfig {

	private String endpointId;

	private String endpointName;

	private int defaultTimeout = 3000;

	private RegistryConfig registryConfig = new RegistryConfig();

	private TransportConfig transportConfig = new TransportConfig();

	public EndpointConfig() {
		super();
	}

	public String getEndpointId() {
		return endpointId;
	}

	public EndpointConfig setEndpointId(String endpointId) {
		this.endpointId = endpointId;
		return this;
	}

	public String getEndpointName() {
		return endpointName;
	}

	public EndpointConfig setEndpointName(String endpointName) {
		this.endpointName = endpointName;
		return this;
	}

	public int getDefaultTimeout() {
		return defaultTimeout;
	}

	public EndpointConfig setDefaultTimeout(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
		return this;
	}

	public RegistryConfig getRegistryConfig() {
		return registryConfig;
	}

	public EndpointConfig setRegistryConfig(RegistryConfig registryConfig) {
		if (registryConfig != null) {
			this.registryConfig.mergeConfiguration(registryConfig);
		}
		return this;
	}

	public TransportConfig getTransportConfig() {
		return transportConfig;
	}

	public EndpointConfig setTransportConfig(TransportConfig transportConfig) {
		if (transportConfig != null) {
			this.transportConfig.mergeConfiguration(transportConfig);
		}
		return this;
	}

}
