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
package com.dinstone.jrpc.registry;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * service description
 *
 * @author dinstone
 * @version 1.0.0
 */
public class ServiceDescription implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private String id;

    private String serviceName;

    private String group;

    private String host;

    private int port;

    private String uri;

    private long registryTime;

    private ServiceAttribute serviceAttribute;

    private volatile InetSocketAddress address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getRegistryTime() {
        return registryTime;
    }

    public void setRegistryTime(long registryTime) {
        this.registryTime = registryTime;
    }

    public ServiceAttribute getServiceAttribute() {
        return serviceAttribute;
    }

    public void setServiceAttribute(ServiceAttribute serviceAttribute) {
        this.serviceAttribute = serviceAttribute;
    }

    public InetSocketAddress getServiceAddress() {
        if (address == null) {
            address = new InetSocketAddress(host, port);
        }

        return address;
    }

    @Override
    public String toString() {
        return "ServiceDescription [id=" + id + ", name=" + serviceName + ", group=" + group + ", host=" + host
                + ", port=" + port + ", uri=" + uri + ", registryTime=" + registryTime + ", serviceAttribute="
                + serviceAttribute + "]";
    }

}
