/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.transport;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConnectionFactory implements ConnectionFactory {

    protected TransportConfig transportConfig = new TransportConfig();

    private Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    @Override
    public Connection create(InetSocketAddress sa) {
        synchronized (connectionMap) {
            String key = sa.getAddress().getHostAddress() + ":" + sa.getPort();
            Connection connection = connectionMap.get(key);
            if (connection == null || !connection.isAlive()) {
                connection = createConnection(sa);
                Connection oc = connectionMap.put(key, connection);
                if (oc != null) {
                    oc.destroy();
                }
            }
            return connection;
        }
    }

    protected abstract Connection createConnection(InetSocketAddress sa);

    @Override
    public void destroy() {
        synchronized (connectionMap) {
            for (Connection connection : connectionMap.values()) {
                connection.destroy();
            }
            connectionMap.clear();
        }
    }

    @Override
    public TransportConfig getTransportConfig() {
        return transportConfig;
    }
}
