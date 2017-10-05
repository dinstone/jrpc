
package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.transport.Connection;
import com.dinstone.jrpc.transport.ConnectionManager;

public class RemoteInvocationHandler implements InvocationHandler {

    private ConnectionManager connectionManager;

    public RemoteInvocationHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public <T> Object handle(Invocation<T> invocation) throws Exception {
        Connection connection = connectionManager.getConnection(invocation.getServiceAddress());

        String group = invocation.getGroup();
        int timeout = invocation.getTimeout();
        Class<?> service = invocation.getService();

        Method method = invocation.getMethod();
        Object[] args = invocation.getParams();

        Call call = new Call(service.getName(), group, timeout, method.getName(), args, method.getParameterTypes());
        return connection.call(call).get(timeout, TimeUnit.MILLISECONDS);
    }

}
