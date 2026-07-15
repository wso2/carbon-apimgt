/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.ai;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.MarketplaceAssistant;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;

/**
 * Default {@link MarketplaceAssistant} implementation that integrates with the WSO2 (Choreo) deployed AI service.
 * This preserves the out-of-the-box Marketplace Assistant behaviour and is used whenever no custom implementation
 * class is configured.
 */
public class DefaultMarketplaceAssistantService implements MarketplaceAssistant {

    private static final Log log = LogFactory.getLog(DefaultMarketplaceAssistantService.class);

    @Override
    public MarketplaceAssistantResponse execute(MarketplaceAssistantRequest request) throws APIManagementException {
        MarketplaceAssistantConfigurationDTO configDto = getConfiguration();
        if (configDto == null || !(configDto.isKeyProvided() || configDto.isAuthTokenProvided())) {
            return null;
        }

        String userRoles = new Gson().toJson(APIUtil.getListOfRoles(request.getUsername()));

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.QUERY, request.getQuery());
        payload.put(APIConstants.HISTORY, request.getHistory());
        payload.put(APIConstants.TENANT_DOMAIN, request.getOrganization());
        payload.put(APIConstants.USERROLES, userRoles.toLowerCase());
        payload.put(APIConstants.APIM_VERSION, APIUtil.getAPIMVersion());

        String aiResponse;
        if (configDto.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), configDto.getTokenEndpoint(),
                    configDto.getKey(), configDto.getChatResource(), payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(configDto.getEndpoint(), null,
                    configDto.getAccessToken(), configDto.getChatResource(), payload.toString(), null);
        }

        MarketplaceAssistantResponse response = new MarketplaceAssistantResponse();
        response.setExecuteResponse(aiResponse);
        return response;
    }

    @Override
    public MarketplaceAssistantResponse getApiCount(MarketplaceAssistantRequest request) throws APIManagementException {
        MarketplaceAssistantConfigurationDTO configDto = getConfiguration();
        if (configDto == null || !(configDto.isKeyProvided() || configDto.isAuthTokenProvided())) {
            return null;
        }

        CloseableHttpResponse httpResponse = null;
        try {
            if (configDto.isKeyProvided()) {
                httpResponse = APIUtil.getMarketplaceChatApiCount(configDto.getEndpoint(),
                        configDto.getTokenEndpoint(), configDto.getKey(), configDto.getApiCountResource());
            } else {
                httpResponse = APIUtil.getMarketplaceChatApiCount(configDto.getEndpoint(),
                        null, configDto.getAccessToken(), configDto.getApiCountResource());
            }
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully completed the Marketplace Assistant api count call with status code: "
                            + statusCode);
                }
                MarketplaceAssistantResponse response = new MarketplaceAssistantResponse();
                response.setCount(EntityUtils.toString(httpResponse.getEntity()));
                return response;
            } else {
                throw new APIManagementException("Error encountered while executing the Marketplace Assistant "
                        + "service to accommodate the specified testing requirement. Received status code: "
                        + statusCode);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error encountered while connecting to the Marketplace Assistant "
                    + "service", e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("Error while closing the CloseableHttpResponse", e);
                }
            }
        }
    }

    private MarketplaceAssistantConfigurationDTO getConfiguration() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
            return null;
        }
        return configuration.getMarketplaceAssistantConfigurationDto();
    }
}
