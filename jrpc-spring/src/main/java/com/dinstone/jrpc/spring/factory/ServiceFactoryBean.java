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

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.dinstone.jrpc.api.Server;

public class ServiceFactoryBean extends AbstractFactoryBean<Object> {

    private Class<Object> service;

    private String group;

    private int timeout;

    private Object instance;

    private Server server;

    public Class<Object> getService() {
        return service;
    }

    public void setService(Class<Object> service) {
        this.service = service;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    @Override
    public Class<?> getObjectType() {
        return service;
    }

    @Override
    protected Object createInstance() throws Exception {
        server.exportService(service, group, timeout, instance);

        return instance;
    }

}
