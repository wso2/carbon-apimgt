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
 * Extension point used to attach additional properties to the payload of an outbound AI service request.
 * <p>
 * Every AI assistance feature (Marketplace Assistant, API Chat, Design Assistant) dispatches its outbound request
 * through a single invocation path, and that path consults the configured implementation of this interface right
 * before the request is sent. The properties returned by {@link #getAdditionalProperties(AIRequestContext)} are added
 * to the outgoing JSON payload as new top level attributes. Attributes the product already places in the payload are
 * never replaced, so an implementation can only contribute new ones.
 * <p>
 * The implementation is selected with the {@code propertyEnricherImpl} configuration under {@code [apim.ai]} in
 * {@code deployment.toml}. A single implementation serves every AI assistance feature; use
 * {@link AIRequestContext#getResource()} to return different properties per AI service operation.
 * <p>
 * Implementations must be thread safe and stateless with respect to a single request. One instance is created lazily
 * at first use and shared across all concurrent requests. Perform expensive initialisation in the constructor, not in
 * {@link #getAdditionalProperties(AIRequestContext)}, which runs on the request critical path.
 * <p>
 * Implementations are advised to extend {@link AbstractAIRequestPropertyEnricher} rather than implementing this
 * interface directly, so that future additions to the contract do not break them.
 */
public interface AIRequestPropertyEnricher {

    /**
     * Returns the additional properties to be added to the outbound AI service request payload.
     *
     * @param context details of the AI service request being dispatched. Never {@code null}, but individual attributes
     *                such as {@link AIRequestContext#getUsername()} may be {@code null} depending on the feature.
     * @return properties to add to the payload. An empty map or {@code null} means no properties are added.
     * @throws APIManagementException if the properties cannot be resolved. The request is then dispatched without the
     *                                additional properties; it is not failed.
     */
    Map<String, Object> getAdditionalProperties(AIRequestContext context) throws APIManagementException;
}
