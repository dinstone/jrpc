
package com.dinstone.jrpc.transport;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConnectionFactory implements ConnectionFactory {

    private Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    @Override
    public Connection create(String host, int port) {
        return create(InetSocketAddress.createUnresolved(host, port));
    }

    @Override
    public Connection create(InetSocketAddress sa) {
        synchronized (connectionMap) {
            String key = sa.getAddress().getHostAddress() + ":" + sa.getPort();
            Connection connection = connectionMap.get(key);
            if (connection == null) {
                connection = createConnection(sa);
                connectionMap.put(key, connection);
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
}
