
package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;

public class Invocation<T> {

    private Class<T> service;

    private String group;

    private int timeout;

    private T instance;

    private Method method;

    private Object[] params;

    public Invocation(Class<T> service, String group, int timeout, T instance, Method method, Object[] params) {
        super();
        this.service = service;
        this.group = group;
        this.timeout = timeout;
        this.instance = instance;
        this.method = method;
        this.params = params;
    }

    public Class<T> getService() {
        return service;
    }

    public void setService(Class<T> service) {
        this.service = service;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

}
