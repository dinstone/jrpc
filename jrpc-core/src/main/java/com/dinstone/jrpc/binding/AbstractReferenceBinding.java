
package com.dinstone.jrpc.binding;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.srd.ServiceDescription;
import com.dinstone.jrpc.srd.ServiceDiscovery;

public abstract class AbstractReferenceBinding implements ReferenceBinding {

    private final AtomicInteger index = new AtomicInteger(0);

    protected ServiceDiscovery serviceDiscovery;

    protected List<InetSocketAddress> backupServiceAddresses = new ArrayList<InetSocketAddress>();

    @Override
    public <T> void bind(ServiceProxy<T> wrapper) {
        if (serviceDiscovery != null) {
            try {
                serviceDiscovery.listen(wrapper.getService().getName(), wrapper.getGroup());
            } catch (Exception e) {
                throw new RuntimeException("service reference bind error", e);
            }
        }
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
            List<ServiceDescription> serviceDescriptions = serviceDiscovery.discovery(serviceName, group);
            if (serviceDescriptions.size() == 0) {
                return null;
            }

            return serviceDescriptions.get(index % serviceDescriptions.size()).getServiceAddress();
        } catch (Exception e) {
            throw new RuntimeException("service " + serviceName + "[" + group + "] discovery error", e);
        }
    }

    @Override
    public void destroy() {
        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
    }

}
