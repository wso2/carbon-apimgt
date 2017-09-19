/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.core.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

/**
 * This builder can be used to build {@link WorkflowConfig} object based on the workflow extension configuration file
 */
public class WorkflowExtensionsConfigBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExtensionsConfigBuilder.class);

    private static WorkflowConfig workflowConfig;

    public static WorkflowConfig getWorkflowConfig() {
        return workflowConfig;
    }

    public static void clearConfig() {
        workflowConfig = null;
    }
    public static void build(ConfigProvider configProvider) {

        try {
            workflowConfig = configProvider.getConfigurationObject(WorkflowConfig.class);
        } catch (ConfigurationException e) {
            logger.error("Error while loading the configuration for worlflow ", e);
            //build default executors
            workflowConfig = new WorkflowConfig();
        }
    }
}
