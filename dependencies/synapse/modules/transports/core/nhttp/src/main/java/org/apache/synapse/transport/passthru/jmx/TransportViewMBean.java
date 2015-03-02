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

package org.apache.synapse.transport.passthru.jmx;

import java.util.Map;

public interface TransportViewMBean {

    // JMX Attributes
    public long getMessagesReceived();
    public long getFaultsReceiving();
    public long getTimeoutsReceiving();
    public long getMessagesSent();
    public long getFaultsSending();
    public long getTimeoutsSending();
    public long getBytesReceived();
    public long getBytesSent();
    public long getMinSizeReceived();
    public long getMaxSizeReceived();
    public double getAvgSizeReceived();
    public long getMinSizeSent();
    public long getMaxSizeSent();
    public double getAvgSizeSent();
    public int  getActiveThreadCount();
    public int getQueueSize();
    public Map getResponseCodeTable();

    // JMX Operations
    public void start() throws Exception;
    public void stop() throws Exception;
    public void pause() throws Exception;
    public void resume() throws Exception;
    public void maintenenceShutdown(long seconds) throws Exception;

    public void resetStatistics();
    public long getLastResetTime();
    public long getMetricsWindow();
}
