package org.wso2.carbon.apimgt.ballerina.publisher;
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;

/**
 * Event publisher interface
 */
public interface EventPublisher {
    /**
     * initialize the data bridge event publisher
     *
     * @param type           publisher type
     * @param receiverURLSet receiver url set
     * @param authURLSet     auth url set
     * @param username       receiver username
     * @param password       receiver password
     * @param configPath     event publisher configuration file path
     * @throws DataEndpointAuthenticationException     throws if authenticate exception occurred
     * @throws DataEndpointAgentConfigurationException throws if agent configuration exception occurred
     * @throws TransportException                      throws if transport exception occurred
     * @throws DataEndpointException                   throws if exception occurred
     * @throws DataEndpointConfigurationException      throws if agent configuration exception occurred
     */
    void initDataPublisher(String type, String receiverURLSet, String authURLSet, String username, String password,
            String configPath)
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException;

    /**
     * publish events synchronously
     *
     * @param aEvent event payload
     */
    void publish(Event aEvent);
}
