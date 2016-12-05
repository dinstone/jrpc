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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected int concurrents;

    protected int runTime;

    protected String classname;

    protected String params;

    protected CaseStatistics statistics;

    /**
     */
    public void execute() {
        init();

        printStartInfo();

        doCase();

        printStatistics();

        destory();
    }

    protected abstract void init();

    protected void printStartInfo() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.SECOND, runTime);
        Date finishDate = calendar.getTime();

        StringBuilder startInfo = new StringBuilder(dateFormat.format(currentDate));
        startInfo.append(" ready to start client benchmark");
        startInfo.append(", concurrents = ").append(concurrents);
        startInfo.append(", data-length = ").append(params).append("KB");
        startInfo.append(", the benchmark will end at ").append(dateFormat.format(finishDate));

        System.out.println(startInfo.toString());
    }

    protected void doCase() {
        // prepare runnables
        long currentTime = System.nanoTime() / 1000L;
        long startTime = currentTime + WARMUPTIME * 1000 * 1000L;
        long stopTime = currentTime + runTime * 1000 * 1000L;

        List<CaseRunnable> runnables = new ArrayList<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(this.concurrents);
        CountDownLatch countDownLatch = new CountDownLatch(this.concurrents);
        for (int i = 0; i < this.concurrents; i++) {
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

    protected void printStatistics() {
        System.out.println("----------Benchmark Statistics--------------");
        System.out.println("Concurrents: " + concurrents);
        System.out.println("Runtime: " + runTime + " seconds");
        System.out.println("ClassName: " + classname);
        System.out.println("Params: " + params);
        statistics.printStatistics();
    }

    protected abstract void destory();
}
