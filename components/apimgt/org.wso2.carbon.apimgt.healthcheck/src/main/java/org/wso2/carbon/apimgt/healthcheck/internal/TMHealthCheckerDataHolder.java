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

package org.wso2.carbon.apimgt.healthcheck.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DataReceiver;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * Holds the OSGi services the Traffic Manager health checker needs, so the checker can be created with no
 * arguments - the same way carbon health check's {@code DataSourceHealthChecker} looks up what it needs itself.
 * Filled in by {@link TMHealthCheckerServiceComponent}.
 */
public final class TMHealthCheckerDataHolder {

    private static volatile APIManagerConfigurationService apiManagerConfigurationService;
    private static volatile EventStreamService eventStreamService;
    private static volatile DataBridgeReceiverService dataBridgeReceiverService;

    /**
     * Same receiver name and default pool size the binary receiver itself uses
     * ({@code BinaryDataReceiverConstants}).
     */
    private static final String BINARY_RECEIVER_NAME = "Binary";
    private static final int DEFAULT_RECEIVER_THREAD_POOL_SIZE = 100;

    private TMHealthCheckerDataHolder() {

    }

    static void setAPIManagerConfigurationService(APIManagerConfigurationService service) {

        apiManagerConfigurationService = service;
    }

    /**
     * Clears the held service only if {@code service} is still the one held. Every reference here is
     * {@code ReferencePolicy.DYNAMIC}: when one is rebound to a replacement, Declarative Services binds the
     * replacement before it unbinds the one being replaced, so by the time this runs for the old service, the
     * field may already hold a different, newer one - which must survive, not be erased.
     */
    static void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {

        if (apiManagerConfigurationService == service) {
            apiManagerConfigurationService = null;
        }
    }

    /**
     * @return the Traffic Manager's live configuration, or {@code null} if the service is not available
     */
    public static APIManagerConfiguration getTrafficManagerConfiguration() {

        APIManagerConfigurationService service = apiManagerConfigurationService;
        return service == null ? null : service.getAPIManagerConfiguration();
    }

    static void setEventStreamService(EventStreamService service) {

        eventStreamService = service;
    }

    /**
     * @see #unsetAPIManagerConfigurationService(APIManagerConfigurationService)
     */
    static void unsetEventStreamService(EventStreamService service) {

        if (eventStreamService == service) {
            eventStreamService = null;
        }
    }

    /**
     * @return the event stream service used to register/remove the marker stream, or {@code null} if not available
     */
    static EventStreamService getEventStreamService() {

        return eventStreamService;
    }

    static void setDataBridgeReceiverService(DataBridgeReceiverService service) {

        dataBridgeReceiverService = service;
    }

    /**
     * @see #unsetAPIManagerConfigurationService(APIManagerConfigurationService)
     */
    static void unsetDataBridgeReceiverService(DataBridgeReceiverService service) {

        if (dataBridgeReceiverService == service) {
            dataBridgeReceiverService = null;
        }
    }

    /**
     * Reads a throttle event receiver thread pool size the same way the binary receiver itself does, from the
     * {@code Binary} receiver's configuration in {@code data-bridge-config.xml}, defaulting to 100.
     *
     * @param configName {@code tcpReceiverThreadPoolSize} or {@code sslReceiverThreadPoolSize}
     * @return the pool size, or {@code null} if the receiver's configuration is not available
     */
    public static Integer getReceiverThreadPoolSize(String configName) {

        DataBridgeReceiverService service = dataBridgeReceiverService;
        DataBridgeConfiguration configuration = service == null ? null : service.getInitialConfig();
        DataReceiver receiver = configuration == null ? null : configuration.getDataReceiver(BINARY_RECEIVER_NAME);
        if (receiver == null) {
            return null;
        }
        try {
            return Integer.parseInt(
                    String.valueOf(receiver.getConfiguration(configName, DEFAULT_RECEIVER_THREAD_POOL_SIZE)).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
