package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.dto.ThrottleDataDTO;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    String authorizedUser;
    String resourceLevelTier;
    String apiContext;
    String apiVersion;
    String appTenant;
    String apiTenant;
    String apiName;
    String appId;
    private ThrottleDataDTO throttleDataDTO;
    Gson gson;
    private AuthenticationContext authenticationContext;

    public DataProcessAndPublishingAgent() {
        this.throttleDataDTO = new ThrottleDataDTO();
        this.gson = new Gson();

    }

    /**
     * This method will clean data references. This method should call whenever we return data process and publish
     * agent back to pool. Every time when we add new property we need to implement cleaning logic as well.
     */
    public void clearDataReference() {
        this.authenticationContext = null;
        this.messageContext = null;
        this.applicationLevelThrottleKey = null;
        this.applicationLevelTier = null;
        this.apiLevelThrottleKey = null;
        this.applicationLevelTier = null;
        this.subscriptionLevelThrottleKey = null;
        this.subscriptionLevelTier = null;
        this.resourceLevelThrottleKey = null;
        this.resourceLevelTier = null;
        this.authorizedUser = null;
        this.apiContext = null;
        this.apiVersion = null;
        this.appTenant = null;
        this.apiTenant = null;
        this.appId = null;
        this.apiName = null;
        this.throttleDataDTO.cleanDTO();
    }

    /**
     * This method will use to set message context.
     */
    public void setDataReference(String applicationLevelThrottleKey, String applicationLevelTier,
                                 String apiLevelThrottleKey, String apiLevelTier,
                                 String subscriptionLevelThrottleKey, String subscriptionLevelTier,
                                 String resourceLevelThrottleKey, String resourceLevelTier,
                                 String authorizedUser, String apiContext, String apiVersion, String appTenant,
                                 String apiTenant, String appId, MessageContext messageContext,
                                 AuthenticationContext authenticationContext){
        if(resourceLevelTier==null && apiLevelTier!=null){
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
        this.apiContext = apiContext;
        this.apiVersion = apiVersion;
        this.appTenant = appTenant;
        this.apiTenant = apiTenant;
        this.appId = appId;
        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        this.apiName = APIUtil.getAPINamefromRESTAPI(apiName);
    }

    public void run() {
        //TODO implement logic to get message details from message context

        String propertiesMap = "{\n" +
                "  \"name\": \"org.wso2.throttle.request.stream\",\n" +
                "  \"version\": \"1.0.0\"}";
        String remoteIP = null;
        Object object = messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (object != null) {
            remoteIP = (String) ((TreeMap) object).get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        }

        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) messageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        if (remoteIP != null && remoteIP.length() > 0) {
            throttleDataDTO.setClientIP(remoteIP);
        }

        TreeMap transportHeaderMap = ((TreeMap) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        if (transportHeaderMap != null && transportHeaderMap.size() > 0) {
            throttleDataDTO.setTransportHeaders((Map<String, String>) transportHeaderMap);
        }
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        //todo Added some parameters
        //TODO get all query parameters and add it to throttleDataDTO
        //Map queryParametersMap = new HashMap<String, String>();
        //queryParametersMap.put("remoteIp", remoteIP);
        //throttleDataDTO.setQueryParameters(queryParametersMap);

        propertiesMap = gson.toJson(throttleDataDTO);

        //this parameter will be used to capture message size and pass it to calculation logic
        int messageSizeInBytes = 0;
        if (authenticationContext.isContentAwareTierPresent()) {
            //this request can match with with bandwidth policy. So we need to get message size.
            Object obj = ((TreeMap) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty("TRANSPORT_HEADERS")).get("Content-Length");
            if (obj != null) {
                messageSizeInBytes = Integer.parseInt(obj.toString());
            } else {
                //TODO write logic
            }

        }

        Object[] objects = new Object[]{messageContext.getMessageID(), this.applicationLevelThrottleKey, this.applicationLevelTier,
                this.apiLevelThrottleKey, this.apiLevelTier,
                this.subscriptionLevelThrottleKey, this.subscriptionLevelTier,
                this.resourceLevelThrottleKey, this.resourceLevelTier,
                this.authorizedUser, this.apiContext, this.apiVersion, this.appTenant, this.apiTenant, this.appId, this.apiName, propertiesMap};

        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);
    }

}
