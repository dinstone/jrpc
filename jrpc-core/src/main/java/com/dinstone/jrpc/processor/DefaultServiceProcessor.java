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
package com.dinstone.jrpc.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.jrpc.protocol.Call;

/**
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public class DefaultServiceProcessor implements ServiceProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DefaultServiceProcessor.class);

    @Override
    public Object process(Service<?> service, Call call) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method method = service.getMethodMap().get(call.getMethod());
        return method.invoke(service.getInstance(), call.getParams());
    }

    @Override
    public void destroy() {

    }

}
