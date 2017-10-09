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
package com.dinstone.jrpc.transport.netty4;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dinstone.jrpc.transport.ResultFuture;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class SessionUtil {

    private static final AttributeKey<Object> RESULT_FUTURE_KEY = AttributeKey
        .valueOf(ConcurrentHashMap.class.getName());

    @SuppressWarnings("unchecked")
    public static Map<Integer, ResultFuture> getResultFutureMap(Channel session) {
        return (Map<Integer, ResultFuture>) session.attr(RESULT_FUTURE_KEY).get();
    }

    public static void setResultFutureMap(Channel session) {
        session.attr(RESULT_FUTURE_KEY).set(new ConcurrentHashMap<Integer, ResultFuture>());
    }
}
