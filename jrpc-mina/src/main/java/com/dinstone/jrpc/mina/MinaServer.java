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

package com.dinstone.jrpc.mina;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.api.DefaultServiceExporter;
import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.api.ServiceExporter;
import com.dinstone.jrpc.binding.DefaultImplementBinding;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.mina.transport.MinaAcceptance;
import com.dinstone.jrpc.transport.TransportConfig;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class MinaServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(MinaServer.class);

    private ImplementBinding implementBinding;

    private ServiceExporter serviceExporter;

    private MinaAcceptance acceptance;

    public MinaServer(String host, int port) {
        this(host, port, new TransportConfig());
    }

    public MinaServer(String host, int port, TransportConfig transportConfig) {
        this(new DefaultImplementBinding(host, port), transportConfig);
    }

    public MinaServer(ImplementBinding implementBinding, TransportConfig transportConfig) {
        this.implementBinding = implementBinding;
        this.serviceExporter = new DefaultServiceExporter(implementBinding);

        this.acceptance = new MinaAcceptance(transportConfig, implementBinding);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.Server#start()
     */
    @Override
    public void start() {
        acceptance.bind();
        LOG.info("jrpc server start on {}", implementBinding.getServiceAddress());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.api.Server#stop()
     */
    @Override
    public void stop() {
        acceptance.destroy();
        serviceExporter.destroy();
        implementBinding.destroy();

        LOG.info("jrpc server stop on {}", implementBinding.getServiceAddress());
    }

    @Override
    public <T> void regist(Class<T> serviceInterface, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, serviceImplement);
    }

    @Override
    public <T> void regist(Class<T> serviceInterface, String group, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, serviceImplement);
    }

    @Override
    public <T> void regist(Class<T> serviceInterface, String group, int timeout, T serviceImplement) {
        serviceExporter.exportService(serviceInterface, group, timeout, serviceImplement);
    }

    public void setDefaultTimeout(int defaultTimeout) {
        serviceExporter.setDefaultTimeout(defaultTimeout);
    }

}
