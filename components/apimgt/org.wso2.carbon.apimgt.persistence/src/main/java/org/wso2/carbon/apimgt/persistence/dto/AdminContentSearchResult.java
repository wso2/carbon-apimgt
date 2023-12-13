/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.persistence.dto;

import org.wso2.carbon.apimgt.api.model.ApplicationInfoKeyManager;

import java.util.ArrayList;
import java.util.List;

public class AdminContentSearchResult {
    int apiCount;

    public int getApiTotal() { return apiTotal; }

    public void setApiTotal(int apiTotal) { this.apiTotal = apiTotal; }

    int apiTotal;
    int applicationCount;
    List<SearchContent> apis = new ArrayList<SearchContent>();

    List<ApplicationInfoKeyManager> applications = new ArrayList<>();

    public List<SearchContent> getApis() {
        return apis;
    }

    public void setApis(List<SearchContent> apis) {
        this.apis = apis;
    }

    public int getApiCount() {
        return apiCount;
    }

    public void setApiCount(int apiCount) {
        this.apiCount = apiCount;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(int applicationCount) {
        this.applicationCount = applicationCount;
    }

    public List<ApplicationInfoKeyManager> getApplications() { return applications; }

    public void setApplications(List<ApplicationInfoKeyManager> applications) {this.applications = applications; }
}
