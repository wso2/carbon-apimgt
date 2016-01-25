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

package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;
import java.util.Set;

/**
 * This class represent the API info DTO.
 */
public class APIInfoDTO implements Serializable{

   private String providerId;
   private String apiName;
   private String version;
   private String context;

   private Set<ResourceInfoDTO> resources;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Set<ResourceInfoDTO> getResources() {
        return resources;
    }

    public void setResources(Set<ResourceInfoDTO> resources) {
        this.resources = resources;
    }

    /**
     * Computes API Identifier using Provider Id, API Name & Version
     * @return API Identifier as a String
     */
    public String getAPIIdentifier() {
        if (providerId != null && apiName != null && version != null) {
            return providerId + '_' + apiName + '_' + version;
        } else {
            return null;
        }
    }
}
