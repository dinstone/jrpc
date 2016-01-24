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

import java.lang.reflect.Proxy;

import com.dinstone.jrpc.Configuration;

/**
 * the interface Client implements.
 * 
 * @author guojinfei
 * @version 1.0.0.2014-6-30
 */
public abstract class AbstractClient implements Client {

    protected Configuration config = new Configuration();

    protected ConnectionFactory factory;

    protected InvocationProxy invoker;

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    protected void build(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory is null");
        }
        this.factory = connectionFactory;

        this.invoker = new InvocationProxy(connectionFactory, config);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.client.Client#getProxy(java.lang.Class)
     */
    public <T> T getProxy(Class<T> proxy) {
        return proxy.cast(Proxy.newProxyInstance(proxy.getClassLoader(), new Class[] { proxy }, invoker));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.client.Client#asyncInvoke(java.lang.String, java.lang.Object[])
     */
    public CallFuture asyncInvoke(String method, Object[] args) throws Throwable {
        return invoker.invoke(method, args);
    }

    public void destroy() {
        if (factory != null) {
            factory.destroy();
        }
    }

}