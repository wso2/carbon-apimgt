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


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.KeyManagerException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;

import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
     * @throws KeyManagerException throws KeyManagerException
     */

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws KeyManagerException {
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
        JSONObject json = new JSONObject();
        json.put(KeyManagerConstants.OAUTH_REDIRECT_URIS, oAuthApplicationInfo.getCallbackUrl());
        json.put(KeyManagerConstants.OAUTH_CLIENT_NAME, oAuthApplicationInfo.getClientId());
        json.put(KeyManagerConstants.OAUTH_CLIENT_OWNER, oAuthApplicationInfo.getAppOwner());
        json.put(KeyManagerConstants.OAUTH_CLIENT_GRANTS, oAuthApplicationInfo.getGrantTypes());
        try {
            // Calling DCR endpoint of IS
            CloseableHttpClient client = APIUtils.getHttpsClient();
            HttpPost httpPost = new HttpPost("http://localhost:9763/identity/connect/register");

            StringEntity params = new StringEntity(json.toString());
            httpPost.addHeader("content-type", "application/json");
            httpPost.setEntity(params);
            CloseableHttpResponse response = client.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity respEntity = response.getEntity();
            if (responseCode == 200) {  //If the DCR call is success
                String responseStr = EntityUtils.toString(respEntity);
                JSONObject jObj = new JSONObject(responseStr);

                String consumerKey = (String) jObj.get(KeyManagerConstants.OAUTH_CLIENT_ID);
                String consumerSecret = (String) jObj.get(KeyManagerConstants.OAUTH_CLIENT_SECRET);
                String clientName = (String) jObj.get(KeyManagerConstants.OAUTH_CLIENT_NAME);
                String grantTypes = (String) jObj.get(KeyManagerConstants.OAUTH_CLIENT_GRANTS);

                oAuthApplicationInfo.setClientName(clientName);
                oAuthApplicationInfo.setClientId(consumerKey);
                oAuthApplicationInfo.setClientSecret(consumerSecret);
                oAuthApplicationInfo.setGrantTypes(grantTypes);


            } else { //If DCR call fails
                throw new KeyManagerException("OAuth app does not contains required data  : " + applicationName);
            }


        } catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {
            throw new KeyManagerException("Can not create OAuth application  : " + applicationName, e);
        } finally {
            APIUtils.releaseInstance(); //Release HttpClient
        }
        return oAuthApplicationInfo;


    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws KeyManagerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete oauth2 application with calling DCR endpoint of WSO2 IS
     *
     * @param consumerKey - will take consumer key as parameter
     * @throws KeyManagerException -throws KeyManagementException type
     */

    @Override
    public void deleteApplication(String consumerKey) throws KeyManagerException {
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
        try {
            //Calling DCR endpoint of WSO2 IS
            CloseableHttpClient client = APIUtils.getHttpsClient();

            HttpDelete httpDel = new HttpDelete("http://localhost:9763/identity/register/" + consumerKey);
            CloseableHttpResponse response = client.execute(httpDel);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) { //If DCR call fails
                LOG.error("Error while deleting the client.");
                throw new KeyManagerException("Error while deleting the client.HTTP error code is:" + responseCode);
            }
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            LOG.error("Error while connecting to token introspect endpoint.", e);
            throw new KeyManagerException(e);
        } finally {
            APIUtils.releaseInstance();

        }


    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagerException {
        //TO-DO- USE CORRECT DCRM ENDPOINT


        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to retrieve OAuth application for consumer key :" + consumerKey);
        }

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
       // try {
            /*keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = keyMgtClient.
            getOAuthApplication(consumerKey);

            if (info == null || info.getClientId() == null) {
                return null;
            }  */
            oAuthApplicationInfo.setClientName("TEST");
            oAuthApplicationInfo.setClientId("2WSDFHF");
            oAuthApplicationInfo.setCallbackUrl("http://google.com");
            oAuthApplicationInfo.setClientSecret("EWSDFFG");
            oAuthApplicationInfo.setGrantTypes("password");


         /*}catch (Exception e) {
            String msg="Can not retrieve OAuth application for the given consumer key : " + consumerKey;
            LOG.error(msg,e);
            throw new KeyManagerException(msg,e); */
        //}
        return oAuthApplicationInfo;
    }

    /**
     * This method is to generate a new application access token
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from Store UI.
     * @return AccessTokenInfo which wraps application access token data
     * @throws KeyManagerException -throws KeyManagementException type
     */
    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws KeyManagerException {
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
        String revokeEndpoint = System.getProperty("RevokeEndpoint", "https://localhost:9443/oauth2/revoke");;

        // Call the /revoke only if there's a token to be revoked.
        try {
            if (tokenRequest.getTokenToRevoke() != null && !"".equals(tokenRequest.getTokenToRevoke())) {

                HttpClient revokeEPClient = APIUtils.getHttpsClient();

                HttpPost httpRevokePost = new HttpPost(revokeEndpoint);

                // Request parameters.
                List<NameValuePair> revokeParams = new ArrayList<>(3);
                revokeParams.add(new BasicNameValuePair(
                        KeyManagerConstants.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
                revokeParams.add(new BasicNameValuePair(
                        KeyManagerConstants.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
                revokeParams.add(new BasicNameValuePair(
                        KeyManagerConstants.OAUTH_TOKEN, tokenRequest.getTokenToRevoke()));


                //Revoke the Old Access Token
                httpRevokePost.setEntity(new UrlEncodedFormEntity(revokeParams, "UTF-8"));
                int statusCode;
                try {
                    HttpResponse revokeResponse = revokeEPClient.execute(httpRevokePost);
                    statusCode = revokeResponse.getStatusLine().getStatusCode();
                } finally {
                    APIUtils.releaseInstance();
                }

                if (statusCode != 200) { //If token revoke failed
                    throw new RuntimeException("Token revoke failed : HTTP error code : " + statusCode);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Successfully submitted revoke request for old application token. HTTP status : 200");
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
            HttpClient tokenEPClient = APIUtils.getHttpsClient();
            HttpPost httpTokpost = new HttpPost(tokenEndpoint);
            List<NameValuePair> tokParams = new ArrayList<>(3);
            tokParams.add(new BasicNameValuePair(KeyManagerConstants.OAUTH_CLIENT_GRANT, GRANT_TYPE_VALUE));
            tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
                    Long.toString(tokenRequest.getValidityPeriod())));
            tokParams.add(new BasicNameValuePair(KeyManagerConstants.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
            tokParams.add(new BasicNameValuePair(
                    KeyManagerConstants.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
            StringBuilder builder = new StringBuilder();
            builder.append(applicationTokenScope);

            for (String scope : tokenRequest.getScopes()) {
                builder.append(' ').append(scope);
            }

            tokParams.add(new BasicNameValuePair(KeyManagerConstants.OAUTH_CLIENT_SCOPE, builder.toString()));

            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            try {
                HttpResponse tokResponse = tokenEPClient.execute(httpTokpost);
                HttpEntity tokEntity = tokResponse.getEntity();

                if (tokResponse.getStatusLine().getStatusCode() != 200) { //If token generation failed
                    throw new RuntimeException("Error occurred while calling token endpoint: HTTP error code : " +
                            tokResponse.getStatusLine().getStatusCode());
                } else {
                    tokenInfo = new AccessTokenInfo();
                    String responseStr = EntityUtils.toString(tokEntity);
                    JSONObject obj = new JSONObject(responseStr);
                    newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
                    validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());
                    if (obj.has("scope")) {
                        tokenInfo.setScopes(((String) obj.get("scope")).split(" "));
                    }
                    tokenInfo.setAccessToken(newAccessToken);
                    tokenInfo.setValidityPeriod(validityPeriod);
                }
            } finally {
                APIUtils.releaseInstance();
            }
        } catch (ClientProtocolException e) {
            String msg = "Error while creating token - Invalid protocol used";
            LOG.error(msg, e);
            throw new KeyManagerException(msg, e);
        } catch (UnsupportedEncodingException e) {
            String msg = "Error while preparing request for token/revoke APIs";
            LOG.error(msg, e);
            throw new KeyManagerException(msg, e);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            String msg = "Error while creating tokens - ";
            LOG.error(msg, e);
            throw new KeyManagerException(msg + e.getMessage(), e);
        } catch (JSONException e) {
            String msg = "Error while parsing response from token api";
            LOG.error(msg, e);
            throw new KeyManagerException(msg, e);
        }

        return tokenInfo;
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagerException {
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        try {
            CloseableHttpClient client = APIUtils.getHttpsClient();
            String introspectEndpoint=System.getProperty("introspectEndpoint", "http://localhost:9763/oauth2/introspect");
            HttpPost httpPost = new HttpPost(introspectEndpoint);

            ArrayList<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair(KeyManagerConstants.OAUTH_TOKEN, accessToken));

            httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

            CloseableHttpResponse response = client.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity respEntity = response.getEntity();
            if (responseCode == 200) {
                String responseStr = EntityUtils.toString(respEntity);
                JSONObject jObj = new JSONObject(responseStr);
                Object active = jObj.get("active");
                if ((Boolean) (active)) {
                    String consumerKey = (String) jObj.get(KeyManagerConstants.OAUTH_CLIENT_ID);
                    String endUser = (String) jObj.get(KeyManagerConstants.USERNAME);
                    long exp = (Long) jObj.get(KeyManagerConstants.OAUTH2_TOKEN_EXP_TIME);
                    long issuedTime = (Long) jObj.get(KeyManagerConstants.OAUTH2_TOKEN_ISSUED_TIME);
                    String scopes = (String) jObj.get(KeyManagerConstants.OAUTH_CLIENT_SCOPE);
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
            }
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            LOG.error("Error while connecting to token introspect endpoint.", e);
            throw new KeyManagerException(e);
        } finally {
            APIUtils.releaseInstance();
        }

        return tokenInfo;
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OAuthApplicationInfo buildFromJSON(String jsonInput) throws KeyManagerException {
        return null;
    }

    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest) throws KeyManagerException {
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
                System.getProperty("validityPeriod", "3600"));


        //check whether given consumer key and secret match or not. If it does not match throw an exception.
        try {
            /*  TO-DO -ADD LOGIC TO RETIRVE OAUTH2 APP
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            info = keyMgtClient.getOAuthApplication(oAuthApplicationInfo.getClientId());
            if (!clientSecret.equals(info.getClientSecret())) {
                throw new APIManagementException("The secret key is wrong for the given consumer key " + consumerKey);
            }
               */
        } catch (Exception e) {
            String msg = "Some thing went wrong while getting OAuth application for given consumer key " +
                    oAuthApplicationInfo.getClientId();
            LOG.error(msg, e);
            throw new KeyManagerException(msg, e);
        } finally {

        }
        /* TO-DO
        if (info != null && info.getClientId() == null) {
            return null;
        } */

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId());
        }


        return oAuthApplicationInfo;
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagerException {
        return true;
    }

    @Override
    public Map getResourceByApiId(String apiId) throws KeyManagerException {
        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws KeyManagerException {
        return false;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws KeyManagerException {

    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws KeyManagerException {

    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String consumerKey) throws KeyManagerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws KeyManagerException {
        return null;
    }

}
