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

import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

/**
 * This is the base ManageLifecycle Interface. If users need to extend life cycle management
 * feature to any of the class they created they can implement this class and use the default implementation.
 */

public interface ManagedLifecycle {

    /**
     * This method is used to attach a lifecycle to any eternal object (Ex: API)
     *
     * @param lcName                        Lc name which associates with the resource.
     * @param user                          The user who invoked the action. This will be used for auditing purposes.
     * @throws LifecycleException           If failed to get lifecycle list.
     */
    default void createLifecycleEntry(String lcName, String user) throws LifecycleException {
        associateLifecycle(LifecycleOperationManager.associateLifecycle(lcName, user));
    }

    /**
     * This method need to call for each life cycle state change.
     *
     * @param targetState                       Required target state of the lifecycle.
     * @param user                              The user who invoked the action. This will be used for auditing
     *                                          purposes.
     * @param lcName                            Lc name which associates with the resource.
     *
     * @throws LifecycleException               If exception occurred while execute life cycle update.
     */
    default LifecycleState executeLifecycleEvent(String targetState, String user, String lcName)
            throws LifecycleException {
        return LifecycleOperationManager
                .executeLifecycleEvent(targetState, getCurrentLifecycleState(lcName).getLifecycleId(), user, this);
    }

    /**
     * Remove the lifecycle from the asset instance.
     *
     * @param lcName                            Lc name which associates with the resource.
     */
    default void removeLifecycleEntry(String lcName) throws LifecycleException {
        LifecycleOperationManager.dissociateLifecycle(getCurrentLifecycleState(lcName).getLifecycleId());
        dissociateLifecycle();
    }

    /**
     * This method need to call for each check list item operation.
     *
     * @param lcName                            Lc name which associates with the resource.
     * @param checkListItemName                 Name of the check list item as specified in the lc config.
     * @param value                             Value of the check list item. Either selected or not.
     *
     * @throws LifecycleException               If exception occurred while execute life cycle update.
     */
    default LifecycleState checkListItemEvent(String lcName, String checkListItemName, boolean value)
            throws LifecycleException {
        return LifecycleOperationManager.checkListItemEvent(getCurrentLifecycleState(lcName).getLifecycleId(),
                getCurrentLifecycleState(lcName).getState(), checkListItemName, value);
    }

    /**
     * This method should be implemented to create association between object which implementing Managed Lifecycle and
     * the Lifecycle framework. This method should implement logic which saves the returned uuid in the external
     * party (API, APP etc). So both parties will have lifecycle uuid saved in their side which will cater the
     * purpose of mapping.
     *
     * @param lifecycleState                    Lifecycle state object.
     *
     */
    void associateLifecycle(LifecycleState lifecycleState) throws LifecycleException;

    /**
     * This method should be implemented to remove the lifecycle data from the object which implements this interface.
     * Persisted lifecycle state id (say stored in database) should be removed by implementing this method.
     *
     */
    void dissociateLifecycle() throws LifecycleException;

    /**
     * This method is used to provide lifecycle state data object for a particular life cycle. "getLifecycleId"
     * method should provide proper implementation in order to resolve lifecycle uuid using lifecycle name.
     *
     * @param lcName                    Name of the lifecycle.
     *
     */
    default LifecycleState getCurrentLifecycleState(String lcName) throws LifecycleException {
        return LifecycleOperationManager.getCurrentLifecycleState(getLifecycleId(lcName));
    }

    /**
     * This method should provide lifecycle uuid when lifecycle name is given. Object which implements this method
     * should maintain a mapping between lifecycle name and uuid. This is important for multiple lifecycle as well.
     *
     * @param lcName                    Name of the lifecycle.
     *
     */
    String getLifecycleId(String lcName);

}
