
package com.dinstone.jrpc.srd;

import com.dinstone.jrpc.Configuration;

public class RegistryDiscoveryConfig extends Configuration {

    private static final String DEFAULT_BASE_PATH = "/jrpc/dsrd";

    private static final String DSRD_BASE_PATH = "dsrd.base.path";

    private static final String RETRY_MAX_COUNT = "retry.max.count";

    private static final String BASE_SLEEP_TIME_MS = "base.sleep.time.ms";

    private static final String ZOOKEEPER_NODE_LIST = "zookeeper.node.list";

    public String getZookeeperNodes() {
        return get(ZOOKEEPER_NODE_LIST);
    }

    public RegistryDiscoveryConfig setZookeeperNodes(String zkNodeList) {
        set(ZOOKEEPER_NODE_LIST, zkNodeList);
        return this;
    }

    public int getBaseSleepTime() {
        return getInt(BASE_SLEEP_TIME_MS, 3000);
    }

    public RegistryDiscoveryConfig setBaseSleepTime(int baseTime) {
        setInt(BASE_SLEEP_TIME_MS, baseTime);
        return this;
    }

    public int getMaxRetries() {
        return getInt(RETRY_MAX_COUNT, 3);
    }

    public RegistryDiscoveryConfig setMaxRetries(int maxRetry) {
        setInt(RETRY_MAX_COUNT, maxRetry);
        return this;
    }

    public String getBasePath() {
        return get(DSRD_BASE_PATH, DEFAULT_BASE_PATH);
    }

    public RegistryDiscoveryConfig setBasePath(String basePath) {
        set(DSRD_BASE_PATH, basePath);
        return this;
    }

}
