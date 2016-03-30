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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FileListDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /**
     * The bean that is created for this tag element
     *
     * @param element
     *        The tag element
     * @return A FileListFactoryBean
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return FileListFactoryBean.class;
    }

    /**
     * Called when the fileList tag is to be parsed
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
        // Set the directory property
        builder.addPropertyValue("directory", element.getAttribute("directory"));

        // Set the scope
        builder.setScope(element.getAttribute("scope"));

        // We want any parsing to occur as a child of this tag so we need to make
        // a new one that has this as it's owner/parent
        ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(),
            builder.getBeanDefinition());

        // Support for filters
        Element exclusionElem = DomUtils.getChildElementByTagName(element, "fileFilter");
        if (exclusionElem != null) {
            // Just make a new Parser for each one and let the parser do the work
            FileFilterDefinitionParser ff = new FileFilterDefinitionParser();
            builder.addPropertyValue("filters", ff.parse(exclusionElem, nestedCtx));
        }

        // Support for nested fileList
        List<Element> fileLists = DomUtils.getChildElementsByTagName(element, "fileList");
        // Any objects that created will be placed in a ManagedList
        // so Spring does the bulk of the resolution work for us
        ManagedList<Object> nestedFiles = new ManagedList<Object>();
        if (fileLists.size() > 0) {
            // Just make a new Parser for each one and let them do the work
            FileListDefinitionParser fldp = new FileListDefinitionParser();
            for (Element fileListElem : fileLists) {
                nestedFiles.add(fldp.parse(fileListElem, nestedCtx));
            }
        }

        // Support for other tags that return File (value will be converted to file)
        try {
            // Go through any other tags we may find. This does not mean we support
            // any tag, we support only what parseLimitedList will process
            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                // Parse each child tag we find in the correct scope but we
                // won't support custom tags at this point as it coudl destablize things
                DefinitionParserUtil.parseLimitedList(nestedFiles, nl.item(i), ctx, builder.getBeanDefinition(),
                    element.getAttribute("scope"), false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set the nestedFiles in the properties so it is set on the FactoryBean
        builder.addPropertyValue("nestedFiles", nestedFiles);

    }

    public static class FileListFactoryBean implements FactoryBean<Collection<File>> {

        String directory;

        private Collection<FileFilter> filters;

        private Collection<File> nestedFiles;

        @Override
        public Collection<File> getObject() throws Exception {
            // These can be an array list because the directory will have unique's and the nested is already only
            // unique's
            Collection<File> files = new ArrayList<File>();
            Collection<File> results = new ArrayList<File>(0);

            if (directory != null) {
                // get all the files in the directory
                File dir = new File(directory);
                File[] dirFiles = dir.listFiles();
                if (dirFiles != null) {
                    files = Arrays.asList(dirFiles);
                }
            }

            // If there are any files that were created from the nested tags,
            // add those to the list of files
            if (nestedFiles != null) {
                files.addAll(nestedFiles);
            }

            // If there are filters we need to go through each filter
            // and see if the files in the list pass the filters.
            // If the files does not pass any one of the filters then it
            // will not be included in the list
            if (filters != null) {
                boolean add;
                for (File f : files) {
                    add = true;
                    for (FileFilter ff : filters) {
                        if (!ff.accept(f)) {
                            add = false;
                            break;
                        }
                    }
                    if (add)
                        results.add(f);
                }
                return results;
            }

            return files;
        }

        @Override
        public Class<?> getObjectType() {
            return Collection.class;
        }

        @Override
        public boolean isSingleton() {
            return false;
        }

        public void setDirectory(String dir) {
            this.directory = dir;
        }

        public void setFilters(Collection<FileFilter> filters) {
            this.filters = filters;
        }

        /**
         * What we actually get from the processing of the nested tags is a collection of files within a collection so
         * we flatten it and only keep the uniques
         */
        public void setNestedFiles(Collection<Collection<File>> nestedFiles) {
            this.nestedFiles = new HashSet<File>(); // keep the list unique
            for (Collection<File> nested : nestedFiles) {
                this.nestedFiles.addAll(nested);
            }
        }

    }
}
