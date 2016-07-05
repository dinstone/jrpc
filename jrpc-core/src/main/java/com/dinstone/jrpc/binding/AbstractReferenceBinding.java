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

package com.dinstone.jrpc.binding;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.jrpc.api.EndpointConfig;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.registry.ServiceAttribute;
import com.dinstone.jrpc.registry.ServiceDescription;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.NetworkAddressUtil;

public abstract class AbstractReferenceBinding implements ReferenceBinding {

    private final AtomicInteger index = new AtomicInteger(0);

    protected InetSocketAddress consumerAddress;

    protected ServiceDiscovery serviceDiscovery;

    protected List<InetSocketAddress> backupServiceAddresses = new ArrayList<InetSocketAddress>();

    public AbstractReferenceBinding() {
        try {
            InetAddress addr = NetworkAddressUtil.getPrivateInetInetAddress().get(0);
            consumerAddress = new InetSocketAddress(addr, 0);
        } catch (SocketException e) {
            throw new RuntimeException("can't init ReferenceBinding", e);
        }
    }

    @Override
    public <T> void bind(ServiceProxy<T> wrapper, EndpointConfig endpointConfig) {
        if (serviceDiscovery != null) {
            try {
                ServiceDescription description = createServiceDescription(wrapper, endpointConfig);
                serviceDiscovery.listen(description);
            } catch (Exception e) {
                throw new RuntimeException("service reference bind error", e);
            }
        }
    }

    protected <T> ServiceDescription createServiceDescription(ServiceProxy<T> wrapper, EndpointConfig endpointConfig) {
        String group = wrapper.getGroup();
        String host = consumerAddress.getAddress().getHostAddress();
        int port = consumerAddress.getPort();

        StringBuilder id = new StringBuilder();
        id.append(host).append(":").append(port).append("@");
        id.append(endpointConfig.getEndpointName()).append("#").append(endpointConfig.getEndpointId()).append("@");
        id.append("group=").append((group == null ? "" : group));

        ServiceDescription description = new ServiceDescription();
        description.setId(id.toString());
        description.setName(wrapper.getService().getName());
        description.setGroup(group);
        description.setHost(host);
        description.setPort(port);

        ServiceAttribute serviceAttribute = new ServiceAttribute();
        serviceAttribute.addAttribute("endpointId", endpointConfig.getEndpointId());
        serviceAttribute.addAttribute("endpointName", endpointConfig.getEndpointName());

        description.setServiceAttribute(serviceAttribute);

        return description;
    }

    @Override
    public <T> InetSocketAddress getServiceAddress(Class<T> serviceInterface, String group) {
        InetSocketAddress serviceAddress = null;

        int next = Math.abs(index.getAndIncrement());
        if (serviceDiscovery != null) {
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
        try {
            List<ServiceDescription> serviceDescriptions = findServices(serviceName, group);
            if (serviceDescriptions.size() == 0) {
                return null;
            }

            return serviceDescriptions.get(index % serviceDescriptions.size()).getServiceAddress();
        } catch (Exception e) {
            throw new RuntimeException("service " + serviceName + "[" + group + "] discovery error", e);
        }
    }

    protected List<ServiceDescription> findServices(String serviceName, String group) throws Exception {
        List<ServiceDescription> services = new ArrayList<ServiceDescription>();
        List<ServiceDescription> serviceDescriptions = serviceDiscovery.discovery(serviceName);
        if (serviceDescriptions != null && serviceDescriptions.size() > 0) {
            for (ServiceDescription serviceDescription : serviceDescriptions) {
                String target = serviceDescription.getGroup();
                if (target == null && group == null) {
                    services.add(serviceDescription);
                    continue;
                }
                if (target != null && target.equals(group)) {
                    services.add(serviceDescription);
                    continue;
                }
            }
        }

        return services;
    }

    @Override
    public void destroy() {
        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
    }

}
