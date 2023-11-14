/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.Map;
import java.util.Objects;

public class OperationPolicy implements Comparable<OperationPolicy> {

    private String policyName = "";
    private String policyVersion = "v1";
    private String direction = null;
    private Map<String, Object> parameters = null;
    private String policyId = null;
    private int order = 1;

    public String getPolicyName() {

        return policyName;
    }

    public void setPolicyName(String policyName) {

        this.policyName = policyName;
    }

    public String getPolicyVersion() {

        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {

        this.policyVersion = policyVersion;
    }

    public Map<String, Object> getParameters() {

        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {

        this.parameters = parameters;
    }

    public void setDirection(String direction) {

        this.direction = direction;
    }

    public String getDirection() {

        return direction;
    }

    public void setPolicyId(String policyId) {

        this.policyId = policyId;
    }

    public String getPolicyId() {

        return policyId;
    }

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OperationPolicy policyObj = (OperationPolicy) o;
        return Objects.equals(policyName, policyObj.policyName) && Objects.equals(policyVersion,
                policyObj.policyVersion) && Objects.equals(direction, policyObj.direction) && Objects.equals(
                parameters, policyObj.parameters) && Objects.equals(policyId, policyObj.policyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyName, policyVersion, direction, parameters, policyId);
    }

    @Override
    public int compareTo(OperationPolicy o) {

        return this.order - o.getOrder();
    }

    @Override
    public String toString() {

        return "operationPolicies {" +
                ", policyName ='" + policyName + '\'' +
                ", policyVersion ='" + policyVersion + '\'' +
                ", direction ='" + direction + '\'' +
                ", order ='" + order + '\'' +
                ", policyId ='" + policyId + '\'' +
                ", parameters ='" + parameters + '\'' +
                '}';
    }
}
