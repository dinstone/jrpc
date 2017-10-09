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
package com.dinstone.jrpc.example;

import com.codahale.metrics.Timer;

/**
 * @author guojf
 * @version 1.0.0.2013-10-29
 */
public class HelloServiceImpl implements HelloService {

    private Timer counter;

    public HelloServiceImpl() {
        counter = new MetricService().getTimer("sayHello");
    }

    public HelloServiceImpl(MetricService metricService) {
        counter = metricService.getTimer("sayHello");
    }

    /**
     * {@inheritDoc}
     *
     * @see com.dinstone.jrpc.cases.HelloService#sayHello(java.lang.String)
     */
    @Override
    public String sayHello(String name) {
        try {
            return name;
        } finally {
            counter.time().stop();
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see com.dinstone.jrpc.cases.SuperInterface#sayHello(java.lang.String, int)
     */
    @Override
    public String sayHello(String name, int age) {
        if (age < 3) {
            return "hi, baby " + name;
        }

        return "hi, opp " + name;
    }

}
