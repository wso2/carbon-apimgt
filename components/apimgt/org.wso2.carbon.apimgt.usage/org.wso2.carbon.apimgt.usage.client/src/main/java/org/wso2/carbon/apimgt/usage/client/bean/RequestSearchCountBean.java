package org.wso2.carbon.apimgt.usage.client.bean;

/**
 * This class represents the request bean for getting the search count for DAS table
 */
public class RequestSearchCountBean {

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

    public RequestSearchCountBean(String tableName, String query) {
        super();
        this.query = query;
        this.tableName = tableName;
    }

    private String query;
    private String tableName;
}
