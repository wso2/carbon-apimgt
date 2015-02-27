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

import org.apache.synapse.SequenceType;

/**
 * A SeqContinuationState is added when mediation flow is branched using a Sequence Mediator.
 * SeqContinuationStates are stored in ContinuationState Stack which resides in MessageContext.
 *
 * @see org.apache.synapse.ContinuationState
 */
public class SeqContinuationState extends AbstractContinuationState {

    private String seqName = null;
    private SequenceType seqType = null;

    public SeqContinuationState(SequenceType seqType, String seqName) {
        this.seqType = seqType;
        this.seqName = seqName;
    }

    /**
     * Get the sequence type
     * @return sequence type
     */
    public SequenceType getSeqType() {
        return seqType;
    }

    /**
     * Get the sequence name.
     * null returns for all sequence types except named sequences
     * @return sequence name
     */
    public String getSeqName() {
        return seqName;
    }

}
