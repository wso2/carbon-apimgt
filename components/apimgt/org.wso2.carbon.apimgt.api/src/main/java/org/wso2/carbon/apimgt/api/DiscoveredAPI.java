/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;

/**
 * This class represents a discovered API.
 */
public class DiscoveredAPI {
    API api;
    String referenceArtifact;

    public DiscoveredAPI(API api, String referenceArtifact) {
        this.api = api;
        this.referenceArtifact = referenceArtifact;
    }
    public API getApi() {
        return api;
    }
    public void setApi(API api) {
        this.api = api;
    }
    public String getReferenceArtifact() {
        return referenceArtifact;
    }
    public void setReferenceArtifact(Object referenceArtifact) {}
}
