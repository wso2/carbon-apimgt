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

import java.util.Collections;
import java.util.Map;

/**
 * Base class for {@link AIRequestPropertyEnricher} implementations. Every method returns an empty map, so a subclass
 * overrides only the AI assistance operations it wants to add properties to and every other payload is dispatched
 * unchanged.
 * <p>
 * For example, an implementation that adds the invoking user name to the Marketplace Assistant chat request and to
 * nothing else:
 * <pre>
 * public class AcmeEnricher extends AbstractAIRequestPropertyEnricher {
 *
 *     &#64;Override
 *     public Map&lt;String, Object&gt; getMarketplaceAssistantChatProperties(AIRequestContext context) {
 *
 *         return Collections.singletonMap("username", context.getUsername());
 *     }
 * }
 * </pre>
 * <p>
 * Extending this class rather than implementing the interface directly keeps an implementation source compatible when
 * methods are added for new AI assistance operations.
 */
public abstract class AbstractAIRequestPropertyEnricher implements AIRequestPropertyEnricher {

    @Override
    public Map<String, Object> getMarketplaceAssistantChatProperties(AIRequestContext context)
            throws APIManagementException {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getMarketplaceAssistantApiPublishProperties(AIRequestContext context)
            throws APIManagementException {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getApiChatPrepareProperties(AIRequestContext context) throws APIManagementException {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getApiChatExecuteProperties(AIRequestContext context) throws APIManagementException {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getDesignAssistantChatProperties(AIRequestContext context)
            throws APIManagementException {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getDesignAssistantPayloadGenProperties(AIRequestContext context)
            throws APIManagementException {

        return Collections.emptyMap();
    }
}
