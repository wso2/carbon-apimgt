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

import java.util.List;

public interface MessageStoreViewMBean {
    /**
     * Delete all the Messages in Message store
     */
    public void deleteAll();

    /**
     * Get the Message IDs of all stored Messages in the Message store
     *
     * @return a list of message ID values
     */
    public List<String> getMessageIds();

    /**
     * Delete the Message with Given id
     *
     * @param messageID ID of the message to be deleted
     */
    public void delete(String messageID);

    /**
     * Get the SOAP envelope of the given Message with given ID
     *
     * @param messageID ID of the message to be returned
     * @return the SOAP envelope content as a string
     */
    public String getEnvelope(String messageID);

    /**
     *
     * @return the number of Messages stored in the store.
     */
    public long getSize();
}
