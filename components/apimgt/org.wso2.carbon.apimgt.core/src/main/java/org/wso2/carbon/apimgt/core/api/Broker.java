/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.BrokerException;

import javax.jms.JMSException;
import javax.jms.TopicConnection;

/**
 * APIM Broker interface
 */
public interface Broker {

    /**
     * Get broker connection
     *
     * @return TopicConnection  new Topic connection to broker
     * @throws JMSException if a JMS related error occurred
     * @throws BrokerException if broker related error occurred
     */
    TopicConnection getTopicConnection() throws JMSException, BrokerException;
}
