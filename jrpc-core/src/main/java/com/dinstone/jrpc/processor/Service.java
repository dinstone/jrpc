/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.jrpc.processor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Service<T> {

    private Class<T> serviceInterface;

    private String group;

    private int timeout;

    private T instance;

    private Map<String, Method> methodMap = new HashMap<String, Method>();

    public Service(Class<T> serviceInterface, String group, int timeout, T instance, Map<String, Method> methodMap) {
        super();
        this.serviceInterface = serviceInterface;
        this.group = group;
        this.timeout = timeout;
        this.instance = instance;
        this.methodMap.putAll(methodMap);
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public String getGroup() {
        return group;
    }

    public int getTimeout() {
        return timeout;
    }

    public T getInstance() {
        return instance;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

}