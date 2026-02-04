/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.APIKeyLastUsedNotifier;

import java.util.Properties;

/**
 * Publisher class to notify the api key usage
 */
public class ApiKeyUsagePublisher {

    private static final Log log = LogFactory.getLog(ApiKeyUsagePublisher.class);

    private static ApiKeyUsagePublisher apiKeyUsagePublisher = null;
    private boolean realtimeNotifierEnabled;
    private APIKeyLastUsedNotifier apiKeyLastUsedNotifier;

    private ApiKeyUsagePublisher() {

        Properties realtimeNotifierProperties = APIManagerConfiguration.getRealtimeApiKeyUsageNotifierProperties();
        realtimeNotifierEnabled = realtimeNotifierProperties != null;
        apiKeyLastUsedNotifier = ServiceReferenceHolder.getInstance().getApiKeyLastUsedNotifier();
        apiKeyLastUsedNotifier.init(realtimeNotifierProperties);
        log.debug("API key last used notifier initialized");
    }

    public static synchronized ApiKeyUsagePublisher getInstance() {

        if (apiKeyUsagePublisher == null) {
            apiKeyUsagePublisher = new ApiKeyUsagePublisher();
        }
        return apiKeyUsagePublisher;
    }

    public void publishApiKeyUsageEvents(Properties properties) {

        if (realtimeNotifierEnabled) {
            log.debug("Realtime message sending is enabled");
            apiKeyLastUsedNotifier.sendMessageOnRealtime(properties);
        } else {
            log.debug("Realtime message sending isn't enabled or configured properly");
        }
    }
}
