
package com.dinstone.jrpc.registry;

public interface RegistryFactory {

    public abstract String getSchema();

    public abstract RegistryConfig getRegistryConfig();

    public abstract ServiceRegistry createServiceRegistry();

    public abstract ServiceDiscovery createServiceDiscovery();

    public abstract void destroy();

}
