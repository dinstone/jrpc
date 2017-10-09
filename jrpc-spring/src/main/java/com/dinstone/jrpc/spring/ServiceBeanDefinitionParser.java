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

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.dinstone.jrpc.api.Server;
import com.dinstone.jrpc.spring.factory.ServiceFactoryBean;

public class ServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private AtomicInteger count = new AtomicInteger();

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ServiceFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String id = element.getAttribute("id");
        if (!StringUtils.hasText(id)) {
            int index = count.incrementAndGet();
            element.setAttribute("id", "ServiceBean[" + index + "]");
        }

        if (StringUtils.hasText(element.getAttribute("interface"))) {
            builder.addPropertyValue("service", element.getAttribute("interface"));
        }

        if (StringUtils.hasText(element.getAttribute("group"))) {
            builder.addPropertyValue("group", element.getAttribute("group"));
        }

        if (StringUtils.hasText(element.getAttribute("timeout"))) {
            builder.addPropertyValue("timeout", element.getAttribute("timeout"));
        }

        builder.addPropertyReference("instance", element.getAttribute("implement"));
        builder.addPropertyReference("server", getServerBeanId(element.getAttribute("server")));
    }

    private String getServerBeanId(String serverId) {
        if (serverId == null || serverId.length() == 0) {
            serverId = Server.class.getSimpleName() + "-" + 1;
        }
        return serverId;
    }
}
