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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayFeatureCatalog {

    private Map<String, Object> gatewayFeatures = new HashMap<String, Object>();
    private Map<String, List<String>> apiTypes = new HashMap<String, List<String>>();

    public Map<String, Object> getGatewayFeatures() {
        return gatewayFeatures;
    }

    public void setGatewayFeatures(Map<String, Object> gatewayFeatures) {
        this.gatewayFeatures = gatewayFeatures;
    }

    public Map<String, List<String>> getApiTypes() {
        return apiTypes;
    }

    public void setApiTypes(Map<String, List<String>> apiTypes) {
        this.apiTypes = apiTypes;
    }
}
