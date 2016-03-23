
package com.dinstone.jrpc.api;

import com.dinstone.jrpc.invoker.DefaultServiceInvoker;
import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.invoker.ServiceInvoker;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.ServiceStubFactory;
import com.dinstone.jrpc.transport.ConnectionFactory;

public class ServiceImporter {

    private static final int DEFAULT_TIMEOUT = 3000;

    private ServiceProxyFactory serviceProxyFactory;

    private int timeout = DEFAULT_TIMEOUT;

    public ServiceImporter(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory) {
        this(referenceBinding, connectionFactory, new DefaultServiceInvoker());
    }

    public ServiceImporter(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory,
            ServiceInvoker serviceInvoker) {
        if (referenceBinding == null) {
            throw new IllegalArgumentException("referenceBinding is null");
        }

        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory is null");
        }

        if (serviceInvoker == null) {
            throw new IllegalArgumentException("serviceInvoker is null");
        }
        serviceProxyFactory = new ServiceStubFactory(referenceBinding, connectionFactory, serviceInvoker);
    }

    public <T> T getService(Class<T> sic) {
        return getService(sic, "");
    }

    public <T> T getService(Class<T> sic, String group) {
        return getService(sic, group, timeout);
    }

    public <T> T getService(Class<T> sic, String group, int timeout) {
        try {
            return serviceProxyFactory.createProxy(sic, group, timeout);
        } catch (Exception e) {
            throw new RuntimeException("can't create service proxy", e);
        }
    }

    public void destroy() {
        if (serviceProxyFactory != null) {
            serviceProxyFactory.destroy();
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
