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
package org.apache.synapse.transport.nhttp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;

import java.io.IOException;

public class ActiveConnectionMonitor implements Runnable {

    private static final Log log = LogFactory.getLog(ActiveConnectionMonitor.class);

    /* Delay between each monitoring tasks */
    private NhttpMetricsCollector nhttpMetricsCollector;
    private DefaultListeningIOReactor ioReactor;

    /* Maximum Active Connections */
    private int maxActive;

    public ActiveConnectionMonitor(NhttpMetricsCollector metrics, DefaultListeningIOReactor ior, int maxActiveConnections) {
        ioReactor = ior;
        nhttpMetricsCollector = metrics;
        maxActive = maxActiveConnections;
    }

    public void run() {
        try {
            if (maxActive > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Active Connection Count : " + nhttpMetricsCollector.getActiveConnectionCount());

                }

                if (nhttpMetricsCollector.getActiveConnectionCount() > maxActive) {
                    log.warn("Pausing the Listening IOReactor : Too many open connections than the max_open_connections limit.");
                    ioReactor.pause();
                } else {
                    ioReactor.resume();
                }
            }
        } catch (IOException e) {
            log.warn("Active Connection Monitor Task - IO Error while pausing/resuming the IOReactor ");
        }
    }

}
