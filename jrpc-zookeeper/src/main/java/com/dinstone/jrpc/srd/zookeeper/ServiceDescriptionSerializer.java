
package com.dinstone.jrpc.srd.zookeeper;

import com.dinstone.jrpc.srd.ServiceDescription;

public interface ServiceDescriptionSerializer {

    byte[] serialize(ServiceDescription service) throws Exception;

    ServiceDescription deserialize(byte[] bytes) throws Exception;

}
