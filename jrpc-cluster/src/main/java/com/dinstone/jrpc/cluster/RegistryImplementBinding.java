
package com.dinstone.jrpc.cluster;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dinstone.jrpc.processor.AbstractImplementBinding;
import com.dinstone.jrpc.processor.Service;
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
    public <T> void bind(Class<T> serviceInterface, String group, Service<T> wrapper) {
        super.bind(serviceInterface, group, wrapper);

        ServiceDescription description = new ServiceDescription();
        String host = serviceAddress.getAddress().getHostAddress();
        int port = serviceAddress.getPort();
        description.setId(host + ":" + port);
        description.setHost(host);
        description.setPort(port);
        description.setName(serviceInterface.getName());
        description.setGroup(group);

        Map<String, Object> attributes = new HashMap<String, Object>();
        List<String> md = new ArrayList<String>();
        for (Method method : wrapper.getMethodMap().values()) {
            md.add(description(method));
        }
        attributes.put("timeout", wrapper.getTimeout());
        attributes.put("methods", md);

        ServiceAttribute attribute = new ServiceAttribute(attributes);
        description.setServiceAttribute(attribute);
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
