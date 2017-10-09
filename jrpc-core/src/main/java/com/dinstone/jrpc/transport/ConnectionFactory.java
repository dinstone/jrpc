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

import java.net.InetSocketAddress;

import com.dinstone.jrpc.SchemaFactory;

/**
 * connetcion factory.
 *
 * @author guojinfei
 * @version 2.0.0.2015-11-3
 */
public interface ConnectionFactory extends SchemaFactory {

    public abstract Connection create(TransportConfig transportConfig, InetSocketAddress sa);
}