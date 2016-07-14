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
import org.wso2.carbon.apimgt.impl.dao.constants.*;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is a factory class, which responsible to load relevant DAO class according to current jdbc Driver.
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

    public static void initializeSQLConstantManager() {

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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static String getSQlString(String sql) {

        Class<?> c = null;

        if ("h2mysql".equals(dbType)) {
            c = sqlConstantsH2MySQL.getClass();
        } else if ("mssql".equals(dbType)) {
            c = sqlConstantsMSSQL.getClass();
        } else if ("db2".equals(dbType)) {
            c = sqlConstantsDB2.getClass();
        }else if ("postgre".equals(dbType)) {
            c = sqlConstantPostgreSQL.getClass();
        }
        else if ("oracle".equals(dbType)) {
            c = sqlConstantOracle.getClass();
        }else{
            log.error("No DB type Found ");
        }
        Field f = null;
        String valueOfMyColor = null;
        try {
            f = c.getDeclaredField(sql);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        f.setAccessible(true);

        try {
            valueOfMyColor = (String) f.get(sqlConstantsH2MySQL);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return valueOfMyColor;

    }

}
