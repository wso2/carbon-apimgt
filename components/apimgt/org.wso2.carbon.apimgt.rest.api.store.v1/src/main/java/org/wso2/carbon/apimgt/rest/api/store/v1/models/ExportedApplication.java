/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.models;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;

import java.util.Set;

/**
 * This class represent the Exported Application model which is an aggregated model
 */
public class ExportedApplication {

    private ApplicationDTO applicationInfo;
    private Set<ExportedSubscribedAPI> subscribedAPIs;
    private Set<ApplicationKeyDTO> applicationKeys;

    public ExportedApplication(ApplicationDTO applicationDto) {
        this.applicationInfo = applicationDto;
    }

    public Set<ExportedSubscribedAPI> getSubscribedAPIs() {
        return subscribedAPIs;
    }

    public void setSubscribedAPIs(Set<ExportedSubscribedAPI> subscribedAPIs) {
        this.subscribedAPIs = subscribedAPIs;
    }

    public ApplicationDTO getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(ApplicationDTO applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public Set<ApplicationKeyDTO> getApplicationKeys() {
        return applicationKeys;
    }

    public void setApplicationKeys(Set<ApplicationKeyDTO> applicationKeys) {
        this.applicationKeys = applicationKeys;
    }
}
