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

package com.dinstone.jrpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.dinstone.jrpc.invoker.ServiceInvoker;

public class ServiceProxyFactory {

    private ServiceInvoker serviceInvoker;

    public ServiceProxyFactory(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public <T> ServiceProxy<T> create(Class<T> serviceInterface, String group, int timeout, T serviceInstance)
            throws Exception {
        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException(serviceInterface.getName() + " is not interface");
        }
        if (serviceInstance != null && !serviceInterface.isInstance(serviceInstance)) {
            throw new IllegalArgumentException(
                serviceInstance + " is not an instance of " + serviceInterface.getName());
        }

        ServiceProxy<T> serviceProxy = new ServiceProxy<>(serviceInterface, group, timeout);
        ProxyInvocationHandler<T> handler = new ProxyInvocationHandler<>(serviceProxy);
        T proxyInstance = serviceInterface
            .cast(Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[] { serviceInterface }, handler));

        serviceProxy.setProxy(proxyInstance);
        serviceProxy.setTarget(serviceInstance);
        return serviceProxy;
    }

    private class ProxyInvocationHandler<T> implements InvocationHandler {

        private ServiceProxy<T> serviceProxy;

        public ProxyInvocationHandler(ServiceProxy<T> serviceProxy) {
            this.serviceProxy = serviceProxy;
        }

        @Override
        public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Object instance = serviceProxy.getProxy();
            if (methodName.equals("hashCode")) {
                return new Integer(System.identityHashCode(instance));
            } else if (methodName.equals("equals")) {
                return (instance == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("toString")) {
                return instance.getClass().getName() + '@' + Integer.toHexString(instance.hashCode());
            } else if (methodName.equals("getClass")) {
                return serviceProxy.getService();
            }

            return serviceInvoker.invoke(serviceProxy.getService().getName(), serviceProxy.getGroup(), methodName,
                serviceProxy.getTimeout(), args, method.getParameterTypes());
        }

    }
}
