/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;

public class ThirdPartyEnvironment extends Environment implements Serializable {

    private String organization;
    private String provider;
    private String developer;

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThirdPartyEnvironment that = (ThirdPartyEnvironment) o;
        // return Objects.equals(environmentName, that.environmentName) && Objects.equals(organization, that.organization) && Objects.equals(provider, that.provider);
        if (!getName().equals(that.getName())) return false;
        if (!provider.equals(that.getProvider())) return false;
        if (!organization.equals(that.getOrganization())) return false;
        if (!developer.equals(that.getDeveloper())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        //return Objects.hash(environmentName, organization, provider);
        int result = provider.hashCode();
        return 31 * result + getName().hashCode();
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }
}
