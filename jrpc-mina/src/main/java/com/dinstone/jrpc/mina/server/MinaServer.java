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

package com.dinstone.jrpc.mina.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.api.ServiceExporter;
import com.dinstone.jrpc.processor.DefaultImplementBinding;
import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.server.Server;
import com.dinstone.jrpc.transport.TransportConfig;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class MinaServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(MinaServer.class);

    private TransportConfig transportConfig = new TransportConfig();

    private ImplementBinding implementBinding;

    private MinaAcceptance acceptance;

    private ServiceExporter serviceExporter;

    public MinaServer(String host, int port) {
        implementBinding = new DefaultImplementBinding(host, port);
        serviceExporter = new ServiceExporter(implementBinding, new MinaAcceptanceFactory(transportConfig));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.server.Server#stop()
     */
    @Override
    public void stop() {
        serviceExporter.destroy();
        implementBinding.destroy();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.server.Server#start()
     */
    @Override
    public void start() {
    }

    public <T> void regist(Class<T> serviceInterface, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, "", 3000, serviceImplement);
    }

    public <T> void regist(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, timeout, serviceImplement);
    }
}
