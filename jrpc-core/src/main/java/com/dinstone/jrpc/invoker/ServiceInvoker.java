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

/**
 * client-side service invoker.
 *
 * @author dinstone
 * @version 1.0.0
 */
public class ServiceInvoker {

    private InvocationHandler invocationHandler;

    public ServiceInvoker(InvocationHandler invocationHandler) {
        if (invocationHandler == null) {
            throw new IllegalArgumentException("invocationHandler is null");
        }
        this.invocationHandler = invocationHandler;
    }

    public Object invoke(String service, String group, String method, int timeout, Object[] params,
            Class<?>[] paramTypes) throws Throwable {
        return invocationHandler.handle(new Invocation(service, group, method, timeout, params, paramTypes));
    }

}
