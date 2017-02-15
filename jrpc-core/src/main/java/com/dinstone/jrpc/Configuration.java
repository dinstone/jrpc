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

package com.dinstone.jrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    /** Prefix for system property placeholders: "${" */
    private static final String PLACEHOLDER_PREFIX = "${";

    /** Suffix for system property placeholders: "}" */
    private static final String PLACEHOLDER_SUFFIX = "}";

    protected final Properties properties = new Properties();

    /**
     *
     */
    public Configuration() {
    }

    /**
     *
     */
    public Configuration(String configLocation) {
        if (configLocation == null) {
            throw new IllegalArgumentException("configLocation is null");
        }
        InputStream stream = getResourceStream(configLocation);
        if (stream == null) {
            throw new IllegalArgumentException("can't find out configuration [" + configLocation + "] from classpath.");
        }

        loadConfiguration(stream);
    }

    public Configuration(Configuration config) {
        mergeConfiguration(config);
    }

    public void mergeConfiguration(Configuration config) {
        if (config != null) {
            this.properties.putAll(config.properties);
        }
    }

    public void writeConfiguration(OutputStream out) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element conf = doc.createElement("configuration");
            doc.appendChild(conf);
            for (Enumeration<?> e = properties.keys(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                Object object = properties.get(name);
                String value = null;
                if (object instanceof String) {
                    value = (String) object;
                } else {
                    continue;
                }
                Element propNode = doc.createElement("property");
                conf.appendChild(propNode);

                Element nameNode = doc.createElement("name");
                nameNode.appendChild(doc.createTextNode(name));
                propNode.appendChild(nameNode);

                Element valueNode = doc.createElement("value");
                valueNode.appendChild(doc.createTextNode(value));
                propNode.appendChild(valueNode);
            }

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfiguration(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("inputstream is null");
        }
        try {
            this.properties.putAll(loadResource(in));
        } catch (Exception e) {
            throw new IllegalStateException("can't load configuration from inputstream.", e);
        }
    }

    /**
     * @param resource
     * @return
     */
    private static InputStream getResourceStream(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = Configuration.class.getClassLoader();
        }
        return classLoader.getResourceAsStream(resource);
    }

    private static Properties loadResource(InputStream in) throws IOException, ParserConfigurationException,
            SAXException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        // ignore all comments inside the xml file
        builderFactory.setIgnoringComments(true);
        // builderFactory.setCoalescing(true);
        builderFactory.setIgnoringElementContentWhitespace(true);
        builderFactory.setNamespaceAware(true);
        // allow includes in the xml file
        try {
            builderFactory.setXIncludeAware(true);
        } catch (UnsupportedOperationException e) {
            LOG.error("Failed to set setXIncludeAware(true) for parser " + builderFactory + ":" + e, e);
        }

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = null;
        try {
            doc = builder.parse(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        Element root = doc.getDocumentElement();
        if (!"configuration".equals(root.getTagName())) {
            LOG.error("bad config file: top-level element not <configuration>");
            throw new IllegalStateException("bad config file: top-level element not <configuration>");
        }

        Properties properties = new Properties();
        parseResource(properties, root);

        return properties;
    }

    /**
     * @param properties
     * @param root
     */
    private static void parseResource(Properties properties, Element root) {
        NodeList props = root.getChildNodes();
        for (int i = 0; i < props.getLength(); i++) {
            Node propNode = props.item(i);
            if (!(propNode instanceof Element)) {
                continue;
            }
            Element prop = (Element) propNode;
            if ("configuration".equals(prop.getTagName())) {
                parseResource(properties, prop);
                continue;
            }
            if (!"property".equals(prop.getTagName())) {
                LOG.warn("bad config file: element not <property>,skip this element {}", prop);
                continue;
            }

            NodeList fields = prop.getChildNodes();
            String attr = null;
            String value = null;
            for (int j = 0; j < fields.getLength(); j++) {
                Node fieldNode = fields.item(j);
                if (!(fieldNode instanceof Element)) {
                    continue;
                }
                Element field = (Element) fieldNode;
                if ("name".equals(field.getTagName()) && field.hasChildNodes()) {
                    attr = ((Text) field.getFirstChild()).getData().trim();
                }
                if ("value".equals(field.getTagName()) && field.hasChildNodes()) {
                    value = ((Text) field.getFirstChild()).getData();
                    value = resolvePlaceholders(value);
                }
            }

            if (attr != null && value != null) {
                properties.setProperty(attr, value);
            }
        }
    }

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding system property values.
     *
     * @param text
     *        the String to resolve
     * @return the resolved String
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    private static String resolvePlaceholders(String text) {
        StringBuilder buf = new StringBuilder(text);

        int startIndex = buf.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                try {
                    String propVal = System.getProperty(placeholder);
                    if (propVal == null) {
                        // Fall back to searching the system environment.
                        propVal = System.getenv(placeholder);
                    }
                    if (propVal != null) {
                        buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
                        nextIndex = startIndex + propVal.length();
                    } else {
                        System.err.println("Could not resolve placeholder '" + placeholder + "' in [" + text
                                + "] as system property: neither system property nor environment variable found");
                    }
                } catch (Throwable ex) {
                    System.err.println("Could not resolve placeholder '" + placeholder + "' in [" + text
                            + "] as system property: " + ex);
                }
                startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
            } else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

    public Configuration setProperties(Properties other) {
        if (other != null) {
            this.properties.putAll(other);
        }

        return this;
    }

    public Properties getProperties() {
        return new Properties(properties);
    }

    /**
     * Get the value of the <code>name</code> property, <code>null</code> if no such property exists.
     *
     * @param name
     * @return
     */
    public String get(String name) {
        return properties.getProperty(name);
    }

    /**
     * Get the value of the <code>name</code> property, <code>defaultValue</code> if no such property exists.
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public String get(String name, String defaultValue) {
        String ret = properties.getProperty(name);
        return ret == null ? defaultValue : ret;
    }

    /**
     * Set the <code>value</code> of the <code>name</code> property.
     *
     * @param name
     *        property name.
     * @param value
     *        property value.
     * @return
     */
    public Configuration set(String name, String value) {
        properties.setProperty(name, value);
        return this;
    }

    /**
     * Get the value of the <code>name</code> property as an <code>int</code>. If no such property exists, or if the
     * specified value is not a valid <code>int</code>, then <code>defaultValue</code> is returned.
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public int getInt(String name, int defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            String hexString = getHexDigits(valueString);
            if (hexString != null) {
                return Integer.parseInt(hexString, 16);
            }
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to an <code>int</code>.
     *
     * @param name
     *        property name.
     * @param value
     *        <code>int</code> value of the property.
     * @return
     */
    public Configuration setInt(String name, int value) {
        set(name, Integer.toString(value));
        return this;
    }

    /**
     * Get the value of the <code>name</code> property as a <code>long</code>. If no such property is specified, or if
     * the specified value is not a valid <code>long</code>, then <code>defaultValue</code> is returned.
     *
     * @param name
     *        property name.
     * @param defaultValue
     *        default value.
     * @return property value as a <code>long</code>, or <code>defaultValue</code>.
     */
    public long getLong(String name, long defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            String hexString = getHexDigits(valueString);
            if (hexString != null) {
                return Long.parseLong(hexString, 16);
            }
            return Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to a <code>long</code>.
     *
     * @param name
     *        property name.
     * @param value
     *        <code>long</code> value of the property.
     * @return
     */
    public Configuration setLong(String name, long value) {
        set(name, Long.toString(value));
        return this;
    }

    private String getHexDigits(String value) {
        boolean negative = false;
        String str = value;
        String hexString = null;
        if (value.startsWith("-")) {
            negative = true;
            str = value.substring(1);
        }
        if (str.startsWith("0x") || str.startsWith("0X")) {
            hexString = str.substring(2);
            if (negative) {
                hexString = "-" + hexString;
            }
            return hexString;
        }
        return null;
    }

    /**
     * Get the value of the <code>name</code> property as a <code>float</code>. If no such property is specified, or if
     * the specified value is not a valid <code>float</code>, then <code>defaultValue</code> is returned.
     *
     * @param name
     *        property name.
     * @param defaultValue
     *        default value.
     * @return property value as a <code>float</code>, or <code>defaultValue</code>.
     */
    public float getFloat(String name, float defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to a <code>float</code>.
     *
     * @param name
     *        property name.
     * @param value
     *        property value.
     * @return
     */
    public Configuration setFloat(String name, float value) {
        set(name, Float.toString(value));
        return this;
    }

    /**
     * Get the value of the <code>name</code> property as a <code>boolean</code> . If no such property is specified, or
     * if the specified value is not a valid <code>boolean</code>, then <code>defaultValue</code> is returned.
     *
     * @param name
     *        property name.
     * @param defaultValue
     *        default value.
     * @return property value as a <code>boolean</code>, or <code>defaultValue</code>.
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String valueString = get(name);
        if ("true".equals(valueString)) {
            return true;
        } else if ("false".equals(valueString)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    @Override
    public String toString() {
        return "Configuration=" + properties;
    }

}
