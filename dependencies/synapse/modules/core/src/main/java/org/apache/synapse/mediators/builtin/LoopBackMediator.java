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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * Loopback further processing/Mediation of the current message to outflow
 */
public class LoopBackMediator extends AbstractMediator {
    public boolean mediate(MessageContext synCtx) {
        SynapseLog synLog = getLog(synCtx);
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Loopback Mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }
        //If message flow is inflow this will be executed.
        if (!synCtx.isResponse()) {
            synCtx.setResponse(true);
            synCtx.setTo(null);
            synCtx.getEnvironment().injectMessage(synCtx);
        }
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : Loopback Mediator");
        }
        return false;
    }

    @Override
    public boolean isContentAware() {
        return false;
    }
}