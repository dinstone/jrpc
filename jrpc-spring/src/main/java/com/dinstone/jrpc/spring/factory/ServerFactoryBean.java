/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
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
package com.dinstone.jrpc.spring.factory;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServerBuilder;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.transport.NetworkAddressUtil;
import com.dinstone.jrpc.transport.TransportConfig;

public class ServerFactoryBean extends AbstractFactoryBean<Server> {

    private static final Logger LOG = LoggerFactory.getLogger(ServerFactoryBean.class);

    private String id;

    private String name;

    // ================================================
    // Transport config
    // ================================================
    private ConfigBean transportBean;

    // ================================================
    // Registry config
    // ================================================
    private ConfigBean registryBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConfigBean getTransportBean() {
        return transportBean;
    }

    public void setTransportBean(ConfigBean transportBean) {
        this.transportBean = transportBean;
    }

    public ConfigBean getRegistryBean() {
        return registryBean;
    }

    public void setRegistryBean(ConfigBean registryBean) {
        this.registryBean = registryBean;
    }

    @Override
    protected Server createInstance() throws Exception {
        LOG.info("create jrpc client {}@{}", id, name);

        String address = transportBean.getAddress();
        if (address == null || address.isEmpty()) {
            throw new RuntimeException("transport address attribute is empty.");
        }

        ServerBuilder builder = new ServerBuilder();

        // setting transport config
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setSchema(transportBean.getSchema());
        transportConfig.setProperties(transportBean.getProperties());
        builder.transportConfig(transportConfig);

        // setting registry config
        if (registryBean.getSchema() != null && !registryBean.getSchema().isEmpty()) {
            RegistryConfig config = new RegistryConfig();
            config.setSchema(registryBean.getSchema()).setProperties(registryBean.getProperties());
            builder.registryConfig(config);
        }

        // setting endpoint config
        EndpointConfig endpointConfig = new EndpointConfig().setEndpointId(id).setEndpointName(name);

        return builder.endpointConfig(endpointConfig).bind(address).build().start();
    }

    protected InetSocketAddress getProviderAddress(String address) {
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
            LOG.warn("parse provider address error", e);
        }

        return providerAddress;
    }

    @Override
    protected void destroyInstance(Server instance) throws Exception {
        instance.stop();
    }

    @Override
    public Class<?> getObjectType() {
        return Server.class;
    }
}
