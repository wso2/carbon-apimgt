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
 * This class is used as a bean for represent API user's usage statistics result from the DAS REST API
 */
public class APIUsageByUserValues {
    private int count;
    private List<String> api_version_userId_apiPublisher_facet;

    public int getCountSum() {
        return count;
    }

    public void setCountSum(int count_sum) {
        this.count = count_sum;
    }

    public List<String> getColumnNames() {
        return api_version_userId_apiPublisher_facet;
    }

    public void setColumnNames(List<String> ColumnNames) {
        this.api_version_userId_apiPublisher_facet = ColumnNames;
    }

    public APIUsageByUserValues(int count_sum, List<String> ColumnNames) {
        this.count = count_sum;
        this.api_version_userId_apiPublisher_facet = ColumnNames;
    }
}
