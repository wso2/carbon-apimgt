/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.Caching;

public class WorkflowExecutorFactory {

    private static final Log log = LogFactory.getLog(WorkflowExecutorFactory.class);

    private static WorkflowExecutorFactory instance;

    private WorkflowExecutorFactory() {
    }

    public static WorkflowExecutorFactory getInstance() {
        if (instance == null) {
            instance = new WorkflowExecutorFactory();
        }
        return instance;
    }

    public TenantWorkflowConfigHolder getWorkflowConfigurations() throws WorkflowException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String cacheName = tenantDomain + "_" + APIConstants.WORKFLOW_CACHE_NAME;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        //synchronized (cacheName.intern()){
        Cache workflowCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.WORKFLOW_CACHE_NAME);
        TenantWorkflowConfigHolder workflowConfig = (TenantWorkflowConfigHolder) workflowCache.get(cacheName);

        if (workflowConfig != null) {
            return workflowConfig;
        } else {
            TenantWorkflowConfigHolder configHolder = new TenantWorkflowConfigHolder(tenantDomain,tenantId);
            try {
                configHolder.load();
                workflowCache.put(cacheName, configHolder);
                return configHolder;
            } catch (WorkflowException e) {
                handleException("Error occurred while creating workflow configurations for tenant " + tenantDomain, e);
            }
        }
        // }

        return null;
    }

    private static void handleException(String msg, Exception e) throws WorkflowException {
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }

    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType) throws WorkflowException {
        TenantWorkflowConfigHolder holder = null;
        try {
            holder = this.getWorkflowConfigurations();
            if (holder != null) {
                return holder.getWorkflowExecutor(workflowExecutorType);
            }
        } catch (WorkflowException e) {
            handleException("Error while creating WorkFlowDTO for " + workflowExecutorType, e);
        }
        return null;
    }

    /**
     * Create a DTO object related to a given workflow type.
     * @param wfType Type of the workflow.
     */
    public WorkflowDTO createWorkflowDTO(String wfType) {

        WorkflowDTO workflowDTO = null;
        if (WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equals(wfType) ||
                WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equals(wfType)) {
            workflowDTO = new ApplicationWorkflowDTO();
            workflowDTO.setWorkflowType(wfType);
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION.equals(wfType)) {
            workflowDTO = new ApplicationRegistrationWorkflowDTO();
            ((ApplicationRegistrationWorkflowDTO) workflowDTO).setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
            workflowDTO.setWorkflowType(wfType);
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX.equals(wfType)) {
            workflowDTO = new ApplicationRegistrationWorkflowDTO();
            ((ApplicationRegistrationWorkflowDTO) workflowDTO).setKeyType(APIConstants.API_KEY_TYPE_SANDBOX);
            workflowDTO.setWorkflowType(wfType);
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(wfType) ||
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE.equals(wfType) ||
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION.equals(wfType)) {
            workflowDTO = new SubscriptionWorkflowDTO();
            workflowDTO.setWorkflowType(wfType);
        } else if (WorkflowConstants.WF_TYPE_AM_USER_SIGNUP.equals(wfType)) {
            workflowDTO = new WorkflowDTO();
            workflowDTO.setWorkflowType(wfType);
        } else if (WorkflowConstants.WF_TYPE_AM_API_STATE.equals(wfType) ||
                WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE.equals(wfType)) {
            workflowDTO = new APIStateWorkflowDTO();
            workflowDTO.setWorkflowType(wfType);
        }
        return workflowDTO;
    }
}
