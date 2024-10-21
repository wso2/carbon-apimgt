/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.notifier.events.LLMProviderEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.UUID;

public class LLMProviderNotificationSender {

    /**
     * Sends a notification about an LLM Provider event with the specified details.
     *
     * @param name         The name of the LLM Provider.
     * @param apiVersion   The API version of the LLM Provider.
     * @param organization The organization associated with the LLM Provider.
     * @param action       The action related to the LLM Provider (e.g., create, update).
     * @throws APIManagementException If an error occurs while sending the notification.
     */
    public void notify(String id, String name, String apiVersion, String configuration,
                       String organization, String action) throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        LLMProviderEvent llmProviderEvent = new LLMProviderEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), action, tenantId, organization, id, name, apiVersion, configuration);
        APIUtil.sendNotification(llmProviderEvent, APIConstants.NotifierType.LLM_PROVIDER.name());
    }
}
