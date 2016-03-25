
package com.dinstone.jrpc.srd;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

public class ZookeeperServiceRegistry implements DistributedServiceRegistry {

    private CuratorFramework zkClient;

    private ServiceDiscovery<ServiceAttribute> serviceDiscovery;

    public ZookeeperServiceRegistry(RegistryDiscoveryConfig registryConfig) {
        String zkNodes = registryConfig.getZookeeperNodes();
        if (zkNodes == null || zkNodes.length() == 0) {
            throw new IllegalArgumentException("zookeeper.node.list is empty");
        }

        String basePath = registryConfig.getBasePath();
        if (basePath == null || basePath.length() == 0) {
            throw new IllegalArgumentException("basePath is empty");
        }

        zkClient = CuratorFrameworkFactory.newClient(zkNodes,
            new ExponentialBackoffRetry(registryConfig.getBaseSleepTime(), registryConfig.getMaxRetries()));
        zkClient.start();

        try {
            serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceAttribute.class).client(zkClient)
                .basePath(basePath).serializer(new JsonInstanceSerializer<ServiceAttribute>(ServiceAttribute.class))
                .build();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publish(ServiceDescription description) throws Exception {
        String serviceName = description.getName() + "-" + description.getGroup();

        ServiceInstance<ServiceAttribute> serviceInstance = ServiceInstance.<ServiceAttribute> builder()
            .id(description.getId()).name(serviceName).address(description.getHost()).port(description.getPort())
            .payload(description.getServiceAttribute()).build();

        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void destroy() {
        zkClient.close();
        try {
            serviceDiscovery.close();
        } catch (IOException e) {
        }
    }

}
