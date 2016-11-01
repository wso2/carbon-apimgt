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
package org.wso2.carbon.apimgt.lifecycle.manager;

import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleState;

/**
 * This is the base ManageLifecycle Interface. If users need to extend life cycle management
 * feature to any of the class they created they can implement this class and use the default implementation.
 */
public interface ManagedLifecycle {

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lcName                        Lc name which associates with the resource.
     * @param user                          The user who invoked the action. This will be used for auditing purposes.
     * @throws LifecycleException           If failed to get lifecycle list.
     */
    default LifecycleState associateLifecycle(String lcName, String user) throws LifecycleException {
        return LifecycleOperationProvider.associateLifecycle(lcName, user);
    }

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param targetState                       {@code String} Required target state of the lifecycle.
     * @param uuid                              {@code String} object that can use to uniquely identify resource.
     * @param user                              The user who invoked the action. This will be used for auditing
     *                                          purposes.
     * @param resource                          {@code Object} Current object to which lifecycle is associated.
     *
     * @throws LifecycleException               If exception occurred while execute life cycle update.
     */
    default LifecycleState executeLifecycleEvent(String targetState, String uuid, String user, Object resource)
            throws LifecycleException {
        return LifecycleOperationProvider.executeLifecycleEvent(targetState, uuid, user, resource);
    }

    /**
     * Remove the lifecycle from the asset instance.
     *
     * @param uuid {@code String} object that can use to uniquely identify asset.
     */
    default void dissociateLifecycle(String uuid) throws LifecycleException {
        LifecycleOperationProvider.dissociateLifecycle(uuid);
    }

    /**
     * This method need to call for each check list item operation.
     *
     * @param uuid                              {@code String} object that can use to uniquely identify resource.
     * @param currentState                      The state which the checklist item is associated with.
     * @param checkListItemName                 Name of the check list item as specified in the lc config.
     * @param value                             Value of the check list item. Either selected or not.
     *
     * @throws LifecycleException               If exception occurred while execute life cycle update.
     */
    default LifecycleState checkListItemEvent(String uuid, String currentState, String checkListItemName, boolean value)
            throws LifecycleException {
        return LifecycleOperationProvider.checkListItemEvent(uuid, currentState, checkListItemName, value);
    }
}
