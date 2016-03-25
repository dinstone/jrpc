
package com.dinstone.jrpc.processor;

import java.net.InetSocketAddress;

public class DefaultImplementBinding extends AbstractImplementBinding {

    public DefaultImplementBinding(String host, int port) {
        this.serviceAddress = new InetSocketAddress(host, port);
    }

}
