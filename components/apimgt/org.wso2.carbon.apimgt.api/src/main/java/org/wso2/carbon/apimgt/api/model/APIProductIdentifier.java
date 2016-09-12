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

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.Serializable;

/**
 * An API Product can be uniquely identified by a combination of providerName,apiProductName & version.
 * This class represents this unique identifier.
 */
@SuppressWarnings("unused")
public class APIProductIdentifier implements Serializable {
    private final String providerName;
    private final String apiProductName;
    private final String version;
    private String tier;
    private String applicationId;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public APIProductIdentifier(String providerName, String apiProductName, String version) {
        this.providerName = providerName;
        this.apiProductName = apiProductName;
        this.version = version;
    }

    public APIProductIdentifier(String apiId) throws APIManagementException {
        //eg: apiId = "P1_API1_v1.0.0"
        String[] split = apiId.split("_");
        if (split.length == 3) {
            this.providerName = split[0];
            this.apiProductName = split[1];
            this.version = split[2];
        } else {
            throw new APIManagementException("Invalid API ID : " + apiId);
        }
    }

    public String getProviderName() {
        return providerName;
    }

    public String getApiProductName() {
        return apiProductName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIProductIdentifier that = (APIProductIdentifier) o;

        return apiProductName.equals(that.apiProductName) && providerName.equals(that.providerName) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = providerName.hashCode();
        result = 31 * result + apiProductName.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.getProviderName() + '-' + this.getApiProductName() + '-' + this.getVersion();
    }
}
