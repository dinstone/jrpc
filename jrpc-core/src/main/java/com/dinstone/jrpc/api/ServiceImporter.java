
package com.dinstone.jrpc.api;

import com.dinstone.jrpc.invoker.DefaultServiceInvoker;
import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.ServiceStubFactory;
import com.dinstone.jrpc.transport.ConnectionFactory;

public class ServiceImporter {

    private static final int DEFAULT_TIMEOUT = 3000;

    private ServiceProxyFactory serviceStubFactory;

    private int timeout = DEFAULT_TIMEOUT;

    public ServiceImporter(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory) {
        if (referenceBinding == null) {
            throw new IllegalArgumentException("referenceBinding is null");
        }

        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory is null");
        }

        serviceStubFactory = new ServiceStubFactory(referenceBinding, new DefaultServiceInvoker(connectionFactory));
    }

    public <T> T getService(Class<T> sic) {
        return getService(sic, "");
    }

    public <T> T getService(Class<T> sic, String group) {
        return getService(sic, group, timeout);
    }

    public <T> T getService(Class<T> sic, String group, int timeout) {
        try {
            return serviceStubFactory.createStub(sic, group, timeout);
        } catch (Exception e) {
            throw new RuntimeException("can't create service proxy", e);
        }
    }

    public void destroy() {
        if (serviceStubFactory != null) {
            serviceStubFactory.destroy();
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
