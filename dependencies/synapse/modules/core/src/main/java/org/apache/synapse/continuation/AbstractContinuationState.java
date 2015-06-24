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

package org.apache.synapse.continuation;

import org.apache.synapse.ContinuationState;

public abstract class AbstractContinuationState implements ContinuationState {

    private int position = 0;

    /**
     * Holds the Child ContinuationState.
     */
    private ContinuationState childContState = null;

    /**
     * Get the child ContinuationState
     * @return child ContinuationState
     */
    public ContinuationState getChildContState() {
        return childContState;
    }

    /**
     * Set the child ContinuationState.
     * Child ContinuationState is added when branching the flow using all FlowContinuableMediators
     * except Sequence Mediator.
     * @param childContState ContinuationState to be added as the child
     */
    public void setChildContState(ContinuationState childContState) {
        this.childContState = childContState;
    }

    /**
     * Get the position of the mediator in the current flow.
     * @return position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Set the position of the mediator in the flow which is currently being processed.
     * Position should be updated only when branching to a new flow (i.e. not necessary to update
     * for each and every mediator execution in the flow)
     * @param position position of the mediator which is currently being processed in the flow.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Check whether child ContinuationState exists
     * @return whether child ContinuationState exists
     */
    public boolean hasChild() {
        return childContState != null;
    }

    /**
     * Get the Leaf Child of this ContinuationState.
     * When mediate using the ContinuationStateStack, first we start from the Lead Child of the
     * ContinuationState.
     *
     * @return Leaf child of the ContinuationState
     */
    public ContinuationState getLeafChild() {
        if (this.hasChild()) {
            return childContState.getLeafChild();
        } else {
            return this;
        }
    }

    /**
     * Add a Leaf child to this ContinuationState
     * @param leafChild ContinuationState which can be added as a Leaf child
     *                   for this SeqContinuationState
     */
    public void addLeafChild(ContinuationState leafChild) {
        if (this.hasChild()) {
            childContState.addLeafChild(leafChild);
        } else {
            this.childContState = leafChild;
        }
    }

    /**
     * Remove the Leaf child form this ContinuationState.
     */
    public void removeLeafChild() {
        if (childContState != null) {
            if (childContState.hasChild()) {
                childContState.removeLeafChild();
            } else {
                this.childContState = null;
            }
        }
    }

}
