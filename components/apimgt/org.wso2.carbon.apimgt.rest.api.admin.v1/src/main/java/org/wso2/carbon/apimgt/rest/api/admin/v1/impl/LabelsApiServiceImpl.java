/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelUsageDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.LabelMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.LabelsUsageMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;


public class LabelsApiServiceImpl implements LabelsApiService {
    private static final Log log = LogFactory.getLog(LabelsApiServiceImpl.class);

    public Response getAllLabels(MessageContext messageContext) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
            List<Label> labelList = apiAdmin.getAllLabelsOfTenant(tenantDomain);
            LabelListDTO labelListDTO =
                    LabelMappingUtil.fromLabelListToLabelListDTO(labelList);
            return Response.ok().entity(labelListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Labels";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response deleteLabel(String labelId, MessageContext messageContext) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
            Label labelOriginal = apiAdmin.getLabelByID(labelId);
            if (labelOriginal == null) {
                RestApiUtil.handleResourceNotFoundInTenantError(RestApiConstants.RESOURCE_LABEL, "", log, tenantDomain);
            }
            apiAdmin.deleteLabel(labelId);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LABELS, labelId,
                    APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Label with ID : " + labelId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response updateLabel(String labelId, LabelDTO body, MessageContext messageContext) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
            Label labelToUpdate = LabelMappingUtil.fromLabelDTOToLabel(body, tenantDomain);
            Label labelOriginal = apiAdmin.getLabelByID(labelId);
            if (labelOriginal == null) {
                RestApiUtil.handleResourceNotFoundInTenantError(RestApiConstants.RESOURCE_LABEL, "", log, tenantDomain);
            }

            //Override several properties as they are not allowed to be updated
            labelToUpdate.setLabelId(labelOriginal.getLabelId());
            labelToUpdate.setType(labelOriginal.getType());
            labelToUpdate.setTenantDomain(labelOriginal.getTenantDomain());

            //We allow to update Label name given that the new label name is not taken yet
            String oldName = labelOriginal.getName();
            String updatedName = labelToUpdate.getName();
            if (!org.apache.commons.lang3.StringUtils.isEmpty(updatedName)) {
                String regExSpecialChars = "!@#$%^&*(),?\"{}[\\]|<>";
                String regExSpecialCharsReplaced = regExSpecialChars.replaceAll(".", "\\\\$0");
                Pattern pattern = Pattern.compile("[" + regExSpecialCharsReplaced + "\\s" + "]");// include \n,\t, space
                Matcher matcher = pattern.matcher(updatedName);
                if (matcher.find()) {
                    RestApiUtil.handleBadRequest("Name field contains special characters.", log);
                }
                if (updatedName.length() > 255) {
                    RestApiUtil.handleBadRequest("Label name is too long.", log);
                }
            } else {
                RestApiUtil.handleBadRequest("Label name is empty.", log);
            }
            if (!oldName.equals(updatedName) && apiAdmin.isLabelNameExists(updatedName, labelOriginal.getType(),
                    labelId, labelOriginal.getTenantDomain())) {
                RestApiUtil.handleResourceAlreadyExistsError("Label with name '" +
                                updatedName + "' already exists", log);
            }
            apiAdmin.updateLabel(labelToUpdate);

            Label updatedLabel = apiAdmin.getLabelByID(labelId);
            LabelDTO updatedLabelDTO = LabelMappingUtil.fromLabelToLabelDTO(updatedLabel);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LABELS, new Gson().toJson(updatedLabelDTO),
                    APIConstants.AuditLogConstants.UPDATED, RestApiCommonUtil.getLoggedInUsername());
            return Response.ok().entity(updatedLabelDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Label '" + body.getName() + "' - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response getLabelUsage(String labelId, MessageContext messageContext) {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
            Label labelOriginal = apiAdmin.getLabelByID(labelId);
            if (labelOriginal == null) {
                RestApiUtil.handleResourceNotFoundInTenantError(RestApiConstants.RESOURCE_LABEL, "", log, tenantDomain);
            }
            List<ApiResult> apiList = apiAdmin.getMappedApisForLabel(labelId);

            LabelUsageDTO labelusageDTO = LabelsUsageMappingUtil.fromApiResultListToLabelUsageDTO(apiList);
            return Response.ok().entity(labelusageDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving label usage for labelId: "
                    + labelId, e, log);
        }
        return null;
    }

    public Response createLabel(LabelDTO body, MessageContext messageContext) {
        Label label = null;
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
            label = LabelMappingUtil.fromLabelDTOToLabel(body, tenantDomain);

            if (!org.apache.commons.lang3.StringUtils.isEmpty(label.getName())) {
                String regExSpecialChars = "!@#$%^&*(),?\"{}[\\]|<>";
                String regExSpecialCharsReplaced = regExSpecialChars.replaceAll(".", "\\\\$0");
                Pattern pattern = Pattern.compile("[" + regExSpecialCharsReplaced + "\\s" + "]");// include \n,\t, space
                Matcher matcher = pattern.matcher(label.getName());
                if (matcher.find()) {
                    RestApiUtil.handleBadRequest("Name field contains special characters.", log);
                }
                if (label.getName().length() > 255) {
                    RestApiUtil.handleBadRequest("Label name is too long.", log);
                }
            } else {
                RestApiUtil.handleBadRequest("Label name is empty.", log);
            }

            if (apiAdmin.isLabelNameExists(label.getName(), label.getType(), null, tenantDomain)) {
                RestApiUtil.handleResourceAlreadyExistsError("Label with name '" +
                                label.getName() + "' already exists", log);
            }

            LabelDTO labelDTO = LabelMappingUtil.
                    fromLabelToLabelDTO(apiAdmin.addLabel(label, tenantDomain));
            URI location = new URI(RestApiConstants.RESOURCE_PATH_LABEL + "/" + labelDTO.getId());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LABELS, new Gson().toJson(labelDTO),
                    APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
            return Response.created(location).entity(labelDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            String errorMessage = "Error while adding new Label '" + body.getName() + "' - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
