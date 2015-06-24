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

package org.apache.synapse.mediators.eip.aggregator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.eip.EIPConstants;
import org.apache.synapse.mediators.eip.EIPUtils;
import org.apache.synapse.util.MessageHelper;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate a number of messages that are determined to be for a particular group, and combine
 * them to form a single message which is then processed through the 'onComplete' sequence. Thus
 * an aggregator acts like a filter, and may look at a correlation XPath expression to select
 * messages for aggregation - or look at messageSequence number properties for aggregation or
 * let any other (i.e. non aggregatable) messages flow through
 * An instance of this mediator will register with a Timer to be notified after a specified timeout,
 * so that aggregations that never would complete could be timed out and cleared from memory and
 * any fault conditions handled
 */
public class AggregateMediator extends AbstractMediator implements ManagedLifecycle,
                                                                   FlowContinuableMediator {

    private static final Log log = LogFactory.getLog(AggregateMediator.class);

    /** The duration as a number of milliseconds for this aggregation to complete */
    private long completionTimeoutMillis = 0;
    /** The maximum number of messages required to complete aggregation */
    private Value minMessagesToComplete;
    /** The minimum number of messages required to complete aggregation */
    private Value maxMessagesToComplete;

    /**
     * XPath that specifies a correlation expression that can be used to combine messages. An
     * example maybe //department@id="11"
     */
    private SynapseXPath correlateExpression = null;
    /**
     * An XPath expression that may specify a selected element to be aggregated from a group of
     * messages to create the aggregated message
     * e.g. //getQuote/return would pick up and aggregate the //getQuote/return elements from a
     * bunch of matching messages into one aggregated message
     */
    private SynapseXPath aggregationExpression = null;

    /** This holds the reference sequence name of the */
    private String onCompleteSequenceRef = null;
    /** Inline sequence definition holder that holds the onComplete sequence */
    private SequenceMediator onCompleteSequence = null;

    /** The active aggregates currently being processd */
    private Map<String, Aggregate> activeAggregates =
        Collections.synchronizedMap(new HashMap<String, Aggregate>());

    private String id = null;

    /** Property which contains the Enclosing element of the aggregated message */
    private String enclosingElementPropertyName = null;

    /** Lock object to provide the synchronized access to the activeAggregates on checking */
    private final Object lock = new Object();

    /** Reference to the synapse environment */
    private SynapseEnvironment synapseEnv;

    public AggregateMediator() {
        try {
            aggregationExpression = new SynapseXPath("/s11:Envelope/s11:Body/child::*[position()=1] | " +
                "/s12:Envelope/s12:Body/child::*[position()=1]");
            aggregationExpression.addNamespace("s11", SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            aggregationExpression.addNamespace("s12", SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        } catch (JaxenException e) {
            if (log.isDebugEnabled()) {
                handleException("Unable to set the default " +
                    "aggregationExpression for the aggregation", e, null);
            }
        }
    }

    public void init(SynapseEnvironment se) {
        synapseEnv = se;
        if (onCompleteSequence != null) {
            onCompleteSequence.init(se);
        } else if (onCompleteSequenceRef != null) {
            SequenceMediator referredOnCompleteSeq =
                    (SequenceMediator) se.getSynapseConfiguration().
                            getSequence(onCompleteSequenceRef);

            if (referredOnCompleteSeq == null || referredOnCompleteSeq.isDynamic()) {
                se.addUnavailableArtifactRef(onCompleteSequenceRef);
            }
        }
    }

    public void destroy() {
        if (onCompleteSequence != null) {
            onCompleteSequence.destroy();
        } else if (onCompleteSequenceRef != null) {
            SequenceMediator referredOnCompleteSeq =
                    (SequenceMediator) synapseEnv.getSynapseConfiguration().
                            getSequence(onCompleteSequenceRef);

            if (referredOnCompleteSeq == null || referredOnCompleteSeq.isDynamic()) {
                synapseEnv.removeUnavailableArtifactRef(onCompleteSequenceRef);
            }
        }
    }

    /**
     * Aggregate messages flowing through this mediator according to the correlation criteria
     * and the aggregation algorithm specified to it
     *
     * @param synCtx - MessageContext to be mediated and aggregated
     * @return boolean true if the complete condition for the particular aggregate is validated
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Aggregate mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        try {
            Aggregate aggregate = null;
            String correlationIdName = (id != null ? EIPConstants.AGGREGATE_CORRELATION + "." + id :
                    EIPConstants.AGGREGATE_CORRELATION);
            // if a correlateExpression is provided and there is a coresponding
            // element in the current message prepare to correlate the messages on that
            Object result = null;
            if (correlateExpression != null) {
                result = correlateExpression.evaluate(synCtx);
                if (result instanceof List) {
                    if (((List) result).isEmpty()) {
                        handleException("Failed to evaluate correlate expression: " + correlateExpression.toString(), synCtx);
                    }
                }
            }
            if (result != null) {

                while (aggregate == null) {

                    synchronized (lock) {

                        if (activeAggregates.containsKey(correlateExpression.toString())) {

                            aggregate = activeAggregates.get(correlateExpression.toString());
                            if (aggregate != null) {
                                if (!aggregate.getLock()) {
                                    aggregate = null;
                                }
                            }

                        } else {

                            if (synLog.isTraceOrDebugEnabled()) {
                                synLog.traceOrDebug("Creating new Aggregator - " +
                                        (completionTimeoutMillis > 0 ? "expires in : "
                                                + (completionTimeoutMillis / 1000) + "secs" :
                                                "without expiry time"));
                            }

                            Double minMsg = Double.parseDouble(minMessagesToComplete.evaluateValue(synCtx));
                            Double maxMsg = Double.parseDouble(maxMessagesToComplete.evaluateValue(synCtx));
                    
                            aggregate = new Aggregate(
                                    synCtx.getEnvironment(),
                                    correlateExpression.toString(),
                                    completionTimeoutMillis,
                                    minMsg.intValue(),
                                    maxMsg.intValue(), this);

                            if (completionTimeoutMillis > 0) {
                                synCtx.getConfiguration().getSynapseTimer().
                                        schedule(aggregate, completionTimeoutMillis);
                            }
                            aggregate.getLock();
                            activeAggregates.put(correlateExpression.toString(), aggregate);
                        }
                    }
                }

            } else if (synCtx.getProperty(correlationIdName) != null) {
                // if the correlattion cannot be found using the correlateExpression then
                // try the default which is through the AGGREGATE_CORRELATION message property
                // which is the unique original message id of a split or iterate operation and
                // which thus can be used to uniquely group messages into aggregates

                Object o = synCtx.getProperty(correlationIdName);
                String correlation;

                if (o != null && o instanceof String) {
                    correlation = (String) o;
                    while (aggregate == null) {
                        synchronized (lock) {
                            if (activeAggregates.containsKey(correlation)) {
                                aggregate = activeAggregates.get(correlation);
                                if (aggregate != null) {
                                    if (!aggregate.getLock()) {
                                        aggregate = null;
                                    }
                                } else {
                                    break;
                                }
                            } else {
                                if (synLog.isTraceOrDebugEnabled()) {
                                    synLog.traceOrDebug("Creating new Aggregator - " +
                                            (completionTimeoutMillis > 0 ? "expires in : "
                                                    + (completionTimeoutMillis / 1000) + "secs" :
                                                    "without expiry time"));
                                }

                                Double minMsg = -1.0;
                                if (minMessagesToComplete != null) {
                                    minMsg = Double.parseDouble(minMessagesToComplete.evaluateValue(synCtx));
                                }
                                Double maxMsg = -1.0;
                                if (maxMessagesToComplete != null) {
                                    maxMsg = Double.parseDouble(maxMessagesToComplete.evaluateValue(synCtx));
                                }

                                aggregate = new Aggregate(
                                        synCtx.getEnvironment(),
                                        correlation,
                                        completionTimeoutMillis,
                                        minMsg.intValue(),
                                        maxMsg.intValue(), this);

                                if (completionTimeoutMillis > 0) {
                                    synchronized(aggregate) {
                                        if (!aggregate.isCompleted()) {
                                            synCtx.getConfiguration().getSynapseTimer().
                                                schedule(aggregate, completionTimeoutMillis);
                                        }
                                    }
                                }
                                aggregate.getLock();
                                activeAggregates.put(correlation, aggregate);
                            }
                        }
                    }
                    
                } else {
                    synLog.traceOrDebug("Unable to find aggrgation correlation property");
                    return true;
                }
            } else {
                synLog.traceOrDebug("Unable to find aggrgation correlation XPath or property");
                return true;
            }

            // if there is an aggregate continue on aggregation
            if (aggregate != null) {
            	//this is a temporary fix           	
                synCtx.getEnvelope().build();
                boolean collected = aggregate.addMessage(synCtx);
                if (synLog.isTraceOrDebugEnabled()) {
                    if (collected) {
                        synLog.traceOrDebug("Collected a message during aggregation");
                        if (synLog.isTraceTraceEnabled()) {
                            synLog.traceTrace("Collected message : " + synCtx);
                        }
                    }
                }
                
                // check the completeness of the aggregate and if completed aggregate the messages
                // if not completed return false and block the message sequence till it completes

                if (aggregate.isComplete(synLog)) {
                    synLog.traceOrDebug("Aggregation completed - invoking onComplete");
                    boolean onCompleteSeqResult = completeAggregate(aggregate);
                    
                    synLog.traceOrDebug("End : Aggregate mediator");
                    return onCompleteSeqResult;
                } else {
                    aggregate.releaseLock();
                }

            } else {
                // if the aggregation correlation cannot be found then continue the message on the
                // normal path by returning true

                synLog.traceOrDebug("Unable to find an aggregate for this message - skip");
                return true;
            }

        } catch (JaxenException e) {
            handleException("Unable to execute the XPATH over the message", e, synCtx);
        }

        synLog.traceOrDebug("End : Aggregate mediator");

        // When Aggregation is not completed return false to hold the flow
        return false;
    }

    public boolean mediate(MessageContext synCtx,
                           ContinuationState contState) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Aggregate mediator : Mediating from ContinuationState");
        }

        boolean result;

        if (!contState.hasChild()) {
            result = getOnCompleteSequence().mediate(synCtx, contState.getPosition() + 1);
        } else {
            FlowContinuableMediator mediator = (FlowContinuableMediator) getOnCompleteSequence().
                    getChild(contState.getPosition());
            result = mediator.mediate(synCtx, contState.getChildContState());
        }
        return result;
    }

    /**
     * Invoked by the Aggregate objects that are timed out, to signal timeout/completion of
     * itself
     * @param aggregate the timed out Aggregate that holds collected messages and properties
     */
    public boolean completeAggregate(Aggregate aggregate) {

        boolean markedCompletedNow = false;
        boolean wasComplete = aggregate.isCompleted();
        if (wasComplete) {
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Aggregation completed or timed out");
        }

        // cancel the timer
        synchronized(this) {
            if (!aggregate.isCompleted()) {
                aggregate.cancel();
                aggregate.setCompleted(true);
                markedCompletedNow = true;
            }
        }

        if (!markedCompletedNow) {
            return false;
        }
        
        MessageContext newSynCtx = getAggregatedMessage(aggregate);

        if (newSynCtx == null) {
            log.warn("An aggregation of messages timed out with no aggregated messages", null);
            return false;
        } else {
            // Get the aggregated message to the next mediator placed after the aggregate mediator
            // in the sequence
            if (newSynCtx.isContinuationEnabled()) {
                try {
                    aggregate.getLastMessage().setEnvelope(
                            MessageHelper.cloneSOAPEnvelope(newSynCtx.getEnvelope()));
                } catch (AxisFault axisFault) {
                    log.warn("Error occurred while assigning aggregated message" +
                             " back to the last received message context");
                }
            }
        }

        activeAggregates.remove(aggregate.getCorrelation());

        if ((correlateExpression != null &&
            !correlateExpression.toString().equals(aggregate.getCorrelation())) ||
            correlateExpression == null) {

            if (onCompleteSequence != null) {

                ContinuationStackManager.
                        addReliantContinuationState(newSynCtx, 0, getMediatorPosition());
                boolean result = onCompleteSequence.mediate(newSynCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(newSynCtx);
                }
                return result;

            } else if (onCompleteSequenceRef != null
                && newSynCtx.getSequence(onCompleteSequenceRef) != null) {

                ContinuationStackManager.updateSeqContinuationState(newSynCtx, getMediatorPosition());
                return newSynCtx.getSequence(onCompleteSequenceRef).mediate(newSynCtx);

            } else {
                handleException("Unable to find the sequence for the mediation " +
                    "of the aggregated message", newSynCtx);
            }
        }
        return false;
    }

    /**
     * Get the aggregated message from the specified Aggregate instance
     *
     * @param aggregate the Aggregate object that holds collected messages and properties of the
     * aggregation
     * @return the aggregated message context
     */
    private MessageContext getAggregatedMessage(Aggregate aggregate) {

        MessageContext newCtx = null;

        for (MessageContext synCtx : aggregate.getMessages()) {
            
            if (newCtx == null) {
                try {
                    newCtx = MessageHelper.cloneMessageContext(synCtx);
                } catch (AxisFault axisFault) {
                    handleException("Error creating a copy of the message", axisFault, synCtx);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Generating Aggregated message from : " + newCtx.getEnvelope());
                }

            } else {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Merging message : " + synCtx.getEnvelope() + " using XPath : " +
                                aggregationExpression);
                    }

                    EIPUtils.enrichEnvelope(
                            newCtx.getEnvelope(), synCtx.getEnvelope(), synCtx, aggregationExpression);

                    if (log.isDebugEnabled()) {
                        log.debug("Merged result : " + newCtx.getEnvelope());
                    }

                } catch (JaxenException e) {
                    handleException("Error merging aggregation results using XPath : " +
                            aggregationExpression.toString(), e, synCtx);
                } catch (SynapseException e) {
                    handleException("Error evaluating expression: " + aggregationExpression.toString() , e, synCtx);
                }
            }
        }

        // Enclose with a parent element if EnclosingElement is defined
        if (enclosingElementPropertyName != null) {

            if (log.isDebugEnabled()) {
                log.debug("Enclosing the aggregated message with enclosing element: " +
                          enclosingElementPropertyName);
            }

            Object enclosingElementProperty = newCtx.getProperty(enclosingElementPropertyName);

            if (enclosingElementProperty != null) {
                if (enclosingElementProperty instanceof OMElement) {
                    OMElement enclosingElement =
                            ((OMElement) enclosingElementProperty).cloneOMElement();
                    EIPUtils.encloseWithElement(newCtx.getEnvelope(), enclosingElement);

                    return newCtx;

                } else {
                    handleException("Enclosing Element defined in the property: " +
                                    enclosingElementPropertyName + " is not an OMElement ", newCtx);
                }
            } else {
                handleException("Enclosing Element property: " +
                                enclosingElementPropertyName + " not found ", newCtx);
            }
        }

        return newCtx;
    }

    public SynapseXPath getCorrelateExpression() {
        return correlateExpression;
    }

    public void setCorrelateExpression(SynapseXPath correlateExpression) {
        this.correlateExpression = correlateExpression;
    }

    public long getCompletionTimeoutMillis() {
        return completionTimeoutMillis;
    }

    public void setCompletionTimeoutMillis(long completionTimeoutMillis) {
        this.completionTimeoutMillis = completionTimeoutMillis;
    }

    public SynapseXPath getAggregationExpression() {
        return aggregationExpression;
    }

    public void setAggregationExpression(SynapseXPath aggregationExpression) {
        this.aggregationExpression = aggregationExpression;
    }

    public String getOnCompleteSequenceRef() {
        return onCompleteSequenceRef;
    }

    public void setOnCompleteSequenceRef(String onCompleteSequenceRef) {
        this.onCompleteSequenceRef = onCompleteSequenceRef;
    }

    public SequenceMediator getOnCompleteSequence() {
        return onCompleteSequence;
    }

    public void setOnCompleteSequence(SequenceMediator onCompleteSequence) {
        this.onCompleteSequence = onCompleteSequence;
    }

    public Map getActiveAggregates() {
        return activeAggregates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public Value getMinMessagesToComplete() {
    	return minMessagesToComplete;
    }

	public void setMinMessagesToComplete(Value minMessagesToComplete) {
    	this.minMessagesToComplete = minMessagesToComplete;
    }

	public Value getMaxMessagesToComplete() {
    	return maxMessagesToComplete;
    }

	public void setMaxMessagesToComplete(Value maxMessagesToComplete) {
    	this.maxMessagesToComplete = maxMessagesToComplete;
    }

    public String getEnclosingElementPropertyName() {
        return enclosingElementPropertyName;
    }

    public void setEnclosingElementPropertyName(String enclosingElementPropertyName) {
        this.enclosingElementPropertyName = enclosingElementPropertyName;
    }

}
