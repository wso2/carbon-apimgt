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

/**
 * <p>Runtime exception for Synapse code to throw
 */
public class SynapseException extends RuntimeException {

    private static final long serialVersionUID = -7244032125641596311L;

    public SynapseException(String string) {
        super(string);
    }

    public SynapseException(String msg, Throwable e) {
        super(msg, e);
    }

    public SynapseException(Throwable t) {
        super(t);
    }

    /**
     * Convenience constructor that allows to log and throw the exception in a single
     * instruction. It will call {@link SynapseLog#logSynapseException(String, Throwable)}
     * to log the error.
     * 
     * @param msg
     * @param synLog
     */
    public SynapseException(String msg, SynapseLog synLog) {
        super(msg);
        synLog.logSynapseException(msg, null);
    }

    /**
     * Convenience constructor that allows to log and throw the exception in a single
     * instruction. It will call {@link SynapseLog#logSynapseException(String, Throwable)}
     * to log the error.
     * 
     * @param msg
     * @param cause
     * @param synLog
     */
    public SynapseException(String msg, Throwable cause, SynapseLog synLog) {
        super(msg, cause);
        synLog.logSynapseException(msg, cause);
    }
}
