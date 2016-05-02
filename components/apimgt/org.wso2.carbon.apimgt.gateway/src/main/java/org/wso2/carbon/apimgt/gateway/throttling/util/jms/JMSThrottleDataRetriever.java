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

package org.wso2.carbon.apimgt.gateway.throttling.util.jms;


import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.io.IOException;
import java.util.*;

public class JMSThrottleDataRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(JMSThrottleDataRetriever.class);
    private static final ThrottleProperties.JMSConnectionProperties jmsConnectionProperties =
            ServiceReferenceHolder.getInstance().getThrottleProperties().getJmsConnectionProperties();
    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Starting JMS based throttle data retrieving process.");
        }
        subscribeForJmsEvents();
    }

    /**
     * This method will used to subscribe JMS and update throttle data map.
     * Then this will listen to topic updates and check all update and updare throttle data map accordingly.
     */
    public void subscribeForJmsEvents() {
        Properties properties;
        Hashtable<String, String> parameters = new Hashtable<String, String>();
        try {
            if (jmsConnectionProperties.getJmsConnectionProperties().isEmpty()){
                properties = new Properties();
                ClassLoader classLoader = getClass().getClassLoader();
                properties.load(classLoader.getResourceAsStream("mb.properties"));
            }
            else{
                properties = jmsConnectionProperties.getJmsConnectionProperties();
            }
            for (final String name : properties.stringPropertyNames()) {
                parameters.put(name, properties.getProperty(name));
            }
            String destination = jmsConnectionProperties.getDestination();
            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(parameters, "Siddhi-JMS-Consumer");
            Map<String, String> messageConfig = new HashMap<String, String>();
            messageConfig.put(JMSConstants.PARAM_DESTINATION, destination);
            int minThreadPoolSize = jmsConnectionProperties.getJmsTaskManagerProperties().getMinThreadPoolSize();
            int maxThreadPoolSize = jmsConnectionProperties.getJmsTaskManagerProperties().getMaxThreadPoolSize();
            int keepAliveTimeInMillis = jmsConnectionProperties.getJmsTaskManagerProperties()
                    .getKeepAliveTimeInMillis();
            int jobQueueSize = jmsConnectionProperties.getJmsTaskManagerProperties().getJobQueueSize();
            JMSTaskManager jmsTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory,
                    "Siddhi-JMS-Consumer", new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize,
                            keepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                            "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
            jmsTaskManager.setJmsMessageListener(new JMSMessageListener(ServiceReferenceHolder.getInstance().getThrottleDataHolder()));

            JMSListener jmsListener = new JMSListener("Siddhi-JMS-Consumer" + "#" + destination,
                    jmsTaskManager);
            jmsListener.startListener();
            log.info("Starting jms topic consumer thread...");

        } catch (IOException e) {
            log.error("Cannot read properties file from resources. " + e.getMessage(), e);
        }
    }

    public void startJMSThrottleDataRetriever() {
        new Timer().schedule(this, ServiceReferenceHolder.getInstance().getThrottleProperties()
                .getJmsConnectionProperties().getInitialDelay());
    }
}
