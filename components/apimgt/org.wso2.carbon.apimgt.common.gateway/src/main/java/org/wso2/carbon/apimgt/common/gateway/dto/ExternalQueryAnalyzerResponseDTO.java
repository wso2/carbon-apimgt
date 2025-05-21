/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.common.gateway.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for External Query Analyzer Response.
 * Contains vulnerability detection results and related information.
 */
public class ExternalQueryAnalyzerResponseDTO {
    boolean isVulnerable;

    List<String> vulList = new ArrayList<>();

    public boolean isVulnerable() {
        return isVulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        isVulnerable = vulnerable;
    }

    public List<String> getVulList() {
        return vulList;
    }

    public void addVulToList(String vul) {
        this.vulList.add(vul);
    }

}
