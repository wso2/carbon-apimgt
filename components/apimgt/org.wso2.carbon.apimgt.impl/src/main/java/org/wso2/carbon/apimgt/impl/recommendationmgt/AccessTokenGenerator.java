/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.recommendationmgt;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccessTokenGenerator {

    private static final Log log = LogFactory.getLog(AccessTokenGenerator.class);

    private String oauthUrl;
    private String consumerKey;
    private String consumerSecret;
    private String tokenEndpoint;
    private String revokeEndpoint;
    private Map<String, AccessTokenInfo> accessTokenInfoMap = new ConcurrentHashMap<>();

    public AccessTokenGenerator(String oauthUrl, String consumerKey, String consumerSecret) {

        this.oauthUrl = oauthUrl;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public AccessTokenGenerator(String tokenEndpoint, String revokeEndpoint, String consumerKey,
                                String consumerSecret) {

        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.tokenEndpoint = tokenEndpoint;
        this.revokeEndpoint = revokeEndpoint;
    }

    public String getAccessToken(String[] scopes) {

        String scopeHash = getScopeHash(scopes);
        AccessTokenInfo accessTokenInfo = accessTokenInfoMap.get(scopeHash);
        if (accessTokenInfo != null) {
            long expiryTime = accessTokenInfo.getIssuedTime() + accessTokenInfo.getValidityPeriod();
            // buffer time is set to 20 seconds
            long buffer = 20000;
            if (System.currentTimeMillis() > expiryTime) {
                if (log.isDebugEnabled()) {
                    log.debug("Access token expired. New token requested");
                }
                accessTokenInfoMap.remove(scopeHash);
                accessTokenInfo = generateNewAccessToken(scopes);
                accessTokenInfoMap.put(scopeHash, accessTokenInfo);
            } else if (buffer > (expiryTime - System.currentTimeMillis())) {
                if (log.isDebugEnabled()) {
                    log.debug("Access Token will expire soon. Generated a new Token after revoking the previous");
                }
                revokeAccessToken(accessTokenInfo.getAccessToken());
                accessTokenInfoMap.remove(scopeHash);
                accessTokenInfo = generateNewAccessToken(scopes);
                accessTokenInfoMap.put(scopeHash, accessTokenInfo);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Valid Access Token already available for the provided application");
                }
                return accessTokenInfo.getAccessToken();
            }
        } else {
            accessTokenInfo = generateNewAccessToken(scopes);
        }
        if (accessTokenInfo != null) {
            accessTokenInfoMap.put(scopeHash, accessTokenInfo);
            return accessTokenInfo.getAccessToken();
        }
        return null;
    }

    private void revokeAccessToken(String accessToken) {

        try {
            String revokeEndpoint;
            URL oauthURL;
            int serverPort;

            if (StringUtils.isNotEmpty(this.revokeEndpoint)) {
                revokeEndpoint = this.revokeEndpoint;
                oauthURL = new URL(revokeEndpoint);
                serverPort = oauthURL.getPort();
            } else {
                oauthURL = new URL(oauthUrl);
                revokeEndpoint = oauthUrl.concat("/revoke");
                serverPort = oauthURL.getPort();
            }

            String serverProtocol = oauthURL.getProtocol();

            HttpPost request = new HttpPost(revokeEndpoint);
            HttpClient httpClient = APIUtil.getHttpClient(serverPort, serverProtocol);

            byte[] credentials = org.apache.commons.codec.binary.Base64
                    .encodeBase64((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                    + new String(credentials, StandardCharsets.UTF_8));
            request.setHeader(APIConstants.CONTENT_TYPE_HEADER, APIConstants.CONTENT_TYPE_APPLICATION_FORM);

            List<BasicNameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(APIConstants.TOKEN_KEY, accessToken));
            request.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully revoked the token");
                }
            } else {
                log.error("Error occurred when revoking the Access token. Server responded with "
                        + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("Error occurred when revoking the Access token", e);
        }
    }

    private AccessTokenInfo generateNewAccessToken(String[] scopes) {

        try {
            String tokenEndpoint;
            int serverPort;

            URL oauthURL;
            if (StringUtils.isNotEmpty(this.tokenEndpoint)){
                tokenEndpoint = this.tokenEndpoint;
                oauthURL = new URL(tokenEndpoint);
                serverPort = oauthURL.getPort();
            }else{
                oauthURL = new URL(oauthUrl);
                serverPort = oauthURL.getPort();
                tokenEndpoint = oauthUrl.concat("/token");
            }
            String serverProtocol = oauthURL.getProtocol();

            HttpPost request = new HttpPost(tokenEndpoint);
            HttpClient httpClient = APIUtil.getHttpClient(serverPort, serverProtocol);

            byte[] credentials = org.apache.commons.codec.binary.Base64
                    .encodeBase64((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                    + new String(credentials, StandardCharsets.UTF_8));
            request.setHeader(APIConstants.CONTENT_TYPE_HEADER, APIConstants.CONTENT_TYPE_APPLICATION_FORM);

            List<BasicNameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(APIConstants.TOKEN_GRANT_TYPE_KEY,
                    APIConstants.GRANT_TYPE_VALUE));
            if (scopes != null && scopes.length>0){
                urlParameters
                        .add(new BasicNameValuePair(APIConstants.OAUTH_RESPONSE_TOKEN_SCOPE, String.join(" ", scopes)));
            }
            request.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse httpResponse = httpClient.execute(request);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String payload = EntityUtils.toString(httpResponse.getEntity());
                JSONObject response = new JSONObject(payload);
                String accessToken = (String) response.get(APIConstants.OAUTH_RESPONSE_ACCESSTOKEN);
                int validityPeriod = (Integer) response.get(APIConstants.OAUTH_RESPONSE_EXPIRY_TIME) * 1000;
                long expiryTime = System.currentTimeMillis() + validityPeriod;
                if (log.isDebugEnabled()) {
                    log.debug("Successfully received an access token which expires in " + expiryTime);
                }
                AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
                accessTokenInfo.setAccessToken(accessToken);
                accessTokenInfo.setIssuedTime(System.currentTimeMillis());
                accessTokenInfo.setValidityPeriod(validityPeriod);
                return accessTokenInfo;
            } else {
                log.error("Error occurred when generating a new Access token. Server responded with "
                        + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("Error occurred when generating a new Access token", e);
        }
        return null;
    }

    public void setOauthUrl(String oauthUrl) {

        this.oauthUrl = oauthUrl;
    }

    public void removeInvalidToken(String[] scopes) {

        String scopeHash = getScopeHash(scopes);
        accessTokenInfoMap.remove(scopeHash);
    }
    private String getScopeHash(String[] scopes){
        Arrays.sort(scopes);
        return DigestUtils.md5Hex(String.join(" ", scopes));
    }

    public String getAccessToken() {
        return getAccessToken(new String[]{APIConstants.OAUTH2_DEFAULT_SCOPE});
    }
}