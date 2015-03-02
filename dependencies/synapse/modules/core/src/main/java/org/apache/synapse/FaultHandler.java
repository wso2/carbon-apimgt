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

package org.apache.synapse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.statistics.StatisticsReporter;

import java.util.Stack;
import java.io.StringWriter;
import java.io.Writer;
import java.io.PrintWriter;

/**
 * This is an abstract class that handles an unexpected error during Synapse mediation, but looking
 * at the stack of registered FaultHanders and invoking on them as appropriate. Sequences and
 * Endpoints would be Synapse entities that handles faults. If such an entity is unable to handle
 * an error condition, then a SynapseException should be thrown, which triggers this fault
 * handling logic.
 */
public abstract class FaultHandler {

    private static final Log log = LogFactory.getLog(FaultHandler.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    public void handleFault(MessageContext synCtx) {

        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        if (traceOrDebugOn) {
            traceOrDebugWarn(traceOn, "FaultHandler executing impl: " + this.getClass().getName());
        }

        try {
            synCtx.getServiceLog().info("FaultHandler executing impl: " + this.getClass().getName());
            onFault(synCtx);

        } catch (SynapseException e) {

            Stack faultStack = synCtx.getFaultStack();
            if (faultStack != null && !faultStack.isEmpty()) {
                ((FaultHandler) faultStack.pop()).handleFault(synCtx);
            }
        }
    }

    /**
     * Extract and set ERROR_MESSAGE and ERROR_DETAIL to the message context from the Exception
     * @param synCtx the message context
     * @param e the exception encountered
     */
    public void handleFault(MessageContext synCtx, Exception e) {

        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        if (e != null && synCtx.getProperty(SynapseConstants.ERROR_CODE) == null) {
            synCtx.setProperty(SynapseConstants.ERROR_CODE, SynapseConstants.DEFAULT_ERROR);
            // use only the first line as the message for multiline exception messages (Axis2 has these)
            synCtx.setProperty(SynapseConstants.ERROR_MESSAGE, e.getMessage().split("\n")[0]);
            synCtx.setProperty(SynapseConstants.ERROR_DETAIL, getStackTrace(e));
            synCtx.setProperty(SynapseConstants.ERROR_EXCEPTION, e);
        }

        if (traceOrDebugOn) {
            traceOrDebugWarn(traceOn, "ERROR_CODE : " +
                synCtx.getProperty(SynapseConstants.ERROR_CODE));
            traceOrDebugWarn(traceOn, "ERROR_MESSAGE : " +
                synCtx.getProperty(SynapseConstants.ERROR_MESSAGE));
            traceOrDebugWarn(traceOn, "ERROR_DETAIL : " +
                synCtx.getProperty(SynapseConstants.ERROR_DETAIL));
            traceOrDebugWarn(traceOn, "ERROR_EXCEPTION : " +
                synCtx.getProperty(SynapseConstants.ERROR_EXCEPTION));
        }

        synCtx.getServiceLog().warn("ERROR_CODE : " +
            synCtx.getProperty(SynapseConstants.ERROR_CODE) + " ERROR_MESSAGE : " + 
            synCtx.getProperty(SynapseConstants.ERROR_MESSAGE));

        try {
            if (traceOrDebugOn) {
                traceOrDebugWarn(traceOn, "FaultHandler : " + this);
            }
            onFault(synCtx);

        } catch (SynapseException se) {

            Stack faultStack = synCtx.getFaultStack();
            if (faultStack != null && !faultStack.isEmpty()) {
                ((FaultHandler) faultStack.pop()).handleFault(synCtx, se);
            } else{
            	throw new RuntimeException(se);
            }
        }
    }

    /**
     * This will be executed to handle any Exceptions occurred within the Synapse environment.
     * @param synCtx SynapseMessageContext of which the fault occured message comprises
     * @throws SynapseException in case there is a failure in the fault execution
     */
    public abstract void onFault(MessageContext synCtx);

    /**
     * Get the stack trace into a String
     * @param aThrowable
     * @return the stack trace as a string
     */
    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    private void traceOrDebugWarn(boolean traceOn, String msg) {
        if (traceOn) {
            trace.warn(msg);
        }
        log.warn(msg);
    }

}
