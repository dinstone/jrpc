/*
 * Copyright (C) 2013~2017 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.invoker;

import java.net.InetSocketAddress;

public class Invocation {

    private String service;

    private String group;

    private String method;

    private int timeout;

    private Object[] params;

    private Class<?>[] paramTypes;

    private InetSocketAddress serviceAddress;

    public Invocation(String service, String group, String method, int timeout, Object[] params,
            Class<?>[] paramTypes) {
        super();
        this.service = service;
        this.group = group;
        this.method = method;
        this.timeout = timeout;
        this.params = params;
        this.paramTypes = paramTypes;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public InetSocketAddress getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(InetSocketAddress serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
