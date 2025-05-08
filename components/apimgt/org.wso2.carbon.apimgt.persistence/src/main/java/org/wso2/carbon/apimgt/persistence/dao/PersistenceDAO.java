package org.wso2.carbon.apimgt.persistence.dao;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.utils.PersistanceDBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceDAO {
    private static final Log log = LogFactory.getLog(PersistenceDAO.class);
    private static PersistenceDAO INSTANCE = null;

    private PersistenceDAO() {

    }

    public static PersistenceDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PersistenceDAO();
        }

        return INSTANCE;
    }

    public int addAPISchema(String uuid, String jsonString, Organization org) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int apiSchemaId = -1;

        String query = SQLConstants.ADD_API_SCHEMA_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, jsonString);
            prepStmt.setString(2, uuid);
            prepStmt.setString(3, org.getName());

            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiSchemaId = rs.getInt(1);
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the add API schema: " + apiSchemaId, ex);
            }
            handleException("Error while adding the API schema: " + apiSchemaId + " to the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return apiSchemaId;
    }

    public List<String> searchAPISchema(String searchQuery, String tenantDomain, int start, int offset) throws SQLException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> apiSchemas = new ArrayList<>();

        String query = SQLConstants.GET_ALL_API_SCHEMA_SQL;

        if (!searchQuery.isEmpty()) {
            query = SQLConstants.SEARCH_API_SCHEMA_SQL;
        }

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tenantDomain);

            if (!searchQuery.isEmpty()) {
                prepStmt.setString(2, "%" + searchQuery.toLowerCase() + "%");
                prepStmt.setInt(3, start);
                prepStmt.setInt(4, offset);
            } else {
                prepStmt.setInt(2, start);
                prepStmt.setInt(3, offset);
            }

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String schemaJson = rs.getString("API_SCHEMA");
                apiSchemas.add(schemaJson);
            }

            return apiSchemas;

        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }

    public int getAllAPICount(String tenantDomain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        int count = 0;

        String query = SQLConstants.GET_ALL_API_COUNT;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tenantDomain);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("TOTAL_API_COUNT");
            }

        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return count;
    }

    public String getAPISchemaByUUID(String uuid, String tenantDomain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String apiSchema = null;

        String query = SQLConstants.GET_API_BY_UUID_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, uuid);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                apiSchema = rs.getString("API_SCHEMA");
            }

        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return apiSchema;
    }

    public String getSwaggerDefinitionByUUID(String uuid, String tenantDomain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String swaggerDefinition = null;

        String query = SQLConstants.GET_SWAGGER_DEFINITION_BY_UUID_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, uuid);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                swaggerDefinition = rs.getString("SWAGGER_DEFINITION");
            }

        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return swaggerDefinition;
    }

    public List<String> searchAPISchemaContent(String searchQuery, String tenantDomain) throws SQLException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> apiSchemas = new ArrayList<>();

        String query = SQLConstants.SEARCH_API_SCHEMA_CONTENT;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, searchQuery.toLowerCase());

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String schemaJson = rs.getString("API_SCHEMA");
                apiSchemas.add(schemaJson);
            }

            return apiSchemas;

        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
