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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.spring.factory.ReferenceFactoryBean;

public class ReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ReferenceFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.addPropertyValue("service", element.getAttribute("interface"));
        builder.addPropertyValue("group", element.getAttribute("group"));
        String timeout = element.getAttribute("timeout");
        if (StringUtils.hasText(timeout)) {
            builder.addPropertyValue("timeout", timeout);
        }
        builder.addPropertyReference("client", getClientBeanId(element.getAttribute("client")));
    }

    private String getClientBeanId(String clientId) {
        if (clientId == null || clientId.length() == 0) {
            clientId = Client.class.getSimpleName() + "-" + 1;
        }
        return clientId;
    }
}
