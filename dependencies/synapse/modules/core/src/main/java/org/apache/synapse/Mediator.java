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
 * All Synapse mediators must implement this Mediator interface. As a message passes
 * through the Synapse system, each mediator's mediate() method is invoked in the
 * sequence/order defined in the SynapseConfiguration.</p>
 *
 * <p>It is recommended to extend the abstract class
 * {@link org.apache.synapse.mediators.AbstractMediator} or the
 * {@link org.apache.synapse.mediators.AbstractListMediator} as appropriate instead of
 * directly implementing this interface
 *
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public interface Mediator extends SynapseArtifact {

    /**
     * Invokes the mediator passing the current message for mediation. Each
     * mediator performs its mediation action, and returns true if mediation
     * should continue, or false if further mediation should be aborted.
     *
     * @param synCtx the current message for mediation
     * @return true if further mediation should continue
     */
    public boolean mediate(MessageContext synCtx);

    /**
     * This is used for debugging purposes and exposes the type of the current
     * mediator for logging and debugging purposes
     * @return a String representation of the mediator type
     */
    public String getType();

    /**
     * This is used to check whether the tracing should be enabled on the current mediator or not
     * @return value that indicate whether tracing is on, off or unset
     */
    public int getTraceState();

    /**
     * This is used to set the value of tracing enable variable
     * @param traceState Set whether the tracing is enabled or not
     */
    public void setTraceState(int traceState);

    public boolean isContentAware();

    /**
     * Get the position of the mediator in sequence flow.
     * @return position of the mediator in sequence
     */
    public int getMediatorPosition();

    /**
     * Set the position of the mediator in the sequence
     * @param position position
     */
    public void setMediatorPosition(int position);

    /**
     * Set the short description of the mediator
     *
     * @param shortDescription to be set to the artifact
     */
    public void setShortDescription(String shortDescription);

    /**
     * Retrieves the short description of the mediator
     *
     * @return short description of the artifact
     */
    public String getShortDescription();
}
