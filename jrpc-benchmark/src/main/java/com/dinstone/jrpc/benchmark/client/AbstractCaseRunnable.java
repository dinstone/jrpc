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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import com.dinstone.jrpc.benchmark.BenchmarkService;

public abstract class AbstractCaseRunnable implements CaseRunnable {

    protected RunnableStatistics statistics;

    private CyclicBarrier cyclicBarrier;

    private CountDownLatch countDownLatch;

    private long startTime;

    private long stopTime;

    private int statisticTime;

    protected BenchmarkService benchmarkService;

    public AbstractCaseRunnable(BenchmarkService benchmarkService, CyclicBarrier barrier, CountDownLatch latch,
            long startTime, long stopTime) {
        this.cyclicBarrier = barrier;
        this.countDownLatch = latch;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.benchmarkService = benchmarkService;

        statisticTime = (int) ((stopTime - startTime) / 1000000);
        statistics = new RunnableStatistics(statisticTime);
    }

    @Override
    public RunnableStatistics getStatistics() {
        return statistics;
    }

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        callService();

        countDownLatch.countDown();
    }

    private void callService() {
        long beginTime = System.nanoTime() / 1000L;
        while (beginTime <= startTime) {
            // warm up
            beginTime = System.nanoTime() / 1000L;
            try {
                call();
            } catch (Exception e) {
                // ignore;
            }
        }

        while (beginTime <= stopTime) {
            beginTime = System.nanoTime() / 1000L;

            Object result = null;
            try {
                result = call();
            } catch (Exception e) {
                // ignore;
            }

            long responseTime = System.nanoTime() / 1000L - beginTime;
            collectResponseTimeDistribution(responseTime);
            int currTime = (int) ((beginTime - startTime) / 1000000L);
            if (currTime >= statisticTime) {
                continue;
            }

            if (result != null) {
                statistics.TPS[currTime]++;
                statistics.RT[currTime] += responseTime;
            } else {
                statistics.errTPS[currTime]++;
                statistics.errRT[currTime] += responseTime;
            }
        }
    }

    private void collectResponseTimeDistribution(long time) {
        double responseTime = time / 1000L;
        if (responseTime >= 0 && responseTime <= 1) {
            statistics.above0sum++;
        } else if (responseTime > 1 && responseTime <= 5) {
            statistics.above1sum++;
        } else if (responseTime > 5 && responseTime <= 10) {
            statistics.above5sum++;
        } else if (responseTime > 10 && responseTime <= 50) {
            statistics.above10sum++;
        } else if (responseTime > 50 && responseTime <= 100) {
            statistics.above50sum++;
        } else if (responseTime > 100 && responseTime <= 500) {
            statistics.above100sum++;
        } else if (responseTime > 500 && responseTime <= 1000) {
            statistics.above500sum++;
        } else if (responseTime > 1000) {
            statistics.above1000sum++;
        }
    }

    protected abstract Object call();

}
