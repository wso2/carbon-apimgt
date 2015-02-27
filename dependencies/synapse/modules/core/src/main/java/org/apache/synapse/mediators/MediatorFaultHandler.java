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

package org.apache.synapse.mediators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.mediators.base.SequenceMediator;

/**
 * This implements the FaultHandler interface as a mediator fault handler. That is the fault handler is
 * specified by a sequence and this handler implements the logic of handling the fault through the set
 * of mediators present in the sequence.
 *
 * @see org.apache.synapse.FaultHandler
 */
public class MediatorFaultHandler extends FaultHandler {

    private static final Log log = LogFactory.getLog(MediatorFaultHandler.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    /**
     * This holds the fault sequence for the mediator fault handler
     */
    private Mediator faultMediator = null;

    /**
     * Constructs the FaultHandler object for handling mediator faults
     *
     * @param faultMediator Mediator in which fault sequence is specified
     */
    public MediatorFaultHandler(Mediator faultMediator) {

        this.faultMediator = faultMediator;
    }

    /**
     * Implements the fault handling method for the mediators (basically sequences)
     *
     * @param synCtx Synapse Message Context of which mediation occurs
     * @throws SynapseException in case there is a failure in the fault execution
     * @see org.apache.synapse.FaultHandler#handleFault(org.apache.synapse.MessageContext)
     */
    public void onFault(MessageContext synCtx) throws SynapseException {

        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        String name = null;
        if (faultMediator instanceof SequenceMediator) {
            name = ((SequenceMediator) faultMediator).getName();
            ContinuationStackManager.clearStack(synCtx);
        }
        if (name == null) {
            name = faultMediator.getClass().getName();
        }

        if (traceOrDebugOn) {
            traceOrDebugWarn(traceOn, "Executing fault handler mediator : " + name);
        }

        synCtx.getServiceLog().warn("Executing fault sequence mediator : " + name);
        this.faultMediator.mediate(synCtx);
    }

    /**
     * Getter for the mediator describing the fault sequence
     *
     * @return Mediator specifying the fault sequence for mediator fault handler
     */
    public Mediator getFaultMediator() {
        return faultMediator;
    }

    /**
     * Setter of the mediator describing the fault sequence
     *
     * @param faultMediator Mediator specifying the fault sequence to be used by the handler
     */
    public void setFaultMediator(Mediator faultMediator) {
        this.faultMediator = faultMediator;
    }

    private void traceOrDebugWarn(boolean traceOn, String msg) {
        if (traceOn) {
            trace.warn(msg);
        }
        log.warn(msg);
    }

}
