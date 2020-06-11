/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Arrays;

public class Interceptors implements KubernetesResource {

    private String[] ballerina;
    private String[] java;

    public String[] getBallerina() { return ballerina; }

    public void setBallerina(String[] ballerina) { this.ballerina = ballerina; }

    public String[] getJava() { return java; }

    public void setJava(String[] java) { this.java = java; }

    /**
     * This method is to create the following json object
     * {
     *   "ballerina": ["${balInterceptorsConfigmapsNames}"],
     *   "java": ["${javaInterceptorsConfigmapsNames}"],
     * },
     *
     * @return
     */
    @Override
    public String toString() {
        return "Interceptors{" +
                "ballerina=" + Arrays.toString(ballerina) +
                ", java=" + Arrays.toString(java) +
                '}';
    }
}
