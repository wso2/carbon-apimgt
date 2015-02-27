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

package org.apache.synapse.core;

import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.synapse.MessageContext;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.aspects.statistics.StatisticsCollector;
import org.apache.synapse.carbonext.TenantInfoConfigurator;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.task.SynapseTaskManager;
import org.apache.synapse.util.xpath.ext.SynapseXpathFunctionContextProvider;
import org.apache.synapse.util.xpath.ext.SynapseXpathVariableResolver;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * The SynapseEnvironment allows access into the the host SOAP engine. It allows
 * the sending of messages, class loader access etc.
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface SynapseEnvironment {

    /**
     * This method injects a new message into the Synapse engine. This is used by
     * the underlying SOAP engine to inject messages into Synapse for mediation.
     * e.g. The SynapseMessageReceiver used by Axis2 invokes this to inject new messages
     *
     * @param smc - Synapse MessageContext to be injected
     * @return boolean true if the message processing should be continued
     *  and false if it should be aborted
     */
    public boolean injectMessage(MessageContext smc);

    /**
     * This method injects a new message into the Synapse engine for the mediation
     * by the specified sequence. This is used by custom mediation tasks like splitting message
     * in EIP mediation. This method will do the mediation asynchronously using a separate
     * thread from the environment thread pool
     *
     * @param smc - Synapse message context to be injected
     * @param seq - Sequence to be used for mediation
     */
    public void injectAsync(MessageContext smc, SequenceMediator seq);

    /**
     * This method allows a message to be sent through the underlying SOAP engine. This will
     * send request messages on (forward), and send the response messages back to the client
     *
     * @param endpoint  - Endpoint to be used for sending
     * @param smc       - Synapse MessageContext to be sent
     */
    public void send(EndpointDefinition endpoint, MessageContext smc);

    /**
     * Creates a new Synapse <code>MessageContext</code> instance.
     *
     * @return a MessageContext
     */
    public MessageContext createMessageContext();

    /**
     * Creates a new <code>TemporaryData</code> instance for the temp storage requirements
     *
     * @return a TemporaryData created from the parameters provided in the synapse.properties
     */
    public OverflowBlob createOverflowBlob();

   /**
     * This method returns the <code>StatisticsCollector</code> responsible for
     * collecting stats for this synapse instance.
     *
     * @return Returns the <code>StatisticsCollector</code>
     */
    public StatisticsCollector getStatisticsCollector();

    /**
     * To set the StatisticsCollector to the environment
     *
     * @param statisticsCollector - StatisticsCollector to be set
     */
    @Deprecated
    public void setStatisticsCollector(StatisticsCollector statisticsCollector);

    /**
     * This is used by anyone who needs access to a SynapseThreadPool.
     * It offers the ability to start work.
     * 
     * @return Returns the ExecutorService
     */
     public ExecutorService getExecutorService();

    /**
     * Has the Synapse Environment properly initialized?
     * 
     * @return true if the environment is ready for processing
     */
    public boolean isInitialized();

    /**
     * Set the environment as ready for message processing
     * 
     * @param state true means ready for processing
     */
    public void setInitialized(boolean state);

    /**
     * Retrieves the {@link SynapseConfiguration} from the <code>environment</code>
     * 
     * @return configuration of the synapse
     */
    public SynapseConfiguration getSynapseConfiguration();

    /**
     * Retrieve the {@link org.apache.synapse.task.SynapseTaskManager} from the
     * <code>environment</code>.
     *
     * @return SynapseTaskManager of this synapse environment
     */
    public SynapseTaskManager getTaskManager();


    /**
     * Get the information about the synapse environment.
     * 
     * @return {@link org.apache.synapse.ServerContextInformation} of this synapse environment
     */
    public ServerContextInformation getServerContextInformation();

    /**
     * Get all Xpath Extension objects for Function contexts
     * @return Map containing xpath extension objects
     */
    public Map<QName, SynapseXpathFunctionContextProvider> getXpathFunctionExtensions();


    /**
     * Get all Xpath Extension objects for Variable contexts
     * @return Map containing xpath extension objects
     */
    public Map<QName, SynapseXpathVariableResolver> getXpathVariableExtensions();

    /**
     *
     * @return
     */
    public TenantInfoConfigurator getTenantInfoConfigurator();

    /**
     * Increment/Decrement the Call mediator count in the environment by 1
     * @param isIncrement whether to increment the count
     */
    public void updateCallMediatorCount(boolean isIncrement);

    /**
     * Whether continuation is enabled in the environment
     * @return whether continuation is enabled in the environment
     */
    public boolean isContinuationEnabled();

    /**
     * Add an artifact reference not available in the environment.
     * @param key artifact reference key
     */
    public void addUnavailableArtifactRef(String key);

    /**
     * Remove the artifact reference which is marked as unavailable in environment
     * from the unavailable list
     *
     * @param key artifact reference key
     */
    public void removeUnavailableArtifactRef(String key);


    /**
     * Clear unavailability of an artifact if it is
     * previously marked as unavailable in the environment
     *
     * @param key artifact reference key
     */
    public void clearUnavailabilityOfArtifact(String key);

}
