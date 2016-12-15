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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;

public class DBScriptRunnerUtil {

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


    private static void executeSQL(String sql, Connection connection) throws Exception {
        // Check and ignore empty statements
        sql = sql.trim();
        for (String query : sql.split(";")){

            if ("".equals(query)) {
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                int success = statement.executeUpdate();
            }
        }
    }
}
