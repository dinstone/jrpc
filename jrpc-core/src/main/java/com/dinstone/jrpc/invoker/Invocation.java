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
package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import com.dinstone.jrpc.proxy.ServiceProxy;

public class Invocation<T> {

    private ServiceProxy<T> proxy;

    private Method method;

    private Object[] params;

    private InetSocketAddress serviceAddress;

    public Invocation(ServiceProxy<T> proxy, Method method, Object[] params) {
        super();
        this.proxy = proxy;
        this.method = method;
        this.params = params;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getParams() {
        return params;
    }

    public Class<T> getService() {
        return proxy.getService();
    }

    public String getGroup() {
        return proxy.getGroup();
    }

    public int getTimeout() {
        return proxy.getTimeout();
    }

    public T getInstance() {
        return proxy.getInstance();
    }

    public InetSocketAddress getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(InetSocketAddress serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
