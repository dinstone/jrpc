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
package com.dinstone.jrpc.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The Call is the abstract of invoking method, RPC protocol body part.
 *
 * @author guojinfei
 * @version 1.0.0.2014-6-23
 */
public class Call implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private String group;

    private String service;

    private String method;

    private int timeout;

    private Object[] params;

    private Class<?>[] paramTypes;

    public Call() {
        super();
    }

    public Call(String service, String group, int timeout, String method, Object[] params, Class<?>[] paramTypes) {
        super();
        this.group = group;
        this.service = service;
        this.method = method;
        this.timeout = timeout;
        this.params = params;
        this.paramTypes = paramTypes;
    }

    /**
     * the method to get
     *
     * @return the method
     * @see Call#method
     */
    public String getMethod() {
        return method;
    }

    /**
     * the method to set
     *
     * @param method
     * @see Call#method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * the params to get
     *
     * @return the params
     * @see Call#params
     */
    public Object[] getParams() {
        return params;
    }

    /**
     * the params to set
     *
     * @param params
     * @see Call#params
     */
    public void setParams(Object[] params) {
        this.params = params;
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    @Override
    public String toString() {
        return "Call [service=" + service + ", group=" + group + ", timeout=" + timeout + ", method=" + method
                + ", params=" + Arrays.toString(params) + "]";
    }

}
