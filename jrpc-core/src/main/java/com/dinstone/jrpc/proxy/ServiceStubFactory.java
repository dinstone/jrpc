
package com.dinstone.jrpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.dinstone.jrpc.client.ConnectionFactory;
import com.dinstone.jrpc.invoker.ReferenceBinding;
import com.dinstone.jrpc.invoker.ServiceInvoker;

public class ServiceStubFactory implements ServiceProxyFactory {

    private ServiceInvoker serviceInvoker;

    private ReferenceBinding referenceBinding;

    private ConnectionFactory connectionFactory;

    public ServiceStubFactory(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory,
            ServiceInvoker serviceInvoker) {
        super();
        this.referenceBinding = referenceBinding;
        this.connectionFactory = connectionFactory;
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public <T> T createProxy(Class<T> si, String group, int timeout) throws Exception {
        JdkInvocationHandler<T> handler = new JdkInvocationHandler<T>(si, group, timeout);
        T sr = si.cast(Proxy.newProxyInstance(si.getClassLoader(), new Class[] { si }, handler));

        referenceBinding.bind(si, group, sr);

        return sr;
    }

    @Override
    public void destroy() {
    }

    private class JdkInvocationHandler<T> implements InvocationHandler {

        private Class<T> serviceInterface;

        private String group;

        private int timeout;

        public JdkInvocationHandler(Class<T> serviceInterface, String group, int timeout) {
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

            return serviceInvoker.invoke(referenceBinding, connectionFactory, serviceInterface, group, timeout, method,
                args);
        }

    }

}
