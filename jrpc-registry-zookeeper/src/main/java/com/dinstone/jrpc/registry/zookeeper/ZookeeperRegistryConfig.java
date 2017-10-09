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

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.registry.RegistryConfig;

public class ZookeeperRegistryConfig extends RegistryConfig {

    private static final String DEFAULT_BASE_PATH = "/jrpc/dsrd";

    private static final String DSRD_BASE_PATH = "dsrd.base.path";

    private static final String RETRY_MAX_COUNT = "retry.max.count";

    private static final String BASE_SLEEP_TIME_MS = "base.sleep.time.ms";

    private static final String ZOOKEEPER_NODE_LIST = "zookeeper.node.list";

    public ZookeeperRegistryConfig() {
    }

    public ZookeeperRegistryConfig(Configuration config) {
        super(config);
    }

    public String getZookeeperNodes() {
        return get(ZOOKEEPER_NODE_LIST);
    }

    public ZookeeperRegistryConfig setZookeeperNodes(String zkNodeList) {
        set(ZOOKEEPER_NODE_LIST, zkNodeList);
        return this;
    }

    public int getBaseSleepTime() {
        return getInt(BASE_SLEEP_TIME_MS, 3000);
    }

    public ZookeeperRegistryConfig setBaseSleepTime(int baseTime) {
        setInt(BASE_SLEEP_TIME_MS, baseTime);
        return this;
    }

    public int getMaxRetries() {
        return getInt(RETRY_MAX_COUNT, 3);
    }

    public ZookeeperRegistryConfig setMaxRetries(int maxRetry) {
        setInt(RETRY_MAX_COUNT, maxRetry);
        return this;
    }

    public String getBasePath() {
        return get(DSRD_BASE_PATH, DEFAULT_BASE_PATH);
    }

    public ZookeeperRegistryConfig setBasePath(String basePath) {
        set(DSRD_BASE_PATH, basePath);
        return this;
    }

}
