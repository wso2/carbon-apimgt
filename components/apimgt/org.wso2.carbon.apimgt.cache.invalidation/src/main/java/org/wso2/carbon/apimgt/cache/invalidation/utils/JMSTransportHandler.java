/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.cache.invalidation.utils;

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.cache.invalidation.APIMgtCacheInvalidationListener;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSConnectionFactory;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSListener;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSTaskManager;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSTaskManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class JMSTransportHandler {

    private static final Log log = LogFactory.getLog(JMSTransportHandler.class);
    private CacheInvalidationConfiguration cacheInvalidationConfiguration;
    private JMSConnectionFactory jmsConnectionFactory;
    private JMSListener jmsListenerForCacheInvalidationTopic;

    public JMSTransportHandler(CacheInvalidationConfiguration cacheInvalidationConfiguration) {

        this.cacheInvalidationConfiguration = cacheInvalidationConfiguration;
    }

    /**
     * This method is used to subscribe to JMS topics and receive JMS messages
     */
    public void subscribeForJmsEvents() {

        Properties properties;
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        if (cacheInvalidationConfiguration.getJmsConnectionParameters().isEmpty()) {
            properties = new Properties();
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream resourceStream = classLoader.getResourceAsStream(ListenerConstants.MB_PROPERTIES)) {
                properties.load(resourceStream);
            } catch (IOException e) {
                log.error("Cannot read properties file from resources. " + e.getMessage(), e);
            }
        } else {
            properties = cacheInvalidationConfiguration.getJmsConnectionParameters();
        }

        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name, properties.getProperty(name));
        }
        jmsConnectionFactory = new JMSConnectionFactory(parameters,
                ListenerConstants
                        .CONNECTION_FACTORY_NAME);
        int minThreadPoolSize = 20;
        int maxThreadPoolSize = 100;
        int keepAliveTimeInMillis = 1000;
        int jobQueueSize = 10;
        //Listening to throttleData topic
        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, cacheInvalidationConfiguration.getCacheInValidationTopic());
        JMSTaskManager jmsTaskManagerForThrottleDataTopic = JMSTaskManagerFactory
                .createTaskManagerForService(jmsConnectionFactory,
                        ListenerConstants.CONNECTION_FACTORY_NAME,
                        new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize,
                                keepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                                "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManagerForThrottleDataTopic
                .setMessageListener(new APIMgtCacheInvalidationListener(cacheInvalidationConfiguration));

        jmsListenerForCacheInvalidationTopic = new JMSListener(ListenerConstants.CONNECTION_FACTORY_NAME
                + "#" + cacheInvalidationConfiguration.getCacheInValidationTopic(), jmsTaskManagerForThrottleDataTopic);
        jmsListenerForCacheInvalidationTopic.startListener();
        log.info("Starting jms topic consumer thread for the Global Cache Invalidation topic...");
    }

    public void stopJMSListener() {
        if (jmsListenerForCacheInvalidationTopic != null){
            jmsListenerForCacheInvalidationTopic.stopListener();
            log.debug("JMS Listeners Stopped");
        }
        if (jmsConnectionFactory != null){
            jmsConnectionFactory.stop();
            log.debug("JMS ConnectionFactory Stopped");
        }
    }

}
