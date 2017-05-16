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

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.client.AMQConnectionFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.core.APIMConfigurations;
import org.wso2.carbon.apimgt.core.api.Broker;
import org.wso2.carbon.apimgt.core.exception.BrokerException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

/**
 * The implementation for APIM broker
 */
public class BrokerImpl implements Broker {

    private APIMConfigurations config;
    private TopicConnectionFactory connFactory = null;
    private static final Logger log = LoggerFactory.getLogger(BrokerUtil.class);

    public BrokerImpl() {
        config = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        try {
            connFactory = new AMQConnectionFactory(getTCPConnectionURL(config));
        } catch (URLSyntaxException e) {
            log.error("Error while initilizing broker connection factory", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicConnection getTopicConnection() throws JMSException, BrokerException {
        if (connFactory == null) {
            String error = "Could not create a new connection to the broker. Connection Factory:[null].";
            log.error(error);
            throw new BrokerException(error, ExceptionCodes.BROKER_EXCEPTION);
        }
        return connFactory.createTopicConnection();
    }

    /**
     * Get connection config
     *
     * @return connection string
     */
    private String getTCPConnectionURL(APIMConfigurations config) {
        //TODO: The broker URL should be obtained from JNDI.properties file
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer().append("amqp://")
                .append(config.getUsername()).append(":")
                .append(config.getPassword()).append("@")
                .append(config.getCarbonClientId()).append("/")
                .append(config.getCarbonVirtualHostName()).append("?brokerlist='tcp://")
                .append(config.getTopicServerHost()).append(":")
                .append(config.getTopicServerPort()).append("'").toString();
    }

}


