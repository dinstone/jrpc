
package com.dinstone.jrpc.invoker;

public interface ServiceInvoker {

    Object invoke(String serviceName, String group, int callTimeout, String methodName, Object[] args) throws Exception;

    public void destroy();
}
