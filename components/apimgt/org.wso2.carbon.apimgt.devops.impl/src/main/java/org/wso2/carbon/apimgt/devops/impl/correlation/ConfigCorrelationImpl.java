/*
 *
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.devops.impl.correlation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.CorrelationConfigDAO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.notifier.events.CorrelationConfigEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import java.util.List;

/**
 * DevOps API Correlation Config Implementation.
 */
public class ConfigCorrelationImpl {
    private static final Log log = LogFactory.getLog(ConfigCorrelationImpl.class);
    private static final String INVALID_LOGGING_PERMISSION = "Invalid logging permission";
    private static final String LOGGING_PERMISSION_PATH = "/permission/protected";

    /**
     * Update correlation configs
     *
     * @param correlationConfigDTOList Correlation config list
     * @return results 
     */
    public boolean updateCorrelationConfigs(List<CorrelationConfigDTO> correlationConfigDTOList) throws
            APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (log.isDebugEnabled()) {
            log.debug("Updating correlation configs for user: " + username);
        }
        if (!APIUtil.hasPermission(username, LOGGING_PERMISSION_PATH)) {
            log.warn("User " + username + " does not have permission to update correlation configs");
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        boolean result = CorrelationConfigDAO.getInstance().updateCorrelationConfigs(correlationConfigDTOList);
        if (result) {
            log.info("Successfully updated correlation configurations");
            publishCorrelationConfigData(correlationConfigDTOList);
        } else {
            log.warn("Failed to update correlation configurations");
        }
        return result;
    }

    /**
     * Publish correlation config data
     *
     * @param correlationConfigDTOList Correlation config list
     */
    private void publishCorrelationConfigData(List<CorrelationConfigDTO> correlationConfigDTOList) {
        if (log.isDebugEnabled()) {
            log.debug("Publishing correlation config data notification");
        }
        CorrelationConfigEvent event = new CorrelationConfigEvent(correlationConfigDTOList,
                APIConstants.EventType.UPDATE_CORRELATION_CONFIGS.name());
        APIUtil.sendNotification(event, APIConstants.NotifierType.CORRELATION_CONFIG.name());
    }

    /**
     * Get correlation configs
     *
     * @return Correlation configs List
     */
    public List<CorrelationConfigDTO> getCorrelationConfigs() throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (log.isDebugEnabled()) {
            log.debug("Retrieving correlation configs for user: " + username);
        }
        if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
            log.warn("User " + username + " does not have admin permission to retrieve correlation configs");
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        List<CorrelationConfigDTO> configs = CorrelationConfigDAO.getInstance().getCorrelationConfigsList();
        if (log.isDebugEnabled()) {
            log.debug("Retrieved " + (configs != null ? configs.size() : 0) + " correlation configurations");
        }
        return configs;
    }
}
