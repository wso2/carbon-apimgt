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

package org.wso2.carbon.apimgt.jms.listener.utils;


import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class JMSTransportHandler {
    private static final Log log = LogFactory.getLog(JMSTransportHandler.class);
    private ThrottleProperties.JMSConnectionProperties jmsConnectionProperties;
    private JMSConnectionFactory jmsConnectionFactory;
    private JMSListener jmsListenerForThrottleDataTopic;
    private JMSListener jmsListenerForTokenRevocationTopic;
    private boolean stopIssued = false;
    private static final Object lock = new Object();

    public JMSTransportHandler() {
        if (ServiceReferenceHolder.getInstance().getAPIMConfiguration() != null) {
            jmsConnectionProperties =
                    ServiceReferenceHolder.getInstance().getAPIMConfiguration().getThrottleProperties()
                            .getJmsConnectionProperties();
        }
    }

    /**
     * This method is used to subscribe to JMS topics and receive JMS messages
     */
    public void subscribeForJmsEvents() {
        Properties properties;
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        if (jmsConnectionProperties.getJmsConnectionProperties().isEmpty()) {
            properties = new Properties();
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream resourceStream = classLoader.getResourceAsStream(ListenerConstants.MB_PROPERTIES)) {
                properties.load(resourceStream);
            } catch (IOException e) {
                log.error("Cannot read properties file from resources. " + e.getMessage(), e);
            }
        } else {
            properties = jmsConnectionProperties.getJmsConnectionProperties();
        }

        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name, properties.getProperty(name));
        }
        jmsConnectionFactory = new JMSConnectionFactory(parameters,
                ListenerConstants
                        .CONNECTION_FACTORY_NAME);
        int minThreadPoolSize = jmsConnectionProperties.getJmsTaskManagerProperties().getMinThreadPoolSize();
        int maxThreadPoolSize = jmsConnectionProperties.getJmsTaskManagerProperties().getMaxThreadPoolSize();
        int keepAliveTimeInMillis = jmsConnectionProperties.getJmsTaskManagerProperties()
                .getKeepAliveTimeInMillis();
        int jobQueueSize = jmsConnectionProperties.getJmsTaskManagerProperties().getJobQueueSize();

        //Listening to throttleData topic
        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, JMSConstants.TOPIC_THROTTLE_DATA);
        JMSTaskManager jmsTaskManagerForThrottleDataTopic = JMSTaskManagerFactory
                .createTaskManagerForService(jmsConnectionFactory,
                        ListenerConstants.CONNECTION_FACTORY_NAME,
                        new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize,
                                keepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                                "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManagerForThrottleDataTopic.setJmsMessageListener(new JMSMessageListener());

        jmsListenerForThrottleDataTopic = new JMSListener(ListenerConstants.CONNECTION_FACTORY_NAME
                + "#" + JMSConstants.TOPIC_THROTTLE_DATA, jmsTaskManagerForThrottleDataTopic);
        jmsListenerForThrottleDataTopic.startListener();
        log.info("Starting jms topic consumer thread for the throttleData topic...");

        //Listening to tokenRevocation topic
        messageConfig.put(JMSConstants.PARAM_DESTINATION, JMSConstants.TOPIC_TOKEN_REVOCATION);
        JMSTaskManager jmsTaskManagerForTokenRevocationTopic = JMSTaskManagerFactory.createTaskManagerForService(
                jmsConnectionFactory,
                ListenerConstants.CONNECTION_FACTORY_NAME,
                new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize,
                        keepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                        "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManagerForTokenRevocationTopic.setJmsMessageListener(new JMSMessageListener());

        jmsListenerForTokenRevocationTopic = new JMSListener(ListenerConstants.CONNECTION_FACTORY_NAME
                + "#" + JMSConstants.TOPIC_TOKEN_REVOCATION, jmsTaskManagerForTokenRevocationTopic);
        jmsListenerForTokenRevocationTopic.startListener();
        log.info("Starting jms topic consumer thread for the tokenRevocation topic...");
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
                    if (jmsListenerForTokenRevocationTopic != null) {
                        jmsListenerForTokenRevocationTopic.stopListener();
                    }
                    if (jmsListenerForThrottleDataTopic != null) {
                        jmsListenerForThrottleDataTopic.stopListener();
                    }
                    log.debug("JMS Listeners Stopped");
                    jmsConnectionFactory.stop();
                    log.debug("JMS Connection Factory Stopped");
                }
            }
        }
    }

}
