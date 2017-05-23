
package com.dinstone.jrpc;

public class SchemaConfig extends Configuration {

    private static final String CONFIG_SCHEMA_KEY = "config.schema";

    public SchemaConfig() {
        super();
    }

    public SchemaConfig(String configLocation) {
        super(configLocation);
    }

    public SchemaConfig(Configuration config) {
        super(config);
    }

    public SchemaConfig setSchema(String schema) {
        if (schema != null && !schema.isEmpty()) {
            this.set(CONFIG_SCHEMA_KEY, schema);
        }

        return this;
    }

    public String getSchema() {
        return this.get(CONFIG_SCHEMA_KEY);
    }
}