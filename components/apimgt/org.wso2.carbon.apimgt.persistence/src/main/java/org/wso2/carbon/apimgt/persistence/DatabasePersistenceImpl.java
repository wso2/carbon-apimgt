package org.wso2.carbon.apimgt.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.persistence.dao.PersistenceDAO;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.utils.DatabasePersistenceUtil;
import org.wso2.carbon.apimgt.persistence.utils.PublisherAPISearchResultComparator;

import java.io.File;
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

        JsonObject apiJson = DatabasePersistenceUtil.mapApiToJson(api);
        String apiJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(apiJson);

        JsonObject orgJson = DatabasePersistenceUtil.mapOrgToJson(org);
        String orgJsonString = DatabasePersistenceUtil.getFormattedJsonStringToSave(orgJson);

        try {
            persistenceDAO.addAPISchema(uuid, apiJsonString, api.getSwaggerDefinition(), orgJsonString);
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
        PublisherAPI returnAPI;
        returnAPI = APIMapper.INSTANCE.toPublisherApi(api);
        return returnAPI;
    }

    @Override
    public String addAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException {
        return "";
    }

    @Override
    public void restoreAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId) throws APIPersistenceException {

    }

    @Override
    public void deleteAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId) throws APIPersistenceException {

    }

    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        return null;
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
            String apiSchema = persistenceDAO.getAPISchemaByUUID(apiId, tenantDomain);
            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(apiSchema);
            if (apiSchema != null) {
                API api = DatabasePersistenceUtil.jsonToApi(jsonObject);
                publisherAPI = APIMapper.INSTANCE.toPublisherApi(api);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return publisherAPI;
    }

    @Override
    public DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException {
        return null;
    }

    @Override
    public void deleteAPI(Organization org, String apiId) throws APIPersistenceException {

    }

    @Override
    public void deleteAllAPIs(Organization org) throws APIPersistenceException {

    }

    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        String requestedTenantDomain = org.getName();

        PublisherAPISearchResult result = null;

        log.debug("Requested query for publisher search: " + searchQuery);

        result = searchPaginatedPublisherAPIs(searchQuery, org, start, offset);

        return result;
    }

    private PublisherAPISearchResult searchPaginatedPublisherAPIs(String searchQuery, Organization org, int start, int offset) {
        int totalLength = 0;
        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
        List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();

        try {
            totalLength = PersistenceDAO.getInstance().getAllAPICount(org.getName());

            List<String> results = persistenceDAO.searchAPISchema(searchQuery, org.getName(), start, offset);

            for (String result: results) {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                apiInfo.setId(DatabasePersistenceUtil.safeGetAsString(jsonObject, "uuid"));
                apiInfo.setApiName(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "apiName"));
                apiInfo.setVersion(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "version"));
                apiInfo.setProviderName(DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "providerName"));
                apiInfo.setContext(DatabasePersistenceUtil.safeGetAsString(jsonObject, "context"));
                apiInfo.setType(DatabasePersistenceUtil.safeGetAsString(jsonObject, "type"));
                apiInfo.setThumbnail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "thumbnailUrl"));
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

                publisherAPIInfoList.add(apiInfo);
            }
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
        return null;
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        PublisherContentSearchResult searchResult = new PublisherContentSearchResult();

        try {
            String requestedTenantDomain = org.getName();
            int totalLength = 0;
            searchQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
            List<String> results = persistenceDAO.searchAPISchemaContent(searchQuery, requestedTenantDomain);
            List<SearchContent> contentData = new ArrayList<>();

            for (String result: results) {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                String type;

//                if (jsonObject.has("type") && !jsonObject.get("type").isJsonNull()) {
//                    type = jsonObject.get("type").getAsString();
//                } else {
//                    type = "API";
//                }

                PublisherAPI publisherAPI = DatabasePersistenceUtil.getAPIForSearch(jsonObject);
                PublisherSearchContent content = new PublisherSearchContent();
                content.setContext(publisherAPI.getContext());
                content.setDescription(publisherAPI.getDescription());
                content.setId(publisherAPI.getId());
                content.setName(publisherAPI.getApiName());
                content.setProvider(
                        DatabasePersistenceUtil.replaceEmailDomainBack(publisherAPI.getProviderName()));
                content.setType("API");
                content.setVersion(publisherAPI.getVersion());
                content.setStatus(publisherAPI.getStatus());
                content.setAdvertiseOnly(publisherAPI.isAdvertiseOnly());
                content.setThumbnailUri(publisherAPI.getThumbnail());
                content.setBusinessOwner(publisherAPI.getBusinessOwner());
                content.setBusinessOwnerEmail(publisherAPI.getBusinessOwnerEmail());
                content.setTechnicalOwner(publisherAPI.getTechnicalOwner());
                content.setTechnicalOwnerEmail(publisherAPI.getTechnicalOwnerEmail());
                content.setMonetizationStatus(publisherAPI.getMonetizationStatus());
                contentData.add(content);
            }
            totalLength = results.size();
            searchResult.setTotalCount(totalLength);
            searchResult.setReturnedCount(contentData.size());
            searchResult.setResults(contentData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return searchResult;
    }

    @Override
    public DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        return null;
    }

    @Override
    public void changeAPILifeCycle(Organization org, String apiId, String status) throws APIPersistenceException {

    }

    @Override
    public void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile) throws WSDLPersistenceException {

    }

    @Override
    public ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException {
        return null;
    }

    @Override
    public void saveOASDefinition(Organization org, String apiId, String apiDefinition) throws OASPersistenceException {
        log.info("Saving OAS definition");
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        String swaggerDefinition = null;
        try {
            swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiId, org.getName());
            swaggerDefinition = DatabasePersistenceUtil.getFormattedJsonString(swaggerDefinition);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return swaggerDefinition;
    }

    @Override
    public void saveAsyncDefinition(Organization org, String apiId, String apiDefinition) throws AsyncSpecPersistenceException {

    }

    @Override
    public String getAsyncDefinition(Organization org, String apiId) throws AsyncSpecPersistenceException {
        return "";
    }

    @Override
    public void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition) throws GraphQLPersistenceException {

    }

    @Override
    public String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException {
        return "";
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
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation) throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        DocumentResult documentation = null;
        try {
            documentation = persistenceDAO.getDocumentation(docId, org.getName());
            JsonObject jsonObject = DatabasePersistenceUtil.stringTojsonObject(documentation.getMetadata());
            jsonObject.addProperty("id", documentation.getUuid());
            return DatabasePersistenceUtil.jsonToDocument(jsonObject);
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public DocumentContent addDocumentationContent(Organization org, String apiId, String docId, DocumentContent content) throws DocumentationPersistenceException {
        try {
            if (DocumentContent.ContentSourceType.FILE.equals(content.getSourceType())) {
                InputStream inputStream = content.getResourceFile().getContent();
                persistenceDAO.addDocumentationFile(docId, apiId, inputStream);
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
            searchQuery = DatabasePersistenceUtil.getSearchQuery(searchQuery);
            List<DocumentResult> results = persistenceDAO.searchDocumentation(apiId, requestedTenantDomain, searchQuery, start, offset);
            List<Documentation> documentationList = new ArrayList<>();

            totalLength = persistenceDAO.getDocumentationCount(requestedTenantDomain, apiId);

            for (DocumentResult result: results) {
                JsonObject jsonObject = JsonParser.parseString(result.getMetadata()).getAsJsonObject();
                jsonObject.addProperty("id", result.getUuid());
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

    }

    @Override
    public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        return null;
    }

    @Override
    public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {

    }

    @Override
    public PublisherAPIProduct addAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct) throws APIPersistenceException {
        return null;
    }

    @Override
    public PublisherAPIProduct updateAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct) throws APIPersistenceException {
        return null;
    }

    @Override
    public PublisherAPIProduct getPublisherAPIProduct(Organization org, String apiProductId) throws APIPersistenceException {
        return null;
    }

    @Override
    public PublisherAPIProductSearchResult searchAPIProductsForPublisher(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        return null;
    }

    @Override
    public void deleteAPIProduct(Organization org, String apiId) throws APIPersistenceException {

    }

    @Override
    public Set<Tag> getAllTags(Organization org, UserContext ctx) throws APIPersistenceException {
        return Set.of();
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
