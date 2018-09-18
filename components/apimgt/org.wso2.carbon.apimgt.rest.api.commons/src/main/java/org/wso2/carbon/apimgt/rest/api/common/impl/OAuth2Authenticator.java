/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.common.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.auth.rest.api.authenticators.RestAPIConstants;
import org.wso2.carbon.auth.rest.api.authenticators.api.RESTAPIAuthenticator;
import org.wso2.carbon.auth.rest.api.authenticators.exceptions.ExceptionCodes;
import org.wso2.carbon.auth.rest.api.authenticators.exceptions.RestAPIAuthSecurityException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;

/**
 * OAuth2 implementation class
 */
public class OAuth2Authenticator implements RESTAPIAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(OAuth2Authenticator.class);
    private static final String LOGGED_IN_USER = "LOGGED_IN_USER";
    private static String authServerURL;

    static {
        authServerURL = SystemVariableUtil.getValue(RestApiConstants.AUTH_SERVER_URL_KEY,
                RestApiConstants.AUTH_SERVER_URL);
        if (authServerURL == null) {
            throw new RuntimeException(RestApiConstants.AUTH_SERVER_URL_KEY + " is not specified.");
        }
    }

    @Override
    public boolean authenticate(Request request, Response responder, Method method) throws
            RestAPIAuthSecurityException {

        boolean isAuthenticated = false;
        HttpHeaders headers = request.getHeaders();
        boolean isCookieHeaderPresent = false;
        boolean isAuthorizationHeaderPresent = false;

        if (request.getHeader(RestApiConstants.COOKIE_HEADER) != null) {
            isCookieHeaderPresent = true;
        }

        if (request.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER) != null) {
            isAuthorizationHeaderPresent = true;
        }

        if (headers != null && isCookieHeaderPresent && isCookieExists(request,
                APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J)) {
            String accessToken = null;
            String cookies = request.getHeader(RestApiConstants.COOKIE_HEADER);
            String partialTokenFromCookie = extractPartialAccessTokenFromCookie(cookies);
            if (partialTokenFromCookie != null && isAuthorizationHeaderPresent) {
                String authHeader = request.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
                String partialTokenFromHeader = extractAccessToken(authHeader);
                accessToken = (partialTokenFromHeader != null) ?
                        partialTokenFromHeader + partialTokenFromCookie :
                        partialTokenFromCookie;
            }
            isAuthenticated = validateTokenAndScopes(request, method, accessToken);
            request.setProperty(LOGGED_IN_USER, getEndUserName(accessToken));
        } else if (headers != null && isAuthorizationHeaderPresent) {
            String authHeader = request.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
            String accessToken = extractAccessToken(authHeader);
            if (accessToken != null) {
                isAuthenticated = validateTokenAndScopes(request, method, accessToken);
                request.setProperty(LOGGED_IN_USER, getEndUserName(accessToken));
            }
        } else if (headers != null && !isAuthorizationHeaderPresent &&
                checkAnonymousPermission(request, method)) {
            // If the REST api resource has anonymous permission, set the logged in user as "__wso2.am.anon__".
            isAuthenticated = true;
            request.setProperty(LOGGED_IN_USER, RestApiConstants.ANONYMOUS_USER);
        } else {
            throw new RestAPIAuthSecurityException("Missing Authorization header in the request.`", ExceptionCodes
                    .MALFORMED_AUTHORIZATION_HEADER_OAUTH);
        }
        return isAuthenticated;
    }

    /**
     * Check if the api resource in the request has anonymous permission defined in the swagger definition.
     *
     * @param request           the request
     * @param serviceMethodInfo
     * @return true if the api resource has given anonymous permission in the swagger definition.
     */
    private boolean checkAnonymousPermission(Request request, Method serviceMethodInfo)
            throws RestAPIAuthSecurityException {

        Swagger restAPIResource = getRestAPISwagger(request);
        String verb = (String) request.getProperty(APIConstants.HTTP_METHOD);
        String path = (String) request.getProperty(APIConstants.REQUEST_URL);
        log.debug("Invoking rest api resource path {} {} to check anonymous permission.", verb, path);
        if (restAPIResource != null) {
            APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
            try {
                String apiResourceDefinitionScopes = apiDefinition.getScopeOfResourcePath(restAPIResource, request,
                        serviceMethodInfo);
                if (StringUtils.isEmpty(apiResourceDefinitionScopes)) {
                    log.debug("Scope not defined in swagger for matching resource {} and verb {}. Hence consider as " +
                            "anonymous permission.", path, verb);
                    return true;
                }
                log.debug("Scope defined in swagger for resource {} and verb {}.", path, verb);
            } catch (APIManagementException e) {
                String message = "Error while validating scopes for matching resource " + path + " and verb "
                        + verb + " for anonymous permission.";
                log.error(message, e);
                throw new RestAPIAuthSecurityException(message, ExceptionCodes.INVALID_SCOPE);
            }
        } else {
            String message = "Rest API resource could not be found for request path '" + path
                    + "' while checking for anonymous permission.";
            throw new RestAPIAuthSecurityException(message, ExceptionCodes.INVALID_SCOPE);
        }
        return false;
    }

    private boolean validateTokenAndScopes(Request request, Method serviceMethodInfo, String accessToken)
            throws RestAPIAuthSecurityException {
        //Map<String, String> tokenInfo = validateToken(accessToken);
        AccessTokenInfo accessTokenInfo = validateToken(accessToken);
        Swagger restAPIResource = getRestAPISwagger(request);

        //scope validation
        return validateScopes(request, serviceMethodInfo, accessTokenInfo.getScopes(), restAPIResource);
    }

    /**
     * Extract the EndUsername from accessToken.
     *
     * @param accessToken the access token
     * @return loggedInUser if the token is a valid token
     */
    private String getEndUserName(String accessToken) throws RestAPIAuthSecurityException {

        String loggedInUser;
        loggedInUser = validateToken(accessToken).getEndUserName();
        return loggedInUser;
    }

    /**
     * Extract the accessToken from the give Authorization header value and validates the accessToken
     * with an external key manager.
     *
     * @param accessToken the access token
     * @return responseData if the token is a valid token
     */
    private AccessTokenInfo validateToken(String accessToken) throws RestAPIAuthSecurityException {
        // 1. Send a request to key server's introspect endpoint to validate this token
        AccessTokenInfo accessTokenInfo = getValidatedTokenResponse(accessToken);

        // 2. Process the response and return true if the token is valid.
        if (!accessTokenInfo.isTokenValid()) {
            throw new RestAPIAuthSecurityException("Invalid Access token.", ExceptionCodes.ACCESS_TOKEN_INACTIVE);
        }
        return accessTokenInfo;
    }

    /**
     * @param cookie Cookies  header which contains the access token
     * @return partial access token present in the cookie.
     */
    private String extractPartialAccessTokenFromCookie(String cookie) {
        //Append unique environment name in deployment.yaml
        String environmentName = APIMConfigurationService.getInstance()
                .getEnvironmentConfigurations().getEnvironmentLabel();

        if (cookie != null) {
            cookie = cookie.trim();
            String[] cookies = cookie.split(";");
            String token2 = Arrays.stream(cookies)
                    .filter(name -> name.contains(
                            APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J + "_" + environmentName
                    )).findFirst().orElse("");
            String tokensArr[] = token2.split("=");
            if (tokensArr.length == 2) {
                return tokensArr[1];
            }
        }
        return null;
    }

    private boolean isCookieExists(Request request, String cookieName) {

        String cookie = request.getHeader(RestApiConstants.COOKIE_HEADER);
        String token2 = null;

        //Append unique environment name in deployment.yaml
        String environmentName = APIMConfigurationService.getInstance()
                .getEnvironmentConfigurations().getEnvironmentLabel();
        if (cookie != null) {
            cookie = cookie.trim();
            String[] cookies = cookie.split(";");
            token2 = Arrays.stream(cookies)
                    .filter(name -> name.contains(cookieName + "_" + environmentName))
                    .findFirst().orElse(null);
        }
        return (token2 != null);
    }

    private Swagger getRestAPISwagger(Request request) {

        String basePath = (String) request.getProperty(RestAPIConstants.ELECTED_BASE_PATH);
        return ServiceReferenceHolder.getInstance().getSecurityConfigurationService().getRestAPIInfoMap().get
                (basePath).getSwagger();

    }

    /**
     *
     * This method validates the given scope against scopes defined in the api resource
     * @param request Http Request
     * @param serviceMethodInfo Http Method invoke
     * @param scopesToValidate scopes extracted from the access token
     * @return true if scope validation successful
     */
    @SuppressFBWarnings({"DLS_DEAD_LOCAL_STORE"})
    private boolean validateScopes(Request request, Method serviceMethodInfo, String scopesToValidate,
                                   Swagger restAPIResource) throws RestAPIAuthSecurityException {

        final boolean authorized[] = {false};

        String path = (String) request.getProperty(APIConstants.REQUEST_URL);
        String verb = (String) request.getProperty(APIConstants.HTTP_METHOD);
        if (log.isDebugEnabled()) {
            log.debug("Invoking rest api resource path " + verb + " " + path + " ");
            log.debug("LoggedIn user scopes " + scopesToValidate);
        }
        String[] scopesArr = new String[0];
        if (scopesToValidate != null) {
            scopesArr = scopesToValidate.split(" ");
        }
        if (scopesToValidate != null && scopesArr.length > 0) {
            final List<String> scopes = Arrays.asList(scopesArr);
            if (restAPIResource != null) {
                APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
                try {
                    String apiResourceDefinitionScopes = apiDefinition.getScopeOfResourcePath(restAPIResource, request,
                            serviceMethodInfo);
                    if (StringUtils.isEmpty(apiResourceDefinitionScopes)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Scope not defined in swagger for matching resource " + path + " and verb "
                                    + verb + " . Hence consider as anonymous permission and let request to continue.");
                        }
                        // scope validation gets through if no scopes found in the api definition
                        authorized[0] = true;
                    } else {
                        Arrays.stream(apiResourceDefinitionScopes.split(" "))
                                .forEach(scopeKey -> {
                                    Optional<String> key = scopes.stream().filter(scp -> {
                                        return scp.equalsIgnoreCase(scopeKey);
                                    }).findAny();
                                    if (key.isPresent()) {
                                        authorized[0] = true;  //scope validation success if one of the
                                        // apiResourceDefinitionScopes found.
                                    }
                                });
                    }

                } catch (APIManagementException e) {
                    String message = "Error while validating scopes";
                    log.error(message, e);
                    throw new RestAPIAuthSecurityException(message, ExceptionCodes.INVALID_SCOPE);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Rest API resource could not be found for request path '" + path + "'");
                }
            }
        } else { // scope validation gets through if access token does not contain scopes to validate
            authorized[0] = true;
        }

        if (!authorized[0]) {
            String message = "Scope validation fails for the scopes " + scopesToValidate;
            throw new RestAPIAuthSecurityException(message, ExceptionCodes.INVALID_SCOPE);

        }
        return authorized[0];
    }

    /**
     * @param authHeader Authorization Bearer header which contains the access token
     * @return access token
     * @throws RestAPIAuthSecurityException if the Authorization header is invalid
     */
    private String extractAccessToken(String authHeader) throws RestAPIAuthSecurityException {

        authHeader = authHeader.trim();
        if (authHeader.toLowerCase(Locale.US).startsWith(RestApiConstants.BEARER_PREFIX)) {
            // Split the auth header to get the access token.
            // Value should be in this format ("Bearer" 1*SP b64token)
            String[] authHeaderParts = authHeader.split(" ");
            if (authHeaderParts.length == 2) {
                return authHeaderParts[1];
            } else if (authHeaderParts.length < 2) {
                return null;
            }
        }

        throw new RestAPIAuthSecurityException("Invalid Authorization : Bearer header " +
                authHeader, ExceptionCodes.MALFORMED_AUTHORIZATION_HEADER_OAUTH);
    }

    /**
     * Validated the given accessToken with an external key server.
     *
     * @param accessToken AccessToken to be validated.
     * @return the response from the key manager server.
     */
    private AccessTokenInfo getValidatedTokenResponse(String accessToken) throws RestAPIAuthSecurityException {

        try {
            AccessTokenInfo accessTokenInfo = APIManagerFactory.getInstance().getIdentityProvider()
                    .getTokenMetaData(accessToken);
            return accessTokenInfo;
        } catch (APIManagementException e) {
            log.error("Error while validating access token", e);
            throw new RestAPIAuthSecurityException("Error while validating access token", ExceptionCodes
                    .AUTH_GENERAL_ERROR);
        }
    }

}
