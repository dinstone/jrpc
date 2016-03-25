
package com.dinstone.jrpc.server;

import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.processor.ServiceProcessor;

public interface AcceptanceFactory {

    Acceptance create(ImplementBinding implementBinding, ServiceProcessor serviceProcessor);

}
