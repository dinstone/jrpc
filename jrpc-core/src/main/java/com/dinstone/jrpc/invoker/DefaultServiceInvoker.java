
package com.dinstone.jrpc.invoker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dinstone.jrpc.client.ConnectionFactory;
import com.dinstone.jrpc.protocol.Call;

public class DefaultServiceInvoker implements ServiceInvoker {

    private ConnectionFactory connectionFactory;

    public DefaultServiceInvoker(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Object invoke(String serviceName, String group, int callTimeout, String methodName, Object[] params)
            throws InterruptedException, TimeoutException {
        String service = serviceName + "." + methodName;
        return connectionFactory.create().call(new Call(service, params)).get(callTimeout, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}
