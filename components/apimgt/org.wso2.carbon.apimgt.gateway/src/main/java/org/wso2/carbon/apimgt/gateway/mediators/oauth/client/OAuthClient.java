/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators.oauth.client;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents the client used to request and retrieve OAuth tokens
 * from an OAuth-protected backend
 */
public class OAuthClient {
    private static final Log log = LogFactory.getLog(OAuthClient.class);

    /**
     * Method to generate the access token for an OAuth backend
     *
     * @param url              The token url of the backend
     * @param clientId         The Client ID
     * @param clientSecret     The Client Secret
     * @param username         The username
     * @param password         The password
     * @param grantType        The grant type
     * @param customParameters The custom parameters JSON Object
     * @param refreshToken     The refresh token
     * @return TokenResponse object
     * @throws IOException            In the event of a problem parsing the response from the backend
     * @throws APIManagementException In the event of an unexpected HTTP status code from the backend
     */
    public static TokenResponse generateToken(String url, String clientId, String clientSecret,
                                              String username, char[] password, String grantType, JSONObject customParameters, String refreshToken)
            throws IOException, APIManagementException, ParseException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing token generation request: [token-endpoint] " + url);
        }

        URL urlObject;
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        urlObject = new URL(url);
        StringBuilder payload = new StringBuilder();
        try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil
                .getHttpClient(urlObject.getPort(), urlObject.getProtocol())) {
            HttpPost httpPost = new HttpPost(url);
            // Set authorization header
            httpPost.setHeader(APIConstants.OAuthConstants.AUTHORIZATION_HEADER, "Basic " + credentials);
            httpPost.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.OAuthConstants.APPLICATION_X_WWW_FORM_URLENCODED);
            if (refreshToken != null) {
                payload.append(APIConstants.OAuthConstants.REFRESH_TOKEN_GRANT_TYPE)
                        .append("&refresh_token=").append(refreshToken);
            } else if (grantType.equals(APIConstants.OAuthConstants.CLIENT_CREDENTIALS)) {
                payload.append(APIConstants.OAuthConstants.CLIENT_CRED_GRANT_TYPE);
            } else if (grantType.equals(APIConstants.OAuthConstants.PASSWORD)) {
                payload.append(APIConstants.OAuthConstants.PASSWORD_GRANT_TYPE + "&username=")
                        .append(username).append("&password=")
                        .append(String.valueOf(password));
            }

            payload = appendCustomParameters(customParameters, payload);

            httpPost.setEntity(new StringEntity(payload.toString()));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return getTokenResponse(response);
            } finally {
                httpPost.releaseConnection();
            }
        }
    }

    /**
     * Method to append the properties from the Custom Parameters JSONObject to the payload
     *
     * @param customParameters Custom Parameters JSONObject
     * @param string           StringBuilder object containing the existing payload
     * @return The StringBuilder object with the updated payload
     */
    private static StringBuilder appendCustomParameters(JSONObject customParameters, StringBuilder string) {
        if (customParameters != null) {
            for (Object keyStr : customParameters.keySet()) {
                Object keyValue = customParameters.get(keyStr);
                string.append("&").append(keyStr).append("=").append(keyValue);
            }
        }
        return string;
    }

    /**
     * Method to retrieve the token response sent from the backend
     *
     * @param response CloseableHttpResponse object
     * @return TokenResponse object containing the details retrieved from the backend
     * @throws APIManagementException In the event of an unexpected HTTP status code from the backend
     * @throws IOException            In the event of a problem parsing the response from the backend
     */
    private static TokenResponse getTokenResponse(CloseableHttpResponse response)
            throws APIManagementException, IOException, ParseException {
        int responseCode = response.getStatusLine().getStatusCode();

        if (!(responseCode == HttpStatus.SC_OK)) {
            throw new APIManagementException("Error while accessing the Token URL. "
                    + "Found http status " + response.getStatusLine());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response
                .getEntity().getContent(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();

        while ((inputLine = reader.readLine()) != null) {
            stringBuilder.append(inputLine);
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(stringBuilder.toString());
        TokenResponse tokenResponse = new TokenResponse();
        if (jsonResponse.containsKey("access_token")) {
            tokenResponse.setAccessToken((String) jsonResponse.get("access_token"));
            if (jsonResponse.containsKey("refresh_token")) {
                tokenResponse.setRefreshToken((String) jsonResponse.get("refresh_token"));
            }
            if (jsonResponse.containsKey("scope")) {
                Set<String> scopeSet = Stream.of(jsonResponse.get("scope").toString().trim()
                        .split("\\s*,\\s*")).collect(Collectors.toSet());
                tokenResponse.setScope(scopeSet);
            }
            if (jsonResponse.containsKey("token_type")) {
                tokenResponse.setTokenType((String) jsonResponse.get("token_type"));
            }
            if (jsonResponse.containsKey("expires_in")) {
                tokenResponse.setExpiresIn(jsonResponse.get("expires_in").toString());
                long currentTimeInSeconds = System.currentTimeMillis() / 1000;
                long expiryTimeInSeconds = currentTimeInSeconds + Long.parseLong(tokenResponse.getExpiresIn());
                tokenResponse.setValidTill(expiryTimeInSeconds);
            } else if (null != APIUtil.getMediationConfigurationFromAPIMConfig(
                    APIConstants.OAuthConstants.OAUTH_MEDIATION_CONFIG +
                            APIConstants.OAuthConstants.EXPIRES_IN_CONFIG)) {
                tokenResponse.setExpiresIn(APIUtil.getMediationConfigurationFromAPIMConfig(
                        APIConstants.OAuthConstants.OAUTH_MEDIATION_CONFIG +
                                APIConstants.OAuthConstants.EXPIRES_IN_CONFIG));
                long currentTimeInSeconds = System.currentTimeMillis() / 1000;
                long expiryTimeInSeconds = currentTimeInSeconds + Long.parseLong(tokenResponse.getExpiresIn());
                tokenResponse.setValidTill(expiryTimeInSeconds);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Response: [status-code] " + responseCode + " [message] "
                    + stringBuilder.toString());
        }
        if (tokenResponse.getAccessToken() != null) {
            return tokenResponse;
        } else {
            return null;
        }
    }

}
