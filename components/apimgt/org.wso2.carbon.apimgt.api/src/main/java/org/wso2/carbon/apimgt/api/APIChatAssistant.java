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
 * Extension point for the API Chat backend integration.
 * <p>
 * An implementation owns the interaction with the underlying AI service for both stages of the API Chat flow -
 * building the request payload, invoking the service over whatever transport/auth mechanism it requires, and
 * returning the response body. This decouples the API Chat logic from any specific AI service contract, allowing
 * deployments to plug in their own AI service by configuring a custom implementation class under
 * {@code <APIChat><ApiChatImplementation></ApiChatImplementation></APIChat>} in {@code api-manager.xml}. When no
 * implementation is configured, {@code DefaultAPIChatAssistantServiceImpl} is used, preserving the out-of-the-box behaviour.
 * <p>
 * Implementations must be thread-safe and provide a public no-argument constructor, as a single instance is created
 * and cached by {@code APIChatServiceFactory}.
 */
public interface APIChatAssistant {

    /**
     * Executes the API Chat "prepare" stage against the underlying AI service.
     *
     * @param request the request context (request id, api id, organization, OpenAPI definition, and any additional
     *                properties)
     * @return the raw JSON response body returned by the AI service, expected to map onto {@code ApiChatResponseDTO}
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    APIChatResponse prepare(APIChatRequest request) throws APIManagementException;

    /**
     * Executes the API Chat "execute" stage against the underlying AI service.
     *
     * @param request the request context (request id, request payload, and any additional properties)
     * @return the raw JSON response body returned by the AI service, expected to map onto {@code ApiChatResponseDTO}
     * @throws APIManagementException if an error occurs while invoking the AI service
     */
    APIChatResponse execute(APIChatRequest request) throws APIManagementException;
}
