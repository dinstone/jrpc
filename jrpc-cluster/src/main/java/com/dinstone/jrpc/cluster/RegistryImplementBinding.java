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

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dinstone.jrpc.binding.AbstractImplementBinding;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.srd.DistributedServiceRegistry;
import com.dinstone.jrpc.srd.RegistryDiscoveryConfig;
import com.dinstone.jrpc.srd.ServiceAttribute;
import com.dinstone.jrpc.srd.ServiceDescription;
import com.dinstone.jrpc.srd.ZookeeperServiceRegistry;

public class RegistryImplementBinding extends AbstractImplementBinding {

    private DistributedServiceRegistry serviceRegistry;

    public RegistryImplementBinding(String host, int port, RegistryDiscoveryConfig registryConfig) {
        this.serviceAddress = new InetSocketAddress(host, port);
        this.serviceRegistry = new ZookeeperServiceRegistry(registryConfig);
    }

    @Override
    public <T> void bind(ServiceProxy<T> wrapper) {
        super.bind(wrapper);

        ServiceDescription description = new ServiceDescription();
        String host = serviceAddress.getAddress().getHostAddress();
        int port = serviceAddress.getPort();
        description.setId(host + ":" + port);
        description.setHost(host);
        description.setPort(port);
        description.setName(wrapper.getService().getName());
        description.setGroup(wrapper.getGroup());

        ServiceAttribute serviceAttribute = new ServiceAttribute();
        List<String> methodDescList = new ArrayList<String>();
        for (Method method : wrapper.getMethodMap().values()) {
            methodDescList.add(description(method));
        }
        serviceAttribute.addAttribute("timeout", wrapper.getTimeout());
        serviceAttribute.addAttribute("methods", methodDescList);

        description.setServiceAttribute(serviceAttribute);
        try {
            serviceRegistry.publish(description);
        } catch (Exception e) {
            throw new RuntimeException("can't regist service", e);
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
        serviceRegistry.destroy();
    }
}
