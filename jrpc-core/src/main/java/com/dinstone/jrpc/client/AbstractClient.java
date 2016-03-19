/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.client;

import java.util.concurrent.ConcurrentHashMap;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;

/**
 * the interface Client implements.
 * 
 * @author guojinfei
 * @version 1.0.0.2014-6-30
 */
public abstract class AbstractClient implements Client {

    private ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<String, Object>();

    protected Configuration config = new Configuration();

    protected ServiceProxyFactory serviceProxyFactory;

    public AbstractClient(ServiceProxyFactory serviceProxyFactory) {
        super();
        this.serviceProxyFactory = serviceProxyFactory;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    public <T> T getService(Class<T> sic) {
        return getService(sic, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> sic, String group) {
        String key = sic.getName() + group;
        Object so = serviceMap.get(key);
        if (so == null) {
            so = serviceProxyFactory.createProxy(sic, group);
            serviceMap.putIfAbsent(key, so);
        }

        return (T) serviceMap.get(key);
    }

    @Override
    public void destroy() {
        serviceMap.clear();
    }

}