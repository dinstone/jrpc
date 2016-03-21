
package com.dinstone.jrpc.processor;

import java.lang.reflect.InvocationTargetException;

import com.dinstone.jrpc.protocol.Call;

public interface ServiceProcessor {

    public Object process(Call call) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

    /**
     * service implementation binding
     * 
     * @param serviceInterface
     * @param serviceInstance
     */
    public <T> void bind(Class<T> serviceInterface, T serviceInstance);

    /**
     * service implementation binding
     * 
     * @param serviceInterface
     * @param serviceObject
     * @param group
     */
    public <T> void bind(Class<T> serviceInterface, T serviceObject, String group);

    public void destroy();
}
