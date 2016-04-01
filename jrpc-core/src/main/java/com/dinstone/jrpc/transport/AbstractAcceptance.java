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
import java.lang.reflect.Method;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.invoker.ServiceInvoker;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.proxy.ServiceProxy;

public abstract class AbstractAcceptance implements Acceptance {

    protected ServiceInvoker serviceInvoker;

    protected ImplementBinding implementBinding;

    public AbstractAcceptance(ImplementBinding implementBinding, ServiceInvoker serviceInvoker) {
        this.implementBinding = implementBinding;
        this.serviceInvoker = serviceInvoker;
    }

    @Override
    public Response handle(Request request) {
        Result result = null;
        try {
            Call call = request.getCall();
            ServiceProxy<?> wrapper = implementBinding.find(call.getService(), call.getGroup());
            if (wrapper != null) {
                Class<?>[] paramTypes = getParamTypes(call);
                Method method = wrapper.getService().getDeclaredMethod(call.getMethod(), paramTypes);
                Object resObj = serviceInvoker.invoke(wrapper.getService(), call.getGroup(), call.getTimeout(),
                    wrapper.getInstance(), method, call.getParams());
                result = new Result(200, resObj);
            } else {
                result = new Result(404, "unkown service");
            }
        } catch (NoSuchMethodException e) {
            result = new Result(405, "unkown method", e);
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

    protected Class<?>[] getParamTypes(Call call) {
        Class<?>[] paramTypes = call.getParamTypes();
        Object[] params = call.getParams();
        if (paramTypes == null && params != null) {
            paramTypes = parseParamTypes(params);
        }
        return paramTypes;
    }

    private Class<?>[] parseParamTypes(Object[] args) {
        Class<?>[] cs = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            cs[i] = arg.getClass();
        }

        return cs;
    }

}
