
package com.dinstone.jrpc.mina.server;

import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.processor.ServiceProcessor;
import com.dinstone.jrpc.server.Acceptance;
import com.dinstone.jrpc.server.AcceptanceFactory;
import com.dinstone.jrpc.transport.TransportConfig;

public class MinaAcceptanceFactory implements AcceptanceFactory {

    private TransportConfig transportConfig;

    public MinaAcceptanceFactory(TransportConfig transportConfig) {
        this.transportConfig = transportConfig;
    }

    @Override
    public Acceptance create(ImplementBinding implementBinding, ServiceProcessor serviceProcessor) {
        MinaAcceptance acceptance = new MinaAcceptance(transportConfig, implementBinding, serviceProcessor);
        acceptance.bind();
        return acceptance;
    }

}
