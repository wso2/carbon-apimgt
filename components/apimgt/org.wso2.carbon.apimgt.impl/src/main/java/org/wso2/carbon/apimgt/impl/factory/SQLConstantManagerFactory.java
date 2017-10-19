/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.constants.*;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is a factory class, which responsible to load relevant constants class according to current jdbc Driver.
 * We will keep separate sql class
 */
public class SQLConstantManagerFactory {

    private static Log log = LogFactory.getLog(KeyManagerHolder.class);
    private static SQLConstants sqlConstants = null;
    private static SQLConstantsH2MySQL sqlConstantsH2MySQL = null;
    private static SQLConstantsMSSQL sqlConstantsMSSQL = null;
    private static SQLConstantsDB2 sqlConstantsDB2 = null;
    private static SQLConstantPostgreSQL sqlConstantPostgreSQL = null;
    private static SQLConstantOracle sqlConstantOracle = null;
    private static String dbType = null;

    /**
     * This method initialize when server start up. And select relevant DB dirver and load the const class.
     * @throws APIManagementException
     */
    public static void initializeSQLConstantManager() throws APIManagementException {

        Connection connection = null;

        try {
            connection = APIMgtDBUtil.getConnection();
            if (connection.getMetaData().getDriverName().contains("MySQL") || connection.getMetaData().getDriverName()
                    .contains("H2")) {
                sqlConstantsH2MySQL = new SQLConstantsH2MySQL();
                dbType = "h2mysql";

            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                sqlConstantsDB2 = new SQLConstantsDB2();
                dbType = "db2";

            } else if (connection.getMetaData().getDriverName().contains("MS SQL")) {
                dbType = "mssql";
                sqlConstantsMSSQL = new SQLConstantsMSSQL();
            } else if (connection.getMetaData().getDriverName().contains("Microsoft")) {
                dbType = "mssql";
                sqlConstantsMSSQL = new SQLConstantsMSSQL();
            } else if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
                dbType = "postgre";
                sqlConstantPostgreSQL = new SQLConstantPostgreSQL();
            } else if (connection.getMetaData().getDriverName().contains("Oracle")) {
                dbType = "oracle";
                sqlConstantOracle = new SQLConstantOracle();
            }else{
                log.error("Could not find DB type to load constants");
                throw new APIManagementException("Error occurred while initializing SQL Constants Manager");
            }
        } catch (SQLException e) {
            log.error("Error occurred while initializeSQLConstantManager");
            throw new APIManagementException("Error occurred while initializing SQL Constants Manager", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, connection, null);
        }

    }

    /**
     * This method will return the class's constant field's value by given string.
     * @param sql Sql constant name
     * @return sql string according to the database.
     */
    public static String getSQlString(String sql) throws APIManagementException {
        String sqlString = null;
        try {
            Class<?> clazz = null;

            if ("h2mysql".equals(dbType)) {
                clazz = sqlConstantsH2MySQL.getClass();
            } else if ("mssql".equals(dbType)) {
                clazz = sqlConstantsMSSQL.getClass();
            } else if ("db2".equals(dbType)) {
                clazz = sqlConstantsDB2.getClass();
            } else if ("postgre".equals(dbType)) {
                clazz = sqlConstantPostgreSQL.getClass();
            } else if ("oracle".equals(dbType)) {
                clazz = sqlConstantOracle.getClass();
            } else {
                String errorMsg = "No DB type Found";
                log.error(errorMsg);
                throw new APIManagementException(errorMsg);
            }
            Field field;

            field = clazz.getDeclaredField(sql);
            field.setAccessible(true);
            sqlString = (String) field.get(sqlConstantsH2MySQL);
        }catch (IllegalAccessException e) {
            log.error("Illegal Access attempt to sql constant class");
            throw new APIManagementException("Illegal Access attempt to sql constant class", e);
        } catch (NoSuchFieldException e) {
            log.error("No such a field found in sql constant class" + sql);
            throw new APIManagementException("No such a field found in sql constant class " + sql, e);
        }
        return sqlString;

    }

}
