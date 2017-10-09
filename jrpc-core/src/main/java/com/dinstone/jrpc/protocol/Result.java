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

/**
 * Method call results, RPC protocol payload part.
 *
 * @author guojinfei
 * @version 1.0.0.2014-6-23
 */
public class Result implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    private Object data;

    public Result() {
        super();
    }

    public Result(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public Result(int code, Object data) {
        super();
        this.code = code;
        this.data = data;
    }

    /**
     * the code to get
     *
     * @return the code
     * @see Result#code
     */
    public int getCode() {
        return code;
    }

    /**
     * the code to set
     *
     * @param code
     * @see Result#code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * the message to get
     *
     * @return the message
     * @see Result#message
     */
    public String getMessage() {
        return message;
    }

    /**
     * the message to set
     *
     * @param message
     * @see Result#message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * the data to get
     *
     * @return the data
     * @see Result#data
     */
    public Object getData() {
        return data;
    }

    /**
     * the data to set
     *
     * @param data
     * @see Result#data
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{code=" + code + ", message=" + message + ", data=" + data + "}";
    }

}
