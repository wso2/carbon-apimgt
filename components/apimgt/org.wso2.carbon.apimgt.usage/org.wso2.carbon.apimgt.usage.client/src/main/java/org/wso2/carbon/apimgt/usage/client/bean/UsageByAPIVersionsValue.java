/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * This class is used as a bean for represent API version based usage statistics result from the DAS REST API
 */
public class UsageByAPIVersionsValue {
    private int totalRequestCount;
    private List<String> api_version_context_facet;

    public UsageByAPIVersionsValue(int lastAccessTime, List<String> api_version_userId_facet) {
        super();
        this.totalRequestCount = lastAccessTime;
        this.api_version_context_facet = api_version_userId_facet;
    }

    public int getTotalRequestCount() {
        return totalRequestCount;
    }

    public void setTotalRequestCount(int lastAccessTime) {
        this.totalRequestCount = lastAccessTime;
    }

    public List<String> getColumnNames() {
        return api_version_context_facet;
    }

    public void setColumnNames(List<String> api_version_userId_facet) {
        this.api_version_context_facet = api_version_userId_facet;
    }

}
