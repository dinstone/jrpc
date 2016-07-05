
package com.dinstone.jrpc.transport;

import com.dinstone.jrpc.binding.ImplementBinding;

public interface AcceptanceFactory {

    public abstract TransportConfig getTransportConfig();

    public abstract Acceptance create(ImplementBinding implementBinding);

    public abstract void destroy();

    public abstract String getSchema();

}
