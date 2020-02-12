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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AccessTokenGeneratorImpl  {

    private static final Log log = LogFactory.getLog(AccessTokenGeneratorImpl.class);

    private static volatile AccessTokenGeneratorImpl accessTokenGenerator = null;
    long generatedTime = 0;
    long validityPeriod = 3600000;
    String accessToken = null;
    String oauthUrl;
    String consumerKey;
    String consumerSecret;

    public AccessTokenGeneratorImpl(String oauthUrl, String consumerKey, String consumerSecret) {
        this.oauthUrl = oauthUrl;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public String getAccessToken() {
        if (System.currentTimeMillis() > (this.generatedTime + this.validityPeriod)) {
            if (log.isDebugEnabled()) {
                log.debug("Access token expired. New token requested");
            }
            return generateNewAccessToken();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Valid Access Token already available for the provided application");
            }
            return this.accessToken;
        }
    }

    public String generateNewAccessToken() {
        try {
            URL oauthURL = new URL(oauthUrl);
            int serverPort = oauthURL.getPort();
            String serverProtocol = oauthURL.getProtocol();

            HttpPost request = new HttpPost(oauthUrl);
            HttpClient httpClient = APIUtil.getHttpClient(serverPort, serverProtocol);

            byte[] credentials = org.apache.commons.codec.binary.Base64
                    .encodeBase64((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                    + new String(credentials, StandardCharsets.UTF_8));
            request.setHeader(APIConstants.CONTENT_TYPE_HEADER, APIConstants.CONTENT_TYPE_APPLICATION_FORM);

            List<BasicNameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(APIConstants.TOKEN_GRANT_TYPE_KEY,
                    APIConstants.GRANT_TYPE_VALUE));
            request.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse httpResponse = httpClient.execute(request);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String payload = EntityUtils.toString(httpResponse.getEntity());
                JSONObject response = new JSONObject(payload);
                this.accessToken = (String) response.get(APIConstants.OAUTH_RESPONSE_ACCESSTOKEN);
                this.validityPeriod = (Integer) response.get(APIConstants.OAUTH_RESPONSE_EXPIRY_TIME) * 1000;
                this.generatedTime = System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debug("Successfully received an access token which expires in " + validityPeriod);
                }
                return (String) response.get(APIConstants.OAUTH_RESPONSE_ACCESSTOKEN);
            } else {
                this.accessToken = null;
                log.error("Error occurred when generating a new Access token. Server responded with "
                        + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            this.accessToken = null;
            log.error("Error occurred when generating a new Access token", e);
        }
        return null;
    }
}