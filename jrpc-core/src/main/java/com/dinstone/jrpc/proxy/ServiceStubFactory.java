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

import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.invoker.ServiceInvoker;

public class ServiceStubFactory implements ServiceProxyFactory {

    private ServiceInvoker serviceInvoker;

    private ReferenceBinding referenceBinding;

    public ServiceStubFactory(ReferenceBinding referenceBinding, ServiceInvoker serviceInvoker) {
        super();
        this.referenceBinding = referenceBinding;
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public <T> void createSkelecton(Class<T> serviceInterface, String group, int timeout, T serviceObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createStub(Class<T> si, String group, int timeout) throws Exception {
        ProxyInvocationHandler<T> handler = new ProxyInvocationHandler<T>(si, group, timeout);
        T sr = si.cast(Proxy.newProxyInstance(si.getClassLoader(), new Class[] { si }, handler));

        referenceBinding.bind(si, group, timeout, sr);

        return sr;
    }

    @Override
    public void destroy() {
    }

    private class ProxyInvocationHandler<T> implements InvocationHandler {

        private Class<T> serviceInterface;

        private String group;

        private int timeout;

        public ProxyInvocationHandler(Class<T> serviceInterface, String group, int timeout) {
            this.serviceInterface = serviceInterface;
            this.group = group;
            this.timeout = timeout;
        }

        public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("hashCode")) {
                return new Integer(System.identityHashCode(proxyObj));
            } else if (methodName.equals("equals")) {
                return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("toString")) {
                return proxyObj.getClass().getName() + '@' + Integer.toHexString(proxyObj.hashCode());
            }

            return serviceInvoker.invoke(serviceInterface, group, timeout, method, args);
        }

    }

}
