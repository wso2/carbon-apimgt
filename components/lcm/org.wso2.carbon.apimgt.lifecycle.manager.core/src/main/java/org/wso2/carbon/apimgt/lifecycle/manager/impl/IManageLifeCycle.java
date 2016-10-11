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

import org.wso2.carbon.apimgt.lifecycle.manager.LifeCycleExecutionException;

public interface IManageLifeCycle {


    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lcName                        lc name which associates with the resource.
     * @return                              object of added life cycle state.
     * @throws LifeCycleExecutionException  if failed to get lifecycle list.
     */
    public LifeCycleState associateLifecycle (String lcName) throws LifeCycleExecutionException;

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param action {@code String} lifecycle action.
     * @param nextState    {@code LifeCycleState} object represent next life cycle state.
     * @return {@code LifeCycleState} object of updated life cycle state.
     * @throws LifeCycleExecutionException if exception occurred while execute life cycle update.
     */
    public LifeCycleState executeLifeCycleEvent(LifeCycleState nextState, String action, Object resource)
            throws LifeCycleExecutionException ;

    /**
     * Get current life cycle state object.
     *
     * @return {@code LifeCycleState} object represent current life cycle.
     */
    public LifeCycleState getCurrentLifeCycleState();

    /**
     * This method will be used to set lifecycle ID_ATTRIBUTE of current object.
     *
     * @param lifeCycleID {@code String} object that can use to uniquely identify resource.
     */
    public void setLifeCycleID(String lifeCycleID) throws LifeCycleExecutionException;
}
