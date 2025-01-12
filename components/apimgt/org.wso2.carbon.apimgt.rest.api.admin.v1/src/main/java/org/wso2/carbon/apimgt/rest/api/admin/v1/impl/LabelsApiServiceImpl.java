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

import javax.ws.rs.core.Response;


public class LabelsApiServiceImpl implements LabelsApiService {
    private static final Log log = LogFactory.getLog(LabelsApiServiceImpl.class);

    public Response getAllLabels(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
        List<Label> labelList = apiAdmin.getAllLabelsOfTenant(tenantDomain);
        LabelListDTO labelListDTO =
                LabelMappingUtil.fromLabelListToLabelListDTO(labelList);
        return Response.ok().entity(labelListDTO).build();
    }

    public Response deleteLabel(String labelId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
        apiAdmin.deleteLabel(labelId, tenantDomain);
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LABELS, labelId,
                APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
        return Response.ok().build();
    }

    public Response updateLabel(String labelId, LabelDTO body, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
        Label labelToUpdate = LabelMappingUtil.fromLabelDTOToLabel(body);
        Label updatedLabel = apiAdmin.updateLabel(labelId, labelToUpdate ,tenantDomain);
        LabelDTO updatedLabelDTO = LabelMappingUtil.fromLabelToLabelDTO(updatedLabel);
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LABELS, new Gson().toJson(updatedLabelDTO),
                APIConstants.AuditLogConstants.UPDATED, RestApiCommonUtil.getLoggedInUsername());
        return Response.ok().entity(updatedLabelDTO).build();
    }

    public Response getLabelUsage(String labelId, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
        List<ApiResult> apiList = apiAdmin.getMappedApisForLabel(labelId, tenantDomain);
        LabelUsageDTO labelusageDTO = LabelsUsageMappingUtil.fromApiResultListToLabelUsageDTO(apiList);
        return Response.ok().entity(labelusageDTO).build();
    }

    public Response createLabel(LabelDTO body, MessageContext messageContext) throws APIManagementException {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String tenantDomain = RestApiUtil.getValidatedOrganization(messageContext);
            Label label = LabelMappingUtil.fromLabelDTOToLabel(body);
            LabelDTO labelDTO = LabelMappingUtil.
                    fromLabelToLabelDTO(apiAdmin.addLabel(label, tenantDomain));
            URI location = new URI(RestApiConstants.RESOURCE_PATH_LABEL + "/" + labelDTO.getId());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LABELS, new Gson().toJson(labelDTO),
                    APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
            return Response.created(location).entity(labelDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding new Label '" + body.getName() + "' - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
