
package com.dinstone.jrpc.invoker;

public interface ServiceInvoker {

    /**
     * service reference bind
     * 
     * @param serviceInterface
     * @param serviceReference
     * @param group
     */
    public <T> void bind(Class<T> serviceInterface, T serviceReference, String group);

    Object invoke(String serviceName, String group, int callTimeout, String methodName, Object[] args) throws Exception;

    void destroy();
}
