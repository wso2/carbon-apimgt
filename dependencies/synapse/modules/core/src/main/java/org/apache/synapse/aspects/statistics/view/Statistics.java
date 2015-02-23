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
package org.apache.synapse.aspects.statistics.view;

import org.apache.synapse.aspects.statistics.ErrorLog;

import java.util.ArrayList;
import java.util.List;

/**
 * The statistics data structure
 */

public class Statistics {

    public static final String ALL = "all";

    /**
     * Maximum processing time for a one way flow
     */
    private long maxProcessingTime = 0;
    /**
     * Minimum processing time for a one way flow
     */
    private long minProcessingTime = -1;
    /**
     * Average processing time for a one way flow
     */
    private double avgProcessingTime = 0;
    /**
     * Total processing time for a one way flow
     */
    private double totalProcessingTime;
    /**
     * The number of access count for a one way flow
     */
    private int count = 0;
    /**
     * The number of fault count for a one way flow
     */
    private int faultCount = 0;
    /**
     * Identifier for this statistics , whose statistics
     */
    private String id;
    /**
     * List of Error log entries
     */
    private final List<ErrorLog> errorLogs = new ArrayList<ErrorLog>();

    public Statistics(String id) {
        this.id = id;
    }

    /**
     * Update the statistics
     *
     * @param currentProcessingTime - The processing end time
     * @param isFault               - A Boolean value that indicate whether fault has occurred or not
     */
    public void update(long currentProcessingTime, boolean isFault) {

        if (currentProcessingTime < 0) {
            return;
        }

        count++;
        if (isFault) {
            faultCount++;
        }

        if (maxProcessingTime < currentProcessingTime) {
            maxProcessingTime = currentProcessingTime;
        }
        if (minProcessingTime > currentProcessingTime) {
            minProcessingTime = currentProcessingTime;
        }
        if (minProcessingTime == -1) {
            minProcessingTime = currentProcessingTime;
        }
        totalProcessingTime = totalProcessingTime + currentProcessingTime;
        avgProcessingTime = totalProcessingTime / count;
    }

    /**
     * @return Returns the Maximum processing time
     */
    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    /**
     * @return Returns the Average processing time
     */
    public double getAvgProcessingTime() {
        return avgProcessingTime;
    }

    /**
     * @return Returns the minimum processing time
     */
    public long getMinProcessingTime() {
        return minProcessingTime;
    }

    /**
     * @return Returns the fault count
     */
    public int getFaultCount() {
        return faultCount;
    }

    /**
     * @return Returns the total count that represents number of access in a one way flow
     */
    public int getCount() {
        return count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ErrorLog> getErrorLogs() {
        return errorLogs;
    }

    public void addErrorLog(ErrorLog errorLog) {
        if (errorLog != null) {
            this.errorLogs.add(errorLog);
        }
    }

    public String toString() {
        return new StringBuffer()
                .append("[Avg Processing Time : ").append(avgProcessingTime).append(" ]")
                .append(" [Max Processing Time : ").append(maxProcessingTime).append(" ]")
                .append(" [Min Processing Time : ").append(minProcessingTime).append(" ]")
                .append(" [Total Request Count : ").append(count).append(" ]")
                .append(" [Total Fault Response Count : ").append(faultCount).append(" ]")
                .toString();
    }
}
