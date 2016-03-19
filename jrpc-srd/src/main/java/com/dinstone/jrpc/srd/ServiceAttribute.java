
package com.dinstone.jrpc.srd;

import java.io.Serializable;
import java.util.Map;

public class ServiceAttribute implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private Map<String, String> attributes;

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "" + attributes;
    }

}
