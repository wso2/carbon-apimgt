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
    private static final String INVALID_LOGGING_PERMISSION = "Invalid logging permission";
    private static final String LOGGING_PERMISSION_PATH = "/permission/protected";

    public boolean updateCorrelationConfigs(List<CorrelationConfigDTO> correlationConfigDTOList) throws
            APIManagementException {
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), LOGGING_PERMISSION_PATH)) {
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        boolean result = CorrelationConfigDAO.getInstance().updateCorrelationConfigs(correlationConfigDTOList);
        if (result) {
            publishCorrelationConfigData(correlationConfigDTOList);
        }
        return result;
    }

    private void publishCorrelationConfigData(List<CorrelationConfigDTO> correlationConfigDTOList) {

        CorrelationConfigEvent event = new CorrelationConfigEvent(correlationConfigDTOList,
                APIConstants.EventType.UPDATE_CORRELATION_CONFIGS.name());
        APIUtil.sendNotification(event, APIConstants.NotifierType.CORRELATION_CONFIG.name());
    }

    public List<CorrelationConfigDTO> getCorrelationConfigs() throws APIManagementException {
        if (!APIUtil.hasPermission(RestApiCommonUtil.getLoggedInUsername(), APIConstants.Permissions.APIM_ADMIN)) {
            throw new APIManagementException(INVALID_LOGGING_PERMISSION,
                    ExceptionCodes.from(ExceptionCodes.INVALID_PERMISSION));
        }
        return CorrelationConfigDAO.getInstance().getCorrelationConfigsList();
    }

}
