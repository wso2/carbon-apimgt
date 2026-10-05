/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import java.util.List;

public class GatewayPortalConfiguration {
    private String gatewayType;
    private Object supportedFeatures;
    private List<String> supportedAPITypes;

    public Object getSupportedFeatures() {
        return supportedFeatures;
    }

    public void setSupportedFeatures(Object supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    public List<String> getSupportedAPITypes() {
        return supportedAPITypes;
    }

    public void setSupportedAPITypes(List<String> supportedAPITypes) {
        this.supportedAPITypes = supportedAPITypes;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }
}
