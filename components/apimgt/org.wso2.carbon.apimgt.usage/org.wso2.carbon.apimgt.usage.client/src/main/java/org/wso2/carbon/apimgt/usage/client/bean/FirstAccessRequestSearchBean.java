package org.wso2.carbon.apimgt.usage.client.bean;

/**
 * Created by rukshan on 9/30/15.
 */
public class FirstAccessRequestSearchBean {
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

    public FirstAccessRequestSearchBean(String query, int start, int count,
            String tableName) {
        super();
        this.query = query;
        this.start = start;
        this.count = count;
        this.tableName = tableName;
    }

    String query;
    int start;
    int count;
    String tableName;
}
