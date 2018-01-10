package org.wso2.carbon.apimgt.core.impl;
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.RestCallUtil;
import org.wso2.carbon.apimgt.core.auth.dto.DCRClientInfo;
import org.wso2.carbon.apimgt.core.configuration.models.KeyMgtConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.HttpResponse;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * This class holds the key manager implementation for light weight key manager
 */
public class DefaultKeyManagerImpl implements KeyManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultKeyManagerImpl.class);
    private KeyMgtConfigurations keyManagerConfigs;
    private RestCallUtil restCallUtil;

    public DefaultKeyManagerImpl() {
        keyManagerConfigs = ServiceReferenceHolder.getInstance().getAPIMConfiguration().getKeyManagerConfigs();
        restCallUtil = new RestCallUtilImpl();
    }

    public DefaultKeyManagerImpl(KeyMgtConfigurations keyManagerConfigs, RestCallUtil restCallUtil) {
        this.keyManagerConfigs = keyManagerConfigs;
        this.restCallUtil = restCallUtil;
    }

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws KeyManagementException {
        APIUtils.logDebug("Creating OAuth2 application: " + oauthAppRequest.toString(), log);

        String applicationName = oauthAppRequest.getClientName();
        String keyType = oauthAppRequest.getKeyType();
        if (keyType != null) {  //Derive oauth2 app name based on key type and user input for app name
            applicationName = applicationName + '_' + keyType;
        }

        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(applicationName);
        dcrClientInfo.setGrantTypes(oauthAppRequest.getGrantTypes());
        if (StringUtils.isNotEmpty(oauthAppRequest.getCallBackURL())) {
            dcrClientInfo.addCallbackUrl(oauthAppRequest.getCallBackURL());
        }

        HttpResponse httpResponse = null;
        try {
            String url = keyManagerConfigs.getDcrEndpoint();
            Map<String, String> map = new HashMap<>();
            map.put(KeyManagerConstants.AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(
                    (keyManagerConfigs.getKeyManagerCredentials().getUsername() + ":" + keyManagerConfigs
                            .getKeyManagerCredentials().getPassword()).getBytes(Charset.defaultCharset())));
            String payload = "{'" + KeyManagerConstants.OAUTH_CLIENT_NAME + "':'" + applicationName + "'}";
            httpResponse = restCallUtil
                    .postRequest(new URI(url), MediaType.APPLICATION_JSON_TYPE, null, Entity.text(payload),
                            MediaType.APPLICATION_JSON_TYPE, map);
        } catch (URISyntaxException e) {
            throw new KeyManagementException("Error occurred while parsing DCR endpoint", e,
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error occurred while invoking DCR endpoint", e,
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        if (httpResponse == null) {
            throw new KeyManagementException("Error occurred while DCR application creation. Response is null",
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }

        APIUtils.logDebug("DCR Response code: " + httpResponse.getResponseCode(), log);
        APIUtils.logDebug("DCR Response: " + httpResponse.getResults(), log);

        if (httpResponse.getResponseCode() == APIMgtConstants.HTTPStatusCodes.SC_201_CREATED) {  //201 - Success
            try {
                OAuthApplicationInfo oAuthApplicationInfoResponse = getOAuthApplicationInfo(httpResponse);
                //setting original parameter list
                oAuthApplicationInfoResponse.setParameters(oauthAppRequest.getParameters());
                APIUtils.logDebug("OAuth2 application created: " + oAuthApplicationInfoResponse.toString(), log);
                return oAuthApplicationInfoResponse;
            } catch (ParseException e) {
                throw new KeyManagementException(
                        "Error occurred while parsing the DCR application creation response " + "message.", e,
                        ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            }
        } else if (httpResponse.getResponseCode()
                == APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST) {  //400 - Known Error
            throw new KeyManagementException(
                    "Error occurred while DCR application creation. Error: " + ". Error Description: "
                            + ". Status Code: " + httpResponse.getResponseCode(),
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } else {  //Unknown Error
            throw new KeyManagementException(
                    "Error occurred while DCR application creation. Error: " + " Status Code: " + httpResponse
                            .getResponseCode(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthApplicationInfo oAuthApplicationInfo)
            throws KeyManagementException {
        //todo: implement
        return null;
    }

    @Override
    public void deleteApplication(String consumerKey) throws KeyManagementException {
        //todo: implement
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException {
        APIUtils.logDebug("Retrieving OAuth application for consumer key: " + consumerKey, log);

        if (StringUtils.isEmpty(consumerKey)) {
            throw new KeyManagementException("Unable to retrieve OAuth Application. Consumer Key is null or empty",
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }
        HttpResponse response = null;
        try {
            String url = keyManagerConfigs.getDcrEndpoint() + "/" + consumerKey;
            response = restCallUtil.getRequest(new URI(url), MediaType.APPLICATION_JSON_TYPE, null);
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error occurred while invoking DCR endpoint", e,
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        } catch (URISyntaxException e) {
            throw new KeyManagementException("Error occurred while parsing DCR endpoint", e,
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        if (response == null) {
            throw new KeyManagementException("Error occurred while retrieving DCR application. Response is null",
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }
        APIUtils.logDebug("DCR Response code: " + response.getResponseCode(), log);
        APIUtils.logDebug("DCR Response: " + response.getResults(), log);

        if (response.getResponseCode() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {   //200 - Success
            try {
                OAuthApplicationInfo oAuthApplicationInfoResponse = getOAuthApplicationInfo(response);
                APIUtils.logDebug("OAuth2 application retrieved: " + oAuthApplicationInfoResponse.toString(), log);
                return oAuthApplicationInfoResponse;
            } catch (ParseException e) {
                throw new KeyManagementException(
                        "Error occurred while parsing the DCR application retrieval " + "response message.", e,
                        ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            }
        } else {  //Unknown Error
            throw new KeyManagementException(
                    "Error occurred while retrieving DCR application. Error: " + " Status Code: " + response
                            .getResponseCode(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }
    }

    @Override
    public AccessTokenInfo getNewAccessToken(AccessTokenRequest tokenRequest) throws KeyManagementException {
        if (tokenRequest == null) {
            throw new KeyManagementException("No information available to generate Token. AccessTokenRequest is null",
                    ExceptionCodes.INVALID_TOKEN_REQUEST);
        }

        // Call the /revoke only if there's a token to be revoked.
        if (!StringUtils.isEmpty(tokenRequest.getTokenToRevoke())) {
            this.revokeAccessToken(tokenRequest.getTokenToRevoke(), tokenRequest.getClientId(),
                    tokenRequest.getClientSecret());
        }

        // When validity time set to a negative value, a token is considered never to expire.
        if (tokenRequest.getValidityPeriod() == -1L) {
            // Setting a different negative value if the set value is -1 (-1 will be ignored by TokenValidator)
            tokenRequest.setValidityPeriod(-2L);
        }

        HttpResponse response = null;
        try {
            if (KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                try {
                    String url =
                            keyManagerConfigs.getTokenEndpoint() + "?" + KeyManagerConstants.OAUTH_CLIENT_GRANT + "="
                                    + KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE + "&"
                                    + KeyManagerConstants.OAUTH_CLIENT_SCOPE + "=" + URLEncoder
                                    .encode(tokenRequest.getScopes());
                    Map<String, String> map = new HashMap<>();
                    map.put(KeyManagerConstants.AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(
                            (tokenRequest.getClientId() + ":" + tokenRequest.getClientSecret())
                                    .getBytes(Charset.defaultCharset())));
                    response = restCallUtil.postRequest(new URI(url), null, null, Entity.text(""),
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE, map);
                } catch (APIManagementException e) {
                    throw new KeyManagementException("Error occurred while invoking token endpoint", e,
                            ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
                } catch (URISyntaxException e) {
                    throw new KeyManagementException("Error occurred while parsing token endpoint", e,
                            ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
                }
            } else if (KeyManagerConstants.PASSWORD_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                try {
                    String url =
                            keyManagerConfigs.getTokenEndpoint() + "?" + KeyManagerConstants.OAUTH_CLIENT_GRANT + "="
                                    + KeyManagerConstants.PASSWORD_GRANT_TYPE + "&"
                                    + KeyManagerConstants.OAUTH_CLIENT_ID + "=" + tokenRequest.getClientId() + "&"
                                    + KeyManagerConstants.OAUTH_CLIENT_SCOPE + "=" + URLEncoder
                                    .encode(tokenRequest.getScopes());
                    Map<String, String> map = new HashMap<>();
                    map.put(KeyManagerConstants.AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder().encodeToString(
                            (tokenRequest.getClientId() + ":" + tokenRequest.getClientSecret())
                                    .getBytes(Charset.defaultCharset())));
                    String payload =
                            KeyManagerConstants.OAUTH_CLIENT_USERNAME + "=" + tokenRequest.getResourceOwnerUsername()
                                    + "&" + KeyManagerConstants.OAUTH_CLIENT_PASSWORD + "=" + tokenRequest
                                    .getResourceOwnerPassword();
                    response = restCallUtil.postRequest(new URI(url), null, null, Entity.text(payload),
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE, map);
                } catch (APIManagementException e) {
                    throw new KeyManagementException("Error occurred while invoking token endpoint", e,
                            ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
                } catch (URISyntaxException e) {
                    throw new KeyManagementException("Error occurred while parsing token endpoint", e,
                            ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
                }
            } else if (KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                //todo: implement
            } else if (KeyManagerConstants.REFRESH_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                //todo: implement
            } else {
                throw new KeyManagementException(
                        "Invalid access token request. Unsupported grant type: " + tokenRequest.getGrantType(),
                        ExceptionCodes.INVALID_TOKEN_REQUEST);
            }
        } catch (APIManagementException ex) {
            throw new KeyManagementException("Token generation request failed. Error: " + ex.getMessage(), ex,
                    ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        if (response == null) {
            throw new KeyManagementException("Error occurred while generating an access token. " + "Response is null",
                    ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }
        APIUtils.logDebug("Token endpoint Response code: " + response.getResponseCode(), log);
        APIUtils.logDebug("Token endpoint Response: " + response.getResults(), log);

        if (response.getResponseCode() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {   //200 - Success
            APIUtils.logDebug("A new access token is successfully generated.", log);
            try {
                JSONParser jsonParser = new JSONParser();
                Object obj = jsonParser.parse(response.getResults());
                JSONObject jsonObject = (JSONObject) obj;

                AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
                accessTokenInfo.setAccessToken((String) jsonObject.get(KeyManagerConstants.OAUTH_RESPONSE_ACCESSTOKEN));
                accessTokenInfo.setScopes((String) jsonObject.get(KeyManagerConstants.OAUTH_RESPONSE_SCOPE));
                accessTokenInfo
                        .setRefreshToken((String) jsonObject.get(KeyManagerConstants.OAUTH_RESPONSE_REFRESH_TOKEN));
                //accessTokenInfo.setIdToken((String) jsonObject.get("refresh_token"));
                accessTokenInfo
                        .setValidityPeriod((long) jsonObject.get(KeyManagerConstants.OAUTH_RESPONSE_EXPIRY_TIME));
                return accessTokenInfo;
            } catch (ParseException e) {
                throw new KeyManagementException("Error occurred while parsing token response", e,
                        ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
            }
        } else {  //Error case
            throw new KeyManagementException(
                    "Token generation request failed. HTTP error code: " + response.getResponseCode()
                            + " Error Response Body: " + response.getResults(),
                    ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException {
        APIUtils.logDebug("Token introspection request is being sent.", log);
        HttpResponse response;
        try {
            String url = keyManagerConfigs.getIntrospectEndpoint();
            String payload = KeyManagerConstants.OAUTH_TOKEN + "=" + accessToken;
            response = restCallUtil
                    .postRequest(new URI(url), MediaType.APPLICATION_JSON_TYPE, null, Entity.text(payload),
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE, Collections.emptyMap());
        } catch (URISyntaxException e) {
            throw new KeyManagementException("Error occurred while parsing introspecting endpoint", e,
                    ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error occurred while invoking introspecting endpoint", e,
                    ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }

        if (response == null) {
            throw new KeyManagementException("Error occurred while introspecting access token. " + "Response is null",
                    ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }
        APIUtils.logDebug("Introspect Response code: " + response.getResponseCode(), log);
        APIUtils.logDebug("Introspect Response: " + response.getResults(), log);

        if (response.getResponseCode() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
            APIUtils.logDebug("Token introspection is successful", log);
            try {
                JSONParser jsonParser = new JSONParser();
                Object obj = jsonParser.parse(response.getResults());
                JSONObject jsonObject = (JSONObject) obj;
                AccessTokenInfo tokenInfo = new AccessTokenInfo();
                boolean active = (Boolean) jsonObject.get("active");
                if (active) {
                    tokenInfo.setTokenValid(true);
                    tokenInfo.setAccessToken(accessToken);
                    //                    tokenInfo.setScopes((String) jsonObject.get("scope"));
                    tokenInfo.setScopes((String) jsonObject
                            .get("apim:api_delete apim:api_publish apim:external_services_discover "
                                    + "apim:subscription_block apim:api_update apim:subscription_view apim:api_create "
                                    + "apim:apidef_update apim:api_view openid"));
                    tokenInfo.setConsumerKey((String) jsonObject.get("clientId"));
                    tokenInfo.setEndUserName("admin");
                    tokenInfo.setIssuedTime((long) jsonObject.get(KeyManagerConstants.OAUTH2_TOKEN_ISSUED_TIME));
                    tokenInfo.setExpiryTime((long) jsonObject.get(KeyManagerConstants.OAUTH2_TOKEN_EXP_TIME));

                    long validityPeriod = tokenInfo.getExpiryTime() - tokenInfo.getIssuedTime();
                    tokenInfo.setValidityPeriod(validityPeriod);
                } else {
                    tokenInfo.setTokenValid(false);
                    log.error("Invalid or expired access token received.");
                    tokenInfo.setErrorCode(KeyManagerConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                }
                return tokenInfo;
            } catch (ParseException e) {
                throw new KeyManagementException("Error occurred while parsing token introspection response", e,
                        ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
            }
        } else {
            throw new KeyManagementException(
                    "Token introspection request failed. HTTP error code: " + response.getResponseCode()
                            + " Error Response Body: " + response.getResults(),
                    ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException {
        return null;
    }

    @Override
    public void revokeAccessToken(String accessToken, String clientId, String clientSecret)
            throws KeyManagementException {

    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagementException {

    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagementException {
        return false;
    }

    @Override
    public Map getResourceByApiId(String apiId) throws KeyManagementException {
        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws KeyManagementException {
        return false;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws KeyManagementException {

    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws KeyManagementException {

    }

    @Override
    public boolean registerScope(Scope scope) throws KeyManagementException {
        return false;
    }

    @Override
    public Scope retrieveScope(String name) throws KeyManagementException {
        return null;
    }

    @Override
    public boolean updateScope(Scope scope) throws KeyManagementException {
        return false;
    }

    @Override
    public boolean deleteScope(String name) throws KeyManagementException {
        return false;
    }

    private OAuthApplicationInfo getOAuthApplicationInfo(HttpResponse response) throws ParseException {
        String body = response.getResults();
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(body);
        JSONObject jsonObject = (JSONObject) obj;

        OAuthApplicationInfo oAuthApplicationInfoResponse = new OAuthApplicationInfo();
        oAuthApplicationInfoResponse.setClientName(KeyManagerConstants.APPLICATION_CLIENT_NAME);
        oAuthApplicationInfoResponse.setClientId((String) jsonObject.get(KeyManagerConstants.APPLICATION_CLIENT_ID));
        oAuthApplicationInfoResponse
                .setClientSecret((String) jsonObject.get(KeyManagerConstants.APPLICATION_CLIENT_SECRET));
        //        oAuthApplicationInfoResponse.setGrantTypes(dcrClientInfoResponse.getGrantTypes());
        //        oAuthApplicationInfoResponse.setCallBackURL(dcrClientInfoResponse.getRedirectURIs().get(0));
        return oAuthApplicationInfoResponse;
    }
}
