/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.processor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.service.Service;
import com.dinstone.jrpc.service.ServiceStats;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class DefaultServiceProcessor extends AbstractServiceProcessor implements ServiceStats {

    static final Logger LOG = LoggerFactory.getLogger(DefaultServiceProcessor.class);

    public DefaultServiceProcessor() {
        bind(ServiceStats.class, this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.service.ServiceStats#serviceList()
     */
    public List<String> serviceList() {
        List<String> services = new ArrayList<String>(serviceMap.size());
        for (Service service : serviceMap.values()) {
            services.add(description(service));
        }
        return services;
    }

    private String description(Service service) {
        Method method = service.getMethod();
        StringBuilder desc = new StringBuilder();
        desc.append(getTypeName(method.getReturnType()) + " ");
        desc.append(getTypeName(method.getDeclaringClass()) + ".");
        desc.append(method.getName() + "(");
        Class<?>[] params = method.getParameterTypes();
        for (int j = 0; j < params.length; j++) {
            desc.append(getTypeName(params[j]));
            if (j < (params.length - 1)) {
                desc.append(",");
            }
        }
        desc.append(")");
        return desc.toString();
    }

    private static String getTypeName(Class<?> type) {
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
            }
        }
        return type.getName();
    }

}
