package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 9/28/15.
 */
public class RequestSearchBean {
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    //    public int getStart() {
    //        return start;
    //    }
    //
    //    public void setStart(int start) {
    //        this.start = start;
    //    }
    //
    //    public int getCount() {
    //        return count;
    //    }
    //
    //    public void setCount(int count) {
    //        this.count = count;
    //    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public RequestSearchBean(String query, int aggregateLevel, String groupByField, String tableName) {
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

    String query;
    //    int start;
    //    int count;
    int aggregateLevel;
    String tableName;
    String groupByField;
    List<AggregateField> aggregateFields;
}
