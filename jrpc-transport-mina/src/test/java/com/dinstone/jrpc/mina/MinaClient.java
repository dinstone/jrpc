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
package com.dinstone.jrpc.mina;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dinstone.jrpc.binding.ReferenceBinding;
import com.dinstone.jrpc.endpoint.DefaultServiceImporter;
import com.dinstone.jrpc.endpoint.EndpointConfig;
import com.dinstone.jrpc.endpoint.ServiceImporter;
import com.dinstone.jrpc.transport.TransportConfig;

/**
 * @author guojf
 * @version 1.0.0.2013-5-2
 */
public class MinaClient {

    private ReferenceBinding referenceBinding;

    private ServiceImporter serviceImporter;

    public MinaClient(final String host, final int port) {
        this(host, port, new TransportConfig());
    }

    public MinaClient(final String host, final int port, TransportConfig config) {
        config.setSchema("mina");
        List<InetSocketAddress> addresses = new ArrayList<>();
        addresses.add(new InetSocketAddress(host, port));
        serviceImporter = new DefaultServiceImporter(new EndpointConfig(), null, null);
    }

    public <T> T getService(Class<T> sic) {
        return serviceImporter.importService(sic);
    }

    public <T> T getService(Class<T> sic, String group) {
        return serviceImporter.importService(sic, group);
    }

    public <T> T getService(Class<T> sic, String group, int timeout) {
        return serviceImporter.importService(sic, group, timeout);
    }

    public void destroy() {
        serviceImporter.destroy();
        referenceBinding.destroy();
    }

    public MinaClient setDefaultTimeout(int timeout) {
        return this;
    }

}
