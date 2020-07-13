/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.BotDetectionDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionDataListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.BotDetectionMappingUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class BotDetectionDataApiServiceImpl implements BotDetectionDataApiService {

    /**
     * Get all bot detected data
     *
     * @param messageContext
     * @return list of all bot detected data
     * @throws APIManagementException
     */
    public Response getBotDetectionData(MessageContext messageContext) throws APIManagementException {

        if (APIUtil.isAnalyticsEnabled()) {
            APIAdmin apiAdmin = new APIAdminImpl();
            List<BotDetectionData> botDetectionDataList = apiAdmin.retrieveBotDetectionData();
            BotDetectionDataListDTO listDTO = BotDetectionMappingUtil.fromBotDetectionModelToDTO(botDetectionDataList);
            return Response.ok().entity(listDTO).build();
        } else {
            throw new APIManagementException("Analytics Not Enabled",
                    ExceptionCodes.from(ExceptionCodes.ANALYTICS_NOT_ENABLED, "Bot Detection Data is",
                            "Bot Detection Data"));
        }
    }
}
