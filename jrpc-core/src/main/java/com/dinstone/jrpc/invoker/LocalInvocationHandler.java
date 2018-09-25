/*
 * Copyright (C) 2013~2017 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.invoker;

import java.lang.reflect.Method;

import com.dinstone.jrpc.RpcException;
import com.dinstone.jrpc.binding.ImplementBinding;
import com.dinstone.jrpc.protocol.Call;
import com.dinstone.jrpc.proxy.ServiceProxy;

public class LocalInvocationHandler implements InvocationHandler {

    private ImplementBinding implementBinding;

    public LocalInvocationHandler(ImplementBinding implementBinding) {
        this.implementBinding = implementBinding;
    }

    @Override
    public Object handle(Invocation invocation) throws Throwable {
        ServiceProxy<?> wrapper = implementBinding.lookup(invocation.getService(), invocation.getGroup());
        if (wrapper == null) {
            throw new RpcException(404,
                "unkown service: " + invocation.getService() + "[" + invocation.getGroup() + "]");
        }

        Class<?>[] paramTypes = getParamTypes(invocation.getParams(), invocation.getParamTypes());
        Method method = wrapper.getService().getDeclaredMethod(invocation.getMethod(), paramTypes);
        return method.invoke(wrapper.getTarget(), invocation.getParams());
    }

    private Class<?>[] getParamTypes(Object[] params, Class<?>[] paramTypes) {
        if (paramTypes == null && params != null) {
            paramTypes = parseParamTypes(params);
        }
        return paramTypes;
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
