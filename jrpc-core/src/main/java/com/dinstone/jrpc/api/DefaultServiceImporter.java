
package com.dinstone.jrpc.api;

import com.dinstone.jrpc.invoker.DefaultServiceInvoker;
import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.ServiceStubFactory;
import com.dinstone.jrpc.transport.ConnectionFactory;

public class DefaultServiceImporter implements ServiceImporter {

    private int defaultTimeout = DEFAULT_TIMEOUT;

    private ServiceProxyFactory serviceStubFactory;

    public DefaultServiceImporter(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory) {
        if (referenceBinding == null) {
            throw new IllegalArgumentException("referenceBinding is null");
        }

        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory is null");
        }

        serviceStubFactory = new ServiceStubFactory(referenceBinding, new DefaultServiceInvoker(referenceBinding,
            connectionFactory));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#importService(java.lang.Class)
     */
    @Override
    public <T> T importService(Class<T> sic) {
        return importService(sic, "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#importService(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T importService(Class<T> sic, String group) {
        return importService(sic, group, defaultTimeout);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#importService(java.lang.Class, java.lang.String, int)
     */
    @Override
    public <T> T importService(Class<T> sic, String group, int timeout) {
        try {
            return serviceStubFactory.createStub(sic, group, timeout);
        } catch (Exception e) {
            throw new RuntimeException("can't create service proxy", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#destroy()
     */
    @Override
    public void destroy() {
        if (serviceStubFactory != null) {
            serviceStubFactory.destroy();
        }
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

}
