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
 * Implementations of this interface holds the runtime state information of important checkpoints
 * of the mediation flow.
 * This information is mainly used to mediate the response message when service is invoked using the
 * Call Mediator.
 * Instances of ContinuationState are stored in the ContinuationState Stack which resides in the
 * MessageContext
 */
public interface ContinuationState {

    /**
     * Get the child ContinuationState
     * @return child ContinuationState
     */
    public ContinuationState getChildContState();

    /**
     * Set the child ContinuationState.
     * @param childContState ContinuationState to be added as the child
     */
    public void setChildContState(ContinuationState childContState);

    /**
     * Get the position of the mediator in the current flow.
     * @return position
     */
    public int getPosition();

    /**
     * Set the position of the mediator in the flow which is currently being processed.
     * @param position position of the mediator which is currently being processed in the flow.
     */
    public void setPosition(int position);

    /**
     * Check whether child ContinuationState exists
     * @return whether child ContinuationState exists
     */
    public boolean hasChild();

    /**
     * Get the Leaf Child of this ContinuationState.
     * @return Leaf child of the ContinuationState
     */
    public ContinuationState getLeafChild();

    /**
     * Add a Leaf child to this ContinuationState
     * @param leafChild ContinuationState which can be added as a Leaf  child
     * for this ContinuationState
     */
    public void addLeafChild(ContinuationState leafChild);

    /**
     * Remove the Leaf child form this ContinuationState.
     */
    public void removeLeafChild();

}
