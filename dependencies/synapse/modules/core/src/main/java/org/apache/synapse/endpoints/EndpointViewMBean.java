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

package org.apache.synapse.endpoints;

import java.util.Map;
import java.util.Date;

public interface EndpointViewMBean {

    // JMX Attributes
    public long getMessagesReceived();
    public long getFaultsReceiving();
    public long getTimeoutsReceiving();
    public long getBytesReceived();
    public long getMinSizeReceived();
    public long getMaxSizeReceived();
    public double getAvgSizeReceived();
    public Map getReceivingFaultTable();

    public long getMessagesSent();
    public long getFaultsSending();
    public long getTimeoutsSending();
    public long getBytesSent();
    public long getMinSizeSent();
    public long getMaxSizeSent();
    public double getAvgSizeSent();
    public Map getSendingFaultTable();
    public Map getResponseCodeTable();

    public Date getSuspendedAt();
    public Date getTimedoutAt();
    public int getConsecutiveEndpointSuspensions();
    public int getConsecutiveEndpointTimeouts();
    public int getTotalEndpointSuspensions();
    public int getTotalEndpointTimeouts();
    public int getLastMinuteEndpointSuspensions();
    public int getLast5MinuteEndpointSuspensions();
    public int getLast15MinuteEndpointSuspensions();
    public int getLastMinuteEndpointTimeouts();
    public int getLast5MinuteEndpointTimeouts();
    public int getLast15MinuteEndpointTimeouts();

    // JMX Operations
    public void switchOn() throws Exception;
    public void switchOff() throws Exception;

    public boolean isActive() throws Exception;
    public boolean isTimedout() throws Exception;
    public boolean isSuspended() throws Exception;
    public boolean isSwitchedOff() throws Exception;

    public int getTotalChildren() throws Exception;
    public int getActiveChildren() throws Exception;
    public int getReadyChildren() throws Exception;

    public void resetStatistics();
    public long getLastResetTime();
    public long getMetricsWindow();
}
