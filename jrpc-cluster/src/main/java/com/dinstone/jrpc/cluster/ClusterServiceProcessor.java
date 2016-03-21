
package com.dinstone.jrpc.cluster;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.processor.AbstractServiceProcessor;
import com.dinstone.jrpc.srd.DistributedServiceRegistry;
import com.dinstone.jrpc.srd.ServiceDescription;
import com.dinstone.jrpc.srd.ZookeeperServiceRegistry;

public class ClusterServiceProcessor extends AbstractServiceProcessor {

    private static final String JRPC_PATH = "/discovery/jrpc";

    private CuratorFramework zkClient;

    private DistributedServiceRegistry serviceRegistry;

    private Configuration config;

    public ClusterServiceProcessor(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.config = config;

        String zkNodes = config.get("zookeeper.node.list");
        if (zkNodes == null || zkNodes.length() == 0) {
            throw new IllegalArgumentException("zookeeper.node.list is empty");
        }

        zkClient = CuratorFrameworkFactory.newClient(zkNodes, new ExponentialBackoffRetry(3000, 3));
        zkClient.start();

        serviceRegistry = new ZookeeperServiceRegistry(zkClient, JRPC_PATH);
    }

    @Override
    public <T> void bind(Class<T> serviceInterface, T serviceObject, String group) {
        super.bind(serviceInterface, serviceObject, group);

        ServiceDescription description = new ServiceDescription();
        description.setId(config.getServiceHost() + ":" + config.getServicePort());
        description.setHost(config.getServiceHost());
        description.setPort(config.getServicePort());
        description.setName(serviceInterface.getName());
        description.setGroup(group);
        try {
            serviceRegistry.publish(description);
        } catch (Exception e) {
            throw new RuntimeException("can't regist service", e);
        }

    }

    @Override
    public void destroy() {
        super.destroy();

        serviceRegistry.destroy();
    }

}
