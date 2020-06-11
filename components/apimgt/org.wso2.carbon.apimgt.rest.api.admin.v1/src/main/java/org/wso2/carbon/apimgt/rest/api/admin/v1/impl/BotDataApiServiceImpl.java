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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.BotDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionDataListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EmailDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.BotDetectionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

public class BotDataApiServiceImpl implements BotDataApiService {

    private static final Log log = LogFactory.getLog(BotDataApiServiceImpl.class);

    @Override
    public Response botDataAddEmailPost(EmailDTO body, MessageContext messageContext) {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        try {
            apiAdminImpl.addBotDataEmailConfiguration(body.getEmail());
            return Response.ok().build();
        } catch (APIManagementException | SQLException e) {
            String errorMessage = "Error when sending email ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response botDataDeleteEmailDelete(String uuid, MessageContext messageContext) {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        try {
            apiAdminImpl.deleteBotDataEmailList(uuid);
            return Response.ok().build();
        } catch (APIManagementException | SQLException e) {
            String errorMessage = "Error when deleting email ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response botDataGetEmailListGet(String tenantDomain, MessageContext messageContext) {
        try {
            List<BotDetectionData> emailList = new APIAdminImpl().retrieveSavedBotDataEmailList();
            return Response.ok().entity(emailList).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error when getting email ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get all bot detected data
     *
     * @param messageContext
     * @return list of all bot detected data
     * @throws APIManagementException
     */
    @Override
    public Response botDataGet(MessageContext messageContext) throws APIManagementException {

        if (APIUtil.isAnalyticsEnabled()) {
            List<BotDetectionData> botDetectionDataList = new APIAdminImpl().retrieveSavedBotData();
            BotDetectionDataListDTO listDTO = BotDetectionMappingUtil.fromBotDetectionModelToDTO(botDetectionDataList);
            return Response.ok().entity(listDTO).build();
        } else {
            throw new APIManagementException("Analytics Not Enabled", ExceptionCodes.ANALYTICS_NOT_ENABLED);
        }
    }
}
