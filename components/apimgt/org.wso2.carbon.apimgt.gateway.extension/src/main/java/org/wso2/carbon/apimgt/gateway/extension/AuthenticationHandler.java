/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.extension;


import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.security.OAuthAuthenticator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.handler.MessagingHandler;



/**
 * Handler to manage API authentication through OAuth2
 **/
@Component(
        name = "org.wso2.carbon.apimgt.gateway.extension.AuthenticationHandler",
        immediate = true,
        service = MessagingHandler.class)


public class AuthenticationHandler implements MessagingHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationHandler.class);
    private volatile Authenticator authenticator;


    public AuthenticationHandler() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Authentication Handler initialized");
        }

        initializeAuthenticator();
    }

    @Override
    public boolean validateRequestContinuation(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        return false;
    }

    @Override
    public void invokeAtSourceConnectionInitiation(String s) {

    }

    @Override
    public void invokeAtSourceConnectionTermination(String s) {

    }

    @Override
    public void invokeAtTargetConnectionInitiation(String s) {

    }

    @Override
    public void invokeAtTargetConnectionTermination(String s) {

    }

    @Override
    public void invokeAtSourceRequestReceiving(CarbonMessage carbonMessage) {
        /*Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName()));
        Timer.Context context = timer.start();
        long startTime = System.nanoTime();
        long endTime;
        long difference;

        try {
            //TO-DO Check analytics enabled and if so,calculate the time
            //if (APIUtil.isAnalyticsEnabled()) {
               // long currentTime = System.currentTimeMillis();
               // messageContext.setProperty("api.ut.requestTime", Long.toString(currentTime));
            //}
            if (authenticator == null) {
                initializeAuthenticator();
            }
            if (authenticator.authenticate(carbonMessage)) {
                if (LOG.isDebugEnabled()) {
                    // We do the calculations only if the debug logs are enabled. Otherwise this would be an overhead
                    // to all the gateway calls that is happening.
                    endTime = System.nanoTime();
                    difference = (endTime - startTime) / 1000000;
                    String messageDetails = logMessageDetails(messageContext);

                    LOG.debug("Authenticated API, authentication response relieved: " + messageDetails +
                            ", elapsedTimeInMilliseconds=" + difference / 1000000);
                }
                setAPIParametersToMessageContext(messageContext);

            }
        } catch (APIKeyMgtException e) {

            if (LOG.isDebugEnabled()) {
                // We do the calculations only if the debug logs are enabled. Otherwise this would be an overhead
                // to all the gateway calls that is happening.
                endTime = System.nanoTime();
                difference = (endTime - startTime) / 1000000;
                String messageDetails = logMessageDetails(carbonMessage);
                LOG.debug("Call to API gateway : " + messageDetails + ", elapsedTimeInMilliseconds=" +
                        difference / 1000000);
            }
            // We do not need to log authentication failures as errors since these are not product errors.
            LOG.warn("API authentication failure due to " +
                    APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));

            LOG.error("API authentication failed with error " + e.getErrorCode(), e);

            handleAuthFailure(messageContext, e);
        } finally {
            carbonMessage.setProperty(APIMgtGatewayConstants.SECURITY_LATENCY,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
            context.stop();

        }

        return false;*/

    }

    @Override
    public void invokeAtSourceRequestSending(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetRequestReceiving(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetRequestSending(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetResponseReceiving(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetResponseSending(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtSourceResponseReceiving(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtSourceResponseSending(CarbonMessage carbonMessage) {

    }

    @Override
    public String handlerName() {
        return "AuthenticationHandler";
    }

    private void initializeAuthenticator() {
        authenticator = new OAuthAuthenticator();
        authenticator.init();
    }

    /*
    private String logMessageDetails(CarbonMessage carbonMessage) {
        //TODO: Hardcoded const should be moved to a common place which is visible to org.wso2.carbon.apimgt.
        // gateway.handlers
        String applicationName = (String) carbonMessage.getProperty(GatewayConstants.APPLICATION_NAME);
        String endUserName = (String) carbonMessage.getProperty(GatewayConstants.END_USER_NAME);
        Date incomingReqTime = null;
        String logMessage = "API call failed reason=API_authentication_failure";
        String logID = (String) carbonMessage.getProperty(""); //TO-DO getMessageId();
        if (applicationName != null) {
            logMessage = " belonging to appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " userName=" + endUserName;
        }
        if (logID != null) {
            logMessage = logMessage + " transactionId=" + logID;
        }
        String userAgent = carbonMessage.getHeader("User-Agent");
        if (userAgent != null) {
            logMessage = logMessage + " with userAgent=" + userAgent;
        }
        String accessToken = carbonMessage.getHeader("Authorization");
        if (accessToken != null) {
            logMessage = logMessage + " with accessToken=" + accessToken;
        }
        String requestURI = (String) carbonMessage.getProperty(GatewayConstants.REST_FULL_REQUEST_PATH);
        if (requestURI != null) {
            logMessage = logMessage + " for requestURI=" + requestURI;
        }
        long reqIncomingTimestamp = Long.parseLong((String) carbonMessage.getProperty(GatewayConstants.
                REQUEST_RECEIVED_TIME));
        incomingReqTime = new Date(reqIncomingTimestamp);
        logMessage = logMessage + " at time=" + incomingReqTime;

        String remoteIP = (String) carbonMessage.getProperty("REMOTE_ADDR");
        if (remoteIP != null) {
            logMessage = logMessage + " from clientIP=" + remoteIP;
        }
        return logMessage;
    }
    */

    /*

    private void handleAuthFailure(CarbonMessage carbonMessage, APIKeyMgtException e) {
       /* carbonMessage.setProperty(SynapseConstants.ERROR_CODE, e.getErrorCode());
        carbonMessage.setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        carbonMessage.setProperty(SynapseConstants.ERROR_EXCEPTION, e);

        Mediator sequence = carbonMessage.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);
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
            //In case of an error it is logged and the process is continued because we're setting a fault
            message in the payload.
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
                headers.put(HttpHeaders.WWW_AUTHENTICATE, authenticator.getChallengeString());
                axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }
        }

        if (carbonMessage.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(e));
        } else {
            Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
        }
        Utils.sendFault(messageContext, status);*/
        /*}

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
    }*/
}
