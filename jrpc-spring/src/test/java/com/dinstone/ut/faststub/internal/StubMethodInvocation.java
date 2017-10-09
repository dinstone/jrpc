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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dinstone.ut.faststub.MethodInvocation;

/**
 * @author dinstone
 */
class StubMethodInvocation implements MethodInvocation {

    private static final Logger LOG = LoggerFactory.getLogger(StubMethodInvocation.class);

    private Map<String, ApplicationContext> stubContextCachedMap;

    private String currentCase;

    private Class<?> stubClass;

    /**
     * @param stubClass
     */
    public StubMethodInvocation(Class<?> stubClass) {
        this.stubClass = stubClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Method method, Object[] args) throws Throwable {
        ApplicationContext stubContext = getStubContext(method);
        Object retObj = stubContext.getBean(method.getName());
        // handle exception
        handleException(retObj);

        // handle array object
        Class<?> retType = method.getReturnType();
        if (retType.isArray() && retObj instanceof List<?>) {
            List<?> list = (List<?>) retObj;
            int len = list.size();
            Object arrObj = Array.newInstance(retType.getComponentType(), len);
            for (int i = 0; i < len; i++) {
                Array.set(arrObj, i, list.get(i));
            }
            return arrObj;
        }

        return retObj;
    }

    private void handleException(Object retObj) throws Throwable {
        if (retObj instanceof Throwable) {
            throw (Throwable) retObj;
        }
    }

    private ApplicationContext getStubContext(Method method) {
        StackTraceElement callTrace = findCaller();
        String testCase = callTrace.getMethodName();
        if (!testCase.equals(currentCase)) {
            stubContextCachedMap = new HashMap<>();
            currentCase = testCase;
        }

        // case name + stub class name
        String cacheKey = currentCase + ":" + stubClass.getName();
        ApplicationContext stubContext = stubContextCachedMap.get(cacheKey);
        if (stubContext == null) {
            String resourcePath = getResourcePath(method, callTrace);
            LOG.info("Loading Stub bean definitions from class path resource [{}]", resourcePath);
            stubContext = new ClassPathXmlApplicationContext(resourcePath);
            stubContextCachedMap.put(cacheKey, stubContext);
        }

        return stubContext;
    }

    private String getResourcePath(Method method, StackTraceElement callTrace) {
        return callTrace.getClassName().replace('.', '/') + "/" + callTrace.getMethodName() + "/"
                + stubClass.getSimpleName() + ".xml";
    }

    private StackTraceElement findCaller() {
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        for (StackTraceElement trace : stackTraces) {
            if (trace.getClassName().endsWith("Test") && trace.getMethodName().startsWith("test")) {
                return trace;
            }
        }

        throw new RuntimeException(
            "Test class name must be 'Test' as a suffix, the test method must start with 'test' prefix.");
    }

}
