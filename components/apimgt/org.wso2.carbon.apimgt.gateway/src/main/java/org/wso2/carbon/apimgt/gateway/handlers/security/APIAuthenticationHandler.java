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
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.*;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.handlers.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

    public void init(SynapseEnvironment synapseEnvironment) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing API authentication handler instance");
		}
        String authenticatorType = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(APISecurityConstants.API_SECURITY_AUTHENTICATOR);
        if (authenticatorType == null) {
            authenticatorType = OAuthAuthenticator.class.getName();
        }
        try {
            authenticator = (Authenticator) Class.forName(authenticatorType).newInstance();
        } catch (Exception e) {
            // Just throw it here - Synapse will handle it
            throw new SynapseException("Error while initializing authenticator of " +
                    "type: " + authenticatorType);
        }
        authenticator.init(synapseEnvironment);
    }

    public void destroy() {        
        if(authenticator != null) {
        	authenticator.destroy();
        } else {
        	log.warn("Unable to destroy uninitialized authentication hander instance");
        }        
    }

    public boolean handleRequest(MessageContext messageContext) {
        try {
            if (authenticator.authenticate(messageContext)) {
                return true;
            }
        } catch (APISecurityException e) {

            if (log.isDebugEnabled()) {
                logMessageDetails(messageContext);
            }
            log.error("API authentication failure", e);
            handleAuthFailure(messageContext, e);
        }
        return false;
    }

    public boolean handleResponse(MessageContext messageContext) {
    	
    	if (Utils.isCORSEnabled()) {
	    	/* For CORS support adding required headers to the response */
	    	org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
	                getAxis2MessageContext();
	    	Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
	    		    	    	
	    	headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin(authenticator.getRequestOrigin()));
	        headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
	        headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
	        axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
    	}
 
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
        try{
            RelayUtils.buildMessage(axis2MC);
        }
        catch (IOException ex){        //In case of an exception, it won't be propagated up, instead, will be logged; because we're setting a fault message in the payload.
            log.error("Error occurred while building the message", ex);
        }
        catch (XMLStreamException ex) {
            log.error("Error occurred while building the message", ex);
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
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(HttpHeaders.WWW_AUTHENTICATE, authenticator.getChallengeString());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(e));
        } else {
            Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
        }
        if (Utils.isCORSEnabled()) {
        	/* For CORS support adding required headers to the fault response */
        	Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin(authenticator.getRequestOrigin()));
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
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
        errorDetail.setText(e.getMessage());

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private void logMessageDetails(MessageContext messageContext) {
        //TODO: Hardcoded const should be moved to a common place which is visible to org.wso2.carbon.apimgt.gateway.handlers
        String applicationName = (String) messageContext.getProperty("APPLICATION_NAME");
        String endUserName = (String) messageContext.getProperty("END_USER_NAME");
        Date incomingReqTime = new Date();
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
        String userAgent = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("User-Agent");
        if (userAgent != null) {
            logMessage = logMessage + " with userAgent=" + userAgent;
        }
        String requestURI = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (requestURI != null) {
            logMessage = logMessage + " for requestURI=" + requestURI;
        }
        long reqIncomingTimestamp = Long.parseLong((String) ((Axis2MessageContext) messageContext).
                getAxis2MessageContext().getProperty("wso2statistics.request.received.time"));
        incomingReqTime = new Date(reqIncomingTimestamp);
        if (incomingReqTime != null) {
            logMessage = logMessage + " at time=" + incomingReqTime;
        }
        String remoteIP = (String) axisMC.getProperty("REMOTE_ADDR");
        if (remoteIP != null) {
            logMessage = logMessage + " from clientIP=" + remoteIP;
        }
        log.debug("Call to API Gateway " + logMessage);
    }
        
}
