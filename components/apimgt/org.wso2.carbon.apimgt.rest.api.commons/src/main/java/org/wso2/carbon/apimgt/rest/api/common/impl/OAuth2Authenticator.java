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

import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.api.RESTAPIAuthenticator;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.configurations.ConfigurationService;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    /*
    * This method performs authentication and authorization
    * @param Request
    * @param Response
    * @param ServiceMethodInfo
    * throws Exception
    * */
    @Override
    public boolean authenticate(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws APIMgtSecurityException {
        ErrorHandler errorHandler = null;
        boolean isTokenValid = false;
        Headers headers = request.getHeaders();
        if (headers != null && headers.contains(RestApiConstants.COOKIE_HEADER) && isCookieExists(headers,
                APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J)) {
            String accessToken = null;
            String cookies = headers.get(RestApiConstants.COOKIE_HEADER);
            String partialTokenFromCookie = extractPartialAccessTokenFromCookie(cookies);
            if (partialTokenFromCookie != null && headers.contains(RestApiConstants.AUTHORIZATION_HTTP_HEADER)) {
                String authHeader = headers.get(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
                String partialTokenFromHeader = extractAccessToken(authHeader);
                accessToken = (partialTokenFromHeader != null) ?
                        partialTokenFromHeader + partialTokenFromCookie :
                        partialTokenFromCookie;
            }
            isTokenValid = validateTokenAndScopes(request, serviceMethodInfo, accessToken);
            request.setProperty(LOGGED_IN_USER, getEndUserName(accessToken));
        } else if (headers != null && headers.contains(RestApiConstants.AUTHORIZATION_HTTP_HEADER)) {
            String authHeader = headers.get(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
            String accessToken = extractAccessToken(authHeader);
            if (accessToken != null) {
                isTokenValid = validateTokenAndScopes(request, serviceMethodInfo, accessToken);
                request.setProperty(LOGGED_IN_USER, getEndUserName(accessToken));
            }
        } else {
            throw new APIMgtSecurityException("Missing Authorization header in the request.`",
                    ExceptionCodes.MALFORMED_AUTHORIZATION_HEADER_OAUTH);
        }

        return isTokenValid;
    }

    private boolean validateTokenAndScopes(Request request, ServiceMethodInfo serviceMethodInfo, String accessToken)
            throws APIMgtSecurityException {
        //Map<String, String> tokenInfo = validateToken(accessToken);
        AccessTokenInfo accessTokenInfo = validateToken(accessToken);
        String restAPIResource = getRestAPIResource(request);

        //scope validation
        return validateScopes(request, serviceMethodInfo, accessTokenInfo.getScopes(), restAPIResource);
    }

    /**
     * Extract the EndUsername from accessToken.
     *
     * @param accessToken the access token
     * @return loggedInUser if the token is a valid token
     */
    private String getEndUserName(String accessToken) throws APIMgtSecurityException {
        String loggedInUser;
        loggedInUser = validateToken(accessToken).getEndUserName();
        return loggedInUser.substring(0, loggedInUser.lastIndexOf("@"));
    }

    /**
     * Extract the accessToken from the give Authorization header value and validates the accessToken
     * with an external key manager.
     *
     * @param accessToken the access token
     * @return responseData if the token is a valid token
     */
    private AccessTokenInfo validateToken(String accessToken) throws APIMgtSecurityException {
        // 1. Send a request to key server's introspect endpoint to validate this token
        AccessTokenInfo accessTokenInfo = getValidatedTokenResponse(accessToken);

        // 2. Process the response and return true if the token is valid.
        if (!accessTokenInfo.isTokenValid()) {
            throw new APIMgtSecurityException("Invalid Access token.", ExceptionCodes.ACCESS_TOKEN_INACTIVE);
        }
        return accessTokenInfo;

        /*
        // 1. Send a request to key server's introspect endpoint to validate this token
        String responseStr = getValidatedTokenResponse(accessToken);
        Map<String, String> responseData = getResponseDataMap(responseStr);

        // 2. Process the response and return true if the token is valid.
        if (responseData == null || !Boolean.parseBoolean(responseData.get(IntrospectionResponse.ACTIVE))) {
            throw new APIMgtSecurityException("Invalid Access token.", ExceptionCodes.ACCESS_TOKEN_INACTIVE);
        }

        return responseData;
        */
    }

    /**
     * @param cookie Cookies  header which contains the access token
     * @return partial access token present in the cookie.
     * @throws APIMgtSecurityException if the Authorization header is invalid
     */
    private String extractPartialAccessTokenFromCookie(String cookie) {
        //Append unique environment name in deployment.yaml
        String environmentName = ConfigurationService.getEnvironmentName();

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

    private boolean isCookieExists(Headers headers, String cookieName) {
        String cookie = headers.get(RestApiConstants.COOKIE_HEADER);
        String token2 = null;

        //Append unique environment name in deployment.yaml
        String environmentName = ConfigurationService.getEnvironmentName();
        if (cookie != null) {
            cookie = cookie.trim();
            String[] cookies = cookie.split(";");
            token2 = Arrays.stream(cookies)
                    .filter(name -> name.contains(cookieName + "_" + environmentName))
                    .findFirst().orElse(null);
        }
        return (token2 != null);
    }

    /*
    * This methos is used to get the rest api resource based on the api context
    * @param Request
    * @return String : api resource object
    * @throws APIMgtSecurityException if resource could not be found.
    * */
    private String getRestAPIResource(Request request) throws APIMgtSecurityException {
        //todo improve to get appname as a property in the Request
        String path = (String) request.getProperty(APIConstants.REQUEST_URL);
        String restAPIResource = null;
        //this is publisher API so pick that API
        try {
            if (path.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT)) {
                restAPIResource = RestApiUtil.getPublisherRestAPIResource();
            } else if (path.contains(RestApiConstants.REST_API_STORE_CONTEXT)) {
                restAPIResource = RestApiUtil.getStoreRestAPIResource();
            } else if (path.contains(RestApiConstants.REST_API_ADMIN_CONTEXT)) {
                restAPIResource = RestApiUtil.getAdminRestAPIResource();
            } else if (path.contains(RestApiConstants.REST_API_ANALYTICS_CONTEXT)) {
                restAPIResource = RestApiUtil.getAnalyticsRestAPIResource();
            } else {
                throw new APIMgtSecurityException("No matching Rest Api definition found for path:" + path);
            }
        } catch (APIManagementException e) {
            throw new APIMgtSecurityException(e.getMessage(), ExceptionCodes.AUTH_GENERAL_ERROR);
        }

        return restAPIResource;

    }

    /*
    * This method validates the given scope against scopes defined in the api resource
    * @param Request
    * @param ServiceMethodInfo
    * @param scopesToValidate scopes extracted from the access token
    * @return true if scope validation successful
    * */
    @SuppressFBWarnings({"DLS_DEAD_LOCAL_STORE"})
    private boolean validateScopes(Request request, ServiceMethodInfo serviceMethodInfo, String scopesToValidate,
                                   String restAPIResource) throws APIMgtSecurityException {
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
                    if (apiResourceDefinitionScopes == null) {
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
                    throw new APIMgtSecurityException(message, ExceptionCodes.INVALID_SCOPE);
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
            throw new APIMgtSecurityException(message, ExceptionCodes.INVALID_SCOPE);

        }
        return authorized[0];
    }

    /**
     * @param authHeader Authorization Bearer header which contains the access token
     * @return access token
     * @throws APIMgtSecurityException if the Authorization header is invalid
     */
    private String extractAccessToken(String authHeader) throws APIMgtSecurityException {
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

        throw new APIMgtSecurityException("Invalid Authorization : Bearer header " +
                authHeader, ExceptionCodes.MALFORMED_AUTHORIZATION_HEADER_OAUTH);
    }

    /**
     * Validated the given accessToken with an external key server.
     *
     * @param accessToken AccessToken to be validated.
     * @return the response from the key manager server.
     */
    private AccessTokenInfo getValidatedTokenResponse(String accessToken) throws APIMgtSecurityException {
        try {
            AccessTokenInfo accessTokenInfo = APIManagerFactory.getInstance().getIdentityProvider()
                    .getTokenMetaData(accessToken);
            return accessTokenInfo;
            /*
            url = new URL(authServerURL);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod(HttpMethod.POST);
            String payload = "token=" + accessToken + "&token_type_hint=" + RestApiConstants.BEARER_PREFIX;
            urlConn.getOutputStream().write(payload.getBytes(Charsets.UTF_8));

            String response = new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
            log.debug("Response received from Auth Server : " + response);
            return response;
        } catch (java.io.IOException e) {
            log.error("Error invoking Authorization Server", e);
            throw new APIMgtSecurityException("Error invoking Authorization Server", ExceptionCodes.AUTH_GENERAL_ERROR);
        */
        } catch (APIManagementException e) {
            log.error("Error while validating access token", e);
            throw new APIMgtSecurityException("Error while validating access token", ExceptionCodes.AUTH_GENERAL_ERROR);
        }
    }

    /**
     * @param responseStr validated token response string returned from the key server.
     * @return a Map of key, value pairs available the response String.
     */
    /*private Map<String, String> getResponseDataMap(String responseStr) {
        Gson gson = new Gson();
        Type typeOfMapOfStrings = new ExtendedTypeToken<Map<String, String>>() {

        }.getType();
        return gson.fromJson(responseStr, typeOfMapOfStrings);
    }*/

    /**
     * This class extends the {@link com.google.gson.reflect.TypeToken}.
     * Created due to the findbug issue when passing anonymous inner class.
     *
     * @param <T> Generic type
     */
    private static class ExtendedTypeToken<T> extends TypeToken {

    }

}
