/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.webhooks;

import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;

import java.util.List;

/*
The interface of webhooks subscription operations
 */
public interface SubscriptionsDataService {

    void addSubscription(String apiKey, String topicName, String tenantDomain, WebhooksDTO subscriber);

    void removeSubscription(String apiKey, String topicName, String tenantDomain, WebhooksDTO subscriber);

    List<WebhooksDTO> getSubscriptionsList(String apiKey, String tenantDomain);

    void updateThrottleStatus(String appID, String apiUUID, String tenantDomain, boolean status);

    boolean getThrottleStatus(String appID, String apiUUID, String tenantDomain);
}
