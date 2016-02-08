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
 * This class is used as a bean for represent Lucene aggregate request for DAS REST API
 */
public class SearchRequestBean {
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SearchRequestBean(String query, int aggregateLevel, String groupByField, String tableName) {
        super();
        this.query = query;
        this.aggregateLevel = aggregateLevel;
        this.groupByField = groupByField;
        this.tableName = tableName;
    }

    public int getAggregateLevel() {
        return aggregateLevel;
    }

    public void setAggregateLevel(int aggregateLevel) {
        this.aggregateLevel = aggregateLevel;
    }

    public String getGroupByField() {
        return groupByField;
    }

    public void setGroupByField(String groupByField) {
        this.groupByField = groupByField;
    }

    public List<AggregateField> getAggregateFields() {
        return aggregateFields;
    }

    public void setAggregateFields(List<AggregateField> aggregateFields) {
        this.aggregateFields = aggregateFields;
    }

    private String query;
    private int aggregateLevel;
    private String tableName;
    private String groupByField;
    private List<AggregateField> aggregateFields;
}
