package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.dto.ThrottleDataDTO;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.xsd.Set;

/**
 * This class is responsible for executing data publishing logic. This class implements runnable interface and
 * need to execute using thread pool executor. Primary task of this class it is accept message context as parameter
 * and perform time consuming data extraction and publish event to data publisher. Having data extraction and
 * transformation logic in this class will help to reduce overhead added to main message flow.
 */
public class DataProcessAndPublishingAgent implements Runnable {
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
    private AuthenticationContext authenticationContext;

    public DataProcessAndPublishingAgent() {

    }

    public void processAndPublishEvent() {

    }

    /**
     * This method will use to set message context.
     *
     */
    public void setDataReference(String applicationLevelThrottleKey, String applicationLevelTier,
                                 String apiLevelThrottleKey, String apiLevelTier,
                                 String subscriptionLevelThrottleKey, String subscriptionLevelTier,
                                 String resourceLevelThrottleKey, String resourceLevelTier,
                                 String authorizedUser, MessageContext messageContext,
                                 AuthenticationContext authenticationContext) {
        if(StringUtils.isEmpty(resourceLevelTier) && apiLevelTier!=null){
            resourceLevelTier = apiLevelTier;
            resourceLevelThrottleKey = apiLevelThrottleKey;
        }
        this.authenticationContext = authenticationContext;
        this.messageContext = messageContext;
        this.applicationLevelThrottleKey = applicationLevelThrottleKey;
        this.applicationLevelTier = applicationLevelTier;
        this.apiLevelThrottleKey = apiLevelThrottleKey;
        this.applicationLevelTier = apiLevelTier;
        this.subscriptionLevelThrottleKey = subscriptionLevelThrottleKey;
        this.subscriptionLevelTier = subscriptionLevelTier;
        this.resourceLevelThrottleKey = resourceLevelThrottleKey;
        this.resourceLevelTier = resourceLevelTier;
        this.authorizedUser = authorizedUser;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void run() {
        //TODO implement logic to get message details from message context
        ThrottleDataDTO throttleDataDTO = new ThrottleDataDTO();

        String propertiesMap = "{\n" +
                "  \"name\": \"org.wso2.throttle.request.stream\",\n" +
                "  \"version\": \"1.0.0\"}";
        String remoteIP = null;
        Object object = messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if(object!=null){
             remoteIP= (String) ((TreeMap) object).get(APIMgtGatewayConstants.X_FORWARDED_FOR) ;
        }

        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) messageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        if(remoteIP !=null && remoteIP.length()>0) {
            throttleDataDTO.setClientIP(remoteIP);
        }
        /**java.util.Set<String> transportHeaderMap = ((TreeMap) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).keySet();
        for(Object key: transportHeaderMap){
            throttleDataDTO.getTransportHeaders().put(key, )
            
        }**/
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        //todo Added some parameters
        Map otherPrameters = new HashMap<String, String>();
        otherPrameters.put("remoteIp", remoteIP);
        throttleDataDTO.setQueryParameters(otherPrameters);
        Gson gson = new Gson();
        propertiesMap = gson.toJson(throttleDataDTO);

        //this parameter will be used to capture message size and pass it to calculation logic
        int messageSizeInBytes = 0;
        if (authenticationContext.isContentAwareTierPresent()) {
            //this request can match with with bandwidth policy. So we need to get message size.
            Object obj = ((TreeMap) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty("TRANSPORT_HEADERS")).get("Content-Length");
            if (obj != null) {
                messageSizeInBytes = Integer.parseInt(obj.toString());
            }

        }
        Object[] objects = new Object[]{messageContext.getMessageID(), this.applicationLevelThrottleKey, this.applicationLevelTier,
                this.apiLevelThrottleKey, this.apiLevelTier,
                this.subscriptionLevelThrottleKey, this.subscriptionLevelTier,
                this.resourceLevelThrottleKey, this.resourceLevelTier,
                this.authorizedUser, propertiesMap};

        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);

    }

}
