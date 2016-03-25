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
package com.dinstone.jrpc.cluster;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.srd.DistributedServiceDiscovery;
import com.dinstone.jrpc.srd.RegistryDiscoveryConfig;
import com.dinstone.jrpc.srd.ServiceDescription;
import com.dinstone.jrpc.srd.ZookeeperServiceDiscovery;

public class DiscoveryReferenceBinding implements ReferenceBinding {

    private final AtomicInteger index = new AtomicInteger(0);

    private DistributedServiceDiscovery serviceDiscovery;

    public DiscoveryReferenceBinding(RegistryDiscoveryConfig discoveryConfig) {
        this.serviceDiscovery = new ZookeeperServiceDiscovery(discoveryConfig);
    }

    @Override
    public <T> void bind(Class<T> serviceInterface, String group, int timeout, T serviceReference) {
        try {
            serviceDiscovery.listen(serviceInterface.getName(), group);
        } catch (Exception e) {
            throw new RuntimeException("service reference bind error", e);
        }
    }

    @Override
    public <T> InetSocketAddress getServiceAddress(Class<T> serviceInterface, String group) {
        ServiceDescription serviceDescription = locateService(serviceInterface.getName(), group);
        if (serviceDescription == null) {
            throw new RuntimeException("service " + serviceInterface.getName() + "[" + group + "] is not ready");
        }

        return serviceDescription.getServiceAddress();
    }

    private ServiceDescription locateService(String serviceName, String group) {
        try {
            List<ServiceDescription> serviceDescriptions = serviceDiscovery.discovery(serviceName, group);
            if (serviceDescriptions.size() == 0) {
                return null;
            }
            int thisIndex = Math.abs(index.getAndIncrement());
            return serviceDescriptions.get(thisIndex % serviceDescriptions.size());
        } catch (Exception e) {
            throw new RuntimeException("service " + serviceName + "[" + group + "] discovery error", e);
        }
    }

    @Override
    public void destroy() {
        serviceDiscovery.destroy();
    }

}
