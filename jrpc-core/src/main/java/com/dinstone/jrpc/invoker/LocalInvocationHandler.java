package com.dinstone.jrpc.invoker;

public class LocalInvocationHandler implements InvocationHandler {

    @Override
    public <T> Object handle(Invocation<T> invocation) throws Exception {
        return invocation.getMethod().invoke(invocation.getInstance(), invocation.getParams());
    }

}
