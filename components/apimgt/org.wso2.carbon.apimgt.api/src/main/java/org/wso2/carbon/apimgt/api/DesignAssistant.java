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
 * Extension point for the API Design Assistant backend integration.
 * <p>
 * An implementation owns the full interaction with the underlying AI service - building the request payload,
 * invoking the service over whatever transport/auth mechanism it requires, and returning the response body. This
 * decouples the Publisher REST layer from any specific AI service contract, allowing deployments to plug in their
 * own AI service by configuring a custom implementation class under
 * {@code <DesignAssistant><DesignAssistantImplementation></DesignAssistantImplementation></DesignAssistant>} in
 * {@code api-manager.xml}. When no implementation is configured, {@code DefaultDesignAssistantServiceImpl} is used,
 * preserving the out-of-the-box behaviour.
 * <p>
 * Implementations must be thread-safe and provide a public no-argument constructor, as a single instance is created
 * and cached by {@code DesignAssistantServiceFactory}.
 */
public interface DesignAssistant {
    /**
     * Generates an API payload for the given design session.
     *
     * @param request the request context (session identifier and any additional properties)
     * @return the raw JSON response body returned by the AI service; or {@code null} if the service is not
     *         applicable/configured, in which case the REST layer produces an empty response
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    DesignAssistantResponse generatePayload(DesignAssistantRequest request) throws APIManagementException;

    /**
     * Executes a Design Assistant chat query against the underlying AI service.
     *
     * @param request the request context (query text, session identifier, and any additional properties)
     * @return the raw JSON response body returned by the AI service, expected to map onto
     *         {@code DesignAssistantChatResponseDTO}; or {@code null} if the service is not applicable/configured,
     *         in which case the REST layer produces an empty response
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    DesignAssistantResponse chat(DesignAssistantRequest request) throws APIManagementException;
}
