package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.event.throttle.core.ThrottlerService;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is implemented to handle
 */
public class CEPBasedThrottleHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(CEPBasedThrottleHandler.class);

    /**
     * The Throttle object - holds all runtime and configuration data
     */
    private volatile Throttle throttle;

    private RoleBasedAccessRateController applicationRoleBasedAccessController;

    /**
     * The key for getting the throttling policy - key refers to a/an [registry] entry
     */
    private String policyKey = null;
    /**
     * The concurrent access control group id
     */
    private String id;
    /**
     * Version number of the throttle policy
     */
    private long version;

    private ThrottlerService throttler = ServiceReferenceHolder.getInstance().getThrottler();

    public CEPBasedThrottleHandler() {
        this.applicationRoleBasedAccessController = new RoleBasedAccessRateController();
    }

    private boolean doRoleBasedAccessThrottlingWithCEP(MessageContext synCtx, ConfigurationContext cc) {
        boolean isThrottled = true;
        String appKey;
        String apiKey;
        String resourceKey;
        String appTier;
        String apiTier;
        String resourceTier = null;
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        String accessToken;
        String consumerKey;
        String authorizedUser;
        String roleID;
        if (authContext != null) {
            //Although the method says getApiKey, what is actually returned is the Bearer header (accessToken)
            accessToken = authContext.getApiKey();
            consumerKey = authContext.getConsumerKey();
            authorizedUser = authContext.getUsername();
            roleID = authContext.getTier();
            appTier = authContext.getApplicationTier();
            appKey = authContext.getApplicationId();

            VerbInfoDTO verbInfoDTO = (VerbInfoDTO) synCtx.getProperty(APIConstants.VERB_INFO_DTO);
            String resourceLevelRoleId = null;
            //no data related to verb information data
            if (verbInfoDTO == null) {
                log.warn("Error while getting throttling information for resource and http verb");
                return false;
            } else {
                String resourceAndHTTPVerbThrottlingTier = verbInfoDTO.getThrottling();
                if (resourceAndHTTPVerbThrottlingTier == null) {
                    log.warn("Unable to find throttling information for resource and http verb. Throttling will " +
                            "not apply");
                } else {
                    resourceTier = resourceAndHTTPVerbThrottlingTier;
                }
                resourceKey = verbInfoDTO.getRequestKey() + '-' + consumerKey + ':' +
                        authorizedUser;
            }

            if (accessToken == null || roleID == null) {
                log.warn("No consumer key or role information found on the request - " +
                        "Throttling not applied");
                return true;
            }
            String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            apiContext = apiContext != null ? apiContext : "";
            apiVersion = apiVersion != null ? apiVersion : "";
            apiKey = apiContext + ':' + apiVersion + ':' + consumerKey + ':' + authorizedUser;
            apiTier = authContext.getTier();
            String remoteIP = "127.0.0.1";//(String) ((TreeMap) synCtx.getProperty(org.apache.axis2.context.MessageContext
            //.TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.X_FORWARDED_FOR);
            if (remoteIP != null) {
                if (remoteIP.indexOf(",") > 0) {
                    remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
                }
            } else {
                remoteIP = (String) synCtx.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
            }

            //todo Added some dummy parameters
            Map propertiesMap = new HashMap<String,String>();
            propertiesMap.put("remoteIp",remoteIP);
            propertiesMap.put("roleID", roleID);

            //this parameter will be used to capture message size and pass it to calculation logic
            int messageSizeInBytes = 0;
            if (authContext.isContentAware()) {
                //this request can match with with bandwidth policy. So we need to get message size.
                String httpVerb = verbInfoDTO.getHttpVerb();
                Object obj = ((TreeMap) ((Axis2MessageContext) synCtx).getAxis2MessageContext().getProperty("TRANSPORT_HEADERS")).get("Content-Length");
                if (obj != null) {
                    messageSizeInBytes = Integer.parseInt(obj.toString());
                }

            }
            
            Object[] objects = new Object[]{synCtx.getMessageID(), appKey, apiKey, appTier, apiTier, authorizedUser, propertiesMap};
            isThrottled = throttler.isThrottled(objects);
        }
        return isThrottled;
    }


    public boolean handleRequest(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;//return doThrottle(messageContext);
    }

    private boolean doThrottle(MessageContext messageContext) {
        boolean isThrottled = false;
        if (!messageContext.isResponse()) {
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            ConfigurationContext cc = axis2MC.getConfigurationContext();
            isThrottled = doRoleBasedAccessThrottlingWithCEP(messageContext, cc);
        }
        if (isThrottled) {
            handleThrottleOut(messageContext);
            return false;
        }
        return true;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public String gePolicyKey() {
        return policyKey;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private OMElement getFaultPayload(int throttleErrorCode, String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(throttleErrorCode));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private void handleThrottleOut(MessageContext messageContext) {

        String errorMessage = null;
        String errorDescription = null;
        int errorCode = -1;
        int httpErrorCode = -1;

        if (APIThrottleConstants.HARD_LIMIT_EXCEEDED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.HARD_LIMIT_EXCEEDED_ERROR_CODE;
            errorMessage = "API Limit Reached";
            errorDescription = "API not accepting requests";
            // It it's a hard limit exceeding, we tell it as service not being available.
            httpErrorCode = HttpStatus.SC_SERVICE_UNAVAILABLE;
        } else {
            errorCode = 503;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        }

        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);

        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(errorCode, errorMessage, errorDescription));
        } else {
            Utils.setSOAPFault(messageContext, "Server", errorMessage, errorDescription);
        }

        Utils.sendFault(messageContext, httpErrorCode);
    }
}
