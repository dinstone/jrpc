
package com.dinstone.jrpc.invoker;

import java.net.InetSocketAddress;

/**
 * service reference binding
 * 
 * @author dinstone
 * @version 1.0.0
 */
public interface ReferenceBinding {

    /**
     * service reference bind
     * 
     * @param serviceInterface
     * @param group
     * @param timeout
     * @param serviceReference
     */
    public <T> void bind(Class<T> serviceInterface, String group, int timeout, T serviceReference);

    public <T> InetSocketAddress getServiceAddress(Class<T> serviceInterface, String group);

    public void destroy();

}
