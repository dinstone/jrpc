
package com.dinstone.jrpc.server;

import java.lang.reflect.InvocationTargetException;

import com.dinstone.jrpc.processor.ServiceProcessor;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;

public class DefaultAcceptance implements Acceptance {

    private ServiceProcessor serviceProcessor;

    public DefaultAcceptance(ServiceProcessor serviceProcessor) {
        super();
        this.serviceProcessor = serviceProcessor;
    }

    @Override
    public Response handle(Request request) {
        Result result = null;
        try {
            Object resObj = serviceProcessor.process(request.getCall());
            result = new Result(200, resObj);
        } catch (IllegalArgumentException e) {
            result = new Result(600, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            result = new Result(601, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            result = new Result(500, t.getMessage(), t);
        } catch (Exception e) {
            result = new Result(509, "unkown exception", e);
        }

        return new Response(request.getMessageId(), request.getSerializeType(), result);
    }
}
