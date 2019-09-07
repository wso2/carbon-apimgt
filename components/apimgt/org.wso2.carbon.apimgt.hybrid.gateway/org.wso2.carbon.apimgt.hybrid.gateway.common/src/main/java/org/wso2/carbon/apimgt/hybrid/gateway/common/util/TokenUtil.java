/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.AccessTokenDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationRequestDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for all the token related functions
 */
public class TokenUtil {

    private static final Log log = LogFactory.getLog(TokenUtil.class);
    private static final String AUTHORIZATION_BASIC = "Basic";

    /**
     * This method generated a Basic Auth header (base64 encoded) for the given two strings
     *
     * @param key    username or consumer key
     * @param secret password or consumer secret
     * @return a Basic Auth header (base64 encoded) for the given username and password
     */
    public static String getBasicAuthHeaderValue(String key, char[] secret) {
        String credentials = key + ":" + String.valueOf(secret);
        byte[] encodedCredentials = Base64.encodeBase64(
                credentials.getBytes(Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        return AUTHORIZATION_BASIC + " " +
                new String(encodedCredentials, Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET));
    }

    /**
     * Method to register a client by calling the dynamic client registration endpoint in API Manager with the default
     * parameters. Uses the default username & password (in APIKeyValidator config)
     *
     * @return OAuthApplicationInfoDTO
     */
    public static OAuthApplicationInfoDTO registerClient()
            throws OnPremiseGatewayException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        char[] password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();

        OAuthApplicationRequestDTO dto = new OAuthApplicationRequestDTO();
        dto.setAppCallbackUrl(OnPremiseGatewayConstants.DEFAULT_DCR_CALLBACK_URL);
        // ex: abc.com-on-premise-gateway
        dto.setClientName(MultitenantUtils.getTenantDomain(username) + "-" +
                OnPremiseGatewayConstants.DEFAULT_DCR_CLIENT_NAME);
        dto.setTokenScope(OnPremiseGatewayConstants.DEFAULT_DCR_SCOPE);
        dto.setOwner(username);
        dto.setGrantType(OnPremiseGatewayConstants.DEFAULT_DCR_GRANT_TYPE);
        dto.setSaasApp(true);
        return registerClient(dto, username, password);
    }

    /**
     * Method to register a client by calling the dynamic client registration endpoint in API Manager.
     *
     * @param applicationRequestDTO OAuth Application  Request DTO
     * @param username              tenant username
     * @param password              tenant password
     * @return OAuthApplicationInfoDTO
     */
    public static OAuthApplicationInfoDTO registerClient(OAuthApplicationRequestDTO applicationRequestDTO,
                                                         String username, char[] password)
            throws OnPremiseGatewayException {

        String restApiVersion = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_VERSION_PROPERTY);
        if (restApiVersion == null) {
            restApiVersion = OnPremiseGatewayConstants.API_DEFAULT_VERSION;
            if (log.isDebugEnabled()) {
                log.debug("Using default API version: " + restApiVersion);
            }
        } else if (OnPremiseGatewayConstants.CLOUD_API.equals(restApiVersion)) {
            restApiVersion = OnPremiseGatewayConstants.EMPTY_STRING;
            if (log.isDebugEnabled()) {
                log.debug("Cloud API doesn't have an version. Therefore, removing the version");
            }
        }
        String apiPublisherUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_PUBLISHER_URL_PROPERTY_KEY);
        if (apiPublisherUrl == null) {
            apiPublisherUrl = OnPremiseGatewayConstants.DEFAULT_API_PUBLISHER_URL;
            if (log.isDebugEnabled()) {
                log.debug("Using default API publisher URL: " + apiPublisherUrl);
            }
        }
        String clientRegistrationUrl =
                apiPublisherUrl + OnPremiseGatewayConstants.DYNAMIC_CLIENT_REGISTRATION_URL_SUFFIX
                        .replace(OnPremiseGatewayConstants.API_VERSION_PARAM, restApiVersion).replace("//",
                                OnPremiseGatewayConstants.URL_PATH_SEPARATOR); //remove "//" created in cloud case.

        URL apiPublisherUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(apiPublisherUrl);
        HttpClient httpClient = APIUtil.getHttpClient(apiPublisherUrlValue.getPort(), apiPublisherUrlValue
                .getProtocol());
        String authHeader = getBasicAuthHeaderValue(username, password);
        HttpPost httpPost = new HttpPost(clientRegistrationUrl);
        httpPost.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeader);
        httpPost.addHeader(OnPremiseGatewayConstants.CONTENT_TYPE_HEADER,
                OnPremiseGatewayConstants.CONTENT_TYPE_APPLICATION_JSON);
        try {
            StringEntity requestEntity = new StringEntity(applicationRequestDTO.toString());
            requestEntity.setContentType(OnPremiseGatewayConstants.CONTENT_TYPE_APPLICATION_JSON);
            httpPost.setEntity(requestEntity);
        } catch (UnsupportedEncodingException e) {
            throw new OnPremiseGatewayException("Failed to assign configured payload to client registration " +
                    "request.", e);
        }

        String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpPost,
                OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        if (log.isDebugEnabled()) {
            log.debug("Received Client Registration OAuthApplicationInfoDTO");
        }
        InputStream is = new ByteArrayInputStream(response.getBytes(
                Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(is, OAuthApplicationInfoDTO.class);
        } catch (IOException e) {
            throw new OnPremiseGatewayException("Failed to convert Client Registration response into " +
                    "OAuthApplicationInfoDTO.", e);
        }
    }

    /**
     * Method to get an access token corresponding to a given scope with given client ID and Secret for the default
     * username & password (in APIKeyValidator config)
     *
     * @param clientId     consumer key
     * @param clientSecret secret key
     * @return a json string containing consumer key/secret key pair
     */
    public static AccessTokenDTO generateAccessToken(String clientId, char[] clientSecret, String requiredScope)
            throws OnPremiseGatewayException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        char[] password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();
        Map<String, String> params = new HashMap<>();
        params.put(OnPremiseGatewayConstants.TOKEN_GRANT_TYPE_KEY,
                OnPremiseGatewayConstants.TOKEN_GRANT_TYPE);
        params.put(OnPremiseGatewayConstants.USERNAME_KEY, username);
        params.put(OnPremiseGatewayConstants.PASSWORD_KEY, String.valueOf(password));
        params.put(OnPremiseGatewayConstants.TOKEN_SCOPE, requiredScope);
        MicroGatewayCommonUtil.cleanPasswordCharArray(password);
        return generateAccessToken(params, clientId, clientSecret);
    }

    /**
     * Method to get an access token corresponding to a given scope
     *
     * @param params       query params
     * @param clientId     consumer key
     * @param clientSecret secret key
     * @return a json string containing consumer key/secret key pair
     */
    public static AccessTokenDTO generateAccessToken(Map<String, String> params, String clientId,
                                                     char[] clientSecret)
            throws OnPremiseGatewayException {
        String tokenApiUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_GATEWAY_URL_PROPERTY_KEY) +
                OnPremiseGatewayConstants.TOKEN_API_SUFFIX;
        String gatewayUrl = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_GATEWAY_URL_PROPERTY_KEY);
        URL gatewayUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(gatewayUrl);
        HttpClient httpClient = APIUtil.getHttpClient(gatewayUrlValue.getPort(), gatewayUrlValue.getProtocol());

        List<NameValuePair> paramsArray = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsArray.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        String authHeader = getBasicAuthHeaderValue(clientId, clientSecret);
        HttpPost httpPost = new HttpPost(tokenApiUrl);
        httpPost.addHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeader);

        try {
            UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(paramsArray);
            httpPost.setEntity(requestEntity);
        } catch (UnsupportedEncodingException e) {
            throw new OnPremiseGatewayException("Failed to assign configured payload parameters to access token " +
                    "generation request.", e);
        }

        String response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpPost,
                OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        InputStream is = new ByteArrayInputStream(response.getBytes(
                Charset.forName(OnPremiseGatewayConstants.DEFAULT_CHARSET)));
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(is, AccessTokenDTO.class);
        } catch (IOException e) {
            throw new OnPremiseGatewayException("Failed to convert Access Token response into " +
                    "OAuthApplicationInfoDTO.", e);
        }

    }
}
