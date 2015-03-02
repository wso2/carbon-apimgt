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

package org.apache.synapse.mediators.builtin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.UIDGenerator;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RMSequenceMediator extends AbstractMediator {

    private SynapseXPath correlation = null;
    private SynapseXPath lastMessage = null;
    private Boolean single = null;
    private String version = null;

    private static final String WSRM_SpecVersion_1_0 = "Spec_2005_02";
    private static final String WSRM_SpecVersion_1_1 = "Spec_2007_02";
    // set sequence expiry time to 5 minutes
    private static final long SEQUENCE_EXPIRY_TIME = 300000;
    private static final Map<String, Entry> sequenceMap =
            Collections.synchronizedMap(new HashMap<String, Entry>());

    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : RMSequence mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (!(synCtx instanceof Axis2MessageContext)) {
            synLog.traceOrDebug("Only axis2 message contexts are supported");

        } else {
            Axis2MessageContext axis2MessageCtx = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext orgMessageCtx =
                axis2MessageCtx.getAxis2MessageContext();

            cleanupSequenceMap();

            String version = getVersionValue();
            orgMessageCtx.getOptions().setProperty(
                SandeshaClientConstants.RM_SPEC_VERSION, version);

            // always we need to start a new sequence if there is an terminated sequence
            orgMessageCtx.getOptions().setProperty(
                        SandeshaClientConstants.AUTO_START_NEW_SEQUENCE, "true");

            if (isSingle()) {
                orgMessageCtx.getOptions().setProperty(
                    SandeshaClientConstants.LAST_MESSAGE, "true");

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Using WS-RM version " + version);
                }

            } else {

                String correlationValue = getCorrelationValue(synCtx);
                boolean lastMessage = isLastMessage(synCtx);
                String offeredSeqID = null;

                if (!sequenceMap.containsKey(correlationValue)) {
                    offeredSeqID = UIDGenerator.generateURNString();
                    orgMessageCtx.getOptions().setProperty(
                        SandeshaClientConstants.OFFERED_SEQUENCE_ID, offeredSeqID);
                }

                String sequenceID = retrieveSequenceID(correlationValue);
                orgMessageCtx.getOptions().setProperty(
                    SandeshaClientConstants.SEQUENCE_KEY, sequenceID);

                if (lastMessage) {
                    orgMessageCtx.getOptions().setProperty(
                        SandeshaClientConstants.LAST_MESSAGE, "true");
                    sequenceMap.remove(correlationValue);
                }

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Correlation value : " + correlationValue +
                            " last message = " + lastMessage + " using sequence : " + sequenceID +
                            (offeredSeqID != null ? " offering sequence : " + offeredSeqID : ""));
                }
            }
        }

        synLog.traceOrDebug("End : RMSequence mediator");
        return true;
    }

    private String retrieveSequenceID(String correlationValue) {
        String sequenceID;
        if (!sequenceMap.containsKey(correlationValue)) {
            sequenceID = UIDGenerator.generateURNString();
            if (log.isDebugEnabled()) {
                log.debug("setting sequenceID " + sequenceID + " for correlation " + correlationValue);
            }
            Entry sequenceEntry = new Entry();
            sequenceEntry.setValue(sequenceID);
            sequenceEntry.setExpiryTime(System.currentTimeMillis() + SEQUENCE_EXPIRY_TIME);
            sequenceMap.put(correlationValue, sequenceEntry);
        } else {
            sequenceID = (String) ((Entry) sequenceMap.get(correlationValue)).getValue();
            if (log.isDebugEnabled()) {
                log.debug("got sequenceID " + sequenceID + " for correlation " + correlationValue);
            }
        }
        return sequenceID;
    }

    private String getCorrelationValue(MessageContext smc) {
        try {
            OMElement node = (OMElement) getCorrelation().selectSingleNode(smc);
            if (node != null) {
                return node.getText();
            } else {
                handleException("XPath expression : " + getCorrelation() +
                        " did not return any node", smc);
            }

        } catch (JaxenException e) {
            handleException("Error evaluating XPath expression to determine correlation : " +
                    getCorrelation(), e, smc);
        }
        return null; // never called
    }

    private String getVersionValue() {
        if (XMLConfigConstants.SEQUENCE_VERSION_1_1.equals(getVersion())) {
            return WSRM_SpecVersion_1_1;
        } else {
            return WSRM_SpecVersion_1_0;
        }
    }

    private boolean isLastMessage(MessageContext smc) {
        if (getLastMessage() == null) {
            return false;
        } else {
            try {
                return getLastMessage().booleanValueOf(smc);
            } catch (JaxenException e) {
                handleException("Error evaluating XPath expression to determine if last message : " +
                    getLastMessage(), e, smc);
            }
            return false;
        }
    }

    private synchronized void cleanupSequenceMap() {
        for (String key : sequenceMap.keySet()) {
            Entry sequenceEntry = sequenceMap.get(key);
            if (sequenceEntry.isExpired()) {
                sequenceMap.remove(key);
            }
        }
    }

    public boolean isSingle() {
        return getSingle() != null && getSingle();
    }

    public SynapseXPath getCorrelation() {
        return correlation;
    }

    public void setCorrelation(SynapseXPath correlation) {
        this.correlation = correlation;
    }

    public SynapseXPath getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(SynapseXPath lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean getSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
