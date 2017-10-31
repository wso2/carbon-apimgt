/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DBScriptRunnerUtil {
    private static final Logger log = LoggerFactory.getLogger(DBScriptRunnerUtil.class);

    public static void executeSQLScript(String dbscriptPath, Connection connection) throws Exception {
        StringBuffer sql = new StringBuffer();

        try (InputStream is = new FileInputStream(dbscriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if ("REM".equalsIgnoreCase(token)) {
                        continue;
                    }
                }

                sql.append(" ").append(line);

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (line.indexOf("--") >= 0) {
                    sql.append('\n');
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
                executeSQL(sql.toString(), connection);
            }
        }
    }

    private static void executeSQL(String sql, Connection connection) {
        // Check and ignore empty statements
        String delimiter = ";";
        String dbType;
        try {
            dbType = connection.getMetaData().getDriverName();
        } catch (SQLException e) {
            log.error("Could not get DB Type", e);
            return;
        }

        if (dbType.contains("Oracle")) {
            delimiter = "/";
        }
        sql = sql.trim();
        List<String> oraclePLSQL = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            for (String query : sql.split(delimiter)) {
                if ("".equals(query)) {
                    return;
                } else if (dbType.contains("Oracle") && query.contains("API_DATASTORE")) { // execute PL/SQL separately
                    oraclePLSQL.add(query);
                    continue;
                }
                statement.execute(query);
            }
        } catch (SQLException e) {
            log.error("Error when executing SQL statement", e);
        }

        // Special logic to execute oracle PL/SQL command
        if (dbType.contains("Oracle")) {
            for (String query : oraclePLSQL) {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute(query);
                } catch (SQLException e) {
                    log.error("Error while executing oracle PL/SQL commands.", e);
                }

            }

            String q = "select index_name,index_type,status,domidx_status,domidx_opstatus from user_indexes "
                    + "where index_type like '%DOMAIN%' and (domidx_status <> 'VALID' or domidx_opstatus <> 'VALID')";
            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(q);) {
                while (rs.next()) {
                    if ("API_INDEX".equals(rs.getString("index_name"))) { // re build index if it has failed.
                        String rebuild = "alter index API_INDEX rebuild";
                        statement.execute(rebuild);
                    }
                }
            } catch (SQLException e) {
                log.error("Error when rebuilding Oracle indexes", e);
            }
        }
    }
}
