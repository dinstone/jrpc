
package com.dinstone.jrpc.processor;

import java.net.InetSocketAddress;

/**
 * service implement binding
 * 
 * @author dinstone
 * @version 1.0.0
 */
public interface ImplementBinding {

    /**
     * binding service implement
     * 
     * @param serviceInterface
     * @param group
     * @param wrapper
     */
    public <T> void bind(Class<T> serviceInterface, String group, Service<T> wrapper);

    public <T> InetSocketAddress getServiceAddress();

    public Service<?> findService(String service, String group, String method);

    public void destroy();
}
