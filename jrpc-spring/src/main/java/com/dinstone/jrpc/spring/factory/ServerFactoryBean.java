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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.mina.MinaServer;
import com.dinstone.jrpc.spring.spi.ServiceBean;
import com.dinstone.jrpc.srd.ServiceRegistry;
import com.dinstone.jrpc.srd.zookeeper.RegistryDiscoveryConfig;
import com.dinstone.jrpc.srd.zookeeper.ZookeeperServiceRegistry;
import com.dinstone.jrpc.transport.NetworkAddressUtil;

public class ServerFactoryBean extends AbstractFactoryBean<Server> {

    private static final Logger LOG = LoggerFactory.getLogger(ServerFactoryBean.class);

    private String id;

    // ================================================
    // Transport config
    // ================================================
    private TransportBean transportBean;

    // ================================================
    // Registry config
    // ================================================
    private RegistryBean registryBean;

    // ================================================
    // Services config
    // ================================================
    List<ServiceBean> services = new ArrayList<ServiceBean>();

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

    public List<ServiceBean> getServices() {
        return services;
    }

    public void setServices(List<ServiceBean> services) {
        if (services != null) {
            for (ServiceBean implementBindingBean : services) {
                this.services.add(implementBindingBean);
            }
        }
    }

    @Override
    protected Server createInstance() throws Exception {
        LOG.info("create jrpc server [{}]", id);

        ServiceRegistry serviceRegistry = null;
        if ("zookeeper".equalsIgnoreCase(registryBean.getSchema()) && registryBean.getAddress() != null) {
            RegistryDiscoveryConfig registryConfig = new RegistryDiscoveryConfig();
            registryConfig.setZookeeperNodes(registryBean.getAddress());
            if (registryBean.getBasePath() != null) {
                registryConfig.setBasePath(registryBean.getBasePath());
            }
            serviceRegistry = new ZookeeperServiceRegistry(registryConfig);
        }

        String host = transportBean.getHost();
        if (host == null || "-".equals(host)) {
            host = NetworkAddressUtil.getPrivateInetInetAddress().get(0).getHostAddress();
        } else if ("+".equals(host)) {
            host = NetworkAddressUtil.getPublicInetInetAddress().get(0).getHostAddress();
        } else if ("*".equals(host)) {
            host = "0.0.0.0";
        }
        int port = transportBean.getPort();

        Server server = null;
        if ("mina".equalsIgnoreCase(transportBean.getType())) {
            server = new MinaServer(host, port, transportBean.getConfig(), serviceRegistry);
        } else {
            server = new MinaServer(host, port, transportBean.getConfig(), serviceRegistry);
        }

        server.start();
        return server;
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
