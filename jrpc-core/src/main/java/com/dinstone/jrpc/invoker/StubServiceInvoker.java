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

package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ConnectionManager;

/**
 * client-side service invoker.
 * 
 * @author dinstone
 * @version 1.0.0
 */
public class StubServiceInvoker implements ServiceInvoker {

    private ReferenceBinding referenceBinding;

    private ConnectionManager connectionManager;

    public StubServiceInvoker(ConnectionManager connectionManager, ReferenceBinding referenceBinding) {
        this.connectionManager = connectionManager;
        this.referenceBinding = referenceBinding;
    }

    @Override
    public Object invoke(ServiceProxy<?> serviceProxy, Method method, Object[] args) throws Exception {
        String methodName = method.getName();
        Object instance = serviceProxy.getInstance();
        if (methodName.equals("hashCode")) {
            return new Integer(System.identityHashCode(instance));
        } else if (methodName.equals("equals")) {
            return (instance == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (methodName.equals("toString")) {
            return instance.getClass().getName() + '@' + Integer.toHexString(instance.hashCode());
        } else if (methodName.equals("getClass")) {
            return serviceProxy.getService();
        }

        String group = serviceProxy.getGroup();
        int timeout = serviceProxy.getTimeout();
        Class<?> service = serviceProxy.getService();

        Connection connection = connectionManager.getConnection(referenceBinding.getServiceAddress(service, group));

        Call call = new Call(service.getName(), group, timeout, methodName, args, method.getParameterTypes());
        return connection.call(call).get(timeout, TimeUnit.MILLISECONDS);
    }

}
