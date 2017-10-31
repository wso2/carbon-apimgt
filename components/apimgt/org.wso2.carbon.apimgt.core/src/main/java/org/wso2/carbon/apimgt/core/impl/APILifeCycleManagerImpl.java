/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.lcm.core.LifecycleOperationManager;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleDataProvider;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;

import java.util.List;

class APILifeCycleManagerImpl implements APILifecycleManager {

    /**
     * @see APILifecycleManager#executeLifecycleEvent(String currentState, String targetState, String uuid, String user,
            Object resource)
     */
    @Override
    public LifecycleState executeLifecycleEvent(String currentState, String targetState, String uuid, String user,
                                                Object resource) throws LifecycleException {
        return LifecycleOperationManager.executeLifecycleEvent(currentState, targetState, uuid, user, resource);
    }

    /**
     * @see APILifecycleManager#checkListItemEvent(String uuid, String currentState, String checkListItemName, boolean
            value)
     */
    @Override
    public LifecycleState checkListItemEvent(String uuid, String currentState, String checkListItemName, boolean
            value) throws LifecycleException {
        return LifecycleOperationManager.checkListItemEvent(uuid, currentState, checkListItemName, value);
    }

    /**
     * @see APILifecycleManager#addLifecycle(String lcName, String user)
     */
    @Override
    public LifecycleState addLifecycle(String lcName, String user) throws LifecycleException {
        return LifecycleOperationManager.addLifecycle(lcName, user);
    }

    /**
     * @see APILifecycleManager#removeLifecycle(String uuid)
     */
    @Override
    public void removeLifecycle(String uuid) throws LifecycleException {
        LifecycleOperationManager.removeLifecycle(uuid);
    }

    /**
     * @see APILifecycleManager#getCurrentLifecycleState(String uuid)
     */
    @Override
    public LifecycleState getCurrentLifecycleState(String uuid) throws LifecycleException {
        return LifecycleOperationManager.getCurrentLifecycleState(uuid);
    }

    /**
     * @see APILifecycleManager#getLifecycleDataForState(String uuid, String lcState)
     */
    @Override
    public LifecycleState getLifecycleDataForState(String uuid, String lcState) throws LifecycleException {
        return LifecycleOperationManager.getLifecycleDataForState(uuid, lcState);
    }

    /**
     * @see APILifecycleManager#getLifecycleHistory(String uuid)
     */
    @Override
    public List<LifecycleHistoryBean> getLifecycleHistory(String uuid) throws LifecycleException {
        return LifecycleDataProvider.getLifecycleHistory(uuid);
    }
}
