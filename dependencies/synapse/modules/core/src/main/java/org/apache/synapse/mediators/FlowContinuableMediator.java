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

import org.apache.synapse.ContinuationState;
import org.apache.synapse.MessageContext;

/**
 * Mediators which implements this interface can mediate the message using a ContinuationState
 *
 */
public interface FlowContinuableMediator {

    /**
     * Mediate the message using a ContinuationState.
     * This is used when mediating the response message when service is invoked
     * using a Call Mediator.
     *
     * @param synCtx MessageContext
     * @param continuationState ContinuationState
     * @return whether mediation flow is completed
     */
    public boolean mediate(MessageContext synCtx,
                           ContinuationState continuationState);

}
