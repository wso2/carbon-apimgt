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
package org.wso2.carbon.apimgt.lifecycle.manager.impl;

import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleHistoryBean;
import org.wso2.carbon.apimgt.lifecycle.manager.util.LifecycleOperationUtil;

import java.util.List;

/**
 * This class provides data related to life cycles
 */
public class LifecycleDataProvider {

    /**
     * Get current life cycle state object.
     *
     * @param uuid  UUID of the lifecycle state.
     * @return {@code LifecycleState} object represent current life cycle.
     * @throws LifecycleException
     */
    public static LifecycleState getCurrentLifecycleState(String uuid) throws LifecycleException {
        return LifecycleOperationUtil.getCurrentLifecycleState(uuid);
    }

    /**
     * This method provides set of operations performed to a particular lifecycle id.
     *
     * @param uuid  Lifecycle Id which requires history.
     * @return {@code List<LifecycleHistoryBean>} List of lifecycle history objects.
     * @throws LifecycleException
     */
    public static List<LifecycleHistoryBean> getLifecycleHistory(String uuid) throws LifecycleException {
        return LifecycleOperationUtil.getLifecycleHistoryFromId(uuid);
    }

    /**
     * This method provides set of lifecycle ids in a particular state.
     * @param state`
     * @return {@code List<LifecycleHistoryBean>} List of lifecycle ids in the given state.
     * @throws LifecycleException
     */
    public static List<String> getIdsFromState(String state) throws LifecycleException {
        return LifecycleOperationUtil.getLifecycleIds(state);
    }
}
