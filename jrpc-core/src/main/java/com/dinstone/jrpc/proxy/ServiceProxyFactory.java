
package com.dinstone.jrpc.proxy;

public interface ServiceProxyFactory {

    public <T> void createSkelecton(Class<T> serviceInterface, String group, int timeout, T serviceObject);

    public <T> T createStub(Class<T> si, String group, int timeout) throws Exception;

    public void destroy();
}
