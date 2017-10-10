/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dinstone.jrpc;

import java.util.Properties;

public class SchemaConfig<T extends SchemaConfig<T>> extends Configuration {

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

    @SuppressWarnings("unchecked")
    public T setSchema(String schema) {
        if (schema != null && !schema.isEmpty()) {
            this.set(CONFIG_SCHEMA_KEY, schema);
        }

        return (T) this;
    }

    public String getSchema() {
        return this.get(CONFIG_SCHEMA_KEY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setProperties(Properties other) {
        super.setProperties(other);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addProperty(String name, String value) {
        super.set(name, value);

        return (T) this;
    }
}