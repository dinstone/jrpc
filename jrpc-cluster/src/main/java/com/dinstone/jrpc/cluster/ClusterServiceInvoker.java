
package com.dinstone.jrpc.cluster;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.client.Connection;
import com.dinstone.jrpc.client.ConnectionManager;
import com.dinstone.jrpc.invoker.ServiceInvoker;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.srd.DistributedServiceDiscovery;
import com.dinstone.jrpc.srd.ServiceDescription;
import com.dinstone.jrpc.srd.ZookeeperServiceDiscovery;

public class ClusterServiceInvoker implements ServiceInvoker {

    private static final String JRPC_PATH = "/discovery/jrpc";

    private final AtomicInteger index = new AtomicInteger(0);

    private ConnectionManager connectionManager;

    private CuratorFramework zkClient;

    private DistributedServiceDiscovery serviceDiscovery;

    public ClusterServiceInvoker(Configuration config, ConnectionManager connectionManager) {
        if (connectionManager == null) {
            throw new IllegalArgumentException("connectionManager is null");
        }
        this.connectionManager = connectionManager;

        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }

        String zkNodes = config.get("zookeeper.node.list");
        if (zkNodes == null || zkNodes.length() == 0) {
            throw new IllegalArgumentException("zookeeper.node.list is empty");
        }

        zkClient = CuratorFrameworkFactory.newClient(zkNodes, new ExponentialBackoffRetry(3000, 3));
        zkClient.start();

        serviceDiscovery = new ZookeeperServiceDiscovery(zkClient, JRPC_PATH);
    }

    @Override
    public <T> void bind(Class<T> serviceInterface, T serviceReference, String group) {
        try {
            serviceDiscovery.listen(serviceInterface.getName(), group);
        } catch (Exception e) {
            throw new RuntimeException("service reference bind error", e);
        }
    }

    @Override
    public Object invoke(String serviceName, String group, int callTimeout, String methodName, Object[] args)
            throws Exception {
        ServiceDescription serviceDescription = locateService(serviceName, group);
        if (serviceDescription == null) {
            throw new RuntimeException("service " + serviceName + "[" + group + "] is not ready");
        }

        Configuration config = new Configuration();
        config.setServiceHost(serviceDescription.getHost());
        config.setServicePort(serviceDescription.getPort());
        Connection connection = connectionManager.getConnection(config);

        return connection.call(new Call(serviceName + "." + methodName, args)).get(callTimeout, TimeUnit.MILLISECONDS);
    }

    private ServiceDescription locateService(String serviceName, String group) throws Exception {
        List<ServiceDescription> serviceDescriptions = serviceDiscovery.discovery(serviceName, group);
        if (serviceDescriptions.size() == 0) {
            return null;
        }
        int thisIndex = Math.abs(index.getAndIncrement());
        return serviceDescriptions.get(thisIndex % serviceDescriptions.size());
    }

    @Override
    public void destroy() {
        serviceDiscovery.destroy();
        zkClient.close();
    }
}
