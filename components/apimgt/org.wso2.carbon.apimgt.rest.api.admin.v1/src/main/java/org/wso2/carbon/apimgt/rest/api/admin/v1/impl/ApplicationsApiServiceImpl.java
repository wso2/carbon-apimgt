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
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.ApplicationsCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl implements ApplicationsApiService {

    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    @Override
    public Response changeApplicationOwner(String owner, String applicationId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        boolean applicationUpdated = ApplicationsCommonImpl
                .changeApplicationOwner(owner, applicationId, organization);
        if (applicationUpdated) {
            return Response.ok().build();
        }
        return null;
    }

    @Override
    public Response removeApplication(String applicationId, MessageContext messageContext) throws APIManagementException {
        ApplicationsCommonImpl.removeApplication(applicationId);
        return Response.ok().build();
    }

    @Override
    public Response getApplicationsByUser(String user, Integer limit, Integer offset, String accept,
                                          String applicationName, String tenantDomain,
                                          String sortBy, String sortOrder, MessageContext messageContext)
            throws APIManagementException {

        if (!MultitenantUtils.getTenantDomain(user).equals(RestApiCommonUtil.getLoggedInUserTenantDomain())) {
            String errorMsg = "User " + user + " is not available for the current tenant domain";
            log.error(errorMsg);
            return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();
        }
        ApplicationListDTO applicationListDTO =
                ApplicationsCommonImpl.getApplicationsByUser(user, limit, offset, applicationName, tenantDomain,
                        sortBy, sortOrder);
        return Response.ok().entity(applicationListDTO).build();
    }

    @Override
    public Response getApplicationById(String applicationId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getOrganization(messageContext);
        ApplicationDTO applicationDTO = ApplicationsCommonImpl.getApplicationById(applicationId, organization);
        return Response.ok().entity(applicationDTO).build();
    }
}
