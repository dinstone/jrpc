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

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;

public class MetricService {

    private MetricRegistry metricRegistry;

    private ScheduledReporter reporter;

    public MetricService() {
        metricRegistry = new MetricRegistry();

        reporter = Slf4jReporter.forRegistry(metricRegistry).build();
        reporter.start(5, TimeUnit.SECONDS);
    }

    public Counter getCounter(String name) {
        return metricRegistry.counter(name);
    }

    public Timer getTimer(String name) {
        return metricRegistry.timer(name);
    }

    public void destory() {
        reporter.stop();
    }

}
