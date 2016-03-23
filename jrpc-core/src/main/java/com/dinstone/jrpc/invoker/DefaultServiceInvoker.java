
package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.dinstone.jrpc.client.Connection;
import com.dinstone.jrpc.client.ConnectionFactory;
import com.dinstone.jrpc.protocol.Call;

public class DefaultServiceInvoker implements ServiceInvoker {

    public DefaultServiceInvoker() {
    }

    @Override
    public <T> Object invoke(ReferenceBinding referenceBinding, ConnectionFactory connectionFactory,
            Class<T> serviceInterface, String group, int timeout, Method method, Object[] args) throws Exception {
        
        
        InetSocketAddress address = referenceBinding.getServiceAddress(serviceInterface, group);
        Connection connection = connectionFactory.create(address);
        return connection.call(new Call(serviceInterface.getName(), group, timeout, method.getName(), args)).get(
            timeout, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
    }

}
