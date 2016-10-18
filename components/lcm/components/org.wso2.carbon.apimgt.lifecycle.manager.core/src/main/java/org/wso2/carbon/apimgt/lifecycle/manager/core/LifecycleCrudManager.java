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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class perform CRUD operations related to lifecycle configurations. Communicate with the DAO layer.
 */
public class LifecycleCrudManager {

    private static Map<String, String> lifecycleMap;

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
                getLCMgtDAOInstance().addLifecycle(lifecycleConfigBean);
                if (lifecycleMap != null) {
                    lifecycleMap.put(lcName, lcContent);

                } else {
                    lifecycleMap = new ConcurrentHashMap<>();
                    lifecycleMap.put(lcName, lcContent);
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
            getLCMgtDAOInstance().updateLifecycle(lifecycleConfigBean);
            if (lifecycleMap != null) {
                lifecycleMap.put(lcName, lcContent);
            } else {
                lifecycleMap = new ConcurrentHashMap<>();
                lifecycleMap.put(lcName, lcContent);

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
                getLCMgtDAOInstance().deleteLifecycle(lcName);
                if (lifecycleMap != null && lifecycleMap.containsKey(lcName)) {
                    lifecycleMap.remove(lcName);
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
     * Get the list of life cycles.
     *
     * @return List of available life cycles.
     * @throws LifecycleException
     */
    public String[] getLifecycleList() throws LifecycleException {
        try {
            if (lifecycleMap != null) {
                return lifecycleMap.keySet().toArray(new String[0]);
            }
            return getLCMgtDAOInstance().getLifecycleList();
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list. ", e);
        }
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return Bean containing lifecycle configuration.
     * @throws LifecycleException
     */
    public LifecycleConfigBean getLifecycleConfiguration(String lcName) throws LifecycleException {
        try {
            if (lifecycleMap != null && lifecycleMap.containsKey(lcName)) {
                LifecycleConfigBean lifecycleConfigBean = new LifecycleConfigBean();
                lifecycleConfigBean.setLcName(lcName);
                lifecycleConfigBean.setLcContent(lifecycleMap.get(lcName));
                return lifecycleConfigBean;
            }
            return getLCMgtDAOInstance().getLifecycleConfig(lcName);
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list.", e);
        }
    }

    /**
     * This method is used to initialize lifecycle map during server startup. All lifecycle configs are retrieved from
     * database and stored in a static tenant specific map.
     *
     * @throws LifecycleException
     */
    public void initLifecycleMap() throws LifecycleException {
        lifecycleMap = new ConcurrentHashMap<>();
        try {
            LifecycleConfigBean[] lifecycleConfigBeen = getLCMgtDAOInstance().getAllLifecycleConfigs();
            for (LifecycleConfigBean lifecycleConfigBean : lifecycleConfigBeen) {
                lifecycleMap.put(lifecycleConfigBean.getLcName(), lifecycleConfigBean.getLcContent());
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list for all tenants", e);
        }

    }

    private boolean checkLifecycleExist(String lcName) throws LifecycleManagerDatabaseException {
        if (lifecycleMap != null && lifecycleMap.containsKey(lcName)) {
            return true;
        }
        return getLCMgtDAOInstance().checkLifecycleExist(lcName);
    }

    private boolean checkLifecycleInUse(String lcName) throws LifecycleManagerDatabaseException {
        return getLCMgtDAOInstance().isLifecycleIsInUse(lcName);
    }

    private LifecycleMgtDAO getLCMgtDAOInstance() {
        return LifecycleMgtDAO.getInstance();
    }
}
