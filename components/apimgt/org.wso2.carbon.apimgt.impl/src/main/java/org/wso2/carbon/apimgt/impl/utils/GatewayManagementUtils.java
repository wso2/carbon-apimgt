/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.dto.PlatformGatewayConnectConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.service.PlatformGatewayServiceImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for gateway management operations.
 * Provides centralized methods for gateway status calculation and data cleanup operations.
 */
public class GatewayManagementUtils {
    
    private static final Log log = LogFactory.getLog(GatewayManagementUtils.class);

    /**
     * Validates the gateway status based on its last updated timestamp.
     * Returns "ACTIVE" if the gateway is live (not expired), "EXPIRED" otherwise.
     *
     * @param lastUpdated the timestamp when the gateway was last updated
     * @return "ACTIVE" if live, "EXPIRED" if expired
     */
    public static String validateGatewayStatus(Timestamp lastUpdated) {
        if (lastUpdated == null) {
            return APIConstants.GatewayNotificationConfigurationConstants.STATUS_EXPIRED;
        }
        try {
            GatewayNotificationConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
            long currentTime = Instant.now().toEpochMilli();
            long expireTimeThreshold = currentTime - (config.getGatewayCleanupConfiguration().getExpireTimeSeconds() * 1000L);

            return lastUpdated.getTime() >= expireTimeThreshold
                    ? APIConstants.GatewayNotificationConfigurationConstants.STATUS_ACTIVE
                    : APIConstants.GatewayNotificationConfigurationConstants.STATUS_EXPIRED;
        } catch (Exception e) {
            log.warn("Error validating gateway status, assuming expired", e);
            return APIConstants.GatewayNotificationConfigurationConstants.STATUS_EXPIRED;
        }
    }

    /**
     * Performs cleanup of old gateway records based on the configured retention period.
     * This method removes gateway instances that are older than the data retention threshold.
     *
     * @throws APIManagementException if the cleanup operation fails
     */
    public static void performGatewayDataCleanup() throws APIManagementException {
        try {
            long currentTime = Instant.now().toEpochMilli();
            GatewayNotificationConfiguration configuration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
            long retentionThreshold = currentTime - 
                    (configuration.getGatewayCleanupConfiguration().getDataRetentionPeriodSeconds() * 1000L);
            Timestamp retentionTimestamp = new Timestamp(retentionThreshold);

            GatewayManagementDAO gatewayManagementDAO = GatewayManagementDAO.getInstance();
            int deletedCount = gatewayManagementDAO.deleteOldGatewayRecords(retentionTimestamp);
            
            if (log.isInfoEnabled() && (deletedCount > 0)) {
                log.info("Gateway cleanup completed - Deleted: " + deletedCount);
            }
        } catch (APIManagementException e) {
            log.error("Gateway cleanup failed: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during gateway cleanup: " + e.getMessage();
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * Validates connect-with-token config on startup. Fails server startup with clear errors if mandatory
     * fields are missing or invalid. Gateways are created on first REGISTER, not at startup.
     */
    public static void performPlatformGatewayConnectFromConfigIfConfigured() {
        try {
            PlatformGatewayConnectConfig config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration().getPlatformGatewayConnectConfig();
            if (config == null) {
                return;
            }
            List<org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig> connectGateways = config.getConnectGateways();
            if (connectGateways == null || connectGateways.isEmpty()) {
                return;
            }
            String sep = PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR;
            List<String> errors = new ArrayList<>();
            int index = 0;
            for (org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig entry : connectGateways) {
                if (entry == null) {
                    continue;
                }
                index++;
                String token = entry.getRegistrationToken();
                if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
                    continue;
                }
                String prefix = "[[apim.universal_gateway.connect]] entry " + index + ": ";
                if (org.apache.commons.lang3.StringUtils.isBlank(entry.getUrl())) {
                    errors.add(prefix + "mandatory 'url' is missing (base URL where the gateway will be accessible, e.g. https://host:8243)");
                }
                int idx = token.indexOf(sep);
                if (idx <= 0 || idx >= token.length() - 1) {
                    errors.add(prefix + "invalid registration_token format (expected tokenId" + sep + "plainToken)");
                }
            }
            if (!errors.isEmpty()) {
                for (String err : errors) {
                    log.error(err);
                }
                throw new IllegalArgumentException(
                        "Platform gateway connect config validation failed at server startup. " +
                        "Fix the following and restart: " + String.join("; ", errors));
            }
            log.info("Connect-with-token configured for " + connectGateways.size() + " gateway(s); they can register on first REGISTER.");
            PlatformGatewayServiceImpl.ensurePlatformGatewayFromConfigOnStartup(config);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Platform gateway connect config failed at startup: " + e.getMessage(), e);
            throw new IllegalStateException("Platform gateway connect config failed at startup: " + e.getMessage(), e);
        }
    }
}
