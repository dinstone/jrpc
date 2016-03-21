
package com.dinstone.jrpc.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.RpcException;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.service.Service;

public abstract class AbstractServiceProcessor implements ServiceProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceProcessor.class);

    private Map<Class<?>, Object> interfaceMap = new ConcurrentHashMap<Class<?>, Object>();

    protected Map<String, Service> serviceMap = new ConcurrentHashMap<String, Service>();

    @Override
    public <T> void bind(Class<T> serviceInterface, T serviceObject) {
        bind(serviceInterface, serviceObject, "");
    }

    public synchronized <T> void bind(Class<T> serviceInterface, T serviceObject, String group) {
        if (!serviceInterface.isInstance(serviceObject)) {
            String message = "the specified service object[" + serviceObject.getClass()
                    + "] is not assignment-compatible with the object represented by this Class[" + serviceInterface
                    + "].";
            LOG.warn(message);
            throw new RpcException(501, message);
        }

        Object obj = interfaceMap.get(serviceInterface);
        if (obj != null) {
            throw new RpcException(502, "multiple object registed with the service interface " + serviceInterface);
        } else {
            interfaceMap.put(serviceInterface, serviceObject);
        }

        String classPrefix = serviceInterface.getName() + ".";
        Map<String, Service> tmpMap = new HashMap<String, Service>();
        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            Service service = new Service(serviceObject, method);
            String key = classPrefix + method.getName();
            if (tmpMap.containsKey(key)) {
                throw new RpcException(503, "method overloading is not supported");
            }
            tmpMap.put(key, service);
        }

        serviceMap.putAll(tmpMap);
    }

    @Override
    public Object process(Call call) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Service service = find(call.getMethod());
        if (service == null) {
            throw new IllegalAccessException("not published service");
        }
        return service.call(call.getParams());
    }

    @Override
    public void destroy() {
        serviceMap.clear();
        interfaceMap.clear();
    }

    private Service find(String methodName) {
        return serviceMap.get(methodName);
    }

}
