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
import org.apache.synapse.*;
import org.apache.synapse.aspects.AspectConfigurable;
import org.apache.synapse.aspects.AspectConfiguration;

/**
 * This is the super class of all mediators, and defines common logging, tracing other aspects
 * for all mediators who extend from this.
 * elements of a mediator class.
 */
public abstract class AbstractMediator implements Mediator, AspectConfigurable {

    /** the standard log for mediators, will assign the logger for the actual subclass */
    protected Log log;
    /** The runtime trace log for mediators */
    protected static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    /**
     * State of tracing for this mediator
     */
    protected int traceState = SynapseConstants.TRACING_UNSET;

    private AspectConfiguration aspectConfiguration;

    private String description;

    private String shortDescription;

    private int mediatorPosition = 0;

    /**
     * A constructor that makes subclasses pick up the correct logger
     */
    protected AbstractMediator() {
        log = LogFactory.getLog(this.getClass());
    }

    /**
     * Returns the class name of the mediator
     * @return the class name of the mediator
     */
    public String getType() {
        String cls = getClass().getName();
        int p = cls.lastIndexOf(".");
        if (p == -1)
            return cls;
        else
            return cls.substring(p + 1);
    }

    /**
     * Returns the tracing state
     * @return the tracing state for this mediator (see SynapseConstants)
     */
    public int getTraceState() {
        return traceState;
    }

    /**
     * Set the tracing state variable
     * @param traceState the new tracing state for this mediator (see SynapseConstants)
     */
    public void setTraceState(int traceState) {
        this.traceState = traceState;
    }

    /**
     * Set the description of the mediator
     * @param description tobe set to the mediator
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gives the description of the mediator
     * @return description of the mediator
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the short description of the mediator
     * @param shortDescription to be set to the mediator
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Gives the short description of the mediator
     * @return short description of the mediator
     */
    public String getShortDescription() {
        return this.shortDescription;
    }

    /**
     * This method is used to save previous tracing state and set next the tracing
     * state for a child mediator
     *
     * @param synCtx current message
     */
    public void setEffectiveTraceState(MessageContext synCtx) {
        // if I have been explicitly asked to enable or disable tracing, use it and pass it on
        // else, do nothing -> i.e. let the parents state flow
        if (traceState != SynapseConstants.TRACING_UNSET) {
            synCtx.setTracingState(traceState);
        }
    }

    /**
     * Get a SynapseLog instance appropriate for the given context.
     * 
     * @param synCtx  the current message context
     * @return MediatorLog instance - an implementation of the SynapseLog
     */
    protected SynapseLog getLog(MessageContext synCtx) {
        return new MediatorLog(log, isTraceOn(synCtx), synCtx);
    }

    /**
     * Should this mediator perform tracing? True if its explicitly asked to
     * trace, or its parent has been asked to trace and it does not reject it
     * @param parentTraceState parents trace state
     * @return true if tracing should be performed
     */
    public boolean shouldTrace(int parentTraceState){
        return
            (traceState == SynapseConstants.TRACING_ON) ||
            (traceState == SynapseConstants.TRACING_UNSET &&
                parentTraceState == SynapseConstants.TRACING_ON);
    }

    /**
     * Should this mediator perform tracing? True if its explicitly asked to
     * trace, or its parent has been asked to trace and it does not reject it
     * 
     * @deprecated This method will be removed in a future version of Synapse.
     *             Please use the {@link SynapseLog} instance returned by
     *             {@link #getLog(MessageContext)} for all logging inside a
     *             mediator.
     * 
     * @param msgCtx the current message
     * @return true if tracing should be performed
     */
    @Deprecated
    protected boolean isTraceOn(MessageContext msgCtx) {
        return
            (traceState == SynapseConstants.TRACING_ON) ||
            (traceState == SynapseConstants.TRACING_UNSET &&
                msgCtx.getTracingState() == SynapseConstants.TRACING_ON);
    }

    /**
     * Is tracing or debug logging on?
     * 
     * @deprecated This method will be removed in a future version of Synapse.
     *             Please use the {@link SynapseLog} instance returned by
     *             {@link #getLog(MessageContext)} for all logging inside a
     *             mediator.
     * 
     * @param isTraceOn is tracing known to be on?
     * @return true, if either tracing or debug logging is on
     */
    @Deprecated
    protected boolean isTraceOrDebugOn(boolean isTraceOn) {
        return isTraceOn || log.isDebugEnabled();
    }

    /**
     * Perform Trace and Debug logging of a message @INFO (trace) and DEBUG (log)
     * 
     * @deprecated This method will be removed in a future version of Synapse.
     *             Please use the {@link SynapseLog} instance returned by
     *             {@link #getLog(MessageContext)} for all logging inside a
     *             mediator.
     * 
     * @param traceOn is runtime trace on for this message?
     * @param msg the message to log/trace
     */
    @Deprecated
    protected void traceOrDebug(boolean traceOn, String msg) {
        if (traceOn) {
            trace.info(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    /**
     * Perform Trace and Debug logging of a message @WARN
     * 
     * @deprecated This method will be removed in a future version of Synapse.
     *             Please use the {@link SynapseLog} instance returned by
     *             {@link #getLog(MessageContext)} for all logging inside a
     *             mediator.
     * 
     * @param traceOn is runtime trace on for this message?
     * @param msg the message to log/trace
     */
    @Deprecated
    protected void traceOrDebugWarn(boolean traceOn, String msg) {
        if (traceOn) {
            trace.warn(msg);
        }
        if (log.isDebugEnabled()) {
            log.warn(msg);
        }
    }

    /**
     * Perform an audit log message to all logs @ INFO. Writes to the general log, the service log
     * and the trace log (of trace is on)
     * 
     * @deprecated This method will be removed in a future version of Synapse.
     *             Please use the {@link SynapseLog} instance returned by
     *             {@link #getLog(MessageContext)} for all logging inside a
     *             mediator.
     * 
     * @param msg the log message
     * @param msgContext the message context
     */
    @Deprecated
    protected void auditLog(String msg, MessageContext msgContext) {
        log.info(msg);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().info(msg);
        }
        if (shouldTrace(msgContext.getTracingState())) {
            trace.info(msg);
        }
    }

    /**
     * Perform an error log message to all logs @ ERROR. Writes to the general log, the service log
     * and the trace log (of trace is on) and throws a SynapseException
     * @param msg the log message
     * @param msgContext the message context
     */
    protected void handleException(String msg, MessageContext msgContext) {
        log.error(msg);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg);
        }
        if (shouldTrace(msgContext.getTracingState())) {
            trace.error(msg);
        }
        throw new SynapseException(msg);
    }

    /**
     * Write an audit entry at WARN and trace and standard logs @WARN
     * 
     * @deprecated This method will be removed in a future version of Synapse.
     *             Please use the {@link SynapseLog} instance returned by
     *             {@link #getLog(MessageContext)} for all logging inside a
     *             mediator.
     * 
     * @param msg the message to log
     * @param msgContext message context
     */
    @Deprecated
    protected void auditWarn(String msg, MessageContext msgContext) {
        log.warn(msg);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().warn(msg);
        }
        if (shouldTrace(msgContext.getTracingState())) {
            trace.warn(msg);
        }
    }

    /**
     * Perform an error log message to all logs @ ERROR. Writes to the general log, the service log
     * and the trace log (of trace is on) and throws a SynapseException
     * @param msg the log message
     * @param e an Exception encountered
     * @param msgContext the message context
     */
    protected void handleException(String msg, Exception e, MessageContext msgContext) {
        log.error(msg, e);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg, e);
        }
        if (shouldTrace(msgContext.getTracingState())) {
            trace.error(msg, e);
        }
        throw new SynapseException(msg, e);
    }

    public boolean isStatisticsEnable() {
        return this.aspectConfiguration != null
                && this.aspectConfiguration.isStatisticsEnable();
    }

    public void disableStatistics() {
        if (this.aspectConfiguration != null) {
            this.aspectConfiguration.disableStatistics();
        }
    }

    public void enableStatistics() {
        if (this.aspectConfiguration != null) {
            this.aspectConfiguration.enableStatistics();
        }
    }

    /**
     * Configure aspects according to the given configuration
     *
     * @param aspectConfiguration AspectConfiguration instance
     */
    public void configure(AspectConfiguration aspectConfiguration) {
       this.aspectConfiguration = aspectConfiguration;
    }

    /**
     * Get the aspects  configuration
     *
     * @return AspectConfiguration instance
     */
    public AspectConfiguration getAspectConfiguration() {
        return aspectConfiguration;
    }

    public boolean isContentAware() {
        return true;
    }

    public int getMediatorPosition() {
       return mediatorPosition;
    }

    public void setMediatorPosition(int position) {
       mediatorPosition = position;
    }
}
