/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.lifecycle.manager.core.beans;

import java.util.LinkedList;
import java.util.List;

/**
 * This bean holds the data about next available states for a particular lifecycle state which are defined in
 * lifecycle configuration. This behaves as a node if we represent lifecycle config as graph.
 */
public class LifecycleNode {

    String lifecycleState;
    List<AvailableTransitionBean> targetStates;

    public LifecycleNode() {
        this.targetStates = new LinkedList<>();
    }

    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public List<AvailableTransitionBean> getTargetStates() {
        return targetStates;
    }

    public void setTargetStates(List<AvailableTransitionBean> targetStates) {
        this.targetStates = targetStates;
    }
}
