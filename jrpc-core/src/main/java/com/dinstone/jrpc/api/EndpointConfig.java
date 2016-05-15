
package com.dinstone.jrpc.api;

import com.dinstone.jrpc.Configuration;

public class EndpointConfig extends Configuration {

    private static final String ENDPOINT_ID = "endpoint.id";

    private static final String ENDPOINT_NAME = "endpoint.name";

    private static final int DEFAULT_TIMEOUT = 3000;

    private static final String DEFAULT_TIMEOUT_KEY = "default.timeout";

    public EndpointConfig() {
        super();
    }

    public EndpointConfig(Configuration config) {
        super(config);
    }

    public EndpointConfig(String configLocation) {
        super(configLocation);
    }

    public void setDefaultTimeout(int defaultTimeout) {
        setInt(DEFAULT_TIMEOUT_KEY, defaultTimeout);
    }

    public int getDefaultTimeout() {
        return getInt(DEFAULT_TIMEOUT_KEY, DEFAULT_TIMEOUT);
    }

    public void setEndpointId(String id) {
        set(ENDPOINT_ID, id);
    }

    public void setEndpointName(String name) {
        set(ENDPOINT_NAME, name);
    }

    public String getEndpointId() {
        return get(ENDPOINT_ID);
    }

    public String getEndpointName() {
        return get(ENDPOINT_NAME);
    }

}
