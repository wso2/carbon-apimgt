package org.wso2.carbon.apimgt.persistence.dao;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.persistence.dto.DocumentResult;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.utils.PersistanceDBUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void addAPISchema(String uuid, String metadata, String apiDefinition, String org) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int apiSchemaId = -1;

        String query = SQLConstants.ADD_ARTIFACT_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, "API");
            prepStmt.setString(2, org);
            prepStmt.setString(3, metadata);
            prepStmt.setString(4, UUID.randomUUID().toString());
            prepStmt.setString(5, uuid);

            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiSchemaId = rs.getInt(1);
            }

            prepStmt.setString(1, "API_DEFINITION");
            prepStmt.setString(3, apiDefinition);
            prepStmt.setString(4, UUID.randomUUID().toString());

            prepStmt.execute();

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
    }

    public List<String> searchAPISchema(String searchQuery, String org, int start, int offset) throws SQLException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> apiSchemas = new ArrayList<>();

        String query = SQLConstants.GET_ALL_API_ARTIFACT_SQL;

        if (!searchQuery.isEmpty()) {
            query = SQLConstants.SEARCH_API_ARTIFACT_SQL;
        }

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, org);

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
                String schemaJson = rs.getString("metadata");
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
            prepStmt.setString(1, uuid);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                apiSchema = rs.getString("metadata");
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
                swaggerDefinition = rs.getString("metadata");
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

    public String addAPIDocumentation(String apiUuid, String metadata, String org) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String docId = UUID.randomUUID().toString();

        String query = SQLConstants.ADD_ARTIFACT_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, "DOCUMENTATION");
            prepStmt.setString(2, org);
            prepStmt.setString(3, metadata);
            prepStmt.setString(4, docId);
            prepStmt.setString(5, apiUuid);

            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API documentation to the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }

        return docId;
    }

    public DocumentResult getDocumentation(String docId, String org) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        DocumentResult documentation = null;

        String query = SQLConstants.GET_DOCUMENTATION_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, org);
            prepStmt.setString(2, docId);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                String metadata = rs.getString("metadata");
                String uuid = rs.getString("uuid");
                String createdTime = rs.getString("created_time");
                String lastUpdatedTime = rs.getString("last_modified");
                documentation = new DocumentResult(metadata, uuid, createdTime, lastUpdatedTime);
            }

        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation from the database", e);
        }
        finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return documentation;
    }

    public List<DocumentResult> searchDocumentation(String apiUuid, String org, String searchQuery, int start, int offset) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<DocumentResult> documentationList = new ArrayList<>();

        String query = SQLConstants.SEARCH_DOCUMENTATION_SQL;

        if (searchQuery == null || searchQuery.isEmpty()) {
            query = SQLConstants.GET_ALL_DOCUMENTATION_SQL;
        }
        
        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);

            if (searchQuery == null || searchQuery.isEmpty()) {
                prepStmt.setString(1, org);
                prepStmt.setString(2, apiUuid);
                prepStmt.setInt(3, start);
                prepStmt.setInt(4, offset);
            } else {
                prepStmt.setString(1, org);
                prepStmt.setString(2, apiUuid);
                prepStmt.setString(3, "%" + searchQuery.toLowerCase() + "%");
                prepStmt.setInt(4, start);
                prepStmt.setInt(5, offset);
            }

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String metadata = rs.getString("metadata");
                String uuid = rs.getString("uuid");
                String createdTime = rs.getString("created_time");
                String lastUpdatedTime = rs.getString("last_modified");
                DocumentResult documentation = new DocumentResult(metadata, uuid, apiUuid, createdTime, lastUpdatedTime);
                documentationList.add(documentation);
            }

        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation from the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return documentationList;
    }

    public int getDocumentationCount(String apiUuid, String org) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int count = 0;

        String query = SQLConstants.GET_DOCUMENTATION_COUNT;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, org);
            prepStmt.setString(2, apiUuid);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("TOTAL_DOC_COUNT");
            }

        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation from the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return count;
    }

    public void addDocumentationFile(String docId, String apiId, ResourceFile resourceFile) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        InputStream fileStream = resourceFile.getContent();

        String query = SQLConstants.ADD_DOCUMENTATION_FILE_SQL;
        String metadataQuery = SQLConstants.ADD_METADATA_FOR_FILE_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            // Read stream to byte array
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();
            int fileLength = bytes.length;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setBinaryStream(1, byteArrayInputStream, fileLength);
            prepStmt.setString(2, docId);

            prepStmt.execute();

            prepStmt = connection.prepareStatement(metadataQuery);
            prepStmt.setString(1, resourceFile.getContentType());
            prepStmt.setString(2, resourceFile.getName());
            prepStmt.setString(3, docId);

            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API documentation file to the database", e);
        } catch (IOException e) {
            handleException("Error while reading the API documentation file", e);
        }finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public void addDocumentationContent(String docId, String apiId, String content) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_DOCUMENTATION_CONTENT_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, content);
            prepStmt.setString(2, docId);

            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API documentation content to the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    public String getDocumentationContent(String docId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String content = null;

        String query = SQLConstants.GET_DOCUMENTATION_CONTENT_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, docId);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                content = rs.getString("metadata");
            }

        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation content from the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return content;
    }

    public InputStream getDocumentationFileContent(String docId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        InputStream content = null;

        String query = SQLConstants.GET_DOCUMENTATION_FILE_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, docId);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                content = rs.getBinaryStream("artifact");
            }

        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation file from the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return content;
    }

    public void deleteDocumentation(String docId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        String query = SQLConstants.DELETE_DOCUMENTATION_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, docId);

            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API documentation from the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
