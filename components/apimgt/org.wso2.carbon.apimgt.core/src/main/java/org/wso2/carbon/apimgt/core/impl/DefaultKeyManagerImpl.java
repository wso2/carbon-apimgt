/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.core.impl;

import feign.Response;
import feign.gson.GsonDecoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStub;
import org.wso2.carbon.apimgt.core.auth.DCRMServiceStubFactory;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubs;
import org.wso2.carbon.apimgt.core.auth.OAuth2ServiceStubsFactory;
import org.wso2.carbon.apimgt.core.auth.dto.DCRClientInfo;
import org.wso2.carbon.apimgt.core.auth.dto.DCRError;
//import org.wso2.carbon.apimgt.core.auth.dto.OAuth2IntrospectionResponse;
import org.wso2.carbon.apimgt.core.auth.dto.OAuth2IntrospectionResponse;
import org.wso2.carbon.apimgt.core.auth.dto.OAuth2TokenInfo;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.io.IOException;
import java.util.Map;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class DefaultKeyManagerImpl implements KeyManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultKeyManagerImpl.class);

    private DCRMServiceStub dcrmServiceStub;
    private OAuth2ServiceStubs oAuth2ServiceStubs;

    /**
     * Default Constructor
     *
     * @throws APIManagementException if error occurred while instantiating DefaultKeyManagerImpl
     */
    public DefaultKeyManagerImpl() throws APIManagementException {
        this(DCRMServiceStubFactory.getDCRMServiceStub(), OAuth2ServiceStubsFactory.getOAuth2ServiceStubs());
    }

    /**
     * Constructor
     *
     * @param dcrmServiceStub    Service stub for DCR(M) service
     * @param oAuth2ServiceStubs Service stub for OAuth2 services
     * @throws APIManagementException if error occurred while instantiating DefaultKeyManagerImpl
     */
    public DefaultKeyManagerImpl(DCRMServiceStub dcrmServiceStub, OAuth2ServiceStubs oAuth2ServiceStubs)
            throws APIManagementException {
        this.dcrmServiceStub = dcrmServiceStub;
        this.oAuth2ServiceStubs = oAuth2ServiceStubs;
    }

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws KeyManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        if (log.isDebugEnabled()) {
            log.debug("Creating OAuth2 application: " + oAuthApplicationInfo.toString());
        }

        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(KeyManagerConstants.APP_KEY_TYPE);
        if (keyType != null) {  //Derive oauth2 app name based on key type and user input for app name
            applicationName = applicationName + '_' + keyType;
        }

        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(applicationName);
        dcrClientInfo.setGrantTypes(oAuthApplicationInfo.getGrantTypes());
        dcrClientInfo.addCallbackUrl(oAuthApplicationInfo.getCallbackUrl());

        Response response = dcrmServiceStub.registerApplication(dcrClientInfo);
        if (response == null) {
            throw new KeyManagementException("Error occurred while DCR application creation. Response is null",
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_201_CREATED) {  //201 - Success
            try {
                OAuthApplicationInfo oAuthApplicationInfoResponse = getOAuthApplicationInfo(response);
                //setting original parameter list
                oAuthApplicationInfoResponse.setParameters(oAuthApplicationInfo.getParameters());
                if (log.isDebugEnabled()) {
                    log.debug("OAuth2 application created: " + oAuthApplicationInfoResponse.toString());
                }
                return oAuthApplicationInfoResponse;
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing the DCR application creation response " +
                        "message.", e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            }
        } else if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST) {  //400 - Known Error
            try {
                DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                throw new KeyManagementException("Error occurred while DCR application creation. Error: " +
                        error.getError() + ". Error Description: " + error.getErrorDescription() + ". Status Code: " +
                        response.status(), ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing the DCR error message.", e,
                        ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            }
        } else {  //Unknown Error
            throw new KeyManagementException("Error occurred while DCR application creation. Error: " +
                    response.body().toString() + " Status Code: " + response.status(),
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest oauthAppRequest) throws KeyManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        if (log.isDebugEnabled()) {
            log.debug("Updating OAuth2 application with : " + oAuthApplicationInfo.toString());
        }

        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(KeyManagerConstants.APP_KEY_TYPE);
        if (keyType != null) {  //Derive oauth2 app name based on key type and user input for app name
            applicationName = applicationName + "_" + keyType;
        }

        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(applicationName);
        dcrClientInfo.setClientId(oAuthApplicationInfo.getClientId());
        dcrClientInfo.setClientSecret(oAuthApplicationInfo.getClientSecret());
        dcrClientInfo.addCallbackUrl(oAuthApplicationInfo.getCallbackUrl());
        dcrClientInfo.setGrantTypes(oAuthApplicationInfo.getGrantTypes());

        Response response = dcrmServiceStub.updateApplication(dcrClientInfo, dcrClientInfo.getClientId());
        if (response == null) {
            throw new KeyManagementException("Error occurred while updating DCR application. Response is null",
                    ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {   //200 - Success
            try {
                OAuthApplicationInfo oAuthApplicationInfoResponse = getOAuthApplicationInfo(response);
                //setting original parameter list
                oAuthApplicationInfoResponse.setParameters(oAuthApplicationInfo.getParameters());
                if (log.isDebugEnabled()) {
                    log.debug("OAuth2 application updated: " + oAuthApplicationInfoResponse.toString());
                }
                return oAuthApplicationInfoResponse;
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing the DCR application update response " +
                        "message.", e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }
        } else if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST) {  //400 - Known Error
            try {
                DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                throw new KeyManagementException("Error occurred while updating DCR application. Error: " +
                        error.getError() + ". Error Description: " + error.getErrorDescription() + ". Status Code: " +
                        response.status(), ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing the DCR error message.", e,
                        ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }
        } else {  //Unknown Error
            throw new KeyManagementException("Error occurred while updating DCR application. Error: " +
                    response.body().toString() + " Status Code: " + response.status(),
                    ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
    }

    @Override
    public void deleteApplication(String consumerKey) throws KeyManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting OAuth application for consumer key: " + consumerKey);
        }
        if (StringUtils.isEmpty(consumerKey)) {
            throw new KeyManagementException("Unable to delete OAuth Application. Consumer Key is null or empty",
                    ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
        }
        Response response = dcrmServiceStub.deleteApplication(consumerKey);
        if (response == null) {
            throw new KeyManagementException("Error occurred while deleting DCR application. Response is null",
                    ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
        }
        if (response.status() != APIMgtConstants.HTTPStatusCodes.SC_204_NO_CONTENT) {
            throw new KeyManagementException("Error occurred while deleting DCR application. Error: " +
                    response.body().toString() + " Status Code: " + response.status(),
                    ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
        }

        if (log.isDebugEnabled()) {
            log.debug("OAuth2 application for consumer key: " + consumerKey + " deleted.");
        }
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving OAuth application for consumer key: " + consumerKey);
        }

        if (StringUtils.isEmpty(consumerKey)) {
            throw new KeyManagementException("Consumer Key is null or empty",
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        Response response = dcrmServiceStub.getApplication(consumerKey);
        if (response == null) {
            throw new KeyManagementException("Error occurred while retrieving DCR application. Response is null",
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }
        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {   //200 - Success
            try {
                OAuthApplicationInfo oAuthApplicationInfoResponse = getOAuthApplicationInfo(response);
                if (log.isDebugEnabled()) {
                    log.debug("OAuth2 application retrieved: " + oAuthApplicationInfoResponse.toString());
                }
                return oAuthApplicationInfoResponse;
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing the DCR application retrieval " +
                        "response message.", e, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            }
        } else if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST) {  //400 - Known Error
            try {
                DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                throw new KeyManagementException("Error occurred while retrieving DCR application. Error: " +
                        error.getError() + ". Error Description: " + error.getErrorDescription() + ". Status Code: " +
                        response.status(), ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing the DCR error message.", e,
                        ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            }
        } else {  //Unknown Error
            throw new KeyManagementException("Error occurred while retrieving DCR application. Error: " +
                    response.body().toString() + " Status Code: " + response.status(),
                    ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
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

        Response response;
        try {
            if (KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                response = oAuth2ServiceStubs.getTokenServiceStub().generateClientCredentialsGrantAccessToken(
                        tokenRequest.getScopes(), tokenRequest.getValidityPeriod(), tokenRequest.getClientId(),
                        tokenRequest.getClientSecret());
            } else if (KeyManagerConstants.PASSWORD_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                response = oAuth2ServiceStubs.getTokenServiceStub().generatePasswordGrantAccessToken(
                        tokenRequest.getResourceOwnerUsername(), tokenRequest.getResourceOwnerPassword(),
                        tokenRequest.getScopes(), tokenRequest.getValidityPeriod(), tokenRequest.getClientId(),
                        tokenRequest.getClientSecret());
            } else if (KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                response = oAuth2ServiceStubs.getTokenServiceStub().generateAuthCodeGrantAccessToken(
                        tokenRequest.getAuthorizationCode(), tokenRequest.getCallbackURI(), tokenRequest.getScopes(),
                        tokenRequest.getValidityPeriod(), tokenRequest.getClientId(), tokenRequest.getClientSecret());
            } else if (KeyManagerConstants.REFRESH_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                response = oAuth2ServiceStubs.getTokenServiceStub().generateRefreshGrantAccessToken(
                        tokenRequest.getRefreshToken(), tokenRequest.getScopes(), tokenRequest.getValidityPeriod(),
                        tokenRequest.getClientId(), tokenRequest.getClientSecret());
            } else {
                throw new KeyManagementException("Invalid access token request. Unsupported grant type: "
                        + tokenRequest.getGrantType(), ExceptionCodes.INVALID_TOKEN_REQUEST);
            }
        } catch (APIManagementException ex) {
            throw new KeyManagementException("Token generation request failed. Error: " + ex.getMessage(), ex,
                    ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        if (response == null) {
            throw new KeyManagementException("Error occurred while generating an access token. " +
                    "Response is null", ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {   //200 - Success
            log.debug("A new access token is successfully generated.");
            try {
                OAuth2TokenInfo oAuth2TokenInfo = (OAuth2TokenInfo) new GsonDecoder().decode(response,
                        OAuth2TokenInfo.class);
                AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
                accessTokenInfo.setAccessToken(oAuth2TokenInfo.getAccessToken());
                accessTokenInfo.setScopes(oAuth2TokenInfo.getScope());
                accessTokenInfo.setRefreshToken(oAuth2TokenInfo.getRefreshToken());
                accessTokenInfo.setValidityPeriod(oAuth2TokenInfo.getExpiresIn());
                return accessTokenInfo;
            } catch (IOException e) {
                throw new KeyManagementException("Error occurred while parsing token response", e,
                        ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
            }
        } else {  //Error case
            throw new KeyManagementException("Token generation request failed. HTTP error code: " + response.status() +
                    " Error Response Body: " + response.body().toString(),
                    ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException {
        log.debug("Token introspection request is being sent.");
        try {
////<<<<<<< HEAD
//            createSSLConnection();
//            String introspectEndpoint = getKeyManagerEndPoint("/oauth2/introspect");
//            //System.getProperty("introspectEndpoint", "https://localhost:9443/oauth2/introspect");
//            url = new URL(introspectEndpoint);
//            urlConn = (HttpURLConnection) url.openConnection();
//            urlConn.setDoOutput(true);
//            urlConn.setRequestMethod("POST");
//            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            String clientEncoded = Base64.getEncoder().encodeToString((System.getProperty("systemUsername",
//                    "admin") + ":" + System.getProperty("systemUserPwd", "admin"))
//                    .getBytes(StandardCharsets.UTF_8));
//            urlConn.setRequestProperty("Authorization", "Basic " + clientEncoded); //temp fix
//            urlConn.getOutputStream().write(("token=" + accessToken).getBytes("UTF-8"));
//            //String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
//            String responseStr = "{\n" + "    \"active\": true,\n"
//                    + "    \"scope\": \"apim:api_view apim:api_create apim:api_publish apim:tier_view
            // apim:tier_manage "
//                    + "apim:subscription_view apim:subscription_block apim:subscribe apim:workflow_approve\",\n"
//                    + "    \"exp\": \"3600000\",\n" + "    \"iat\": \"1496115930752\"\n" + "}";
//            JsonParser parser = new JsonParser();
//            JsonObject jObj = parser.parse(responseStr).getAsJsonObject();
//            boolean active = jObj.getAsJsonPrimitive("active").getAsBoolean();
//            if (active) {
//                //String consumerKey = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_ID).getAsString();
//                //String endUser = jObj.getAsJsonPrimitive(KeyManagerConstants.USERNAME).getAsString();
//                long exp = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_EXP_TIME).getAsLong();
//                long issuedTime = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_ISSUED_TIME).getAsLong();
//                String scopes = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SCOPE).getAsString();
//                if (scopes != null) {
//                    String[] scopesArray = scopes.split(" ");
//                    tokenInfo.setScopes(scopesArray);
//                }
//                tokenInfo.setTokenValid(true);
//                tokenInfo.setAccessToken(accessToken);
//                //tokenInfo.setConsumerKey(consumerKey);
//                //tokenInfo.setEndUserName(endUser);
//                tokenInfo.setIssuedTime(issuedTime);
//
//                // Convert Expiry Time to milliseconds.
//                if (exp == Long.MAX_VALUE) {
//                    tokenInfo.setValidityPeriod(Long.MAX_VALUE);
//                } else {
//                    tokenInfo.setValidityPeriod(exp * 1000);
//=======
            Response response = oAuth2ServiceStubs.getIntrospectionServiceStub().introspectToken(accessToken);
            if (response == null) {
                throw new KeyManagementException("Error occurred while introspecting access token. " +
                        "Response is null", ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
            }
            if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
                log.debug("Token introspection is successful");

                try {
                    OAuth2IntrospectionResponse introspectResponse = (OAuth2IntrospectionResponse) new GsonDecoder()
                            .decode(response, OAuth2IntrospectionResponse.class);
                    AccessTokenInfo tokenInfo = new AccessTokenInfo();
                    boolean active = introspectResponse.isActive();
                    if (active) {
                        tokenInfo.setTokenValid(true);
                        tokenInfo.setAccessToken(accessToken);
                        tokenInfo.setScopes(introspectResponse.getScope());
                        tokenInfo.setConsumerKey(introspectResponse.getClientId());
                        tokenInfo.setEndUserName(introspectResponse.getUsername());
                        tokenInfo.setIssuedTime(introspectResponse.getIat());
                        tokenInfo.setExpiryTime(introspectResponse.getExp());

                        long validityPeriod = introspectResponse.getExp() - introspectResponse.getIat();
                        tokenInfo.setValidityPeriod(validityPeriod);
                    } else {
                        tokenInfo.setTokenValid(false);
                        log.error("Invalid or expired access token received.");
                        tokenInfo.setErrorCode(KeyManagerConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                    }
                    return tokenInfo;
                } catch (IOException e) {
                    throw new KeyManagementException("Error occurred while parsing token introspection response", e,
                            ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
//>>>>>>> master
                }
            } else {
                throw new KeyManagementException("Token introspection request failed. HTTP error code: "
                        + response.status() + " Error Response Body: " + response.body().toString(),
                        ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
            }
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error occurred while introspecting access token.", e,
                    ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        }
    }

    @Override
    public void revokeAccessToken(String accessToken, String clientId, String clientSecret)
            throws KeyManagementException {
        log.debug("Revoking access token");
        try {
            Response response = oAuth2ServiceStubs.getRevokeServiceStub().revokeToken(accessToken, clientId,
                    clientSecret);
            if (response == null) {
                throw new KeyManagementException("Error occurred while revoking current access token. " +
                        "Response is null", ExceptionCodes.ACCESS_TOKEN_REVOKE_FAILED);
            }
            if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_200_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully revoked access token: " + accessToken);
                }
            } else {
                throw new KeyManagementException("Token revocation failed. HTTP error code: " + response.status()
                        + " Error Response Body: " + response.body().toString(),
                        ExceptionCodes.ACCESS_TOKEN_REVOKE_FAILED);
            }
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error occurred while revoking current access token", e,
                    ExceptionCodes.ACCESS_TOKEN_REVOKE_FAILED);
        }
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest)
            throws KeyManagementException {
        //initiate OAuthApplicationInfo
        OAuthApplicationInfo oAuthApplicationInfo = appInfoRequest.getOAuthApplicationInfo();
        //String consumerKey = oAuthApplicationInfo.getClientId();
        String tokenScope = (String) oAuthApplicationInfo.getParameter(KeyManagerConstants.OAUTH_CLIENT_TOKEN_SCOPE);
        String tokenScopes[] = new String[1];
        tokenScopes[0] = tokenScope;
        String clientSecret = (String) oAuthApplicationInfo.getParameter(KeyManagerConstants.OAUTH_CLIENT_ID);
        oAuthApplicationInfo.setClientSecret(clientSecret);
        //for the first time we set default time period. TO-DO Add the validity period config value
        oAuthApplicationInfo.addParameter(KeyManagerConstants.VALIDITY_PERIOD,
                System.getProperty(KeyManagerConstants.VALIDITY_PERIOD, "3600"));

        //check whether given consumer key and secret match or not. If it does not match throw an exception.
        try {
            /*  TO-DO -ADD logIC TO RETIRVE OAUTH2 APP

               */
        } catch (Exception e) {
            String msg = "Some thing went wrong while getting OAuth application for given consumer key "
                    + oAuthApplicationInfo.getClientId();
            log.error(msg, e);
            throw new KeyManagementException(msg, e, ExceptionCodes.OAUTH2_APP_MAP_FAILED);
        } finally {

        }
        /* TO-DO
        if (info != null && info.getClientId() == null) {
            return null;
        } */

        oAuthApplicationInfo.addParameter(KeyManagerConstants.OAUTH_CLIENT_TOKEN_SCOPE, tokenScopes);
        APIUtils.logDebug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId(),
                log);

        return oAuthApplicationInfo;
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagementException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagementException {
        return true;
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

    private OAuthApplicationInfo getOAuthApplicationInfo(Response response) throws IOException {
        OAuthApplicationInfo oAuthApplicationInfoResponse = new OAuthApplicationInfo();
        DCRClientInfo dcrClientInfoResponse = (DCRClientInfo) new GsonDecoder().decode(response, DCRClientInfo.class);
        oAuthApplicationInfoResponse.setClientName(dcrClientInfoResponse.getClientName());
        oAuthApplicationInfoResponse.setClientId(dcrClientInfoResponse.getClientId());
        oAuthApplicationInfoResponse.setClientSecret(dcrClientInfoResponse.getClientSecret());
        oAuthApplicationInfoResponse.setGrantTypes(dcrClientInfoResponse.getGrantTypes());
        oAuthApplicationInfoResponse.setCallbackUrl(dcrClientInfoResponse.getRedirectURIs().get(0));
        return oAuthApplicationInfoResponse;
    }
}
