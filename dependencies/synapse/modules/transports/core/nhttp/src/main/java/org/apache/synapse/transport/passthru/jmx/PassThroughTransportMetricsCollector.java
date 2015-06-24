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

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.MetricsCollector;

/**
 * <p>This simple extension of the Axis2 transport MetricsCollector implementation,
 * maintains a ConnectionsView instance, which is updated based on the events fired
 * by the pass-through transport implementation. In addition to the usual events handled
 * by the Axis2 MetricsCollector, this implementation handles two new events:</p>
 * <ul>
 *    <li>connected (this should get called whenever a new connection is created)</li>
 *    <li>disconnected (this should get called whenever an existing connection is closed)</li>
 * <ul>
 * <p>These new events are used to update the ConnectionsView at runtime.</p>
 */
public class PassThroughTransportMetricsCollector extends MetricsCollector {

    private ConnectionsView view;
    private boolean listener;

    public PassThroughTransportMetricsCollector(boolean listener, String schemeName)
            throws AxisFault {
        this.listener = listener;
        String name = schemeName + "-" + (listener ? "listener" : "sender");
        this.view = new ConnectionsView(name);
    }

    public void destroy() {
        view.destroy();
    }

    public void connected() {
        view.connected();
    }

    public void disconnected() {
        view.disconnected();
    }

    @Override
    public void notifyReceivedMessageSize(long l) {
        super.notifyReceivedMessageSize(l);
        if (l > 0) {
            view.notifyMessageSize(l, listener);
        }
    }

    @Override
    public void notifySentMessageSize(long l) {
        super.notifySentMessageSize(l);
        if (l > 0) {
            view.notifyMessageSize(l, !listener);
        }
    }

    public int getActiveConnectionCount() {
        return view.getActiveConnections();
    }
}

