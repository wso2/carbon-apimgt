package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
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
    String applicationLevelThrottleKey;
    String applicationLevelTier;
    String apiLevelThrottleKey;
    String apiLevelTier;
    String subscriptionLevelThrottleKey;
    String subscriptionLevelTier;
    String resourceLevelThrottleKey;
    String resourceLevelTier;
    String authorizedUser;


    public DataProcessAndPublishingAgent() {

    }
    public void processAndPublishEvent(){

    }

    /**
     * This method will use to set message context.
     * @param messageContext
     */
    public void setDataReference(String applicationLevelThrottleKey, String applicationLevelTier,
                                 String apiLevelThrottleKey, String apiLevelTier,
                                 String subscriptionLevelThrottleKey, String subscriptionLevelTier,
                                 String resourceLevelThrottleKey, String resourceLevelTier,
                                 String authorizedUser, MessageContext messageContext){
        if(resourceLevelTier==null && apiLevelTier!=null){
            resourceLevelTier = apiLevelTier;
        }
        this.messageContext =messageContext;
        this.applicationLevelThrottleKey =applicationLevelThrottleKey;
        this.applicationLevelTier = applicationLevelTier;
        this.apiLevelThrottleKey = apiLevelThrottleKey;
        this.applicationLevelTier = apiLevelTier;
        this.subscriptionLevelThrottleKey = subscriptionLevelThrottleKey;
        this.subscriptionLevelTier = subscriptionLevelTier;
        this.resourceLevelThrottleKey = resourceLevelThrottleKey;
        this.resourceLevelTier =resourceLevelTier;
        this.authorizedUser = authorizedUser;
    }

    @Override
    public void run() {
        //TODO implement logic to get message details from message context
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        String authorizedUser = authContext.getUsername();
        String applicationLevelThrottleKey = authContext.getApplicationId() + ":" + authorizedUser;;
        String applicationLevelThrottleTier = authContext.getApplicationTier();
        String apiLevelThrottleKey = "";//authContext.getThrottlingDataList().get(0);
        if(authContext.getThrottlingDataList() != null && authContext.getThrottlingDataList().get(0) !=null){
            apiLevelThrottleKey = authContext.getThrottlingDataList().get(0);
        }
        else {
            apiLevelThrottleKey="";
        }
        String apiLevelThrottleTier = authContext.getApiTier();
        String propertiesMap = "{\n" +
                "  \"name\": \"org.wso2.throttle.request.stream\",\n" +
                "  \"version\": \"1.0.0\"}";

        Object[] objects = new Object[]{messageContext.getMessageID(), this.applicationLevelThrottleKey,this.applicationLevelTier,
                this.apiLevelThrottleKey, this.apiLevelTier,
                this.subscriptionLevelThrottleKey, this.subscriptionLevelTier,
                this.resourceLevelThrottleKey, this.resourceLevelTier,
                this.authorizedUser, propertiesMap};

        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);

    }

}
