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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.AIService;
import org.wso2.carbon.apimgt.api.AIServiceConfiguration;
import org.wso2.carbon.apimgt.api.AIServiceEndpointConfiguration;
import org.wso2.carbon.apimgt.api.APIChatRequest;
import org.wso2.carbon.apimgt.api.APIChatResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.DesignAssistantRequest;
import org.wso2.carbon.apimgt.api.DesignAssistantResponse;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;

/**
 * Base {@link AIService} that provides the default, out-of-the-box behaviour for every AI capability by integrating
 * with the WSO2 (Choreo) deployed AI service.
 * <p>
 * A custom implementation extends this class and overrides only the methods it wants to change; any method it does
 * not override keeps this default behaviour. The resolved configuration is pushed in once through
 * {@link #init(AIServiceConfiguration)} by the framework and stored; the default behaviour and any override read
 * their per-capability settings (endpoint, credentials, resource paths) from that injected
 * {@link AIServiceConfiguration} via {@link #getConfiguration()} - so a subclass never has to reach into
 * {@code APIManagerConfiguration}.
 * <p>
 * The shipped {@link DefaultAIServiceImpl} is an empty subclass used whenever no custom implementation is configured.
 */
public abstract class AbstractAIService implements AIService {

    private static final Log log = LogFactory.getLog(AbstractAIService.class);
    private static final String TEXT = "text";
    private static final String SESSIONID = "sessionId";

    private AIServiceConfiguration configuration;

    @Override
    public void init(AIServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the configuration injected through {@link #init(AIServiceConfiguration)}. Subclasses read their
     * endpoint/credentials/resource paths from here.
     *
     * @return the injected {@link AIServiceConfiguration}, or {@code null} if the instance has not been initialized
     */
    protected AIServiceConfiguration getConfiguration() {
        return configuration;
    }

    // ---- API Design Assistant ----------------------------------------------------------------------------------

    @Override
    public DesignAssistantResponse generatePayload(DesignAssistantRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getDesignAssistantConfiguration();

        JSONObject payload = new JSONObject();
        payload.put(SESSIONID, request.getSessionId());

        String resource = config.getResource(AIServiceEndpointConfiguration.GEN_API_PAYLOAD_RESOURCE);
        String aiResponse;
        if (config.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), resource, payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                    resource, payload.toString(), null);
        }

        DesignAssistantResponse response = new DesignAssistantResponse();
        response.setPayload(aiResponse);
        return response;
    }

    @Override
    public DesignAssistantResponse chat(DesignAssistantRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getDesignAssistantConfiguration();

        JSONObject payload = new JSONObject();
        payload.put(TEXT, request.getText());
        payload.put(SESSIONID, request.getSessionId());

        String resource = config.getResource(AIServiceEndpointConfiguration.CHAT_RESOURCE);
        String aiResponse;
        if (config.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), resource, payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                    resource, payload.toString(), null);
        }

        DesignAssistantResponse response = new DesignAssistantResponse();
        response.setChatResponse(aiResponse);
        return response;
    }

    // ---- Marketplace Assistant ---------------------------------------------------------------------------------

    @Override
    public MarketplaceAssistantResponse execute(MarketplaceAssistantRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getMarketplaceAssistantConfiguration();

        String userRoles = new Gson().toJson(APIUtil.getListOfRoles(request.getUsername()));

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.QUERY, request.getQuery());
        payload.put(APIConstants.HISTORY, request.getHistory());
        payload.put(APIConstants.TENANT_DOMAIN, request.getOrganization());
        payload.put(APIConstants.USERROLES, userRoles.toLowerCase());
        payload.put(APIConstants.APIM_VERSION, APIUtil.getAPIMVersion());

        String resource = config.getResource(AIServiceEndpointConfiguration.CHAT_RESOURCE);
        String aiResponse;
        if (config.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), resource, payload.toString(), null);
        } else {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null,
                    config.getAccessToken(), resource, payload.toString(), null);
        }

        MarketplaceAssistantResponse response = new MarketplaceAssistantResponse();
        response.setExecuteResponse(aiResponse);
        return response;
    }

    @Override
    public MarketplaceAssistantResponse getApiCount(MarketplaceAssistantRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getMarketplaceAssistantConfiguration();
        String resource = config.getResource(AIServiceEndpointConfiguration.API_COUNT_RESOURCE);

        CloseableHttpResponse httpResponse = null;
        try {
            if (config.isKeyProvided()) {
                httpResponse = APIUtil.getMarketplaceChatApiCount(config.getEndpoint(),
                        config.getTokenEndpoint(), config.getKey(), resource);
            } else {
                httpResponse = APIUtil.getMarketplaceChatApiCount(config.getEndpoint(),
                        null, config.getAccessToken(), resource);
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

    @Override
    public void publishAPI(MarketplaceAssistantRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getMarketplaceAssistantConfiguration();
        API api = request.getApi();
        if (api == null) {
            return;
        }
        String apiType = api.getType();
        if (APIConstants.API_TYPE_MCP.equals(apiType)) {
            return;
        }

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.API_SPEC_TYPE, apiType);
        switch (apiType) {
            case APIConstants.API_TYPE_GRAPHQL:
                payload.put(APIConstants.API_SPEC_TYPE_GRAPHQL, api.getGraphQLSchema());
                break;
            case APIConstants.API_TYPE_ASYNC:
            case APIConstants.API_TYPE_WS:
            case APIConstants.API_TYPE_WEBSUB:
            case APIConstants.API_TYPE_SSE:
            case APIConstants.API_TYPE_WEBHOOK:
                payload.put(APIConstants.API_SPEC_TYPE_ASYNC, api.getAsyncApiDefinition());
                break;
            case APIConstants.API_TYPE_HTTP:
            case APIConstants.API_TYPE_PRODUCT:
            case APIConstants.API_TYPE_SOAP:
            case APIConstants.API_TYPE_SOAPTOREST:
                payload.put(APIConstants.API_SPEC_TYPE_REST, api.getSwaggerDefinition());
                break;
            default:
                break;
        }

        payload.put(APIConstants.UUID, api.getUuid());
        payload.put(APIConstants.DESCRIPTION, api.getDescription());
        payload.put(APIConstants.API_SPEC_NAME, api.getId().getApiName());
        payload.put(APIConstants.TENANT_DOMAIN, request.getTenantDomain());
        payload.put(APIConstants.VERSION, request.getVersion());
        String visibleRoles = request.getVisibleRoles();
        if (visibleRoles == null) {
            visibleRoles = "";
        }
        payload.put(APIConstants.VISIBILITYROLES, visibleRoles.toLowerCase());
        payload.put(APIConstants.APIM_VERSION, APIUtil.getAPIMVersion());

        String resource = config.getResource(AIServiceEndpointConfiguration.API_PUBLISH_RESOURCE);
        if (config.isKeyProvided()) {
            APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(), config.getKey(),
                    resource, payload.toString(), null);
        } else {
            APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                    resource, payload.toString(), null);
        }
    }

    @Override
    public void deleteAPI(MarketplaceAssistantRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getMarketplaceAssistantConfiguration();
        String resource = config.getResource(AIServiceEndpointConfiguration.API_DELETE_RESOURCE);
        if (config.isKeyProvided()) {
            APIUtil.marketplaceAssistantDeleteService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), resource, request.getUuid());
        } else {
            APIUtil.marketplaceAssistantDeleteService(config.getEndpoint(), null,
                    config.getAccessToken(), resource, request.getUuid());
        }
    }

    // ---- API Chat --------------------------------------------------------------------------------------------

    @Override
    public APIChatResponse prepare(APIChatRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getApiChatConfiguration();
        try {
            // Generate the payload for the prepare call
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode openAPIDefinitionJsonNode = objectMapper.readTree(request.getOpenAPIDefinition());
            ObjectNode payload = objectMapper.createObjectNode();
            payload.set(APIConstants.OPEN_API, openAPIDefinitionJsonNode);

            String resource = config.getResource(AIServiceEndpointConfiguration.PREPARE_RESOURCE);
            String aiResponse;
            if (config.isKeyProvided()) {
                aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                        config.getKey(), resource, payload.toString(), request.getApiChatRequestId());
            } else {
                aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                        resource, payload.toString(), request.getApiChatRequestId());
            }

            APIChatResponse response = new APIChatResponse();
            response.setPrepareResponse(aiResponse);
            return response;
        } catch (JsonProcessingException e) {
            String error = "Error while parsing OpenAPI definition to JSON for API ID: " + request.getApiId();
            log.error(error, e);
            throw new APIManagementException(error, e);
        }
    }

    @Override
    public APIChatResponse execute(APIChatRequest request) throws APIManagementException {
        AIServiceEndpointConfiguration config = getApiChatConfiguration();
        String resource = config.getResource(AIServiceEndpointConfiguration.EXECUTE_RESOURCE);
        String aiResponse;
        if (config.isKeyProvided()) {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), config.getTokenEndpoint(),
                    config.getKey(), resource, request.getRequestPayload(), request.getApiChatRequestId());
        } else {
            aiResponse = APIUtil.invokeAIService(config.getEndpoint(), null, config.getAccessToken(),
                    resource, request.getRequestPayload(), request.getApiChatRequestId());
        }

        APIChatResponse response = new APIChatResponse();
        response.setExecuteResponse(aiResponse);
        return response;
    }

    // ---- Configuration helpers (exposed to subclasses) --------------------------------------------------------

    /**
     * Returns the validated API Design Assistant endpoint configuration from the injected configuration.
     *
     * @return the {@link AIServiceEndpointConfiguration} for the Design Assistant
     * @throws APIManagementException if the instance was not initialized or no credentials are configured
     */
    protected AIServiceEndpointConfiguration getDesignAssistantConfiguration() throws APIManagementException {
        return validateCredentials(requireConfiguration().getDesignAssistant(), "API Design Assistant");
    }

    /**
     * Returns the validated Marketplace Assistant endpoint configuration from the injected configuration.
     *
     * @return the {@link AIServiceEndpointConfiguration} for the Marketplace Assistant
     * @throws APIManagementException if the instance was not initialized or no credentials are configured
     */
    protected AIServiceEndpointConfiguration getMarketplaceAssistantConfiguration() throws APIManagementException {
        return validateCredentials(requireConfiguration().getMarketplaceAssistant(), "Marketplace Assistant");
    }

    /**
     * Returns the validated API Chat endpoint configuration from the injected configuration.
     *
     * @return the {@link AIServiceEndpointConfiguration} for API Chat
     * @throws APIManagementException if the instance was not initialized or no credentials are configured
     */
    protected AIServiceEndpointConfiguration getApiChatConfiguration() throws APIManagementException {
        return validateCredentials(requireConfiguration().getApiChat(), "API Chat");
    }

    private AIServiceConfiguration requireConfiguration() throws APIManagementException {
        if (configuration == null) {
            throw new APIManagementException("AI service configuration is not initialized.");
        }
        return configuration;
    }

    private AIServiceEndpointConfiguration validateCredentials(AIServiceEndpointConfiguration config,
            String serviceName) throws APIManagementException {
        if (config == null || !(config.isKeyProvided() || config.isAuthTokenProvided())) {
            String errorMessage = serviceName + " service is not configured properly. Please provide the API key or "
                    + "the access token in the configuration.";
            log.error(errorMessage);
            throw new APIManagementException(errorMessage);
        }
        return config;
    }
}
