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

package org.apache.synapse.message;

import org.apache.synapse.MessageContext;

public interface MessageProducer {
    /**
     * Stores the given message to the store associated with this message consumer.
     * @param synCtx Message to be saved.
     * @return {@code true} if storing of the message is successful, {@code false} otherwise.
     */
    boolean storeMessage(MessageContext synCtx);

    /**
     * Cleans up this message consumer
     * @return {@code true} if clean up is successful, {@code false} otherwise.
     */
    boolean cleanup();

    /**
     * Sets the ID of this message consumer.
     * @param id ID
     */
    public void setId(int id);

    /**
     * Returns the ID of this message  consumer.
     * @return ID
     */
    public String getId();
}
