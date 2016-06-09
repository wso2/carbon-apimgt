package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

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
    private AuthenticationContext authenticationContext;

    public DataProcessAndPublishingAgent() {

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
                                 AuthenticationContext authenticationContext){
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
    }

    public void run() {

        String remoteIP = null;
        JSONObject jsonObMap = new JSONObject();

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        //Set transport headers of the message
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if(transportHeaderMap != null){
             remoteIP = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        }

        //Setting IP of the client
        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        if(remoteIP !=null && remoteIP.length()>0) {
            jsonObMap.put("ip", APIUtil.ipToLong(remoteIP));
        }

        if (ServiceReferenceHolder.getInstance().getThrottleProperties().isEnableHeaderConditions()) {
            jsonObMap.putAll(transportHeaderMap);
        }
        //Setting query parameters
        if (ServiceReferenceHolder.getInstance().getThrottleProperties().isEnableQueryParamConditions()) {

            String queryString = (String) axis2MessageContext.getProperty(NhttpConstants.REST_URL_POSTFIX);
            if (!StringUtils.isEmpty(queryString)) {
                if (queryString.indexOf("?") > -1) {
                    queryString = queryString.substring(queryString.indexOf("?") + 1);
                }
                String[] queryParams = queryString.split("&");
                Map<String, String> queryParamsMap = new HashMap<String, String>();
                String[] queryParamArr;
                String queryParamName, queryParamValue = "";
                for (String queryParam : queryParams) {
                    queryParamArr = queryParam.split("=");
                    if (queryParamArr.length == 2) {
                        queryParamName = queryParamArr[0];
                        queryParamValue = queryParamArr[1];
                    } else {
                        queryParamName = queryParamArr[0];
                    }
                    queryParamsMap.put(queryParamName, queryParamValue);
                    jsonObMap.put(queryParamName, queryParamValue);
                }
            }
        }

        //Publish jwt claims
        if (ServiceReferenceHolder.getInstance().getThrottleProperties().isEnableJwtConditions()) {

            if (authenticationContext.getCallerToken() != null) {
                //take first part
                String[] jwtTokenArray = authenticationContext.getCallerToken().split(Pattern.quote("."));
                // decode  JWT part
                try {
                    byte[] jwtByteArray = Base64.decodeBase64(jwtTokenArray[1].getBytes("UTF-8"));
                    String jwtHeader = new String(jwtByteArray, "UTF-8");
                    JSONParser parser = new JSONParser();
                    JSONObject jwtHeaderOb = (JSONObject) parser.parse(jwtHeader);
                    jsonObMap.putAll(jwtHeaderOb);
                } catch (UnsupportedEncodingException e) {
                    log.info("Error while decoding jwt header", e);
                } catch (ParseException e) {
                    log.info("Error while parsing jwt header", e);
                }
            }
        }

        //this parameter will be used to capture message size and pass it to calculation logic
        long messageSizeInBytes = 0;
        if (authenticationContext.isContentAwareTierPresent()) {
            //this request can match with with bandwidth policy. So we need to get message size.
            Object obj = transportHeaderMap.get("Content-Length");
            if (obj != null) {
                messageSizeInBytes = Integer.parseInt(obj.toString());
            } else {
                try {
                    RelayUtils.buildMessage(axis2MessageContext);
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
            jsonObMap.put("messageSize", messageSizeInBytes);
        }

        Object[] objects = new Object[]{messageContext.getMessageID(),
                                        this.applicationLevelThrottleKey, this.applicationLevelTier,
                                        this.apiLevelThrottleKey, this.apiLevelTier,
                                        this.subscriptionLevelThrottleKey, this.subscriptionLevelTier,
                                        this.resourceLevelThrottleKey, this.resourceLevelTier,
                                        this.authorizedUser, this.apiContext, this.apiVersion,
                                        this.appTenant, this.apiTenant, this.appId, this.apiName , jsonObMap.toString()};
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                                                                        System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);
    }

}
