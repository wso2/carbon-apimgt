/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.TenantInfoApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TenantInfoDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.Base64;

public class TenantInfoApiServiceImpl extends TenantInfoApiService {
    Log log = LogFactory.getLog(TenantInfoApiServiceImpl.class);

    @Override
    public Response getTenantInfoByUsername(String username){
        TenantInfoDTO tenantInfoDTO = new TenantInfoDTO();
        String decodedUserName;
        try {
            if (StringUtils.isEmpty(username)) {
                RestApiUtil.handleBadRequest("User Name should not be empty", log);
            }
            try {
                decodedUserName = new String(Base64.getDecoder().decode(username));
            } catch (IllegalArgumentException e) {
                log.warn("Could not decode the username. Using original username");
                decodedUserName = username;
            }
            if (!APIUtil.isUserExist(decodedUserName)) {
                RestApiUtil.handleBadRequest("Requested User " + decodedUserName + " does not exist", log);
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(decodedUserName);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
            tenantInfoDTO.setTenantDomain(tenantDomain);
            tenantInfoDTO.setTenantId(tenantId);
            tenantInfoDTO.setUsername(decodedUserName);
            return Response.status(Response.Status.OK).entity(tenantInfoDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Internal Server Error occurred while retrieving tenant "
                    + "information", e, log);
        }
        return null;
    }
}
