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
import org.wso2.carbon.apimgt.lifecycle.manager.core.LifecycleOperationManager;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleDataProvider;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleHistoryBean;

import java.util.List;

class APILifeCycleManagerImpl implements APILifecycleManager {
    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param targetState {@code String} Required target state.
     * @param uuid        {@code String} Lifecycle id that maps with the asset.
     * @param user        The user who invoked the action. This will be used for auditing
     *                    purposes.
     * @param resource    {@code Object} The current object to which lifecycle is attached to.
     * @return {@code LifecycleState} object of updated life cycle state.
     * @throws LifecycleException If exception occurred while execute life cycle state change.
     */
    @Override
    public LifecycleState executeLifecycleEvent(String currentState, String targetState, String uuid, String user,
                                                Object resource) throws LifecycleException {
        return LifecycleOperationManager.executeLifecycleEvent(currentState, targetState, uuid, user, resource);
    }

    /**
     * This method need to call for each check list item operation.
     *
     * @param uuid              Object that can use to uniquely identify resource.
     * @param currentState      The state which the checklist item is associated with.
     * @param checkListItemName Name of the check list item as specified in the lc config.
     * @param value             Value of the check list item. Either selected or not.
     * @throws LifecycleException If exception occurred while execute life cycle update.
     */
    @Override
    public LifecycleState checkListItemEvent(String uuid, String currentState, String checkListItemName, boolean
            value) throws LifecycleException {
        return LifecycleOperationManager.checkListItemEvent(uuid, currentState, checkListItemName, value);
    }

    /**
     * This method is used to associate a lifecycle with an asset.
     *
     * @param lcName LC name which associates with the resource.
     * @param user   The user who invoked the action. This will be used for auditing purposes.
     * @return Object of added life cycle state.
     * @throws LifecycleException If failed to associate life cycle with asset.
     */
    @Override
    public LifecycleState addLifecycle(String lcName, String user) throws LifecycleException {
        return LifecycleOperationManager.addLifecycle(lcName, user);
    }

    /**
     * This method is used to detach a lifecycle from an asset.
     *
     * @param uuid Lifecycle id that maps with the asset.
     * @throws LifecycleException If failed to associate life cycle with asset.
     */
    @Override
    public void removeLifecycle(String uuid) throws LifecycleException {
        LifecycleOperationManager.removeLifecycle(uuid);
    }

    /**
     * Get current life cycle state object.
     *
     * @param uuid
     * @return {@code LifecycleState} object represent current life cycle.
     */
    @Override
    public LifecycleState getCurrentLifecycleState(String uuid) throws LifecycleException {
        return LifecycleOperationManager.getCurrentLifecycleState(uuid);
    }

    /**
     * Get Current Lifecycle History for uuid
     *
     * @param uuid uuid of lifecycle instance
     * @return
     * @throws LifecycleException
     */
    @Override
    public List<LifecycleHistoryBean> getLifecycleHistory(String uuid) throws LifecycleException {
        return LifecycleDataProvider.getLifecycleHistory(uuid);
    }
}
