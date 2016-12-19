/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.client.AMQConnectionFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.gateway.APIMConfigurations;
//import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
//import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;

/**
 * This class is used to subscribe to the jms topic and save the read configuration to the file system
 * TODO refactor class when kernal is updated to 5.2.0
 */
public class APITopicSubscriber {
    private static final Logger log = LoggerFactory.getLogger(APITopicSubscriber.class);
    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private APIMConfigurations config = null;

    APITopicSubscriber() {
        /*try {
            config = ServiceReferenceHolder.getInstance().getConfigProvider()
                    .getConfigurationObject(APIMConfigurations.class);
        } catch (CarbonConfigurationException e) {
            log.error("error getting config", e);
        }

        if (config == null) {
            config = new APIMConfigurations();
            log.info("Setting default configurations");
        }*/

        config = new APIMConfigurations();
    }
    /**
     * Subscribe to the topic
     *
     * @return subscriber
     * @throws NamingException    throws if any name resolution issue occur
     * @throws JMSException       throws if JMS exception occurred
     * @throws URLSyntaxException throws if connection string exception found
     */
    public TopicSubscriber subscribe() throws NamingException, JMSException, URLSyntaxException {
        // Lookup connection factory
        TopicConnectionFactory connFactory = new AMQConnectionFactory(
                getTCPConnectionURL(config.getUsername(), config.getPassword()));
        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        // Send message
        Topic topic = topicSession.createTopic(config.getTopicName());
        TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);
        return topicSubscriber;
    }

    /**
     * Try to read msg from topic
     *
     * @param topicSubscriber current subscriber
     * @throws NamingException throws if any name resolution issue occur
     * @throws JMSException    throws if JMS exception occurred
     */
    public void receive(TopicSubscriber topicSubscriber) throws NamingException, JMSException {
        Message message = topicSubscriber.receive();
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String msg = textMessage.getText();
            if (log.isDebugEnabled()) {
                log.debug("Got API config from topic subscriber = " + msg);
            }
            deployApiConfig(msg);
        }
    }

    /**
     * Stop the jms listener
     */
    public void stop() {
        if (topicSession != null) {
            try {
                topicSession.close();
            } catch (JMSException e) {
                // TODO Auto-generated catch block
                log.error("Error closing connections", e);
            }
        }
        if (topicConnection != null) {
            try {
                topicConnection.stop();
                topicConnection.close();
            } catch (JMSException e) {
                // TODO Auto-generated catch block
                log.error("Error closing connections", e);
            }
        }

    }

    /**
     * Construct and get JMS connection String
     *
     * @param username user name
     * @param password password
     * @return connection String as String
     */
    private String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return new StringBuffer().append("amqp://").append(username).append(":").append(password).append("@")
                .append(config.getCarbonClientId()).append("/")
                .append(config.getCarbonVirtualHostName()).append("?brokerlist='tcp://")
                .append(config.getTopicServerHost()).append(":")
                .append(config.getTopicServerPort()).append("'").toString();
    }

    /**
     * Deploying API config in to FS
     *
     * @param apiConfig api configuration
     */
    private void deployApiConfig(String apiConfig) {
        String apiConfigExtension = ".xyz";
        String reg = "\"(.*?)\"";
        Pattern p = Pattern.compile(reg);

        String context;
        Matcher m = p.matcher(apiConfig);
        if (m.find()) {
            context = m.group(1);
        } else {
            context = "defaultApiName_" + System.currentTimeMillis();
            log.warn("unable to find the API name and version. Setting default file name as " + context);
        }
        String fileName = context.replaceAll("/", "_") + apiConfigExtension;
        String path =
                System.getProperty("carbon.home") + File.separator + "deployment" + File.separator + "integration-flows"
                        + File.separator + fileName;
        saveApi(path, apiConfig);
        log.info("Deployed API config in " + path);
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
}
