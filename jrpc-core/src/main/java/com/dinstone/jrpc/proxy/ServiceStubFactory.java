
package com.dinstone.jrpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.invoker.ServiceInvoker;

public class ServiceStubFactory implements ServiceProxyFactory {

    private ServiceInvoker serviceInvoker;

    private ReferenceBinding referenceBinding;

    public ServiceStubFactory(ReferenceBinding referenceBinding, ServiceInvoker serviceInvoker) {
        super();
        this.referenceBinding = referenceBinding;
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public <T> void createSkelecton(Class<T> serviceInterface, String group, int timeout, T serviceObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createStub(Class<T> si, String group, int timeout) throws Exception {
        ProxyInvocationHandler<T> handler = new ProxyInvocationHandler<T>(si, group, timeout);
        T sr = si.cast(Proxy.newProxyInstance(si.getClassLoader(), new Class[] { si }, handler));

        referenceBinding.bind(si, group, timeout, sr);

        return sr;
    }

    @Override
    public void destroy() {
    }

    private class ProxyInvocationHandler<T> implements InvocationHandler {

        private Class<T> serviceInterface;

        private String group;

        private int timeout;

        public ProxyInvocationHandler(Class<T> serviceInterface, String group, int timeout) {
            this.serviceInterface = serviceInterface;
            this.group = group;
            this.timeout = timeout;
        }

        public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("hashCode")) {
                return new Integer(System.identityHashCode(proxyObj));
            } else if (methodName.equals("equals")) {
                return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("toString")) {
                return proxyObj.getClass().getName() + '@' + Integer.toHexString(proxyObj.hashCode());
            }

            return serviceInvoker.invoke(serviceInterface, group, timeout, method, args);
        }

    }

}
