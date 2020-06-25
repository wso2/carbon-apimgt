/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.Base64;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.BasicAuthValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

/**
 * An API consumer authenticator which authenticates user requests using
 * Basic Authentication which uses username and password for authentication.
 */
public class BasicAuthAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(BasicAuthAuthenticator.class);
    private final String basicAuthKeyHeaderSegment = "Basic";
    static final String PUBLISHER_TENANT_DOMAIN = "tenant.info.domain";

    private String securityHeader;
    private String requestOrigin;
    private BasicAuthCredentialValidator basicAuthCredentialValidator;
    private OpenAPI openAPI = null;
    private boolean isMandatory;

    /**
     * Initialize the authenticator with the required parameters.
     *
     * @param authorizationHeader the Authorization header
     */
    public BasicAuthAuthenticator(String authorizationHeader, boolean isMandatory) {
        this.securityHeader = authorizationHeader;
        this.isMandatory = isMandatory;
    }

    /**
     * Set the BasicAuthCredentialValidator
     *
     * @param basicAuthCredentialValidator the BasicAuthCredentialValidator instance to be set
     */
    public void setBasicAuthCredentialValidator(BasicAuthCredentialValidator basicAuthCredentialValidator) {
        this.basicAuthCredentialValidator = basicAuthCredentialValidator;
    }

    /**
     * Initializes this authenticator instance.
     *
     * @param env Current SynapseEnvironment instance
     */
    public void init(SynapseEnvironment env) {
        try {
            this.basicAuthCredentialValidator = new BasicAuthCredentialValidator();
        } catch (APISecurityException e) {
            log.error(e);
        }
    }

    /**
     * Destroys this authenticator and releases any resources allocated to it.
     */
    @java.lang.Override
    public void destroy() {
    }

    /**
     * Authenticates the given request to see if an API consumer is allowed to access
     * a particular API or not.
     *
     * @param synCtx The message to be authenticated
     * @return an AuthenticationResponse object which contains the authentication status
     */
    @MethodStats
    public AuthenticationResponse authenticate(MessageContext synCtx) {
        if (log.isDebugEnabled()) {
            log.info("Basic Authentication initialized");
        }

        openAPI = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
        if (openAPI == null && !APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
            log.error("OpenAPI definition is missing in the gateway. Basic authentication cannot be performed.");
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF, "Basic authentication cannot be performed.");
        }

        // Extract basic authorization header while removing it from the authorization header
        String basicAuthHeader = extractBasicAuthHeader(synCtx);

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        // Check for resource level authentication
        String authenticationScheme;

        List<VerbInfoDTO> verbInfoList;

        if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
            HashMap<String, Boolean> operationAuthSchemeMappingList =
                    (HashMap<String, Boolean>) synCtx.getProperty(APIConstants.OPERATION_AUTH_SCHEME_MAPPING);
            HashMap<String, String> operationThrottlingMappingList =
                    (HashMap<String, String>) synCtx.getProperty(APIConstants.OPERATION_THROTTLING_MAPPING);

            String[] operationList = matchingResource.split(",");
            verbInfoList = new ArrayList<>(1);
            authenticationScheme = APIConstants.AUTH_NO_AUTHENTICATION;
            for (String operation: operationList) {
                boolean operationAuthSchemeEnabled = operationAuthSchemeMappingList.get(operation);
                VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                if (operationAuthSchemeEnabled) {
                    verbInfoDTO.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    authenticationScheme = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                } else {
                    verbInfoDTO.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                }
                verbInfoDTO.setThrottling(operationThrottlingMappingList.get(operation));
                verbInfoDTO.setRequestKey(apiContext + "/" + apiVersion + operation + ":" + httpMethod);
                verbInfoList.add(verbInfoDTO);
            }
        } else {
            authenticationScheme = OpenAPIUtils.getResourceAuthenticationScheme(openAPI, synCtx);
            verbInfoList = new ArrayList<>(1);
            VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
            verbInfoDTO.setAuthType(authenticationScheme);
            verbInfoDTO.setThrottling(OpenAPIUtils.getResourceThrottlingTier(openAPI, synCtx));
            verbInfoDTO.setRequestKey(apiContext + "/" + apiVersion + matchingResource + ":" + httpMethod);
            verbInfoList.add(verbInfoDTO);
        }


        if (APIConstants.AUTH_NO_AUTHENTICATION.equals(authenticationScheme)) {
            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: Found Resource Authentication Scheme: ".concat(authenticationScheme));
            }
            //using existing constant in Message context removing the additional constant in API Constants
            String clientIP = null;
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).
                    getAxis2MessageContext();
            TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) axis2MessageContext
                    .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (transportHeaderMap != null) {
                clientIP = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
            }

            //Setting IP of the client
            if (clientIP != null && !clientIP.isEmpty()) {
                if (clientIP.indexOf(",") > 0) {
                    clientIP = clientIP.substring(0, clientIP.indexOf(","));
                }
            } else {
                clientIP = (String) axis2MessageContext
                        .getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
            }

            //Create a dummy AuthenticationContext object with hard coded values for
            // Tier and KeyType. This is because we cannot determine the Tier nor Key
            // Type without subscription information..
            AuthenticationContext authContext = new AuthenticationContext();
            authContext.setAuthenticated(true);
            authContext.setTier(APIConstants.UNAUTHENTICATED_TIER);
            //Since we don't have details on unauthenticated tier we setting stop on quota reach true
            authContext.setStopOnQuotaReach(true);
            //Requests are throttled by the ApiKey that is set here. In an unauthenticated scenario,
            //we will use the client's IP address for throttling.
            authContext.setApiKey(clientIP);
            authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
            //This name is hardcoded as anonymous because there is no associated user token
            authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
            authContext.setCallerToken(null);
            authContext.setApplicationName(null);
            authContext.setApplicationId(clientIP); //Set clientIp as application ID in unauthenticated scenario
            authContext.setConsumerKey(null);
            APISecurityUtils.setAuthenticationContext(synCtx, authContext, null);

            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: Authentication succeeded by ignoring auth headers for API resource: "
                        .concat(matchingResource));
            }
            return new AuthenticationResponse(true, isMandatory, false, 0, null);
        }

        String[] credentials;
        try {
            credentials = extractBasicAuthCredentials(basicAuthHeader);
        } catch (APISecurityException ex) {
            return new AuthenticationResponse(false, isMandatory, true, ex.getErrorCode(), ex.getMessage());
        }
        String username = getEndUserName(credentials[0]);
        String password = credentials[1];

        // If end user tenant domain does not match the API publisher's tenant domain, return error
        if (!MultitenantUtils.getTenantDomain(username).equals(synCtx.getProperty(PUBLISHER_TENANT_DOMAIN))) {
            log.error("Basic Authentication failure: tenant domain mismatch for user :" + username);
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.API_AUTH_FORBIDDEN, APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
        }

        BasicAuthValidationInfoDTO basicAuthValidationInfoObj;
        try {
            basicAuthValidationInfoObj = basicAuthCredentialValidator.validate(username, password);
        } catch (APISecurityException ex) {
            return new AuthenticationResponse(false, isMandatory, true, ex.getErrorCode(), ex.getMessage());
        }
        if (!basicAuthValidationInfoObj.isAuthenticated()) {
            log.error("Basic Authentication failure: Username and Password mismatch");
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        } else { // username password matches
            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: Username and Password authenticated");
            }
            //scope validation
            boolean scopesValid = false;
            try {
                scopesValid = basicAuthCredentialValidator
                        .validateScopes(username,  openAPI, synCtx, basicAuthValidationInfoObj.getUserRoleList());
            } catch (APISecurityException ex) {
                return new AuthenticationResponse(false, isMandatory, true, ex.getErrorCode(), ex.getMessage());
            }
            String domainQualifiedUserName = basicAuthValidationInfoObj.getDomainQualifiedUsername();

            if (scopesValid) {
                if (APISecurityUtils.getAuthenticationContext(synCtx) == null) {
                    //Create a dummy AuthenticationContext object with hard coded values for
                    // Tier and KeyType. This is because we cannot determine the Tier nor Key
                    // Type without subscription information..
                    AuthenticationContext authContext = new AuthenticationContext();
                    authContext.setAuthenticated(true);
                    authContext.setTier(APIConstants.UNAUTHENTICATED_TIER);
                    authContext.setStopOnQuotaReach(
                            true);//Since we don't have details on unauthenticated tier we setting stop on quota reach true
                    synCtx.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);
                    //In basic authentication scenario, we will use the username for throttling.
                    authContext.setApiKey(domainQualifiedUserName);
                    authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
                    authContext.setUsername(domainQualifiedUserName);
                    authContext.setCallerToken(null);
                    authContext.setApplicationName(APIConstants.BASIC_AUTH_APPLICATION_NAME);
                    authContext.setApplicationId(
                            domainQualifiedUserName); //Set username as application ID in basic auth scenario
                    authContext.setConsumerKey(null);
                    APISecurityUtils.setAuthenticationContext(synCtx, authContext, null);
                }
                log.debug("Basic Authentication: Scope validation passed");
                return new AuthenticationResponse(true, isMandatory, false, 0, null);
            }
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
        }
    }

    /**
     * Extract the basic authentication credentials from the basic authorization header via Base64 decoding.
     *
     * @param basicAuthHeader the basic authorization header
     * @return a String array containing username and password
     * @throws APISecurityException in case of invalid authorization header or no header
     */
    private String[] extractBasicAuthCredentials(String basicAuthHeader) throws APISecurityException {
        if (basicAuthHeader == null) {
            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: No Basic Auth Header found");
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
        } else {
            if (basicAuthHeader.contains(basicAuthKeyHeaderSegment)) {
                try {
                    String basicAuthKey = new String(Base64.decode(
                            basicAuthHeader.substring(basicAuthKeyHeaderSegment.length() + 1).trim()));
                    if (basicAuthKey.contains(":")) {
                        return basicAuthKey.split(":");
                    } else {
                        log.error("Basic Authentication: Invalid Basic Auth token");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    }
                } catch (WSSecurityException e) {
                    log.error("Error occured during Basic Authentication: Invalid Basic Auth token");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Basic Authentication: No Basic Auth Header found");
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                        APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
            }
        }
    }

    /**
     * Extract the Basic Auth header segment from the Auth header.
     *
     * @param synCtx The message to be authenticated
     * @return the basic auth header segment.
     */
    private String extractBasicAuthHeader(MessageContext synCtx) {
        final String authHeaderSplitter = ",";
        Map headers = (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            String authHeader = (String) headers.get(securityHeader);
            if (authHeader == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Basic Authentication: Expected authorization header with the name '"
                            .concat(securityHeader).concat("' was not found."));
                }
                return null;
            } else {
                if (authHeader.contains(basicAuthKeyHeaderSegment)) {
                    String[] authHeaderArr = authHeader.split(authHeaderSplitter);
                    ArrayList<String> remainingAuthHeaders = new ArrayList<>();
                    String basicAuthHeader = null;
                    for (String headerSegment : authHeaderArr) {
                        if (headerSegment.trim().split(" ")[0].equals(basicAuthKeyHeaderSegment)) {
                            basicAuthHeader = headerSegment.trim();
                        } else {
                            remainingAuthHeaders.add(headerSegment.trim());
                        }
                    }
                    String remainingAuthHeader = String.join(authHeaderSplitter, remainingAuthHeaders);
                    //Remove basic authorization header segment sent and pass others to the backend
                    if (StringUtils.isNotBlank(remainingAuthHeader)) {
                        headers.put(securityHeader, remainingAuthHeader);
                    } else {
                        headers.remove(securityHeader);
                    }
                    return basicAuthHeader;
                }
            }
        }
        return null;
    }

    /**
     * Returns a string representation of the authentication challenge imposed by this
     * authenticator. In case of an authentication failure this value will be sent back
     * to the API consumer in the form of a WWW-Authenticate header.
     *
     * @return A string representation of the authentication challenge
     */
    public String getChallengeString() {
        return "Basic Auth realm=\"WSO2 API Manager\"";
    }


    /**
     * Returns the origin of the request
     *
     * @return returns the origin of the request
     */
    public String getRequestOrigin() {
        return requestOrigin;
    }

    /**
     * Sets the origin of the request
     *
     * @param requestOrigin the origin of the request
     */
    public void setRequestOrigin(String requestOrigin) {
        this.requestOrigin = requestOrigin;
    }

    private String getEndUserName(String username) {
        return MultitenantUtils.getTenantAwareUsername(username) + "@" + MultitenantUtils.getTenantDomain(username);
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
