/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.apikey;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.gateway.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.DataLoadingException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class APIKeysRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(APIKeysRetriever.class);
    private static String tenantDomain;

    @Override
    public void run() {
        log.debug("Starting web service based API key data retrieving process.");
        retrieveAPIKeysDataFromWebService(tenantDomain);
    }

    /**
     * This method will retrieve API keys by calling a WebService.
     *
     */
    public void retrieveAPIKeysDataFromWebService(String tenantDomain) {

        try {
            String url = getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat(
                    "/api-keys");
            byte[] credentials = Base64.encodeBase64((getEventHubConfiguration().getUsername() + ":" +
                    getEventHubConfiguration().getPassword()).getBytes
                    (StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            if (tenantDomain != null) {
                method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
            }
            URL eventHubUrl = new URL(url);
            int keyMgtPort = eventHubUrl.getPort();
            String protocol = eventHubUrl.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, protocol);
            try (CloseableHttpResponse httpResponse = APIUtil.executeHTTPRequestWithRetries(method, httpClient)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String responseString = EntityUtils.toString(
                            httpResponse.getEntity(), StandardCharsets.UTF_8);
                    ObjectMapper mapper = new ObjectMapper();
                    List<APIKeyDTO> dtoList =
                            mapper.readValue(responseString,
                                    new TypeReference<List<APIKeyDTO>>() {});
                    for (APIKeyDTO dto : dtoList) {
                        // Convert DTO -> APIKeyInfo
                        APIKeyInfo info = new APIKeyInfo();
                        info.setLookupKey(dto.getApiKeyHash());
                        info.setApiKeyHash(dto.getApiKeyHash());
                        info.setKeyName(dto.getKeyName());
                        info.setKeyType(dto.getKeyType());
                        info.setStatus(dto.getStatus());
                        if (dto.getExpiresAt() != null) {
                            info.setExpiresAt(dto.getExpiresAt());
                        }
                        if (dto.getAppId() != null) {
                            info.setAppId(dto.getAppId());
                        }
                        if (dto.getApiId() != null) {
                            info.setApiId(dto.getApiId());
                        }
                        info.setApiUUId(dto.getApiUUID());
                        info.setApplicationId(dto.getApplicationUUID());
                        if (dto.getCreatedTime() != null) {
                            info.setCreatedTime(dto.getCreatedTime());
                        }
                        if (dto.getValidityPeriod() != null) {
                            info.setValidityPeriod(dto.getValidityPeriod());
                        }
                        info.setAuthUser(dto.getAuthUser());
                        info.setKeyBoundary(dto.getKeyBoundary());
                        Map<String, String> properties = dto.getAdditionalProperties();
                        info.setAdditionalProperties(properties);
                        DataHolder.getInstance().addOpaqueAPIKeyInfo(info);
                    }
                } else {
                    log.error("Failed to retrieve API keys from the internal web service.");
                }
            } catch (APIManagementException e) {
                throw new DataLoadingException("Error while retrieving API keys", e);
            }
        } catch (IOException | DataLoadingException e) {
            log.error("Exception when retrieving API keys from remote endpoint ", e);
        }
    }

    protected EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

    public void startWebServiceApiKeyRetriever(String tenantDomain) {
        APIKeysRetriever.tenantDomain = tenantDomain;
        new Timer().schedule(this, getEventHubConfiguration().getInitDelay());
    }
}
