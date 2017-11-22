package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.stream.XMLStreamException;

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
    private DataPublisher dataPublisher;



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
    Map<String, String> headersMap;
    private AuthenticationContext authenticationContext;

    public DataProcessAndPublishingAgent() {

        dataPublisher = getDataPublisher();
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
                                 AuthenticationContext authenticationContext) {
        if (!StringUtils.isEmpty(apiLevelTier)) {
            resourceLevelTier = apiLevelTier;
            resourceLevelThrottleKey = apiLevelThrottleKey;
        }
        this.authenticationContext = authenticationContext;
        this.messageContext = messageContext;
        this.applicationLevelThrottleKey = applicationLevelThrottleKey;
        this.applicationLevelTier = applicationLevelTier;
        this.apiLevelThrottleKey = apiLevelThrottleKey;
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


        if (getThrottleProperties().isEnableHeaderConditions()) {
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) axis2MessageContext
                    .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            //Set transport headers of the message. Header Map will be put to the JSON Map which gets transferred
            // to CEP. Since this operation runs asynchronously if we are to get the header Map present in the
            // messageContext a ConcurrentModificationException will be thrown. Reason is at the time of sending the
            // request out, header map is modified by the Synapse layer. It's to avoid this problem a clone of the
            // map is used.
            if (transportHeaderMap != null) {
                this.headersMap = (Map<String, String>) transportHeaderMap.clone();
            }
        }
    }

    public void run() {

        JSONObject jsonObMap = new JSONObject();

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        //Set transport headers of the message
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);


        String remoteIP = GatewayUtils.getIp(axis2MessageContext);

        if (remoteIP != null && remoteIP.length() > 0) {
            jsonObMap.put(APIThrottleConstants.IP, APIUtil.ipToLong(remoteIP));
        }

        //HeaderMap will only be set if the Header Publishing has been enabled.
        if (this.headersMap != null) {
            jsonObMap.putAll(this.headersMap);
        }

        //Setting query parameters
        if (getThrottleProperties().isEnableQueryParamConditions()) {
            Map<String, String> queryParams = GatewayUtils.getQueryParams(axis2MessageContext);
            if (queryParams != null) {
                jsonObMap.putAll(queryParams);
            }

        }

        //Publish jwt claims
        if (getThrottleProperties().isEnableJwtConditions()) {
            if (authenticationContext.getCallerToken() != null) {
                Map assertions = GatewayUtils.getJWTClaims(authenticationContext);
                if (assertions != null) {
                    jsonObMap.putAll(assertions);
                }
            }
        }

        //this parameter will be used to capture message size and pass it to calculation logic
        long messageSizeInBytes = 0;
        if (authenticationContext.isContentAwareTierPresent()) {
            //this request can match with with bandwidth policy. So we need to get message size.
            Object contentLength = null;
            if (transportHeaderMap != null) {
                contentLength = transportHeaderMap.get(APIThrottleConstants.CONTENT_LENGTH);
            }

            if (contentLength != null) {
                messageSizeInBytes = Integer.parseInt(contentLength.toString());
            } else {
                try {
                    buildMessage(axis2MessageContext);
                } catch (IOException ex) {
                    //In case of an exception, it won't be propagated up,and set response size to 0
                    log.error("Error occurred while building the message to" +
                              " calculate the response body size", ex);
                } catch (XMLStreamException ex) {
                    log.error("Error occurred while building the message to calculate the response" +
                              " body size", ex);
                }

                SOAPEnvelope env = messageContext.getEnvelope();
                if (env != null) {
                    SOAPBody soapbody = env.getBody();
                    if (soapbody != null) {
                        byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                        messageSizeInBytes = size.length;
                    }
                }
            }
            jsonObMap.put(APIThrottleConstants.MESSAGE_SIZE, messageSizeInBytes);
        }

        Object[] objects = new Object[]{messageContext.getMessageID(),
                                        this.applicationLevelThrottleKey, this.applicationLevelTier,
                                        this.apiLevelThrottleKey, this.apiLevelTier,
                                        this.subscriptionLevelThrottleKey, this.subscriptionLevelTier,
                                        this.resourceLevelThrottleKey, this.resourceLevelTier,
                                        this.authorizedUser, this.apiContext, this.apiVersion,
                                        this.appTenant, this.apiTenant, this.appId, this.apiName, jsonObMap.toString()};
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                                                                                                      System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);
    }

    protected void buildMessage(org.apache.axis2.context.MessageContext axis2MessageContext) throws IOException,
            XMLStreamException {
        RelayUtils.buildMessage(axis2MessageContext);
    }

    protected ThrottleProperties getThrottleProperties() {
        return ServiceReferenceHolder.getInstance().getThrottleProperties();
    }
    protected DataPublisher getDataPublisher() {
        return ThrottleDataPublisher.getDataPublisher();
    }
}
