/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.api;

import java.util.Map;

/**
 * Extension point used to attach additional properties to the payloads of outbound AI service requests.
 * <p>
 * There is one method per AI assistance operation. Each is invoked only when that particular operation builds its
 * payload, so an implementation contributes properties exactly to the operations whose methods it overrides and leaves
 * every other payload untouched. An implementation that only needs to add properties to the Marketplace Assistant chat
 * request overrides {@link #enrichMarketplaceAssistantChatProperties(AIRequestContext)} alone.
 * <p>
 * The properties returned are added to the outgoing JSON payload as new top level attributes. Attributes the product
 * already places in the payload are never replaced, so an implementation can only contribute new ones.
 * <p>
 * The implementation is selected with the {@code propertyEnricherImpl} configuration under {@code [apim.ai]} in
 * {@code deployment.toml}. A single implementation serves every AI assistance operation.
 * <p>
 * Implementations must be thread safe and stateless with respect to a single request. One instance is created lazily at
 * first use and shared across all concurrent requests. Perform expensive initialisation in the constructor, not in
 * these methods, which run on the request critical path.
 * <p>
 * Implementations are expected to extend {@link AbstractAIRequestPropertyEnricher}, which returns an empty map from
 * every method, rather than implementing this interface directly. Doing so keeps an implementation source compatible
 * when methods are added for new AI assistance operations.
 */
public interface AIRequestPropertyEnricher {

    /**
     * Returns the additional properties for the Marketplace Assistant chat request, raised when a Developer Portal user
     * sends a query to the Marketplace Assistant.
     *
     * @param context details of the AI service request being dispatched
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) throws APIManagementException;

    /**
     * Returns the additional properties for the Marketplace Assistant API publish request, raised asynchronously by a
     * notifier when an API is published to the vector database. There is no end user behind this request, so
     * {@link AIRequestContext#getUsername()} is {@code null}.
     *
     * @param context details of the AI service request being dispatched
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> enrichMarketplaceAssistantApiPublishProperties(AIRequestContext context)
            throws APIManagementException;

    /**
     * Returns the additional properties for the API Chat prepare request, which uploads the API definition to the AI
     * service before a test run begins.
     *
     * @param context details of the AI service request being dispatched
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> enrichApiChatPrepareProperties(AIRequestContext context) throws APIManagementException;

    /**
     * Returns the additional properties for the API Chat execute request, raised for each step of an API Chat test run.
     *
     * @param context details of the AI service request being dispatched
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> enrichApiChatExecuteProperties(AIRequestContext context) throws APIManagementException;

    /**
     * Returns the additional properties for the Design Assistant chat request, raised when a Publisher user describes
     * an API to the Design Assistant.
     *
     * @param context details of the AI service request being dispatched
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> enrichDesignAssistantChatProperties(AIRequestContext context) throws APIManagementException;

    /**
     * Returns the additional properties for the Design Assistant payload generation request, which turns a completed
     * design session into an API payload.
     *
     * @param context details of the AI service request being dispatched
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> enrichDesignAssistantPayloadGenProperties(AIRequestContext context) throws APIManagementException;
}
