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

import java.util.Objects;

public class OperationPolicyDefinition {

    public enum GatewayType {
        Synapse,
        ChoreoConnect
    }

    private GatewayType gatewayType = GatewayType.Synapse;
    private String content = null;
    private String md5Hash = null;

    public GatewayType getGatewayType() {

        return gatewayType;
    }

    public void setGatewayType(GatewayType gatewayType) {

        this.gatewayType = gatewayType;
    }

    public String getContent() {

        return content;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public String getMd5Hash() {

        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {

        this.md5Hash = md5Hash;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof OperationPolicyDefinition))
            return false;
        OperationPolicyDefinition that = (OperationPolicyDefinition) o;
        return gatewayType == that.gatewayType && content.equals(that.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(gatewayType, content);
    }
}
