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

/**
 * ReliantContinuationStates are added when branching the flow from a FlowContinuableMediator
 * which cannot be directly referred from the synapse configuration as an artifact.
 * For an example Filter mediator is a FlowContinuableMediator and we cannot directly
 * access from the synapse configuration. We only can access the Sequence which filter is placed.
 * So a ReliantContinuationState is added for all FlowContinuableMediator Mediators except
 * Sequence Mediator.
 * <p/>
 * ReliantContinuationStates are get added as a child of a SeqContinuationState.
 * Grand parent of the ReliantContinuationState is always a SeqContinuationState which are placed
 * in the ContinuationState Stack.
 *
 * @see org.apache.synapse.ContinuationState
 */
public class ReliantContinuationState extends AbstractContinuationState {

    private int subBranch = 0;

    public ReliantContinuationState(int subBranch) {
        this.subBranch = subBranch;
    }

    /**
     * Get the subBranch of the FlowContinuableMediator.
     * @return subBranch id
     */
    public int getSubBranch() {
        return subBranch;
    }

    /**
     * Set the subBranch of the FLowContinuableMediator
     * @param subBranch subBranch id
     */
    public void setSubBranch(int subBranch) {
        this.subBranch = subBranch;
    }

}
