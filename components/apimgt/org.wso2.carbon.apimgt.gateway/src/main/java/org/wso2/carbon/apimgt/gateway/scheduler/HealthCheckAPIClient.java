/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.scheduler;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HealthCheckAPIClient {
    private static final Log log = LogFactory.getLog(HealthCheckAPIClient.class);
    private static final String CONTENT_TYPE = "application/json";

    /**
     * Sends a notification (register or heartbeat) to /notify-gateway.
     * @param payload JSON payload as per NotifyGatewayPayload
     * @return response body as String
     */
    public String notifyGateway(String payload) {
        String endpoint = getServiceURL() + "/notify-gateway";
        try {
            HttpResponse response = executePost(endpoint, payload);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("/notify-gateway called. Status: " + statusCode + ", Response: " + responseBody);
            return responseBody;
        } catch (IOException e) {
            log.error("Error occurred while calling /notify-gateway", e);
            return null;
        }
    }

    public String extractGatewayIdFromResponse(String responseBody) {
        int idx = responseBody.indexOf("\"gatewayId\"");
        if (idx != -1) {
            int start = responseBody.indexOf(':', idx) + 1;
            int quote1 = responseBody.indexOf('"', start);
            int quote2 = responseBody.indexOf('"', quote1 + 1);
            if (quote1 != -1 && quote2 != -1) {
                return responseBody.substring(quote1 + 1, quote2);
            }
        }
        return null;
    }

    public String extractStatusFromResponse(String responseBody) {
        int idx = responseBody.indexOf("\"status\"");
        if (idx != -1) {
            int start = responseBody.indexOf(':', idx) + 1;
            int quote1 = responseBody.indexOf('"', start);
            int quote2 = responseBody.indexOf('"', quote1 + 1);
            if (quote1 != -1 && quote2 != -1) {
                return responseBody.substring(quote1 + 1, quote2);
            }
        }
        return null;
    }

    private HttpResponse executePost(String endpoint, String payload) throws IOException {
        URL url = new URL(endpoint);
        EventHubConfigurationDto config = getEventHubConfiguration();
        HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

        HttpPost request = new HttpPost(endpoint);
        request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(getServiceCredentials(config), StandardCharsets.UTF_8));
        request.setHeader("Content-Type", CONTENT_TYPE);
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

        return httpClient.execute(request);
    }

    private String getServiceURL() {
        return getEventHubConfiguration().getServiceUrl() + APIConstants.INTERNAL_WEB_APP_EP;
    }

    private EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

    private byte[] getServiceCredentials(EventHubConfigurationDto config) {
        String credentials = config.getUsername() + APIConstants.DELEM_COLON + config.getPassword();
        return Base64.encodeBase64(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
