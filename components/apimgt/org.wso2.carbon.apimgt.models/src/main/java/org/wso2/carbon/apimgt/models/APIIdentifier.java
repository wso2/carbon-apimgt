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

package org.wso2.carbon.apimgt.models;

import java.io.Serializable;

/**
 * An API can be uniquely identified by a combination of providerName,apiName & version.
 * This class represents this unique identifier.
 */

public final class APIIdentifier implements Serializable {

    private final String providerName;
    private final String apiName;
    private final String version;

    public APIIdentifier(String providerName, String apiName, String version) {
        this.providerName = providerName;
        this.apiName = apiName;
        this.version = version;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIIdentifier that = (APIIdentifier) o;

        return apiName.equals(that.apiName) && providerName.equals(that.providerName) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = providerName.hashCode();
        result = 31 * result + apiName.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
    	return this.getProviderName() + '-' + this.getApiName() + '-' + this.getVersion();
    }
}
