/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.api.model;

public class MajorRangeVersionInfo {
    private Boolean isLatest;
    private String latestVersion;
    private String latestVersionAPIId;

    public MajorRangeVersionInfo(Boolean isLatest, String latestVersion, String latestVersionAPIId) {
        this.isLatest = isLatest;
        this.latestVersion = latestVersion;
        this.latestVersionAPIId = latestVersionAPIId;
    }

    public Boolean getLatest() {
        return isLatest;
    }

    public void setLatest(Boolean latest) {
        isLatest = latest;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestVersionAPIId() {
        return latestVersionAPIId;
    }

    public void setLatestVersionAPIId(String latestVersionAPIId) {
        this.latestVersionAPIId = latestVersionAPIId;
    }
}
