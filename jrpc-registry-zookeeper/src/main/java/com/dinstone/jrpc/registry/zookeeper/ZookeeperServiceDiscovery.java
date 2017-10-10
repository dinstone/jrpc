/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ThreadUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.registry.ServiceDescription;

public class ZookeeperServiceDiscovery implements com.dinstone.jrpc.registry.ServiceDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    private final ServiceDescriptionSerializer serializer = new ServiceDescriptionSerializer();

    private final ConcurrentHashMap<String, ServiceCache> serviceCacheMap = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<ServiceDescription> listenServices = new ConcurrentLinkedQueue<>();

    private volatile ConnectionState connectionState = ConnectionState.LOST;

    private final String basePath;

    private final CuratorFramework client;

    private ConnectionStateListener connectionStateListener;

    public ZookeeperServiceDiscovery(ZookeeperRegistryConfig discoveryConfig) {
        String zkNodes = discoveryConfig.getZookeeperNodes();
        if (zkNodes == null || zkNodes.length() == 0) {
            throw new IllegalArgumentException("zookeeper.node.list is empty");
        }

        String basePath = discoveryConfig.getBasePath();
        if (basePath == null || basePath.length() == 0) {
            throw new IllegalArgumentException("basePath is empty");
        }
        this.basePath = basePath;

        // build CuratorFramework Object;
        this.client = CuratorFrameworkFactory.newClient(zkNodes,
            new ExponentialBackoffRetry(discoveryConfig.getBaseSleepTime(), discoveryConfig.getMaxRetries()));

        // add connection state change listener
        this.connectionStateListener = new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                connectionState = newState;
                if ((newState == ConnectionState.RECONNECTED) || (newState == ConnectionState.CONNECTED)) {
                    try {
                        watch();
                    } catch (Exception e) {
                        LOG.error("Could not re-register instances after reconnection", e);
                    }
                }
            }
        };
        this.client.getConnectionStateListenable().addListener(connectionStateListener);

        // start CuratorFramework service;
        this.client.start();
    }

    protected void watch() throws Exception {
        ServiceDescription serviceDescription = null;
        while ((serviceDescription = listenServices.peek()) != null) {
            internalListen(serviceDescription);

            listenServices.remove();
        }
    }

    @Override
    public void destroy() {
        for (ServiceCache serviceCache : serviceCacheMap.values()) {
            serviceCache.destroy();
        }
        serviceCacheMap.clear();

        client.close();
    }

    @Override
    public void cancel(ServiceDescription description) {
        ServiceCache serviceCache = serviceCacheMap.get(description.getName());
        if (serviceCache != null) {
            serviceCache.destroy();
            serviceCacheMap.remove(description.getName());
        }
    }

    @Override
    public void listen(ServiceDescription description) throws Exception {
        if (connectionState != ConnectionState.CONNECTED) {
            listenServices.add(description);
        } else {
            internalListen(description);
        }
    }

    protected void internalListen(ServiceDescription description) throws Exception {
        ServiceCache serviceCache = null;

        synchronized (serviceCacheMap) {
            serviceCache = serviceCacheMap.get(description.getName());
            if (serviceCache == null) {
                String providerPath = pathForProviders(description.getName());
                ThreadFactory threadFactory = ThreadUtils.newThreadFactory("ServiceDiscovery");
                serviceCache = new ServiceCache(client, providerPath, threadFactory).build();
                serviceCacheMap.put(description.getName(), serviceCache);
            }
        }

        serviceCache.addConsumer(description);
    }

    @Override
    public List<ServiceDescription> discovery(String serviceName) throws Exception {
        ServiceCache serviceCache = serviceCacheMap.get(serviceName);
        if (serviceCache != null) {
            return serviceCache.getProviders();
        }
        return null;
    }

    private String pathForConsumer(String name, String id) {
        return ZKPaths.makePath(pathForService(name) + "/consumers", id);
    }

    private String pathForProviders(String name) {
        return ZKPaths.makePath(pathForService(name) + "/providers", "");
    }

    private String pathForService(String name) {
        return ZKPaths.makePath(basePath, name);
    }

    public class ServiceCache implements PathChildrenCacheListener {

        private final ConcurrentHashMap<String, ServiceDescription> providers = new ConcurrentHashMap<>();

        private final ConcurrentHashMap<String, ServiceDescription> consumers = new ConcurrentHashMap<>();

        private PathChildrenCache cache;

        public ServiceCache(CuratorFramework client, String name, ThreadFactory threadFactory) {
            cache = new PathChildrenCache(client, name, true, threadFactory);
            cache.getListenable().addListener(this);
        }

        public List<ServiceDescription> getProviders() {
            ArrayList<ServiceDescription> pl = new ArrayList<>(providers.size());
            pl.addAll(providers.values());
            return pl;
        }

        public ServiceCache build() throws Exception {
            cache.start(StartMode.BUILD_INITIAL_CACHE);
            // init cache data
            for (ChildData childData : cache.getCurrentData()) {
                addProvider(childData, true);
            }

            return this;
        }

        public void addConsumer(ServiceDescription service) throws Exception {
            synchronized (consumers) {
                if (consumers.containsKey(service.getId())) {
                    return;
                }

                byte[] bytes = serializer.serialize(service);
                String path = pathForConsumer(service.getName(), service.getId());

                final int MAX_TRIES = 2;
                boolean isDone = false;
                for (int i = 0; !isDone && (i < MAX_TRIES); ++i) {
                    try {
                        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, bytes);
                        isDone = true;
                    } catch (KeeperException.NodeExistsException e) {
                        // must delete then re-create so that watchers fire
                        client.delete().forPath(path);
                    }
                }

                consumers.put(service.getId(), service);
            }
        }

        private void addProvider(ChildData childData, boolean onlyIfAbsent) throws Exception {
            String instanceId = ZKPaths.getNodeFromPath(childData.getPath());
            ServiceDescription serviceInstance = serializer.deserialize(childData.getData());
            if (onlyIfAbsent) {
                providers.putIfAbsent(instanceId, serviceInstance);
            } else {
                providers.put(instanceId, serviceInstance);
            }
            cache.clearDataBytes(childData.getPath(), childData.getStat().getVersion());
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            switch (event.getType()) {
                case CHILD_ADDED:
                case CHILD_UPDATED: {
                    addProvider(event.getData(), false);
                    break;
                }

                case CHILD_REMOVED: {
                    providers.remove(ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }
                default:
                    break;
            }
        }

        public void destroy() {
            if (connectionState == ConnectionState.CONNECTED) {
                for (ServiceDescription consumer : consumers.values()) {
                    String path = pathForConsumer(consumer.getName(), consumer.getId());
                    try {
                        client.delete().forPath(path);
                    } catch (Exception e) {
                    }
                }
            }

            cache.getListenable().removeListener(this);
            try {
                cache.close();
            } catch (IOException e) {
            }
        }

    }

}
