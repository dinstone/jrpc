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
package com.dinstone.ut.faststub;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * The factory bean that create a {@link java.util.Date} object by the specified format string.
 *
 * @author dinstone
 */
public class DateFactoryBean extends AbstractFactoryBean {

    private String pattern = "yyyy-MM-dd HH:mm:ss";

    private String dateText;

    public DateFactoryBean() {
        super();
    }

    public DateFactoryBean(String dateText) {
        this.dateText = dateText;
    }

    public DateFactoryBean(String pattern, String dateText) {
        this.pattern = pattern;
        this.dateText = dateText;
    }

    @Override
    public Class<?> getObjectType() {
        return Date.class;
    }

    @Override
    public Object createInstance() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateText);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

}
