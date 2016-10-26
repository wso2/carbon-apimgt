/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;

/**
 * Identifier class which uniquely identify an API entity
 */
public class APIIdentifier {
    private final String providerName;
    private final String apiName;
    private final String version;
    private String tier;
    private String applicationId;

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTier() {
        return this.tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public APIIdentifier(String providerName, String apiName, String version) {
        this.providerName = providerName;
        this.apiName = apiName;
        this.version = version;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public String getApiName() {
        return this.apiName;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            APIIdentifier that = (APIIdentifier) o;
            return this.apiName.equals(that.apiName)
                   && this.providerName.equals(that.providerName)
                   && this.version.equals(that.version);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.providerName.hashCode();
        result = 31 * result + this.apiName.hashCode();
        result = 31 * result + this.version.hashCode();
        return result;
    }

    public String toString() {
        return this.getProviderName() + '-' + this.getApiName() + '-' + this.getVersion();
    }
}
