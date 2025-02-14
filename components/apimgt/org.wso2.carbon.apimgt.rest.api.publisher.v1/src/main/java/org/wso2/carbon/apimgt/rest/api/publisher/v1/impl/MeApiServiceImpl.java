/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.impl.restapi.publisher.MeApiServiceImplUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.MeApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for operations related to the logged-in user (/me endpoint)
 */
public class MeApiServiceImpl implements MeApiService {

    /**
     * Check whether the logged-in user has given role
     *
     * @param roleId Base64 URL encoded form of role name -Base64URLEncode{user-store-name/role-name}
     * @return 200 if logged-in user has given role
     */
    public Response validateUserRole(String roleId, MessageContext messageContext) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        boolean isUserInRole = false;

        if (roleId != null) {
            isUserInRole = MeApiServiceImplUtils.checkUserInRole(roleId, userName);
        }
        if (isUserInRole) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response organizationInformation(MessageContext messageContext) throws APIManagementException {
        OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
        OrganizationInfoDTO dto = new OrganizationInfoDTO();
        dto.setName(orgInfo.getName());
        dto.setOrganizationId(orgInfo.getOrganizationId());
        return Response.ok().entity(dto).build();
    }
}
