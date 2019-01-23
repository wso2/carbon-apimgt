/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.status.checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException ;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.MicroGatewayCommonUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.dto.MicroGatewayPingDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Status Checker which pings to update Gateway status
 */
public class StatusChecker implements Runnable {

    private static final Log log = LogFactory.getLog(StatusChecker.class);
    private String token;
    private String pingURL;

    public StatusChecker(String token, String pingURL) {
        this.token = token;
        this.pingURL = pingURL;
    }

    /**
     * Method to override run
     */
    @Override public void run() {
        //Get credentials
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                               .getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        char[] password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();
        //Retrieve tenant domain
        String[] usernameParts = username.split(OnPremiseGatewayConstants.USERNAME_SEPARATOR);
        String tenantDomain = usernameParts[2];
        try {
            String payload = getPingingPayload(tenantDomain, this.token);
            callPingAPIEndpoint(username, password, payload, this.pingURL);
            MicroGatewayCommonUtil.cleanPasswordCharArray(password);
        } catch (IOException e) {
            log.error("Error occurred while calling ping API. ", e);
        }
    }

    /**
     * Ping the API Cloud endpoint to update the status of Micro Gateway
     *
     * @param username
     * @param password
     * @param payload
     * @param pingAPIUrl
     */
    protected void callPingAPIEndpoint(String username, char[] password, String payload, String pingAPIUrl) {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            String authHeaderValue = TokenUtil.getBasicAuthHeaderValue(username, password);
            HttpPost httpPost = createPostRequest(pingAPIUrl, payload, authHeaderValue);
            String response = HttpRequestUtil.executeHTTPMethodWithRetry(client, httpPost,
                                                                         OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
            if (response != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Pinged " + pingAPIUrl + " with the response " + response);
                }
            }
        } catch (OnPremiseGatewayException | UnsupportedEncodingException e) {
            log.error("Error occurred while calling Micro Gateway pinging service", e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    log.warn("Failed to close http client.", e);
                }
            }
        }
    }

    /**
     * Get the details to be sent to status update endpoint as a String
     *
     * @param tenantDomain
     * @param token
     * @return
     * @throws IOException
     */
    protected String getPingingPayload(String tenantDomain, String token) throws
            IOException {
        //Create object
        MicroGatewayPingDTO microGatewayPingDTO = new MicroGatewayPingDTO();
        microGatewayPingDTO.setTenantDomain(tenantDomain);
        microGatewayPingDTO.setToken(token);
        //Convert to JSON string
        ObjectMapper mapper = new ObjectMapper();
        String details = mapper.writeValueAsString(microGatewayPingDTO);
        return details;
    }

    /**
     * Create an Http post request
     *
     * @param pingAPIUrl
     * @param payload
     * @param authHeaderValue
     * @return
     */
    protected HttpPost createPostRequest(String pingAPIUrl, String payload, String authHeaderValue) throws
            UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(pingAPIUrl);
        httpPost.setEntity(new StringEntity(payload));
        httpPost.setHeader(OnPremiseGatewayConstants.AUTHORIZATION_HEADER, authHeaderValue);
        httpPost.setHeader(OnPremiseGatewayConstants.CONTENT_TYPE_HEADER,
                           OnPremiseGatewayConstants.CONTENT_TYPE_APPLICATION_JSON);
        return httpPost;
    }

}
