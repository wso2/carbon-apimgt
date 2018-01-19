/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.core.api.Broker;
import org.wso2.carbon.apimgt.core.configuration.models.BrokerConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.JMSConnectionConfiguration;
import org.wso2.carbon.apimgt.core.exception.BrokerException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;


/**
 * The implementation for APIM broker
 */
public class BrokerImpl implements Broker {

    private TopicConnectionFactory connFactory = null;
    private static final Logger log = LoggerFactory.getLogger(BrokerUtil.class);
    private BrokerConfigurations config;

    public BrokerImpl() {
        config = ServiceReferenceHolder.getInstance().getAPIMConfiguration().getBrokerConfigurations();
        JMSConnectionConfiguration jmsConnectionConfiguration = config.getJmsConnectionConfiguration();
        Class<?> clientClass = null;
        Constructor<?> construct = null;
        Object clientInst = null;
        try {
            clientClass = Class.forName("org.wso2.andes.client.AMQConnectionFactory");
            construct = clientClass.getConstructor(String.class);
            String username = jmsConnectionConfiguration.getUsername();
            String password = jmsConnectionConfiguration.getPassword();
            String url = jmsConnectionConfiguration.getTopicConnectionFactoryURL();
            String connectionUrl = getBrokerConnectionString(username, password, url);
            clientInst = construct.newInstance(connectionUrl);
            connFactory = (TopicConnectionFactory) clientInst;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            String error = "Could not create a JMS client connection from the class";
            log.error(error, e);
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
     * Get full broker url
     * @return
     */
    private String getBrokerConnectionString(String username, String password, String brokerUrl) {
        return "amqp://" + username + ":" + password + "@clientID/carbon?brokerlist='" +
                brokerUrl + "'";
    }
}


