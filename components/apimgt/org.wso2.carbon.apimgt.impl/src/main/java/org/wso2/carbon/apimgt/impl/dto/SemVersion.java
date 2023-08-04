/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import org.jetbrains.annotations.NotNull;

public class SemVersion implements Comparable<SemVersion>{
    private String version;
    private int major;
    private int minor;
    private Integer patch;

    public SemVersion(String version, int major, int minor, int patch) {
        this.version = version;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public SemVersion(String version, int major, int minor) {
        this.version = version;
        this.major = major;
        this.minor = minor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public Integer getPatch() {
        return patch;
    }

    public void setPatch(Integer patch) {
        this.patch = patch;
    }

    @Override
    public int compareTo(@NotNull SemVersion version) {
        if (this.major < version.major) {
            return 1;
        } else if (this.major > version.major) {
            return -1;
        } else {
            if (this.minor < version.minor) {
                return 1;
            } else if (this.minor > version.minor) {
                return -1;
            } else {
                if (this.patch != null && version.patch != null) {
                    if( this.patch < version.patch) {
                        return 1;
                    } else if (this.patch > version.patch) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else if (this.patch == null && version.patch != null) {
                    return 1;
                } else if (this.patch != null && version.patch == null) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
