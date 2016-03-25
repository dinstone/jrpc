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
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ConnectionFactory;

public class DefaultServiceInvoker implements ServiceInvoker {

    private ReferenceBinding referenceBinding;

    private ConnectionFactory connectionFactory;

    public DefaultServiceInvoker(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory) {
        this.referenceBinding = referenceBinding;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> Object invoke(Class<T> serviceInterface, String group, int timeout, Method method, Object[] args)
            throws Exception {
        InetSocketAddress address = referenceBinding.getServiceAddress(serviceInterface, group);
        Connection connection = connectionFactory.create(address);
        return connection.call(new Call(serviceInterface.getName(), group, timeout, method.getName(), args)).get(
            timeout, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
    }

}
