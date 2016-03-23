
package com.dinstone.jrpc.client;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.serialize.SerializeType;

public class TransportConfig extends Configuration {

    /** service host name */
    private static final String SERVICE_HOST = "rpc.service.host";

    /** service port */
    private static final String SERVICE_PORT = "rpc.service.port";

    /** max size */
    private static final String MAX_SIZE = "rpc.max.size";

    /** serialize type */
    private static final String SERIALIZE_TYPE = "rpc.serialize.type";

    /** call timeout */
    private static final String CALL_TIMEOUT = "rpc.call.timeout";

    /** parallel count */
    private static final String PARALLEL_COUNT = "rpc.parallel.count";

    public String getServiceHost() {
        return get(SERVICE_HOST);
    }

    public void setServiceHost(String host) {
        set(SERVICE_HOST, host);
    }

    public int getServicePort() {
        return getInt(SERVICE_PORT, 9958);
    }

    public void setServicePort(int port) {
        setInt(SERVICE_PORT, port);
    }

    public int getMaxSize() {
        return getInt(MAX_SIZE, Integer.MAX_VALUE);
    }

    public void setMaxSize(int maxSize) {
        setInt(MAX_SIZE, maxSize);
    }

    public void setSerializeType(SerializeType type) {
        setInt(SERIALIZE_TYPE, type.getValue());
    }

    public SerializeType getSerializeType() {
        return SerializeType.valueOf(getInt(SERIALIZE_TYPE, SerializeType.JACKSON.getValue()));
    }

    public int getCallTimeout() {
        return getInt(CALL_TIMEOUT, 3000);
    }

    public void setCallTimeout(int timeout) {
        setInt(CALL_TIMEOUT, timeout);
    }

    public int getParallelCount() {
        return getInt(PARALLEL_COUNT, Runtime.getRuntime().availableProcessors());
    }

    public void setParallelCount(int count) {
        setInt(PARALLEL_COUNT, count);
    }

    public String getServiceAddress() {
        return getServiceHost() + ":" + getServicePort();
    }
}
