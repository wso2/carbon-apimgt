/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.APISynchronizer;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.exceptions.APISynchronizationException;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * Class which performs the task of periodically checking for API Publish/Re-publish events and
 * updating the APIs accordingly
 */
public class APISynchronizationTask implements Task {

    private static final Log log = LogFactory.getLog(APISynchronizationTask.class);

    @Override
    public void setProperties(Map<String, String> map) {
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.info("Starting API synchronization task.");
        }
        try {
            APISynchronizer synchronizer = new APISynchronizer();
            synchronizer.updateApis();
            if (log.isDebugEnabled()) {
                log.info("API synchronization task completed.");
            }
        } catch (APISynchronizationException e) {
            log.error("Failed to synchronize updated APIs.", e);
        }
        if (log.isDebugEnabled()) {
            log.info("API synchronization task completed.");
        }
    }
}
