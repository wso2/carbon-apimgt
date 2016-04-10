package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.databridge.agent.DataPublisher;

/**
 * This class is responsible for executing data publishing logic. This class implements runnable interface and
 * need to execute using thread pool executor. Primary task of this class it is accept message context as parameter
 * and perform time consuming data extraction and publish event to data publisher. Having data extraction and
 * transformation logic in this class will help to reduce overhead added to main message flow.
 */
public class DataProcessAndPublishingAgent implements Runnable{
    private static final Log log = LogFactory.getLog(DataProcessAndPublishingAgent.class);

    private static String streamID = "org.wso2.throttle.request.stream:1.0.0";
    private MessageContext messageContext;
    private DataPublisher dataPublisher = ThrottleDataPublisher.getDataPublisher();
    public DataProcessAndPublishingAgent() {

    }
    public void processAndPublishEvent(){

    }

    /**
     * This method will use to set message context.
     * @param messageContext
     */
    public void setDataReference(MessageContext messageContext){
        this.messageContext =messageContext;
    }

    @Override
    public void run() {
        //TODO implement logic to get message details from message context
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String applicationLevelThrottleKey = "";
        String subscriptionLevelTier = "";
        String applicationLevelTier = "";
        String subscriptionLevelThrottleKey = "";
        String propertiesMap = "";
        String authorizedUser = "";
        Object[] objects = new Object[]{messageContext.getMessageID(), applicationLevelThrottleKey,
                subscriptionLevelThrottleKey, applicationLevelTier, subscriptionLevelTier,
                authorizedUser, propertiesMap};

        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);

    }


}
