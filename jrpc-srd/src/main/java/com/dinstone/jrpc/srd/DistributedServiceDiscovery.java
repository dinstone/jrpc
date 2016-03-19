
package com.dinstone.jrpc.srd;

import java.util.List;

/**
 * Service Discovery
 * 
 * @author dinstone
 * @version 1.0.0
 */
public interface DistributedServiceDiscovery {

    public abstract void destroy();

    public abstract void cancel(String serviceName, String group);

    public abstract void listen(String serviceName, String group) throws Exception;

    public abstract List<ServiceDescription> discovery(String serviceName, String group) throws Exception;

}
