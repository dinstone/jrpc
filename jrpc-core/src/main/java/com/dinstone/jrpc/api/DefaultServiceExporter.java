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

import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.proxy.ServiceProxyFactory;
import com.dinstone.jrpc.proxy.ServiceSkelectonFactory;

public class DefaultServiceExporter implements ServiceExporter {

    private int defaultTimeout = DEFAULT_TIMEOUT;

    private ServiceProxyFactory serviceProxyFactory;

    public DefaultServiceExporter(ImplementBinding implementBinding) {
        this.serviceProxyFactory = new ServiceSkelectonFactory(implementBinding);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, T serviceImplement) {
        exportService(serviceInterface, "", defaultTimeout, serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, T serviceImplement) {
        exportService(serviceInterface, group, defaultTimeout, serviceImplement);
    }

    @Override
    public <T> void exportService(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceProxyFactory.createSkelecton(serviceInterface, group, timeout, serviceImplement);
    }

    @Override
    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public void destroy() {
        if (serviceProxyFactory != null) {
            serviceProxyFactory.destroy();
        }
    }

}
