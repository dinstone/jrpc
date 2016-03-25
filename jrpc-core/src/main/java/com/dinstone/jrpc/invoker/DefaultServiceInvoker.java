
package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ConnectionFactory;

public class DefaultServiceInvoker implements ServiceInvoker {

    private ConnectionFactory connectionFactory;

    public DefaultServiceInvoker(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> Object invoke(ReferenceBinding referenceBinding, Class<T> serviceInterface,
            String group, int timeout, Method method, Object[] args) throws Exception {

        InetSocketAddress address = referenceBinding.getServiceAddress(serviceInterface, group);
        Connection connection = connectionFactory.create(address);
        return connection.call(new Call(serviceInterface.getName(), group, timeout, method.getName(), args)).get(
            timeout, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
    }

}
