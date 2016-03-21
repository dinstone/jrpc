
package com.dinstone.jrpc.client;

import java.util.HashMap;
import java.util.Map;

import com.dinstone.jrpc.Configuration;

public abstract class AbstractConnectionManager implements ConnectionManager {

    private Map<String, ConnectionFactory> factoryMap = new HashMap<String, ConnectionFactory>();

    @Override
    public Connection getConnection(Configuration config) {
        synchronized (factoryMap) {
            String key = config.getServiceAddress();
            ConnectionFactory factory = factoryMap.get(key);
            if (factory == null) {
                factory = createConnectionFactory(config);
                factoryMap.put(key, factory);
            }
            return factory.create();
        }
    }

    protected abstract ConnectionFactory createConnectionFactory(Configuration config);

    @Override
    public void destroy() {
        synchronized (factoryMap) {
            for (ConnectionFactory factory : factoryMap.values()) {
                factory.destroy();
            }

            factoryMap.clear();
        }
    }

}
