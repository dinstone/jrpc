
package com.dinstone.jrpc.transport;

import com.dinstone.jrpc.Configuration;
import com.dinstone.jrpc.serialize.SerializeType;

public class TransportConfig extends Configuration {

    /** max size */
    private static final String MAX_SIZE = "rpc.max.size";

    /** serialize type */
    private static final String SERIALIZE_TYPE = "rpc.serialize.type";

    /** Connect Timeout */
    private static final String CONNECT_TIMEOUT = "rpc.connect.timeout";

    /** parallel count */
    private static final String PARALLEL_COUNT = "rpc.parallel.count";

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

    public int getConnectTimeout() {
        return getInt(CONNECT_TIMEOUT, 30000);
    }

    public void setConnectTimeout(int timeout) {
        setInt(CONNECT_TIMEOUT, timeout);
    }

    public int getParallelCount() {
        return getInt(PARALLEL_COUNT, Runtime.getRuntime().availableProcessors());
    }

    public void setParallelCount(int count) {
        setInt(PARALLEL_COUNT, count);
    }

}
