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

import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.MediatorWorker;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.concurrent.RejectedExecutionException;

/**
 * This mediator execute a given sequence with a given priority.
 *
 * It accepts the priority as and argument. The executor used for executing this
 * sequence should support this priority. If it doesn't support this priority it
 * executor can throw exceptions.
 */
public class EnqueueMediator extends AbstractMediator {
    private String executorName = null;

    private int priority = 0;

    private String sequenceName = null;

    public boolean mediate(MessageContext synCtx) {
        SynapseLog log = getLog(synCtx);
        if (log.isTraceOrDebugEnabled()) {
            log.traceOrDebug("Start: enqueue mediator");
        }

        assert executorName != null : "executor name shouldn't be null";

        PriorityExecutor executor = synCtx.getConfiguration().
                getPriorityExecutors().get(executorName);
        if (executor == null) {
            log.auditWarn("Cannot find executor " + executorName + ". Using existing thread for mediation");
            Mediator m = synCtx.getSequence(sequenceName);
            if (m != null && m instanceof SequenceMediator) {
                return m.mediate(synCtx);
            } else {
                handleException("Sequence cannot be found : " + sequenceName, synCtx);
                return false;
            }
        }


        Mediator m = synCtx.getSequence(sequenceName);
        if (m != null && m instanceof SequenceMediator) {
            MediatorWorker worker = new MediatorWorker(m, synCtx);
            try {
                // execute with the given priority
                executor.execute(worker, priority);
            } catch (RejectedExecutionException ex) {
                //if RejectedExecutionException, jump to fault handler
                handleException("Unable to process message in priority executor " + executorName + " with priority " +
                        priority +". Thread pool exhausted.", synCtx);
            }


            // with the nio transport, this causes the listener not to write a 202
            // Accepted response, as this implies that Synapse does not yet know if
            // a 202 or 200 response would be written back.
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().getOperationContext().setProperty(
                    org.apache.axis2.Constants.RESPONSE_WRITTEN, "SKIP");

            if (log.isTraceOrDebugEnabled()) {
                log.traceOrDebug("End: enqueue mediator");
            }

            return true;
        } else {
            handleException("Sequence cannot be found : " + sequenceName, synCtx);
            return false;
        }               
    }

    public String getExecutorName() {
        return executorName;
    }

    public int getPriority() {
        return priority;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean isContentAware() {
        return false;
    }
}
