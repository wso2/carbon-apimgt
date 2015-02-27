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

package org.apache.synapse.mediators.filters;

import org.apache.synapse.ContinuationState;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.continuation.ReliantContinuationState;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.xml.SwitchCase;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.util.ArrayList;
import java.util.List;

/**
 * The switch mediator implements the functionality of the "switch" construct. It first
 * evaluates the given XPath expression into a String value, and performs a match against
 * the given list of cases. This is actually a list of sequences, and depending on the
 * selected case, the selected sequence gets executed.
 */
public class SwitchMediator extends AbstractMediator implements ManagedLifecycle,
                                                                FlowContinuableMediator {

    /** The Path expression specifying the source element to apply the switch case expressions against   */
    private SynapsePath source = null;
    /** The list of switch cases    */
    private final List<SwitchCase> cases = new ArrayList<SwitchCase>();
    /** The default switch case, if any */
    private SwitchCase defaultCase = null;

    public void init(SynapseEnvironment se) {
        for (ManagedLifecycle swCase : cases) {
            swCase.init(se);
        }
        if (defaultCase != null) {
            defaultCase.init(se);
        }
    }

    public void destroy() {
        for (ManagedLifecycle swCase : cases) {
            swCase.destroy();
        }
        if (defaultCase != null) {
            defaultCase.destroy();
        }
    }

    /**
     * Iterate over switch cases and find match and execute selected sequence
     *
     * @param synCtx current context
     * @return as per standard semantics
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Switch mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        int parentsEffectiveTraceState = synCtx.getTracingState();
        // if I have been explicitly asked to enable or disable tracing, set it to the message
        // to pass it on; else, do nothing -> i.e. let the parents state flow
        setEffectiveTraceState(synCtx);

        String sourceText = source.stringValueOf(synCtx);
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("XPath : " + source + " evaluates to : " + sourceText);
        }

        try {
            if ((sourceText == null || cases.isEmpty()) && defaultCase != null) {
                synLog.traceOrDebug("Source XPath evaluated to : null or no switch " +
                        "cases found. Executing the default case");

                ContinuationStackManager.
                        addReliantContinuationState(synCtx, 0, getMediatorPosition() );
                boolean result = defaultCase.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }
                return result;

            } else {
                for (int i = 0; i < cases.size(); i++) {
                    SwitchCase swCase = cases.get(i);
                    if (swCase != null) {
                        if (swCase.matches(sourceText)) {
                            if (synLog.isTraceOrDebugEnabled()) {
                                synLog.traceOrDebug("Matching case found : " + swCase.getRegex());
                            }
                            ContinuationStackManager.
                                    addReliantContinuationState(synCtx, i + 1, getMediatorPosition());
                            boolean result = swCase.mediate(synCtx);
                            if (result) {
                                ContinuationStackManager.removeReliantContinuationState(synCtx);
                            }
                            return result;
                        }
                    }
                }

                if (defaultCase != null) {
                    // if any of the switch cases did not match
                    synLog.traceOrDebug("None of the switch cases matched - executing default");
                    ContinuationStackManager.
                            addReliantContinuationState(synCtx, 0, getMediatorPosition());
                    boolean result = defaultCase.mediate(synCtx);
                    if (result) {
                        ContinuationStackManager.removeReliantContinuationState(synCtx);
                    }
                    return result;
                } else {
                    synLog.traceOrDebug("None of the switch cases matched - no default case");
                }
            }

        } finally {
            synCtx.setTracingState(parentsEffectiveTraceState);
        }

        synLog.traceOrDebug("End : Switch mediator");
        return true;
    }

    public boolean mediate(MessageContext synCtx,
                           ContinuationState continuationState) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Switch mediator : Mediating from ContinuationState");
        }

        boolean result;
        int subBranch = ((ReliantContinuationState) continuationState).getSubBranch();
        if (subBranch == 0) {
            if (!continuationState.hasChild()) {
                result = defaultCase.getCaseMediator().
                        mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator =
                        (FlowContinuableMediator) defaultCase.getCaseMediator().
                                getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        } else {
            if (!continuationState.hasChild()) {
                result = cases.get(subBranch - 1).getCaseMediator().
                        mediate(synCtx, continuationState.getPosition() + 1);
            } else {
                FlowContinuableMediator mediator =
                        (FlowContinuableMediator) cases.get(subBranch - 1).getCaseMediator().
                                getChild(continuationState.getPosition());
                result = mediator.mediate(synCtx, continuationState.getChildContState());
            }
        }
        return result;
    }

    /**
     * Adds the given mediator (Should be a SwitchCaseMediator) to the list of cases
     * of this Switch mediator
     *
     * @param m the SwitchCaseMediator instance to be added
     */
    public void addCase(SwitchCase m) {
        cases.add(m);
    }

    /**
     * Get the list of cases
     *
     * @return the cases list
     */
    public List<SwitchCase> getCases() {
        return cases;
    }

    /**
     * Return the source Path expression set
     *
     * @return thje source Path expression
     */
    public SynapsePath getSource() {
        return source;
    }

    /**
     * Sets the source Path expression
     *
     * @param source the Path expression to be used as the source
     */
    public void setSource(SynapsePath source) {
        this.source = source;
    }

    /**
     * Get default case
     *
     * @return the default case
     */
    public SwitchCase getDefaultCase() {
        return defaultCase;
    }

    /**
     * setting the default case ...which contains mediators to invoke when no case condition satisfy
     * @param defaultCase A SwitchCase instance representing default case
     */
    public void setDefaultCase(SwitchCase defaultCase) {
        this.defaultCase = defaultCase;
    }
    
    @Override
    public boolean isContentAware() {
        if (source != null) {
            return source.isContentAware();
        } 
        return false;
    }

}
