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

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.registry.ServiceDescription;
import com.dinstone.jrpc.registry.ServiceRegistry;

public class DefaultImplementBinding implements ImplementBinding {

    protected Map<String, ServiceProxy<?>> serviceProxyMap = new ConcurrentHashMap<>();

    protected InetSocketAddress providerAddress;

    protected ServiceRegistry serviceRegistry;

    protected EndpointConfig endpointConfig;

    public DefaultImplementBinding(EndpointConfig endpointConfig, ServiceRegistry serviceRegistry,
            InetSocketAddress providerAddress) {
        this.endpointConfig = endpointConfig;
        this.serviceRegistry = serviceRegistry;
        this.providerAddress = providerAddress;
    }

    @Override
    public <T> void bind(ServiceProxy<T> serviceWrapper, EndpointConfig endpointConfig) {
        String serviceId = serviceWrapper.getService().getName() + "-" + serviceWrapper.getGroup();
        if (serviceProxyMap.get(serviceId) != null) {
            throw new RuntimeException("multiple object registed with the service interface " + serviceId);
        }
        serviceProxyMap.put(serviceId, serviceWrapper);

        if (serviceRegistry != null) {
            publish(serviceWrapper, endpointConfig);
        }
    }

    protected void publish(ServiceProxy<?> wrapper, EndpointConfig endpointConfig) {
        String host = providerAddress.getAddress().getHostAddress();
        int port = providerAddress.getPort();
        String group = wrapper.getGroup();

        StringBuilder id = new StringBuilder();
        id.append(host).append(":").append(port).append("@");
        id.append(endpointConfig.getEndpointName()).append("#").append(endpointConfig.getEndpointId()).append("@");
        id.append("group=").append((group == null ? "" : group));

        ServiceDescription description = new ServiceDescription();
        description.setId(id.toString());
        description.setHost(host);
        description.setPort(port);
        description.setName(wrapper.getService().getName());
        description.setGroup(group);
        description.setOpTime(System.currentTimeMillis());

        List<String> methodDescList = new ArrayList<>();
        for (Method method : wrapper.getService().getDeclaredMethods()) {
            methodDescList.add(description(method));
        }
        description.addAttribute("methods", methodDescList);
        description.addAttribute("timeout", wrapper.getTimeout());

        description.addAttribute("endpointId", endpointConfig.getEndpointId());
        description.addAttribute("endpointName", endpointConfig.getEndpointName());

        try {
            serviceRegistry.register(description);
        } catch (Exception e) {
            throw new RuntimeException("can't publish service", e);
        }
    }

    private String description(Method method) {
        StringBuilder desc = new StringBuilder();
        desc.append(getTypeName(method.getReturnType()) + " ");
        desc.append(getTypeName(method.getDeclaringClass()) + ".");
        desc.append(method.getName() + "(");
        Class<?>[] params = method.getParameterTypes();
        for (int j = 0; j < params.length; j++) {
            desc.append(getTypeName(params[j]));
            if (j < (params.length - 1)) {
                desc.append(",");
            }
        }
        desc.append(")");
        return desc.toString();
    }

    private static String getTypeName(Class<?> type) {
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
            }
        }
        return type.getName();
    }

    @Override
    public void destroy() {
        if (serviceRegistry != null) {
            serviceRegistry.destroy();
        }
    }

    @Override
    public ServiceProxy<?> lookup(String service, String group) {
        String serviceId = service + "-" + group;
        return serviceProxyMap.get(serviceId);
    }

}
