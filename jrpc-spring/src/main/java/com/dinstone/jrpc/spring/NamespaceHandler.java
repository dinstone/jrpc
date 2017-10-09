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
package com.dinstone.jrpc.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.spring.factory.ClientFactoryBean;
import com.dinstone.jrpc.spring.factory.ServerFactoryBean;

public class NamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("server", new EndpointBeanDefinitionParser(ServerFactoryBean.class, Server.class));
        registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());

        registerBeanDefinitionParser("client", new EndpointBeanDefinitionParser(ClientFactoryBean.class, Client.class));
        registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
    }
}
