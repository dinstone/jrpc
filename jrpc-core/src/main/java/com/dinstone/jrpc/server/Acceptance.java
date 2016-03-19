
package com.dinstone.jrpc.server;

import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;

public interface Acceptance {

    public abstract Response handle(Request request);
}
