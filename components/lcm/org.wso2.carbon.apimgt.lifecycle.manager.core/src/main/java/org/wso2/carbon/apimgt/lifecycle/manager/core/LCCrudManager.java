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

import java.util.HashMap;
import java.util.Map;

/**
 * This class perform CRUD operations related to lifecycle configurations. Communicate with the DAO layer.
 */
public class LCCrudManager {

    private int tenantId;
    private static Map<Integer, Map<String, String>> tenantLifecycleMap;

    public LCCrudManager(){
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Add new lifecycle configuration.
     *
     * @param lcName                     Name of lifecycle which asset being associated with.
     * @param lcContent                  Lifecycle configuration
     * @throws LifeCycleException
     */
    public void addLifecycle(String lcName, String lcContent) throws LifeCycleException{
        try {
            if(!checkLifecycleExist(lcName)) {
                if (tenantLifecycleMap != null) {
                    Map<String, String> lifecycleMaps = tenantLifecycleMap.get(tenantId);
                    if (lifecycleMaps != null) {
                        lifecycleMaps.put(lcName, lcContent);
                        tenantLifecycleMap.put(tenantId, lifecycleMaps);
                    } else {
                        Map<String, String> tempLifecycleMap = new HashMap<>();
                        tempLifecycleMap.put(lcName, lcContent);
                        tenantLifecycleMap.put(tenantId, tempLifecycleMap);
                    }
                } else {
                    tenantLifecycleMap = new HashMap<>();
                    Map<String, String> tempLifecycleMap = new HashMap<>();
                    tempLifecycleMap.put(lcName, lcContent);
                    tenantLifecycleMap.put(tenantId, tempLifecycleMap);
                }
                LCConfigBean lcConfigBean = new LCConfigBean();
                lcConfigBean.setLCName(lcName);
                lcConfigBean.setLCContent(lcContent);
                getLCMgtDAOInstance().addLifecycle(lcConfigBean, tenantId);
            }
            else {
                throw new LifeCycleException("Lifecycle already exist with name "+ lcName);
            }
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error in adding lifecycle with name "+ lcName, e);
        }
    }

    /**
     * Get the list of life cycles for a particular tenant.
     *
     * @return List of available life cycles.
     * @throws LifeCycleException
     */
    public String[] getLifecycleList() throws LifeCycleException{
        try {
            if(tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId)){
                return tenantLifecycleMap.get(tenantId).keySet().toArray(new String[0]);
            }
            return getLCMgtDAOInstance().getLifecycleList(tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error while getting Lifecycle list for tenant "+ tenantId, e);
        }
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return                      Bean containing lifecycle configuration.
     * @throws LifeCycleException
     */
    public LCConfigBean getLifecycleConfiguration(String lcName) throws LifeCycleException{
        try {
            if(tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId) && tenantLifecycleMap.get
                    (tenantId)
                    .containsKey(lcName)){
                LCConfigBean lcConfigBean = new LCConfigBean();
                lcConfigBean.setLCName(lcName);
                lcConfigBean.setLCContent(tenantLifecycleMap.get(tenantId).get(lcName));
                return lcConfigBean;
            }
            return getLCMgtDAOInstance().getLifecycleConfig(lcName, tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error while getting Lifecycle list for tenant "+ tenantId, e);
        }
    }

    public void initLifeCycleMap() throws LifeCycleException{
        tenantLifecycleMap = new HashMap<>();
        try {
            LCConfigBean[] lcConfigBeans = getLCMgtDAOInstance().getAllLifecycleConfigs();
            for(LCConfigBean lcConfigBean : lcConfigBeans) {
                Map<String, String> lifecycleMaps = tenantLifecycleMap.get(lcConfigBean.getTenantId());
                if (lifecycleMaps != null) {
                    lifecycleMaps.put(lcConfigBean.getLCName(), lcConfigBean.getLCContent());
                    tenantLifecycleMap.put(lcConfigBean.getTenantId(), lifecycleMaps);
                } else {
                    Map<String, String> tempLifecycleMap = new HashMap<>();
                    tempLifecycleMap.put(lcConfigBean.getLCName(), lcConfigBean.getLCContent());
                    tenantLifecycleMap.put(lcConfigBean.getTenantId(), tempLifecycleMap);
                }
            }
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleException("Error while getting Lifecycle list for all tenants", e);
        }

    }

    private boolean checkLifecycleExist(String lcName) throws LCManagerDatabaseException{
        if(tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId) && tenantLifecycleMap.get
                (tenantId)
                .containsKey(lcName)){
            return true;
        }
        return getLCMgtDAOInstance().checkLifecycleExist(lcName, tenantId);
    }

    private LCMgtDAO getLCMgtDAOInstance(){
        return LCMgtDAO.getInstance();
    }
}
