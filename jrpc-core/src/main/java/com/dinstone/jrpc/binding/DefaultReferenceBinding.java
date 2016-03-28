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

package com.dinstone.jrpc.binding;

import java.net.InetSocketAddress;

import com.dinstone.jrpc.srd.ServiceDiscovery;

public class DefaultReferenceBinding extends AbstractReferenceBinding {

    public DefaultReferenceBinding(String host, int port) {
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("host is null");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than zero");
        }

        backupServiceAddresses.add(new InetSocketAddress(host, port));
    }

    public DefaultReferenceBinding(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public DefaultReferenceBinding(String serviceAddresses) {
        this(serviceAddresses, null);
    }

    public DefaultReferenceBinding(String serviceAddresses, ServiceDiscovery serviceDiscovery) {
        if (serviceAddresses == null || serviceAddresses.length() == 0) {
            throw new IllegalArgumentException("serviceAddresses is empty");
        }

        String hostsList[] = serviceAddresses.split(",");
        for (String host : hostsList) {
            int port = 0;
            int pidx = host.lastIndexOf(':');
            if (pidx > 0 && (pidx < host.length() - 1)) {
                port = Integer.parseInt(host.substring(pidx + 1));
                host = host.substring(0, pidx);

                backupServiceAddresses.add(new InetSocketAddress(host, port));
            }
        }

        this.serviceDiscovery = serviceDiscovery;
    }

}
