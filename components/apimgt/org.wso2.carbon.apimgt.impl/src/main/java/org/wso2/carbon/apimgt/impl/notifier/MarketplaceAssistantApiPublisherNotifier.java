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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MarketplaceAssistantRequest;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.ai.MarketplaceAssistantServiceFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.context.CarbonContext;


/**
 * The default API notification service implementation in which API creation, update, delete and LifeCycle change
 * events are published to gateway.
 */
public class MarketplaceAssistantApiPublisherNotifier extends ApisNotifier{
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(MarketplaceAssistantApiPublisherNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        APIManagerConfiguration configuration = ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
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
                String currentStatus = apiEvent.getCurrentStatus();
                if (lifecycleEvent == null || currentStatus == null) {
                    return;
                }
                currentStatus = currentStatus.toUpperCase();
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

            MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
            request.setApi(api);
            request.setUuid(apiId);
            request.setTenantDomain(apiEvent.getTenantDomain());
            request.setVersion(apiEvent.getApiVersion());
            request.setVisibleRoles(apiEvent.getApiVisibleRoles());

            Thread thread = new Thread(new MarketplaceAssistantPostTask(request), "MarketplaceAssistantPostThread");
            thread.start();

        } catch (APIManagementException e) {
            String errorMessage = "Error encountered while Uploading the API with UUID: " +
                    apiId + " to the vector database" + e.getMessage();
            log.error(errorMessage, e);
        }
    }

    private void deleteRequest(APIEvent apiEvent) throws NotifierException {
        MarketplaceAssistantRequest request = new MarketplaceAssistantRequest();
        request.setUuid(apiEvent.getUuid());
        Thread thread = new Thread(new MarketplaceAssistantDeletionTask(request), "MarketplaceAssistantDeletionThread");
        thread.start();
    }

    /**
     * Asynchronously publishes an API to the Marketplace Assistant vector store through the configured
     * {@link org.wso2.carbon.apimgt.api.MarketplaceAssistant} implementation.
     */
    class MarketplaceAssistantPostTask implements Runnable {
        private final MarketplaceAssistantRequest request;

        MarketplaceAssistantPostTask(MarketplaceAssistantRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            try {
                MarketplaceAssistantServiceFactory.getMarketplaceAssistantService().publishAPI(request);
            } catch (APIManagementException e) {
                String errorMessage = "Error encountered while Uploading the API with UUID: " +
                        request.getUuid() + " to the vector database" + e.getMessage();
                log.error(errorMessage, e);
            }
        }
    }

    /**
     * Asynchronously deletes an API from the Marketplace Assistant vector store through the configured
     * {@link org.wso2.carbon.apimgt.api.MarketplaceAssistant} implementation.
     */
    class MarketplaceAssistantDeletionTask implements Runnable {
        private final MarketplaceAssistantRequest request;

        MarketplaceAssistantDeletionTask(MarketplaceAssistantRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            try {
                MarketplaceAssistantServiceFactory.getMarketplaceAssistantService().deleteAPI(request);
            } catch (APIManagementException e) {
                String errorMessage = "Error encountered while Deleting the API with UUID: " +
                        request.getUuid() + " from the vector database" + e.getMessage();
                log.error(errorMessage, e);
            }
        }
    }
}
