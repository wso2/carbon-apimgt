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

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base class for all List mediators
 *
 * @see ListMediator
 */
public abstract class AbstractListMediator extends AbstractMediator
        implements ListMediator {

    /** the list of child mediators held. These are executed sequentially */
    protected final List<Mediator> mediators = new ArrayList<Mediator>();

    private boolean contentAware = false;

    public boolean mediate(MessageContext synCtx) {
        return  mediate(synCtx,0);
    }

    public boolean mediate(MessageContext synCtx, int mediatorPosition) {

        boolean returnVal = true;
        int parentsEffectiveTraceState = synCtx.getTracingState();
        // if I have been explicitly asked to enable or disable tracing, set it to the message
        // to pass it on; else, do nothing -> i.e. let the parents state flow
        setEffectiveTraceState(synCtx);
        int myEffectiveTraceState = synCtx.getTracingState();

        try {
            SynapseLog synLog = getLog(synCtx);
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Sequence <" + getType() + "> :: mediate()");
                synLog.traceOrDebug("Mediation started from mediator position : " + mediatorPosition);
            }

            if (contentAware) {
                try {
                    RelayUtils.buildMessage(((Axis2MessageContext) synCtx).getAxis2MessageContext(),false);
                } catch (Exception e) {
                    handleException("Error while building message", e, synCtx);
                }
            }

            for (int i = mediatorPosition; i < mediators.size(); i++) {
                // ensure correct trace state after each invocation of a mediator
                synCtx.setTracingState(myEffectiveTraceState);
                if (!mediators.get(i).mediate(synCtx)) {
                    returnVal = false;
                    break;
                }
            }
        } finally {
            synCtx.setTracingState(parentsEffectiveTraceState);
        }

        return returnVal;
    }

    public List<Mediator> getList() {
        return mediators;
    }

    public boolean addChild(Mediator m) {
        return mediators.add(m);
    }

    public boolean addAll(List<Mediator> c) {
        return mediators.addAll(c);
    }

    public Mediator getChild(int pos) {
        return mediators.get(pos);
    }

    public boolean removeChild(Mediator m) {
        return mediators.remove(m);
    }

    public Mediator removeChild(int pos) {
        return mediators.remove(pos);
    }

    /**
     * Initialize child mediators recursively
     * @param se synapse environment
     */
    public void init(SynapseEnvironment se) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing child mediators of mediator : " + getType());
        }

        for (int i = 0; i < mediators.size(); i++) {
            Mediator mediator = mediators.get(i);
            mediator.setMediatorPosition(i);

            if (mediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) mediator).init(se);
            }

            if (mediator.isContentAware()) {
                contentAware = true;
            }
        }
    }

    /**
     * Destroy child mediators recursively
     */
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Destroying child mediators of mediator : " + getType());
        }

        for (Mediator mediator : mediators) {
            if (mediator instanceof ManagedLifecycle) {
                ((ManagedLifecycle) mediator).destroy();
            }
        }
    }

    @Override
    public boolean isContentAware() {
        return contentAware;
    }

}
