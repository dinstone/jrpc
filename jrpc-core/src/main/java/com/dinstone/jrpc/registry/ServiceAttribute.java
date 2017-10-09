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
package com.dinstone.jrpc.registry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceAttribute implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private Map<String, Object> attributes = new HashMap<>();

    public ServiceAttribute() {
        super();
    }

    public ServiceAttribute(Map<String, Object> attributes) {
        setAttributes(attributes);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    public ServiceAttribute addAttribute(String att, Object value) {
        this.attributes.put(att, value);
        return this;
    }

    public ServiceAttribute removeAttribute(String att) {
        this.attributes.remove(att);
        return this;
    }

    @Override
    public String toString() {
        return "[attributes=" + attributes + "]";
    }

}
