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

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.endpoint.ServiceExporter;
import com.dinstone.jrpc.transport.Acceptance;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private ServiceExporter serviceExporter;

    private ImplementBinding implementBinding;

    private Acceptance acceptance;

    Server(ServiceExporter serviceExporter, ImplementBinding implementBinding, Acceptance acceptance) {
        this.serviceExporter = serviceExporter;
        this.acceptance = acceptance;
        this.implementBinding = implementBinding;
    }

    public synchronized Server start() {
        acceptance.bind();

        LOG.info("JRPC server is started", implementBinding.getServiceAddress());

        return this;
    }

    public synchronized Server stop() {
        if (acceptance != null) {
            acceptance.destroy();
        }
        if (serviceExporter != null) {
            serviceExporter.destroy();
        }
        if (implementBinding != null) {
            implementBinding.destroy();
        }

        LOG.info("JRPC server is stopped", implementBinding.getServiceAddress());

        return this;
    }

    public ServiceExporter serviceExporter() {
        return serviceExporter;
    }

    public InetSocketAddress getServiceAddress() {
        return implementBinding.getServiceAddress();
    }

}
