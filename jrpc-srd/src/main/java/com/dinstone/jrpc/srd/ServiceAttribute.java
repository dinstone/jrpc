
package com.dinstone.jrpc.srd;

import java.io.Serializable;
import java.util.Map;

public class ServiceAttribute implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private Map<String, Object> attributes;

    public ServiceAttribute() {
        super();
    }

    public ServiceAttribute(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
