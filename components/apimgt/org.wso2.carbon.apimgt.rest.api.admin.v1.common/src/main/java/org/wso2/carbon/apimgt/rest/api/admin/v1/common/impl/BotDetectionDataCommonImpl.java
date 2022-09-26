/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.mappings.BotDetectionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionDataListDTO;

import java.util.List;

public class BotDetectionDataCommonImpl {

    private BotDetectionDataCommonImpl() {
    }

    /**
     * Get all bot detected data
     *
     * @return List of Bot Detection data details
     * @throws APIManagementException When an internal error occurs
     */
    public static BotDetectionDataListDTO getBotDetectionData() throws APIManagementException {
        if (APIUtil.isAnalyticsEnabled()) {
            APIAdmin apiAdmin = new APIAdminImpl();
            List<BotDetectionData> botDetectionDataList = apiAdmin.retrieveBotDetectionData();
            return BotDetectionMappingUtil.fromBotDetectionModelToDTO(botDetectionDataList);
        } else {
            throw new APIManagementException("Analytics Not Enabled",
                    ExceptionCodes.from(ExceptionCodes.ANALYTICS_NOT_ENABLED, "Bot Detection Data is",
                            "Bot Detection Data"));
        }
    }
}
