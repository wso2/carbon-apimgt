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
package org.wso2.carbon.apimgt.lifecycle.manager.interfaces;

import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleState;

/**
 * This is the base ManageLifecycle Interface. If users need to extend life cycle management
 * feature to any of the class they created they can extend this class and implement required
 * methods.
 */
public interface ManagedLifecycle {

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lcName                        Lc name which associates with the resource.
     * @param user                          The user who invoked the action. This will be used for auditing purposes.
     * @return                              Object of added life cycle state.
     * @throws LifecycleException           If failed to get lifecycle list.
     */
    LifecycleState associateLifecycle(String lcName, String user) throws LifecycleException;

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param action                            {@code String} lifecycle action.
     * @param nextState                         {@code LifecycleState} object represent next life cycle state.
     * @param user                              The user who invoked the action. This will be used for auditing
     *                                          purposes.
     * @return                                  {@code LifecycleState} object of updated life cycle state.
     * @throws LifecycleException               If exception occurred while execute life cycle update.
     */
    LifecycleState executeLifecycleEvent(LifecycleState nextState, String action, String user, Object resource)
            throws LifecycleException;

    /**
     * Get current life cycle state object.
     *
     * @return {@code LifecycleState} object represent current life cycle.
     */
    LifecycleState getCurrentLifecycleState();

    /**
     * This method will be used to set lifecycle ID_ATTRIBUTE of current object.
     *
     * @param lifecycleID {@code String} object that can use to uniquely identify resource.
     */
    void setLifecycleID(String lifecycleID) throws LifecycleException;
}
