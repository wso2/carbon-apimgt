/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.OrganizationDetailsDTO;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.OrganizationsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class OrganizationsApiServiceImpl implements OrganizationsApiService {

    public Response organizationsGet(MessageContext messageContext) throws APIManagementException {
        
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            String orgId = null;
            if (orgInfo != null && orgInfo.getOrganizationId() != null) {
                orgId = orgInfo.getOrganizationId();
            } else {
                String errorMessage = "User does not belong to any organization.";
                throw new APIManagementException(errorMessage, ExceptionCodes.MISSING_ORGANINATION);
            }
            List<OrganizationDetailsDTO> orgList = apiProvider.getOrganizations(orgId, superOrganization);

            OrganizationListDTO organizationsListDTO = OrganizationsMappingUtil.toOrganizationsListDTO(orgList, orgId);
            return Response.ok().entity(organizationsListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Organizations";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }
}
