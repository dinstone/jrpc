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

import java.util.List;

import com.dinstone.jrpc.registry.ServiceDescription;

public class ZookeeperServiceDiscoveryTest {

    public static void main(String[] args) {

        ZookeeperRegistryConfig config = new ZookeeperRegistryConfig().setZookeeperNodes("localhost:2181");

        ZookeeperServiceDiscovery discovery = new ZookeeperServiceDiscovery(config);
        ServiceDescription description = new ServiceDescription();
        String serviceName = "TestService";
        description.setName(serviceName);
        description.setId("service-consumer-1");
        description.setHost("localhost");
        description.setPort(0);

        try {
            discovery.listen(description);

            discovery.listen(description);
            //
            // description.setId("service-consumer-2");
            // discovery.listen(description);

            // description.setServiceName("TestService2");
            // description.setId("service-consumer-1");
            // discovery.listen(description);

            while (true) {
                List<ServiceDescription> plist = discovery.discovery(serviceName);
                if (plist != null && plist.size() > 0) {
                    for (ServiceDescription psd : plist) {
                        System.out.println(psd);
                    }
                } else {
                    System.out.println("empty");
                }

                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            discovery.destroy();
        }

    }

}
