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
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils;
import org.wso2.carbon.apimgt.rest.api.admin.v1.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.WorkflowMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * This class is contains rest apis related to workflows
 */
public class WorkflowsApiServiceImpl implements WorkflowsApiService {

    private static final Log log = LogFactory.getLog(WorkflowsApiService.class);

    /**
     * This is used to get the workflow pending request according to ExternalWorkflowReference
     *
     * @param externalWorkflowRef is the unique identifier for workflow request
     * @return
     */
    @Override
    public Response workflowsExternalWorkflowRefGet(String externalWorkflowRef, MessageContext messageContext)
            throws APIManagementException {
        WorkflowInfoDTO workflowinfoDTO;
        try {
            Workflow workflow;
            String status = "CREATED";
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIAdmin apiAdmin = new APIAdminImpl();
            workflow = apiAdmin.getworkflowReferenceByExternalWorkflowReferenceID(externalWorkflowRef, status, tenantDomain);
            workflowinfoDTO = WorkflowMappingUtil.fromWorkflowsToInfoDTO(workflow);
            return Response.ok().entity(workflowinfoDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving workflow request by the " +
                    "external workflow reference. ", e, log);
        }
        return null;
    }

    /**
     * This is used to get the workflow pending requests
     *
     * @param limit        maximum number of workflow returns
     * @param offset       starting index
     * @param accept       accept header value
     * @param workflowType is the the type of the workflow request. (e.g: Application Creation, Application Subscription etc.)
     * @return
     */
    @Override
    public Response workflowsGet(Integer limit, Integer offset, String accept, String workflowType,
                                 MessageContext messageContext) throws APIManagementException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        WorkflowListDTO workflowListDTO;
        try {
            Workflow[] workflows;
            String status = "CREATED";
            APIAdmin apiAdmin = new APIAdminImpl();
            if(workflowType != null) {
                if (workflowType.equals("APPLICATION_CREATION")) {
                    workflowType = "AM_APPLICATION_CREATION";
                } else if (workflowType.equals("SUBSCRIPTION_CREATION")) {
                    workflowType = "AM_SUBSCRIPTION_CREATION";
                } else if (workflowType.equals("USER_SIGNUP")) {
                    workflowType = "AM_USER_SIGNUP";
                } else if (workflowType.equals("APPLICATION_REGISTRATION_PRODUCTION")) {
                    workflowType = "AM_APPLICATION_REGISTRATION_PRODUCTION";
                } else if (workflowType.equals("APPLICATION_REGISTRATION_SANDBOX")) {
                    workflowType = "AM_APPLICATION_REGISTRATION_SANDBOX";
                } else if (workflowType.equals("API_STATE")) {
                    workflowType = "AM_API_STATE";
                }
            }
            workflows = apiAdmin.getworkflows(workflowType, status, tenantDomain);
            workflowListDTO = WorkflowMappingUtil.fromWorkflowsToDTO(workflows, limit, offset);
            WorkflowMappingUtil.setPaginationParams(workflowListDTO, limit, offset,
                    workflows.length);
            return Response.ok().entity(workflowListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving workflow requests. ", e, log);
        }
        return null;
    }

    /**
     * This is used to update the workflow status
     *
     * @param workflowReferenceId workflow reference id that is unique to each workflow
     * @param body                body should contain the status, optionally can contain a
     *                            description and an attributes object
     * @return
     */
    @Override
    public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO body,
                                                      MessageContext messageContext) {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        boolean isTenantFlowStarted = false;
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomainOfUser = MultitenantUtils.getTenantDomain(username);
        try {
            if (workflowReferenceId == null) {
                RestApiUtil.handleBadRequest("workflowReferenceId is empty", log);
            }

            org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReferenceId);

            if (workflowDTO == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_WORKFLOW, workflowReferenceId, log);
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
                RestApiUtil.handleBadRequest("Request payload is missing", log);
            }

            if (body.getDescription() != null) {
                workflowDTO.setWorkflowDescription(body.getDescription());
            }

            if (body.getStatus() == null) {
                RestApiUtil.handleBadRequest("Workflow status is not defined", log);
            } else {
                workflowDTO.setStatus(WorkflowStatus.valueOf(body.getStatus().toString()));
            }

            if (body.getAttributes() != null) {
                workflowDTO.setAttributes(body.getAttributes());
            }

            String workflowType = workflowDTO.getWorkflowType();
            WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
            workflowExecutor.complete(workflowDTO);
            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                WorkflowUtils.sendNotificationAfterWFComplete(workflowDTO, workflowType);
            }
            return Response.ok().entity(body).build();

        } catch (APIManagementException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        } catch (WorkflowException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return null;
    }
}
