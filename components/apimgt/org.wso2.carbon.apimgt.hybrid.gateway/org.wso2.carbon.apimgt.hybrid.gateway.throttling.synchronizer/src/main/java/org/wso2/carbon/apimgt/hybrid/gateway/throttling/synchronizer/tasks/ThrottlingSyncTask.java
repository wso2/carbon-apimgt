/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.tasks;

import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.ThrottlingSynchronizer;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * Task for Synchronizing throttling tiers
 */
public class ThrottlingSyncTask implements Task {

    @Override
    public void setProperties(Map<String, String> map) {
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        ThrottlingSynchronizer synchronizer = new ThrottlingSynchronizer();
        synchronizer.synchronize();
    }
}
