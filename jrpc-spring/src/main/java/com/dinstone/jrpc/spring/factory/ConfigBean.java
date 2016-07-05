
package com.dinstone.jrpc.spring.factory;

import java.util.Properties;

public class ConfigBean {

    private String schema;

    private String address;

    private Properties properties;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
