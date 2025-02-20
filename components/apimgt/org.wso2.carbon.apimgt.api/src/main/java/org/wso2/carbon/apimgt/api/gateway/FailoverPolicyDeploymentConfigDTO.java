/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.api.gateway;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FailoverPolicyDeploymentConfigDTO {

    @SerializedName("targetModel")
    private ModelEndpointDTO targetModelEndpoint;
    @SerializedName("fallbackModels")
    private List<ModelEndpointDTO> fallbackModelEndpoints;

    // Getters and setters
    public ModelEndpointDTO getTargetModelEndpoint() {

        return targetModelEndpoint;
    }

    public void setTargetModelEndpoint(ModelEndpointDTO targetModelEndpoint) {

        this.targetModelEndpoint = targetModelEndpoint;
    }

    public List<ModelEndpointDTO> getFallbackModelEndpoints() {

        return fallbackModelEndpoints;
    }

    public void setFallbackModelEndpoints(List<ModelEndpointDTO> fallbackModelEndpoints) {

        this.fallbackModelEndpoints = fallbackModelEndpoints;
    }

}
