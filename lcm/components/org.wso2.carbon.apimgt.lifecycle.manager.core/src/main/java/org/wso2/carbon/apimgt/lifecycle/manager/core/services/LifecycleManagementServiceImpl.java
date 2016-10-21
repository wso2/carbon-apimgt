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

package org.wso2.carbon.apimgt.lifecycle.manager.core.services;

import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;

/**
 * Service class to perform CRUD operations related to lifecycle configurations
 */
public class LifecycleManagementServiceImpl implements LifecycleManagementService {

    /**
     * API to add new lifecycle configuration.
     *
     * @param lifecycleConfiguration                  Lifecycle configuration
     * @throws LifecycleException
     */
    public void createLifecycle (String lifecycleConfiguration) throws LifecycleException {
        LifecycleUtils.addLifecycle(lifecycleConfiguration);
    }

    /**
     * API to  update existing lifecycle configuration.
     *
     * @param oldName                       Name of the existing lifecycle.
     * @param lifecycleConfiguration        Lifecycle configuration
     * @throws LifecycleException
     */
    public void updateLifecycle (String oldName, String lifecycleConfiguration) throws LifecycleException {
        LifecycleUtils.updateLifecycle(oldName, lifecycleConfiguration);
    }

    /**
     * API to delete exiting lifecycle.
     *
     * @param lcName                  Lifecycle to be deleted.
     * @throws LifecycleException
     */
    public void deleteLifecycle (String lcName) throws LifecycleException {
        LifecycleUtils.deleteLifecycle(lcName);
    }

    /**
     * Get the list of life cycles for a particular tenant.
     *
     * @return List of available life cycles.
     * @throws LifecycleException
     */
    public String[] getLifecycleList() throws LifecycleException {
        return LifecycleUtils.getLifecycleList();
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return                      Lifecycle configuration.
     * @throws LifecycleException
     */
    public String getLifecycleConfiguration(String lcName) throws LifecycleException {
        return LifecycleUtils.getLifecycleConfiguration(lcName);
    }


}
