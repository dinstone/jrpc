
package com.dinstone.jrpc.api;

import com.dinstone.jrpc.processor.DefaultServiceProcessor;
import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.ServiceSkelectonFactory;
import com.dinstone.jrpc.server.Acceptance;
import com.dinstone.jrpc.server.AcceptanceFactory;

public class ServiceExporter {

    private ServiceProxyFactory serviceSkelectonFactory;

    private Acceptance acceptance;

    public ServiceExporter(ImplementBinding implementBinding, AcceptanceFactory acceptanceFactory) {
        this.serviceSkelectonFactory = new ServiceSkelectonFactory(implementBinding);
        this.acceptance = acceptanceFactory.create(implementBinding, new DefaultServiceProcessor());
    }

    public <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceSkelectonFactory.createSkelecton(serviceInterface, group, timeout, serviceImplement);
    }

    public void destroy() {
        if (serviceSkelectonFactory != null) {
            serviceSkelectonFactory.destroy();
        }
        if (acceptance != null) {
            acceptance.destroy();
        }
    }
}
