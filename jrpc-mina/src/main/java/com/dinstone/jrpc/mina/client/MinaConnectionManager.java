
package com.dinstone.jrpc.mina.client;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.client.AbstractConnectionManager;
import com.dinstone.jrpc.client.ConnectionFactory;

public class MinaConnectionManager extends AbstractConnectionManager {

    @Override
    protected ConnectionFactory createConnectionFactory(Configuration config) {
        return new MinaConnectionFactory(config);
    }

}
