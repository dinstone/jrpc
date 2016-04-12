
package com.dinstone.jrpc.srd.zookeeper;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import com.dinstone.jrpc.srd.ServiceDescription;

public class DefaultServiceDescriptionSerializer implements ServiceDescriptionSerializer {

    private final ObjectMapper mapper;

    private final JavaType type;

    public DefaultServiceDescriptionSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        type = mapper.getTypeFactory().constructType(ServiceDescription.class);
    }

    @Override
    public byte[] serialize(ServiceDescription service) throws Exception {
        return mapper.writeValueAsBytes(service);
    }

    @Override
    public ServiceDescription deserialize(byte[] bytes) throws Exception {
        return mapper.readValue(bytes, type);
    }

}
