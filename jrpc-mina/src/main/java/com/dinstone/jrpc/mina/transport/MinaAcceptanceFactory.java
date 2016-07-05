
package com.dinstone.jrpc.mina.transport;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.transport.Acceptance;
import com.dinstone.jrpc.transport.AcceptanceFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class MinaAcceptanceFactory implements AcceptanceFactory {

    protected TransportConfig transportConfig = new TransportConfig();

    @Override
    public TransportConfig getTransportConfig() {
        return transportConfig;
    }

    @Override
    public Acceptance create(ImplementBinding implementBinding) {
        return new MinaAcceptance(transportConfig, implementBinding);
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getSchema() {
        return "mina";
    }

}
