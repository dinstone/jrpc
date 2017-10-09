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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.dinstone.ut.faststub.DateFactoryBean;
import com.dinstone.ut.faststub.NullFactoryBean;

/**
 * @author dinstone
 */
public class StubNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("null", new NullBeanDefinitionParser());
        registerBeanDefinitionParser("date", new DateBeanDefinitionParser());
    }

    private static class DateBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return DateFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            String pattern = element.getAttribute("pattern");
            if (StringUtils.hasText(pattern)) {
                builder.addPropertyValue("pattern", pattern);
            }

            String v = element.getAttribute("value");
            if (!StringUtils.hasText(v)) {
                parserContext.getReaderContext().error("Attribute 'value' must not be empty", element);
                return;
            }
            builder.addPropertyValue("dateText", v);
        }
    }

    private static class NullBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return NullFactoryBean.class;
        }

    }

}
