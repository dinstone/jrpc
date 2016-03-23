/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.mina.client;

import com.dinstone.jrpc.api.ServiceImporter;
import com.dinstone.jrpc.client.AbstractClient;
import com.dinstone.jrpc.client.Client;
import com.dinstone.jrpc.invoker.DefaultReferenceBinding;
import com.dinstone.jrpc.transport.ConnectionFactory;
import com.dinstone.jrpc.transport.TransportConfig;

/**
 * @author guojf
 * @version 1.0.0.2013-5-2
 */
public class MinaClient extends AbstractClient implements Client {

    private TransportConfig config = new TransportConfig();

    private ConnectionFactory connectionFactory;

    private DefaultReferenceBinding referenceBinding;

    public MinaClient(final String host, final int port) {
        connectionFactory = new MinaConnectionFactory(config);
        referenceBinding = new DefaultReferenceBinding(host, port);
        serviceImporter = new ServiceImporter(referenceBinding, connectionFactory);
    }

    public MinaClient(String serviceAddresses) {
        connectionFactory = new MinaConnectionFactory(config);
        referenceBinding = new DefaultReferenceBinding(serviceAddresses);
        serviceImporter = new ServiceImporter(referenceBinding, connectionFactory);
    }

    @Override
    public void destroy() {
        serviceImporter.destroy();
        referenceBinding.destroy();
        connectionFactory.destroy();
    }

    public MinaClient setCallTimeout(int timeout) {
        config.setCallTimeout(timeout);

        return this;
    }

}
