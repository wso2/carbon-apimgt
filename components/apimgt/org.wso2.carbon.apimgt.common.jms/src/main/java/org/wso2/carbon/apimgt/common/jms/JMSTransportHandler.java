/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.common.jms;

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.factory.JMSConnectionFactory;
import org.wso2.carbon.apimgt.common.jms.factory.JMSTaskManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.jms.MessageListener;

/**
 * Handler for JMS transport initialization and subscription.
 */
public class JMSTransportHandler {

    private static final Log log = LogFactory.getLog(JMSTransportHandler.class);
    private JMSConnectionFactory jmsConnectionFactory;
    private List<JMSListener> jmsListenerList = new ArrayList<>();
    private int minThreadPoolSize = 20;
    private int maxThreadPoolSize = 100;
    private int keepAliveTimeInMillis = 1000;
    private int jobQueueSize = 10;
    private boolean stopIssued = false;
    private static final Object lock = new Object();

    public JMSTransportHandler(Properties jmsConnectionProperties) {

        Properties properties;
        Hashtable<String, String> parameters = new Hashtable<>();

        if (jmsConnectionProperties.isEmpty()) {
            properties = new Properties();
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream resourceStream = classLoader.getResourceAsStream(ListenerConstants.MB_PROPERTIES)) {
                properties.load(resourceStream);
            } catch (IOException e) {
                log.error("Cannot read properties file from resources. " + e.getMessage(), e);
            }
        } else {
            properties = jmsConnectionProperties;
        }

        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name, properties.getProperty(name));
        }
        jmsConnectionFactory = new JMSConnectionFactory(parameters, ListenerConstants.CONNECTION_FACTORY_NAME);
    }

    /**
     * This method is used to subscribe to JMS topics and receive JMS messages
     * @param messageListener
     */
    public void subscribeForJmsEvents(String topicName, MessageListener messageListener) {

        //Listening to throttleData topic
        JMSListener jmsMessageListener =
                createJMSMessageListener(topicName, minThreadPoolSize, maxThreadPoolSize, keepAliveTimeInMillis,
                        jobQueueSize, messageListener);
        jmsMessageListener.startListener();
        jmsListenerList.add(jmsMessageListener);
        log.info("Starting jms topic consumer thread for the " + topicName + " topic...");

    }

    private JMSListener createJMSMessageListener(String topicName, int minThreadPoolSize, int maxThreadPoolSize,
                                                 int keepAliveTimeInMillis, int jobQueueSize,
                                                 MessageListener messageListener) {

        Map<String, String> messageConfig = new HashMap<>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);
        JMSTaskManager jmsTaskManager = JMSTaskManagerFactory
                .createTaskManagerForService(jmsConnectionFactory,
                        ListenerConstants.CONNECTION_FACTORY_NAME,
                        new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize,
                                keepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                                "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManager.setMessageListener(messageListener);

        JMSListener jmsListener = new JMSListener(ListenerConstants.CONNECTION_FACTORY_NAME
                + "#" + topicName, jmsTaskManager);
        return jmsListener;
    }

    public void unSubscribeFromEvents() {

        log.info("Starting to Shutdown the Listener...");

        if (!stopIssued && jmsConnectionFactory != null) {
            // To prevent multiple components executing stop at the same time,
            // we are checking if a shutdown triggered by a previous thread is in progress.
            synchronized (lock) {
                if (!stopIssued) {
                    stopIssued = true;
                    log.debug("Stopping JMS Listeners");
                    for (JMSListener jmsListener : jmsListenerList) {
                        jmsListener.stopListener();
                    }
                    log.debug("JMS Listeners Stopped");
                    jmsConnectionFactory.stop();
                    log.debug("JMS Connection Factory Stopped");
                }
            }
        }
    }

}
