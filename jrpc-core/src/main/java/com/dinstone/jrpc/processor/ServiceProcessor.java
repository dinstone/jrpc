
package com.dinstone.jrpc.processor;

import java.lang.reflect.InvocationTargetException;

import com.dinstone.jrpc.protocol.Call;

public interface ServiceProcessor {

    public Object process(Service<?> service, Call call) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException;

    public void destroy();
}
