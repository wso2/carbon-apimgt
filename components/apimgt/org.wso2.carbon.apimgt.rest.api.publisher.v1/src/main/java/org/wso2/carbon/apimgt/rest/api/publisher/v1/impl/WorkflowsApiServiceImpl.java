/*
 *
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.WorkflowMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class WorkflowsApiServiceImpl implements WorkflowsApiService {

    private static final Log log = LogFactory.getLog(WorkflowsApiService.class);
    private static final String createdStatus = "CREATED";

    /**
     * This is used to get the workflow pending request according to ExternalWorkflowReference
     *
     * @param externalWorkflowRef is the unique identifier for workflow request
     * @param messageContext      Message context of the request
     * @return Response
     */
    @Override
    public Response workflowsExternalWorkflowRefGet(String externalWorkflowRef, MessageContext messageContext)
            throws APIManagementException {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIAdmin apiAdmin = new APIAdminImpl();
            Workflow workflow = apiAdmin.getworkflowReferenceByExternalWorkflowReferenceID(externalWorkflowRef,
                    createdStatus, tenantDomain);
            return Response.ok().entity(WorkflowMappingUtil.fromWorkflowsToInfoDTO(workflow)).build();
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while retrieving workflow request by the " +
                    "external workflow reference.", ExceptionCodes.FAILED_TO_RETRIEVE_WORKFLOW_BY_EXTERNAL_REFERENCE_ID);
        }
    }

    /*
     * @param limit        maximum number of workflow returns
     * @param offset       starting index
     * @param accept       accept header value
     * @param workflowType is the the type of the workflow request.
     * (e.g: Application Creation, Application Subscription etc.)
     * @return
     */
    @Override
    public Response workflowsGet(Integer limit, Integer offset, String accept, String workflowType,
                                 MessageContext messageContext) throws APIManagementException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String query = "";
        Map<String, Object> result;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        int start = 0;
        List<String> nameVersionList = new ArrayList<>();
        APIAdmin apiAdmin = new APIAdminImpl();
        WorkflowListDTO workflowListDTO;
        Workflow[] workflows;
        try {
            workflows = apiAdmin.getworkflows(workflowType, createdStatus, tenantDomain);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while retrieving workflow requests. ",
                    ExceptionCodes.FAILED_TO_RETRIEVE_WORKFLOWS);
        }
        if (workflows.length == 0) {
            workflowListDTO = WorkflowMappingUtil.fromWorkflowsToDTO(workflows, limit, offset);
            WorkflowMappingUtil.setPaginationParams(workflowListDTO, limit, offset, workflows.length);
            return Response.ok().entity(workflowListDTO).build();
        }
        while (start >= 0) {
            result = apiProvider.searchPaginatedAPIs(query, tenantDomain, start, start + 100);
            Set<API> apis = (Set<API>) result.get("apis");
            for (API api : apis) {
                String organization = (RestApiUtil.getOrganization(messageContext) != null) ?
                        RestApiUtil.getOrganization(messageContext) : SUPER_TENANT_DOMAIN_NAME;
                String apiNameWithVersion = api.getId().getApiName() + ":" + api.getId().getVersion()
                        + ":" + organization;
                nameVersionList.add(apiNameWithVersion);
            }
            int length = Integer.parseInt(result.get("length").toString());
            if (length > start + 100) {
                start = start + 101;
            } else {
                start = -1;
            }
        }
        if (workflowType != null) {
            if (workflowType.equals("SUBSCRIPTION_CREATION")) {
                workflowType = WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
            } else if (workflowType.equals("SUBSCRIPTION_UPDATE")) {
                workflowType = WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE;
            }
        }
        if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(workflowType) ||
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE.equals(workflowType)) {
            List<Workflow> workflowList = new LinkedList<>();
            for (Workflow workflow : workflows) {
                String apiName = workflow.getProperties().get("apiName").toString();
                String apiVersion = workflow.getProperties().get("apiVersion").toString();
                String nameWithVersion = apiName + ":" + apiVersion + ":" + workflow.getTenantDomain();
                if (nameVersionList.contains(nameWithVersion)) {
                    workflowList.add(workflow);
                }
            }
            workflows = workflowList.toArray(new Workflow[workflowList.size()]);
            workflowListDTO = WorkflowMappingUtil.fromWorkflowsToDTO(workflows, limit, offset);
            WorkflowMappingUtil.setPaginationParams(workflowListDTO, limit, offset, workflows.length);
            return Response.ok().entity(workflowListDTO).build();
        } else {
            throw new APIManagementException("Invalid Workflow Type", ExceptionCodes.WORKFLOW_INVALID_WFTYPE);
        }
    }

    /**
     * This is used to update the workflow status
     *
     * @param workflowReferenceId workflow reference id that is unique to each workflow
     * @param body                body should contain the status, optionally can contain a
     *                            description and an attributes object
     * @return Response
     */
    @Override
    public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO body,
                                                      MessageContext messageContext) throws APIManagementException {
        boolean isTenantFlowStarted = false;
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomainOfUser = MultitenantUtils.getTenantDomain(username);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            if (workflowReferenceId == null) {
                RestApiUtil.handleBadRequest("workflowReferenceId is empty", log);
            }
            org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO = (org.wso2.carbon.apimgt.impl.dto.WorkflowDTO)
                    apiProvider.retrieveWorkflow(workflowReferenceId);
            if (workflowDTO == null) {
                throw new APIManagementException("Workflow not found for : " + workflowReferenceId,
                        ExceptionCodes.WORKFLOW_NOT_FOUND);
            }
            String tenantDomain = workflowDTO.getTenantDomain();
            if (tenantDomain != null && !tenantDomain.equals(tenantDomainOfUser)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (tenantDomain != null && !SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            if (body == null) {
                throw new APIManagementException("Request payload is missing", ExceptionCodes.WORKFLOW_PAYLOAD_MISSING);
            }
            if (body.getDescription() != null) {
                workflowDTO.setWorkflowDescription(body.getDescription());
            }
            if (body.getStatus() == null) {
                throw new APIManagementException("Workflow status not defined",
                        ExceptionCodes.WORKFLOW_STATUS_NOT_DEFINED);
            } else {
                workflowDTO.setStatus(WorkflowStatus.valueOf(body.getStatus().toString()));
            }
            if (body.getAttributes() != null) {
                workflowDTO.setAttributes(body.getAttributes());
            }
            String workflowType = workflowDTO.getWorkflowType();
            if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE.equals(workflowType) ||
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(workflowType)) {
                WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(
                        workflowType);
                workflowExecutor.complete(workflowDTO);
                if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                    WorkflowUtils.sendNotificationAfterWFComplete(workflowDTO, workflowType);
                }
                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.WORKFLOW_STATUS, new Gson().toJson(workflowDTO),
                        APIConstants.AuditLogConstants.UPDATED, RestApiCommonUtil.getLoggedInUsername());
                return Response.ok().entity(body).build();
            } else {
                throw new APIManagementException("Invalid Workflow Type", ExceptionCodes.WORKFLOW_INVALID_WFTYPE);
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while resuming workflow for " + workflowReferenceId, e);
        } catch (WorkflowException e) {
            throw new APIManagementException("Error while resuming workflow " + workflowReferenceId, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
