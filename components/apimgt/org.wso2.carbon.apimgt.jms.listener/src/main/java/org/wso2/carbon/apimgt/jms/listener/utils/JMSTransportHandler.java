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

import javax.jms.MessageListener;

public class JMSTransportHandler {
    private static final Log log = LogFactory.getLog(JMSTransportHandler.class);
    private ThrottleProperties.JMSConnectionProperties jmsConnectionProperties;
    private JMSConnectionFactory jmsConnectionFactory;
    private JMSListener jmsListenerForThrottleDataTopic;
    private JMSListener jmsListenerForTokenRevocationTopic;
    private JMSListener jmsListenerForCacheInvalidationTopic;
    private JMSListener jmsListenerForKeyManagerTopic;

    private JMSListener jmsListenerForNotificationTopic;
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
        Hashtable<String, String> parameters = new Hashtable<>();
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
        jmsListenerForThrottleDataTopic = createJMSMessageListener(JMSConstants.TOPIC_THROTTLE_DATA, minThreadPoolSize,
                maxThreadPoolSize, keepAliveTimeInMillis, jobQueueSize,new JMSMessageListener());
        jmsListenerForThrottleDataTopic.startListener();
        log.info("Starting jms topic consumer thread for the throttleData topic...");

        //Listening to tokenRevocation topic
        jmsListenerForTokenRevocationTopic = createJMSMessageListener(JMSConstants.TOPIC_TOKEN_REVOCATION, minThreadPoolSize,
                maxThreadPoolSize, keepAliveTimeInMillis, jobQueueSize,new GatewayTokenRevocationMessageListener());
        jmsListenerForTokenRevocationTopic.startListener();
        log.info("Starting jms topic consumer thread for the tokenRevocation topic...");

        //Listening to tokenRevocation topic
        jmsListenerForCacheInvalidationTopic = createJMSMessageListener(JMSConstants.TOPIC_CACHE_INVALIDATION, minThreadPoolSize,
                maxThreadPoolSize, keepAliveTimeInMillis, jobQueueSize,new APIMgtGatewayCacheMessageListener());
        jmsListenerForCacheInvalidationTopic.startListener();
        log.info("Starting jms topic consumer thread for the cacheInvalidation topic...");

        jmsListenerForKeyManagerTopic = createJMSMessageListener(JMSConstants.TOPIC_KEY_MANAGER, minThreadPoolSize,
                maxThreadPoolSize, keepAliveTimeInMillis, jobQueueSize, new KeyManagerJMSMessageListener());
        jmsListenerForKeyManagerTopic.startListener();
        log.info("Starting jms topic consumer thread for the keyManager topic...");
        jmsListenerForNotificationTopic = createJMSMessageListener(JMSConstants.TOPIC_NOTIFICATION, minThreadPoolSize,
                maxThreadPoolSize, keepAliveTimeInMillis, jobQueueSize, new GatewayJMSMessageListener());
        jmsListenerForNotificationTopic.startListener();
        log.info("Starting jms topic consumer thread for the notification topic...");
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
                    if (jmsListenerForTokenRevocationTopic != null) {
                        jmsListenerForTokenRevocationTopic.stopListener();
                    }
                    if (jmsListenerForThrottleDataTopic != null) {
                        jmsListenerForThrottleDataTopic.stopListener();
                    }
                    if (jmsListenerForCacheInvalidationTopic != null) {
                        jmsListenerForCacheInvalidationTopic.stopListener();
                    }
                    if (jmsListenerForKeyManagerTopic != null) {
                        jmsListenerForKeyManagerTopic.stopListener();
                    }
                    if (jmsListenerForNotificationTopic != null) {
                        jmsListenerForNotificationTopic.stopListener();
                    }

                    log.debug("JMS Listeners Stopped");
                    jmsConnectionFactory.stop();
                    log.debug("JMS Connection Factory Stopped");
                }
            }
        }
    }

}
