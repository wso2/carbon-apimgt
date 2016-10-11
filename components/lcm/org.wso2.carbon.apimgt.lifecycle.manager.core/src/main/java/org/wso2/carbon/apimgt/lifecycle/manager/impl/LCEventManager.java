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
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LCStateBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LCMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LCManagerDatabaseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * This class communicate with DAO layer to perform lifecycle operations.
 */
public class LCEventManager {

    private int tenantId;

    public LCEventManager(){
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Associates lifecycle with an asset. Sets the initial state as the current state.
     *
     * @param lcName                        Name of lifecycle which asset being associated with.
     * @param initialState                  initial state provided in the scxml configuration.
     * @return                              uuid generated for that particular asset.
     * @throws LifeCycleExecutionException
     */
    public String associateLifecycle (String lcName, String initialState) throws LifeCycleExecutionException{
        try {
            return getLCMgtDAOInstance().addLifecycleState(initialState, lcName, tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleExecutionException("Error while associating lifecycle " + lcName,e);
        }
    }

    /**
     * Changes the lifecycle state.
     *
     * @param requiredState                 The expected state
     * @param id                            uuid of the current state which maps with the asset.
     * @throws LifeCycleExecutionException
     */
    public void changeLifecycleState(String requiredState, String id) throws LifeCycleExecutionException{
        LCStateBean lcStateBean = new LCStateBean();
        lcStateBean.setStatus(requiredState);
        lcStateBean.setStateId(id);
        lcStateBean.setTenantId(tenantId);
        try {
            getLCMgtDAOInstance().changeLifecycleState(lcStateBean);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleExecutionException("Error while changing lifecycle state to  " + requiredState,e);
        }
    }

    /**
     * Get data related to particular uuid from LC_DATA table
     *
     * @param uuid                        uuid of the state.
     * @return                            Lifecycle state data associated with the uuid.
     * @throws LifeCycleExecutionException
     */
    public LCStateBean getLifecycleStateData (String uuid) throws LifeCycleExecutionException{
        try {
            return getLCMgtDAOInstance().getLifecycleStateDataFromId(uuid, tenantId);
        } catch (LCManagerDatabaseException e) {
            throw new LifeCycleExecutionException("error while getting lifecycle data for id : "+ uuid);
        }
    }

    private LCMgtDAO getLCMgtDAOInstance(){
        return LCMgtDAO.getInstance();
    }
}
