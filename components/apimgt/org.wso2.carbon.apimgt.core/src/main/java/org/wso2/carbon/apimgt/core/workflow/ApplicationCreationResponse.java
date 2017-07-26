/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.workflow;

import org.wso2.carbon.apimgt.core.api.WorkflowResponse;

/**
 * The Application Response, that can be used to include attributes relating to a application creation request.
 */
public class ApplicationCreationResponse {

    private String applicationUUID = null;
    private WorkflowResponse workflowResponse = null;

    public ApplicationCreationResponse(String addedSubscriptionUUID, WorkflowResponse workflowResponse) {

        this.applicationUUID = addedSubscriptionUUID;
        this.workflowResponse = workflowResponse;
    }

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public WorkflowResponse getWorkflowResponse() {
        return workflowResponse;
    }

}
