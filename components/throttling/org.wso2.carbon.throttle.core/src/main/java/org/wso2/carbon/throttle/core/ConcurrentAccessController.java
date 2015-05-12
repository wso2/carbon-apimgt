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
package org.wso2.carbon.throttle.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls the concurrent access through throttle to the system based on policy
 */

public class ConcurrentAccessController implements Serializable {

    private static Log log = LogFactory.getLog(ConcurrentAccessController.class.getName());

    /* The Max number of concurrent access */
    private int limit;
    /* The counter variable - hold current access count */
    private AtomicInteger counter;

    private static final long serialVersionUID = -6857325377726757251L;

    public ConcurrentAccessController(int limit) {
        this.limit = limit;
        this.counter = new AtomicInteger(limit);
    }

    /**
     * Decrements by one the current value and Returns the previous value
     *
     * @return the previous value
     */
    public int getAndDecrement() {
        int ret = this.counter.getAndDecrement();
        if (ret <= 0) {
            this.counter.incrementAndGet();
            return 0;
        } else {
            return ret;
        }
    }

    /**
     * Increments by one the current value and Returns the the updated value
     *
     * @return the updated value
     */
    public int incrementAndGet() {
        int ret = this.counter.incrementAndGet();
        if (ret < 0) {
            return 0;
        }
        return ret;
    }

    /**
     * Gets  the Max number of access - access limit
     *
     * @return the limit
     */
    public int getLimit() {
        return this.limit;
    }

    /**
     * Resets the counter and the limit
     *
     * @param newValue The new Value
     */
    public void set(int newValue) {
        this.counter.set(newValue);
        this.limit = newValue;
    }

}
