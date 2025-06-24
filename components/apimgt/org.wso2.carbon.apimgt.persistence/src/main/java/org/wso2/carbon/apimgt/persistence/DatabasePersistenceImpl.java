package org.wso2.carbon.apimgt.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.persistence.dao.PersistenceDAO;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.APIProductMapper;
import org.wso2.carbon.apimgt.persistence.utils.*;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class DatabasePersistenceImpl implements APIPersistence {
    private static final Log log = LogFactory.getLog(DatabasePersistenceImpl.class);
    private Properties properties;
    private static PersistenceDAO persistenceDAO;

    public DatabasePersistenceImpl() {
        persistenceDAO = PersistenceDAO.getInstance();
    }

    public DatabasePersistenceImpl(Properties properties) {
        this();
        this.properties = properties;
    }

    @Override
    public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        API api = APIMapper.INSTANCE.toApi(publisherAPI);
        String uuid = UUID.randomUUID().toString();
        APIIdentifier id = new APIIdentifier(api.getId().getProviderName(), api.getId().getApiName(), api.getId().getVersion(), uuid);
        api.setUuid(uuid);
        api.setId(id);
        api.setCreatedTime(String.valueOf(new Date().getTime()));
        api.setLastUpdated(new Date());

        JsonObject apiJson = DatabasePersistenceUtil.mapApiToJson(api);
        String apiJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(apiJson);

        JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
        String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

        try {
            persistenceDAO.addAPISchema(uuid, apiJsonString, orgJsonString);

            if (publisherAPI.getSwaggerDefinition() != null) {
                try {
                    persistenceDAO.addSwaggerDefinition(uuid, publisherAPI.getSwaggerDefinition(), orgJsonString);
                } catch (APIManagementException e) {
                    log.error("Error while saving Swagger definition for API: " + api.getId().getApiName(), e);
                }
            }

            if (publisherAPI.getAsyncApiDefinition() != null) {
                try {
                    persistenceDAO.addAsyncDefinition(uuid, publisherAPI.getAsyncApiDefinition(), orgJsonString);
                } catch (APIManagementException e) {
                    log.error("Error while saving Async API definition for API: " + api.getId().getApiName(), e);
                }
            }

        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
        PublisherAPI returnAPI;
        returnAPI = APIMapper.INSTANCE.toPublisherApi(api);
        return returnAPI;
    }

    @Override
    public String addAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException {
        String revisionUUID;
        try {
            // Get type of artifact
            String artifactType = persistenceDAO.getAssociatedType(org.getName(), apiUUID);

            if (artifactType == null || artifactType.isEmpty()) {
                throw new APIPersistenceException("No associated type found for API: " + apiUUID);
            }

            // Check if the API exists
            if (!persistenceDAO.isAPIExists(apiUUID, org.getName())) {
                throw new APIPersistenceException("API with UUID: " + apiUUID + " does not exist in organization: " + org.getName());
            }

            // Get API Schema data
            String apiOrApiProductSchema = null;

            if (APIConstants.API_PRODUCT_DB_NAME.equals(artifactType)) {
                apiOrApiProductSchema = persistenceDAO.getApiProductByUUID(org.getName(), apiUUID);
            } else {
                apiOrApiProductSchema = persistenceDAO.getAPISchemaByUUID(apiUUID, org.getName());
            }

            String swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiUUID, org.getName());
            String asyncApiDefinition = persistenceDAO.getAsyncAPIDefinitionByUUID(apiUUID, org.getName());

            revisionUUID = UUID.randomUUID().toString();

            // Prepare JSON objects
            JsonObject apiJson = DatabasePersistenceUtil.stringTojsonObject(apiOrApiProductSchema);
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            // Add revision entry
            persistenceDAO.addAPIRevisionSchema(apiUUID, artifactType, revisionId, revisionUUID, apiJson.toString(), orgJsonString);

            // Add API definitions if they exist
            if (swaggerDefinition != null) {
                try {
                    persistenceDAO.addAPIRevisionSwaggerDefinition(apiUUID, revisionId, revisionUUID,
                        swaggerDefinition, orgJsonString);
                } catch (APIManagementException e) {
                    log.error("Error while saving Swagger definition for API revision: " + apiUUID, e);
                }
            }

            if (asyncApiDefinition != null) {
                try {
                    persistenceDAO.addAPIRevisionAsyncDefinition(apiUUID, revisionId, revisionUUID,
                        asyncApiDefinition, orgJsonString);
                } catch (APIManagementException e) {
                    log.error("Error while saving Async API definition for API revision: " + apiUUID, e);
                }
            }

            // Handle thumbnail if exists
            try {
                ResourceFile thumbnailResource = this.getThumbnail(org, apiUUID);

               if (thumbnailResource != null && thumbnailResource.getContent() != null) {
                   JsonObject thumbnailMetadata = new JsonObject();
                   thumbnailMetadata.addProperty("fileType", thumbnailResource.getContentType());
                   thumbnailMetadata.addProperty("fileName", thumbnailResource.getName());
                   String thumbnailMetadataString = DatabasePersistenceUtil.getFormattedJsonStringToSave(thumbnailMetadata);

                   persistenceDAO.addAPIRevisionThumbnail(apiUUID, revisionId, revisionUUID,
                           thumbnailResource.getContent(), thumbnailMetadataString, orgJsonString);
                }
            } catch (Exception e) {
                // Log error but continue since thumbnail is not critical
                log.error("Error while saving thumbnail for API revision: " + apiUUID, e);
            }

            if (log.isDebugEnabled()) {
                log.debug("API Revision " + revisionId + " created for API: " + apiUUID);
            }

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while creating API Revision: " + revisionId +
                " for API: " + apiUUID, e);
        }
        return revisionUUID;
    }

    @Override
    public void restoreAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId) throws APIPersistenceException {
        boolean transactionCommitted = false;
        try {
            // Get API revision data
            String apiRevisionSchema = persistenceDAO.getAPIRevisionSchemaById(revisionUUID, org.getName());
            String swaggerRevisionDefinition = persistenceDAO.getAPIRevisionSwaggerDefinitionById(revisionUUID, org.getName());
            String asyncAPIRevisionDefinition = persistenceDAO.getAPIRevisionAsyncDefinitionById(revisionUUID, org.getName());
            String existingLifecycleStatus = persistenceDAO.getAPILifeCycleStatus(apiUUID, org.getName());

            if (apiRevisionSchema == null) {
                throw new APIMgtResourceNotFoundException("API Revision not found for revision ID: " + revisionId,
                    ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, String.valueOf(revisionId)));
            }

            // Store the revision as current API version
            JsonObject apiJson = DatabasePersistenceUtil.stringTojsonObject(apiRevisionSchema);
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            // Update value if there's property called status else add new
            apiJson.addProperty("status", existingLifecycleStatus);
            apiRevisionSchema = DatabasePersistenceUtil.getFormattedJsonStringToSave(apiJson);

            // Update main API with revision data
            persistenceDAO.updateAPISchema(apiUUID, apiRevisionSchema);

            // Update swagger definition if exists
            if (swaggerRevisionDefinition != null) {
                try {
                    persistenceDAO.updateSwaggerDefinition(apiUUID, swaggerRevisionDefinition);
                } catch (APIManagementException e) {
                    log.error("Error while updating Swagger definition from revision for API: " + apiUUID, e);
                }
            }

            // Update async API definition if exists
            if (asyncAPIRevisionDefinition != null) {
                try {
                    persistenceDAO.updateAsyncAPIDefinition(apiUUID, asyncAPIRevisionDefinition);
                } catch (APIManagementException e) {
                    log.error("Error while updating Async API definition from revision for API: " + apiUUID, e);
                }
            }

            // Restore thumbnail if exists in revision
            try {
                FileResult thumbnailRevision = persistenceDAO.getAPIRevisionThumbnail(apiUUID, revisionId, revisionUUID, org.getName());
                if (thumbnailRevision != null && thumbnailRevision.getContent() != null) {
                    persistenceDAO.updateThumbnail(apiUUID, thumbnailRevision.getContent(),
                        thumbnailRevision.getMetadata());
                }
            } catch (APIManagementException e) {
                // Log error but continue since thumbnail is not critical
                log.error("Error while restoring thumbnail from revision for API: " + apiUUID, e);
            }

            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                log.debug("API Revision " + revisionId + " restored successfully for API: " + apiUUID);
            }

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while restoring API Revision: " + revisionId +
                    " for API: " + apiUUID, e);
        } finally {
            if (!transactionCommitted) {
                log.error("Transaction for restoring API Revision " + revisionId + " failed for API: " + apiUUID);
            }
        }
    }

    @Override
    public void deleteAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId) throws APIPersistenceException {
        boolean transactionCommitted = false;
        try {
            // Delete all associated revision artifacts
            persistenceDAO.deleteAPIRevision(revisionUUID);

            transactionCommitted = true;
            if (log.isDebugEnabled()) {
                log.debug("API Revision " + revisionId + " deleted successfully for API: " + apiUUID);
            }
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while deleting API Revision: " + revisionId +
                " for API: " + apiUUID, e);
        }
    }

    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        API api = APIMapper.INSTANCE.toApi(publisherAPI);
        boolean transactionCommitted = false;

        try {
            // Convert API data to JSON format
            JsonObject apiJson = DatabasePersistenceUtil.mapApiToJson(api);
            String apiJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(apiJson);

            // Convert organization data to JSON format
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            // Update API schema
            persistenceDAO.updateAPISchema(api.getUuid(), apiJsonString);

            // Update Swagger definition if exists
            if (api.getSwaggerDefinition() != null) {
                try {
                    persistenceDAO.updateSwaggerDefinition(api.getUuid(), api.getSwaggerDefinition());
                } catch (APIManagementException e) {
                    log.error("Error while updating Swagger definition for API: " + api.getId().getApiName(), e);
                }
            }

            // Update Async API definition if exists
            if (api.getAsyncApiDefinition() != null) {
                try {
                    persistenceDAO.updateAsyncAPIDefinition(api.getUuid(), api.getAsyncApiDefinition());
                } catch (APIManagementException e) {
                    log.error("Error while updating Async API definition for API: " + api.getId().getApiName(), e);
                }
            }

            // Update thumbnail if exists
            try {
                ResourceFile thumbnailResource = this.getThumbnail(org, api.getUuid());
                if (thumbnailResource != null) {
                    persistenceDAO.updateThumbnail(api.getUuid(), thumbnailResource.getContent(),
                        thumbnailResource.getContentType());
                }
            } catch (Exception e) {
                // Log error but continue since thumbnail is not critical
                log.error("Error while updating thumbnail for API: " + api.getUuid(), e);
            }

            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
//                setSoapToRestSequences(publisherAPI, persistenceDAO);
            }

            transactionCommitted = true;
            PublisherAPI returnAPI = APIMapper.INSTANCE.toPublisherApi(api);
            if (log.isDebugEnabled()) {
                log.debug("Updated API :" + returnAPI.toString());
            }
            return returnAPI;

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while updating API : " + api.getUuid(), e);
        } finally {
            try {
                if (!transactionCommitted) {
                    // Handle transaction rollback if needed
                    log.error("Transaction for API update not committed. Rolling back.");
                }
            } catch (Exception e) {
                log.error("Error while rolling back API update transaction", e);
            }
        }
    }

    @Override
    public String getSecuritySchemeOfAPI(Organization org, String apiId) throws APIPersistenceException {
        return "";
    }

    @Override
    public PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException {
        String tenantDomain = org.getName();
        PublisherAPI publisherAPI = null;

        try {
            if (!persistenceDAO.isAPIExists(apiId, org.getName())) {
                try {
                    apiId = persistenceDAO.getAPIUUIDByRevisionUUID(org.getName(), apiId);
                } catch (APIManagementException e) {
                    log.error("Error while retrieving API UUID for revision UUID: " + apiId, e);
                    throw new APIPersistenceException("API with ID: " + apiId + " does not exist in organization: " + tenantDomain, e);
                }
            }

            // Determine if the artifact is an API or API Product
            String artifactType = persistenceDAO.getAssociatedType(tenantDomain, apiId);

            if (artifactType == null || artifactType.isEmpty()) {
                throw new APIPersistenceException("No associated type found for API or API Product: " + apiId);
            }

            String schema;
            String swaggerDefinition;

            if (APIConstants.API_PRODUCT_DB_NAME.equals(artifactType)) {
                // Handle API Product
                schema = persistenceDAO.getApiProductByUUID(tenantDomain, apiId);
            } else {
                // Handle API
                schema = persistenceDAO.getAPISchemaByUUID(apiId, tenantDomain);
            }
            swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiId, tenantDomain);

            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(schema);
            jsonObject.addProperty("swaggerDefinition", swaggerDefinition);

            try {
                ResourceFile thumbnailResource = this.getThumbnail(org, apiId);
                if (thumbnailResource != null) {
                    String thumbnailUrl = DatabasePersistenceUtil.convertToBase64(thumbnailResource.getContent(), thumbnailResource.getContentType());
                    jsonObject.addProperty("thumbnailUrl", thumbnailUrl);
                } else {
                    jsonObject.addProperty("thumbnailUrl", "");
                }
            } catch (Exception e) {
                // Log error but continue
                log.error("Error while retrieving thumbnail for API or API Product: " + apiId, e);
            }

            if (schema != null) {
                if (APIConstants.API_PRODUCT_DB_NAME.equals(artifactType)) {
                    // Map to API Product
                    APIProduct apiProduct = DatabasePersistenceUtil.jsonToApiProduct(jsonObject);
                    PublisherAPIProduct publisherAPIProduct = APIProductMapper.INSTANCE.toPublisherApiProduct(apiProduct);
                    publisherAPI = DatabasePersistenceUtil.convertToPublisherAPI(publisherAPIProduct);
                } else {
                    // Map to API
                    API api = DatabasePersistenceUtil.jsonToApi(jsonObject);
                    publisherAPI = APIMapper.INSTANCE.toPublisherApi(api);
                }
            }
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while retrieving API or API Product with ID: " + apiId, e);
        }

        return publisherAPI;
    }

    @Override
    public DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException {
        try {
            // Determine if the artifact is an API or API Product
            String artifactType = persistenceDAO.getAssociatedType(org.getName(), apiId);

            if (artifactType == null || artifactType.isEmpty()) {
                throw new APIPersistenceException("No associated type found for API or API Product: " + apiId);
            }

            String schema;
            String swaggerDefinition;

            if (APIConstants.API_PRODUCT_DB_NAME.equals(artifactType)) {
                // Handle API Product
                schema = persistenceDAO.getApiProductByUUID(org.getName(), apiId);
            } else {
                // Handle API
                schema = persistenceDAO.getAPISchemaByUUID(apiId, org.getName());
            }
            swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiId, org.getName());

            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(schema);
            jsonObject.addProperty("swaggerDefinition", swaggerDefinition);

            try {
                ResourceFile thumbnailResource = this.getThumbnail(org, apiId);
                if (thumbnailResource != null) {
                    String thumbnailUrl = DatabasePersistenceUtil.convertToBase64(thumbnailResource.getContent(), thumbnailResource.getContentType());
                    jsonObject.addProperty("thumbnailUrl", thumbnailUrl);
                } else {
                    jsonObject.addProperty("thumbnailUrl", "");
                }
            } catch (Exception e) {
                log.error("Error while retrieving thumbnail for API or API Product: " + apiId, e);
            }

            if (APIConstants.API_PRODUCT_DB_NAME.equals(artifactType)) {
                // Convert API Product data to DevPortalAPI
                APIProduct apiProduct = DatabasePersistenceUtil.jsonToApiProduct(jsonObject);
                DevPortalAPI devPortalAPI = DatabasePersistenceUtil.mapAPIProductToDevPortalAPI(apiProduct);
                return devPortalAPI;
            } else {
                // Convert API data to DevPortalAPI
                API api = DatabasePersistenceUtil.jsonToApi(jsonObject);
                return APIMapper.INSTANCE.toDevPortalApi(api);
            }

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while retrieving API or API Product with ID: " + apiId, e);
        }
    }

    @Override
    public void deleteAPI(Organization org, String apiId) throws APIPersistenceException {
        try {
            persistenceDAO.deleteAPISchema(apiId, org.getName());
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while deleting API", e);
        }
    }

    @Override
    public void deleteAllAPIs(Organization org) throws APIPersistenceException {
        throw new UnsupportedOperationException("This method is not supported on this instance");
    }

    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        String requestedTenantDomain = org.getName();

        PublisherAPISearchResult result = null;

        log.debug("Requested query for publisher API product search: " + searchQuery);
        SearchQuery modifiedQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
        log.debug("Modified query for publisher API product search: " + modifiedQuery);

        result = searchPaginatedPublisherAPIs(modifiedQuery, org, start, offset, ctx);

        return result;
    }

    private PublisherAPISearchResult searchPaginatedPublisherAPIs(SearchQuery searchQuery, Organization org, int start, int offset, UserContext ctx) {
        int totalLength = 0;
        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
        List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();
        String[] userRoles = ctx.getRoles();

        try {
            totalLength = persistenceDAO.getAllAPICount(org.getName(), userRoles);
            List<String> results = null;

            if (searchQuery == null) {
                results = persistenceDAO.getAllPIs(org.getName(), start, offset, userRoles);
            } else {
                results = DatabaseSearchUtil.searchAPIsForPublisher(searchQuery, org.getName(), start, offset, userRoles);
            }

            for (String result: results) {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

                API api = DatabasePersistenceUtil.jsonToApi(jsonObject);

                String access = api.getAccessControl();
                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                String apiId = DatabasePersistenceUtil.safeGetAsString(jsonObject, "uuid");
                apiInfo.setId(apiId);
                apiInfo.setApiName(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "apiName"));
                apiInfo.setVersion(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "version"));
                apiInfo.setProviderName(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "providerName"));
                apiInfo.setContext(DatabasePersistenceUtil.safeGetAsString(jsonObject, "context"));
                apiInfo.setType(DatabasePersistenceUtil.safeGetAsString(jsonObject, "type"));
                apiInfo.setCreatedTime(DatabasePersistenceUtil.safeGetAsString(jsonObject, "createdTime"));
                apiInfo.setUpdatedTime(jsonObject.has("lastUpdated") && !jsonObject.get("lastUpdated").isJsonNull()
                        ? new Date(jsonObject.get("lastUpdated").getAsString())
                        : null);
                apiInfo.setAudience(DatabasePersistenceUtil.safeGetAsString(jsonObject, "audience"));

                JsonArray audiencesJson = jsonObject.get("audiences").getAsJsonArray();
                apiInfo.setAudiences(DatabasePersistenceUtil.jsonArrayToSet(audiencesJson));

                apiInfo.setUpdatedBy(DatabasePersistenceUtil.safeGetAsString(jsonObject, "updatedBy"));
                apiInfo.setGatewayVendor(DatabasePersistenceUtil.safeGetAsString(jsonObject, "gatewayVendor"));
                apiInfo.setAdvertiseOnly(jsonObject.has("advertiseOnly") && !jsonObject.get("advertiseOnly").isJsonNull()
                        && jsonObject.get("advertiseOnly").getAsBoolean());
                apiInfo.setBusinessOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "businessOwner"));
                apiInfo.setBusinessOwnerEmail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "businessOwnerEmail"));
                apiInfo.setTechnicalOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "technicalOwner"));
                apiInfo.setTechnicalOwnerEmail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "technicalOwnerEmail"));
                apiInfo.setMonetizationStatus(jsonObject.has("monetizationStatus") && !jsonObject.get("monetizationStatus").isJsonNull()
                        && jsonObject.get("monetizationStatus").getAsBoolean());

                try {
                    ResourceFile thumbnailResource = this.getThumbnail(org, apiId);
                    if (thumbnailResource != null) {
                        String thumbnailUrl = DatabasePersistenceUtil.convertToBase64(thumbnailResource.getContent(), thumbnailResource.getContentType());
                        apiInfo.setThumbnail(thumbnailUrl);
                    } else {
                        apiInfo.setThumbnail("");
                    }
                } catch (Exception e) {
//                    log.error("Error while retrieving thumbnail for API: " + apiId, e);
                }


                publisherAPIInfoList.add(apiInfo);


            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        publisherAPIInfoList.sort(new PublisherAPISearchResultComparator());
        searchResults.setPublisherAPIInfoList(publisherAPIInfoList);
        searchResults.setReturnedAPIsCount(publisherAPIInfoList.size());
        searchResults.setTotalAPIsCount(totalLength);

        return searchResults;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
       String requestedDomain = org.getName();
       DevPortalAPISearchResult result = null;

        SearchQuery modifiedQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
        result = searchPaginatedDevPortalAPIs(modifiedQuery, org, start, offset, ctx);

        return result;
    }

    private DevPortalAPISearchResult searchPaginatedDevPortalAPIs(SearchQuery searchQuery, Organization org, int start, int offset, UserContext ctx) {
        int totalLength = 0;
        DevPortalAPISearchResult searchResult = new DevPortalAPISearchResult();
        String orgName = org.getName();
        String[] userRoles = ctx.getRoles();

        try {
            totalLength = persistenceDAO.getAllAPICount(orgName, ctx.getRoles());

            List<ContentSearchResult> results = null;

            if (searchQuery == null) {
                results = persistenceDAO.getAllAPIsForDevPortal(orgName, start, offset, userRoles);
            } else {
                results = DatabaseSearchUtil.serachAPIsForDevPortal(searchQuery, orgName, start, offset, userRoles);
            }

            if (results == null || results.isEmpty()) {
                searchResult.setDevPortalAPIInfoList(Collections.emptyList());
                searchResult.setReturnedAPIsCount(0);
                searchResult.setTotalAPIsCount(totalLength);
                return searchResult;
            }

            List<DevPortalAPIInfo> devPortalAPIInfoList = new ArrayList<>();

            for (ContentSearchResult result : results) {
                if (result.getMetadata() == null || result.getMetadata().isEmpty()) {
                    continue; // Skip if metadata ilifecycleStatuss empty
                }

                String apiId = result.getApiId();
                String type = result.getType();
                if (type == null || type.isEmpty()) {
                    type = "API"; // Default to API if type is not specified
                }

                JsonObject jsonObject = JsonParser.parseString(result.getMetadata()).getAsJsonObject();

                DevPortalAPIInfo apiInfo = null;

                if (type.equals("API_PRODUCT")) {
                    APIProduct apiProduct = DatabasePersistenceUtil.jsonToApiProduct(jsonObject);
                    apiInfo = DatabasePersistenceUtil.mapAPIProductToAPIInfo(apiProduct);
                } else {
                    API api = DatabasePersistenceUtil.jsonToApi(jsonObject);
                    apiInfo = DatabasePersistenceUtil.mapAPItoAPIInfo(api);
                }
                try {
                    ResourceFile thumbnailResource = this.getThumbnail(org, apiId);
                    if (thumbnailResource != null) {
                        String thumbnailUrl = DatabasePersistenceUtil.convertToBase64(thumbnailResource.getContent(), thumbnailResource.getContentType());
                        apiInfo.setThumbnail(thumbnailUrl);
                    } else {
                        apiInfo.setThumbnail("");
                    }
                } catch (Exception e) {
                    log.debug("Error while retrieving thumbnail for API: " + apiId, e);
                }
                devPortalAPIInfoList.add(apiInfo);
            }

            searchResult.setDevPortalAPIInfoList(devPortalAPIInfoList);
            searchResult.setReturnedAPIsCount(devPortalAPIInfoList.size());
            searchResult.setTotalAPIsCount(totalLength);
        } catch (APIManagementException e) {
            throw new RuntimeException("Error while searching Dev Portal APIs", e);
        }catch (SQLException e) {
            throw new RuntimeException("Error while retrieving API count for Dev Portal", e);
        }

        return searchResult;
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        PublisherContentSearchResult searchResult = new PublisherContentSearchResult();
        String[] userRoles = ctx.getRoles();

        try {
            String requestedTenantDomain = org.getName();
            int totalLength = 0;
            SearchQuery modifiedQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
            List<ContentSearchResult> results = DatabaseSearchUtil.searchContentForPublisher(modifiedQuery, requestedTenantDomain, start, offset, userRoles);
            List<SearchContent> contentData = new ArrayList<>();

            for (ContentSearchResult result: results) {
                JsonObject jsonObject = JsonParser.parseString(result.getMetadata()).getAsJsonObject();
                String contentType = result.getType();

                if (contentType == null || contentType.isEmpty()) {
                    contentType = "API";
                }

                if (contentType.equals("API_PRODUCT")) {
                    contentType = "APIProduct";
                }

                if (contentType.equals("DOCUMENTATION")) {
                    // Handle documentation content
                    DocumentSearchContent docContent = new DocumentSearchContent();
                    Documentation doc = DatabasePersistenceUtil.jsonToDocument(jsonObject);
                    String apiId = result.getApiId();

                    if (apiId != null) {
                        PublisherAPI pubAPI = this.getPublisherAPI(org, apiId);
                        docContent.setApiName(pubAPI.getApiName());
                        docContent.setApiProvider(pubAPI.getProviderName());
                        docContent.setApiVersion(pubAPI.getVersion());
                        docContent.setApiUUID(pubAPI.getId());
                        docContent.setDocType(doc.getType());
                        docContent.setId(doc.getId());
                        docContent.setSourceType(doc.getSourceType());
                        docContent.setVisibility(doc.getVisibility());
                        docContent.setName(doc.getName());
                        contentData.add(docContent);
                    }
                } else if (contentType.equals("API_DEFINITION") || contentType.equals("ASYNC_API_DEFINITION") || contentType.equals("GRAPHQL_SCHEMA") || contentType.equals("WSDL")) {
                    // Handle API definition content
                    APIDefSearchContent defContent = new APIDefSearchContent();
                    String apiId = result.getApiId();

                    if (apiId != null) {
                        PublisherAPI pubAPI = this.getPublisherAPI(org, apiId);

                        String associatedType = persistenceDAO.getAssociatedType(org.getName(), apiId);

                        defContent.setId(pubAPI.getId());
                        switch (contentType) {
                            case "API_DEFINITION":
                                defContent.setName(pubAPI.getApiName() + " swagger");
                                break;
                            case "ASYNC_API_DEFINITION":
                                defContent.setName(pubAPI.getApiName() + " async");
                                break;
                            case "GRAPHQL_SCHEMA":
                                defContent.setName(pubAPI.getApiName() + " graphql");
                                break;
                            case "WSDL":
                                defContent.setName(pubAPI.getApiName() + " wsdl");
                                break;
                        }
                        defContent.setApiUUID(pubAPI.getId());
                        defContent.setApiName(pubAPI.getApiName());
                        defContent.setApiContext(pubAPI.getContext());
                        defContent.setApiProvider(pubAPI.getProviderName());
                        defContent.setApiVersion(pubAPI.getVersion());
                        defContent.setApiType(determineAPIType(pubAPI.getType()));
                        defContent.setAssociatedType(associatedType);
                        contentData.add(defContent);
                    }
                } else {
                    // Handle API content
                    PublisherAPI pubAPI = DatabasePersistenceUtil.getAPIForSearch(jsonObject);
                    PublisherSearchContent content = new PublisherSearchContent();
                    content.setContext(pubAPI.getContext());
                    content.setDescription(pubAPI.getDescription());
                    content.setId(pubAPI.getId());
                    content.setName(pubAPI.getApiName());
                    content.setProvider(DatabasePersistenceUtil.replaceEmailDomainBack(pubAPI.getProviderName()));
                    content.setType(contentType);
                    content.setVersion(pubAPI.getVersion());
                    content.setStatus(pubAPI.getStatus());
                    content.setAdvertiseOnly(pubAPI.isAdvertiseOnly());
                    content.setThumbnailUri(pubAPI.getThumbnail());
                    content.setBusinessOwner(pubAPI.getBusinessOwner());
                    content.setBusinessOwnerEmail(pubAPI.getBusinessOwnerEmail());
                    content.setTechnicalOwner(pubAPI.getTechnicalOwner());
                    content.setTechnicalOwnerEmail(pubAPI.getTechnicalOwnerEmail());
                    content.setMonetizationStatus(pubAPI.getMonetizationStatus());
                    contentData.add(content);
                }
            }
            totalLength = results.size();
            searchResult.setTotalCount(totalLength);
            searchResult.setReturnedCount(contentData.size());
            searchResult.setResults(contentData);
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while searching content for publisher: " + searchQuery, e);
        }

        return searchResult;
    }

    private APIDefSearchContent.ApiType determineAPIType(String apiType) {
        if (APIConstants.API_TYPE_SOAP.equals(apiType) ||
                APIConstants.API_TYPE_SOAPTOREST.equals(apiType)) {
            return APIDefSearchContent.ApiType.SOAP;
        } else if (APIConstants.API_TYPE_GRAPHQL.equals(apiType)) {
            return APIDefSearchContent.ApiType.GRAPHQL;
        } else if (APIConstants.API_TYPE_WS.equals(apiType) ||
                APIConstants.API_TYPE_WEBHOOK.equals(apiType) ||
                APIConstants.API_TYPE_SSE.equals(apiType) ||
                APIConstants.API_TYPE_WEBSUB.equals(apiType)) {
            return APIDefSearchContent.ApiType.ASYNC;
        } else {
            return APIDefSearchContent.ApiType.REST;
        }
    }

    @Override
    public DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        DevPortalContentSearchResult searchResult = new DevPortalContentSearchResult();

        try {
            String requestedTenantDomain = org.getName();
            int totalLength = 0;
            SearchQuery modifiedQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
            List<ContentSearchResult> results = DatabaseSearchUtil.searchContentForDevPortal(modifiedQuery, requestedTenantDomain, start, offset, ctx.getRoles());
            List<SearchContent> contentData = new ArrayList<>();

            for (ContentSearchResult result: results) {
                JsonObject jsonObject = JsonParser.parseString(result.getMetadata()).getAsJsonObject();
                String contentType = result.getType();

                if (contentType == null || contentType.isEmpty()) {
                    contentType = "API";
                }

                if (contentType.equals("API_PRODUCT")) {
                    contentType = "APIProduct";
                }

                if (contentType.equals("DOCUMENTATION")) {
                    // Handle documentation content
                    DocumentSearchContent docContent = new DocumentSearchContent();
                    Documentation doc = DatabasePersistenceUtil.jsonToDocument(jsonObject);
                    String apiId = result.getApiId();

                    if (apiId != null) {
                        DevPortalAPI devAPI = this.getDevPortalAPI(org, apiId);
                        docContent.setApiName(devAPI.getApiName());
                        docContent.setApiProvider(devAPI.getProviderName());
                        docContent.setApiVersion(devAPI.getVersion());
                        docContent.setApiUUID(devAPI.getId());
                        docContent.setDocType(doc.getType());
                        docContent.setId(doc.getId());
                        docContent.setSourceType(doc.getSourceType());
                        docContent.setVisibility(doc.getVisibility());
                        docContent.setName(doc.getName());
                        contentData.add(docContent);
                    }
                } else if (contentType.equals("API_DEFINITION") || contentType.equals("ASYNC_API_DEFINITION") || contentType.equals("GRAPHQL_SCHEMA") || contentType.equals("WSDL")) {
                    // Handle API definition content
                    APIDefSearchContent defContent = new APIDefSearchContent();
                    String apiId = result.getApiId();

                    if (apiId != null) {
                        DevPortalAPI devAPI = this.getDevPortalAPI(org, apiId);

                        String associatedType = persistenceDAO.getAssociatedType(org.getName(), apiId);

                        defContent.setId(devAPI.getId());
                        switch (contentType) {
                            case "API_DEFINITION":
                                defContent.setName(devAPI.getApiName() + " swagger");
                                break;
                            case "ASYNC_API_DEFINITION":
                                defContent.setName(devAPI.getApiName() + " async");
                                break;
                            case "GRAPHQL_SCHEMA":
                                defContent.setName(devAPI.getApiName() + " graphql");
                                break;
                            case "WSDL":
                                defContent.setName(devAPI.getApiName() + " wsdl");
                                break;
                        }
                        defContent.setApiUUID(devAPI.getId());
                        defContent.setApiName(devAPI.getApiName());
                        defContent.setApiContext(devAPI.getContext());
                        defContent.setApiProvider(devAPI.getProviderName());
                        defContent.setApiVersion(devAPI.getVersion());
                        defContent.setApiType(determineAPIType(devAPI.getType()));
                        defContent.setAssociatedType(associatedType);
                        contentData.add(defContent);
                    }
                } else {
                    // Handle API content
                    DevPortalAPI devAPI = this.getDevPortalAPI(org, result.getApiId());
                    DevPortalSearchContent content = new DevPortalSearchContent();
                    content.setContext(devAPI.getContext());
                    content.setDescription(devAPI.getDescription());
                    content.setId(devAPI.getId());
                    content.setName(devAPI.getApiName());
                    content.setProvider(DatabasePersistenceUtil.replaceEmailDomainBack(devAPI.getProviderName()));
                    content.setType(contentType);
                    content.setVersion(devAPI.getVersion());
                    content.setStatus(devAPI.getStatus());
                    content.setAdvertiseOnly(devAPI.isAdvertiseOnly());
                    content.setThumbnailUri(devAPI.getThumbnail());
                    content.setBusinessOwner(devAPI.getBusinessOwner());
                    content.setBusinessOwnerEmail(devAPI.getBusinessOwnerEmail());
                    content.setTechnicalOwner(devAPI.getTechnicalOwner());
                    content.setTechnicalOwnerEmail(devAPI.getTechnicalOwnerEmail());
                    content.setMonetizationStatus(devAPI.getMonetizationStatus());
                    contentData.add(content);
                }
            }
            totalLength = results.size();
            searchResult.setTotalCount(totalLength);
            searchResult.setReturnedCount(contentData.size());
            searchResult.setResults(contentData);
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while searching content for Dev Portal: " + searchQuery, e);
        }

        return searchResult;
    }

    @Override
    public void changeAPILifeCycle(Organization org, String apiId, String status) throws APIPersistenceException {
        // Unused method
    }

    @Override
    public void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile) throws WSDLPersistenceException {
        try {
            JsonObject metadataJson = new JsonObject();

            if (wsdlResourceFile.getContentType() != null && !wsdlResourceFile.getContentType().isEmpty()) {
                metadataJson.addProperty("fileType", wsdlResourceFile.getContentType());
            }
            String metadataJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(metadataJson);

            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            persistenceDAO.addWSDL(apiId, orgJsonString, wsdlResourceFile.getContent(), metadataJsonString);
        } catch (APIManagementException e) {
            throw new WSDLPersistenceException("Error while saving WSDL for API: " + apiId, e);
        }
    }

    @Override
    public ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException {
        try {
            FileResult wsdlResult = persistenceDAO.getWSDL(apiId, org.getName());
            if (wsdlResult != null) {
                InputStream content = wsdlResult.getContent();
                String metadata = wsdlResult.getMetadata();
                JsonObject metadataJson = DatabasePersistenceUtil.stringTojsonObject(metadata);
                String contentType = DatabasePersistenceUtil.safeGetAsString(metadataJson, "fileType");
                if (contentType == null || contentType.isEmpty()) {
                    contentType = "application/wsdl+xml"; // Default WSDL content type
                }
                ResourceFile resourceFile = new ResourceFile(content, contentType);
                PublisherAPI publisherAPI = this.getPublisherAPI(org, apiId);
                API api = APIMapper.INSTANCE.toApi(publisherAPI);
                resourceFile.setName(DatabasePersistenceUtil.createWsdlFileName(
                        api.getId().getApiName(), api.getId().getVersion(), api.getId().getProviderName()
                ));
                return resourceFile;
            } else {
                throw new WSDLPersistenceException("WSDL not found for API: " + apiId);
            }
        } catch (APIManagementException e) {
            throw new WSDLPersistenceException("Error while retrieving WSDL for API: " + apiId, e);
        } catch (APIPersistenceException e) {
            throw new WSDLPersistenceException("Error while retrieving API for WSDL: " + apiId, e);
        }
    }

    @Override
    public void saveOASDefinition(Organization org, String apiId, String apiDefinition) throws OASPersistenceException {
        try {
            String swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiId, org.getName());
            if (swaggerDefinition != null) {
                // If the API definition already exists, update it
                persistenceDAO.saveOASDefinition(apiId, apiDefinition);
            } else {
                // If the API definition does not exist, create a new entry
                persistenceDAO.addSwaggerDefinition(apiId, apiDefinition, DatabasePersistenceUtil.mapOrgToJson(org).toString());
            }
        } catch (APIManagementException e) {
            throw new OASPersistenceException("Error while saving OAS definition", e);
        }
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        String swaggerDefinition = null;
        try {
            swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiId, org.getName());
            if (swaggerDefinition != null) {
                swaggerDefinition = DatabasePersistenceUtil.getFormattedJsonString(swaggerDefinition);
            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
        return swaggerDefinition;
    }

    @Override
    public void saveAsyncDefinition(Organization org, String apiId, String apiDefinition) throws AsyncSpecPersistenceException {
        try {
            persistenceDAO.saveAsyncAPIDefinition(apiId, apiDefinition);
        } catch (APIManagementException e) {
            throw new AsyncSpecPersistenceException("Error while saving Async API definition", e);
        }
    }

    @Override
    public String getAsyncDefinition(Organization org, String apiId) throws AsyncSpecPersistenceException {
        String asyncApiDefinition = null;
        try {
            asyncApiDefinition = persistenceDAO.getAsyncAPIDefinitionByUUID(apiId, org.getName());
            if (asyncApiDefinition != null) {
                asyncApiDefinition = DatabasePersistenceUtil.getFormattedJsonString(asyncApiDefinition);
            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
        return asyncApiDefinition;
    }

    @Override
    public void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition) throws GraphQLPersistenceException {
        try {
            JsonObject schemaJson = new JsonObject();
            schemaJson.addProperty("schemaDefinition", schemaDefinition);
            String schemaJsonString = schemaJson.toString();

            String existingSchema = persistenceDAO.getGraphQLSchema(apiId, org.getName());
            if (existingSchema != null) {
                // If the GraphQL schema already exists, update it
                persistenceDAO.updateGraphQLSchema(apiId, schemaJsonString);
            } else {
                // If the GraphQL schema does not exist, create a new entry
                persistenceDAO.addGraphQLSchema(apiId, schemaJsonString, DatabasePersistenceUtil.mapOrgToJson(org).toString());
            }
        } catch (APIManagementException e) {
            throw new GraphQLPersistenceException("Error while saving GraphQL schema definition", e);
        }
    }

    @Override
    public String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException {
        String graphQLSchema = null;
        try {
            graphQLSchema = persistenceDAO.getGraphQLSchema(apiId, org.getName());
            if (graphQLSchema != null) {
                graphQLSchema = DatabasePersistenceUtil.safeGetAsString(
                        DatabasePersistenceUtil.stringTojsonObject(graphQLSchema), "schemaDefinition"
                );
            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
        return graphQLSchema;
    }

    @Override
    public Documentation addDocumentation(Organization org, String apiId, Documentation documentation) throws DocumentationPersistenceException {
        try {
            JsonObject docJson = DatabasePersistenceUtil.mapDocumentToJson(documentation);
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);

            String docJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(docJson);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            String docId = persistenceDAO.addAPIDocumentation(apiId, docJsonString, orgJsonString);
            documentation.setId(docId);
        } catch (APIManagementException e) {
            throw new DocumentationPersistenceException("Error while adding documentation", e);
        }
        return documentation;
    }

    @Override
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {

        try {
            // Convert documentation metadata to JSON format
            JsonObject docJson = DatabasePersistenceUtil.mapDocumentToJson(documentation);
            String docJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(docJson);

            // Convert organization data to JSON format
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            // Update documentation metadata in database
            persistenceDAO.updateDocumentation(documentation.getId(), docJsonString);

            return documentation;

        } catch (APIManagementException e) {
            throw new DocumentationPersistenceException("Error while updating documentation", e);
        }
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        DocumentResult documentation = null;
        try {
            documentation = persistenceDAO.getDocumentation(docId, org.getName());
            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(documentation.getMetadata());
            jsonObject.addProperty("id", documentation.getUuid());
            jsonObject.addProperty("createdTime", documentation.getCreatedTime());
            jsonObject.addProperty("lastUpdatedTime", documentation.getLastUpdatedTime());
            return DatabasePersistenceUtil.jsonToDocument(jsonObject);
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        DocumentContent documentContent = null;
        try {
            String documentationString = persistenceDAO.getDocumentationContent(docId);
            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(documentationString);
            documentContent = DatabasePersistenceUtil.jsonToDocumentationContent(jsonObject);

            if (documentContent.getSourceType().equals(DocumentContent.ContentSourceType.FILE)) {
                InputStream content = persistenceDAO.getDocumentationFileContent(docId);
                ResourceFile resourceFile = new ResourceFile(content, documentContent.getResourceFile().getContentType());
                resourceFile.setName(documentContent.getResourceFile().getName());
                documentContent.setResourceFile(resourceFile);
            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }

        return documentContent;
    }

    @Override
    public DocumentContent addDocumentationContent(Organization org, String apiId, String docId, DocumentContent content) throws DocumentationPersistenceException {
        try {
            if (DocumentContent.ContentSourceType.FILE.equals(content.getSourceType())) {
                persistenceDAO.addDocumentationFile(docId, apiId, content.getResourceFile());
            } else {
                String contentString = content.getTextContent();
                persistenceDAO.addDocumentationContent(docId, apiId, contentString);
            }
        } catch (APIManagementException e) {
            throw new DocumentationPersistenceException("Error while adding documentation content", e);
        }
        return null;
    }

    @Override
    public DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset, String searchQuery, UserContext ctx) throws DocumentationPersistenceException {
        DocumentSearchResult searchResult = null;
        int totalLength = 0;

        if (offset == 0) {
            offset = 100;
        }

        try {
            String requestedTenantDomain = org.getName();
            searchQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery) != null ? DatabasePersistenceUtil.getSearchQuery(searchQuery).getContent() : "";
            List<DocumentResult> results = persistenceDAO.searchDocumentation(apiId, requestedTenantDomain, searchQuery, start, offset);
            List<Documentation> documentationList = new ArrayList<>();

            totalLength = persistenceDAO.getDocumentationCount(requestedTenantDomain, apiId);

            for (DocumentResult result: results) {
                JsonObject jsonObject = JsonParser.parseString(result.getMetadata()).getAsJsonObject();
                jsonObject.addProperty("id", result.getUuid());
                jsonObject.addProperty("createdTime", result.getCreatedTime());
                jsonObject.addProperty("lastUpdatedTime", result.getLastUpdatedTime());
                Documentation documentation = DatabasePersistenceUtil.jsonToDocument(jsonObject);
                documentationList.add(documentation);
            }
            if (!results.isEmpty()) {
                searchResult = new DocumentSearchResult();
                searchResult.setReturnedDocsCount(documentationList.size());
                searchResult.setTotalDocsCount(totalLength);
                searchResult.setDocumentationList(documentationList);
            }
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }

        return searchResult;
    }

    @Override
    public void deleteDocumentation(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        try {
            persistenceDAO.deleteDocumentation(docId);
        } catch (APIManagementException e) {
            throw new DocumentationPersistenceException("Error while deleting documentation", e);
        }

    }

    @Override
    public Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId) throws MediationPolicyPersistenceException {
        return null;
    }

    @Override
    public List<MediationInfo> getAllMediationPolicies(Organization org, String apiId) throws MediationPolicyPersistenceException {
        return List.of();
    }

    @Override
    public void saveThumbnail(Organization org, String apiId, ResourceFile resourceFile) throws ThumbnailPersistenceException {
        try {
            JsonObject metadataJson = new JsonObject();
            metadataJson.addProperty("fileType", resourceFile.getContentType());
            metadataJson.addProperty("fileName", resourceFile.getName());
            String metadataJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(metadataJson);

            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            persistenceDAO.addThumbnail(apiId, orgJsonString,resourceFile.getContent() ,metadataJsonString);
        } catch (Exception e) {
            throw new ThumbnailPersistenceException("Error while saving thumbnail for API: " + apiId, e);
        }
    }

    @Override
    public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        try {
           FileResult thumbnailResult = persistenceDAO.getThumbnail(apiId, org.getName());
            if (thumbnailResult != null) {
                InputStream content = thumbnailResult.getContent();
                String metadata = thumbnailResult.getMetadata();
                JsonObject metadataJson = DatabasePersistenceUtil.stringTojsonObject(metadata);
                String contentType = DatabasePersistenceUtil.safeGetAsString(metadataJson, "fileType");
                String fileName = DatabasePersistenceUtil.safeGetAsString(metadataJson, "fileName");
                ResourceFile resourceFile = new ResourceFile(content, contentType);
                resourceFile.setName(fileName);
                return resourceFile;
            } else {
                return null; // No thumbnail found for the given API ID
            }
        } catch (APIManagementException e) {
            throw new ThumbnailPersistenceException("Error while retrieving thumbnail for API: " + apiId, e);
        }
    }

    @Override
    public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        try {
            persistenceDAO.deleteThumbnail(apiId, org.getName());
        } catch (APIManagementException e) {
            throw new ThumbnailPersistenceException("Error while deleting thumbnail for API: " + apiId, e);
        }
    }

    @Override
    public PublisherAPIProduct addAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct)
            throws APIPersistenceException {
        try {
            // Convert to APIProduct model
            APIProduct apiProduct = APIProductMapper.INSTANCE.toApiProduct(publisherAPIProduct);
            String uuid = UUID.randomUUID().toString();
            APIProductIdentifier id = new APIProductIdentifier(publisherAPIProduct.getProviderName(),
                    publisherAPIProduct.getApiProductName(), publisherAPIProduct.getVersion());
            apiProduct.setID(id);
            apiProduct.setUuid(uuid);
            apiProduct.setCreatedTime(new Date());
            apiProduct.setLastUpdated(new Date());

            // Convert APIProduct to JSON format
            JsonObject apiProductJson = DatabasePersistenceUtil.mapApiProductToJson(apiProduct);
            String apiProductJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(apiProductJson);

            // Convert organization data to JSON format
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            // Save APIProduct metadata in database
            persistenceDAO.addAPIProductSchema(uuid, apiProductJsonString, orgJsonString);

            // Save API Product definition if exists
            if (publisherAPIProduct.getDefinition() != null) {
                try {
                    persistenceDAO.addSwaggerDefinition(uuid, publisherAPIProduct.getDefinition(), orgJsonString);
                } catch (APIManagementException e) {
                    log.error("Error while saving API Product definition for API Product: " + apiProduct.getId().getName(), e);
                }
            }

            // Set created time and UUID on response object
            publisherAPIProduct.setCreatedTime(String.valueOf(new Date().getTime()));
            publisherAPIProduct.setId(uuid);

            if (log.isDebugEnabled()) {
                log.debug("API Product Name: " + apiProduct.getId().getName() + " API Product Version " +
                        apiProduct.getId().getVersion() + " created");
            }

            return publisherAPIProduct;

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while creating API Product", e);
        }
    }

    @Override
    public PublisherAPIProduct updateAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct)
            throws APIPersistenceException {
        try {
            if (publisherAPIProduct.getId() == null || publisherAPIProduct.getId().isEmpty()) {
                throw new APIPersistenceException("API Product ID cannot be null or empty");
            }

            if (publisherAPIProduct.getApiProductName() == null || publisherAPIProduct.getApiProductName().isEmpty()) {
                PublisherAPIProduct currentAPIProduct = getPublisherAPIProduct(org, publisherAPIProduct.getId());
                if (currentAPIProduct != null) {
                    publisherAPIProduct.setApiProductName(currentAPIProduct.getApiProductName());
                } else {
                    throw new APIPersistenceException("API Product not found for ID: " + publisherAPIProduct.getId());
                }
            }

            // Convert to APIProduct model
            APIProduct apiProduct = APIProductMapper.INSTANCE.toApiProduct(publisherAPIProduct);
            APIProductIdentifier id = new APIProductIdentifier(publisherAPIProduct.getProviderName(),
                    publisherAPIProduct.getApiProductName(), publisherAPIProduct.getVersion());
            apiProduct.setID(id);
            apiProduct.setLastUpdated(new Date());

            // Convert APIProduct metadata to JSON format
            JsonObject apiProductJson = DatabasePersistenceUtil.mapApiProductToJson(apiProduct);
            String apiProductJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(apiProductJson);

            // Convert organization data to JSON format
            JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
            String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

            // Update APIProduct schema in database
            persistenceDAO.updateAPIProductSchema(publisherAPIProduct.getId(), apiProductJsonString);

            // Update API Product definition if exists
            if (publisherAPIProduct.getDefinition() != null) {
                try {
                    persistenceDAO.updateSwaggerDefinition(publisherAPIProduct.getId(),
                            publisherAPIProduct.getDefinition());
                } catch (APIManagementException e) {
                    log.error("Error while updating API Product definition for API Product: " +
                            apiProduct.getId().getName(), e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("API Product " + apiProduct.getId().getName() + "-" +
                        apiProduct.getId().getVersion() + " updated successfully");
            }

            return publisherAPIProduct;

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while updating API Product", e);
        }
    }

    @Override
    public PublisherAPIProduct getPublisherAPIProduct(Organization org, String apiProductId) throws APIPersistenceException {
        String tenantDomain = org.getName();
        PublisherAPIProduct publisherAPIProduct = null;

        try {
            if (!persistenceDAO.isAPIExists(apiProductId, org.getName())) {
                try {
                    apiProductId = persistenceDAO.getAPIUUIDByRevisionUUID(org.getName(), apiProductId);
                } catch (APIManagementException e) {
                    log.error("Error while retrieving API UUID for revision UUID: " + apiProductId, e);
                    throw new APIPersistenceException("API with ID: " + apiProductId + " does not exist in organization: " + tenantDomain, e);
                }
            }

            // Get API Product schema from database
            String apiProductSchema = persistenceDAO.getApiProductByUUID(tenantDomain, apiProductId);
            String swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiProductId, tenantDomain);
            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(apiProductSchema);
            jsonObject.addProperty("definition", swaggerDefinition);

            try {
                ResourceFile thumbnailResource = this.getThumbnail(org, apiProductId);
                if (thumbnailResource != null) {
                    String thumbnailUrl = DatabasePersistenceUtil.convertToBase64(thumbnailResource.getContent(), thumbnailResource.getContentType());
                    jsonObject.addProperty("thumbnailUrl", thumbnailUrl);
                } else {
                    jsonObject.addProperty("thumbnailUrl", "");
                }
            } catch (Exception e) {
                log.error("Error while retrieving thumbnail for API Product: " + apiProductId, e);
            }

            if (apiProductSchema != null) {
                APIProduct apiProduct = DatabasePersistenceUtil.jsonToApiProduct(jsonObject);
                publisherAPIProduct = APIProductMapper.INSTANCE.toPublisherApiProduct(apiProduct);

                // Set needed fields explicitly
                publisherAPIProduct.setId(apiProduct.getUuid());
                publisherAPIProduct.setApiProductName(apiProduct.getId().getName());
                publisherAPIProduct.setProviderName(apiProduct.getId().getProviderName());
                publisherAPIProduct.setVersion(apiProduct.getId().getVersion());
                publisherAPIProduct.setState(apiProduct.getState());
                publisherAPIProduct.setThumbnail(apiProduct.getThumbnailUrl());
                publisherAPIProduct.setDefinition(swaggerDefinition);
            }
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while retrieving API Product with ID " + apiProductId, e);
        }

        return publisherAPIProduct;
    }

    @Override
    public PublisherAPIProductSearchResult searchAPIProductsForPublisher(Organization org, String searchQuery,
                                                                         int start, int offset, UserContext ctx)
                                                                         throws APIPersistenceException {
        String requestedTenantDomain = org.getName();
        PublisherAPIProductSearchResult searchResult = null;
        String[] userRoles = ctx.getRoles();

        log.debug("Requested query for publisher API product search: " + searchQuery);
        SearchQuery modifiedQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
        log.debug("Modified query for publisher API product search: " + modifiedQuery);

        try {
            int totalLength = 0;
            List<PublisherAPIProductInfo> apiProductList = new ArrayList<>();

            // Get all API Products according to search criteria
            List<String> results = null;

            if (modifiedQuery != null && !modifiedQuery.getContent().isEmpty()) {
                results = DatabaseSearchUtil.searchAPIProductsForPublisher(modifiedQuery, requestedTenantDomain, start, offset, userRoles);
            } else {
                results = persistenceDAO.getAllApiProducts(requestedTenantDomain, start, offset, userRoles);
            }

            totalLength = persistenceDAO.getAllAPIProductCount(org.getName());

            if (results == null || results.isEmpty()) {
                log.debug("No API Products found for the given search criteria.");
                return new PublisherAPIProductSearchResult();
            }

            for (String result: results) {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

                PublisherAPIProductInfo apiProductInfo = new PublisherAPIProductInfo();
                String apiProductId = DatabasePersistenceUtil.safeGetAsString(jsonObject, "uuid");
                apiProductInfo.setId(apiProductId);
                apiProductInfo.setProviderName(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "providerName"));
                apiProductInfo.setApiProductName(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "apiProductName"));
                apiProductInfo.setVersion(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "version"));
                apiProductInfo.setState(DatabasePersistenceUtil.safeGetAsString(jsonObject, "state"));
                apiProductInfo.setContext(DatabasePersistenceUtil.safeGetAsString(jsonObject, "context"));
                apiProductInfo.setApiSecurity(DatabasePersistenceUtil.safeGetAsString(jsonObject, "apiSecurity"));

                // Get thumbnail
                try {
                    ResourceFile thumbnailResource = this.getThumbnail(org, apiProductId);
                    if (thumbnailResource != null) {
                        String thumbnailUrl = DatabasePersistenceUtil.convertToBase64(thumbnailResource.getContent(), thumbnailResource.getContentType());
                        apiProductInfo.setThumbnail(thumbnailUrl);
                    } else {
                        apiProductInfo.setThumbnail("");
                    }
                } catch (Exception e) {
                    log.error("Error while retrieving thumbnail for API Product: " + apiProductId, e);
                }

                // Get audience info
                JsonArray audiencesJson = jsonObject.get("audiences").getAsJsonArray();
                apiProductInfo.setAudiences(DatabasePersistenceUtil.jsonArrayToSet(audiencesJson));

                // Get business owner info
                apiProductInfo.setBusinessOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "businessOwner"));
                apiProductInfo.setBusinessOwnerEmail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "businessOwnerEmail"));
                apiProductInfo.setTechnicalOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "technicalOwner"));
                apiProductInfo.setTechnicalOwnerEmail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "technicalOwnerEmail"));

                // Get monetization status
                apiProductInfo.setMonetizationStatus(jsonObject.has("monetizationStatus") &&
                    !jsonObject.get("monetizationStatus").isJsonNull() &&
                    jsonObject.get("monetizationStatus").getAsBoolean());

                apiProductList.add(apiProductInfo);
            }

            searchResult = new PublisherAPIProductSearchResult();
            searchResult.setPublisherAPIProductInfoList(apiProductList);
            searchResult.setReturnedAPIsCount(apiProductList.size());
            searchResult.setTotalAPIsCount(totalLength);

        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while searching API Products", e);
        }
        return searchResult;
    }

    @Override
    public void deleteAPIProduct(Organization org, String apiProductId) throws APIPersistenceException {
        try {
            // Delete API Product schema
            persistenceDAO.deleteAPIProductSchema(apiProductId, org.getName());

            // Delete swagger definition if exists
            try {
                persistenceDAO.deleteSwaggerDefinition(apiProductId);
            } catch (APIManagementException e) {
                log.error("Error while deleting Swagger definition for API Product: " + apiProductId, e);
            }

            // Delete thumbnail if exists
            try {
                persistenceDAO.deleteThumbnail(apiProductId, org.getName());
            } catch (APIManagementException e) {
                log.error("Error while deleting thumbnail for API Product: " + apiProductId, e);
            }

            // Delete documentation if exists
            try {
                List<DocumentResult> docs = persistenceDAO.getAllDocuments(apiProductId);
                if (docs != null) {
                    for (DocumentResult doc : docs) {
                        persistenceDAO.deleteDocumentation(doc.getUuid());
                    }
                }
            } catch (APIManagementException e) {
                log.error("Error while deleting documentation for API Product: " + apiProductId, e);
            }

            // Delete API Product revisions if exist
            try {
                List<String> getRevisionIds = persistenceDAO.getAllAPIRevisionIds(apiProductId);

                if (getRevisionIds != null && !getRevisionIds.isEmpty()) {
                    for (String revisionId : getRevisionIds) {
                        persistenceDAO.deleteAPIRevision(revisionId);
                    }
                }

            } catch (APIManagementException e) {
                log.error("Error while deleting revisions for API Product: " + apiProductId, e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Successfully deleted API Product with ID: " + apiProductId);
            }
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while deleting API Product: " + apiProductId, e);
        }
    }

    @Override
    public Set<Tag> getAllTags(Organization org, UserContext ctx) throws APIPersistenceException {
        Set<Tag> tags = new HashSet<>();
        try {
            tags = persistenceDAO.getAllTags(org.getName());
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while retrieving all tags", e);
        }
        return tags;
    }

    @Override
    public void updateSoapToRestSequences(Organization org, String apiId, List<SOAPToRestSequence> sequences) throws APIPersistenceException {

    }

    @Override
    public void changeApiProvider(String providerName, String apiId, String org) throws APIManagementException, APIPersistenceException {

    }

    @Override
    public AdminContentSearchResult searchContentForAdmin(String org, String searchQuery, int start, int count, int limit) throws APIPersistenceException {
        return null;
    }

    protected static int getMaxPaginationLimit() {

        return Integer.MAX_VALUE;
    }
}
