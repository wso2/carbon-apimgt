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

import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleConfigBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LifecycleMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LifecycleManagerDatabaseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class perform CRUD operations related to lifecycle configurations. Communicate with the DAO layer.
 */
public class LifecycleCrudManager {

    private static Map<Integer, Map<String, String>> tenantLifecycleMap;
    private int tenantId;

    public LifecycleCrudManager() {
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Add new lifecycle configuration.
     *
     * @param lcName                     Name of lifecycle.
     * @param lcContent                  Lifecycle configuration
     * @throws LifecycleException
     */
    public void addLifecycle(String lcName, String lcContent) throws LifecycleException {
        try {
            if (!checkLifecycleExist(lcName)) {
                LifecycleConfigBean lifecycleConfigBean = new LifecycleConfigBean();
                lifecycleConfigBean.setLcName(lcName);
                lifecycleConfigBean.setLcContent(lcContent);
                getLCMgtDAOInstance().addLifecycle(lifecycleConfigBean, tenantId);
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
                    tenantLifecycleMap = new ConcurrentHashMap<>();
                    Map<String, String> tempLifecycleMap = new HashMap<>();
                    tempLifecycleMap.put(lcName, lcContent);
                    tenantLifecycleMap.put(tenantId, tempLifecycleMap);
                }

            } else {
                throw new LifecycleException("Lifecycle already exist with name " + lcName);
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error in adding lifecycle with name " + lcName, e);
        }
    }

    /**
     * Update existing lifecycle configuration.
     *
     * @param lcName                     Name of lifecycle .
     * @param lcContent                  Lifecycle configuration
     * @throws LifecycleException
     */
    public void updateLifecycle(String lcName, String lcContent) throws LifecycleException {
        try {
            LifecycleConfigBean lifecycleConfigBean = new LifecycleConfigBean();
            lifecycleConfigBean.setLcName(lcName);
            lifecycleConfigBean.setLcContent(lcContent);
            getLCMgtDAOInstance().updateLifecycle(lifecycleConfigBean, tenantId);
            if (tenantLifecycleMap != null) {
                Map<String, String> lifecycleMaps = tenantLifecycleMap.get(tenantId);
                if (lifecycleMaps != null) {
                    lifecycleMaps.put(lcName, lcContent);
                } else {
                    lifecycleMaps = new HashMap<>();
                    lifecycleMaps.put(lcName, lcContent);
                }
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error in adding lifecycle with name " + lcName, e);
        }
    }

    /**
     * Delete the given lifecycle. Deletion fails if the lifecycle is already associated with assets.
     *
     * @param lcName                Lifecycle to be deleted..
     * @throws LifecycleException
     */
    public void deleteLifecycle(String lcName) throws LifecycleException {
        try {
            if (!checkLifecycleInUse(lcName)) {
                getLCMgtDAOInstance().deleteLifecycle(lcName, tenantId);
                if (tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId)) {
                    tenantLifecycleMap.get(tenantId).remove(lcName);
                }
            } else {
                throw new LifecycleException(
                        lcName + " is associated with assets. Delete operation can not be " + "allowed");
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error in deleting lifecycle with name " + lcName, e);
        }
    }

    /**
     * Get the list of life cycles for a particular tenant.
     *
     * @return List of available life cycles.
     * @throws LifecycleException
     */
    public String[] getLifecycleList() throws LifecycleException {
        try {
            if (tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId)) {
                return tenantLifecycleMap.get(tenantId).keySet().toArray(new String[0]);
            }
            return getLCMgtDAOInstance().getLifecycleList(tenantId);
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list for tenant " + tenantId, e);
        }
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return                      Bean containing lifecycle configuration.
     * @throws LifecycleException
     */
    public LifecycleConfigBean getLifecycleConfiguration(String lcName) throws LifecycleException {
        try {
            if (tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId) && tenantLifecycleMap
                    .get(tenantId).containsKey(lcName)) {
                LifecycleConfigBean lifecycleConfigBean = new LifecycleConfigBean();
                lifecycleConfigBean.setLcName(lcName);
                lifecycleConfigBean.setLcContent(tenantLifecycleMap.get(tenantId).get(lcName));
                return lifecycleConfigBean;
            }
            return getLCMgtDAOInstance().getLifecycleConfig(lcName, tenantId);
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list for tenant " + tenantId, e);
        }
    }

    /**
     * This method is used to initialize lifecycle map during server startup. All lifecycle configs are retrieved from
     * database and stored in a static tenant specific map.
     *
     * @throws LifecycleException
     */
    public void initLifecycleMap() throws LifecycleException {
        tenantLifecycleMap = new ConcurrentHashMap<>();
        try {
            LifecycleConfigBean[] lifecycleConfigBeen = getLCMgtDAOInstance().getAllLifecycleConfigs();
            for (LifecycleConfigBean lifecycleConfigBean : lifecycleConfigBeen) {
                Map<String, String> lifecycleMaps = tenantLifecycleMap.get(lifecycleConfigBean.getTenantId());
                if (lifecycleMaps != null) {
                    lifecycleMaps.put(lifecycleConfigBean.getLcName(), lifecycleConfigBean.getLcContent());
                    tenantLifecycleMap.put(lifecycleConfigBean.getTenantId(), lifecycleMaps);
                } else {
                    Map<String, String> tempLifecycleMap = new HashMap<>();
                    tempLifecycleMap.put(lifecycleConfigBean.getLcName(), lifecycleConfigBean.getLcContent());
                    tenantLifecycleMap.put(lifecycleConfigBean.getTenantId(), tempLifecycleMap);
                }
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list for all tenants", e);
        }

    }

    private boolean checkLifecycleExist(String lcName) throws LifecycleManagerDatabaseException {
        if (tenantLifecycleMap != null && tenantLifecycleMap.containsKey(tenantId) && tenantLifecycleMap.get(tenantId)
                .containsKey(lcName)) {
            return true;
        }
        return getLCMgtDAOInstance().checkLifecycleExist(lcName, tenantId);
    }

    private boolean checkLifecycleInUse(String lcName) throws LifecycleManagerDatabaseException {
        return getLCMgtDAOInstance().isLifecycleIsInUse(lcName, tenantId);
    }

    private LifecycleMgtDAO getLCMgtDAOInstance() {
        return LifecycleMgtDAO.getInstance();
    }
}
