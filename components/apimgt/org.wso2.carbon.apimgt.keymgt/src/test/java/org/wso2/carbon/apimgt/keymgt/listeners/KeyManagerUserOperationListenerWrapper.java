/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 *
 */

package org.wso2.carbon.apimgt.keymgt.listeners;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;

public class KeyManagerUserOperationListenerWrapper extends KeyManagerUserOperationListener {

    private ApiMgtDAO apiMgtDAO;
    private WorkflowExecutor workflowExecutor;
    private Tenant tenant;
    private APIManagerConfiguration config;
    private APIAuthenticationAdminClient apiAuthenticationAdminClient;
    private boolean isEnabled;

    public KeyManagerUserOperationListenerWrapper(ApiMgtDAO apiMgtDAO, WorkflowExecutor workflowExecutor, Tenant
            tenant, APIManagerConfiguration config) {
        this.apiMgtDAO = apiMgtDAO;
        this.workflowExecutor = workflowExecutor;
        this.tenant = tenant;
        this.config = config;
        this.isEnabled = true;
    }


    public KeyManagerUserOperationListenerWrapper(ApiMgtDAO apiMgtDAO, WorkflowExecutor workflowExecutor, Tenant
            tenant, APIManagerConfiguration config, APIAuthenticationAdminClient apiAuthenticationAdminClient,
                                                  boolean isEnabled) {
        this.apiMgtDAO = apiMgtDAO;
        this.workflowExecutor = workflowExecutor;
        this.tenant = tenant;
        this.config = config;
        this.apiAuthenticationAdminClient = apiAuthenticationAdminClient;
        this.isEnabled = isEnabled;
    }

    @Override
    protected ApiMgtDAO getDAOInstance() {
        return apiMgtDAO;
    }

    @Override
    protected Tenant getTenant(int tenantId) throws UserStoreException {
        return tenant;
    }

    @Override
    protected int getTenantId() {
        return tenant != null ? tenant.getId() : -1234;
    }

    @Override
    protected String getTenantDomain() {
        return tenant != null ? tenant.getDomain() : "carbon.super";
    }

    @Override
    protected WorkflowExecutor getWorkflowExecutor(String workflowType) throws WorkflowException {
        return workflowExecutor;
    }

    @Override
    protected String getUserStoreDomainName(UserStoreManager userStoreManager) {
        return UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
    }

    @Override
    protected boolean isUserStoreInUsernameCaseSensitive(String username) {
        return false;
    }

    @Override
    protected APIManagerConfiguration getApiManagerConfiguration() {
       return config;
    }

    @Override
    protected APIAuthenticationAdminClient getApiAuthenticationAdminClient(Environment environment) throws AxisFault {
        return apiAuthenticationAdminClient;
    }

    @Override
    public boolean isEnable() {
        return isEnabled;
    }
}
