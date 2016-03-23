
package com.dinstone.jrpc.srd;

import com.dinstone.jrpc.Configuration;

public class RegistryDiscoveryConfig extends Configuration {

    private static final String JRPC_PATH = "/jrpc/dsrd";

    public String getZookeeperNodes() {
        return get("zookeeper.node.list");
    }

    public int getBaseSleepTime() {
        return 3000;
    }

    public int getMaxRetries() {
        return 3;
    }

    public String getBasePath() {
        return JRPC_PATH;
    }

}
