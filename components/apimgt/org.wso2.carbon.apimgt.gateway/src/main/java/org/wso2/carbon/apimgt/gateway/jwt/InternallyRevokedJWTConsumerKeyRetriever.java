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

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.util.*;
import org.wso2.carbon.apimgt.gateway.dto.*;
import org.wso2.carbon.apimgt.gateway.internal.*;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.dto.*;
import org.wso2.carbon.apimgt.impl.utils.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

/**
 * This class will fetch consumer keys of revoked JWTs via webservice database during startup
 */
public class InternallyRevokedJWTConsumerKeyRetriever extends TimerTask {

    private static final Log log = LogFactory.getLog(InternallyRevokedJWTConsumerKeyRetriever.class);
    private static final int revokedJWTConsumerKeyRetrievalTimeoutInSeconds = 15;
    private static final int revokedJWTConsumerKeyRetrievalRetries = 15;

    @Override
    public void run() {

        log.debug("Starting web service based revoked JWT consumer key retrieving process.");
        loadRevokedJWTConsumerKeysFromWebService();
    }

    /**
     * This method will retrieve revoked JWT consumer keys by calling a web service.
     *
     * @return List of RevokedJWTConsumerKeyDTO.
     */
    private RevokedJWTConsumerKeyDTO[] retrieveRevokedJWTConsumerKeysData() {

        try {
            // The resource resides in the throttle web app. Hence, reading throttle configs
            String url = getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat(
                    "/revokedconsumerkeys");
            HttpGet method = new HttpGet(url);
            byte[] credentials = Base64.encodeBase64((getEventHubConfiguration().getUsername() + ":" +
                    getEventHubConfiguration().getPassword()).getBytes(StandardCharsets.UTF_8));
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
                    if (retryCount < revokedJWTConsumerKeyRetrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving revoked JWT consumer keys from remote endpoint: " +
                                ex.getMessage() + ". Retrying after " + revokedJWTConsumerKeyRetrievalTimeoutInSeconds +
                                " seconds...");
                        Thread.sleep(revokedJWTConsumerKeyRetrievalTimeoutInSeconds * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while (retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, RevokedJWTConsumerKeyDTO[].class);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving revoked JWT consumer keys from remote endpoint ", e);
        }
        return null;
    }

    private void loadRevokedJWTConsumerKeysFromWebService() {

        RevokedJWTConsumerKeyDTO[] revokedJWTConsumerKeyDTOS = retrieveRevokedJWTConsumerKeysData();
        if (revokedJWTConsumerKeyDTOS != null) {
            for (RevokedJWTConsumerKeyDTO revokedJWTConsumerKey : revokedJWTConsumerKeyDTOS) {
                if (revokedJWTConsumerKey.isRevokedAppOnly()) {
                    // handle user event revocations of app tokens since the 'sub' claim is client id
                    InternalRevokedJWTDataHolder.getInstance().
                            addInternalRevokedJWTClientIDToAppOnlyMap(revokedJWTConsumerKey.getConsumerKey(),
                                    revokedJWTConsumerKey.getRevocationTime());
                } else {
                    InternalRevokedJWTDataHolder.getInstance().
                            addInternalRevokedJWTClientIDToMap(revokedJWTConsumerKey.getConsumerKey(),
                                    revokedJWTConsumerKey.getRevocationTime());
                }
                if (log.isDebugEnabled()) {
                    log.debug("JWT signature : " + revokedJWTConsumerKey.getConsumerKey()
                            + " added to the revoke map.");
                }
            }
        } else {
            log.debug("No revoked JWT consumer keys are retrieved via web service");
        }
    }

    /**
     *  Initiates the timer task to fetch data from the web service.
     *  Timer task will not run after the retry count is completed.
     */
    public void startRevokedJWTConsumerKeyssRetriever() {
        //using same initDelay as in keytemplates,blocking conditions retriever
        new Timer().schedule(this, getEventHubConfiguration().getInitDelay());
    }

    protected EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

}
