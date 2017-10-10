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
package com.dinstone.jrpc.spring.spi;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dinstone.jrpc.api.Client;
import com.dinstone.jrpc.spring.factory.ClientFactoryBean;

public class ClientBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ClientFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String id = element.getAttribute("id");
        if (!StringUtils.hasText(id)) {
            builder.addPropertyValue("clientId", Client.class.getName());
            element.setAttribute("id", Client.class.getName());
        }

        // ================================================
        // Transport config
        // ================================================
        builder.addPropertyValue("transportBean", getTransportBeanDefinition(element, parserContext));

        // ================================================
        // Registry config
        // ================================================
        builder.addPropertyValue("registryBean", getRegistryBeanDefinition(element, parserContext));
    }

    // private List<BeanDefinition> getServiceBeanDefinition(Element element, ParserContext parserContext) {
    // List<BeanDefinition> sblist = new ManagedList<BeanDefinition>();
    //
    // Element services = getChildElement(element, "services");
    // if (services != null) {
    // NodeList snl = services.getChildNodes();
    // for (int i = 0; i < snl.getLength(); i++) {
    // Node node = snl.item(i);
    // if (node instanceof Element && nodeMatch(node, "service")) {
    // Element sn = (Element) node;
    //
    // BeanDefinitionBuilder sbd = BeanDefinitionBuilder.genericBeanDefinition(ServiceBean.class);
    //
    // String si = sn.getAttribute("interface");
    // sbd.addPropertyValue("service", si);
    //
    // String so = sn.getAttribute("implement");
    // sbd.addPropertyReference("instance", so);
    //
    // String sg = sn.getAttribute("group");
    // sbd.addPropertyValue("group", sg);
    //
    // String st = sn.getAttribute("timeout");
    // sbd.addPropertyValue("timeout", st);
    //
    // sblist.add(sbd.getBeanDefinition());
    // }
    // }
    // }
    //
    // return sblist;
    // }

    private BeanDefinition getRegistryBeanDefinition(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder sbd = BeanDefinitionBuilder.genericBeanDefinition(RegistryBean.class);

        Element registry = getChildElement(element, "registry");
        if (registry != null) {
            String schema = registry.getAttribute("schema");
            sbd.addPropertyValue("schema", schema);

            String addresses = registry.getAttribute("addresses");
            sbd.addPropertyValue("addresses", addresses);

            String basePath = registry.getAttribute("basePath");
            if (StringUtils.hasText(basePath)) {
                sbd.addPropertyValue("basePath", basePath);
            }
        }

        return sbd.getBeanDefinition();
    }

    protected BeanDefinition getTransportBeanDefinition(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder sbd = BeanDefinitionBuilder.genericBeanDefinition(TransportBean.class);
        String addresses = element.getAttribute("addresses");
        if (StringUtils.hasText(addresses)) {
            sbd.addPropertyValue("addresses", addresses);
        }

        String transport = element.getAttribute("transport");
        if (StringUtils.hasText(transport)) {
            sbd.addPropertyValue("type", transport);
        }

        return sbd.getBeanDefinition();
    }

    public static Element getChildElement(Element ele, String childName) {
        NodeList nl = ele.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element && nodeMatch(node, childName)) {
                return (Element) node;
            }
        }
        return null;
    }

    private static boolean nodeMatch(Node node, String desiredName) {
        return (desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName()));
    }

}