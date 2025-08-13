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
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.sql.Timestamp;
import java.time.Instant;

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
            return APIConstants.GatewayNotification.STATUS_EXPIRED;
        }
        try {
            GatewayNotificationConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
            long currentTime = Instant.now().toEpochMilli();
            long expireTimeThreshold = currentTime - (config.getGatewayCleanupConfiguration().getExpireTimeSeconds() * 1000L);

            return lastUpdated.getTime() >= expireTimeThreshold
                    ? APIConstants.GatewayNotification.STATUS_ACTIVE
                    : APIConstants.GatewayNotification.STATUS_EXPIRED;
        } catch (Exception e) {
            log.warn("Error validating gateway status, assuming expired", e);
            return APIConstants.GatewayNotification.STATUS_EXPIRED;
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
}
