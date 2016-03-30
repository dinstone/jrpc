/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.mina.MinaClient;
import com.dinstone.jrpc.srd.ServiceDiscovery;
import com.dinstone.jrpc.srd.zookeeper.RegistryDiscoveryConfig;
import com.dinstone.jrpc.srd.zookeeper.ZookeeperServiceDiscovery;

public class ClientFactoryBean extends AbstractFactoryBean<Client> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientFactoryBean.class);

    private String id;

    // ================================================
    // Transport config
    // ================================================
    private TransportBean transportBean;

    // ================================================
    // Registry config
    // ================================================
    private RegistryBean registryBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransportBean getTransportBean() {
        return transportBean;
    }

    public void setTransportBean(TransportBean transportBean) {
        this.transportBean = transportBean;
    }

    public RegistryBean getRegistryBean() {
        return registryBean;
    }

    public void setRegistryBean(RegistryBean registryBean) {
        this.registryBean = registryBean;
    }

    @Override
    protected Client createInstance() throws Exception {
        LOG.info("create jrpc client [{}]", id);

        ServiceDiscovery serviceDiscovery = null;
        if ("zookeeper".equalsIgnoreCase(registryBean.getSchema()) && registryBean.getAddresses() != null) {
            RegistryDiscoveryConfig registryConfig = new RegistryDiscoveryConfig();
            registryConfig.setZookeeperNodes(registryBean.getAddresses());
            if (registryBean.getBasePath() != null) {
                registryConfig.setBasePath(registryBean.getBasePath());
            }
            serviceDiscovery = new ZookeeperServiceDiscovery(registryConfig);
        }

        Client client = null;
        if ("mina".equalsIgnoreCase(transportBean.getType())) {
            if (serviceDiscovery != null) {
                client = new MinaClient(serviceDiscovery, transportBean.getConfig());
            } else {
                client = new MinaClient(transportBean.getAddresses(), transportBean.getConfig());
            }
        } else {
            if (serviceDiscovery != null) {
                client = new MinaClient(serviceDiscovery, transportBean.getConfig());
            } else {
                client = new MinaClient(transportBean.getAddresses(), transportBean.getConfig());
            }
        }

        return client;
    }

    @Override
    protected void destroyInstance(Client instance) throws Exception {
        instance.destroy();
    }

    @Override
    public Class<?> getObjectType() {
        return Client.class;
    }
}
