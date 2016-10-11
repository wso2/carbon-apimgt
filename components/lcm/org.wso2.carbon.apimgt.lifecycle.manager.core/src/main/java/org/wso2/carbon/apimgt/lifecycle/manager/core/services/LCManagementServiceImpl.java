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

import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LCUtils;

/**
 * Service class to perform CRUD operations related to lifecycle configurations
 */
public class LCManagementServiceImpl implements LCManagementService {

    /**
     * API to add new lifecycle configuration.
     *
     * @param lifecycleConfiguration                  Lifecycle configuration
     * @throws LifeCycleException
     */
    public void createLifecycle (String lifecycleConfiguration) throws LifeCycleException{
        LCUtils.addLifecycle(lifecycleConfiguration);
    }

    /**
     * Get the list of life cycles for a particular tenant.
     *
     * @return List of available life cycles.
     * @throws LifeCycleException
     */
    public String[] getLifecycleList() throws LifeCycleException{
        return LCUtils.getLifeCycleList();
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return                      Lifecycle configuration.
     * @throws LifeCycleException
     */
    public String getLifecycleConfiguration(String lcName) throws LifeCycleException{
        return LCUtils.getLifecycleConfiguration(lcName);
    }


}
