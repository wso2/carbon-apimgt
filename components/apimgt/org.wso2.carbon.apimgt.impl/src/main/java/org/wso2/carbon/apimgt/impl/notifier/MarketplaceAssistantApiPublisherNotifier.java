/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.impl.notifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;


/**
 * The default API notification service implementation in which API creation, update, delete and LifeCycle change
 * events are published to gateway.
 */
public class MarketplaceAssistantApiPublisherNotifier extends ApisNotifier{
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(MarketplaceAssistantApiPublisherNotifier.class);
    private static MarketplaceAssistantConfigurationDTO marketplaceAssistantConfigurationDto =
            new MarketplaceAssistantConfigurationDTO();

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            marketplaceAssistantConfigurationDto = configuration.getMarketplaceAssistantConfigurationDto();
            process(event);
        }
        return true;
    }

    /**
     * Add or Delete APIs from external DB when life cycle state changes
     *
     * @param event APIEvent to undeploy APIs from external gateway
     * @throws NotifierException if error occurs
     */
    private void process (Event event) throws NotifierException {
        APIEvent apiEvent;
        apiEvent = (APIEvent) event;

        if (APIConstants.EventType.API_UPDATE.name().equals(event.getType())) {
            String currentStatus = apiEvent.getCurrentStatus().toUpperCase();
            switch (currentStatus) {
                case APIConstants.PROTOTYPED:
                case APIConstants.PUBLISHED:
                    postRequest(apiEvent);
                    break;
                default:
                    break;
            }
        } else {

            if (APIConstants.EventType.API_LIFECYCLE_CHANGE.name().equals(event.getType())) {
                String lifecycleEvent = apiEvent.getLifecycleEvent();
                String currentStatus = apiEvent.getCurrentStatus().toUpperCase();
                switch (lifecycleEvent) {
                    case APIConstants.DEMOTE_TO_CREATED:
                    case APIConstants.BLOCK:
                        deleteRequest(apiEvent);
                        break;
                    case APIConstants.DEPRECATE:
                        if (APIConstants.PUBLISHED.equals(currentStatus)){
                            deleteRequest(apiEvent);
                            break;
                        }
                    case APIConstants.PUBLISH:
                    case APIConstants.DEPLOY_AS_A_PROTOTYPE:
                        if (APIConstants.CREATED.equals(currentStatus)) {
                            postRequest(apiEvent);
                        }
                        break;
                    case APIConstants.REPUBLISH:
                        postRequest(apiEvent);
                        break;
                    default:
                        break;
                }
            } else if (APIConstants.EventType.API_DELETE.name().equals(event.getType())) {
                String currentStatus = apiEvent.getApiStatus().toUpperCase();
                switch (currentStatus) {
                    case APIConstants.PROTOTYPED:
                    case APIConstants.PUBLISHED:
                        deleteRequest(apiEvent);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void postRequest(APIEvent apiEvent) throws NotifierException {
        String apiId = apiEvent.getUuid();

        try {
            apiMgtDAO = ApiMgtDAO.getInstance();
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiId, apiMgtDAO.getOrganizationByAPIUUID(apiId));

            MarketplaceAssistantPostTask task = new MarketplaceAssistantPostTask(api, apiEvent, apiId);
            Thread thread = new Thread(task, "MarketplaceAssistantPostThread");
            thread.start();

        } catch (APIManagementException e) {
            String errorMessage = "Error encountered while Uploading the API with UUID: " +
                    apiId + " to the vector database" + e.getMessage();
            log.error(errorMessage, e);
        }
    }

    private void deleteRequest(APIEvent apiEvent) throws NotifierException {
        String uuid = apiEvent.getUuid();
        MarketplaceAssistantDeletionTask task = new MarketplaceAssistantDeletionTask(uuid);
        Thread thread = new Thread(task, "MarketplaceAssistantDeletionThread");
        thread.start();
    }

    class MarketplaceAssistantPostTask implements Runnable {
        private API api;
        private APIEvent apiEvent;
        private String apiId;
        public MarketplaceAssistantPostTask(API api, APIEvent apiEvent, String apiId) {
            this.api = api;
            this.apiEvent = apiEvent;
            this.apiId = apiId;
        }

        @Override
        public void run() {
            try {
                String api_type = api.getType();
                JSONObject payload = new JSONObject();

                payload.put(APIConstants.API_SPEC_TYPE, api_type);

                switch (api_type) {
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
                payload.put(APIConstants.TENANT_DOMAIN, apiEvent.getTenantDomain());
                payload.put(APIConstants.VERSION, apiEvent.getApiVersion());

                String visibleRoles = apiEvent.getApiVisibleRoles();
                if (visibleRoles == null) {
                    visibleRoles = "";  // Assign an empty string if null
                }
                payload.put(APIConstants.VISIBILITYROLES, visibleRoles.toLowerCase());
                payload.put(APIConstants.APIM_VERSION, APIUtil.getAPIMVersion());

                if (marketplaceAssistantConfigurationDto.isKeyProvided()) {
                    APIUtil.invokeAIService(marketplaceAssistantConfigurationDto.getEndpoint(),
                            marketplaceAssistantConfigurationDto.getTokenEndpoint(),
                            marketplaceAssistantConfigurationDto.getKey(),
                            marketplaceAssistantConfigurationDto.getApiPublishResource(), payload.toString(), null);
                } else if (marketplaceAssistantConfigurationDto.isAuthTokenProvided()) {
                    APIUtil.invokeAIService(marketplaceAssistantConfigurationDto.getEndpoint(), null,
                            marketplaceAssistantConfigurationDto.getAccessToken(),
                            marketplaceAssistantConfigurationDto.getApiPublishResource(), payload.toString(), null);
                }
            } catch (APIManagementException e) {
                String errorMessage = "Error encountered while Uploading the API with UUID: " +
                        apiId + " to the vector database" + e.getMessage();
                log.error(errorMessage, e);
            }
        }
    }

    class MarketplaceAssistantDeletionTask implements Runnable {
        private String uuid;
        public MarketplaceAssistantDeletionTask(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void run() {
            try {
                if (marketplaceAssistantConfigurationDto.isKeyProvided()) {
                    APIUtil.marketplaceAssistantDeleteService(marketplaceAssistantConfigurationDto.getEndpoint(),
                            marketplaceAssistantConfigurationDto.getTokenEndpoint(),
                            marketplaceAssistantConfigurationDto.getKey(),
                            marketplaceAssistantConfigurationDto.getApiDeleteResource(), uuid);
                } else if (marketplaceAssistantConfigurationDto.isAuthTokenProvided()) {
                    APIUtil.marketplaceAssistantDeleteService(marketplaceAssistantConfigurationDto.getEndpoint(),
                            null,
                            marketplaceAssistantConfigurationDto.getAccessToken(),
                            marketplaceAssistantConfigurationDto.getApiDeleteResource(), uuid);
                }
            } catch (APIManagementException e) {
                String errorMessage = "Error encountered while Deleting the API with UUID: " +
                        uuid + " from the vector database" + e.getMessage();
                log.error(errorMessage, e);
            }
        }
    }
}
