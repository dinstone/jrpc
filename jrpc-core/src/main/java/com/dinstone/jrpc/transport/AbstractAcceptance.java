/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.jrpc.transport;

import java.lang.reflect.InvocationTargetException;

import com.dinstone.jrpc.processor.ImplementBinding;
import com.dinstone.jrpc.processor.Service;
import com.dinstone.jrpc.processor.ServiceProcessor;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;

public abstract class AbstractAcceptance implements Acceptance {

    protected ServiceProcessor serviceProcessor;

    protected ImplementBinding implementBinding;

    public AbstractAcceptance(ImplementBinding implementBinding, ServiceProcessor serviceProcessor) {
        this.implementBinding = implementBinding;
        this.serviceProcessor = serviceProcessor;
    }

    @Override
    public Response handle(Request request) {
        Result result = null;
        try {
            Call call = request.getCall();
            Service<?> service = implementBinding.findService(call.getService(), call.getGroup(), call.getMethod());
            if (service != null) {
                Object resObj = serviceProcessor.process(service, call);
                result = new Result(200, resObj);
            } else {
                result = new Result(404, "unkown service");
            }
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
