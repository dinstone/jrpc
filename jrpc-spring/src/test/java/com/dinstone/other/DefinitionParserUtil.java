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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DefinitionParserUtil {

    /**
     * Parses the children of the passed in ParentNode for the following tags: <br/>
     * value ref idref bean property *custom*
     * <p/>
     * The value tag works with Spring EL even in a Spring Batch scope="step"
     *
     * @param objects
     *        The list of resultings objects from the parsing (passed in for recursion purposes)
     * @param parentNode
     *        The node who's children should be parsed
     * @param ctx
     *        The ParserContext to use
     * @param parentBean
     *        The BeanDefinition of the bean who is the parent of the parsed bean (i.e. the Bean that is the parentNode)
     * @param scope
     *        The scope to execute in. Checked if 'step' to provide Spring EL support in a Spring Batch env
     * @throws Exception
     */
    public static void parseLimitedList(ManagedList<Object> objects, Node node, ParserContext ctx,
            BeanDefinition parentBean, String scope) throws Exception {
        parseLimitedList(objects, node, ctx, parentBean, scope, true);
    }

    /**
     * Parses the children of the passed in ParentNode for the following tags: <br/>
     * value ref idref bean property *custom*
     * <p/>
     * The value tag works with Spring EL even in a Spring Batch scope="step"
     *
     * @param objects
     *        The list of resultings objects from the parsing (passed in for recursion purposes)
     * @param parentNode
     *        The node who's children should be parsed
     * @param ctx
     *        The ParserContext to use
     * @param parentBean
     *        The BeanDefinition of the bean who is the parent of the parsed bean (i.e. the Bean that is the parentNode)
     * @param scope
     *        The scope to execute in. Checked if 'step' to provide Spring EL support in a Spring Batch env
     * @param supportCustomTags
     *        Should we support custom tags within our tags?
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    public static void parseLimitedList(ManagedList<Object> objects, Node node, ParserContext ctx,
            BeanDefinition parentBean, String scope, boolean supportCustomTags) throws Exception {
        // Only worry about element nodes
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) node;
            String tagName = node.getLocalName();

            if (tagName.equals("value")) {
                String val = node.getTextContent();
                // to get around an issue with Spring Batch not parsing Spring EL
                // we will do it for them
                if (scope.equals("step") && (val.startsWith("#{") && val.endsWith("}"))
                        && (!val.startsWith("#{jobParameters"))) {
                    // Set up a new EL parser
                    ExpressionParser parser = new SpelExpressionParser();
                    // Parse the value
                    Expression exp = parser.parseExpression(val.substring(2, val.length() - 1));
                    // Place the results in the list of created objects
                    objects.add(exp.getValue());
                } else {
                    // Otherwise, just treat it as a normal value tag
                    objects.add(val);
                }
            }
            // Either of these is a just a lookup of an existing bean
            else if (tagName.equals("ref") || tagName.equals("idref")) {
                objects.add(ctx.getRegistry().getBeanDefinition(node.getTextContent()));
            }
            // We need to create the bean
            else if (tagName.equals("bean")) {
                // There is no quick little util I could find to create a bean
                // on the fly programmatically in Spring and still support all
                // Spring functionality so basically I mimic what Spring actually
                // does but on a smaller scale. Everything Spring allows is
                // still supported

                // Create a factory to make the bean
                DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
                // Set up a parser for the bean
                BeanDefinitionParserDelegate pd = new BeanDefinitionParserDelegate(ctx.getReaderContext());
                // Parse the bean get its information, now in a DefintionHolder
                BeanDefinitionHolder bh = pd.parseBeanDefinitionElement(elem, parentBean);
                // Register the bean will all the other beans Spring is aware of
                BeanDefinitionReaderUtils.registerBeanDefinition(bh, beanFactory);
                // Get the bean from the factory. This will allows Spring
                // to do all its work (EL processing, scope, etc) and give us
                // the actual bean itself
                Object bean = beanFactory.getBean(bh.getBeanName());
                objects.add(bean);
            }
            /*
             * This is handled a bit differently in that it actually sets the property on the parent bean for us based
             * on the property
             */
            else if (tagName.equals("property")) {
                BeanDefinitionParserDelegate pd = new BeanDefinitionParserDelegate(ctx.getReaderContext());
                // This method actually set eh property on the parentBean for us so
                // we don't have to add anything to the objects object
                pd.parsePropertyElement(elem, parentBean);
            } else if (supportCustomTags) {
                // handle custom tag
                BeanDefinitionParserDelegate pd = new BeanDefinitionParserDelegate(ctx.getReaderContext());
                BeanDefinition bd = pd.parseCustomElement(elem, parentBean);
                objects.add(bd);
            }
        }
    }
}
