package org.wso2.carbon.apimgt.perf.populator;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.String;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SubscriptionPopulator {
    
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String URL = "jdbc:mysql://localhost:3306/wso2am2";
    public static final String USER = "root";
    public static final String PASS = "root123";

	public static void main(String[] args) {

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        
        for (int i = 0; i < 1000; i++) {
            String userId = "developer" + (i + 1);
            System.out.println("Adding metadata for user: " + userId);
            int subscriberId = addSubscriber(userId);
            int applicationId = addApplication("Application" + (i + 1), subscriberId);
            addSubscription(applicationId, i + 1);
            addSubscription(applicationId, i + 1 + 1000);
            addKey(userId, applicationId);
        }

        int userIndex = 0;
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 100; j++) {
                String userId = "enduser" + (userIndex + 1);
                System.out.println("Adding key for end user: " + userId);
                addUserKey("developer" + (i+1) + "_key", userId);
                userIndex++;
            }
        }
	}
    
    public static void addUserKey(String consumerKey, String userId) {
        String INSERT_TOKEN_SQL = "INSERT" +
                " INTO IDN_OAUTH2_ACCESS_TOKEN (ACCESS_TOKEN, CONSUMER_KEY, TOKEN_STATE, TOKEN_SCOPE, AUTHZ_USER) " +
                " VALUES (?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            stmt = conn.prepareStatement(INSERT_TOKEN_SQL);
            stmt.setString(1, userId + "_token");
            stmt.setString(2, consumerKey);
            stmt.setString(3, "ACTIVE");
            stmt.setString(4, "PRODUCTION");
            stmt.setString(5, userId);
            stmt.execute();
        } catch (SQLException e)  {
            e.printStackTrace();
        } finally {
            closeConnection(conn, stmt, rs);
        }
    }

    public static void addKey(String userId, int applicationId) {
        String INSERT_CONSUMER_APP_SQL = "INSERT INTO IDN_OAUTH_CONSUMER_APPS " +
                "(CONSUMER_KEY, CONSUMER_SECRET, USERNAME, TENANT_ID, OAUTH_VERSION) VALUES (?,?,?,?,?) ";
        String INSERT_TOKEN_SQL = "INSERT" +
                " INTO IDN_OAUTH2_ACCESS_TOKEN (ACCESS_TOKEN, CONSUMER_KEY, TOKEN_STATE, TOKEN_SCOPE, AUTHZ_USER) " +
                " VALUES (?,?,?,?,?)";
        String INSERT_MAPPING_SQL = "INSERT " +
                "INTO AM_APPLICATION_KEY_MAPPING (APPLICATION_ID, CONSUMER_KEY, KEY_TYPE) " +
                "VALUES (?,?,?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            //conn.setAutoCommit(false);
            stmt = conn.prepareStatement(INSERT_CONSUMER_APP_SQL);
            stmt.setString(1, userId + "_key");
            stmt.setString(2, userId + "_secret");
            stmt.setString(3, userId);
            stmt.setInt(4, -1234);
            stmt.setString(5, "OAuth-1.0a");
            stmt.execute();

            stmt = conn.prepareStatement(INSERT_TOKEN_SQL);
            stmt.setString(1, userId + "_token");
            stmt.setString(2, userId + "_key");
            stmt.setString(3, "ACTIVE");
            stmt.setString(4, "PRODUCTION");
            stmt.setString(5, userId);
            stmt.execute();

            stmt = conn.prepareStatement(INSERT_MAPPING_SQL);
            stmt.setInt(1, applicationId);
            stmt.setString(2, userId + "_key");
            stmt.setString(3, "PRODUCTION");
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, stmt, rs);
        }
    }

    public static int addSubscription(int applicationId, int apiId) {
        int subscriptionId = -1;
        String INSERT_SUBSCRIPTIONS_SQL = "INSERT INTO AM_SUBSCRIPTION ( TIER_ID , API_ID , APPLICATION_ID) VALUES (?,?,?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            stmt = conn.prepareStatement(INSERT_SUBSCRIPTIONS_SQL, new String[]{"SUBSCRIPTION_ID"});
            stmt.setString(1, "Unlimited");
            stmt.setInt(2, apiId);
            stmt.setInt(3, applicationId);
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                subscriptionId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, stmt, rs);
        }
        return subscriptionId;
    }
    
    public static int addApplication(String name, int subscriberId) {
        int applicationId = -1;
        String INSERT_APPLICATIONS_SQL = "INSERT INTO AM_APPLICATION (NAME,SUBSCRIBER_ID) VALUES (?,?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            stmt = conn.prepareStatement(INSERT_APPLICATIONS_SQL, new String[]{"APPLICATION_ID"});
            stmt.setString(1, name);
            stmt.setInt(2, subscriberId);
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, stmt, rs);
        }
        return applicationId;
    }
    
    public static int addSubscriber(String userId) {
        int subscriberId = -1;
        String INSERT_SUBSCRIBER_SQL = "INSERT INTO AM_SUBSCRIBER (USER_ID,TENANT_ID,DATE_SUBSCRIBED) VALUES(?,?,NOW())";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            stmt = conn.prepareStatement(INSERT_SUBSCRIBER_SQL, new String[]{"SUBSCRIBER_ID"});
            stmt.setString(1, userId);
            stmt.setInt(2, -1234);
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                subscriberId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(conn, stmt, rs);
        }
        return subscriberId;
    }
    
    private static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                
            }
        }
    }

}
