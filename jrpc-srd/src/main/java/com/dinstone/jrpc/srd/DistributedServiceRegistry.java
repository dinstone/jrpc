
package com.dinstone.jrpc.srd;

/**
 * Service Registry
 * 
 * @author dinstone
 * @version 1.0.0
 */
public interface DistributedServiceRegistry {

    public abstract void regist(ServiceDescription description) throws Exception;

    public abstract void destroy();

}
