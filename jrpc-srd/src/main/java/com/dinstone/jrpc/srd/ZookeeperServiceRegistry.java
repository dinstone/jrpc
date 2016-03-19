
package com.dinstone.jrpc.srd;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

public class ZookeeperServiceRegistry implements DistributedServiceRegistry {

    private ServiceDiscovery<ServiceAttribute> serviceDiscovery;

    public ZookeeperServiceRegistry(CuratorFramework zkClient, String basePath) {
        if (zkClient == null) {
            throw new IllegalArgumentException("zkClient is null");
        }
        if (basePath == null || basePath.length() == 0) {
            throw new IllegalArgumentException("basePath is empty");
        }

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
    public void regist(ServiceDescription description) throws Exception {
        String serviceName = description.getName() + "-" + description.getGroup();
        UriSpec uriSpec = new UriSpec("{scheme}://{host}:{port}/{name}");

        ServiceInstance<ServiceAttribute> serviceInstance = ServiceInstance.<ServiceAttribute> builder()
            .id(description.getId()).name(serviceName).address(description.getHost()).port(description.getPort())
            .payload(description.getServiceAttribute()).uriSpec(uriSpec).build();

        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void destroy() {
        try {
            serviceDiscovery.close();
        } catch (IOException e) {
        }
    }

}
