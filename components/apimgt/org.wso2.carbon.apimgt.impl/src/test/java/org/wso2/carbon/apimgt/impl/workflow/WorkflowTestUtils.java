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

package org.wso2.carbon.apimgt.impl.workflow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.wso2.carbon.apimgt.impl.dto.WorkflowConfigDTO;

import java.util.Map;
import java.util.Set;

/**
 * Util class for workflow tests
 */
public class WorkflowTestUtils {

    /**
     * Utility method used in unit test to convert workflow config json to WorkflowConfigDTO
     *
     * @param tenantConfig JsonObject
     * @return WorkflowConfigDTO
     */
    public static WorkflowConfigDTO getWorkFlowConfigDTOFromJsonConfig(JsonObject tenantConfig) {
        WorkflowConfigDTO config = new WorkflowConfigDTO();
        JsonObject workflowConfig = (JsonObject) tenantConfig.get("Workflows");
        if (workflowConfig != null) {
            Set<Map.Entry<String, JsonElement>> configEntries = workflowConfig.entrySet();
            for (Map.Entry<String, JsonElement> entry : configEntries) {
                String workflowName = entry.getKey();
                JsonObject workflowConfigEntry = (JsonObject) entry.getValue();

                boolean isEnabled = workflowConfigEntry.get("Enabled") != null
                        && workflowConfigEntry.get("Enabled").getAsBoolean();
                String className = workflowConfigEntry.get("Class") != null ?
                        workflowConfigEntry.get("Class").getAsString() : "";
                JsonObject properties = workflowConfigEntry.get("Properties") != null ?
                        workflowConfigEntry.get("Properties").getAsJsonObject() : null;

                config.addWorkflowConfig(workflowName, isEnabled, className, properties);
            }
        }
        return config;
    }
}
