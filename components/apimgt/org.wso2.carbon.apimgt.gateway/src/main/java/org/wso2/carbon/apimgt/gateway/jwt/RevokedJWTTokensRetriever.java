/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.jwt;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.gateway.dto.RevokedJWTTokensDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class RevokedJWTTokensRetriever extends TimerTask {

    private static final Log log = LogFactory.getLog(RevokedJWTTokensRetriever.class);
    private static final int revokedJWTTokensRetrievalTimeoutInSeconds = 15;
    private static final int revokedJWTTokensRetrievalRetries = 15;
    private static RevokedJWTDataHolder revokedJWTDataHolder = new RevokedJWTDataHolder();

    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("Starting web service based revoked JWT tokens retrieving process.");
        }
        loadRevokedJWTTokensFromWebService();
    }

    /**
     * This method will retrieve revoked JWT tokens by calling a WebService.
     *
     * @return .
     */
    private RevokedJWTTokensDTO retrieveRevokedJWTTokensData() {

        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfiguration();
            String serviceURL = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
            String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
            String url = serviceURL + "/revokedjwt";
            byte[] credentials = Base64.encodeBase64((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < revokedJWTTokensRetrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving revoked JWT tokens from remote endpoint: " + ex.getMessage()
                                + ". Retrying after " + revokedJWTTokensRetrievalTimeoutInSeconds + " seconds...");
                        Thread.sleep(revokedJWTTokensRetrievalTimeoutInSeconds * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while (retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, RevokedJWTTokensDTO.class);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving revoked JWT tokens from remote endpoint ", e);
        }
        return null;
    }

    private void loadRevokedJWTTokensFromWebService() {
        RevokedJWTTokensDTO revokedJWTTokensDTO = retrieveRevokedJWTTokensData();
        if(revokedJWTTokensDTO != null) {
            revokedJWTDataHolder.addRevokedJWTToMap(null,null);
        }
    }

    public void startRevokedJWTTokensRetriever() {
        //todo init delay configurable?
        new Timer().schedule(this, 60000);
    }

}
