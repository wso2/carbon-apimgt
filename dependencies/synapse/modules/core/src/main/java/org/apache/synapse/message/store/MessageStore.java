/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.message.store;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.Nameable;
import org.apache.synapse.SynapseArtifact;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.MessageProducer;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface MessageStore extends ManagedLifecycle, Nameable, SynapseArtifact {
    /**
     * Returns a Message Producer for this message store. <br/>
     * @return  A non-null message producer that can produce messages to this message store.
     */
    MessageProducer getProducer();

    /**
     * Returns a Message Consumer for this message store. <br/>
     * @return A non-null message consumer that can read messages from this message store.<br/>
     */
    MessageConsumer getConsumer();

    /**
     * set the implementation specific parameters
     * @param parameters A map of parameters or null
     */
    public void setParameters(Map<String,Object> parameters);

    /**
     * get the implementation specific parameters of the Message store
     * @return a properties map
     */
    public Map<String,Object> getParameters();

    /**
     * Set the name of the file that the Message store is configured
     *
     * @param filename Name of the file where this artifact is defined
     */
    public void setFileName(String filename);

    /**
     * get the file name that the message store is configured
     *
     * @return Name of the file where this artifact is defined
     */
    public String getFileName();

    /**
     * Returns the type of this message store. <br/>
     * The type of a message store can be one of following types, <br/>
     * {@link Constants#JMS_MS}, {@link Constants#INMEMORY_MS},
     * or {@link Constants#JDBC_MS}
     * @return Type of the message store.
     */
    public int getType();

    /**
     * Retrieves and removes the first Message in this store.
     * Message ordering will depend on the underlying implementation
     *
     * @return first message context in the store
     * @throws java.util.NoSuchElementException
     *          if store is empty
     */
    public MessageContext remove() throws NoSuchElementException;

    /**
     * Delete all the Messages in the Message Store
     */
    public void clear();

    /**
     * Delete and return the MessageContext with given Message id
     *
     * @param messageID message id of the Message
     * @return MessageContext instance
     */
    public MessageContext remove(String messageID);

    /**
     * Returns the number of Messages  in this store.
     *
     * @return the number of Messages in this Store
     */
    public int size();

    /**
     * Return the Message in given index position
     * (this may depend on the implementation)
     *
     * @param index position of the message
     * @return Message in given index position
     */
    public MessageContext get(int index);

    /**
     * Get the All messages in the Message store without removing them from the queue
     *
     * @return List of all Messages
     */
    public List<MessageContext> getAll();

    /**
     * Get the Message with the given ID from the Message store without removing it
     *
     * @param messageId A message ID string
     * @return Message with given ID
     */
    public MessageContext get(String messageId);
}
