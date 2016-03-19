
package com.dinstone.jrpc.processor;

import java.lang.reflect.InvocationTargetException;

import com.dinstone.jrpc.protocol.Call;

public interface ServiceProcessor {

    public Object process(Call call) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;

    public <T> void regist(Class<T> serviceInterface, T serviceObject);

    public void destroy();
}
