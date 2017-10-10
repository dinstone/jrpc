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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractBenchmarkClient {

    private static final int WARMUPTIME = 30;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    protected CaseConfig caseConfig;

    protected CaseStatistics statistics;

    public AbstractBenchmarkClient(CaseConfig caseConfig) {
        this.caseConfig = caseConfig;
    }

    /**
     */
    public void execute() {
        init();

        printCaseInfo();

        doCase();

        printStatistics();

        destory();
    }

    protected abstract void init();

    protected void doCase() {
        long currentTime = System.nanoTime() / 1000L;
        long startTime = currentTime + WARMUPTIME * 1000 * 1000L;
        long stopTime = currentTime + caseConfig.runTimeSeconds * 1000 * 1000L;

        int concurrents = caseConfig.concurrents;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(concurrents);
        CountDownLatch countDownLatch = new CountDownLatch(concurrents);
        List<CaseRunnable> runnables = new ArrayList<>();
        for (int i = 0; i < concurrents; i++) {
            CaseRunnable runnable = getCaseRunnable(cyclicBarrier, countDownLatch, startTime, stopTime);
            runnables.add(runnable);
            new Thread(runnable, "benchmarkclient-" + i).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<RunnableStatistics> runnableStatisticses = new ArrayList<>();
        for (CaseRunnable runnable : runnables) {
            runnableStatisticses.add(runnable.getStatistics());
        }
        statistics = new CaseStatistics(runnableStatisticses);
        statistics.collectStatistics();
    }

    protected abstract CaseRunnable getCaseRunnable(CyclicBarrier barrier, CountDownLatch latch, long startTime,
            long endTime);

    protected void printCaseInfo() {
        Date startTime = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.SECOND, caseConfig.runTimeSeconds);
        Date finishTime = calendar.getTime();

        System.out.println("----------------------Benchmark Test----------------------");
        System.out.println("Case Info  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Transport : " + caseConfig.transportSchema);
        System.out.println("Connection: " + caseConfig.connectPoolSize);
        System.out.println("ClassName : " + caseConfig.caseClassName);
        System.out.println("Concurrent: " + caseConfig.concurrents);
        System.out.println("DataLength: " + caseConfig.dataLength);
        System.out.println("Runtime(second): " + caseConfig.runTimeSeconds);
        System.out.println("StartTime : " + dateFormat.format(startTime));
        System.out.println("FinishTime: " + dateFormat.format(finishTime));
        // System.out.println("--------------------------------------------");
    }

    protected void printStatistics() {
        System.out.println("Statistics >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // System.out.println("----------Benchmark Statistics--------------");
        // System.out.println("ClassName: " + caseConfig.caseClassName);
        // System.out.println("Runtime(second): " + caseConfig.runTimeSeconds);
        // System.out.println("Concurrent: " + caseConfig.concurrents);
        // System.out.println("DataLength: " + caseConfig.dataLength);
        // System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        statistics.printStatistics();

        System.out.println("----------------------------------------------------------");
    }

    protected abstract void destory();
}
