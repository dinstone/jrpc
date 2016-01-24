/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.mina.client;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.client.Connection;
import com.dinstone.jrpc.client.ConnectionFactory;

public class MinaConnectionFactory implements ConnectionFactory {

    private Configuration config;

    private MinaConnector connector;

    private MinaConnection[] connections;

    private int count;

    protected MinaConnectionFactory(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.config = config;

        this.connector = new MinaConnector(config);

        this.connections = new MinaConnection[config.getParallelCount()];
    }

    public synchronized Connection create() {
        int index = count++ % connections.length;
        MinaConnection connection = connections[index];
        if (connection == null || !connection.isAlive()) {
            if (connection != null) {
                connections[index] = null;
                connection.close();
            }

            connection = new MinaConnection(connector.createSession(), config);
            connections[index] = connection;
        }

        return connection;
    }

    public synchronized void destroy() {
        for (MinaConnection connection : connections) {
            if (connection != null) {
                connection.close();
            }
        }

        if (connector != null) {
            connector.dispose();
        }
    }

}
