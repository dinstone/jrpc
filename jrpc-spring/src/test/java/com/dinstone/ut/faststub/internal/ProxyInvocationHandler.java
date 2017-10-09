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
package com.dinstone.ut.faststub.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.ut.faststub.MethodInterceptor;

/**
 * @author dinstone
 */
class ProxyInvocationHandler implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyInvocationHandler.class);

    private MethodInterceptor interceptor;

    private StubMethodInvocation methodInvocation;

    /**
     * @param toStub
     * @param interceptor
     */
    public ProxyInvocationHandler(Class<?> toStub, MethodInterceptor interceptor) {
        methodInvocation = new StubMethodInvocation(toStub);

        if (interceptor == null) {
            interceptor = new DefaultMethodInterceptor();
        }
        this.interceptor = interceptor;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOG.debug("the interceptor[{}] will be inoked", interceptor.getClass());
        return interceptor.invoke(methodInvocation, method, args);
    }

}
