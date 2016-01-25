/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dinstone.jrpc.RpcException;
import com.dinstone.jrpc.protocol.Result;

/**
 * @author guojf
 * @version 1.0.0.2013-5-2
 */
public class CallFuture {

    private Lock lock = new ReentrantLock();

    private Condition ready = lock.newCondition();

    private boolean done;

    private Result result;

    private List<CallFutureListener> listeners;

    /**
     * 
     */
    public CallFuture() {
        super();
    }

    public Object get() throws InterruptedException {
        lock.lock();
        try {
            while (!done) {
                ready.await();
            }
            return getValue();
        } finally {
            lock.unlock();
        }

    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            if (!done) {
                boolean success = ready.await(timeout, unit);
                if (!success) {
                    throw new TimeoutException("operation timeout (" + timeout + " " + unit + ")");
                }
            }
            return getValue();
        } finally {
            lock.unlock();
        }
    }

    public void setResult(Result result) {
        setValue(result);
    }

    private Object getValue() {
        if (result.getCode() != 200) {
            Throwable fault = (Throwable) result.getData();
            if (fault != null) {
                throw new RpcException(result.getCode(), result.getMessage(), fault);
            } else {
                throw new RpcException(result.getCode(), result.getMessage());
            }
        } else {
            return result.getData();
        }
    }

    /**
     * @param result
     */
    private void setValue(Result result) {
        lock.lock();
        try {
            if (done) {
                return;
            }

            this.result = result;
            done = true;
            this.ready.signalAll();
        } finally {
            lock.unlock();
        }

        if (listeners != null) {
            for (CallFutureListener listener : listeners) {
                notifyListener(listener);
            }
            listeners = null;
        }
    }

    private void notifyListener(CallFutureListener listener) {
        if (listener != null) {
            try {
                listener.complete(this);
            } catch (Exception e) {
            }
        }
    }

    public CallFuture addListener(CallFutureListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener");
        }

        boolean notifyNow = false;
        lock.lock();
        try {
            if (done) {
                notifyNow = true;
            } else {
                if (listeners == null) {
                    listeners = new ArrayList<CallFutureListener>(1);
                }
                listeners.add(listener);
            }
        } finally {
            lock.unlock();
        }

        if (notifyNow) {
            notifyListener(listener);
        }
        return this;
    }
}
