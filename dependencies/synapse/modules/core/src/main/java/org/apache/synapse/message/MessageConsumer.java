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

public interface MessageConsumer {
    /**
     * Receives the next message from the store.
     * @return Synapse message context of the last message received from the store.
     */
    MessageContext receive();

    /**
     * Acknowledges the last message received so that it will be removed from the store.
     * @return {@code true} if the acknowledgement is successful. {@code false} otherwise.
     */
    boolean ack();

    /**
     * Cleans up this message consumer
     * @return {@code true} if cleanup is successful, {@code false} otherwise.
     */
    boolean cleanup();

    /**
     * Sets the ID of this message consumer.
     * @param i ID
     */
    public void setId(int i);

    /**
     * Returns the ID of this Message consumer.
     * @return ID
     */
    public String getId();
}
