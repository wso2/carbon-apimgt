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

package org.apache.synapse.mediators.eip;

/** Constants related to the EIP mediators */
public final class EIPConstants {

    /** Typically the message ID of the parent message in a split/iterate etc so that
     * its children could be uniquely aggregated by the aggrgate mediator etc
     */
    public static final String AGGREGATE_CORRELATION = "aggregateCorelation";

    /** Constant for the message sequence property key */
    public static final String MESSAGE_SEQUENCE = "messageSequence";

    /** Delimiter for the message sequence value */
    public static final String MESSAGE_SEQUENCE_DELEMITER = "/";
}
