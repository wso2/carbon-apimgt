/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.perf.populator;


import java.sql.*;

public class UserPopulator {

    public static void main(String args[]) {
        int userCount = 1000;
        String userPrefix = "developer";

        String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/wso2am2";
        String USER = "root";
        String PASS = "root123";

        String ADD_USER_SQL =
                "INSERT INTO UM_USER " +
                        "(UM_USER_NAME ,UM_USER_PASSWORD ,UM_SALT_VALUE ,UM_TENANT_ID ) " +
                        "VALUES " +
                        "(?,?,?,?)";
        String ADD_ROLE_SQL = "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID ,UM_TENANT_ID )  VALUES (?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(ADD_USER_SQL, new String[]{"SUBSCRIPTION_ID"});

            for (int a = 0; a < userCount; a++) {
                ps.setString(1, userPrefix + a);
                ps.setString(2, "Mx5VpNXKTxHv2WrWUPj7iUR9IfWs4qN18xYUV3sxo94=");
                ps.setString(3, "Q3En4ZN+pVGk6UYkTx6SDQ==");
                ps.setInt(4, -1234);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.clearBatch();
            System.out.println("Added users");

            //Add Admin role to users
            ResultSet rs = ps.getGeneratedKeys();
            ps = conn.prepareStatement(ADD_ROLE_SQL);
            int userId;
            while (rs.next()) {
                userId = Integer.valueOf(rs.getString(1)).intValue();
                System.out.println(userId);
                ps.setInt(1, 1); //admin
                ps.setInt(2, userId);
                ps.setInt(3, -1234);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            System.out.println("Added roles");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

        }

    }
}
