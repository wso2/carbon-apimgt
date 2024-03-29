/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.test.TestTenantManager;
import org.wso2.carbon.apimgt.impl.workflow.SampleWorkFlowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class APIConsumerImplWrapper extends APIConsumerImpl {

    private final Log log = LogFactory.getLog(APIConsumerImplWrapper.class);

    public APIConsumerImplWrapper() throws APIManagementException {
    }

    public APIConsumerImplWrapper(ApiMgtDAO apiMgtDAO) throws APIManagementException {
        this(apiMgtDAO, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    public APIConsumerImplWrapper(ApiMgtDAO apiMgtDAO, String organization) throws APIManagementException {
        this.apiMgtDAO = apiMgtDAO;
        this.tenantDomain = organization;
        this.organization = organization;
    }

    public APIConsumerImplWrapper(ApiMgtDAO apiMgtDAO, APIPersistence apiPersistenceInstance)
            throws APIManagementException {
        this.apiMgtDAO = apiMgtDAO;
        this.apiPersistenceInstance = apiPersistenceInstance;
    }
    /**
     * Returns API manager configurations.
     *
     * @return APIManagerConfiguration object
     */
    protected APIManagerConfiguration getAPIManagerConfiguration() {

        return new APIManagerConfiguration();
    }

    protected WorkflowExecutor getWorkflowExecutor(String workflowType) throws WorkflowException {
        return new SampleWorkFlowExecutor();
    }


    @Override
    protected TenantManager getTenantManager() {
        return new TestTenantManager();
    }

    protected boolean startTenantFlowForTenantDomain(String tenantDomain) {
        return true;
    }

    protected void endTenantFlow(){
    }

    protected  int getTenantId(String requestedTenantDomain) throws UserStoreException {
        return -1234;
    }

    protected void setUsernameToThreadLocalCarbonContext(String username) {
    }

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        API api = new API(identifier);
        if("published_api".equals(identifier.getApiName())) {
            api.setStatus(APIConstants.PUBLISHED);
        } else {
            api.setStatus(APIConstants.CREATED);
        }
        return  api;
    }
}
