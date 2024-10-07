/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

public class AIConfiguration {
    private String llmProviderId;
    private String llmProviderName;
    private String llmProviderApiVersion;

    public String getLlmProviderName() {

        return llmProviderName;
    }

    public void setLlmProviderName(String llmProviderName) {

        this.llmProviderName = llmProviderName;
    }

    public String getLlmProviderApiVersion() {

        return llmProviderApiVersion;
    }

    public void setLlmProviderApiVersion(String llmProviderApiVersion) {

        this.llmProviderApiVersion = llmProviderApiVersion;
    }

    public String getLlmProviderId() {

        return llmProviderId;
    }

    public void setLlmProviderId(String llmProviderId) {

        this.llmProviderId = llmProviderId;
    }
}
