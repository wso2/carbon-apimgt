/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.lifecycle.manager.core;

import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LCConfigBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LCMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LCManagerDatabaseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class LCCrudManager {

    private int tenantId;

    public LCCrudManager(){
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    public void addLifecycle(String lcName, String lcContent) throws LifeCycleException{
        try {
            LCConfigBean lcConfigBean = new LCConfigBean();
            lcConfigBean.setLCName(lcName);
            lcConfigBean.setLCContent(lcContent);
            getLCMgtDAOInstance().addLifecycle(lcConfigBean, tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error in adding lifecycle with name "+ lcName, e);
        }
    }

    public String[] getLifecycleList() throws LifeCycleException{
        try {
            return getLCMgtDAOInstance().getLifecycleList(tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error while getting Lifecycle list for tenant "+ tenantId, e);
        }
    }

    public LCConfigBean getLifecycleConfiguration(String lcName) throws LifeCycleException{
        try {
            return getLCMgtDAOInstance().getLifecycleConfig(lcName, tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error while getting Lifecycle list for tenant "+ tenantId, e);
        }
    }

    private LCMgtDAO getLCMgtDAOInstance(){
        return LCMgtDAO.getInstance();
    }
}
