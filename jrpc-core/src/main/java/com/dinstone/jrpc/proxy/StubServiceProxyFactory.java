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
package com.dinstone.jrpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.dinstone.jrpc.invoker.ServiceInvoker;

public class StubServiceProxyFactory implements ServiceProxyFactory {

    private ServiceInvoker serviceInvoker;

    public StubServiceProxyFactory(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public <T> ServiceProxy<T> create(Class<T> serviceInterface, String group, int timeout, T serviceObject)
            throws Exception {
        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException(serviceInterface.getName() + " is not interface");
        }
        if (serviceObject != null && !serviceInterface.isInstance(serviceObject)) {
            throw new IllegalArgumentException(serviceObject + " is not an instance of " + serviceInterface.getName());
        }

        ServiceProxy<T> serviceProxy = new ServiceProxy<>(serviceInterface, group, timeout);
        ProxyInvocationHandler<T> handler = new ProxyInvocationHandler<>(serviceProxy);
        T proxy = serviceInterface
            .cast(Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[] { serviceInterface }, handler));

        serviceProxy.setProxy(proxy);
        serviceProxy.setInstance(serviceObject);
        return serviceProxy;
    }

    private class ProxyInvocationHandler<T> implements InvocationHandler {

        private ServiceProxy<T> serviceProxy;

        public ProxyInvocationHandler(ServiceProxy<T> serviceProxy) {
            this.serviceProxy = serviceProxy;
        }

        @Override
        public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
            return serviceInvoker.invoke(serviceProxy, method, args);
        }

    }
}
