/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.commons.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.transport.base.threads.NativeThreadFactory;

import java.util.concurrent.*;
import java.util.Map;
import java.util.HashMap;

/**
 * This is the class used for executing the tasks with a given priority. It is backed by a
 * BlockingQueue and a ThreadPoolExecutor. The BlockingQueue is a custom implementation which 
 * has multiple internal queues for handling separate priorities.
 */
public class PriorityExecutor {
    private final Log log = LogFactory.getLog(PriorityExecutor.class);    

    /** Actual thread pool executor */
    private ThreadPoolExecutor executor;
    /** Name of the executor */
    private String name = null;
    /** Core threads count */
    private int core = ExecutorConstants.DEFAULT_CORE;
    /** Max thread count */
    private int max = ExecutorConstants.DEFAULT_MAX;
    /** Keep alive time for spare threads */
    private int keepAlive = ExecutorConstants.DEFAULT_KEEP_ALIVE;
    /** This will be executed before the Task is submitted  */
    private BeforeExecuteHandler beforeExecuteHandler;
    /** Queue used by the executor */
    private MultiPriorityBlockingQueue<Runnable> queue;
    /** this is used by the file based synapse xml configuration */
    private String fileName;
    /** Weather executor is initializer */
    private boolean initialzed;

    /**
     * Execute a given task with the priority specified. If the task throws an exception,
     * it will be captured and logged to prevent the threads from dying. 
     *
     * @param task task to be executed
     * @param priority priority of the task
     */
    public void execute(final Runnable task, int priority) {
        if (!initialzed) {
            throw new IllegalStateException("Executor is not initialized");
        }
        // create a dummy worker to execute the task
        Worker w = new Worker(task, priority);

        if (beforeExecuteHandler != null) {
            beforeExecuteHandler.beforeExecute(w);
        }
        // we are capturing all the exceptions to prevent threads from dying
        executor.execute(w);
    }


    /**
     * Initialize the executor by using the properties. Create the queues
     * and ThreadPool executor.
     */
    public void init() {
        if (queue == null) {
            throw new IllegalStateException("Queue should be specified before initializing");
        }

        executor = new ThreadPoolExecutor(core, max, keepAlive, TimeUnit.SECONDS, queue,
                new NativeThreadFactory(new ThreadGroup("executor-group"),
                        "priority-worker" + (name != null ? "-" + name : "")));

        initialzed = true;

        if (log.isDebugEnabled()) {
            log.debug("Started the thread pool executor with threads, " +
                    "core = " + core + " max = " + max +
                    ", keep-alive = " + keepAlive);
        }
    }

    /**
     * Destroy the executor. Stop all the threads running. 
     */
    public void destroy() {
        if (initialzed) {
            if (log.isDebugEnabled()) {
                log.debug("Shutting down priority executor" + (name != null ? ": " + name : ""));
            }

            executor.shutdown();

            try {
                executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("Failed to Shut down Executor");
            }

            initialzed = false;
        }
    }

    /**
     * Set the name of the executor
     *
     * @param name of the executor
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of the executor
     *
     * @return name of the executor
     */
    public String getName() {
        return name;
    }

    /**
     * Set a handler for execute before putting a worker in to the queues.
     * User can set some properties to the worker at this point. This
     * allows users to get more control over the queue selection algorithm.
     * This is an optional configuration.
     *
     * @param beforeExecuteHandler an object implementing the BeforeExecuteHandler 
     */
    public void setBeforeExecuteHandler(
            BeforeExecuteHandler beforeExecuteHandler) {
        this.beforeExecuteHandler = beforeExecuteHandler;
    }

    /**
     * Get the handler that is executed before the worker is put in to the queue
     *
     * @return an object of BeforeExecuteHandler 
     */
    public BeforeExecuteHandler getBeforeExecuteHandler() {
        return beforeExecuteHandler;
    }

    /**
     * Set the queue.
     *
     * @param queue queue used for handling the priorities
     */
    public void setQueue(MultiPriorityBlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    /**
     * Get the queue.
     *
     * @return queue used for handling multiple priorities
     */
    public MultiPriorityBlockingQueue<Runnable> getQueue() {
        return queue;
    }

    /**
     * Get the core number of threads
     *
     * @return core number of threads
     */
    public int getCore() {
        return core;
    }

    /**
     * Get the max threads
     *
     * @return max thread
     */
    public int getMax() {
        return max;
    }

    /**
     * Get the keep alive time for threads
     *
     * @return keep alive time for threads
     */
    public int getKeepAlive() {
        return keepAlive;
    }

    /**
     * Set the core number of threads
     *
     * @param core core number of threads
     */
    public void setCore(int core) {
        this.core = core;
    }

    /**
     * Set the max number of threads
     *
     * @param max max threads
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * Set the keep alive time for threads
     *
     * @param keepAlive keep alive threads
     */
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Get the file used to store this executor config
     *
     * @return file used for storing the config
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the file used to store the config
     *
     * @param fileName file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Private class for executing the tasks submitted. This class is used for
     * prevent the threads from dying in case of unhandled exceptions. Also
     * this class implements the Importance for carrying the priority.     
     */
    private class Worker implements Runnable, Importance {
        private Runnable runnable = null;

        private Map<String, Object> properties = new HashMap<String, Object>();

        private int priority = 1;

        private Worker(Runnable runnable, int priority) {
            this.priority = priority;
            this.runnable = runnable;
        }

        public void run() {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.error("Unhandled exception", e);
            }
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int p) {
            this.priority = p;
        }

        public void setProperty(String name, Object value) {
            properties.put(name, value);
        }

        public Object getProperty(String name) {
            return properties.get(name);
        }
    }
}
