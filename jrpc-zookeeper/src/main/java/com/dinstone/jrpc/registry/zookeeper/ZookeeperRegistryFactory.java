
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
