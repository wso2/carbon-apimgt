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

package org.apache.synapse.commons.jmx;

import java.util.Date;

/**
 * Provides metrics related to a given thread pool or group.
 */
public interface ThreadingViewMBean {

    public int getTotalWorkerCount();
    public String[] getDeadLockedWorkers();
    public double getAvgBlockedWorkerPercentage();
    public double getAvgUnblockedWorkerPercentage();
    public double getLastMinuteBlockedWorkerPercentage();
    public double getLast5MinuteBlockedWorkerPercentage();
    public double getLast15MinuteBlockedWorkerPercentage();
    public double getLastHourBlockedWorkerPercentage();
    public double getLast8HourBlockedWorkerPercentage();
    public double getLast24HourBlockedWorkerPercentage();
    public void reset();
    public Date getLastResetTime();

}
