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

package com.dinstone.jrpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.dinstone.jrpc.invoker.ServiceInvoker;

public class StubProxyFactory implements ServiceProxyFactory {

    private ServiceInvoker serviceInvoker;

    public StubProxyFactory(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public <T> ServiceProxy<T> create(Class<T> serviceInterface, String group, int timeout, T serviceObject)
            throws Exception {
        ProxyInvocationHandler<T> handler = new ProxyInvocationHandler<T>();
        T proxy = serviceInterface.cast(Proxy.newProxyInstance(serviceInterface.getClassLoader(),
            new Class[] { serviceInterface }, handler));
        ServiceProxy<T> serviceProxy = new ServiceProxy<T>(serviceInterface, group, timeout, proxy);
        handler.serviceProxy = serviceProxy;
        return serviceProxy;
    }

    @Override
    public void destroy() {
    }

    private class ProxyInvocationHandler<T> implements InvocationHandler {

        private ServiceProxy<T> serviceProxy;

        public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
            return serviceInvoker.invoke(serviceProxy, method, args);
        }

    }

}
