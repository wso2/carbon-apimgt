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

package org.apache.synapse.message.processor;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Nameable;
import org.apache.synapse.SynapseArtifact;
import org.apache.synapse.message.MessageConsumer;

import java.util.Map;

public interface MessageProcessor extends ManagedLifecycle, Nameable, SynapseArtifact {
    /**
     * This method is used to start the message processor. Once the message processor is started
     * it will start receiving messages
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean start();

    /**
     * This method is used to stop the message processor. Once the the message processor is stopped
     * it will no longer receive messages. A stopped message processor cannot re restarted without
     * re-instantiating.
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean stop();

    /**
     * This method is used to deactivate the message processor. This will temporarily halt executing services.
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean deactivate();

    /**
     * This method is used to activate a deactivated message processor. Activating message processor
     * will cause the services to start
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean activate();

    /**
     * This method is used to see if the message processor is deactivated.
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean isDeactivated();

    /**
     * This method is used to set the associated message store of the message processor. Every message processor
     * has to be bound to a message store
     * @param messageStoreName Name of this message store.
     */
    void setMessageStoreName(String messageStoreName);

    /**
     * This method returns the associated message store name of the message processor.
     * @return Name of this message store.
     */
    String getMessageStoreName();

    /**
     * This method is used to set configuration parameters of the message processor. For example, triggering interval
     * retrying interval, and etc.
     * @param parameters Message processor parameters.
     */
    void setParameters(Map<String,Object> parameters);

    /**
     * This method is used to retrieve the configuration parameters of message processor.
     * @return the extracted parameters of the message processor configuration
     */
    Map<String , Object> getParameters();

    /**
     * This method is used to set the actual configuration file. This file has all the configuration related
     * to particular message processor.
     * @param fileName is the name of the file
     */
    void setFileName(String fileName);

    /**
     * This method is used to retrieve the configuration file name of the message processor.
     * @return the file name
     */
    String getFileName();

    /**
     * This method is used to set the message consumer of message processor. Consumer is the one who is
     * responsible for retrieving messages from a store.
     * @param messageConsumer is the name of the associated message consumer
     * @return is true if the message if the message consumer is returned successfully. Otherwise false.
     */
    boolean setMessageConsumer(MessageConsumer messageConsumer);

    /**
     * This method retrieves the message consumer of message processor.
     * @return the message consumer
     */
    MessageConsumer getMessageConsumer();

    /**
     * This method set the target endpoint associated with the message processor. Without a target endpoint
     * a message processor could not operated successfully.
     * @param targetEndpoint is the name of the associated endpoint
     */
    void setTargetEndpoint(String targetEndpoint);

    /**
     * This method is used to retrieve the associated target endpoint name of the message processor.
     * @return The name of the endpoint
     */
    String getTargetEndpoint();

    /**
     * This method is only used by the associated forwarding services of message processors.
     * When the service fails to send the message to the backend it pauses the message processor and
     * starts retrying. Pausing the message processor avoids re-triggering new services till the existing
     * service succeed.
     */
    void pauseService();

    /**
     * This is the opposite of pauseService method. This method resumes a paused method.
     */
    void resumeService();

    /**
     * This method is used to check if the state is in paused mode.
     * @return returns true on success.
     */
    boolean isPaused();
}
