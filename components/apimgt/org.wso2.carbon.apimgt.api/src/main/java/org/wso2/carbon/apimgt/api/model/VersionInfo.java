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

public class VersionInfo {
    private String version;
    private MajorRangeVersionInfo majorRange;

    public VersionInfo(String version, MajorRangeVersionInfo majorRange) {
        this.version = version;
        this.majorRange = majorRange;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MajorRangeVersionInfo getMajorRangeVersionInfo() {
        return majorRange;
    }

    public void setMajorRangeVersionInfo(MajorRangeVersionInfo majorRange) {
        this.majorRange = majorRange;
    }
}
