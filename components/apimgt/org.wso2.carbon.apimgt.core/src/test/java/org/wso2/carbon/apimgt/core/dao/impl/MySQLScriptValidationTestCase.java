/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class MySQLScriptValidationTestCase {

    private static final String dbscriptPath = ".." + File.separator + ".." + File.separator + ".." + File.separator
                + "features" + File.separator + "apimgt" + File.separator
                + "org.wso2.carbon.apimgt.core.feature" + File.separator + "resources"
            + File.separator + "dbscripts" + File.separator + "mysql.sql";


    @Test(description = "Test if UTF8 character set has been applied to all tables in script")
    public void testCharacterSetUTF8() throws Exception {
        try (InputStream is = new FileInputStream(dbscriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"))) {

            final String createTableSyntax = "CREATE TABLE";
            final char openingBrace = '(';
            final String utf8CharSet = "CHARACTER SET utf8 COLLATE utf8_general_ci";
            final String endOfStatement = ";";

            Deque<String> tables = new ArrayDeque<>();
            Map<String, Boolean> tableCharsetUTF8 = new HashMap<>();

            boolean isCharsetDetected = false;
            boolean isTableDetected = false;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(createTableSyntax)) {
                    int startOfCreateTable = line.indexOf(createTableSyntax);
                    int startOfOpeningBrace = line.indexOf(openingBrace);

                    tables.add(line.substring(startOfCreateTable + createTableSyntax.length(),
                                            startOfOpeningBrace).trim());
                    isTableDetected = true;
                }

                if (line.contains(utf8CharSet) && isTableDetected) {
                    isCharsetDetected = true;
                }

                if (line.contains(endOfStatement)) {
                    if (isTableDetected) {
                        tableCharsetUTF8.put(tables.getLast(), isCharsetDetected);
                    }

                    isCharsetDetected = false;
                    isTableDetected = false;
                }
            }

            for (Map.Entry<String, Boolean> entry : tableCharsetUTF8.entrySet()) {
                Assert.assertTrue(entry.getValue(), "Table " + entry.getKey() +
                        " has been defined without " + utf8CharSet + " in mysql db script");
            }
        }

    }
}
