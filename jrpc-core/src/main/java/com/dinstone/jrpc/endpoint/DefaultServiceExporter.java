/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
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
package com.dinstone.jrpc.endpoint;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.StubServiceProxyFactory;

public class DefaultServiceExporter implements ServiceExporter {

    private EndpointConfig endpointConfig = new EndpointConfig();

    private ImplementBinding implementBinding;

    private ServiceProxyFactory serviceProxyFactory;

    public DefaultServiceExporter(EndpointConfig endpointConfig, ImplementBinding implementBinding) {
        if (endpointConfig == null) {
            throw new IllegalArgumentException("endpointConfig is null");
        }
        this.endpointConfig = endpointConfig;

        if (implementBinding == null) {
            throw new IllegalArgumentException("implementBinding is null");
        }
        this.implementBinding = implementBinding;

        this.serviceProxyFactory = new StubServiceProxyFactory(null);

    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, T serviceImplement) {
        exportService(serviceInterface, "", endpointConfig.getDefaultTimeout(), serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, T serviceImplement) {
        exportService(serviceInterface, group, endpointConfig.getDefaultTimeout(), serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        if (group == null) {
            group = "";
        }
        if (timeout <= 0) {
            timeout = endpointConfig.getDefaultTimeout();
        }

        try {
            ServiceProxy<T> wrapper = serviceProxyFactory.create(serviceInterface, group, timeout, serviceImplement);
            implementBinding.bind(wrapper, endpointConfig);
        } catch (Exception e) {
            throw new RuntimeException("can't export service", e);
        }
    }

    @Override
    public void destroy() {
    }

}
