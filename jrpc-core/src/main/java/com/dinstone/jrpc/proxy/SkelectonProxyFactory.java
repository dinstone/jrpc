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

package com.dinstone.jrpc.proxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SkelectonProxyFactory implements ServiceProxyFactory {

    public SkelectonProxyFactory() {
    }

    public <T> ServiceProxy<T> createSkelecton(Class<T> serviceInterface, String group, int timeout, T serviceObject) {
        Map<String, Method> methodMap = new HashMap<String, Method>();
        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            if (methodMap.containsKey(method.getName())) {
                throw new RuntimeException("method overloading is not supported");
            }
            methodMap.put(method.getName(), method);
        }

        return new ServiceProxy<T>(serviceInterface, group, timeout, serviceObject, methodMap);
    }

    @Override
    public <T> ServiceProxy<T> createStub(Class<T> si, String group, int timeout) throws Exception {
        // ignore
        return null;
    }

    @Override
    public void destroy() {
    }
}
