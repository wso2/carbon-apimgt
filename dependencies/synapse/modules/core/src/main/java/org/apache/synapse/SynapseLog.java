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

import org.apache.synapse.mediators.AbstractMediator;

/**
 * Synapse logging interface.
 * This interface defines a set of Synapse specific log levels.
 * <p>
 * Note that the definition of this interface is not yet stable.
 * Please refer to https://issues.apache.org/jira/browse/SYNAPSE-374 for
 * more information.
 * 
 * @see AbstractMediator#getLog(org.apache.synapse.MessageContext)
 */
public interface SynapseLog {
    /**
     * Check whether a call to {@link #traceOrDebug(Object)} would actually cause a log
     * message to be written to the logs.
     *
     * @return <code>true</code> if trace or debug is enabled
     */
    boolean isTraceOrDebugEnabled();
    
    /**
     * Log a message at level 'traceOrDebug'.
     * In mediators this method should be used to replace the following code:
     * <pre>
     * if (traceOrDebugOn) {
     *     traceOrDebug(traceOn, ...);
     * }</pre>
     * 
     * @param msg the message to be logged
     */
    void traceOrDebug(Object msg);
    
    /**
     * Log a message at level 'traceOrDebugWarn'.
     * In mediators this method should be used to replace the following code:
     * <pre>
     * if (traceOrDebugOn) {
     *     traceOrDebugWarn(...);
     * }</pre>
     *
     * @param msg the message to be logged
     */
    void traceOrDebugWarn(Object msg);
    
    /**
     * Check whether a call to {@link #traceTrace(Object)} would actually cause a log
     * message to be written to the logs.
     *
     * @return <code>true</code> if trace is enabled for the trace log
     */
    boolean isTraceTraceEnabled();
    
    /**
     * Log a message at level 'traceTrace'.
     * In mediators this method should be used to replace the following code:
     * <pre>
     * if (traceOn && trace.isTraceEnabled()) {
     *     trace.trace(...);
     * }</pre>
     * 
     * @param msg the message to be logged
     */
    void traceTrace(Object msg);
    
    /**
     * Log an audit message.
     * In mediators this method should be used to replace
     * {@link AbstractMediator#auditLog(String, MessageContext)}.
     * 
     * @param msg the message to be logged
     */
    void auditLog(Object msg);

    /**
     * Log an audit message at the TRACE category.
     *
     * @param msg the message to be logged
     */
    void auditDebug(Object msg);

    /**
     * Log an audit message at the TRACE category.
     *
     * @param msg the message to be logged
     */
    void auditTrace(Object msg);
    
    /**
     * Log a warning message.
     * In mediators this method should be used to replace
     * {@link AbstractMediator#auditWarn(String, MessageContext)}.
     * 
     * @param msg the message to be logged
     */
    void auditWarn(Object msg);

    /**
     * Log an audit message at the ERROR category.
     *
     * @param msg the message to be logged
     */
    void auditError(Object msg);

    /**
     * Log an audit message at the FATAL category.
     *
     * @param msg the message to be logged
     */
    void auditFatal(Object msg);
    
    /**
     * Log a message at level 'error'.
     * 
     * @param msg the message to be logged
     */
    void error(Object msg);
    
    /**
     * Log a fatal exception. This method should only be called when a
     * {@link SynapseException} is being thrown.
     * 
     * @param msg the message of the exception
     * @param cause the cause of the exception
     */
    void logSynapseException(String msg, Throwable cause);
}
