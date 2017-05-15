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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */

public class DefaultKeyManagerImpl implements KeyManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultKeyManagerImpl.class);

    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_REFRESH_TOKEN = "refresh_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "client_credentials";
    private static final String PASSWORD_GRANT_TYPE = "password";
    private static final String REFRESH_GRANT_TYPE = "refresh_token";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";
    private static final String EXTERNEL_KEYMANAGER_ENDPOINT = "https://localhost:9443";
    private static final String INTERNAL_KEYMANAGER_ENDPOINT = "https://localhost:9292/keyserver";
    private static boolean isExternalKeyManager = false;

    /**
     * Create the oauth2 application with calling DCR endpoint of WSO2 IS
     *
     * @param oauthAppRequest - this object contains values of oAuth app properties.
     * @return OAuthApplicationInfo -this object contains oauth2 app details
     * @throws org.wso2.carbon.apimgt.core.exception.KeyManagementException throws KeyManagerException
     */

    @Override public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest)
            throws KeyManagementException {
        // OAuthApplications are created by calling to DCR endpoint of WSO2 IS
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(KeyManagerConstants.APP_KEY_TYPE);
        if (keyType != null) {  //Derive oauth2 app name based on key type and user input for app name
            applicationName = applicationName + "_" + keyType;
        }

        APIUtils.logDebug("Trying to create OAuth application :", log);

        //Create json payload for DCR endpoint
        JsonObject json = new JsonObject();
        JsonArray callbackArray = new JsonArray();
        if (oAuthApplicationInfo.getCallbackUrl() != null) {
            callbackArray.add(oAuthApplicationInfo.getCallbackUrl());
        } else {
            callbackArray.add("");
        }
        json.add(KeyManagerConstants.OAUTH_REDIRECT_URIS, callbackArray);
        json.addProperty(KeyManagerConstants.OAUTH_CLIENT_NAME, applicationName);
        json.addProperty(KeyManagerConstants.OAUTH_CLIENT_OWNER, oAuthApplicationInfo.getAppOwner());
        JsonArray grantArray = new JsonArray();
        if (oAuthApplicationInfo.getGrantTypes() != null) {
            for (String grantType : oAuthApplicationInfo.getGrantTypes()) {
                grantArray.add(grantType);
            }
            json.add(KeyManagerConstants.OAUTH_CLIENT_GRANTS, grantArray);
        } else { // Temp if condition because generate keys in store application does not send any of the grant types
            grantArray.add(GRANT_TYPE_VALUE);
            json.add(KeyManagerConstants.OAUTH_CLIENT_GRANTS, grantArray);
        }

        URL url;
        HttpURLConnection urlConn = null;
        try {
            createSSLConnection();
            // Calling DCR endpoint of IS
            String dcrEndpoint = getKeyManagerEndPoint("/identity/connect/register");
                    /*System.getProperty("dcrEndpoint",
                    "https://localhost:9443/identity/connect/register");*/
            url = new URL(dcrEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("content-type", "application/json");
            String clientEncoded = Base64.getEncoder().encodeToString((System.getProperty("systemUsername",
                    "admin") + ":" + System.getProperty("systemUserPwd", "admin"))
                    .getBytes(StandardCharsets.UTF_8));
            urlConn.setRequestProperty("Authorization", "Basic " + clientEncoded); //temp fix
            urlConn.getOutputStream().write((json.toString()).getBytes("UTF-8"));
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 201) {  //If the DCR call is success
                String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
                JsonParser parser = new JsonParser();
                JsonObject jObj = parser.parse(responseStr).getAsJsonObject();
                String consumerKey = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_ID).getAsString();
                String consumerSecret = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SECRET).getAsString();
                String clientName = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_NAME).getAsString();
                String grantTypes = "";
                if (jObj.has(KeyManagerConstants.OAUTH_CLIENT_GRANTS)) {
                    grantTypes = jObj.getAsJsonArray(KeyManagerConstants.OAUTH_CLIENT_GRANTS).toString();
                }

                oAuthApplicationInfo.setClientName(clientName);
                oAuthApplicationInfo.setClientId(consumerKey);
                oAuthApplicationInfo.setClientSecret(consumerSecret);
                oAuthApplicationInfo.setGrantTypes(Arrays.asList(grantTypes.split(",")));

            } else { //If DCR call fails
                throw new KeyManagementException("OAuth app does not contains required data  : " + applicationName,
                        ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            }
        } catch (IOException e) {
            String errorMsg = "Can not create OAuth application  : " + applicationName;
            log.error(errorMsg, e);
            throw new KeyManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } catch (JsonSyntaxException e) {
            String errorMsg = "Error while processing the response returned from DCR endpoint.Can not create" +
                    " OAuth application : " + applicationName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new KeyManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            String errorMsg = "Error while connecting to the DCR endpoint.Can not create" +
                    " OAuth application : " + applicationName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new KeyManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
        return oAuthApplicationInfo;
    }

    @Override public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws KeyManagementException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete oauth2 application with calling DCR endpoint of WSO2 IS
     *
     * @param consumerKey - will take consumer key as parameter
     * @throws org.wso2.carbon.apimgt.core.exception.KeyManagementException -throws KeyManagementException type
     */

    @Override public void deleteApplication(String consumerKey) throws KeyManagementException {
        APIUtils.logDebug("Trying to delete OAuth application for consumer key :" + consumerKey, log);
        if (consumerKey == null || consumerKey.isEmpty()) { //If the consumer key empty or null,returns
            return;
        }
        APIUtils.logDebug("Getting OAuth App for " + consumerKey + consumerKey, log);

            /*code to check the client app exists/not
            * */

        // if (spAppName == null) {
        //   log.info("Couldn't find OAuth App for Consumer Key : " + consumerKey);
        //  return;
        // }
        URL url;
        HttpURLConnection urlConn = null;

        try {
            // Calling DCR endpoint of IS
            createSSLConnection();
            String dcrEndpoint = getKeyManagerEndPoint("/identity/connect/register/") + consumerKey;
                    /*System.getProperty("dcrEndpoint", "https://localhost:9443/identity/connect/register/") +
                            consumerKey;*/
            url = new URL(dcrEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("DELETE");

            int responseCode = urlConn.getResponseCode();
            if (responseCode != 200) { //If DCR call fails
                log.error("Error while deleting the client-" + consumerKey);
                throw new KeyManagementException("Error while deleting the client.HTTP error code is:" + responseCode,
                        ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
            }

        } catch (IOException e) {
            log.error("Error while deleting the client- " + consumerKey, e);
            throw new KeyManagementException("Error while deleting the client- " + consumerKey, e,
                    ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            log.error("Error while connecting to DCR endpoint for deleting the client- " + consumerKey, e);
            throw new KeyManagementException("Error while connecting to DCR endpoint for deleting the client- "
                    + consumerKey, e,
                    ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

    }

    @Override public OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException {

        APIUtils.logDebug("Trying to retrieve OAuth application for consumer key :" + consumerKey, log);
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        URL url;
        HttpURLConnection urlConn = null;
        try {
            createSSLConnection();
            // Calling DCR endpoint of IS using consumer key
            String dcrEndpoint = getKeyManagerEndPoint("/identity/connect/register/") + consumerKey;
                   /* System.getProperty("dcrEndpoint", "https://localhost:9443/identity/connect/register/" +
                    consumerKey);*/
            url = new URL(dcrEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("content-type", "application/json");
            String clientEncoded = Base64.getEncoder().encodeToString((System.getProperty("systemUsername",
                    "admin") + ":" + System.getProperty("systemUserPwd", "admin"))
                    .getBytes(StandardCharsets.UTF_8));
            urlConn.setRequestProperty("Authorization", "Basic " + clientEncoded); //temp fix
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {  //If the DCR call is success
                String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
                JsonParser parser = new JsonParser();
                JsonObject jObj = parser.parse(responseStr).getAsJsonObject();
                String consumerSecret = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SECRET).getAsString();
                String clientName = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_NAME).getAsString();
                String grantTypes = "";
                if (jObj.has(KeyManagerConstants.OAUTH_CLIENT_GRANTS)) {
                    grantTypes = jObj.getAsJsonArray(KeyManagerConstants.OAUTH_CLIENT_GRANTS).getAsString();
                }

                oAuthApplicationInfo.setClientName(clientName);
                oAuthApplicationInfo.setClientId(consumerKey);
                oAuthApplicationInfo.setClientSecret(consumerSecret);
                oAuthApplicationInfo.setGrantTypes(Arrays.asList(grantTypes.split(",")));
                oAuthApplicationInfo.addParameter(KeyManagerConstants
                        .VALIDITY_PERIOD, "3600");

            } else { //If DCR call fails
                throw new KeyManagementException("Error while getting oauth application info for key : " + consumerKey,
                        ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            }
        } catch (IOException e) {
            String errorMsg = "Error while getting application with key : " + consumerKey;
            log.error(errorMsg, e);
            throw new KeyManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        } catch (JsonSyntaxException e) {
            String errorMsg = "Error while processing the response returned from DCR endpoint.Can not find" +
                    " OAuth application : " + consumerKey;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
            throw new KeyManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            String errorMsg = "Error while connecting to the DCR endpoint.Can not retrieve the" +
                    " OAuth application.";
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new KeyManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

         /*} TO-DO-Add logic to exception handling in retrieving oauth2 application */
        //}
        return oAuthApplicationInfo;
    }

    /**
     * This method is to generate a new application access token
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from Store UI.
     * @return AccessTokenInfo which wraps application access token data
     * @throws org.wso2.carbon.apimgt.core.exception.KeyManagementException -throws KeyManagementException type
     */
    @Override public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest)
            throws KeyManagementException {
        String newAccessToken, refreshToken = null;
        long validityPeriod;
        AccessTokenInfo tokenInfo = null;

        if (tokenRequest == null) {
            log.warn("No information available to generate Token.");
            return null;
        }

        //TO-DO -ADD A CONFIG FOR TOKEN ENDPOINT
        String tokenEndpoint = getKeyManagerEndPoint("/oauth2/token");
        //System.getProperty("TokenEndpoint", "https://localhost:9443/oauth2/token");
        //TO-DO -ADD A CONFIG FOR REVOKE ENDPOINT
        String revokeEndpoint = getKeyManagerEndPoint("/oauth2/revoke");
        //System.getProperty("RevokeEndpoint", "https://localhost:9443/oauth2/revoke");
        ;

        // Call the /revoke only if there's a token to be revoked.

        URL url;
        HttpURLConnection urlConn = null;

        if (tokenRequest.getTokenToRevoke() != null && !"".equals(tokenRequest.getTokenToRevoke())) {
            //Revoke the Old Access Token
            try {
                createSSLConnection();
                url = new URL(revokeEndpoint);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoOutput(true);
                urlConn.setRequestMethod("POST");
                String postParams = KeyManagerConstants.OAUTH_CLIENT_ID + "=" + tokenRequest.getClientId() +
                        KeyManagerConstants.OAUTH_CLIENT_SECRET + "=" + tokenRequest.getClientSecret() +
                        KeyManagerConstants.OAUTH_TOKEN + "=" + tokenRequest.getTokenToRevoke();
                urlConn.getOutputStream().write((postParams).getBytes("UTF-8"));
                int responseCode = urlConn.getResponseCode();
                if (responseCode != 200) { //If token revoke failed
                    throw new RuntimeException("Token revoke failed : HTTP error code : " + responseCode);
                } else {
                    APIUtils.logDebug(
                            "Successfully submitted revoke request for old application token. HTTP status : 200", log);
                }
            } catch (IOException e) {
                String msg = "Error while revoking the existing token.";
                log.error(msg, e);
                throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                        APPLICATION_TOKEN_GENERATION_FAILED);
            } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
                String msg = "Error while connecting with the revoke endpoint for revoking the existing token.";
                log.error(msg, e);
                throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                        APPLICATION_TOKEN_GENERATION_FAILED);
            } finally {
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            }

        }
        //get default application access token name from config.
        //TO-DO -DEFINE APPICATION TOKEN SCOPES
        String applicationTokenScope = "";

        // When validity time set to a negative value, a token is considered never to expire.
        if (tokenRequest.getValidityPeriod() == -1L) {
            // Setting a different -ve value if the set value is -1 (-1 will be ignored by TokenValidator)
            tokenRequest.setValidityPeriod(-2);
        }

        //Generate New Access Token by client credentials grant type with calling Token API
        try {
            createSSLConnection();
            url = new URL(tokenEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String clientEncoded = Base64.getEncoder().encodeToString(
                    (tokenRequest.getClientId() + ":" + tokenRequest.getClientSecret())
                            .getBytes(StandardCharsets.UTF_8));
            urlConn.setRequestProperty("Authorization", "Basic " + clientEncoded);
            StringBuilder builder = new StringBuilder();
            builder.append(applicationTokenScope);
            for (String scope : tokenRequest.getScopes()) {
                builder.append(' ').append(scope);
            }
            String postParams = "";
            if (PASSWORD_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                postParams =
                        KeyManagerConstants.OAUTH_CLIENT_GRANT + "=" + PASSWORD_GRANT_TYPE + "&username=" + tokenRequest
                                .getResourceOwnerUsername() + "&password=" + tokenRequest.getResourceOwnerPassword();
            } else if (REFRESH_GRANT_TYPE.equals(tokenRequest.getGrantType())) {
                postParams = KeyManagerConstants.OAUTH_CLIENT_GRANT + "=" + REFRESH_GRANT_TYPE + "&"
                        + OAUTH_RESPONSE_REFRESH_TOKEN + "=" + tokenRequest.getRefreshToken();
            } else {
                postParams = KeyManagerConstants.OAUTH_CLIENT_GRANT + "=" + GRANT_TYPE_VALUE;
            }
            if (tokenRequest.getScopes().length > 0) {
                postParams += "&" + KeyManagerConstants.OAUTH_CLIENT_SCOPE + "=" + builder.toString();
            }
            postParams += "&" + GRANT_TYPE_PARAM_VALIDITY + "=" + Long.toString(tokenRequest.getValidityPeriod());
//            + "&" +
//                    GRANT_TYPE_PARAM_VALIDITY + "=" + Long.toString(tokenRequest.getValidityPeriod()) + "&" +
//                    KeyManagerConstants.OAUTH_CLIENT_ID + "=" + tokenRequest.getClientId() + "&" +
//                    KeyManagerConstants.OAUTH_CLIENT_SECRET + "=" + tokenRequest.getClientSecret() + "&" +
//                    KeyManagerConstants.OAUTH_CLIENT_SCOPE + "=" + builder.toString();

            urlConn.getOutputStream().write((postParams).getBytes("UTF-8"));
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 404) { //If token endpoint not found
                throw new KeyManagementException(
                        "Error occurred while calling token endpoint: HTTP error code : " + responseCode);
            } else if (responseCode == 401) { //If token generation failed
                throw new KeyManagementException("Invalid credentials provided. HTTP status code: " + responseCode,
                        ExceptionCodes.INVALID_CREDENTIALS);
            } else if (responseCode == 200) {
                APIUtils.logDebug("Successfully submitted token request for old application token. HTTP status : 200",
                        log);
                tokenInfo = new AccessTokenInfo();
                String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse(responseStr).getAsJsonObject();
                newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).getAsString();
                if (obj.has(OAUTH_RESPONSE_REFRESH_TOKEN)) {
                    refreshToken = obj.get(OAUTH_RESPONSE_REFRESH_TOKEN).getAsString();
                }
                validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());
                if (obj.has(KeyManagerConstants.OAUTH_CLIENT_SCOPE)) {
                    tokenInfo.setScopes((obj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SCOPE).getAsString()).
                            split(" "));
                }
                tokenInfo.setAccessToken(newAccessToken);
                tokenInfo.setValidityPeriod(validityPeriod);
                tokenInfo.setRefreshToken(refreshToken);
            } else {
                throw new KeyManagementException(
                        "Error occurred while getting token for grant type : " + tokenRequest.getGrantType(),
                        ExceptionCodes.APPLICATION_TOKEN_GENERATION_FAILED);
            }
        } catch (IOException e) {
            String msg = "Error while creating the new token for token regeneration.";
            log.error(msg, e);
            throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                    APPLICATION_TOKEN_GENERATION_FAILED);
        } catch (JsonSyntaxException e) {
            String msg = "Error while processing the response returned from token generation call.";
            log.error(msg, e);
            throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                    APPLICATION_TOKEN_GENERATION_FAILED);
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            String msg = "Error while connecting to the token generation endpoint.";
            log.error(msg, e);
            throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                    APPLICATION_TOKEN_GENERATION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

        return tokenInfo;
    }

    @Override public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException {
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        URL url;
        HttpURLConnection urlConn = null;
        try {
            createSSLConnection();
            String introspectEndpoint = getKeyManagerEndPoint("/oauth2/introspect");
            //System.getProperty("introspectEndpoint", "https://localhost:9443/oauth2/introspect");
            url = new URL(introspectEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String clientEncoded = Base64.getEncoder().encodeToString((System.getProperty("systemUsername",
                    "admin") + ":" + System.getProperty("systemUserPwd", "admin"))
                    .getBytes(StandardCharsets.UTF_8));
            urlConn.setRequestProperty("Authorization", "Basic " + clientEncoded); //temp fix
            urlConn.getOutputStream().write(("token=" + accessToken).getBytes("UTF-8"));
            String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
            JsonParser parser = new JsonParser();
            JsonObject jObj = parser.parse(responseStr).getAsJsonObject();
            boolean active = jObj.getAsJsonPrimitive("active").getAsBoolean();
            if (active) {
                //String consumerKey = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_ID).getAsString();
                //String endUser = jObj.getAsJsonPrimitive(KeyManagerConstants.USERNAME).getAsString();
                long exp = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_EXP_TIME).getAsLong();
                long issuedTime = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_ISSUED_TIME).getAsLong();
                String scopes = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SCOPE).getAsString();
                if (scopes != null) {
                    String[] scopesArray = scopes.split(" ");
                    tokenInfo.setScopes(scopesArray);
                }
                tokenInfo.setTokenValid(true);
                tokenInfo.setAccessToken(accessToken);
                //tokenInfo.setConsumerKey(consumerKey);
                //tokenInfo.setEndUserName(endUser);
                tokenInfo.setIssuedTime(issuedTime);

                // Convert Expiry Time to milliseconds.
                if (exp == Long.MAX_VALUE) {
                    tokenInfo.setValidityPeriod(Long.MAX_VALUE);
                } else {
                    tokenInfo.setValidityPeriod(exp * 1000);
                }

            } else {

                tokenInfo.setTokenValid(false);
                log.error("Invalid OAuth Token. ");
                tokenInfo.setErrorcode(KeyManagerConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return tokenInfo;

            }

        } catch (IOException e) {
            String msg = "Error while connecting to token introspect endpoint.";
            log.error(msg, e);
            throw new KeyManagementException(msg, e, ExceptionCodes.
                    TOKEN_INTROSPECTION_FAILED);
        } catch (JsonSyntaxException e) {
            String msg = "Error while processing the response returned from token introspect endpoint.";
            log.error("Error while processing the response returned from token introspect endpoint.", e);
            throw new KeyManagementException(msg, e, ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            String msg = "Error while connecting to the token introspect endpoint.";
            log.error("Error while connecting to the token introspect endpoint.", e);
            throw new KeyManagementException(msg, e, ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

        return tokenInfo;
    }

    /**
     * This method is called to revoke an existing access token which is used to log in to publisher,store or admin
     * apps.
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from UI.
     * @throws KeyManagementException   Exception while revoking the access token
     */
    @Override
    public void revokeLogInAccessToken(AccessTokenRequest tokenRequest) throws KeyManagementException {

        if (tokenRequest == null) {
            log.warn("No information available to generate Token.");
            return;
        }

        //TO-DO -ADD A CONFIG FOR REVOKE ENDPOINT
        String revokeEndpoint = getKeyManagerEndPoint("/oauth2/revoke");

        URL url;
        HttpURLConnection urlConn = null;

        if (tokenRequest.getTokenToRevoke() != null && !tokenRequest.getTokenToRevoke().isEmpty()) {
            //Revoke the Old Access Token
            try {
                createSSLConnection();
                url = new URL(revokeEndpoint);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoOutput(true);
                urlConn.setRequestMethod("POST");
                String clientEncoded = Base64.getEncoder().encodeToString(
                        (tokenRequest.getClientId() + ":" + tokenRequest.getClientSecret())
                                .getBytes(StandardCharsets.UTF_8));
                urlConn.setRequestProperty("Authorization", "Basic " + clientEncoded);
                String postParams = KeyManagerConstants.OAUTH_TOKEN + "=" + tokenRequest.getTokenToRevoke();
                urlConn.getOutputStream().write((postParams).getBytes("UTF-8"));
                int responseCode = urlConn.getResponseCode();
                if (responseCode != 200) { //If token revoke failed
                    throw new KeyManagementException("Token revoke failed : HTTP error code : " + responseCode,
                            ExceptionCodes.
                                    ACCESS_TOKEN_REVOKE_FAILED);
                } else {
                    APIUtils.logDebug(
                            "Successfully submitted revoke request for old access token. HTTP status : 200", log);
                }
            } catch (IOException e) {
                String msg = "Error while revoking the existing token.";
                log.error(msg, e);
                throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                        ACCESS_TOKEN_REVOKE_FAILED);
            } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
                String msg = "Error while connecting with the revoke endpoint for revoking the existing token.";
                log.error(msg, e);
                throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                        ACCESS_TOKEN_REVOKE_FAILED);
            } finally {
                if (urlConn != null) {
                    urlConn.disconnect();
                }
            }

        }
    }

    @Override public KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest)
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

    @Override public void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagementException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagementException {
        return true;
    }

    @Override public Map getResourceByApiId(String apiId) throws KeyManagementException {
        return null;
    }

    @Override public boolean updateRegisteredResource(API api, Map resourceAttributes) throws KeyManagementException {
        return false;
    }

    @Override public void deleteRegisteredResourceByAPIId(String apiID) throws KeyManagementException {

    }

    @Override public void deleteMappedApplication(String consumerKey) throws KeyManagementException {

    }

    private static void createSSLConnection() throws NoSuchAlgorithmException, java.security.KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }};


        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /*
    This is a temp method to test with external IS 5.3.0
     */
    private static String getKeyManagerEndPoint(String context) {
        if (isExternalKeyManager) {
            return EXTERNEL_KEYMANAGER_ENDPOINT + context;
        }
        return INTERNAL_KEYMANAGER_ENDPOINT + context;
    }
}
