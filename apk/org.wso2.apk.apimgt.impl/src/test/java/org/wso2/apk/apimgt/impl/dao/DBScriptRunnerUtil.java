package org.wso2.apk.apimgt.impl.dao;

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
        sql = sql.trim();
        try (Statement statement = connection.createStatement()) {
            //todo: need to fix this properly
            String function = "CREATE OR REPLACE FUNCTION update_modified_column()\n" +
                    "RETURNS TRIGGER AS $$\n" +
                    "BEGIN\n" +
                    "    NEW.TIME_STAMP= now();\n" +
                    "    RETURN NEW;\n" +
                    "END;\n" +
                    "$$ language 'plpgsql';";
            statement.execute(function);
            for (String query : sql.split(delimiter)) {
                if ("".equals(query)) {
                    return;
                }
                statement.execute(query);
            }
        } catch (SQLException e) {
            log.error("Error when executing SQL statement", e);
        }
    }
}
