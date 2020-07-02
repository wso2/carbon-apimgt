/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.containermgt.k8scrd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class APICustomResourceDefinitionSpec implements KubernetesResource {

    private String mode;
    private String updateTimeStamp;
    private int replicas;
    private Definition definition;
    private boolean override;

    public String getUpdateTimeStamp() {
        return updateTimeStamp;
    }

    public void setUpdateTimeStamp(String updateTimeStamp) {
        this.updateTimeStamp = updateTimeStamp;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * This method returns the string equivalent to the following json object.
     * {
     * "definition": {
     * "swaggerConfigmapNames": ["${swaggerConfigmapNames}"],
     * "type": "${type}",
     * "interceptors": {
     * "ballerina": ["${balInterceptorsConfigmapsNames}"],
     * "java": ["${javaInterceptorsConfigmapsNames}"],
     * },
     * },
     * "replicas": "${replicas}",
     * "mode": "${mode}",
     * "override": bool,
     * "updateTimeStamp": "",
     * }
     *
     * @return
     */
    @Override
    public String toString() {
        return "APICrdSpec{" +
                "replicas=" + replicas +
                ", mode=" + mode +
                ", definition=" + definition +
                ", override=" + override +
                ", updateTimeStamp=" + updateTimeStamp +
                "}";
    }
}
