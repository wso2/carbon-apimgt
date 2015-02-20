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
package org.apache.synapse.core.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.HttpCoreNIOListener;

import java.util.Map;

/**
 * Provides functionality to pause and resume listeners and transports and retrieve
 * current thread count.
 */
public class Axis2TransportHelper {

    private static Log log = LogFactory.getLog(Axis2TransportHelper.class);

    private ConfigurationContext configurationContext;

    /**
     * Creates a new Axis2TransportHelper using the provided Axis2 configuration context.
     *
     * @param  configurationContext  an Axis2 configuration context
     */
    public Axis2TransportHelper(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Pauses all Axis2 listeners which support this operation.
     */
    public void pauseListeners() {
        if ((configurationContext != null)
                && (configurationContext.getAxisConfiguration() != null)) {

            Map<String, TransportInDescription> trpIns
                    = configurationContext.getAxisConfiguration().getTransportsIn();

            for (TransportInDescription trpIn : trpIns.values()) {
                TransportListener trpLst = trpIn.getReceiver();
                if (trpLst instanceof ManagementSupport) {
                    try {
                        ((ManagementSupport) trpLst).pause();
                    } catch (AxisFault axisFault) {
                        log.error(new StringBuilder("Error putting transport listener for: ")
                                .append(trpIn.getName()).append(" into maintenence").toString());
                    }
                }
            }
        }
    }
    
    /**
     * Resumes all paused Axis2 listeners.
     */
    public void resumeListeners() {
        if ((configurationContext != null)
                && (configurationContext.getAxisConfiguration() != null)) {

            Map<String, TransportInDescription> trpIns
                    = configurationContext.getAxisConfiguration().getTransportsIn();
    
            for (TransportInDescription trpIn : trpIns.values()) {
                TransportListener trpLst = trpIn.getReceiver();
                if (trpLst instanceof ManagementSupport) {
                    try {
                        ((ManagementSupport) trpLst).resume();
                    } catch (AxisFault axisFault) {
                        log.error(new StringBuilder("Error resuming transport listener for: ")
                                .append(trpIn.getName()).append(" from maintenence").toString());
                    }
                }
            }
        }
    }
    
    /**
     * Pauses all Axis2 senders which support this operation.
     */
    public void pauseSenders() {
        if ((configurationContext != null)
                && (configurationContext.getAxisConfiguration() != null)) {

            Map<String, TransportOutDescription> trpOuts
                    = configurationContext.getAxisConfiguration().getTransportsOut();

            for (TransportOutDescription trpOut : trpOuts.values()) {
                TransportSender trpSnd = trpOut.getSender();
                if (trpSnd instanceof ManagementSupport) {
                    try {
                        ((ManagementSupport) trpSnd).pause();
                    } catch (AxisFault axisFault) {
                        log.error(new StringBuilder("Error pausing transport sender: ")
                                .append(trpOut.getName()).toString());
                    }
                }
            }
        }
    }
    
    /**
     * Resumes all paused Axis2 senders.
     */
    public void resumeSenders() {
        if ((configurationContext != null)
                && (configurationContext.getAxisConfiguration() != null)) {

            Map<String, TransportOutDescription> trpOuts
                    = configurationContext.getAxisConfiguration().getTransportsOut();

            for (TransportOutDescription trpOut : trpOuts.values()) {
                TransportSender trpSnd = trpOut.getSender();
                if (trpSnd instanceof ManagementSupport) {
                    try {
                        ((ManagementSupport) trpSnd).resume();
                    } catch (AxisFault axisFault) {
                        log.error(new StringBuilder("Error resuming transport sender for : ")
                                .append(trpOut.getName()).append(" from maintenence").toString());
                    }
                }
            }
        }
    }

    /**
     * Determines the total number of pending listener threads (active + queued).
     * 
     * @return the total number of pending listener threads (active + queued).
     */
    public int getPendingListenerThreadCount() {

        int pendingThreads = 0;
        Map<String, TransportInDescription> trpIns
                = configurationContext.getAxisConfiguration().getTransportsIn();

        for (TransportInDescription trpIn : trpIns.values()) {
            TransportListener trpLst = trpIn.getReceiver();

            if (trpLst instanceof ManagementSupport) {
                int inUse = ((ManagementSupport) trpLst).getActiveThreadCount();
                int inQue = ((ManagementSupport) trpLst).getQueueSize();

                if ((inUse + inQue) > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug(new StringBuilder("Transport Listener : ")
                                .append(trpIn.getName()).append(" currently using : ")
                                .append(inUse).append(" threads with ").append(inQue)
                                .append(" requests already queued...").toString());
                    }
                    pendingThreads = (inUse + inQue);
                }
            }
        }

        return pendingThreads;
    }

    public int getActiveConnectionsCount() {
        Map<String, TransportInDescription> trpIns
                = configurationContext.getAxisConfiguration().getTransportsIn();

        for (TransportInDescription trpIn : trpIns.values()) {
            if (trpIn.getReceiver() instanceof HttpCoreNIOListener) {
                return ((HttpCoreNIOListener) trpIn.getReceiver()).getActiveConnectionsSize();
            }
        }

        return 0;
    }

    /**
     * Determines the total number of pending sender threads (active + queued).
     * 
     * @return the total number of pending sender threads (active + queued).
     */
    public int getPendingSenderThreadCount() {

        int pendingThreads = 0;
        Map<String, TransportOutDescription> trpOuts
                = configurationContext.getAxisConfiguration().getTransportsOut();

        for (TransportOutDescription trpOut : trpOuts.values()) {
            TransportSender trpSnd = trpOut.getSender();

            if (trpSnd instanceof ManagementSupport) {
                int inUse = ((ManagementSupport) trpSnd).getActiveThreadCount();
                int inQue = ((ManagementSupport) trpSnd).getQueueSize();

                if ((inUse + inQue) > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug(new StringBuilder("Transport Sender : ")
                                .append(trpSnd.getName()).append(" currently using : ")
                                .append(inUse).append(" threads with ").append(inQue)
                                .append(" requests already queued...").toString());
                    }
                    pendingThreads += (inUse + inQue);
                }
            }
        }

        return pendingThreads;
    }
}
