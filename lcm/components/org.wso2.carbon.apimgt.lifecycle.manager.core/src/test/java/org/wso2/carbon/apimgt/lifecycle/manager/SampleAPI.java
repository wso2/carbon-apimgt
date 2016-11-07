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

import org.wso2.carbon.apimgt.lifecycle.manager.core.ManagedLifecycle;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a mock api class which implements ManagedLifecycle interface.
 */
public class SampleAPI implements ManagedLifecycle {

    private String name;
    private String version;
    private LifecycleState lifecycleState;
    private Map<String, String> lifecycleIdMap = new HashMap<>();

    public SampleAPI() {
        lifecycleState = new LifecycleState();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    /*@Override
    public void creatLifecycleEntry(String lcName, String user) throws LifecycleException {
        this.lifecycleState = LifecycleOperationManager
                .creatLifecycleEntry(TestConstants.SERVICE_LIFE_CYCLE, TestConstants.ADMIN);
    }

    @Override
    public void executeLifecycleEvent(LifecycleState nextState, String uuid, String action, String user,
            Object resource) throws LifecycleException {
        this.lifecycleState = LifecycleOperationManager.executeLifecycleEvent(nextState, uuid, action, user, this);
    }

    @Override
    public void setCurrentLifecycleState(String uuid) throws LifecycleException {
        this.lifecycleState = LifecycleOperationManager.getCurrentLifecycleState(uuid);
    }*/

    @Override
    public void dissociateLifecycle() {
        this.lifecycleState = null;
        // Implement logic to remove persisted lifecycle id in API side.
    }

    @Override
    public void associateLifecycle (LifecycleState lifecycleState) {
        setLifecycleState(lifecycleState);
        lifecycleIdMap.put(lifecycleState.getLcName(), lifecycleState.getLifecycleId());
        // Implement logic to persist lifecycle id in API side as well
    }

    @Override
    public String getLifecycleId(String lcName) {
        return lifecycleIdMap.get(lcName);
    }

}
