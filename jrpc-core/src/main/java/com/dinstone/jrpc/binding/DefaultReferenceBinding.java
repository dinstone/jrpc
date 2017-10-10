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

package com.dinstone.jrpc.binding;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.registry.ServiceDescription;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.transport.NetworkAddressUtil;

public class DefaultReferenceBinding implements ReferenceBinding {

    protected InetSocketAddress consumerAddress;

    protected ServiceDiscovery serviceDiscovery;

    protected EndpointConfig endpointConfig;

    public DefaultReferenceBinding(EndpointConfig endpointConfig, ServiceDiscovery serviceDiscovery) {
        this(endpointConfig, serviceDiscovery, null);
    }

    public DefaultReferenceBinding(EndpointConfig endpointConfig, ServiceDiscovery serviceDiscovery,
            InetSocketAddress consumerAddress) {
        if (endpointConfig == null) {
            throw new IllegalArgumentException("endpointConfig is null");
        }
        this.endpointConfig = endpointConfig;
        this.serviceDiscovery = serviceDiscovery;

        if (consumerAddress == null) {
            try {
                InetAddress addr = NetworkAddressUtil.getPrivateInetInetAddress().get(0);
                consumerAddress = new InetSocketAddress(addr, 0);
            } catch (Exception e) {
                throw new RuntimeException("can't init ReferenceBinding", e);
            }
        }
        this.consumerAddress = consumerAddress;
    }

    @Override
    public <T> void bind(ServiceProxy<T> wrapper) {
        if (serviceDiscovery != null) {
            try {
                serviceDiscovery.listen(createServiceDescription(wrapper, endpointConfig));
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

        description.addAttribute("endpointId", endpointConfig.getEndpointId());
        description.addAttribute("endpointName", endpointConfig.getEndpointName());

        return description;
    }

    @Override
    public List<ServiceDescription> lookup(String serviceName, String group) {
        try {
            if (serviceDiscovery != null) {
                return findServices(serviceName, group);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("service " + serviceName + "[" + group + "] discovery error", e);
        }
    }

    protected List<ServiceDescription> findServices(String serviceName, String group) throws Exception {
        List<ServiceDescription> services = new ArrayList<>();
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
