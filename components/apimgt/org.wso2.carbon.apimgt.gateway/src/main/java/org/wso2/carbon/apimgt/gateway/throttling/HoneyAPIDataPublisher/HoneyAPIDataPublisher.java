package org.wso2.carbon.apimgt.gateway.throttling.HoneyAPIDataPublisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HoneyAPIDataPublisher{
    private  static HoneyDataPublisherPool dataPublisherPool;

    public static final Log log = LogFactory.getLog(HoneyAPIDataPublisher.class);

    static DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    private static volatile DataPublisher dataPublisher = null;

    private Executor executor;

    public HoneyAPIDataPublisher(){
        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance().getThrottleProperties();
        if (throttleProperties != null) {
            ThrottleProperties.DataPublisher dataPublisherConfiguration = ServiceReferenceHolder.getInstance()
                    .getThrottleProperties().getDataPublisher();
            if (dataPublisherConfiguration != null && dataPublisherConfiguration.isEnabled()) {
                dataPublisherPool = HoneyDataPublisherPool.getInstance();
                ThrottleProperties.DataPublisherThreadPool dataPublisherThreadPoolConfiguration = ServiceReferenceHolder
                        .getInstance().getThrottleProperties().getDataPublisherThreadPool();

                try {
                    executor = new DataPublisherThreadPoolExecutor(dataPublisherThreadPoolConfiguration.getCorePoolSize(),
                            dataPublisherThreadPoolConfiguration.getMaximumPoolSize(), dataPublisherThreadPoolConfiguration
                            .getKeepAliveTime(),
                            TimeUnit
                                    .SECONDS,
                            new LinkedBlockingDeque<Runnable>() {
                            });
                    dataPublisher = new DataPublisher(dataPublisherConfiguration.getType(), dataPublisherConfiguration
                            .getReceiverUrlGroup(), dataPublisherConfiguration.getAuthUrlGroup(), dataPublisherConfiguration
                            .getUsername(),
                            dataPublisherConfiguration.getPassword());

                } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException | DataEndpointAuthenticationException | TransportException e) {
                    log.error("Error in initializing binary data-publisher to send requests to global throttling engine " +
                            e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Initiating the data publishing from GW to TM when invoking the HoneyPot API
     */
    public void publishEvent(long currentTime, String messageId, String apiMethod, String headerSet, String messageBody,
                             String clientIp) {
        try {
            if (dataPublisherPool != null) {
                HoneyAPIDataProcessAndPublishAgent agent = dataPublisherPool.get();
                agent.setDataReference(currentTime,messageId,apiMethod, headerSet,messageBody, clientIp);
                if (log.isDebugEnabled()) {
                    log.debug("Publishing HoneyAPI data from gateway to traffic-manager for started at "
                            + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
                }
                executor.execute(agent);
                if (log.isDebugEnabled()) {
                    log.debug("Publishing throttle data from gateway to traffic-manager for ended at "
                            + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
                }
            } else {
                log.debug("HoneyPot data publisher pool is not initialized.");
            }
        } catch (Exception e) {
            log.error("Error while publishing data to TM", e);
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
        DataPublisherThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                        TimeUnit unit, LinkedBlockingDeque<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        protected void afterExecute(java.lang.Runnable r, java.lang.Throwable t) {
            try {
                HoneyAPIDataProcessAndPublishAgent agent = (HoneyAPIDataProcessAndPublishAgent) r;
                //agent.setDataReference(null);
                HoneyAPIDataPublisher.dataPublisherPool.release(agent);
            } catch (Exception e) {
                log.error("Error while returning HoneyPotAPI data publishing agent back to pool" + e.getMessage());
            }
        }
    }
}

