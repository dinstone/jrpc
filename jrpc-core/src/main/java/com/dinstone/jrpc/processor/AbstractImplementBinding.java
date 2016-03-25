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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractImplementBinding implements ImplementBinding {

    protected Map<String, Service<?>> serviceMap = new ConcurrentHashMap<String, Service<?>>();

    protected InetSocketAddress serviceAddress;

    public <T> void bind(Class<T> serviceInterface, String group, Service<T> serviceWrapper) {
        String serviceId = serviceInterface.getName() + "-" + group;
        Service<?> wrapper = serviceMap.get(serviceId);
        if (wrapper != null) {
            throw new RuntimeException("multiple object registed with the service interface " + serviceId);
        }
        serviceMap.put(serviceId, serviceWrapper);
    }

    @Override
    public Service<?> findService(String service, String group, String method) {
        String serviceId = service + "-" + group;
        Service<?> wrapper = serviceMap.get(serviceId);
        if (wrapper != null && wrapper.getMethodMap().containsKey(method)) {
            return wrapper;
        }

        return null;
    }

    @Override
    public <T> InetSocketAddress getServiceAddress() {
        return serviceAddress;
    }

    @Override
    public void destroy() {
    }

}
