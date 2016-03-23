
package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;

import com.dinstone.jrpc.transport.ConnectionFactory;

public interface ServiceInvoker {

    <T> Object invoke(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory,
            Class<T> serviceInterface, String group, int timeout, Method method, Object[] args) throws Exception;

    void destroy();
}
