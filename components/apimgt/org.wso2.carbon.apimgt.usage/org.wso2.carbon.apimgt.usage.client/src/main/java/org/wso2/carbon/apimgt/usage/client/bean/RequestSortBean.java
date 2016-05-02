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
import java.util.Map;

/**
 * This class is used as a bean representing Lucene search sorting bean
 */
public class RequestSortBean {
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, String>> getSortBy() {
        return sortBy;
    }

    public void setSortBy(List<Map<String, String>> sortBy) {
        this.sortBy = sortBy;
    }

    public RequestSortBean(String query, int start, int count, String tableName, List<Map<String, String>> sortBy) {
        super();
        this.query = query;
        this.start = start;
        this.count = count;
        this.tableName = tableName;
        this.sortBy = sortBy;
    }

    private String query;
    private int start;
    private int count;
    private String tableName;
    private List<Map<String,String>> sortBy;
}
