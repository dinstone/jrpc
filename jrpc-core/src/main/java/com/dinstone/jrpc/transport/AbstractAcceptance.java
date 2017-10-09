/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
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
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;

import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.protocol.Request;
import com.dinstone.jrpc.protocol.Response;
import com.dinstone.jrpc.protocol.Result;
import com.dinstone.jrpc.proxy.ServiceProxy;

public abstract class AbstractAcceptance implements Acceptance {

    protected TransportConfig transportConfig;

    protected ImplementBinding implementBinding;

    protected InetSocketAddress serviceAddress;

    public AbstractAcceptance(TransportConfig transportConfig, ImplementBinding implementBinding,
            InetSocketAddress serviceAddress) {
        this.transportConfig = transportConfig;
        if (implementBinding == null) {
            throw new IllegalArgumentException("implementBinding is null");
        }
        this.implementBinding = implementBinding;
        this.serviceAddress = serviceAddress;
    }

    @Override
    public Response handle(Request request) {
        Result result = null;
        Call call = request.getCall();
        try {
            ServiceProxy<?> wrapper = implementBinding.lookup(call.getService(), call.getGroup());
            if (wrapper != null) {
                Class<?>[] paramTypes = getParamTypes(call);
                Method method = wrapper.getService().getDeclaredMethod(call.getMethod(), paramTypes);
                Object resObj = method.invoke(wrapper.getProxy(), call.getParams());
                result = new Result(200, resObj);
            } else {
                result = new Result(404, "unkown service: " + call.getService() + "[" + call.getGroup() + "]");
            }
        } catch (NoSuchMethodException e) {
            result = new Result(405,
                "unkown method: " + call.getService() + "[" + call.getGroup() + "]." + call.getMethod());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            String message = "illegal access: " + call.getService() + "[" + call.getGroup() + "]." + call.getMethod()
                    + "(): " + e.getMessage();
            result = new Result(502, message);
        } catch (InvocationTargetException e) {
            Throwable t = getTargetException(e);
            String message = "service exception: " + call.getService() + "[" + call.getGroup() + "]." + call.getMethod()
                    + "(): " + t.getMessage();
            result = new Result(500, message);
        } catch (Throwable e) {
            String message = "service exception: " + call.getService() + "[" + call.getGroup() + "]." + call.getMethod()
                    + "(): " + e.getMessage();
            result = new Result(509, message);
        }

        return new Response(request.getMessageId(), request.getSerializeType(), result);
    }

    private Throwable getTargetException(InvocationTargetException e) {
        Throwable t = e.getTargetException();
        if (t instanceof UndeclaredThrowableException) {
            UndeclaredThrowableException ut = (UndeclaredThrowableException) t;
            t = ut.getCause();
            if (t instanceof InvocationTargetException) {
                return getTargetException((InvocationTargetException) t);
            }
        }
        return t;
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
