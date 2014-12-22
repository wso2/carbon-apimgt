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

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;

/**
 * An API consumer authenticator which authenticates user requests using
 * the OAuth protocol. This implementation uses some default token/delimiter
 * values to parse OAuth headers, but if needed these settings can be overridden
 * through the APIManagerConfiguration.
 */
public class OAuthAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    protected APIKeyValidator keyValidator;

    private String securityHeader = HttpHeaders.AUTHORIZATION;
    private String defaultAPIHeader="WSO2_AM_API_DEFAULT_VERSION";
    private String consumerKeyHeaderSegment = "Bearer";
    private String oauthHeaderSplitter = ",";
    private String consumerKeySegmentDelimiter = " ";
    private String securityContextHeader;
    private boolean removeOAuthHeadersFromOutMessage=true;
    private boolean removeDefaultAPIHeaderFromOutMessage=true;
    private String clientDomainHeader = "referer";
    private String requestOrigin;

    public void init(SynapseEnvironment env) {
        this.keyValidator = new APIKeyValidator(env.getSynapseConfiguration().getAxisConfiguration());
        initOAuthParams();
    }

    public void destroy() {
        this.keyValidator.cleanup();
    }

    public boolean authenticate(MessageContext synCtx) throws APISecurityException {
        Map headers = (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        requestOrigin = (String) headers.get("Origin");
        String apiKey = null;
        if (headers != null) {
            apiKey = extractCustomerKeyFromAuthHeader(headers);
            if(log.isDebugEnabled()){
                log.debug("Received Token ".concat(apiKey));
            }
        }

        //Check if client invoked the default version API (accessing API without version).
        boolean defaultVersionInvoked = headers.containsKey(defaultAPIHeader);
        if(log.isDebugEnabled()){
            log.debug("Default Version API invoked");
        }

        if(removeOAuthHeadersFromOutMessage){
            headers.remove(securityHeader);
            if(log.isDebugEnabled()){
                log.debug("Removing Authorization header from headers");
            }
        }
        if(removeDefaultAPIHeaderFromOutMessage){
            headers.remove(defaultAPIHeader);
        }

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String fullRequestPath = (String)synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);

        String requestPath = fullRequestPath.substring((apiContext + apiVersion).length() + 1, fullRequestPath.length());

        if(log.isDebugEnabled()){
            log.debug("Full Request Path = ".concat(requestPath));
        }
        if (requestPath.equals("")) {
            requestPath = requestPath + "/";
        }
        String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);

        String clientDomain = getClientDomain(synCtx);
        if(log.isDebugEnabled() && null != clientDomain) {
            log.debug("Received Client Domain ".concat(clientDomain));
        }
        //If the matching resource does not require authentication
        //String authenticationScheme = keyValidator.getResourceAuthenticationScheme(apiContext, apiVersion, requestPath, httpMethod);
        String authenticationScheme = keyValidator.getResourceAuthenticationScheme(synCtx);
        APIKeyValidationInfoDTO info;
        if(APIConstants.AUTH_NO_AUTHENTICATION.equals(authenticationScheme)){

            if(log.isDebugEnabled()){
                log.debug("Found Authentication Scheme: ".concat(authenticationScheme));
            }

            String clientIP = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().getProperty(APIConstants.REMOTE_ADDR);

            //Create a dummy AuthenticationContext object with hard coded values for
            // Tier and KeyType. This is because we cannot determine the Tier nor Key
            // Type without subscription information..
            AuthenticationContext authContext = new AuthenticationContext();
            authContext.setAuthenticated(true);
            authContext.setTier(APIConstants.UNAUTHENTICATED_TIER);
            //Requests are throttled by the ApiKey that is set here. In an unauthenticated scenario,
            //we will use the client's IP address for throttling.
            authContext.setApiKey(clientIP);
            authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
            //This name is hardcoded as anonymous because there is no associated user token
            authContext.setUsername("anonymous");
            authContext.setCallerToken(null);
            authContext.setApplicationName(null);
            authContext.setConsumerKey(null);
            APISecurityUtils.setAuthenticationContext(synCtx, authContext, securityContextHeader);
            return true;
        } else if (APIConstants.NO_MATCHING_AUTH_SCHEME.equals(authenticationScheme)) {
            info = new APIKeyValidationInfoDTO();
            info.setAuthorized(false);
            info.setValidationStatus(900906);
        } else if (apiKey == null || apiContext == null || apiVersion == null) {
            if(log.isDebugEnabled()){
                if(apiKey == null){
                    log.debug("OAuth headers not found");
                }
                else if(apiContext == null){
                    log.debug("Couldn't find API Context");
                }
                else if(apiVersion == null){
                    log.debug("Could not find api version");
                }
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    "Required OAuth credentials not provided");
        } else {
            String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            if(log.isDebugEnabled()){
                log.debug("Matching resource is: ".concat(matchingResource));
            }
            org.apache.axis2.context.MessageContext axis2MessageCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MessageCtx);

            info = keyValidator.getKeyValidationInfo(apiContext, apiKey, apiVersion, authenticationScheme, clientDomain,
                    matchingResource, httpMethod, defaultVersionInvoked);

            synCtx.setProperty("APPLICATION_NAME", info.getApplicationName());
            synCtx.setProperty("END_USER_NAME", info.getEndUserName());
        }

        if (info.isAuthorized()) {
            AuthenticationContext authContext = new AuthenticationContext();
            authContext.setAuthenticated(true);
            authContext.setTier(info.getTier());
            authContext.setApiKey(apiKey);
            authContext.setKeyType(info.getType());
            authContext.setUsername(info.getEndUserName());
            authContext.setCallerToken(info.getEndUserToken());
            authContext.setApplicationId(info.getApplicationId());
            authContext.setApplicationName(info.getApplicationName());
            authContext.setApplicationTier(info.getApplicationTier());
            authContext.setSubscriber(info.getSubscriber());
            authContext.setConsumerKey(info.getConsumerKey());
            APISecurityUtils.setAuthenticationContext(synCtx, authContext, securityContextHeader);
            
            /* Synapse properties required for BAM Mediator*/
            String tenantDomain = MultitenantUtils.getTenantDomain(info.getApiPublisher());
            synCtx.setProperty("API_PUBLISHER", tenantDomain);
            synCtx.setProperty("API_NAME", info.getApiName());

            try {
                APIUtil.checkClientDomainAuthorized(info, clientDomain);
            } catch (APIManagementException e) {
               throw new APISecurityException(info.getValidationStatus(), e.getMessage());
            }
            if(log.isDebugEnabled()){
                log.debug("User is authorized to access the Resource");
            }
            return true;
        } else {
            if(log.isDebugEnabled()){
                log.debug("User is NOT authorized to access the Resource");
            }
            throw new APISecurityException(info.getValidationStatus(),
                    "Access failure for API: " + apiContext + ", version: " + apiVersion +
                            " with key: " + apiKey);
        }
    }

    /**
     * Extracts the customer API key from the OAuth Authentication header. If the required
     * security header is present in the provided map, it will be removed from the map
     * after processing.
     *
     * @param headersMap Map of HTTP headers
     * @return extracted customer key value or null if the required header is not present
     */
    public String extractCustomerKeyFromAuthHeader(Map headersMap) {

        //From 1.0.7 version of this component onwards remove the OAuth authorization header from
        // the message is configurable. So we dont need to remove headers at this point.
        String authHeader = (String) headersMap.get(securityHeader);
        if (authHeader == null) {
            return null;
        }

        if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
            authHeader = authHeader.substring(authHeader.indexOf("o"));
        }

        String[] headers = authHeader.split(oauthHeaderSplitter);
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                String[] elements = headers[i].split(consumerKeySegmentDelimiter);
                if (elements != null && elements.length > 1) {
                    int j = 0;
                    boolean isConsumerKeyHeaderAvailable = false;
                    for (String element : elements) {
                        if (!"".equals(element.trim())) {
                            if (consumerKeyHeaderSegment.equals(elements[j].trim())) {
                                isConsumerKeyHeaderAvailable = true;
                            } else if (isConsumerKeyHeaderAvailable) {
                                return removeLeadingAndTrailing(elements[j].trim());
                            }
                        }
                        j++;
                    }
                }
            }
        }
        return null;
    }

    private String removeLeadingAndTrailing(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }

    protected void initOAuthParams() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_OAUTH_HEADER);
        if (value != null) {
            securityHeader = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_CONSUMER_KEY_HEADER_SEGMENT);
        if (value != null) {
            consumerKeyHeaderSegment = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_OAUTH_HEADER_SPLITTER);
        if (value != null) {
            oauthHeaderSplitter = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_CONSUMER_KEY_SEGMENT_DELIMITER);
        if (value != null) {
            consumerKeySegmentDelimiter = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_CONTEXT_HEADER);
        if (value != null) {
            securityContextHeader = value;
        }
        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_REMOVE_OAUTH_HEADERS_FROM_OUT_MESSAGE);
        if (value != null) {
            removeOAuthHeadersFromOutMessage = Boolean.parseBoolean(value);
        }

        value = config.getFirstProperty
                (APIConstants.API_GATEWAY_CLIENT_DOMAIN_HEADER);
        if (value != null) {
            clientDomainHeader = value;
        }
    }

    public String getChallengeString() {
        return "OAuth2 realm=\"WSO2 API Manager\"";
    }

    private String getClientDomain(MessageContext synCtx) {
        String clientDomainHeaderValue = null;
        Map headers = (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            clientDomainHeaderValue = (String) headers.get(clientDomainHeader);
        }
        return clientDomainHeaderValue;
    }

	public String getRequestOrigin() {
		return requestOrigin;
	}
}
