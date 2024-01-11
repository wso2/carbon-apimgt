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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.dto.RevokedJWTTokenDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.DataLoadingException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import static org.wso2.carbon.apimgt.impl.APIConstants.DigestAuthConstants.CHARSET;

/**
 * Class which is responsible to fetch the revoked JWT signatures via webservice database during startup
 */
public class RevokedJWTTokensRetriever extends TimerTask {

    private static final Log log = LogFactory.getLog(RevokedJWTTokensRetriever.class);

    @Override
    public void run() {

        log.debug("Starting web service based revoked JWT tokens retrieving process.");
        loadRevokedJWTTokensFromWebService();
    }

    /**
     * This method will retrieve revoked JWT tokens by calling a web service.
     *
     * @return List of RevokedJWTTokensDTOs.
     */
    private RevokedJWTTokenDTO[] retrieveRevokedJWTTokensData() {

        try {
            // The resource resides in the throttle web app. Hence reading throttle configs
            String url = getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat(
                    "/revokedjwt");
            HttpGet method = new HttpGet(url);
            byte[] credentials = Base64.encodeBase64((getEventHubConfiguration().getUsername() + ":" +
                    getEventHubConfiguration().getPassword()).getBytes(StandardCharsets.UTF_8));
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            String responseString;
            try (CloseableHttpResponse httpResponse = APIUtil.executeHTTPRequestWithRetries(method, httpClient)) {
                responseString = EntityUtils.toString(httpResponse.getEntity(), CHARSET);
            } catch (APIManagementException e) {
                throw new DataLoadingException("Error while retrieving revoked JWT tokens", e);
            }

            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, RevokedJWTTokenDTO[].class);
            }
        } catch (IOException | DataLoadingException e) {
            log.error("Exception when retrieving revoked JWT tokens from remote endpoint ", e);
        }
        return null;
    }

    private void loadRevokedJWTTokensFromWebService() {

        RevokedJWTTokenDTO[] revokedJWTTokenDTOS = retrieveRevokedJWTTokensData();
        if(revokedJWTTokenDTOS != null) {
            for (RevokedJWTTokenDTO revokedJWTToken : revokedJWTTokenDTOS) {
                RevokedJWTDataHolder.getInstance().addRevokedJWTToMap(revokedJWTToken.getSignature(),
                        revokedJWTToken.getExpiryTime());
                if(log.isDebugEnabled()) {
                    log.debug("JWT signature : " + revokedJWTToken.getSignature() + " added to the revoke map.");
                }
            }
        } else {
            log.debug("No revoked JWT tokens are retrieved via web service");
        }
    }

    /**
     *  Initiates the timer task to fetch data from the web service.
     *  Timer task will not run after the retry count is completed.
     */
    public void startRevokedJWTTokensRetriever() {
        //using same initDelay as in keytemplates,blocking conditions retriever
        new Timer().schedule(this, getEventHubConfiguration().getInitDelay());
    }

    protected EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

}
