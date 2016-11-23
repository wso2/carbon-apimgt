/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.client.AMQConnectionFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.core.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.core.template.APITemplateException;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;


/**
 * API gateway related functions
 */
public class APIGatewayImpl implements APIGateway {
    private static final Logger log = LoggerFactory.getLogger(APIGatewayImpl.class);
    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static final String CARBON_CLIENT_ID = "carbon";
    private static final String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static final String CARBON_DEFAULT_HOSTNAME = "localhost";
    private static final String CARBON_DEFAULT_PORT = "5672";
    private String topicName = "MYTopic";

    /**
     * Publishing API configuration artifacts to the gateway
     *
     * @param api API model
     * @return is publishing success
     */
    @Override
    public boolean publishToGateway(API api) {
        APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
        try {
            String content = apiTemplateBuilder.getConfigStringForTemplate(api);
            if (System.getProperty("gateway") != null) {
                String path = System.getProperty("gateway") + File.separator + "deployment" + File.separator
                        + "integration-flows" + File.separator + api.getName() + ".xyz";
                saveApi(path, content);
            }
            publishMessage(content);
            return true;
        } catch (APITemplateException e) {
            log.error("Error generating API configuration for API " + api.getName(), e);
        } catch (JMSException e) {
            log.error("Error generating API configuration for API " + api.getName(), e);
        } catch (URLSyntaxException e) {
            log.error("Error generating API configuration for API " + api.getName(), e);
        }
        return false;
    }

    /**
     * Save API into FS
     *
     * @param path    path
     * @param content API config
     */
    private void saveApi(String path, String content) {
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            printWriter = new PrintWriter(writer);
            printWriter.println(content);
        } catch (IOException e) {
            log.error("Error saving API configuration in " + path, e);
        } finally {
            try {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error("Error closing connections", e);
            }
        }
    }

    /**
     * Publishing the API config to gateway
     *
     * @param content API configuration
     * @throws JMSException       if JMS issue is occurred
     * @throws URLSyntaxException If connection String is invalid
     */
    private void publishMessage(String content) throws JMSException, URLSyntaxException {
        String userName = "admin";
        String password = "admin";
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(userName, password));

        // create connection factory
        TopicConnectionFactory connFactory = new AMQConnectionFactory(getTCPConnectionURL(userName, password));
        TopicConnection topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        TopicSession topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        // Send message
        Topic topic = topicSession.createTopic(topicName);
        // create the message to send
        TextMessage textMessage = topicSession.createTextMessage(content);
        TopicPublisher topicPublisher = topicSession.createPublisher(topic);
        topicPublisher.publish(textMessage);

        topicPublisher.close();
        topicSession.close();
        topicConnection.stop();
        topicConnection.close();
    }

    /**
     * Get connection config
     *
     * @param username username
     * @param password password
     * @return connection string
     */
    private String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer().append("amqp://").append(username).append(":").append(password).append("@")
                .append(CARBON_CLIENT_ID).append("/").append(CARBON_VIRTUAL_HOST_NAME).append("?brokerlist='tcp://")
                .append(CARBON_DEFAULT_HOSTNAME).append(":").append(CARBON_DEFAULT_PORT).append("'").toString();

    }
}
