/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.OrganizationDetailsDTO;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.OrganizationsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import com.google.gson.Gson;

import org.apache.cxf.jaxrs.ext.MessageContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.Response;


public class OrganizationsApiServiceImpl implements OrganizationsApiService {

    public Response organizationsGet(MessageContext messageContext) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            String parentOrgId = null;
            if (orgInfo != null && orgInfo.getOrganizationId() != null) {
                parentOrgId = orgInfo.getOrganizationId(); // assign current user org as the parent org.
            }
            // retrieve child organizations for the current user.
            List<OrganizationDetailsDTO> orgList = apiAdmin.getOrganizations(parentOrgId,
                    superOrganization);

            OrganizationListDTO organizationsListDTO = OrganizationsMappingUtil.toOrganizationsListDTO(orgList, parentOrgId);
            return Response.ok().entity(organizationsListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Organizations";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public Response organizationsOrganizationIdDelete(String organizationId, MessageContext messageContext)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationDetailsDTO organizationInfoDTO = apiAdmin.getOrganizationDetails(organizationId,
                    superOrganization);
            if (organizationInfoDTO == null) {
                throw new APIManagementException("Requested Organization not found",
                        ExceptionCodes.INVALID_ORGANINATION);
            }
            apiAdmin.deleteOrganization(organizationId, superOrganization);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.ORGANIZATION, new Gson().toJson(organizationInfoDTO),
                    APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Organizations";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public Response organizationsOrganizationIdPut(String organizationId, OrganizationDTO organizationDTO,
            MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationDetailsDTO organizationInfoDTO = apiAdmin.getOrganizationDetails(organizationId,
                    superOrganization);
            if (organizationInfoDTO == null) {
                throw new APIManagementException("Requested Organization not found",
                        ExceptionCodes.INVALID_ORGANINATION);
            }
            String parentOrgId = null;
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            // Currently we don't support changing parent organization id.
            if (orgInfo != null && orgInfo.getOrganizationId() != null) {
                parentOrgId = orgInfo.getOrganizationId(); // assign current user org as the parent org.
            }
            organizationInfoDTO.setExternalOrganizationReference(organizationDTO.getExternalOrganizationId());
            organizationInfoDTO.setName(organizationDTO.getDisplayName());
            organizationInfoDTO.setDescription(organizationDTO.getDescription());
            OrganizationDetailsDTO updatedOrganizationInfoDTO = apiAdmin.updateOrganization(organizationInfoDTO,
                    parentOrgId, superOrganization);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.ORGANIZATION,
                    new Gson().toJson(updatedOrganizationInfoDTO), APIConstants.AuditLogConstants.UPDATED,
                    RestApiCommonUtil.getLoggedInUsername());
            OrganizationDTO returnedorganizationDTO = OrganizationsMappingUtil
                    .toOrganizationsDTO(updatedOrganizationInfoDTO, parentOrgId);
            return Response.ok().entity(returnedorganizationDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Organization";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public Response organizationsPost(OrganizationDTO organizationDTO, MessageContext messageContext)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            String parentOrgId = null;
            if (orgInfo != null && orgInfo.getOrganizationId() != null) {
                parentOrgId = orgInfo.getOrganizationId(); // set current users organization as parent id if available.
            }
            if (parentOrgId == null) {
                throw new APIManagementException("Parent Organization not found",
                        ExceptionCodes.MISSING_ORGANINATION);
            }
            OrganizationDetailsDTO orgDto = OrganizationsMappingUtil.toOrganizationDetailsDTO(organizationDTO);
            orgDto = apiAdmin.addOrganization(orgDto, parentOrgId, superOrganization);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.ORGANIZATION,
                    new Gson().toJson(orgDto),
                    APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
            URI location = new URI(RestApiConstants.ORGANIZATIONS_PATH + "/" + orgInfo.getId());
            OrganizationDTO returnedorganizationDTO = OrganizationsMappingUtil.toOrganizationsDTO(orgDto, parentOrgId);
            return Response.created(location).entity(returnedorganizationDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            String errorMessage = "Error while creating Organization";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public Response organizationsOrganizationIdGet(String organizationId, MessageContext messageContext)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            String parentOrgId = null;
            if (orgInfo != null && orgInfo.getOrganizationId() != null) {
                parentOrgId = orgInfo.getOrganizationId(); // set current users organization as parent id if available.
            }
            OrganizationDetailsDTO organizationInfoDTO = apiAdmin.getOrganizationDetails(organizationId,
                    superOrganization);
            if (organizationInfoDTO == null) {
                throw new APIManagementException("Requested Organization not found",
                        ExceptionCodes.INVALID_ORGANINATION);
            }
            OrganizationDTO returnedorganizationDTO = OrganizationsMappingUtil
                    .toOrganizationsDTO(organizationInfoDTO, parentOrgId);
            return Response.ok().entity(returnedorganizationDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Organizations";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }
}
