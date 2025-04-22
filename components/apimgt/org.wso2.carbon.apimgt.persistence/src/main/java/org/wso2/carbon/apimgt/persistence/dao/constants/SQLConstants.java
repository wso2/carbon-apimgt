package org.wso2.carbon.apimgt.persistence.dao.constants;

public class SQLConstants {
    public static final String ADD_API_SCHEMA_SQL =
            "INSERT INTO AM_API_JSON_SCHEMA (API_SCHEMA, API_UUID)" +
            "VALUES (?, ?)";
}
