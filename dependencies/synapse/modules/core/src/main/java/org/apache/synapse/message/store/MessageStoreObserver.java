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
package org.apache.synapse.message.store;


/**
 * An implementation of this interface can be registered with a Message store instance to receive
 * Message store update update events. When ever a message is added/removed events defined in this
 * interface will be fired
 */
public interface MessageStoreObserver {
    /**
     * Method invoked when a message is added to the store
     * @param messageId of the message that was added to the Message store
     */
    public void messageAdded(String messageId);

    /**
     * Method invoked when a message is removed from the store
     * @param messageId of the Message that was removed
     */
    public void messageRemoved(String messageId);
}
