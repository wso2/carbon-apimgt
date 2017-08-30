/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authentication handler for REST APIs exposed in the API gateway. This handler will
 * drop the requests if an authentication failure occurs. But before a message is dropped
 * it looks for a special custom error handler sequence APISecurityConstants.API_AUTH_FAILURE_HANDLER
 * through which the message will be mediated when available. This is a custom extension point
 * provided to the users to handle authentication failures in a deployment specific manner.
 * Once the custom error handler has been invoked, this implementation will further try to
 * respond to the client with a 401 Unauthorized response. If this is not required, the users
 * must drop the message in their custom error handler itself.
 * <p/>
 * If no authentication errors are encountered, this will add some AuthenticationContext
 * information to the request and let it through to the next handler in the chain.
 */
public class APIAuthenticationHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(APIAuthenticationHandler.class);

    private volatile Authenticator authenticator;

    private SynapseEnvironment synapseEnvironment;

    public void init(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
		if (log.isDebugEnabled()) {
			log.debug("Initializing API authentication handler instance");
		}
        if (ServiceReferenceHolder.getInstance().getApiManagerConfigurationService() != null){
            initializeAuthenticator();
        }
    }

    public void destroy() {
        if(authenticator != null) {
        	authenticator.destroy();
        } else {
        	log.warn("Unable to destroy uninitialized authentication handler instance");
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "LEST_LOST_EXCEPTION_STACK_TRACE", justification = "The exception needs to thrown for fault sequence invocation")
    private void initializeAuthenticator() {
        authenticator = new OAuthAuthenticator();
        authenticator.init(synapseEnvironment);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EXS_EXCEPTION_SOFTENING_RETURN_FALSE",
            justification = "Error is sent through payload")
    public boolean handleRequest(MessageContext messageContext) {
        Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName()));
        Timer.Context context = timer.start();
        long startTime = System.nanoTime();
        long endTime;
        long difference;

        try {
            if (APIUtil.isAnalyticsEnabled()) {
                long currentTime = System.currentTimeMillis();
                messageContext.setProperty("api.ut.requestTime", Long.toString(currentTime));
            }
            if (authenticator == null) {
                initializeAuthenticator();
            }
            if (authenticator.authenticate(messageContext)) {
                if (log.isDebugEnabled()) {
                    // We do the calculations only if the debug logs are enabled. Otherwise this would be an overhead
                    // to all the gateway calls that is happening.
                    endTime = System.nanoTime();
                    difference = (endTime - startTime) / 1000000;
                    String messageDetails = logMessageDetails(messageContext);

                    log.debug("Authenticated API, authentication response relieved: " + messageDetails +
                            ", elapsedTimeInMilliseconds=" + difference / 1000000);
                }
                setAPIParametersToMessageContext(messageContext);
                return true;
            }
        } catch (APISecurityException e) {

            if (log.isDebugEnabled()) {
                // We do the calculations only if the debug logs are enabled. Otherwise this would be an overhead
                // to all the gateway calls that is happening.
                endTime = System.nanoTime();
                difference = (endTime - startTime) / 1000000;
                String messageDetails = logMessageDetails(messageContext);
                log.debug("Call to API gateway : " + messageDetails + ", elapsedTimeInMilliseconds=" +
                        difference / 1000000);
            }
            // We do not need to log authentication failures as errors since these are not product errors.
            log.warn("API authentication failure due to " +
                    APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));

            if (log.isDebugEnabled()) {
                log.debug("API authentication failed with error " + e.getErrorCode(), e);
            }

            handleAuthFailure(messageContext, e);
        } finally {
            messageContext.setProperty(APIMgtGatewayConstants.SECURITY_LATENCY,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
            context.stop();

        }

        return false;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private void handleAuthFailure(MessageContext messageContext, APISecurityException e) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, e.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, e);

        Mediator sequence = messageContext.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        // By default we send a 401 response back
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
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");
        int status;
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_GENERAL_ERROR) {
            status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if (e.getErrorCode() == APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE ||
                   e.getErrorCode() == APISecurityConstants.API_AUTH_FORBIDDEN ||
                e.getErrorCode() == APISecurityConstants.INVALID_SCOPE) {
            status = HttpStatus.SC_FORBIDDEN;
        } else {
            status = HttpStatus.SC_UNAUTHORIZED;
            Map<String, String> headers =
                    (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (headers != null) {
                headers.put(HttpHeaders.WWW_AUTHENTICATE, authenticator.getChallengeString() +
                        ", error=\"invalid token\"" +
                        ", error_description=\"The access token expired\"");
                axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(e));
        } else {
            Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
        }
        Utils.sendFault(messageContext, status);
    }

    private OMElement getFaultPayload(APISecurityException e) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(e.getErrorCode()));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(), e.getMessage()));

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private String logMessageDetails(MessageContext messageContext) {
        //TODO: Hardcoded const should be moved to a common place which is visible to org.wso2.carbon.apimgt.gateway.handlers
        String applicationName = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME);
        String endUserName = (String) messageContext.getProperty(APIMgtGatewayConstants.END_USER_NAME);
        Date incomingReqTime = null;
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String logMessage = "API call failed reason=API_authentication_failure"; //"app-name=" + applicationName + " " + "user-name=" + endUserName;
        String logID = axisMC.getOptions().getMessageId();
        if (applicationName != null) {
            logMessage = " belonging to appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " userName=" + endUserName;
        }
        if (logID != null) {
            logMessage = logMessage + " transactionId=" + logID;
        }
        String userAgent = (String) ((TreeMap) axisMC.getProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS)).get(APIConstants.USER_AGENT);
        if (userAgent != null) {
            logMessage = logMessage + " with userAgent=" + userAgent;
        }
        String accessToken = (String) ((TreeMap) axisMC.getProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.AUTHORIZATION);
        if (accessToken != null) {
            logMessage = logMessage + " with accessToken=" + accessToken;
        }
        String requestURI = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (requestURI != null) {
            logMessage = logMessage + " for requestURI=" + requestURI;
        }
        String requestReceivedTime = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(APIMgtGatewayConstants.REQUEST_RECEIVED_TIME);
        if (requestReceivedTime != null) {
            long reqIncomingTimestamp = Long.parseLong(requestReceivedTime);
            incomingReqTime = new Date(reqIncomingTimestamp);
            logMessage = logMessage + " at time=" + incomingReqTime;
        }

        String remoteIP = (String) axisMC.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        if (remoteIP != null) {
            logMessage = logMessage + " from clientIP=" + remoteIP;
        }
        return logMessage;
    }

    private void setAPIParametersToMessageContext(MessageContext messageContext) {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String consumerKey = "";
        String username = "";
        String applicationName = "";
        String applicationId = "";
        if (authContext != null) {
            consumerKey = authContext.getConsumerKey();
            username = authContext.getUsername();
            applicationName = authContext.getApplicationName();
            applicationId = authContext.getApplicationId();
        }

        String context = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);

        String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        //if publisher is null,extract the publisher from the api_version
        if (apiPublisher == null) {
            int ind = apiVersion.indexOf("--");
            apiPublisher = apiVersion.substring(0, ind);
            if (apiPublisher.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
                apiPublisher = apiPublisher
                        .replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT, APIConstants.EMAIL_DOMAIN_SEPARATOR);
            }
        }
        int index = apiVersion.indexOf("--");

        if (index != -1) {
            apiVersion = apiVersion.substring(index + 2);
        }

        String api = apiVersion.split(":")[0];
        String version = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String resource = extractResource(messageContext);
        String method = (String) (axis2MsgContext.getProperty(
                Constants.Configuration.HTTP_METHOD));
        String hostName = APIUtil.getHostAddress();

        messageContext.setProperty(APIMgtGatewayConstants.CONSUMER_KEY, consumerKey);
        messageContext.setProperty(APIMgtGatewayConstants.USER_ID, username);
        messageContext.setProperty(APIMgtGatewayConstants.CONTEXT, context);
        messageContext.setProperty(APIMgtGatewayConstants.API_VERSION, apiVersion);
        messageContext.setProperty(APIMgtGatewayConstants.API, api);
        messageContext.setProperty(APIMgtGatewayConstants.VERSION, version);
        messageContext.setProperty(APIMgtGatewayConstants.RESOURCE, resource);
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_METHOD, method);
        messageContext.setProperty(APIMgtGatewayConstants.HOST_NAME, hostName);
        messageContext.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiPublisher);
        messageContext.setProperty(APIMgtGatewayConstants.APPLICATION_NAME, applicationName);
        messageContext.setProperty(APIMgtGatewayConstants.APPLICATION_ID, applicationId);
    }

    private String extractResource(MessageContext mc) {
        String resource = "/";
        Pattern pattern = Pattern.compile(APIMgtGatewayConstants.RESOURCE_PATTERN);
        Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }

}
