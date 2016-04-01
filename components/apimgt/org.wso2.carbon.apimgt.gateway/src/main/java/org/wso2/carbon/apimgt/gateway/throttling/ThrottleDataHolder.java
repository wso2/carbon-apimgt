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

package org.wso2.carbon.apimgt.gateway.throttling;

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.throttling.util.jms.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will hold throttle data per given node. All throttle handler objects should refer values from this.
 * When throttle data holder initialize it should read complete throttle decision table from global policy engine
 * via web service calls. In addition to that it should subscribe to topic and listen throttle updates.
 *
 */
public class ThrottleDataHolder {
    private static final Log log = LogFactory.getLog(JMSMessageListener.class);

    public void init(){
        //First do web service call and update map.
        //Then init JMS listner to listen que and update it.
        //Following method will initialize JMS listnet and listen all updates and keep throttle data map up to date
        subscribeForJmsEvents();

    }


    public Map<String, String> getThrottleDataMap() {
        return throttleDataMap;
    }

    public void setThrottleDataMap(Map<String, String> throttleDataMap) {
        this.throttleDataMap = throttleDataMap;
    }

    Map<String,String> throttleDataMap =  new ConcurrentHashMap();

    public boolean isThrottled(String key){
        if(null!=this.throttleDataMap.get(key)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * This method will used to subscribe JMS and update throttle data map.
     */
    public void subscribeForJmsEvents() {
        Properties properties = new Properties();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            properties.load(classLoader.getResourceAsStream("mb.properties"));
            Hashtable<String, String> parameters = new Hashtable<String, String>();
            for (final String name : properties.stringPropertyNames()) {
                parameters.put(name, properties.getProperty(name));
            }

            String destination = "throttleData";
            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(parameters, "Siddhi-JMS-Consumer");
            Map<String, String> messageConfig = new HashMap<String, String>();
            messageConfig.put(JMSConstants.PARAM_DESTINATION, destination);
            int minThreadPoolSize = 4;
            int maxThreadPoolSize = 4;
            int keepAliveTimeInMillis = 1000;
            int jobQueueSize = 1000;
            JMSTaskManager jmsTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory,
                    "Siddhi-JMS-Consumer", new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize,
                            keepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                    "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
            jmsTaskManager.setJmsMessageListener(new JMSMessageListener(this));

            JMSListener jmsListener = new JMSListener("Siddhi-JMS-Consumer" + "#" + destination,
                    jmsTaskManager);
            jmsListener.startListener();
            log.info("Starting jms topic consumer thread...");

        } catch (IOException e) {
            log.error("Cannot read properties file from resources. " + e.getMessage(), e);
        }
    }

}
