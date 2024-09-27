/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.utils;

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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccessTokenGenerator {

    private static final Log log = LogFactory.getLog(AccessTokenGenerator.class);

    private String tokenEndpoint;
    private String authKey;
    private Map<String, AccessTokenInfo> accessTokenInfoMap = new ConcurrentHashMap<>();

    public AccessTokenGenerator(String tokenEndpoint, String authKey) {
        this.tokenEndpoint = tokenEndpoint;
        this.authKey = authKey;
    }

    public String getAccessToken() {
        AccessTokenInfo accessTokenInfo = accessTokenInfoMap.get(tokenEndpoint);
        if (accessTokenInfo != null) {
            long expiryTime = accessTokenInfo.getIssuedTime() + accessTokenInfo.getValidityPeriod();
            long buffer = 20000;

            if (buffer > (expiryTime - System.currentTimeMillis())) {
                if (log.isDebugEnabled()) {
                    log.debug("Access token expired. New token requested");
                }
                accessTokenInfoMap.remove(tokenEndpoint);
                accessTokenInfo = generateNewAccessToken();
                accessTokenInfoMap.put(tokenEndpoint, accessTokenInfo);
                assert accessTokenInfo != null;
                return accessTokenInfo.getAccessToken();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Valid Access Token already available");
                }
                return accessTokenInfo.getAccessToken();
            }
        } else {
            accessTokenInfo = generateNewAccessToken();
            if (accessTokenInfo != null) {
                accessTokenInfoMap.put(tokenEndpoint, accessTokenInfo);
                return accessTokenInfo.getAccessToken();
            }
        }
        return null;
    }

    private AccessTokenInfo generateNewAccessToken() {
        try {
            URL oauthURL = new URL(tokenEndpoint);
            HttpPost request = new HttpPost(tokenEndpoint);
            HttpClient httpClient = APIUtil.getHttpClient(oauthURL.getPort(), oauthURL.getProtocol());

            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC + authKey);
            request.setHeader(APIConstants.CONTENT_TYPE_HEADER, APIConstants.CONTENT_TYPE_APPLICATION_FORM);

            List<BasicNameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(APIConstants.TOKEN_GRANT_TYPE_KEY, APIConstants.GRANT_TYPE_VALUE));
            request.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String payload = EntityUtils.toString(httpResponse.getEntity());
                JSONObject response = new JSONObject(payload);
                String accessToken = response.getString(APIConstants.OAUTH_RESPONSE_ACCESSTOKEN);
                int validityPeriod = response.getInt(APIConstants.OAUTH_RESPONSE_EXPIRY_TIME) * 1000;
                long expiryTime = System.currentTimeMillis() + validityPeriod;

                if (log.isDebugEnabled()) {
                    log.debug("Successfully received an access token which expires in " + expiryTime);
                }

                AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
                accessTokenInfo.setAccessToken(accessToken);
                accessTokenInfo.setIssuedTime(System.currentTimeMillis());
                accessTokenInfo.setValidityPeriod(validityPeriod);
                accessTokenInfo.setTokenValid(true);
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
}
