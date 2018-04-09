/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.authenticator.utils;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.dto.ErrorDTO;
import org.wso2.msf4j.Request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

/**
 * This method authenticate the user.
 */
public class AuthUtil {

    public static final String COOKIE_PATH_SEPARATOR = "; path=";
    public static final String COOKIE_VALUE_SEPARATOR = "; ";

    private static final Map<String, Map<String, String>> contextPaths = new HashMap<>();

    /**
     * This method authenticate the user.
     *
     * @param cookie Cookie value
     * @return Header Value
     */
    public static String getHttpOnlyCookieHeader(Cookie cookie) {
        return cookie + "; HttpOnly";
    }

    private static Map<String, Map<String, String>> consumerKeySecretMap = new HashMap<>();

    private static List<String> roleList;

    public static String getAppContext(Request request) {
        //TODO this method should provide uuf app context. Consider the scenarios of reverse proxy as well.
        return "/" + request.getProperty("REQUEST_URL").toString().split("/")[3];
    }

    public static Map<String, Map<String, String>> getConsumerKeySecretMap() {
        return consumerKeySecretMap;
    }


    /**
     * This method is used to generate access token request to login for uuf apps.
     *
     * @param username username for generate token
     * @param password password for generate token
     * @param grantType grantType requested
     * @param refreshToken refreshToken if present
     * @param accessToken accessToken to revoke
     * @param validityPeriod validityPeriod for token
     * @param scopes requested scopes
     * @param clientId clientId of app
     * @param clientSecret clientSecret of app
     * @return AccessTokenRequest object
     */
    public static AccessTokenRequest createAccessTokenRequest(String username, String password, String grantType,
                                                              String refreshToken, String accessToken, long
                                                                      validityPeriod, String scopes, String clientId,
                                                              String clientSecret) {

        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(clientId);
        tokenRequest.setClientSecret(clientSecret);
        tokenRequest.setGrantType(grantType);
        tokenRequest.setRefreshToken(refreshToken);
        tokenRequest.setResourceOwnerUsername(username);
        tokenRequest.setResourceOwnerPassword(password);
        tokenRequest.setScopes(scopes);
        tokenRequest.setValidityPeriod(validityPeriod);
        tokenRequest.setTokenToRevoke(accessToken);
        return tokenRequest;

    }

    public static List<String> getRoleList() {
        return roleList;
    }

    public static void setRoleList(List<String> roleList) {
        AuthUtil.roleList = roleList;
    }

    /**
     * Returns a generic errorDTO
     *
     * @param errorHandler The error handler object.
     * @param paramList list of parameters for more detail
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(ErrorHandler errorHandler, HashMap<String, String> paramList) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(errorHandler.getErrorCode());
        errorDTO.setMoreInfo(paramList);
        errorDTO.setMessage(errorHandler.getErrorMessage());
        errorDTO.setDescription(errorHandler.getErrorDescription());
        return errorDTO;
    }

    /**
     * Method used to extract refresh token from headers.
     *
     * @param request msf4j request to get the headers
     * @param cookieHeader header value of cookie
     * @param environmentName environment name for token to be extract
     * @return refresh token present in the cookie and authorization header..
     */
    public static String extractTokenFromHeaders(Request request, String cookieHeader, String environmentName) {
        String authHeader = request.getHeader(AuthenticatorConstants.AUTHORIZATION_HTTP_HEADER);
        String token = "";
        if (authHeader != null && authHeader.toLowerCase(Locale.US).startsWith(AuthenticatorConstants.BEARER_PREFIX)) {
            authHeader = authHeader.trim();
            // Split the auth header to get the access token.
            String[] authHeaderParts = authHeader.split(" ");
            if (authHeaderParts.length == 2) {
                token = authHeaderParts[1];
            } else if (authHeaderParts.length < 2) {
                return null;
            }
        } else {
            return null;
        }

        String cookie = request.getHeader(AuthenticatorConstants.COOKIE_HEADER);
        if (cookie != null) {
            cookie = cookie.trim();
            String[] cookies = cookie.split(";");
            String tokenFromCookie = Arrays.stream(cookies).filter(
                    name -> name.contains(cookieHeader + "_" + environmentName))
                    .findFirst()
                    .orElse("");
            String[] tokenParts = tokenFromCookie.split("=");
            if (tokenParts.length == 2) {
                token += tokenParts[1];
            }
        }
        return token;
    }

    /**
     * Method used to build a cookie object.
     *
     * @param name       Name of the cookie.
     * @param value      Value of the cookie.
     * @param path       Context to which cookie should be set.
     * @param isHttpOnly If this a http only cookie.
     * @param isSecure   If this a secure cookie.
     * @param expiresIn  Expiration time of the cookie.
     * @param environmentName environment name for cookie to be created
     * @return Cookie object.
     */
    public static NewCookie cookieBuilder(String name, String value, String path, boolean isSecure,
                                          boolean isHttpOnly, String expiresIn, String environmentName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(value).append(COOKIE_PATH_SEPARATOR).append(path).append(COOKIE_VALUE_SEPARATOR);
        if (!StringUtils.isEmpty(expiresIn)) {
            stringBuilder.append(expiresIn).append(COOKIE_VALUE_SEPARATOR);
        }
        if (isHttpOnly) {
            stringBuilder.append(AuthenticatorConstants.HTTP_ONLY_COOKIE).append(COOKIE_VALUE_SEPARATOR);
        }
        if (isSecure) {
            stringBuilder.append(AuthenticatorConstants.SECURE_COOKIE).append(COOKIE_VALUE_SEPARATOR);
        }

        return new NewCookie(name + "_" + environmentName, stringBuilder.toString());
    }

    /**
     * Get context paths for the application
     *
     * @param appName Name of the Application
     * @return Map of context paths
     */
    public static Map<String, String> getContextPaths(String appName) {
        Map<String, String> contextPaths = AuthUtil.contextPaths.get(appName);
        if (contextPaths != null) {
            return contextPaths;
        }
        contextPaths = new HashMap<>();

        String appContext = AuthenticatorConstants.URL_PATH_SEPARATOR + appName;
        contextPaths.put(AuthenticatorConstants.Context.APP_CONTEXT, appContext);
        contextPaths.put(AuthenticatorConstants.Context.LOGOUT_CONTEXT,
                AuthenticatorConstants.LOGOUT_SERVICE_CONTEXT + appContext);
        contextPaths.put(AuthenticatorConstants.Context.LOGIN_CONTEXT,
                AuthenticatorConstants.LOGIN_SERVICE_CONTEXT + appContext);

        String restAPIContext;
        if (appContext.contains(AuthenticatorConstants.EDITOR_APPLICATION)) {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + AuthenticatorConstants.URL_PATH_SEPARATOR +
                    AuthenticatorConstants.PUBLISHER_APPLICATION;
        } else {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + appContext;
        }
        contextPaths.put(AuthenticatorConstants.Context.REST_API_CONTEXT, restAPIContext);

        AuthUtil.contextPaths.put(appName, contextPaths);
        return contextPaths;
    }
}
