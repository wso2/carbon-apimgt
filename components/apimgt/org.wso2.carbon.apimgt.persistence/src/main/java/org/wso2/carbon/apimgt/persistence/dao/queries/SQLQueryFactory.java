package org.wso2.carbon.apimgt.persistence.dao.queries;
import org.wso2.carbon.apimgt.persistence.dao.queries.impl.Oracle;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceDBUtil;

import java.sql.Connection;

public class SQLQueryFactory {
    private static final String DEFAULT_DB_TYPE = "Oracle";

    public static SQLQueryInterface getSQLQueries() {
        String dbType = null;
        try {
            Connection connection = PersistenceDBUtil.getConnection();
            if (connection != null) {
                dbType = connection.getMetaData().getDriverName();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while getting SQL constants", e);
        }

        if (dbType == null || dbType.isEmpty()) {
            dbType = DEFAULT_DB_TYPE; // Fallback to default if unable to determine
        }

        switch (dbType.toLowerCase()) {
            case "mysql":
//                return new MySQLConstants();
            case "postgresql":
//                return new PostgreSQLConstants();
            case "oracle":
                return new Oracle();
            case "mssql":
//                return new MSSQLConstants();
            case "db2":
//                return new DB2Constants();
            default:
                return new Oracle(); // Default to MySQL
        }
    }
}