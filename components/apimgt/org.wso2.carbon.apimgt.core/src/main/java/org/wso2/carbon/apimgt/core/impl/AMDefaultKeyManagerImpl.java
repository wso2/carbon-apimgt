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

import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Map;


/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */

public class AMDefaultKeyManagerImpl implements KeyManager {
    static final Logger LOG = LoggerFactory.getLogger(AMDefaultKeyManagerImpl.class);

    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "client_credentials";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";

    /**
     * Create the oauth2 application with calling DCR endpoint of WSO2 IS
     *
     * @param oauthAppRequest - this object contains values of oAuth app properties.
     * @return OAuthApplicationInfo -this object contains oauth2 app details
     * @throws KeyManagementException throws KeyManagerException
     */

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws KeyManagementException {
        // OAuthApplications are created by calling to DCR endpoint of WSO2 IS
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(KeyManagerConstants.APP_KEY_TYPE);
        if (keyType != null) {  //Derive oauth2 app name based on key type and user input for app name
            applicationName = applicationName + "_" + keyType;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to create OAuth application :" + applicationName);
        }
        //Create json payload for DCR endpoint
        JsonObject json = new JsonObject();
        json.addProperty(KeyManagerConstants.OAUTH_REDIRECT_URIS, oAuthApplicationInfo.getCallbackUrl());
        json.addProperty(KeyManagerConstants.OAUTH_CLIENT_NAME, oAuthApplicationInfo.getClientId());
        json.addProperty(KeyManagerConstants.OAUTH_CLIENT_OWNER, oAuthApplicationInfo.getAppOwner());
        json.addProperty(KeyManagerConstants.OAUTH_CLIENT_GRANTS, oAuthApplicationInfo.getGrantTypes());
        URL url;
        HttpURLConnection urlConn = null;
        try {
            // Calling DCR endpoint of IS
            String dcrEndpoint = System.getProperty("dcrEndpoint",
                    "http://localhost:9763/identity/connect/register");
            url = new URL(dcrEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("content-type", "application/json");
            urlConn.getOutputStream()
                    .write((json.toString()).getBytes("UTF-8"));
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {  //If the DCR call is success
                String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
                JsonParser parser = new JsonParser();
                JsonObject jObj =  parser.parse(responseStr).getAsJsonObject();
                String consumerKey = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_ID).getAsString();
                String consumerSecret = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SECRET).getAsString();
                String clientName = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_NAME).getAsString();
                String grantTypes = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_GRANTS).getAsString();

                oAuthApplicationInfo.setClientName(clientName);
                oAuthApplicationInfo.setClientId(consumerKey);
                oAuthApplicationInfo.setClientSecret(consumerSecret);
                oAuthApplicationInfo.setGrantTypes(grantTypes);

            } else { //If DCR call fails
                throw new KeyManagementException("OAuth app does not contains required data  : " + applicationName);
            }


        } catch (IOException e) {
            LOG.error("Can not create OAuth application  : " + applicationName, e);
            throw new KeyManagementException("Can not create OAuth application  : " + applicationName, e,
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } catch (JsonSyntaxException e) {
            LOG.error("Error while processing the response returned from DCR endpoint.Can not create" +
                    " OAuth application : " + applicationName, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new KeyManagementException("Can not create OAuth application  : " + applicationName, e,
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }

        }
        return oAuthApplicationInfo;

    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws KeyManagementException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete oauth2 application with calling DCR endpoint of WSO2 IS
     *
     * @param consumerKey - will take consumer key as parameter
     * @throws KeyManagementException -throws KeyManagementException type
     */

    @Override
    public void deleteApplication(String consumerKey) throws KeyManagementException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to delete OAuth application for consumer key :" + consumerKey);
        }
        if (consumerKey == null || consumerKey.isEmpty()) { //If the consumer key empty or null,returns
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting OAuth App for " + consumerKey);
        }
            /*code to check the client app exists/not
            * */

        // if (spAppName == null) {
        //   LOG.info("Couldn't find OAuth App for Consumer Key : " + consumerKey);
        //  return;
        // }
        URL url;
        HttpURLConnection urlConn = null;

        try {
            // Calling DCR endpoint of IS
            String dcrEndpoint = System.getProperty("dcrEndpoint",
                    "http://localhost:9763/identity/connect/register/") + consumerKey;
            url = new URL(dcrEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("DELETE");

            int responseCode = urlConn.getResponseCode();
            if (responseCode != 200) { //If DCR call fails
                LOG.error("Error while deleting the client-" + consumerKey);
                throw new KeyManagementException("Error while deleting the client.HTTP error code is:" + responseCode,
                        ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
            }


        } catch (IOException e) {
            LOG.error("Error while deleting the client- " + consumerKey, e);
            throw new KeyManagementException("Error while deleting the client- " + consumerKey, e,
                    ExceptionCodes.OAUTH2_APP_DELETION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException {
        //TO-DO- USE CORRECT DCRM ENDPOINT


        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to retrieve OAuth application for consumer key :" + consumerKey);
        }

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
       /* try {
           TO-DO-Add logic to retrieve oauth2 application
            }  */
        oAuthApplicationInfo.setClientName("TEST");
        oAuthApplicationInfo.setClientId("2WSDFHF");
        oAuthApplicationInfo.setCallbackUrl("http://google.com");
        oAuthApplicationInfo.setClientSecret("EWSDFFG");
        oAuthApplicationInfo.setGrantTypes("password");


         /*} TO-DO-Add logic to exception handling in retrieving oauth2 application */
        //}
        return oAuthApplicationInfo;
    }

    /**
     * This method is to generate a new application access token
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from Store UI.
     * @return AccessTokenInfo which wraps application access token data
     * @throws KeyManagementException -throws KeyManagementException type
     */
    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws KeyManagementException {
        String newAccessToken;
        long validityPeriod;
        AccessTokenInfo tokenInfo = null;

        if (tokenRequest == null) {
            LOG.warn("No information available to generate Token.");
            return null;
        }

        //TO-DO -ADD A CONFIG FOR TOKEN ENDPOINT
        String tokenEndpoint = System.getProperty("TokenEndpoint", "https://localhost:9443/oauth2/token");
        //TO-DO -ADD A CONFIG FOR REVOKE ENDPOINT
        String revokeEndpoint = System.getProperty("RevokeEndpoint", "https://localhost:9443/oauth2/revoke");
        ;

        // Call the /revoke only if there's a token to be revoked.

        URL url;
        HttpURLConnection urlConn = null;

        if (tokenRequest.getTokenToRevoke() != null && !"".equals(tokenRequest.getTokenToRevoke())) {
            //Revoke the Old Access Token
            try {
                url = new URL(revokeEndpoint);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoOutput(true);
                urlConn.setRequestMethod("POST");
                String postParams = KeyManagerConstants.OAUTH_CLIENT_ID + "=" + tokenRequest.getClientId() +
                        KeyManagerConstants.OAUTH_CLIENT_SECRET + "=" + tokenRequest.getClientSecret() +
                        KeyManagerConstants.OAUTH_TOKEN + "=" + tokenRequest.getTokenToRevoke();
                urlConn.getOutputStream()
                        .write((postParams).getBytes("UTF-8"));
                int responseCode = urlConn.getResponseCode();
                if (responseCode != 200) { //If token revoke failed
                    throw new RuntimeException("Token revoke failed : HTTP error code : " + responseCode);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Successfully submitted revoke request for old application token. HTTP status : 200");
                    }
                }
            } catch (IOException e) {
                String msg = "Error while revoking the existing token.";
                LOG.error(msg, e);
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
            url = new URL(tokenEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            StringBuilder builder = new StringBuilder();
            builder.append(applicationTokenScope);
            for (String scope : tokenRequest.getScopes()) {
                builder.append(' ').append(scope);
            }
            String postParams = KeyManagerConstants.OAUTH_CLIENT_GRANT + "=" + GRANT_TYPE_VALUE + "&" +
                    GRANT_TYPE_PARAM_VALIDITY + "=" + Long.toString(tokenRequest.getValidityPeriod()) + "&" +
                    KeyManagerConstants.OAUTH_CLIENT_ID + "=" + tokenRequest.getClientId() + "&" +
                    KeyManagerConstants.OAUTH_CLIENT_SECRET + "=" + tokenRequest.getClientSecret() + "&" +
                    KeyManagerConstants.OAUTH_CLIENT_SCOPE + "=" + builder.toString();

            urlConn.getOutputStream()
                    .write((postParams).getBytes("UTF-8"));
            int responseCode = urlConn.getResponseCode();
            if (responseCode != 200) { //If token generation failed
                throw new RuntimeException("Error occurred while calling token endpoint: HTTP error code : " +
                        responseCode);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Successfully submitted token request for old application token. HTTP status : 200");
                }
                tokenInfo = new AccessTokenInfo();
                String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse(responseStr).getAsJsonObject();
                newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
                validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());
                if (obj.has(KeyManagerConstants.OAUTH_CLIENT_SCOPE)) {
                    tokenInfo.setScopes((obj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SCOPE).getAsString()).
                            split(" "));
                }
                tokenInfo.setAccessToken(newAccessToken);
                tokenInfo.setValidityPeriod(validityPeriod);
            }
        } catch (IOException e) {
            String msg = "Error while creating the new token for token regeneration.";
            LOG.error(msg, e);
            throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                    APPLICATION_TOKEN_GENERATION_FAILED);
        } catch (JsonSyntaxException e) {
            String msg = "Error while processing the response returned from token generation call.";
            LOG.error(msg, e);
            throw new KeyManagementException(msg + e.getMessage(), e, ExceptionCodes.
                    APPLICATION_TOKEN_GENERATION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

        return tokenInfo;
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException {
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        URL url;
        HttpURLConnection urlConn = null;
        try {
            String introspectEndpoint = System.getProperty("introspectEndpoint",
                    "http://localhost:9763/oauth2/introspect");
            url = new URL(introspectEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.getOutputStream()
                    .write(("token=" + accessToken).getBytes("UTF-8"));
            String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
            JsonParser parser = new JsonParser();
            JsonObject jObj = parser.parse(responseStr).getAsJsonObject();
            boolean active = jObj.getAsJsonPrimitive("active").getAsBoolean();
            if (active) {
                String consumerKey = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_ID).getAsString();
                String endUser = jObj.getAsJsonPrimitive(KeyManagerConstants.USERNAME).getAsString();
                long exp = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_EXP_TIME).getAsLong();
                long issuedTime = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_ISSUED_TIME).getAsLong();
                String scopes = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SCOPE).getAsString();
                if (scopes != null) {
                    String[] scopesArray = scopes.split("\\s+");
                    tokenInfo.setScopes(scopesArray);
                }
                tokenInfo.setTokenValid(true);
                tokenInfo.setAccessToken(accessToken);
                tokenInfo.setConsumerKey(consumerKey);
                tokenInfo.setEndUserName(endUser);
                tokenInfo.setIssuedTime(issuedTime);

                // Convert Expiry Time to milliseconds.
                if (exp == Long.MAX_VALUE) {
                    tokenInfo.setValidityPeriod(Long.MAX_VALUE);
                } else {
                    tokenInfo.setValidityPeriod(exp * 1000);
                }

            } else {

                tokenInfo.setTokenValid(false);
                LOG.error("Invalid OAuth Token. ");
                tokenInfo.setErrorcode(KeyManagerConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return tokenInfo;


            }

        } catch (IOException e) {
            String msg = "Error while connecting to token introspect endpoint.";
            LOG.error(msg, e);
            throw new KeyManagementException(msg, e, ExceptionCodes.
                    TOKEN_INTROSPECTION_FAILED);
        } catch (JsonSyntaxException e) {
            String msg = "Error while processing the response returned from token introspect endpoint.";
            LOG.error("Error while processing the response returned from token introspect endpoint.", e);
            throw new KeyManagementException(msg, e,
                    ExceptionCodes.TOKEN_INTROSPECTION_FAILED);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

        return tokenInfo;
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

   

    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest) throws KeyManagementException {
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
            /*  TO-DO -ADD LOGIC TO RETIRVE OAUTH2 APP

               */
        } catch (Exception e) {
            String msg = "Some thing went wrong while getting OAuth application for given consumer key " +
                    oAuthApplicationInfo.getClientId();
            LOG.error(msg, e);
            throw new KeyManagementException(msg, e, ExceptionCodes.OAUTH2_APP_MAP_FAILED);
        } finally {

        }
        /* TO-DO
        if (info != null && info.getClientId() == null) {
            return null;
        } */

        oAuthApplicationInfo.addParameter(KeyManagerConstants.OAUTH_CLIENT_TOKEN_SCOPE, tokenScopes);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId());
        }


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

}
