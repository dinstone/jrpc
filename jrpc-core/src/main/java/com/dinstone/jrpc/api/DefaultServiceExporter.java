
package com.dinstone.jrpc.api;

import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.ServiceSkelectonFactory;

public class DefaultServiceExporter implements ServiceExporter {

    private int defaultTimeout = DEFAULT_TIMEOUT;

    private ServiceProxyFactory serviceProxyFactory;

    public DefaultServiceExporter(ImplementBinding implementBinding) {
        this.serviceProxyFactory = new ServiceSkelectonFactory(implementBinding);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, T serviceImplement) {
        exportService(serviceInterface, "", defaultTimeout, serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, T serviceImplement) {
        exportService(serviceInterface, group, defaultTimeout, serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceProxyFactory.createSkelecton(serviceInterface, group, timeout, serviceImplement);
    }

    @Override
    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public void destroy() {
        if (serviceProxyFactory != null) {
            serviceProxyFactory.destroy();
        }
    }

}
