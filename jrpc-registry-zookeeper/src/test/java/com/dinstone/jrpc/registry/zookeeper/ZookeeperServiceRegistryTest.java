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

import com.dinstone.jrpc.registry.ServiceDescription;

public class ZookeeperServiceRegistryTest {

    public static void main(String[] args) {
        ServiceDescription description = new ServiceDescription();
        String serviceName = "TestService";
        description.setName(serviceName);
        description.setId("service-provider-1");
        description.setHost("localhost");
        description.setPort(80);

        ZookeeperRegistryConfig config = new ZookeeperRegistryConfig().setZookeeperNodes("localhost:2181");
        ZookeeperServiceRegistry registry = new ZookeeperServiceRegistry(config);

        try {
            registry.register(description);

            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            registry.destroy();
        }

    }
}
