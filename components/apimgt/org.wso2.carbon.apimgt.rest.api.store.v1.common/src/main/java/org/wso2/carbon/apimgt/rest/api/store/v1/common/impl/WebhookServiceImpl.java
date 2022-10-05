/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.common.impl;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.AsyncAPIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionListDTO;

import java.util.Set;

/**
 * This class has Webhooks ApiService related Implementation
 */
public class WebhookServiceImpl {

    private WebhookServiceImpl() {
    }

    /**
     *
     * @param applicationId
     * @param apiId
     * @return
     */
    public static WebhookSubscriptionListDTO getWebhooksSubscriptions(String applicationId, String apiId)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        WebhookSubscriptionListDTO webhookSubscriptionListDTO;
        Set<Subscription> subscriptionSet = apiConsumer.getTopicSubscriptions(applicationId, apiId);
        webhookSubscriptionListDTO = AsyncAPIMappingUtil.fromSubscriptionListToDTO(subscriptionSet);
        return webhookSubscriptionListDTO;

    }
}
