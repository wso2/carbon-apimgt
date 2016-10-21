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

import org.wso2.carbon.apimgt.lifecycle.manager.constants.TestConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.ManagedLifecycleUtil;
import org.wso2.carbon.apimgt.lifecycle.manager.interfaces.ManagedLifecycle;

/**
 * This is a mock api class which implements ManagedLifecycle interface.
 */
public class SampleAPI implements ManagedLifecycle {

    private String name;
    private String version;
    private LifecycleState lifecycleState;

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

    @Override
    public void associateLifecycle(String lcName, String user) throws LifecycleException {
        this.lifecycleState = ManagedLifecycleUtil
                .associateLifecycle(TestConstants.SERVICE_LIFE_CYCLE, TestConstants.ADMIN);
    }

    @Override
    public void executeLifecycleEvent(LifecycleState nextState, String uuid, String action, String user,
            Object resource) throws LifecycleException {
        this.lifecycleState = ManagedLifecycleUtil.executeLifecycleEvent(nextState, uuid, action, user, this);
    }

    @Override
    public void getCurrentLifecycleState(String uuid) throws LifecycleException {
        this.lifecycleState = ManagedLifecycleUtil.getCurrentLifecycleState(uuid);
    }

    @Override
    public void dissociateLifecycle(String uuid) throws LifecycleException {
        ManagedLifecycleUtil.dissociateLifecycle(uuid);
        this.lifecycleState = null;
    }

}
