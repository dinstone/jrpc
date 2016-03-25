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
