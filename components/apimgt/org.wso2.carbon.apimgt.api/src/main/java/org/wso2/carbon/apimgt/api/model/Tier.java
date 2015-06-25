/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.Map;

/**
 * This class represent the Tier
 */
@SuppressWarnings("unused")
public class Tier implements Serializable{

    private String name;
    private String displayName;
    private String description;
    private byte[] policyContent;
    private Map<String,Object> tierAttributes;

    public Tier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getPolicyContent() {
        return policyContent;
    }

    public void setPolicyContent(byte[] policyContent) {
        this.policyContent = policyContent;
    }
    public Map<String,Object> getTierAttributes() {
        return tierAttributes;
    }

    public void setTierAttributes(Map<String,Object> tierAttributes) {
        this.tierAttributes = tierAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tier tier = (Tier) o;

        return !(name != null ? !name.equals(tier.name) : tier.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
