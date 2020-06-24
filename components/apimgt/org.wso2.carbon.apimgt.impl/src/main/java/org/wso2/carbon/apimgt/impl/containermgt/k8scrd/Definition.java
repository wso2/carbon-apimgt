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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;

@JsonDeserialize(using = JsonDeserializer.None.class)

public class Definition implements KubernetesResource {

    private String[] swaggerConfigmapNames;
    private String type;
    private Interceptors interceptors;

    public String[] getSwaggerConfigmapNames() { return swaggerConfigmapNames; }

    public void setSwaggerConfigmapNames(String[] swaggerConfigmapNames) { this.swaggerConfigmapNames = swaggerConfigmapNames; }

    public Interceptors getInterceptors() { return interceptors; }

    public void setInterceptors(Interceptors interceptors) { this.interceptors = interceptors; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * This method is to create the following json object
     * {
     * "swaggerConfigmapNames": ["${swaggerConfigmapNames}"],
     * "type": "swagger"
     * "interceptors": {
     * "ballerina": ["${balInterceptorsConfigmapsNames}"],
     * "java": ["${javaInterceptorsConfigmapsNames}"],
     * },
     * },
     *
     * @return
     */
    @Override
    public String toString() {
        return "Definition{" +
                "swaggerConfigmapNames=" + Arrays.toString(swaggerConfigmapNames) +
                ", type='" + type + '\'' +
                ", interceptors=" + interceptors +
                '}';
    }
}
