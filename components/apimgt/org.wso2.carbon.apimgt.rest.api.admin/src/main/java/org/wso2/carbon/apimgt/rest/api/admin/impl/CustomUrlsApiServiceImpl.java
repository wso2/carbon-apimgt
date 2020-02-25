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
package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import java.util.List;

import java.io.InputStream;
import java.util.Map;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;

public class CustomUrlsApiServiceImpl extends CustomUrlsApiService {

    private static final Log log = LogFactory.getLog(CustomUrlsApiServiceImpl.class);

    @Override
    public Response getCustomUrlInfoByTenantDomain(String tenantDomain){

        try {
            boolean isTenantAvailable = APIUtil.isTenantAvailable(tenantDomain);
            if(!isTenantAvailable) {
                return Response.status(Response.Status.NOT_FOUND).build(); // tenant does not exist
            }
            CustomUrlInfoDTO customUrlInfoDTO = new CustomUrlInfoDTO();
            boolean  perTenantServiceProviderEnabled = APIUtil.isPerTenantServiceProviderEnabled(tenantDomain);
            if(perTenantServiceProviderEnabled) {
                Map tenantBasedStoreDomainMapping = APIUtil.getTenantBasedStoreDomainMapping(tenantDomain);
                if(tenantBasedStoreDomainMapping != null) {
                    CustomUrlInfoDevPortalDTO customUrlInfoDevPortalDTO = new CustomUrlInfoDevPortalDTO();
                    customUrlInfoDevPortalDTO.setUrl((String)tenantBasedStoreDomainMapping.get("customUrl"));
                    customUrlInfoDTO.setDevPortal(customUrlInfoDevPortalDTO);
                }
            }
            customUrlInfoDTO.setTenantAdminUsername(APIUtil.getTenantAdminUserName(tenantDomain));
            customUrlInfoDTO.setEnabled(perTenantServiceProviderEnabled);
            customUrlInfoDTO.setTenantDomain(tenantDomain);
            return Response.ok().entity(customUrlInfoDTO).build();
        } catch (UserStoreException | APIManagementException | RegistryException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving custom url info for tenant : " +
                    tenantDomain, log);
        }
        return null;
    }
}
