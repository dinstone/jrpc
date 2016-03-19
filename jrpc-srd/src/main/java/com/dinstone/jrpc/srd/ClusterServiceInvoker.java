
package com.dinstone.jrpc.srd;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.invoker.ServiceInvoker;

public class ClusterServiceInvoker implements ServiceInvoker {

    private static final String JRPC_PATH = "/discovery/jrpc";

    private final AtomicInteger index = new AtomicInteger(0);

    private Configuration config;

    private CuratorFramework zkClient;

    private DistributedServiceDiscovery serviceDiscovery;

    public ClusterServiceInvoker(Configuration config) {
        super();
        this.config = config;
        zkClient = CuratorFrameworkFactory.newClient(config.get("zookeeper.node.list"), new ExponentialBackoffRetry(
            3000, 3));
        serviceDiscovery = new ZookeeperServiceDiscovery(zkClient, JRPC_PATH);
    }

    @Override
    public Object invoke(String serviceName, String group, int callTimeout, String methodName, Object[] args)
            throws Exception {
        ServiceDescription serviceDescription = locateService(serviceName, group);
        if (serviceDescription == null) {
            throw new RuntimeException("service " + serviceName + "[" + group + "] is not ready");
        }
        
        

        return null;
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
