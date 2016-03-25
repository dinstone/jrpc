
package com.dinstone.jrpc.proxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.processor.Service;

public class ServiceSkelectonFactory implements ServiceProxyFactory {

    private ImplementBinding implementBinding;

    public ServiceSkelectonFactory(ImplementBinding implementBinding) {
        this.implementBinding = implementBinding;
    }

    public <T> void createSkelecton(Class<T> serviceInterface, String group, int timeout, T serviceObject) {
        if (!serviceInterface.isInstance(serviceObject)) {
            String message = "the specified service object[" + serviceObject.getClass()
                    + "] is not assignment-compatible with the object represented by this Class[" + serviceInterface
                    + "].";
            throw new RuntimeException(message);
        }

        Map<String, Method> methodMap = new HashMap<String, Method>();
        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            if (methodMap.containsKey(method.getName())) {
                throw new RuntimeException("method overloading is not supported");
            }
            methodMap.put(method.getName(), method);
        }
        Service<T> wrapper = new Service<T>(serviceInterface, group, timeout, serviceObject, methodMap);

        implementBinding.bind(serviceInterface, group, wrapper);
    }

    @Override
    public <T> T createStub(Class<T> si, String group, int timeout) throws Exception {
        // ignore
        return null;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }
}
