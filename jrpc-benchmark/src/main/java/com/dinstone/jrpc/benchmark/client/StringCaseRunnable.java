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
package com.dinstone.jrpc.benchmark.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

import com.dinstone.jrpc.benchmark.BenchmarkService;

public class StringCaseRunnable extends AbstractCaseRunnable {

    private String caseString;

    public StringCaseRunnable(BenchmarkService service, CaseConfig caseConfig, CyclicBarrier barrier,
            CountDownLatch latch, long startTime, long endTime) {
        super(service, barrier, latch, startTime, endTime);

        int length = caseConfig.dataLength;
        StringBuilder builder = new StringBuilder(length);
        ThreadLocalRandom current = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            builder.append((char) (current.nextInt(33, 128)));
        }
        caseString = builder.toString();
    }

    @Override
    protected Object call() {
        return benchmarkService.echoService(caseString);
    }
}
