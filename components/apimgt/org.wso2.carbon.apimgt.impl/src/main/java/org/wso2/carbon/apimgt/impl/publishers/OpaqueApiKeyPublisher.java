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
import org.wso2.carbon.apimgt.impl.token.OpaqueAPIKeyNotifier;

import java.util.Properties;

/**
 * Publisher class to notify api key info
 */
public class OpaqueApiKeyPublisher {

    private static final Log log = LogFactory.getLog(OpaqueApiKeyPublisher.class);

    private static OpaqueApiKeyPublisher opaqueApiKeyPublisher = null;
    private boolean realtimeNotifierEnabled;
    private OpaqueAPIKeyNotifier opaqueApiKeyNotifier;

    private OpaqueApiKeyPublisher() {

        Properties realtimeNotifierProperties = APIManagerConfiguration.getRealtimeOpaqueApiKeyNotifierProperties();
        realtimeNotifierEnabled = realtimeNotifierProperties != null;
        opaqueApiKeyNotifier = ServiceReferenceHolder.getInstance().getOpaqueApiKeyNotifier();
        opaqueApiKeyNotifier.init(realtimeNotifierProperties);
        log.debug("Opaque API key notifier initialized");
    }

    public static synchronized OpaqueApiKeyPublisher getInstance() {

        if (opaqueApiKeyPublisher == null) {
            opaqueApiKeyPublisher = new OpaqueApiKeyPublisher();
        }
        return opaqueApiKeyPublisher;
    }

    public void publishApiKeyUsageEvents(Properties properties) {

        if (realtimeNotifierEnabled) {
            log.debug("Realtime message sending is enabled");
            opaqueApiKeyNotifier.sendLastUsedTimeOnRealtime(properties);
        } else {
            log.debug("Realtime message sending isn't enabled or configured properly");
        }
    }

    public void publishApiKeyInfoEvents(Properties properties) {

        if (realtimeNotifierEnabled) {
            log.debug("Realtime message sending is enabled");
            opaqueApiKeyNotifier.sendApiKeyInfoOnRealtime(properties);
        } else {
            log.debug("Realtime message sending isn't enabled or configured properly");
        }
    }
}
