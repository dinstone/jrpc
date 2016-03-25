
package com.dinstone.jrpc.transport;

import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;

public interface Acceptance {

    public abstract Response handle(Request request);

    public abstract Acceptance bind();

    public abstract void destroy();
}
