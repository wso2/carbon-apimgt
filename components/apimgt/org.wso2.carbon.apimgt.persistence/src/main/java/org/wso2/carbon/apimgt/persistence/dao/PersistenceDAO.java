package org.wso2.carbon.apimgt.persistence.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.persistence.dao.queries.SQLQueryFactory;
import org.wso2.carbon.apimgt.persistence.dao.queries.SQLQueryInterface;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceDBUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class PersistenceDAO {
    private static final Log log = LogFactory.getLog(PersistenceDAO.class);
    private static PersistenceDAO INSTANCE = null;
    private final SQLQueryInterface SQLQuery;

    private PersistenceDAO() {
        this.SQLQuery = SQLQueryFactory.getSQLQueries();
    }

    public static PersistenceDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PersistenceDAO();
        }
        return INSTANCE;
    }

    public void addAPISchema(String uuid, String metadata, String org) throws APIManagementException {
        String query = SQLQuery.getAddArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                prepStmt.setString(1, "API");
                prepStmt.setString(2, org);
                prepStmt.setString(3, metadata);
                prepStmt.setString(4, UUID.randomUUID().toString());
                prepStmt.setString(5, uuid);
                prepStmt.execute();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Error while adding the API schema: " + uuid + " to the database", e);
            }
        } catch (SQLException e) {
            handleException("Error while adding the API schema: " + uuid + " to the database", e);
        }
    }

    public void addSwaggerDefinition(String uuid, String metadata, String org) throws APIManagementException {
        String query = SQLQuery.getAddArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "API_DEFINITION");
            prepStmt.setString(2, org);
            prepStmt.setString(3, metadata);
            prepStmt.setString(4, UUID.randomUUID().toString());
            prepStmt.setString(5, uuid);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the Swagger definition to the database", e);
        }
    }

    public void addAsyncDefinition(String uuid, String asyncApiDefinition, String orgJsonString) throws APIManagementException {
        String query = SQLQuery.getAddArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "ASYNC_API_DEFINITION");
            prepStmt.setString(2, orgJsonString);
            prepStmt.setString(3, asyncApiDefinition);
            prepStmt.setString(4, UUID.randomUUID().toString());
            prepStmt.setString(5, uuid);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the Async API definition to the database", e);
        }
    }

    public int getAllAPICount(String tenantDomain, String[] roles) throws SQLException {
        int count = 0;
        if (roles == null || roles.length == 0) {
            return count; // No roles provided, return count as 0
        }

        String query = SQLQuery.getAllApiCountSql(roles);
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, tenantDomain);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("TOTAL_API_COUNT");
                }
            }
        }
        return count;
    }

    public SearchResult getAllPIs(String org, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int totalCount = 0;
        if (roles == null || roles.length == 0) {
            return new SearchResult(totalCount, apiResults); // No roles provided, return empty list
        }

        String query = SQLQuery.getAllApiArtifactSql(roles);
        String countQuery = SQLQuery.getAllApiCountSql(roles);
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setInt(2, start);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String schemaJson = rs.getString("metadata");
                    apiResults.add(schemaJson);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API artifacts from the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            countStmt.setString(1, org);
            countStmt.setString(2, org);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) {
                    totalCount = rs.getInt("TOTAL_API_COUNT");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API count from the database", e);
        }
        return new SearchResult(totalCount, apiResults);
    }

    public String getAPISchemaByUUID(String uuid, String tenantDomain) throws APIManagementException {
        String apiSchema = null;
        String query = SQLQuery.getGetAPIByUUIDSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiSchema = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API schema for UUID: " + uuid, e);
        }
        return apiSchema;
    }

    public String getSwaggerDefinitionByUUID(String uuid, String tenantDomain) throws APIManagementException {
        String swaggerDefinition = null;
        String query = SQLQuery.getGetSwaggerDefinitionByUUIDSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    swaggerDefinition = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the Swagger definition for UUID: " + uuid, e);
        }
        return swaggerDefinition;
    }

    public String addAPIDocumentation(String apiUuid, String metadata, String org) throws APIManagementException {
        String docId = UUID.randomUUID().toString();
        String query = SQLQuery.getAddArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "DOCUMENTATION");
            prepStmt.setString(2, org);
            prepStmt.setString(3, metadata);
            prepStmt.setString(4, docId);
            prepStmt.setString(5, apiUuid);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API documentation to the database", e);
        }
        return docId;
    }

    public DocumentResult getDocumentation(String docId, String org) throws APIManagementException {
        DocumentResult documentation = null;
        String query = SQLQuery.getGetDocumentationSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, docId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String uuid = rs.getString("uuid");
                    String createdTime = rs.getString("created_time");
                    String lastUpdatedTime = rs.getString("last_modified");
                    documentation = new DocumentResult(metadata, uuid, createdTime, lastUpdatedTime);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation from the database", e);
        }
        return documentation;
    }

    public List<DocumentResult> searchDocumentation(String apiUuid, String org, String searchQuery, int start, int offset) throws APIManagementException {
        List<DocumentResult> documentationList = new ArrayList<>();
        String query = SQLQuery.getSearchDocumentationSQL();
        if (searchQuery == null || searchQuery.isEmpty()) {
            query = SQLQuery.getGetAllDocumentationSQL();
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            if (searchQuery == null || searchQuery.isEmpty()) {
                prepStmt.setString(1, org);
                prepStmt.setString(2, apiUuid);
                prepStmt.setInt(3, start);
                prepStmt.setInt(4, offset);
            } else {
                prepStmt.setString(1, org);
                prepStmt.setString(2, apiUuid);
                prepStmt.setString(3, searchQuery.toLowerCase());
                prepStmt.setInt(4, start);
                prepStmt.setInt(5, offset);
            }
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String uuid = rs.getString("uuid");
                    String createdTime = rs.getString("created_time");
                    String lastUpdatedTime = rs.getString("last_modified");
                    DocumentResult documentation = new DocumentResult(metadata, uuid, apiUuid, createdTime, lastUpdatedTime);
                    documentationList.add(documentation);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation from the database", e);
        }
        return documentationList;
    }

    public int getDocumentationCount(String apiUuid, String org) throws APIManagementException {
        int count = 0;
        String query = SQLQuery.getGetDocumentationCount();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, apiUuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("TOTAL_DOC_COUNT");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation from the database", e);
        }
        return count;
    }

    public void addDocumentationFile(String docId, String apiId, ResourceFile resourceFile, String fileTextContent) throws APIManagementException {
        String query = SQLQuery.getAddDocumentationFileSQL();
        String metadataQuery = SQLQuery.getAddMetadataForFileSQL();

        try (Connection connection = PersistenceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            // Read stream to byte array
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            try (InputStream fileStream = resourceFile.getContent()) {
                while ((nRead = fileStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
            }
            byte[] bytes = buffer.toByteArray();
            int fileLength = bytes.length;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setBinaryStream(1, byteArrayInputStream, fileLength);
                prepStmt.setString(2, docId);
                prepStmt.execute();
            }
            try (PreparedStatement prepStmt = connection.prepareStatement(metadataQuery)) {
                prepStmt.setString(1, resourceFile.getContentType());
                prepStmt.setString(2, resourceFile.getName());
                prepStmt.setString(3, fileTextContent);
                prepStmt.setString(4, docId);
                prepStmt.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API documentation file to the database", e);
        } catch (IOException e) {
            handleException("Error while reading the API documentation file", e);
        }
    }

    public void addDocumentationContent(String docId, String apiId, String content) throws APIManagementException {
        String query = SQLQuery.getAddDocumentationContentSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, content);
            prepStmt.setString(2, docId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API documentation content to the database", e);
        }
    }

    public String getDocumentationContent(String docId) throws APIManagementException {
        String content = null;
        String query = SQLQuery.getGetDocumentationContentSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, docId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    content = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation content from the database", e);
        }
        return content;
    }

    public InputStream getDocumentationFileContent(String docId) throws APIManagementException {
        InputStream content = null;
        String query = SQLQuery.getGetDocumentationFileSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, docId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    content = rs.getBinaryStream("artifact");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API documentation file from the database", e);
        }
        return content;
    }

    public void saveOASDefinition(String apiId, String oasDefinition) throws APIManagementException {
        String query = SQLQuery.getSaveOASDefinitionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, oasDefinition);
            prepStmt.setString(2, apiId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while saving the OAS definition to the database", e);
        }
    }

    public void saveAsyncAPIDefinition(String apiId, String asyncApiDefinition) throws APIManagementException {
        String query = SQLQuery.getSaveAsyncAPIDefinitionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, asyncApiDefinition);
            prepStmt.setString(2, apiId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while saving the Async API definition to the database", e);
        }
    }

    public String getAsyncAPIDefinitionByUUID(String apiId, String org) throws APIManagementException {
        String asyncApiDefinition = null;
        String query = SQLQuery.getGetAsyncAPIDefinitionByUUIDSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    asyncApiDefinition = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the Async API definition from the database", e);
        }
        return asyncApiDefinition;
    }

    public void deleteDocumentation(String docId) throws APIManagementException {
        String query = SQLQuery.getDeleteDocumentationSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, docId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API documentation from the database", e);
        }
    }

    public void deleteAPISchema(String apiId, String name) throws APIManagementException {
        String query = SQLQuery.getDeleteAPISchemaSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API schema from the database", e);
        }
    }

    public void addThumbnail(String apiId, String org, InputStream fileStream, String metadata) throws APIManagementException {
        String query = SQLQuery.getAddFileArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();
            int fileLength = bytes.length;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setString(1, "THUMBNAIL");
                prepStmt.setString(2, org);
                prepStmt.setString(3, metadata);
                prepStmt.setString(4, UUID.randomUUID().toString());
                prepStmt.setBinaryStream(5, byteArrayInputStream, fileLength);
                prepStmt.setString(6, apiId);
                prepStmt.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API thumbnail to the database", e);
        } catch (IOException e) {
            handleException("Error while reading the API thumbnail file", e);
        }
    }

    public FileResult getThumbnail(String apiId, String org) throws APIManagementException {
        FileResult thumbnailResult = null;
        String query = SQLQuery.getGetThumbnailSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    InputStream content = rs.getBinaryStream("artifact");
                    String metadata = rs.getString("metadata");
                    thumbnailResult = new FileResult(content, metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API thumbnail from the database", e);
        }
        return thumbnailResult;
    }

    public FileResult getWSDL(String apiId, String org) throws APIManagementException {
        FileResult wsdlResult = null;
        String query = SQLQuery.getGetWSDLSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    InputStream content = rs.getBinaryStream("artifact");
                    String metadata = rs.getString("metadata");
                    wsdlResult = new FileResult(content, metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the WSDL from the database", e);
        }
        return wsdlResult;
    }

    public void deleteThumbnail(String apiId, String org) throws APIManagementException {
        String query = SQLQuery.getDeleteThumbnailSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, org);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API thumbnail from the database", e);
        }
    }

    public void addWSDL(String apiId, String org, InputStream wsdlStream, String metadata) throws APIManagementException {
        String query = SQLQuery.getAddFileArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = wsdlStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();
            int fileLength = bytes.length;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setString(1, "WSDL");
                prepStmt.setString(2, org);
                prepStmt.setString(3, metadata);
                prepStmt.setString(4, UUID.randomUUID().toString());
                prepStmt.setBinaryStream(5, byteArrayInputStream, fileLength);
                prepStmt.setString(6, apiId);
                prepStmt.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the WSDL to the database", e);
        } catch (IOException e) {
            handleException("Error while reading the WSDL file", e);
        }
    }

    public void addGraphQLSchema(String apiId, String metadata, String org) throws APIManagementException {
        String query = SQLQuery.getAddArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "GRAPHQL_SCHEMA");
            prepStmt.setString(2, org);
            prepStmt.setString(3, metadata);
            prepStmt.setString(4, UUID.randomUUID().toString());
            prepStmt.setString(5, apiId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the GraphQL schema to the database", e);
        }
    }

    public void updateGraphQLSchema(String apiId, String metadata) throws APIManagementException {
        String query = SQLQuery.getUpdateGraphQLSchemaSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, metadata);
            prepStmt.setString(2, apiId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating the GraphQL schema in the database", e);
        }
    }

    public String getGraphQLSchema(String apiId, String org) throws APIManagementException {
        String graphqlSchema = null;
        String query = SQLQuery.getGetGraphQLSchemaSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    graphqlSchema = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the GraphQL schema from the database", e);
        }
        return graphqlSchema;
    }

    public void addAPIRevisionSchema(String apiUUID, String type, int revisionId, String revisionUUID, String metadata, String orgJsonString) throws APIManagementException {
        String query = SQLQuery.getAddAPIRevisionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, type);
            prepStmt.setString(2, orgJsonString);
            prepStmt.setString(3, apiUUID);
            prepStmt.setString(4, revisionUUID);
            prepStmt.setInt(5, revisionId);
            prepStmt.setString(6, metadata);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API revision schema to the database", e);
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }


    public void addAPIRevisionSwaggerDefinition(String apiUUID, int revisionId, String revisionUUID, String swaggerDefinition, String orgJsonString) throws APIManagementException {
        String query = SQLQuery.getAddAPIRevisionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "API_DEFINITION");
            prepStmt.setString(2, orgJsonString);
            prepStmt.setString(3, apiUUID);
            prepStmt.setString(4, revisionUUID);
            prepStmt.setInt(5, revisionId);
            prepStmt.setString(6, swaggerDefinition);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API revision Swagger definition to the database", e);
        }
    }

    public void addAPIRevisionAsyncDefinition(String apiUUID, int revisionId, String revisionUUID, String asyncApiDefinition, String orgJsonString) throws APIManagementException {
        String query = SQLQuery.getAddAPIRevisionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "ASYNC_API_DEFINITION");
            prepStmt.setString(2, orgJsonString);
            prepStmt.setString(3, apiUUID);
            prepStmt.setString(4, revisionUUID);
            prepStmt.setInt(5, revisionId);
            prepStmt.setString(6, asyncApiDefinition);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API revision Async API definition to the database", e);
        }
    }

    public void addAPIRevisionThumbnail(String apiUUID, int revisionId, String revisionUUID, InputStream content, String metadata, String orgJsonString) throws APIManagementException {
        String query = SQLQuery.getAddAPIRevisionArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = content.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();
            int fileLength = bytes.length;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setString(1, "THUMBNAIL");
                prepStmt.setString(2, orgJsonString);
                prepStmt.setString(3, apiUUID);
                prepStmt.setString(4, revisionUUID);
                prepStmt.setInt(5, revisionId);
                prepStmt.setBinaryStream(6, byteArrayInputStream, fileLength);
                prepStmt.setString(7, metadata);
                prepStmt.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API revision thumbnail to the database", e);
        } catch (IOException e) {
            handleException("Error while reading the API revision thumbnail file", e);
        }
    }

    public String getAPIRevisionSchemaById(String revisionUUID, String org) throws APIManagementException {
        String apiSchema = null;
        String query = SQLQuery.getGetAPIRevisionByIdSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, revisionUUID);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiSchema = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API revision schema from the database", e);
        }
        return apiSchema;
    }

    public String getAPIRevisionSwaggerDefinitionById(String revisionUUID, String name) throws APIManagementException {
        String swaggerDefinition = null;
        String query = SQLQuery.getGetAPIRevisionSwaggerDefinitionByIdSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, revisionUUID);
            prepStmt.setString(2, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    swaggerDefinition = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API revision Swagger definition from the database", e);
        }
        return swaggerDefinition;
    }

    public String getAPIRevisionAsyncDefinitionById(String revisionUUID, String org) throws APIManagementException {
        String asyncApiDefinition = null;
        String query = SQLQuery.getGetAPIRevisionAsyncDefinitionByIdSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, revisionUUID);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    asyncApiDefinition = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API revision Async API definition from the database", e);
        }
        return asyncApiDefinition;
    }

    public String getAPILifeCycleStatus(String apiUUID, String org) throws APIManagementException {
        String lifecycleStatus = null;
        String query = SQLQuery.getGetAPILifecycleStatusSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiUUID);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    lifecycleStatus = rs.getString("status");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API lifecycle status from the database", e);
        }
        return lifecycleStatus;
    }

    public void updateAPISchema(String apiUUID, String apiRevisionSchema) throws APIManagementException {
        String query = SQLQuery.getUpdateAPISQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiRevisionSchema);
            prepStmt.setString(2, apiUUID);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating the API schema in the database", e);
        }
    }

    public void updateSwaggerDefinition(String apiUUID, String swaggerRevisionDefinition) throws APIManagementException {
        try {
            String query = SQLQuery.getUpdateSwaggerDefinitionSQL();
            try (Connection connection = PersistenceDBUtil.getConnection();
                 PreparedStatement prepStmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepStmt.setString(1, swaggerRevisionDefinition);
                prepStmt.setString(2, apiUUID);
                prepStmt.execute();
                connection.commit();
            }
        } catch (SQLException e) {
            handleException("Error while updating the API Swagger definition in the database", e);
        }

    }

    public void updateAsyncAPIDefinition(String apiUUID, String asyncAPIRevisionDefinition) throws APIManagementException {
        try {
            String query = SQLQuery.getUpdateAsyncDefinitionSQL();
            try (Connection connection = PersistenceDBUtil.getConnection();
                 PreparedStatement prepStmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepStmt.setString(1, asyncAPIRevisionDefinition);
                prepStmt.setString(2, apiUUID);
                prepStmt.execute();
                connection.commit();
            }
        } catch (SQLException e) {
            handleException("Error while updating the API Async API definition in the database", e);
        }
    }

    public FileResult getAPIRevisionThumbnail(String apiUUID, int revisionId, String revisionUUID, String name) throws APIManagementException {
        FileResult thumbnailResult = null;
        String query = SQLQuery.getGetAPIRevisionThumbnailSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiUUID);
            prepStmt.setInt(2, revisionId);
            prepStmt.setString(3, revisionUUID);
            prepStmt.setString(4, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    InputStream content = rs.getBinaryStream("artifact");
                    String metadata = rs.getString("metadata");
                    thumbnailResult = new FileResult(content, metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API revision thumbnail from the database", e);
        }
        return thumbnailResult;
    }

    public void updateThumbnail(String apiUUID, InputStream content, String metadata) throws APIManagementException {
        String query = SQLQuery.getUpdateThumbnailSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = content.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();
            int fileLength = bytes.length;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            prepStmt.setBinaryStream(1, byteArrayInputStream, fileLength);
            prepStmt.setString(2, metadata);
            prepStmt.setString(3, apiUUID);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating the API revision thumbnail in the database", e);
        } catch (IOException e) {
            handleException("Error while reading the API revision thumbnail file", e);
        }
    }

    public void deleteAPIRevision(String revisionUUID) throws APIManagementException {
        String query = SQLQuery.getDeleteAPIRevisionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, revisionUUID);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API revision from the database", e);
        }
    }

    public void updateDocumentation(String docId, String docJsonString) throws APIManagementException {
        String query = SQLQuery.getUpdateDocumentationSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, docJsonString);
            prepStmt.setString(2, docId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating the API documentation in the database", e);
        }
    }

    public void addAPIProductSchema(String uuid, String apiProductJsonString, String orgJsonString) throws APIManagementException {
        String query = SQLQuery.getAddArtifactSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, "API_PRODUCT");
            prepStmt.setString(2, orgJsonString);
            prepStmt.setString(3, apiProductJsonString);
            prepStmt.setString(4, UUID.randomUUID().toString());
            prepStmt.setString(5, uuid);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding the API product schema to the database", e);
        }
    }

    public void updateAPIProductSchema(String apiId, String apiProductJsonString) throws APIManagementException {
        try {
            String query = SQLQuery.getUpdateAPIProductSQL();
            try (Connection connection = PersistenceDBUtil.getConnection();
                 PreparedStatement prepStmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                prepStmt.setString(1, apiProductJsonString);
                prepStmt.setString(2, apiId);
                prepStmt.execute();
                connection.commit();
            }
        } catch (SQLException e) {
            handleException("Error while updating the API product schema in the database", e);
        }
    }

    public int getAllAPIProductCount(String name) throws APIManagementException {
        int count = 0;
        String query = SQLQuery.getGetAPIProductCountSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the API product count from the database", e);
        }
        return count;
    }

    public void deleteAPIProductSchema(String apiProductId, String name) throws APIManagementException {
        String query = SQLQuery.getDeleteAPIProductSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiProductId);
            prepStmt.setString(2, name);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API product schema from the database", e);
        }
    }

    public void deleteSwaggerDefinition(String apiProductId) throws APIManagementException {
        String query = SQLQuery.getDeleteAPIProductSwaggerDefinitionSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiProductId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while deleting the API product Swagger definition from the database", e);
        }
    }

    public List<DocumentResult> getAllDocuments(String apiId) throws APIManagementException {
        String query = SQLQuery.getGetAllDocumentsForAPISQL();
        List<DocumentResult> documents = new ArrayList<>();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String uuid = rs.getString("uuid");
                    String apiUuid = rs.getString("api_uuid");
                    String createdTime = rs.getString("created_time");
                    String lastUpdatedTime = rs.getString("last_modified");
                    DocumentResult documentResult = new DocumentResult(metadata, uuid, apiUuid, createdTime, lastUpdatedTime);
                    documents.add(documentResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving all documents for the API from the database", e);
        }
        return documents;
    }

    public List<String> getAllAPIRevisionIds(String apiUUID) throws APIManagementException {
        List<String> revisionIds = new ArrayList<>();
        String query = SQLQuery.getGetAllAPIRevisionIdsSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiUUID);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    revisionIds.add(rs.getString("revision_uuid"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving all API revision IDs from the database", e);
        }
        return revisionIds;
    }

    // Search methods can be added here as needed
    public SearchResult searchAPIsByContent(String org, String searchQuery, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByContentSql(roles);
        String countQuery = SQLQuery.searchApiByContentCountSql(roles);
        searchQuery = searchQuery.toLowerCase();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, searchQuery);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by content in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, searchQuery);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by content in the database", e);
        }

        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByName(String org, String name, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByNameSql(roles);
        String countQuery = SQLQuery.searchApiByNameCountSql(roles);
        name = "%" + name.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, name);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by name in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by name in the database", e);
        }

        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByProvider(String org, String provider, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByProviderSql(roles);
        String countQuery = SQLQuery.searchApiByProviderCountSql(roles);
        provider = "%" + provider.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, provider);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by provider in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, provider);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by provider in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByVersion(String org, String version, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByVersionSql(roles);
        String countQuery = SQLQuery.searchApiByVersionCountSql(roles);
        version = "%" + version.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, version);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by version in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, version);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by version in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByContext(String org, String context, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByContextSql(roles);
        String countQuery = SQLQuery.searchApiByContextCountSql(roles);
        context = "%" + context.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, context);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by context in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, context);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by context in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByStatus(String org, String status, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByStatusSql(roles);
        String countQuery = SQLQuery.searchApiByStatusCountSql(roles);
        status = "%" + status.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, status);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by status in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, status);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by status in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByDescription(String org, String description, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByDescriptionSql(roles);
        String countQuery = SQLQuery.searchApiByDescriptionCountSql(roles);
        description = "%" + description.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, description);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by description in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, description);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by description in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByTags(String org, String tags, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByTagsSql(roles);
        String countQuery = SQLQuery.searchApiByTagsCountSql(roles);
        tags = "%" + tags.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, tags);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by tags in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, tags);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by tags in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByCategory(String org, String category, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByApiCategorySql(roles);
        String countQuery = SQLQuery.searchApiByApiCategoryCountSql(roles);
        category = "%" + category.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, category);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by category in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, category);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by category in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public SearchResult searchAPIsByOther(String org, String property, String value, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiResults = new ArrayList<>();
        int apiCount = 0;
        String query = SQLQuery.searchApiByOtherSql(property, roles);
        String countQuery = SQLQuery.searchApiByOtherCountSql(property, roles);
        value = "%" + value.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    apiResults.add(rs.getString("metadata"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by other criteria in the database", e);
        }
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(countQuery)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, value);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiCount = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by other criteria in the database", e);
        }
        return new SearchResult(apiCount, apiResults);
    }

    public List<ContentSearchResult> searchContentByContent(String org, String searchContent, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        searchContent = searchContent.toLowerCase();
        String query = SQLQuery.searchContentByContentSql(roles, searchContent);
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setInt(2, start);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    String uuid = rs.getString("uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId, uuid);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by content in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByName(String org, String name, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByNameSql(roles);
        name = "%" + name.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, name);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by name in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByProvider(String org, String provider, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByProviderSql(roles);
        provider = "%" + provider.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, provider);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by provider in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByVersion(String org, String version, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByVersionSql(roles);
        version = "%" + version.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, version);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by version in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByContext(String org, String context, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByContextSql(roles);
        context = "%" + context.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, context);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by context in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByStatus(String org, String status, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByStatusSql(roles);
        status = "%" + status.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, status);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by status in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByDescription(String org, String description, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByDescriptionSql(roles);
        description = "%" + description.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, description);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by description in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByTags(String org, String tags, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByTagsSql(roles);
        tags = "%" + tags.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, tags);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by tags in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByCategory(String org, String category, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByApiCategorySql(roles);
        category = "%" + category.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, category);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by category in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByOther(String org, String property, String value, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByOtherSql(property, roles);
        value = "%" + value.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, value);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by other criteria in the database", e);
        }
        return apiResults;
    }

    public List<String> searchAPIProductsByContent(String org, String searchContent, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByContentSql(roles);
        searchContent = searchContent.toLowerCase();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, searchContent);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by content in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByName(String org, String name, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByNameSql(roles);
        name = "%" + name.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, name);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by name in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByProvider(String org, String provider, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByProviderSql(roles);
        provider = "%" + provider.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, provider);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by provider in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByVersion(String org, String version, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByVersionSql(roles);
        version = "%" + version.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, version);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by version in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByContext(String org, String context, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByContextSql(roles);
        context = "%" + context.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, context);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by context in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByStatus(String org, String status, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByStatusSql(roles);
        status = "%" + status.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, status);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by status in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByDescription(String org, String description, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByDescriptionSql(roles);
        description = "%" + description.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, description);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by description in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByTags(String org, String tags, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByTagsSql(roles);
        tags = "%" + tags.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, tags);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by tags in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByCategory(String org, String category, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByTagsSql(roles);
        category = "%" + category.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, category);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by category in the database", e);
        }
        return apiProductResults;
    }

    public List<String> searchAPIProductsByOther(String org, String property, String value, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.searchApiProductByOtherSql(property, roles);
        value = "%" + value.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching API Products by other criteria in the database", e);
        }
        return apiProductResults;
    }

    public List<String> getAllApiProducts(String org, int start, int offset, String[] roles) throws APIManagementException {
        List<String> apiProductResults = new ArrayList<>();
        String query = SQLQuery.getAllApiProductSql(roles);
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setInt(2, start);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    apiProductResults.add(metadata);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving all API Products in the database", e);
        }
        return apiProductResults;
    }

    // Keep all DEV_PORTAL related methods unchanged
    public List<ContentSearchResult> getAllAPIsForDevPortal(String org, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.getAllApiArtifactsForDevPortalSql(roles);
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setInt(2, start);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving all APIs for Dev Portal from the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByContentForDevPortal(String org, String searchContent, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByContentForDevPortalSql(roles);
        searchContent = searchContent.toLowerCase();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, searchContent);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by content for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByNameForDevPortal(String org, String name, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByNameForDevPortalSql(roles);
        name = "%" + name.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, name);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by name for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByProviderForDevPortal(String org, String provider, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByProviderForDevPortalSql(roles);
        provider = "%" + provider.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, provider);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by provider for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByVersionForDevPortal(String org, String version, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByVersionForDevPortalSql(roles);
        version = "%" + version.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, version);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by version for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByContextForDevPortal(String org, String context, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByContextForDevPortalSql(roles);
        context = "%" + context.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, context);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by context for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByStatusForDevPortal(String org, String status, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByStatusForDevPortalSql(roles);
        status = "%" + status.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, status);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by status for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByDescriptionForDevPortal(String org, String description, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByDescriptionForDevPortalSql(roles);
        description = "%" + description.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, description);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by description for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByTagsForDevPortal(String org, String tags, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByTagsForDevPortalSql(roles);
        tags = "%" + tags.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, tags);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by tags for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByCategoryForDevPortal(String org, String category, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByApiCategoryForDevPortalSql(roles);
        category = "%" + category.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, category);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by category for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchAPIsByOtherForDevPortal(String org, String property, String value, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchApiByOtherForDevPortalSql(property, roles);
        value = "%" + value.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching APIs by other criteria for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByContentForDevPortal(String org, String searchContent, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        searchContent = searchContent.toLowerCase();
        String query = SQLQuery.searchContentByContentForDevPortalSql(roles, searchContent);
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setInt(2, start);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    String uuid = rs.getString("uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId, uuid);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by content for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByNameForDevPortal(String org, String name, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByNameForDevPortalSql(roles);
        name = "%" + name.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, name);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by name for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByProviderForDevPortal(String org, String provider, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByProviderForDevPortalSql(roles);
        provider = "%" + provider.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, provider);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by provider for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByVersionForDevPortal(String org, String version, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByVersionForDevPortalSql(roles);
        version = "%" + version.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, version);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by version for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByContextForDevPortal(String org, String context, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByContextForDevPortalSql(roles);
        context = "%" + context.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, context);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by context for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByStatusForDevPortal(String org, String status, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByStatusForDevPortalSql(roles);
        status = "%" + status.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, status);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by status for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByDescriptionForDevPortal(String org, String description, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByDescriptionForDevPortalSql(roles);
        description = "%" + description.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, description);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by description for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByTagsForDevPortal(String org, String tags, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByTagsForDevPortalSql(roles);
        tags = "%" + tags.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, tags);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by tags for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByCategoryForDevPortal(String org, String category, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByApiCategoryForDevPortalSql(roles);
        category = "%" + category.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            prepStmt.setString(2, category);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by category for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public List<ContentSearchResult> searchContentByOtherForDevPortal(String org, String property, String value, int start, int offset, String[] roles) throws APIManagementException {
        List<ContentSearchResult> apiResults = new ArrayList<>();
        String query = SQLQuery.searchContentByOtherForDevPortalSql(property, roles);
        value = "%" + value.toLowerCase() + "%";
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, value);
            prepStmt.setString(2, org);
            prepStmt.setInt(3, start);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String metadata = rs.getString("metadata");
                    String type = rs.getString("type");
                    String apiId = rs.getString("api_uuid");
                    ContentSearchResult contentSearchResult = new ContentSearchResult(metadata, type, apiId);
                    apiResults.add(contentSearchResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error while searching content by other criteria for Dev Portal in the database", e);
        }
        return apiResults;
    }

    public String getAssociatedType(String name, String apiId) throws APIManagementException {
        String type = null;
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLQuery.getGetArtifactTypeByUUIDSQL())) {
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    type = rs.getString("type");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving associated type for API: " + apiId, e);
        }
        return type;
    }

    public boolean isAPIExists(String apiUUID, String name) throws APIManagementException {
        boolean exists = false;
        String query = SQLQuery.getCheckAPIExistsSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, name);
            prepStmt.setString(2, apiUUID);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    exists =  count > 0;
                }
            }
        } catch (SQLException e) {
            handleException("Error while checking if API exists in the database", e);
        }
        return exists;
    }

    public String getAPIUUIDByRevisionUUID(String org, String revisionUUID) throws APIManagementException {
        String apiUUID = null;
        String query = SQLQuery.getGetAsyncAPIDefinitionByUUIDSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, revisionUUID);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiUUID = rs.getString("api_uuid");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving API UUID by revision UUID in the database", e);
        }
        return apiUUID;
    }

    public String getApiProductByUUID(String org, String apiProductUUID) throws APIManagementException {
        String apiProductMetadata = null;
        String query = SQLQuery.getGetAPIProductSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, apiProductUUID);
            prepStmt.setString(2, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    apiProductMetadata = rs.getString("metadata");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving API Product by UUID in the database", e);
        }
        return apiProductMetadata;
    }

    public Set<Tag> getAllTags(String org) throws APIManagementException {
        Set<Tag> tags = new HashSet<>();
        List<String> tagNames = new ArrayList<>();
        String query = SQLQuery.getGetAllTagsSQL();
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            prepStmt.setString(1, org);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String tagResults = rs.getString("tags");
                    List<String> tagNamesList = Arrays.asList(tagResults.replace("[", "").replace("]", "").split(","));
                    if (!tagNamesList.isEmpty()) {
                        for (String tag : tagNamesList) {
                            if (tag != null && !tag.trim().isEmpty()) {
                                tag = tag.trim();
                                // Avoid adding duplicate tags
                                if (!tagNames.contains(tag)) {
                                    Tag tagObj = new Tag(tag, 1);
                                    tags.add(tagObj);
                                } else {
                                    // If tag already exists, increment the occurrence count
                                    for (Tag existingTag : tags) {
                                        if (existingTag.getName().equals(tag)) {
                                            existingTag.setNoOfOccurrences(existingTag.getNoOfOccurrences() + 1);
                                            break;
                                        }
                                    }
                                }
                                tagNames.add(tag);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving all tags in the database", e);
        }
        return tags;
    }

    public String getSecuritySchemeByUUID(String apiId, String name) throws APIManagementException{
        String securityScheme = null;
        try (Connection connection = PersistenceDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLQuery.getGetSecuritySchemeByUUIDSQL())) {
            prepStmt.setString(1, apiId);
            prepStmt.setString(2, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    securityScheme = rs.getString("api_security_scheme");
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving security scheme for API: " + apiId, e);
        }
        return securityScheme;
    }
}
