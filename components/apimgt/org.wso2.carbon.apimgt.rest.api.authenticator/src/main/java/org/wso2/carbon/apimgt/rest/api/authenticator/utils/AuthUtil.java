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

import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.dto.ErrorDTO;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.rest.api.configurations.ConfigurationService;
import org.wso2.carbon.messaging.Headers;
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
 *
 */
public class AuthUtil {

    public static final String COOKIE_PATH_SEPERATOR = "; path=";
    public static final String COOKIE_VALUE_SEPERATOR = "; ";

    /**
     * This method authenticate the user.
     *
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
     */
    public static AccessTokenRequest createAccessTokenRequest(String username, String password, String grantType,
            String refreshToken, String accessToken, long validityPeriod, String scopes, String clientId, String
            clientSecret) {

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
     * @param headers  headers which contains the access token
     * @return refresh token present in the cookie and authorization header..
     */
    public static String extractTokenFromHeaders(Headers headers, String cookieHeader) {
        String authHeader = headers.get(AuthenticatorConstants.AUTHORIZATION_HTTP_HEADER);
        String token = "";
        if (authHeader != null) {
            authHeader = authHeader.trim();
            if (authHeader.toLowerCase(Locale.US).startsWith(AuthenticatorConstants.BEARER_PREFIX)) {
                // Split the auth header to get the access token.
                String[] authHeaderParts = authHeader.split(" ");
                if (authHeaderParts.length == 2) {
                    token = authHeaderParts[1];
                } else if (authHeaderParts.length < 2) {
                    return null;
                }
            }
        } else {
            return null;
        }

        //Append unique environment name in deployment.yaml
        String environmentName = ConfigurationService.getEnvironmentName();
        String cookie = headers.get(AuthenticatorConstants.COOKIE_HEADER);
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
     * @param name       Name of the cookie.
     * @param value      Value of the cookie.
     * @param path       Context to which cookie should be set.
     * @param isHttpOnly If this a http only cookie.
     * @param isSecure   If this a secure cookie.
     * @param expiresIn  Expiration time of the cookie.
     * @return Cookie object.
     */
    public static NewCookie cookieBuilder(String name, String value, String path, boolean isSecure,
            boolean isHttpOnly, String expiresIn) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(value).append(COOKIE_PATH_SEPERATOR).append(path).append(COOKIE_VALUE_SEPERATOR);
        if (isHttpOnly) {
            stringBuilder.append(AuthenticatorConstants.HTTP_ONLY_COOKIE).append(COOKIE_VALUE_SEPERATOR);
        }
        if (isSecure) {
            stringBuilder.append(AuthenticatorConstants.SECURE_COOKIE);
        }
        if (expiresIn != null && !expiresIn.isEmpty()) {
            stringBuilder.append(COOKIE_VALUE_SEPERATOR).append(expiresIn);
        }

        //Append unique environment name in deployment.yaml
        String environmentName = ConfigurationService.getEnvironmentName();
        return new NewCookie(name + "_" + environmentName, stringBuilder.toString());
    }

}
