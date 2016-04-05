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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.gateway.throttling.util.jms.*;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will hold throttle data per given node. All throttle handler objects should refer values from this.
 * When throttle data holder initialize it should read complete throttle decision table from global policy engine
 * via web service calls. In addition to that it should subscribe to topic and listen throttle updates.
 */
public class ThrottleDataHolder implements Runnable{
    private static final Log log = LogFactory.getLog(ThrottleDataHolder.class);
    private DataPublisher dataPublisher = null;
    private String streamID;

    public void start() {
        //First do web service call and update map.
        //Then init JMS listner to listen que and update it.
        //Following method will initialize JMS listnet and listen all updates and keep throttle data map up to date
        //loadThrottleDecisionsFromWebService();
        subscribeForJmsEvents();
        initDataPublisher();
    }

    public Map<String, String> getThrottleDataMap() {
        return throttleDataMap;
    }

    public void setThrottleDataMap(Map<String, String> throttleDataMap) {
        this.throttleDataMap = throttleDataMap;
    }

    Map<String, String> throttleDataMap = new ConcurrentHashMap();

    public boolean isThrottled(String key) {
        if (null != this.throttleDataMap.get(key)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method will used to subscribe JMS and update throttle data map.
     */
    public void subscribeForJmsEvents() {
        for(int i=1; i<10000 ; i++){
            String str = "test"+i;
            this.throttleDataMap.put( str,"throttled");
        }
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

    public void sendToGlobalThrottler(Object[] throttleRequest) {
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, throttleRequest);
        dataPublisher.tryPublish(event);
    }

    //todo exception handling

    /**
     * This method will initialize data publisher and this data publisher will be used to push events to central policy
     * server.
     */
    private void initDataPublisher() {
        try {
            dataPublisher = new DataPublisher("Binary", "tcp://localhost:9611", "ssl://localhost:9711", "admin", "admin");
            streamID = DataBridgeCommonsUtils.generateStreamId("org.wso2.throttle.request.stream", "1.0.0");
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (DataEndpointException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (DataEndpointConfigurationException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (DataEndpointAuthenticationException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        } catch (TransportException e) {
            log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                    e.getMessage(), e);
        }

    }

    /**
     * This method will retrieve throttled events from service deployed in global policy server.
     * @return String object array which contains throttled keys.
     */
    private String[] retrieveThrottlingData() {

        try {
            String url = "http://localhost:9763/throttle/data/v1/throttleAsString";

            HttpGet method = new HttpGet(url);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(method);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return responseString.split(",");
            }

        } catch (IOException e) {
            log.error("Exception when retrieving throttling data from remote endpoint ", e);
        }

        return null;

    }


    /**
     *This method will call throttle data web service deployed in central plocy engine at server loading time.
     * Then it will update local throttle data map with the results obtained. This need to be fine tuned as large
     * number of results can slow down web service call. However this need to be controlled from server side and
     * this client will add all recieved events to local map. Even if we missed few events from this call it will
     * eventually update as missed events go to global policy engine and decision will be anyway pushed to topic and
     * all subscriber will notify it
     */
    public void loadThrottleDecisionsFromWebService() {
        String[] throttleKeyArray = retrieveThrottlingData();
        if (throttleKeyArray != null && throttleKeyArray.length > 0) {
            for (String throttleKey : throttleKeyArray) {
                this.getThrottleDataMap().put(throttleKey, "throttled");
            }
        }
    }

    public void run() {
        start();
    }

    public void startThrottler() {
        new Thread(this).start();
    }
}
