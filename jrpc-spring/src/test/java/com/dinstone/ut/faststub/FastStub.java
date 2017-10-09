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
package com.dinstone.ut.faststub;

import com.dinstone.ut.faststub.internal.ProxyFactory;

/**
 * The stub factory to generate Stub instance.The stub type either Interface type or Class type.
 *
 * @author dinstone
 */
public class FastStub {

    /**
     * Creating stub instance.
     *
     * @param <T>
     *        stub type, Interface or non final Class
     * @param toStub
     *        required stub class
     * @return stub instance
     */
    public static <T> T createStub(final Class<T> toStub) {
        return createStub(toStub, null);
    }

    /**
     * Creating stub instance, specify a custom method interceptor.
     *
     * @param <T>
     *        stub type, Interface or non final Class
     * @param toStub
     *        required stub class
     * @param interceptor
     *        method interceptor to intercept the target method invocation
     * @return stub instance
     */
    public static <T> T createStub(final Class<T> toStub, MethodInterceptor interceptor) {
        ProxyFactory factory = new ProxyFactory();
        return factory.createProxyInstance(toStub, interceptor);
    }

}
