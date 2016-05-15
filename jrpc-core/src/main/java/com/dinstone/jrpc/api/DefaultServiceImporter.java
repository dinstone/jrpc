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

package com.dinstone.jrpc.api;

import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.invoker.StubServiceInvoker;
import com.dinstone.jrpc.proxy.ServiceProxy;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.StubProxyFactory;
import com.dinstone.jrpc.transport.ConnectionFactory;

public class DefaultServiceImporter implements ServiceImporter {

    private EndpointConfig endpointConfig = new EndpointConfig();

    private ServiceProxyFactory serviceStubFactory;

    private ReferenceBinding referenceBinding;

    public DefaultServiceImporter(EndpointConfig endpointConfig, ReferenceBinding referenceBinding,
            ConnectionFactory connectionFactory) {
        if (referenceBinding == null) {
            throw new IllegalArgumentException("referenceBinding is null");
        }
        this.referenceBinding = referenceBinding;

        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory is null");
        }
        this.serviceStubFactory = new StubProxyFactory(new StubServiceInvoker(referenceBinding, connectionFactory));

        if (endpointConfig != null) {
            this.endpointConfig.merge(endpointConfig);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#importService(java.lang.Class)
     */
    @Override
    public <T> T importService(Class<T> sic) {
        return importService(sic, "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#importService(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T importService(Class<T> sic, String group) {
        return importService(sic, group, endpointConfig.getDefaultTimeout());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#importService(java.lang.Class, java.lang.String, int)
     */
    @Override
    public <T> T importService(Class<T> sic, String group, int timeout) {
        try {
            ServiceProxy<T> wrapper = serviceStubFactory.createStub(sic, group, timeout);
            referenceBinding.bind(wrapper, endpointConfig);
            return wrapper.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("can't create service proxy", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.ServiceImporter#destroy()
     */
    @Override
    public void destroy() {
        if (serviceStubFactory != null) {
            serviceStubFactory.destroy();
        }
    }

    @Override
    public EndpointConfig getEndpointConfig() {
        return endpointConfig;
    }

}
