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

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaFactoryLoader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaFactoryLoader.class);

    private static ConcurrentMap<Class<?>, SchemaFactoryLoader<?>> loaderMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> SchemaFactoryLoader<T> getInstance(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type.getName() + " is not interface");
        }
        if (!SchemaFactory.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.getName() + " is not " + SchemaFactory.class.getName());
        }

        SchemaFactoryLoader<T> loader = (SchemaFactoryLoader<T>) loaderMap.get(type);
        if (loader == null) {
            loaderMap.putIfAbsent(type, new SchemaFactoryLoader<>(type));
            loader = (SchemaFactoryLoader<T>) loaderMap.get(type);
        }

        return loader;
    }

    // ====================================================
    // object domain
    // ====================================================
    private final ConcurrentMap<String, T> schemaFactoryMap = new ConcurrentHashMap<>();

    private SchemaFactoryLoader(Class<T> type) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(type);
        for (T factory : serviceLoader) {
            String schema = ((SchemaFactory) factory).getSchema();
            LOG.info("load {} provider for schema : {}", type.getSimpleName(), schema);
            schemaFactoryMap.put(schema, factory);
        }
    }

    public T getSchemaFactory(String schema) {
        return schemaFactoryMap.get(schema);
    }

}
