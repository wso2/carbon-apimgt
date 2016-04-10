/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.ThrottlingRunTimeException;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThrottleDataPublisher {
    private static final ThrottleDataPublisherPool dataPublisherPool = ThrottleDataPublisherPool.getInstance();

    public static final Log log = LogFactory.getLog(ThrottleDataPublisher.class);

    private static volatile DataPublisher dataPublisher = null;

    static Executor pool = Executors.newFixedThreadPool(100);
    private ThrottleProperties.DataPublisher dataPublisherConfiguration = ServiceReferenceHolder.getInstance()
            .getThrottleProperties().getDataPublisher();

    public ThrottleDataPublisher() {
        try {
            dataPublisher = new DataPublisher(dataPublisherConfiguration.getType(), dataPublisherConfiguration
                    .getReceiverUrlGroup(), dataPublisherConfiguration.getAuthUrlGroup(), dataPublisherConfiguration
                    .getUsername(),
                    dataPublisherConfiguration.getPassword());
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (DataEndpointException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (DataEndpointConfigurationException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (DataEndpointAuthenticationException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (TransportException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        }
    }

    public void publishNonThrottledEvent(MessageContext messageContext) {
        DataProcessAndPublishingAgent agent = null;
        long start = System.currentTimeMillis();
        try {

            agent = dataPublisherPool.get();
            agent.processAndPublishEvent(messageContext,dataPublisher);
            pool.execute(agent);

        } catch (Exception e) {
            throw new ThrottlingRunTimeException("Error while publishing throttling events to global policy server");
        }
    }
}
