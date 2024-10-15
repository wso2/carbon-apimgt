/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants.UTF8;

public class LLMProviderManager {

    private final EventHubConfigurationDto eventHubConfigurationDto;
    private static final LLMProviderManager llmProviderManager = new LLMProviderManager();

    private static final Log log = LogFactory.getLog(LLMProviderManager.class);

    /**
     * Returns the singleton instance of LlmProviderManager.
     *
     * @return The LlmProviderManager instance.
     */
    public static LLMProviderManager getInstance() {

        return llmProviderManager;
    }

    /**
     * Initializes LlmProviderManager with the EventHub configuration.
     */
    public LLMProviderManager() {

        this.eventHubConfigurationDto = ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

    /**
     * Fetches and initializes LLM Provider configurations from an internal service.
     * Adds the configurations to the data holder.
     *
     * @throws IOException            If there is an issue invoking the service.
     * @throws APIManagementException If there is an issue processing the response.
     */
    public void initializeLLMProviderConfigurations() {

        try {
            String responseString = invokeService(AIAPIConstants.LLM_CONFIGS_ENDPOINT);
            JSONObject responseJson = new JSONObject(responseString);

            JSONArray llmProviderConfigArray = responseJson.getJSONArray("apis");
            for (int i = 0; i < llmProviderConfigArray.length(); i++) {
                JSONObject apiObj = llmProviderConfigArray.getJSONObject(i);
                String id = apiObj.getString(AIAPIConstants.LLM_PROVIDER_ID);
                String name = apiObj.getString(AIAPIConstants.NAME);
                String apiVersion = apiObj.getString(AIAPIConstants.API_VERSION);
                String configurations = apiObj.getString(AIAPIConstants.LLM_PROVIDER_CONFIGURATIONS);

                LLMProvider provider = new LLMProvider();
                provider.setId(id);
                provider.setName(name);
                provider.setApiVersion(apiVersion);
                provider.setConfigurations(configurations);
                DataHolder.getInstance().addLLMProviderConfigurations(provider);
            }
            if (log.isDebugEnabled()) {
                log.debug("Response : " + responseJson);
            }
        } catch (IOException | APIManagementException ex) {
            log.error("Error while calling internal service API", ex);
        }
    }

    public String getLLMProviderConfiguration(String providerId) {
        try {
            String responseString = invokeService(AIAPIConstants.LLM_CONFIGS_ENDPOINT + "/" + providerId);
            JSONObject responseJson = new JSONObject(responseString);
            String configurations = responseJson.getString(AIAPIConstants.LLM_PROVIDER_CONFIGURATIONS);
            if (log.isDebugEnabled()) {
                log.debug("Response : " + responseJson);
            }
            return configurations;
        } catch (IOException | APIManagementException ex) {
            log.error("Error while calling internal service API", ex);
        }
        return null;
    }

    /**
     * Invokes an internal service at the specified path and returns the response as a string.
     *
     * @param path The endpoint path to invoke.
     * @return The response from the service.
     * @throws IOException            If there's an I/O error.
     * @throws APIManagementException If an error occurs during the service call.
     */
    private String invokeService(String path) throws IOException, APIManagementException {

        String serviceURLStr = eventHubConfigurationDto.getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP);
        HttpGet method = new HttpGet(serviceURLStr + path);

        URL serviceURL = new URL(serviceURLStr + path);
        byte[] credentials = getServiceCredentials(eventHubConfigurationDto);
        int servicePort = serviceURL.getPort();
        String serviceProtocol = serviceURL.getProtocol();
        method.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC
                + new String(credentials, StandardCharsets.UTF_8));
        HttpClient httpClient = APIUtil.getHttpClient(servicePort, serviceProtocol);
        try (CloseableHttpResponse httpResponse = APIUtil.executeHTTPRequestWithRetries(method, httpClient)) {
            return EntityUtils.toString(httpResponse.getEntity(), UTF8);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while calling internal service", e);
        }
    }

    /**
     * Generates Base64-encoded service credentials from the provided configuration.
     *
     * @param eventHubConfigurationDto The configuration containing the username and password.
     * @return Base64-encoded credentials as a byte array.
     */
    private byte[] getServiceCredentials(EventHubConfigurationDto eventHubConfigurationDto) {

        String username = eventHubConfigurationDto.getUsername();
        String pw = eventHubConfigurationDto.getPassword();
        return Base64.encodeBase64((username + APIConstants.DELEM_COLON + pw).getBytes
                (StandardCharsets.UTF_8));
    }

}
