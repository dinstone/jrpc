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

package com.dinstone.jrpc.registry.zookeeper;

import com.dinstone.jrpc.registry.RegistryConfig;
import com.dinstone.jrpc.registry.RegistryFactory;
import com.dinstone.jrpc.registry.ServiceDiscovery;
import com.dinstone.jrpc.registry.ServiceRegistry;

public class ZookeeperRegistryFactory implements RegistryFactory {

    private RegistryConfig registryConfig = new RegistryConfig();

    private ServiceRegistry serviceRegistry;

    private ServiceDiscovery serviceDiscovery;

    @Override
    public String getSchema() {
        return "zookeeper";
    }

    @Override
    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    @Override
    public synchronized ServiceRegistry createServiceRegistry() {
        if (serviceRegistry == null) {
            serviceRegistry = new ZookeeperServiceRegistry(new ZookeeperRegistryConfig(registryConfig));
        }
        return serviceRegistry;
    }

    @Override
    public synchronized ServiceDiscovery createServiceDiscovery() {
        if (serviceDiscovery == null) {
            serviceDiscovery = new ZookeeperServiceDiscovery(new ZookeeperRegistryConfig(registryConfig));
        }
        return serviceDiscovery;
    }

    @Override
    public void destroy() {
        if (serviceRegistry != null) {
            serviceRegistry.destroy();
        }
        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
    }

}
