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

package org.apache.synapse.mediators.eip.sample;

import org.apache.synapse.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class UnboundedMessageQueue implements MessageQueue {

    private List<MessageContext> messageQueue = new ArrayList<MessageContext>();

    public void add(MessageContext synCtx) {
        messageQueue.add(synCtx);
    }

    public MessageContext get() {
        if (!messageQueue.isEmpty()) {
            return messageQueue.remove(0);
        } else {
            return null;
        }
    }

    public boolean isEmpty() {
        return messageQueue.isEmpty();
    }

    public boolean isPersistent() {
        return false;
    }

    public boolean persist() {
        return false;
    }

    public void load() {
        throw new UnsupportedOperationException("Not Implemented");
    }

}
