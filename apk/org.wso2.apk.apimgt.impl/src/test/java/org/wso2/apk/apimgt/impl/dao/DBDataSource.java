package org.wso2.apk.apimgt.impl.dao;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBDataSource {
    static HikariDataSource basicDataSource = new HikariDataSource();
    static String databaseName = "apim";

    DBDataSource() throws Exception {
        //todo: need to fetch the IP correctly without hardcoding
        String ipAddress = "localhost";
        String port = "5432";
        basicDataSource.setDriverClassName("org.postgresql.Driver");
        basicDataSource.setJdbcUrl("jdbc:postgresql://" + ipAddress + ":" + port + "/" + databaseName);
        basicDataSource.setUsername("admin");
        basicDataSource.setPassword("admin");
        basicDataSource.setAutoCommit(true);
        basicDataSource.setMaximumPoolSize(20);
    }

    /**
     * Get a {@link Connection} object
     *
     * @return {@link Connection} from given DataSource
     */
    public Connection getConnection() throws SQLException {
        return basicDataSource.getConnection();
    }

    /**
     * Return javax.sql.DataSource object
     *
     * @return {@link javax.sql.DataSource} object
     */
    public HikariDataSource getDatasource() throws SQLException {
        return basicDataSource;
    }

    /*public void resetDB() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Connection connection = basicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT table_name as TABLE_NAME FROM " +
                    "information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema='public'")) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : tables) {
                statement.addBatch("DROP TABLE " + table + " CASCADE");
            }
            statement.executeBatch();
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }*/
}