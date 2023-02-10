/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.dto;

public class ClonePolicyMetadataDTO {
    String currentPolicyUUID, clonedPolicyUUID, apiUUID, revisionUUID;

    public String getCurrentPolicyUUID() {
        return currentPolicyUUID;
    }

    public void setCurrentPolicyUUID(String currentPolicyUUID) {
        this.currentPolicyUUID = currentPolicyUUID;
    }

    public String getClonedPolicyUUID() {
        return clonedPolicyUUID;
    }

    public void setClonedPolicyUUID(String clonedPolicyUUID) {
        this.clonedPolicyUUID = clonedPolicyUUID;
    }

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    public String getRevisionUUID() {
        return revisionUUID;
    }

    public void setRevisionUUID(String revisionUUID) {
        this.revisionUUID = revisionUUID;
    }
}
