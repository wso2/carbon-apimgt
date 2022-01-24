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

import java.util.List;

public class OperationPolicySpecification {

    private String policyName;
    private String displayName;
    private String policyDescription;
    private List<String> flow;
    private List<String> supportedGatewayTypes;
    private List<String> apiTypes;
    private List<OperationPolicySpecAttribute> policyAttributes;

    public String getPolicyName() {

        return policyName;
    }

    public void setPolicyName(String policyName) {

        this.policyName = policyName;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getPolicyDescription() {

        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {

        this.policyDescription = policyDescription;
    }

    public List<String> getFlow() {

        return flow;
    }

    public void setFlow(List<String> flow) {

        this.flow = flow;
    }

    public List<String> getSupportedGatewayTypes() {

        return supportedGatewayTypes;
    }

    public void setSupportedGatewayTypes(List<String> supportedGatewayTypes) {

        this.supportedGatewayTypes = supportedGatewayTypes;
    }

    public List<String> getApiTypes() {

        return apiTypes;
    }

    public void setApiTypes(List<String> apiTypes) {

        this.apiTypes = apiTypes;
    }

    public List<OperationPolicySpecAttribute> getPolicyAttributes() {

        return policyAttributes;
    }

    public void setPolicyAttributes(
            List<OperationPolicySpecAttribute> policyAttributes) {

        this.policyAttributes = policyAttributes;
    }
}
