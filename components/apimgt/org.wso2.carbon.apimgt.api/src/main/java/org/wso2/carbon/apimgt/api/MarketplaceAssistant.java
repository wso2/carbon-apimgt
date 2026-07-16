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
package org.wso2.carbon.apimgt.api;

/**
 * Extension point for the Marketplace Assistant backend integration.
 * <p>
 * An implementation owns the full interaction with the underlying AI service - building the request payload,
 * invoking the service over whatever transport/auth mechanism it requires, and returning the response body. This
 * decouples the Developer Portal REST layer from any specific AI service contract, allowing deployments to plug in
 * their own AI service by configuring a custom implementation class under
 * {@code <MarketplaceAssistant><MarketPlaceAssistanceImplementation></MarketPlaceAssistanceImplementation></MarketplaceAssistant>}
 * in {@code api-manager.xml}. When no implementation is configured, {@code DefaultMarketplaceAssistantServiceImpl} is
 * used, preserving the out-of-the-box behaviour.
 * <p>
 * Implementations must be thread-safe and provide a public no-argument constructor, as a single instance is created
 * and cached by {@code MarketplaceAssistantServiceFactory}.
 */
public interface MarketplaceAssistant {

    /**
     * Executes a Marketplace Assistant chat query against the underlying AI service.
     *
     * @param request the request context (query, history, organization, user, and any additional properties)
     * @return the raw JSON response body returned by the AI service, expected to map onto
     *         {@code MarketplaceAssistantResponseDTO}; or {@code null} if the service is not applicable/configured,
     *         in which case the REST layer produces an empty response
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    MarketplaceAssistantResponse execute(MarketplaceAssistantRequest request) throws APIManagementException;

    /**
     * Retrieves the number of APIs available to the Marketplace Assistant.
     *
     * @param request the request context (organization and any additional properties)
     * @return the raw JSON response body returned by the AI service, expected to map onto
     *         {@code MarketplaceAssistantApiCountResponseDTO}; or {@code null} if the service is not
     *         applicable/configured, in which case the REST layer produces an empty response
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    MarketplaceAssistantResponse getApiCount(MarketplaceAssistantRequest request) throws APIManagementException;

    /**
     * Publishes an API to the Marketplace Assistant vector store. Invoked asynchronously by the API publisher
     * notifier when an API is published/created.
     *
     * @param request the request context (the {@code api} together with {@code tenantDomain}, {@code version} and
     *                {@code visibleRoles}, plus any additional properties)
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    void publishAPI(MarketplaceAssistantRequest request) throws APIManagementException;

    /**
     * Deletes an API from the Marketplace Assistant vector store. Invoked asynchronously by the API publisher
     * notifier when an API is deleted/demoted.
     *
     * @param request the request context (the {@code uuid} of the API to delete, plus any additional properties)
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    void deleteAPI(MarketplaceAssistantRequest request) throws APIManagementException;
}
