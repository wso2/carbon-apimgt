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

package org.apache.synapse.mediators.eip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.axis2.addressing.EndpointReference;

/**
 * A bean class that holds the target (i.e. sequence or endpoint) information for a message
 * as used by common EIP mediators
 */
public class Target {
    private static final Log log = LogFactory.getLog(Target.class);
    
    /** An optional To address to be set on the message when handing over to the target */
    private String toAddress = null;

    /** An optional Action to be set on the message when handing over to the target */
    private String soapAction = null;

    /** The in-lined target sequence definition */
    private SequenceMediator sequence = null;

    /** The target sequence reference key */
    private String sequenceRef = null;

    /** The in-lined target endpoint definition */
    private Endpoint endpoint = null;

    /** The target endpoint reference key */
    private String endpointRef = null;

    /** if true the mediation will happen in a different thread than the original
     * thread invoked the mediate method*/
    private boolean asynchronous = true;

    /**
     * process the message through this target (may be to mediate
     * using the target sequence, send message to the target endpoint or both)
     *
     * @param synCtx - MessageContext to be mediated
     * @return <code>false</code> if the target is mediated as synchronous and the sequence
     * mediation returns <code>false</code>, <code>true</code> otherwise
     */
    public boolean mediate(MessageContext synCtx) {

        boolean returnValue = true;

        if (log.isDebugEnabled()) {
            log.debug("Target mediation : START");
        }

        if (soapAction != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the SOAPAction as : " + soapAction);
            }
            synCtx.setSoapAction(soapAction);
        }

        if (toAddress != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the To header as : " + toAddress);
            }
            if (synCtx.getTo() != null) {
                synCtx.getTo().setAddress(toAddress);
            } else {
                synCtx.setTo(new EndpointReference(toAddress));
            }
        }

        // since we are injecting the new messages asynchronously, we cannot process a message
        // through a sequence and then again with an endpoint
        if (sequence != null) {
            if (asynchronous) {
                if (log.isDebugEnabled()) {
                    log.debug("Asynchronously mediating using the in-lined anonymous sequence");
                }
                synCtx.getEnvironment().injectAsync(synCtx, sequence);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Synchronously mediating using the in-lined anonymous sequence");
                }
                returnValue = sequence.mediate(synCtx);
            }
        } else if (sequenceRef != null) {
            SequenceMediator refSequence = (SequenceMediator) synCtx.getSequence(sequenceRef);

            // if target directs the message to a defined sequence, ReliantContState added by
            // Clone/Iterate mediator is no longer needed as defined sequence can be directly
            // referred from a SeqContinuationState
            ContinuationStackManager.removeReliantContinuationState(synCtx);

            if (refSequence != null) {
                if (asynchronous) {
                    if (log.isDebugEnabled()) {
                        log.debug("Asynchronously mediating using the sequence " +
                                "named : " + sequenceRef);
                    }
                    synCtx.getEnvironment().injectAsync(synCtx, refSequence);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Synchronously mediating using the sequence " +
                                "named : " + sequenceRef);
                    }
                    returnValue = refSequence.mediate(synCtx);
                }
            } else {
                handleException("Couldn't find the sequence named : " + sequenceRef);
            }
        } else if (endpoint != null) {
            if (log.isDebugEnabled()) {
                log.debug("Sending using the in-lined anonymous endpoint");
            }
            ContinuationStackManager.removeReliantContinuationState(synCtx);
            endpoint.send(synCtx);
        } else if (endpointRef != null) {
            ContinuationStackManager.removeReliantContinuationState(synCtx);
            Endpoint epr = synCtx.getConfiguration().getEndpoint(endpointRef);
            if (epr != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending using the endpoint named : " + endpointRef);
                }
				if (!epr.isInitialized()) {
					epr.init(synCtx.getEnvironment()); // initializing registry
													   // base endpoint configuration
				}
				epr.send(synCtx);
                //epr.destroy();
            } else {
                handleException("Couldn't find the endpoint named : " + endpointRef);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Target mediation : END");
        }

        return returnValue;
    }

    private void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //                        Getters and Setters                                        //
    ///////////////////////////////////////////////////////////////////////////////////////

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public SequenceMediator getSequence() {
        return sequence;
    }

    public void setSequence(SequenceMediator sequence) {
        this.sequence = sequence;
    }

    public String getSequenceRef() {
        return sequenceRef;
    }

    public void setSequenceRef(String sequenceRef) {
        this.sequenceRef = sequenceRef;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpointRef() {
        return endpointRef;
    }

    public void setEndpointRef(String endpointRef) {
        this.endpointRef = endpointRef;
    }

    public void setAsynchronous(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }
}