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
package com.dinstone.other;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FileFilterDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /**
     * The bean that is created for this tag element
     *
     * @param element
     *        The tag element
     * @return A FileFilterFactoryBean
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return FileFilterFactoryBean.class;
    }

    /**
     * Called when the fileFilter tag is to be parsed
     *
     * @param element
     *        The tag element
     * @param ctx
     *        The context in which the parsing is occuring
     * @param builder
     *        The bean definitions build to use
     */
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {

        // Set the scope
        builder.setScope(element.getAttribute("scope"));

        try {
            // All of the filters will eventually end up in this list
            // We use a 'ManagedList' and not a regular list because anything
            // placed in a ManagedList object will support all of Springs
            // functionalities and scopes for us, we dont' have to code anything
            // in terms of reference lookups, EL, etc
            ManagedList<Object> filters = new ManagedList<Object>();

            // For each child node of the fileFilter tag, parse it and place it
            // in the filtes list
            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                DefinitionParserUtil.parseLimitedList(filters, nl.item(i), ctx, builder.getBeanDefinition(),
                    element.getAttribute("scope"));
            }

            // Add the filtes to the list of properties (this is applied
            // to the factory beans setFilters below)
            builder.addPropertyValue("filters", filters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class FileFilterFactoryBean implements FactoryBean<Collection<FileFilter>> {

        private final List<FileFilter> filters = new ArrayList<FileFilter>();

        @Override
        public Collection<FileFilter> getObject() throws Exception {
            return filters;
        }

        @Override
        public Class<?> getObjectType() {
            return Collection.class;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }

        /**
         * Go through the list of filters and convert the String ones (the ones that were set with <value> and make them
         * NameFileFilters
         */
        public void setFilters(Collection<Object> filterList) {
            for (Object o : filterList) {
                if (o instanceof String) {
//                    filters.add(new NameFileFilter(o.toString()));
                } else if (o instanceof FileFilter) {
                    filters.add((FileFilter) o);
                }
            }
        }

    }
}
