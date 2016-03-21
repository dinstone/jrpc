
package com.dinstone.jrpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.invoker.ServiceInvoker;

public class DefaultServiceProxyFactory implements ServiceProxyFactory {

    private Configuration config;

    private ServiceInvoker serviceInvoker;

    public DefaultServiceProxyFactory(Configuration config, ServiceInvoker serviceInvoker) {
        super();
        this.config = config;
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public <T> T createProxy(Class<T> si, String group) throws Exception {
        T sr = si.cast(Proxy.newProxyInstance(si.getClassLoader(), new Class[] { si }, new JdkInvocationHandler<T>(si,
            group)));
        serviceInvoker.bind(si, sr, group);

        return sr;
    }

    @Override
    public void destroy() {
    }

    private class JdkInvocationHandler<T> implements InvocationHandler {

        private Class<T> serviceInterface;

        private String group;

        public JdkInvocationHandler(Class<T> serviceInterface, String group) {
            this.serviceInterface = serviceInterface;
            this.group = group;
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
         *      java.lang.Object[])
         */
        public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("hashCode")) {
                return new Integer(System.identityHashCode(proxyObj));
            } else if (methodName.equals("equals")) {
                return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (methodName.equals("toString")) {
                return proxyObj.getClass().getName() + '@' + Integer.toHexString(proxyObj.hashCode());
            }

            // methodName = method.getDeclaringClass().getName() + "." + methodName;

            return invoke(serviceInterface, group, config.getCallTimeout(), method, args);
        }

        private Object invoke(Class<T> serviceInterface, String group, int callTimeout, Method method, Object[] args)
                throws Exception {
            return serviceInvoker.invoke(serviceInterface.getName(), group, callTimeout, method.getName(), args);
        }

    }

}
