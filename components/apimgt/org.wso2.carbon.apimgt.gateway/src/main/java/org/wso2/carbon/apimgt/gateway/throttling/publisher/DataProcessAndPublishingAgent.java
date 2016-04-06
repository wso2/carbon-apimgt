package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.databridge.agent.DataPublisher;

public class DataProcessAndPublishingAgent implements Runnable{
    private static final Log log = LogFactory.getLog(DataProcessAndPublishingAgent.class);

    private static String streamID = "org.wso2.throttle.request.stream:1.0.0";
    private MessageContext messageContext;
    private DataPublisher dataPublisher;
    public DataProcessAndPublishingAgent() {

    }
    public void processAndPublishEvent(MessageContext messageContext, DataPublisher dataPublisher){
        this.dataPublisher = dataPublisher;
        this.messageContext =messageContext;
    }

    @Override
    public void run() {

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
