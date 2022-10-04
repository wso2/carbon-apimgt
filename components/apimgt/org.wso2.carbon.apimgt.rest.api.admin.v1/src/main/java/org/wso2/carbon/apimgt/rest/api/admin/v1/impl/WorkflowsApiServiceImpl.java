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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.WorkflowsCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowListDTO;

import javax.ws.rs.core.Response;

/**
 * This class is contains rest apis related to workflows
 */
public class WorkflowsApiServiceImpl implements WorkflowsApiService {

    /**
     * This is used to get the workflow pending request according to ExternalWorkflowReference
     *
     * @param externalWorkflowRef is the unique identifier for workflow request
     * @return Response
     */
    @Override
    public Response getWorkflowByExternalRef(String externalWorkflowRef, MessageContext messageContext)
            throws APIManagementException {
        WorkflowInfoDTO workflowinfoDTO = WorkflowsCommonImpl.getWorkflowByExternalRef(externalWorkflowRef);
        return Response.ok().entity(workflowinfoDTO).build();
    }

    /**
     * This is used to get the workflow pending requests
     *
     * @param limit        maximum number of workflow returns
     * @param offset       starting index
     * @param accept       accept header value
     * @param workflowType is the type of the workflow request. (e.g: Application Creation, Application Subscription etc.)
     * @return Response
     */
    @Override
    public Response getAllPendingWorkflows(Integer limit, Integer offset, String accept, String workflowType,
                                           MessageContext messageContext) throws APIManagementException {
        WorkflowListDTO workflowListDTO = WorkflowsCommonImpl.getAllPendingWorkflows(limit, offset, workflowType);
        return Response.ok().entity(workflowListDTO).build();
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
    public Response updateWorkflowStatus(String workflowReferenceId, WorkflowDTO body,
                                         MessageContext messageContext) throws APIManagementException {
        WorkflowsCommonImpl.updateWorkflowStatus(workflowReferenceId, body);
        return Response.ok().entity(body).build();
    }
}
