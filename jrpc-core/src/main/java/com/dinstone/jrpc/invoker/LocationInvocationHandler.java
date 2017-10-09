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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.registry.ServiceDescription;

public class LocationInvocationHandler implements InvocationHandler {

    private final AtomicInteger index = new AtomicInteger(0);

    private InvocationHandler invocationHandler;

    private ReferenceBinding referenceBinding;

    private List<InetSocketAddress> backupServiceAddresses = new ArrayList<>();

    public LocationInvocationHandler(InvocationHandler invocationHandler, ReferenceBinding referenceBinding,
            List<InetSocketAddress> serviceAddresses) {
        this.invocationHandler = invocationHandler;
        this.referenceBinding = referenceBinding;

        if (serviceAddresses != null) {
            backupServiceAddresses.addAll(serviceAddresses);
        }
    }

    @Override
    public <T> Object handle(Invocation<T> invocation) throws Throwable {
        invocation.setServiceAddress(getServiceAddress(invocation.getService(), invocation.getGroup()));
        return invocationHandler.handle(invocation);
    }

    public <T> InetSocketAddress getServiceAddress(Class<T> serviceInterface, String group) {
        InetSocketAddress serviceAddress = null;

        int next = Math.abs(index.getAndIncrement());
        if (referenceBinding != null) {
            serviceAddress = locateServiceAddress(serviceInterface.getName(), group, next);
        }

        if (serviceAddress == null && backupServiceAddresses.size() > 0) {
            serviceAddress = backupServiceAddresses.get(next % backupServiceAddresses.size());
        }

        if (serviceAddress == null) {
            throw new RuntimeException("service " + serviceInterface.getName() + "[" + group + "] is not ready");
        }

        return serviceAddress;
    }

    private InetSocketAddress locateServiceAddress(String serviceName, String group, int index) {
        List<ServiceDescription> serviceDescriptions = referenceBinding.lookup(serviceName, group);
        if (serviceDescriptions == null || serviceDescriptions.size() == 0) {
            return null;
        }

        return serviceDescriptions.get(index % serviceDescriptions.size()).getServiceAddress();
    }

}
