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

import org.apache.synapse.MessageContext;

/**
 * The filter mediator is a list mediator, which executes the given (sub) list of mediators
 * if the specified condition is satisfied
 *
 * @see FilterMediator#test(org.apache.synapse.MessageContext)
 */
public interface FilterMediator extends ListMediator {

    /**
     * Should return true if the sub/child mediators should execute. i.e. if the filter
     * condition is satisfied
     * @param synCtx
     * @return true if the configured filter condition evaluates to true
     */
    public boolean test(MessageContext synCtx);
}
