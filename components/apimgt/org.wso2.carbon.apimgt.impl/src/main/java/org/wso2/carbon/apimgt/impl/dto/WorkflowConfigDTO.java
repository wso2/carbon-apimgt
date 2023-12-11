/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Workflow configurations DTO to hold the workflow config
 */
public class WorkflowConfigDTO {

    private final Map<String, WorkflowConfig> workflowConfigMap;

    public WorkflowConfigDTO() {
        workflowConfigMap = new HashMap<>();
    }

    /**
     * Method adds each WorkflowConfig to the workflowConfigMap
     *
     * @param workflowName type of workflow
     * @param className    executor class name (org.wso2.......ExampleWorkflowExecutor)
     * @param properties   the class specific properties
     */
    public void addWorkflowConfig(String workflowName, String className, JsonObject properties) {
        WorkflowConfig workflowConfig = new WorkflowConfig(className, properties);
        workflowConfigMap.put(workflowName, workflowConfig);
    }

    public Map<String, WorkflowConfig> getWorkflowConfigMap() {
        return workflowConfigMap;
    }


    /**
     * Represents configuration of each workflow type
     */
    public static class WorkflowConfig {
        private String className;
        private JsonObject properties;

        private WorkflowConfig(String className, JsonObject properties) {

            this.className = className;
            this.properties = properties;
        }

        public String getClassName() {

            return className;
        }

        public void setClassName(String className) {

            this.className = className;
        }

        public JsonObject getProperties() {

            return properties;
        }

        public void setProperties(JsonObject properties) {

            this.properties = properties;
        }
    }


}
