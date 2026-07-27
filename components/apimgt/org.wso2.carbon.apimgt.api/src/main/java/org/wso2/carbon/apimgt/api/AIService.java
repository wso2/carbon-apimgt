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
 * Single extension point for all AI-service integrations - the API Design Assistant, the Marketplace Assistant and
 * API Chat.
 * <p>
 * Rather than one contract (and one configured implementation class) per feature, every AI capability is gathered
 * behind this one type. A deployment plugs in its own AI service by configuring a single implementation class under
 * {@code <AIServiceImplementation>} in {@code api-manager.xml}; that one class serves every capability. When no class
 * is configured, {@code DefaultAIServiceImpl} is used, preserving the out-of-the-box behaviour.
 * <p>
 * Implementations are expected to extend {@code AbstractAIService}, which already provides the default behaviour for
 * every method - so a custom implementation overrides only the capabilities it wants to change and inherits the rest.
 * A single instance is created and cached, so implementations must be thread-safe and provide a public no-argument
 * constructor.
 */
public interface AIService {

    /**
     * Injects the resolved AI-service configuration into this implementation. Invoked once by the framework
     * immediately after construction and before the instance is used; the same instance is then cached and shared,
     * so implementations must be thread-safe.
     * <p>
     * The default is a no-op. {@code AbstractAIService} overrides it to store the configuration and expose it to
     * subclasses, so an implementation that extends {@code AbstractAIService} reads its settings from the injected
     * {@link AIServiceConfiguration} rather than from the {@code impl}-module configuration classes.
     *
     * @param configuration the resolved configuration (per-capability endpoints, credentials and resource paths);
     *                       never {@code null}
     * @throws APIManagementException if the implementation cannot be initialized with the given configuration
     */
    default void init(AIServiceConfiguration configuration) throws APIManagementException {
        // No-op by default; implementations that need injected configuration override this.
    }

    // ---- API Design Assistant ----------------------------------------------------------------------------------

    /**
     * Generates an API payload for the given design session.
     *
     * @param request the request context (session identifier and any additional properties)
     * @return the response holding the raw JSON body returned by the AI service; or {@code null} if the service is
     *         not applicable/configured
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    DesignAssistantResponse generatePayload(DesignAssistantRequest request) throws APIManagementException;

    /**
     * Executes a Design Assistant chat query against the underlying AI service.
     *
     * @param request the request context (query text, session identifier, and any additional properties)
     * @return the response holding the raw JSON body returned by the AI service; or {@code null} if the service is
     *         not applicable/configured
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    DesignAssistantResponse chat(DesignAssistantRequest request) throws APIManagementException;

    // ---- Marketplace Assistant ---------------------------------------------------------------------------------

    /**
     * Executes a Marketplace Assistant chat query against the underlying AI service.
     *
     * @param request the request context (query, history, organization, user, and any additional properties)
     * @return the response holding the raw JSON body returned by the AI service; or {@code null} if the service is
     *         not applicable/configured
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    MarketplaceAssistantResponse execute(MarketplaceAssistantRequest request) throws APIManagementException;

    /**
     * Retrieves the number of APIs available to the Marketplace Assistant.
     *
     * @param request the request context (organization and any additional properties)
     * @return the response holding the raw JSON body returned by the AI service; or {@code null} if the service is
     *         not applicable/configured
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

    // ---- API Chat --------------------------------------------------------------------------------------------

    /**
     * Executes the API Chat "prepare" stage against the underlying AI service.
     *
     * @param request the request context (request id, api id, organization, OpenAPI definition, and any additional
     *                properties)
     * @return the response holding the raw JSON body returned by the AI service
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    APIChatResponse prepare(APIChatRequest request) throws APIManagementException;

    /**
     * Executes the API Chat "execute" stage against the underlying AI service.
     *
     * @param request the request context (request id, request payload, and any additional properties)
     * @return the response holding the raw JSON body returned by the AI service
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    APIChatResponse execute(APIChatRequest request) throws APIManagementException;
}
