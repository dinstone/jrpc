
package com.dinstone.jrpc.invoker;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.jrpc.registry.ServiceDescription;
import com.dinstone.jrpc.registry.ServiceDiscovery;

public class LocationInvocationHandler implements InvocationHandler {

    private final AtomicInteger index = new AtomicInteger(0);

    private InvocationHandler invocationHandler;

    private ServiceDiscovery serviceDiscovery;

    private List<InetSocketAddress> backupServiceAddresses = new ArrayList<InetSocketAddress>();

    public LocationInvocationHandler(InvocationHandler invocationHandler, ServiceDiscovery serviceDiscovery,
            List<InetSocketAddress> serviceAddresses) {
        this.invocationHandler = invocationHandler;
        this.serviceDiscovery = serviceDiscovery;

        if (serviceAddresses != null) {
            backupServiceAddresses.addAll(serviceAddresses);
        }
    }

    @Override
    public <T> Object handle(Invocation<T> invocation) throws Exception {
        invocation.setServiceAddress(getServiceAddress(invocation.getService(), invocation.getGroup()));
        return invocationHandler.handle(invocation);
    }

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

}
