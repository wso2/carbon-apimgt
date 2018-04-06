package org.wso2.carbon.apimgt.core.dao.impl;

/**
 * Store DB table meta information
 */

class DBTableMetaData {
    private String tableName;
    private String columnName;

    String getTableName() {
        return tableName;
    }

    void setTableName(String tableName) {
        this.tableName = tableName;
    }

    String getColumnName() {
        return columnName;
    }

    void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
