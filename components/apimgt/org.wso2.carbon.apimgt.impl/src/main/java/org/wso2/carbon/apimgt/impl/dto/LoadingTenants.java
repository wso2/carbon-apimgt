/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.dto;

import java.util.ArrayList;
import java.util.List;

public class LoadingTenants {
    private boolean includeAllTenants = false;
    private List<String> includingTenants= new ArrayList<>();
    private List<String> excludingTenants= new ArrayList<>();

    public boolean isIncludeAllTenants() {
        return includeAllTenants;
    }

    public void setIncludeAllTenants(boolean includeAllTenants) {
        this.includeAllTenants = includeAllTenants;
    }

    public List<String> getIncludingTenants() {
        return includingTenants;
    }

    public void setIncludingTenants(List<String> includingTenants) {
        this.includingTenants = includingTenants;
    }

    public List<String> getExcludingTenants() {
        return excludingTenants;
    }

    public void setExcludingTenants(List<String> excludingTenants) {
        this.excludingTenants = excludingTenants;
    }
}
