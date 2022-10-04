/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.mappings.WorkflowMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class WorkflowsCommonImpl {
    private WorkflowsCommonImpl() {
    }

    /**
     * Get the workflow pending request according to ExternalWorkflowReference
     *
     * @param externalWorkflowRef Workflow reference
     * @return Workflow info DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static WorkflowInfoDTO getWorkflowByExternalRef(String externalWorkflowRef) throws APIManagementException {
        Workflow workflow;
        String status = "CREATED";
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        workflow = apiAdmin
                .getworkflowReferenceByExternalWorkflowReferenceID(externalWorkflowRef, status, tenantDomain);
        return WorkflowMappingUtil.fromWorkflowsToInfoDTO(workflow);
    }

    /**
     * @param limit        maximum number of workflow returns
     * @param offset       starting index
     * @param workflowType is the type of the workflow request. (e.g: Application Creation, Application Subscription etc.)
     * @return Pending workflow list
     * @throws APIManagementException When an internal error occurs
     */
    public static WorkflowListDTO getAllPendingWorkflows(Integer limit, Integer offset, String workflowType)
            throws APIManagementException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        WorkflowListDTO workflowListDTO;

        Workflow[] workflows;
        String status = "CREATED";
        APIAdmin apiAdmin = new APIAdminImpl();
        if (workflowType != null) {
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
            } else if (workflowType.equals("API_PRODUCT_STATE")) {
                workflowType = "AM_API_PRODUCT_STATE";
            }
        }

        workflows = apiAdmin.getworkflows(workflowType, status, tenantDomain);
        workflowListDTO = WorkflowMappingUtil.fromWorkflowsToDTO(workflows, limit, offset);
        WorkflowMappingUtil.setPaginationParams(workflowListDTO, limit, offset,
                workflows.length);
        return workflowListDTO;
    }

    /**
     * This is used to update the workflow status
     *
     * @param workflowReferenceId Workflow reference id that is unique to each workflow
     * @param body                Body should contain the status, optionally can contain a
     *                            description and an attributes object
     * @throws APIManagementException When an internal error occurs
     */
    public static void updateWorkflowStatus(String workflowReferenceId, WorkflowDTO body)
            throws APIManagementException {
        boolean isTenantFlowStarted = false;
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomainOfUser = MultitenantUtils.getTenantDomain(username);

        if (workflowReferenceId == null) {
            throw new APIManagementException("workflowReferenceId is empty", ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        try {
            org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReferenceId);

            if (workflowDTO == null) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                        RestApiConstants.RESOURCE_WORKFLOW, workflowReferenceId));
            }

            String tenantDomain = workflowDTO.getTenantDomain();
            if (tenantDomain != null && !tenantDomain.equals(tenantDomainOfUser)) {
                throw new APIManagementException(ExceptionCodes.UNAUTHORIZED);
            }
            if (tenantDomain != null && !SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            if (body == null) {
                throw new APIManagementException("Request payload is missing", ExceptionCodes.PARAMETER_NOT_PROVIDED);
            }

            if (body.getDescription() != null) {
                workflowDTO.setWorkflowDescription(body.getDescription());
            }

            workflowDTO.setStatus(WorkflowStatus.valueOf(body.getStatus().toString()));

            if (body.getAttributes() != null) {
                workflowDTO.setAttributes(body.getAttributes());
            }

            String workflowType = workflowDTO.getWorkflowType();

            if (WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equals(workflowType) &&
                    WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
                int applicationId = Integer.parseInt(workflowDTO.getWorkflowReference());
                apiConsumer.cleanupPendingTasksForApplicationDeletion(applicationId);
            }

            WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
            workflowExecutor.complete(workflowDTO);
            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                WorkflowUtils.sendNotificationAfterWFComplete(workflowDTO, workflowType);
            }
        } catch (WorkflowException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            throw new APIManagementException(msg, ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, msg));
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
