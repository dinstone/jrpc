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

import java.lang.reflect.Method;

/**
 * classes utility class.
 *
 * @author dinstone
 */
public class ClassUtil {

    public static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassUtil.class.getClassLoader();
        }
        return cl;
    }

    public static Class<?> loadClass(String clazz) throws ClassNotFoundException {
        return getClassLoader().loadClass(clazz);
    }

    public static boolean declaresException(Method method, Class<?> exceptionType) {
        Class<?>[] declaredExceptions = method.getExceptionTypes();
        for (int i = 0; i < declaredExceptions.length; i++) {
            Class<?> declaredException = declaredExceptions[i];
            if (declaredException.isAssignableFrom(exceptionType)) {
                return true;
            }
        }
        return false;
    }

}
