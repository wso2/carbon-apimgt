/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.core.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool implementation which accepts ObserverNotifier(Runnable) objects and execute them using separate threads
 * available in a pool of threads.
 * Singleton: Bill Pugh implementation has been used.
 */
public class ObserverNotifierThreadPool {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 50;
    private static final long THREAD_ALIVE_TIME = 300L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private ThreadPoolExecutor executor;

    /**
     * Private constructor to make the class singleton.
     */
    private ObserverNotifierThreadPool() {
        BlockingQueue<Runnable> threadPool = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, THREAD_ALIVE_TIME, TIME_UNIT,
                threadPool);
        executor.prestartAllCoreThreads();
    }

    /**
     * A static class which holds the instance of ObserverNotifierThreadPool class.
     */
    private static class SingletonHelper {
        private static final ObserverNotifierThreadPool instance = new ObserverNotifierThreadPool();
    }

    /**
     * To get the instance of ObserverNotifierThreadPool class.
     *
     * @return Object of class ObserverNotifierThreadPool
     */
    public static ObserverNotifierThreadPool getInstance() {
        return SingletonHelper.instance;
    }

    /**
     * To execute a Runnable task provided.
     * This will handover the Runnable job to a separate thread available in thread pool.
     *
     * @param observerNotifier ObserverNotifier(Runnable) object
     */
    public void executeTask(ObserverNotifier observerNotifier) {
        executor.execute(observerNotifier);
    }
}
