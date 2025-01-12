/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents the Validation Task Scheduler, which is responsible
 * for scheduling and processing validation tasks.
 */
public class ValidationTaskScheduler {

    private static final Log log = LogFactory.getLog(ValidationTaskScheduler.class);

    private static final int TASK_PROCESSOR_THREAD_POOL_SIZE = 10;
    private static final int CHECK_INTERVAL_MINUTES = 1;

    private static ScheduledExecutorService taskScheduler;
    private static ExecutorService taskProcessorPool;

    /**
     * Initialize the Validation Task Scheduler
     */
    public static void initialize() {
        log.info("Initializing Validation Task Scheduler...");

        // Create a single-threaded scheduler for task checking
        taskScheduler = Executors.newSingleThreadScheduledExecutor();

        // Create a thread pool of 10 threads for task processing
        taskProcessorPool = Executors.newFixedThreadPool(TASK_PROCESSOR_THREAD_POOL_SIZE);
        if (log.isDebugEnabled()) {
            log.debug("Task Processor Pool with size " + TASK_PROCESSOR_THREAD_POOL_SIZE + " created.");
        }

        startTaskScheduler();
    }

    private static void startTaskScheduler() {

        taskScheduler.scheduleAtFixedRate(() -> {
            if (log.isDebugEnabled()) {
                log.debug("Checking for new validation tasks...");
            }

            try {

                boolean hasTasks = checkForNewTasks();

                if (hasTasks) {
                    // Submit tasks to the task processor pool for execution
                    taskProcessorPool.submit(() -> processTasks());
                }
            } catch (Exception e) {
                log.error("Error while checking for new validation tasks: ", e);
            }
        }, 0, CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);

        log.info("Task Scheduler started. Task checking will occur every " + CHECK_INTERVAL_MINUTES + " minutes.");
    }

    private static boolean checkForNewTasks() {
        // Simulate checking the database for new tasks
        log.info("Simulating task checking...");
        return true; // Replace with actual logic to check for tasks
    }

    private static void processTasks() {
        try {
            log.info("Processing validation tasks...");
            // Replace this with the actual task processing logic
            Thread.sleep(2000); // Simulate task processing
            log.info("Validation tasks processed successfully.");
        } catch (Exception e) {
            log.error("Error while processing validation tasks: ", e);
        }
    }
    
    /**
     * Shut down the Validation Task Scheduler
     */
    public static void shutdown() {
        log.info("Shutting down Validation Task Scheduler...");

        // Shut down the task scheduler
        if (taskScheduler != null) {
            taskScheduler.shutdown();
            try {
                if (!taskScheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown of Task Scheduler...");
                    taskScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Task Scheduler shutdown interrupted: ", e);
                taskScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Shut down the task processor pool
        if (taskProcessorPool != null) {
            taskProcessorPool.shutdown();
            try {
                if (!taskProcessorPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown of Task Processor Pool...");
                    taskProcessorPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Task Processor Pool shutdown interrupted: ", e);
                taskProcessorPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("Validation Task Scheduler shut down.");
    }
}
