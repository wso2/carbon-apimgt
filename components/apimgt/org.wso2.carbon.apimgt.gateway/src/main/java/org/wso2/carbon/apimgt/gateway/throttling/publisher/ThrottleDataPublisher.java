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

package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.throttling.util.ThrottlingRunTimeException;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.concurrent.*;

/**
 * Throttle data publisher class is here to publish throttle data to global policy engine.
 * This can publish data according to defined protocol. Protocol can be thrift or binary.
 * When we use this for high concurrency usecases proper tuning is mandatory.
 */
public class ThrottleDataPublisher {
    public static final ThrottleDataPublisherPool dataPublisherPool = ThrottleDataPublisherPool.getInstance();

    public static final Log log = LogFactory.getLog(ThrottleDataPublisher.class);

    public static DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    static volatile DataPublisher dataPublisher = null;

    Executor executor;

    /**
     * This method will initialize throttle data publisher. Inside this we will start executor and initialize data
     * publisher which we used to publish throttle data.
     */
    public ThrottleDataPublisher() {
        try {
            executor = new DataPublisherThreadPoolExecutor(200, 500, 100, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<Runnable>() {
                    });
            dataPublisher = new DataPublisher("thrift", "tcp://localhost:7611", "ssl://localhost:7711", "admin", "admin");
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
     * This method used to pass message context and let it run within separate thread.
     *
     * @param messageContext is message context object that holds
     */
    public void publishNonThrottledEvent(MessageContext messageContext) {
        //DataProcessAndPublishingAgent agent = null;
        //long start = System.currentTimeMillis();
        //String remoteIP = "127.0.0.1";
        //(String) ((TreeMap) synCtx.getProperty(org.apache.axis2.context.MessageContext
        //.TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        //if (remoteIP != null && !remoteIP.isEmpty()) {
        //    if (remoteIP.indexOf(",") > 0) {
        //        remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
        //    }
        //} else {
        //    remoteIP = (String) synCtx.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        // }

        //todo Added some dummy parameters
        //Map propertiesMap = new HashMap<String, String>();
        //propertiesMap.put("remoteIp", remoteIP);
        //propertiesMap.put("roleID", subscriptionLevelTier);

        //this parameter will be used to capture message size and pass it to calculation logic
                        /*int messageSizeInBytes = 0;
                        //getThrottleData.isContentAwareTierPresent
                        if (authContext.isContentAwareTierPresent()) {
                            //this request can match with with bandwidth policy. So we need to get message size.
                            httpVerb = verbInfoDTO.getHttpVerb();
                            Object obj = ((TreeMap) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                                    getProperty("TRANSPORT_HEADERS")).get("Content-Length");
                            if (obj != null) {
                                messageSizeInBytes = Integer.parseInt(obj.toString());
                            }

                        }*/

                        /*Object[] objects = new Object[]{synCtx.getMessageID(), applicationLevelThrottleKey,
                                subscriptionLevelThrottleKey, applicationLevelTier, subscriptionLevelTier,
                                authorizedUser, propertiesMap};
                        *///After publishing events return true
        //log.info("##########################################Publishing event");
        try {
            DataProcessAndPublishingAgent agent = dataPublisherPool.get();
            agent.setDataReference(messageContext);
            executor.execute(agent);
            //log.info("##########################################Time Taken:"+(System.currentTimeMillis() -start));

        } catch (Exception e) {
            throw new ThrottlingRunTimeException("Error while publishing throttling events to global policy server");
        }
    }

    /**
     * This class will act as thread pool executor and after executing each thread it will return runnable
     * object back to pool. This implementation specifically used to minimize number of objectes created during
     * runtime. In this queuing strategy the submitted task will wait in the queue if the corePoolsize theads are
     * busy and the task will be allocated if any of the threads become idle.Thus ThreadPool will always have number
     * of threads running  as mentioned in the corePoolSize.
     * LinkedBlockingQueue without the capacity can be used for this queuing strategy.If the corePoolsize of the
     * threadpool is less and there are more number of time consuming task were submitted,there is more possibility
     * that the task has to wait in the queue for more time before it is run by any of the ideal thread.
     * So tuning core pool size is something we need to tune properly.
     * Also no task will be rejected in Threadpool until the threadpool was shutdown.
     */
    private class DataPublisherThreadPoolExecutor extends ThreadPoolExecutor {
        public DataPublisherThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                               TimeUnit unit, LinkedBlockingDeque<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        protected void afterExecute(java.lang.Runnable r, java.lang.Throwable t) {
            try {
                DataProcessAndPublishingAgent agent = (DataProcessAndPublishingAgent) r;
                agent.setDataReference(null);
                ThrottleDataPublisher.dataPublisherPool.release(agent);
            } catch (Exception e) {
                log.error("Error while returning Throttle data publishing agent back to pool" + e.getMessage());
            }
        }
    }
}

