/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.lifecycle.manager.sql.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.Constants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.SQLConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LCManagerDatabaseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;
import javax.sql.DataSource;

/**
 * This class executes the sql script in dbscripts/lifecycle folder to initialize the database
 * tables during startup.
 */
public class LCDatabaseCreator  {

    public static final String OPENEDGE = "openedge";
    public static final String REM = "REM";
    public static final String HSQL = "hsql";
    public static final String DERBY = "derby";
    public static final String MYSQL = "mysql";
    public static final String ORACLE = "oracle";
    public static final String MSSQL = "mssql";
    public static final String H2 = "h2";
    public static final String DB2 = "db2";
    public static final String POSTGRESQL = "postgresql";
    public static final String OPENEDGE1 = "openedge";
    public static final String INFORMIX = "informix";
    private static Log log = LogFactory.getLog(LCDatabaseCreator.class);
    private DataSource dataSource;
    private String delimiter = ";";
    Statement statement;

    public LCDatabaseCreator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This method will be used to create life cycle database tables if they are not created already.
     *
     * @throws LCManagerDatabaseException   If error occurs during database creation.
     */
    public void createLifecycleDatabase() throws LCManagerDatabaseException {
        if(!isDatabaseStructureCreated()){
            log.debug("DB structure not found for life cycles. Executing scripts");
            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                statement = conn.createStatement();
                executeSQLScript();
                conn.commit();
                log.info("Lifecycle DB tables created successfully.");
            } catch (SQLException e) {
                String msg = "Failed to create database tables for Lifecycles. "
                        + e.getMessage();
                log.error(msg, e);
                throw new LCManagerDatabaseException(msg, e);
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    log.error("Failed to close database connection.", e);
                }
            }
        }else{
            log.info("Lifecycle database already exists. Not creating a new database.");
        }
    }

    /**
     * This method will execute database scripts based on the database type.
     *
     * @throws LCManagerDatabaseException   If sql script reading fails.
     * @throws SQLException                 If script execution fails.
     */
    private void executeSQLScript() throws LCManagerDatabaseException,SQLException {
        String databaseType = LCDatabaseCreator.getDatabaseType(dataSource.getConnection());
        if(log.isDebugEnabled()) {
            log.debug("Executing DB script for :" + databaseType);
        }
        boolean keepFormat = false;
        if (ORACLE.equals(databaseType)) {
            delimiter = "/";
        } else if (DB2.equals(databaseType)) {
            delimiter = "/";
        } else if (OPENEDGE.equals(databaseType)) {
            delimiter = "/";
            keepFormat = true;
        }

        String dbScriptLocation = getDbScriptLocation(databaseType);

        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;


        try {
            InputStream is = new FileInputStream(dbScriptLocation);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!keepFormat) {
                    if (line.startsWith("//")) {
                        continue;
                    }
                    if (line.startsWith("--")) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (REM.equalsIgnoreCase(token)) {
                            continue;
                        }
                    }
                }
                sql.append(keepFormat ? "\n" : " ").append(line);

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (!keepFormat && line.contains("--")) {
                    sql.append("\n");
                }
                if ((checkStringBufferEndsWith(sql, delimiter))) {
                    executeSQL(sql.substring(0, sql.length() - delimiter.length()));
                    sql.replace(0, sql.length(), "");
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
                executeSQL(sql.toString());
            }
        } catch (IOException e) {
            log.error("Error occurred while executing SQL script for creating Lifecycle database", e);
            throw new LCManagerDatabaseException("Error occurred while executing SQL script for creating Lifecycle database", e);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e){
                    log.error("Error while closing stream");
                }

            }
        }
    }

    /**
     * This method will be used to get database type .
     *  @param conn     Database connection
     *  @return database type
     *
     *  @throws SQLException
     *  @throws LCManagerDatabaseException
     */
    private static String getDatabaseType(Connection conn) throws SQLException, LCManagerDatabaseException {
        String type = null;
            if (conn != null && (!conn.isClosed())) {
                DatabaseMetaData metaData = conn.getMetaData();
                String databaseProductName = metaData.getDatabaseProductName();
                if (databaseProductName.matches("(?i).*hsql.*")) {
                    type = HSQL;
                } else if (databaseProductName.matches("(?i).*derby.*")) {
                    type = DERBY;
                } else if (databaseProductName.matches("(?i).*mysql.*")) {
                    type = MYSQL;
                } else if (databaseProductName.matches("(?i).*oracle.*")) {
                    type = ORACLE;
                } else if (databaseProductName.matches("(?i).*microsoft.*")) {
                    type = MSSQL;
                } else if (databaseProductName.matches("(?i).*h2.*")) {
                    type = H2;
                } else if (databaseProductName.matches("(?i).*db2.*")) {
                    type = DB2;
                } else if (databaseProductName.matches("(?i).*postgresql.*")) {
                    type = POSTGRESQL;
                } else if (databaseProductName.matches("(?i).*openedge.*")) {
                    type = OPENEDGE1;
                } else if (databaseProductName.matches("(?i).*informix.*")) {
                    type = INFORMIX;
                } else {
                    String msg = "Unsupported database: " + databaseProductName +
                            ". Database will not be created automatically by the Carbon Server. " +
                            "Please create the database using appropriate database scripts for " +
                            "the Lifecycle database.";
                    throw new LCManagerDatabaseException(msg);
                }
            }
        return type;
    }

    /**
     * This method will provide database script for particular database type.
     *
     * @param databaseType
     * @return database script location (/repository/dbscripts/lifecycle....)
     */
    private String getDbScriptLocation(String databaseType) {
        //String scriptName = databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from : resource.sql");
        }
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        return carbonHome + File.separator + "dbscripts" + File.separator + "lifecycle" + File.separator + databaseType
                + File.separator + "resource.sql";
    }

    /**
     * Checks that a string buffer ends up with a given string. It may sound
     * trivial with the existing
     * JDK API but the various implementation among JDKs can make those
     * methods extremely resource intensive
     * and perform poorly due to massive memory allocation and copying. See
     *
     * @param buffer the buffer to perform the check on
     * @param suffix the suffix
     * @return <code>true</code> if the character sequence represented by the
     * argument is a suffix of the character sequence represented by
     * the StringBuffer object; <code>false</code> otherwise. Note that the
     * result will be <code>true</code> if the argument is the
     * empty string.
     */
    public static boolean checkStringBufferEndsWith(StringBuffer buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        // this loop is done on purpose to avoid memory allocation performance
        // problems on various JDKs
        // StringBuffer.lastIndexOf() was introduced in jdk 1.4 and
        // implementation is ok though does allocation/copying
        // StringBuffer.toString().endsWith() does massive memory
        // allocation/copying on JDK 1.5
        // See http://issues.apache.org/bugzilla/show_bug.cgi?id=37169
        int endIndex = suffix.length() - 1;
        int bufferIndex = buffer.length() - 1;
        while (endIndex >= 0) {
            if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
                return false;
            }
            bufferIndex--;
            endIndex--;
        }
        return true;
    }

    /**
     * executes given sql
     *
     * @param sql
     * @throws LCManagerDatabaseException
     */
    private void executeSQL(String sql) throws LCManagerDatabaseException {
        // Check and ignore empty statements
        if ("".equals(sql.trim())) {
            return;
        }

        ResultSet resultSet = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("SQL : " + sql);
            }

            boolean ret;
            int updateCount, updateCountTotal = 0;
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            resultSet = statement.getResultSet();
            do {
                if (!ret) {
                    if (updateCount != -1) {
                        updateCountTotal += updateCount;
                    }
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                    resultSet = statement.getResultSet();
                }
            } while (ret);

            if (log.isDebugEnabled()) {
                log.debug(sql + " : " + updateCountTotal + " rows affected");
            }
            Connection conn = dataSource.getConnection();
            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                log.debug(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32") || e.getSQLState().equals("42710")) {
                // eliminating the table already exception for the derby and DB2 database types
                //if (log.isDebugEnabled()) {
                log.info("Table Already Exists. Hence, skipping table creation");
                //}
            } else {
                throw new LCManagerDatabaseException("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing result set.", e);
                }
            }
        }
    }

    /**
     * Checks whether database tables are created.
     *
     * @return <code>true</core> if checkSQL is success, else <code>false</code>.
     */
    private boolean isDatabaseStructureCreated() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Running a query to test the database tables existence.");
            }
            //check whether the tables are already created with a query
            Connection conn = dataSource.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(SQLConstants.DB_CHECK_SQL);
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } finally {
                    if (conn != null) {
                        conn.close();
                    }
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
}
