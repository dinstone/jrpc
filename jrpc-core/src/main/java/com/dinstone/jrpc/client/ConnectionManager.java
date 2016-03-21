
package com.dinstone.jrpc.client;

import com.dinstone.jrpc.Configuration;

public interface ConnectionManager {

    public abstract Connection getConnection(Configuration config);

    public abstract void destroy();

}
