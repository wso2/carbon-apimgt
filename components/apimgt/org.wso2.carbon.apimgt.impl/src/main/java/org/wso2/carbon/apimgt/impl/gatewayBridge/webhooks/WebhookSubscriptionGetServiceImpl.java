/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.gatewayBridge.webhooks;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.gatewayBridge.dao.WebhooksDAO;
import org.wso2.carbon.apimgt.impl.gatewayBridge.dto.WebhookSubscriptionDTO;

import java.util.List;
/**
 * Get the subscriptions for the topic from database.
 */
public class WebhookSubscriptionGetServiceImpl implements WebhookSubscriptionGetService {

    /**
     *Invoke the services to retrieve
     * subscription list.
     * @param subscriberName the subscribed topic.
     * @return a list of webhook subscription.
     */
    @Override
    public List<WebhookSubscriptionDTO> getWebhookSubscription(String subscriberName) throws APIManagementException {
        WebhooksDAO webhooksDAO = new WebhooksDAO();
        return webhooksDAO.getSubscriptionsList(subscriberName);
    }
}
