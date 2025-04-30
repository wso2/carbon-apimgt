package org.wso2.carbon.apimgt.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.persistence.dao.PersistenceDAO;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.JSONMapper;
import org.wso2.carbon.apimgt.persistence.utils.JsonUtils;
import org.wso2.carbon.apimgt.persistence.utils.PublisherAPISearchResultComparator;
import org.wso2.carbon.apimgt.persistence.utils.RegistrySearchUtil;
import org.wso2.carbon.registry.core.pagination.PaginationContext;

import java.sql.SQLException;
import java.util.*;

public class DatabasePersistenceImpl implements APIPersistence {
    private static final Log log = LogFactory.getLog(DatabasePersistenceImpl.class);
    private Properties properties;
    private PersistenceDAO persistenceDAO;
    private JSONMapper jsonMapper = new JSONMapper();

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
        api.setUuid(uuid);

        JsonObject json = jsonMapper.mapApiToJson(api);
        String jsonString = json.toString();
        try {
            int apiSchemaId = persistenceDAO.addAPISchema(uuid, jsonString, org);
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
            if (apiSchema != null) {
                API api = jsonMapper.mapJsonStringToAPI(apiSchema);
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

//        String modifiedQuery = RegistrySearchUtil.getPublisherSearchQuery(searchQuery, ctx);
//
//        log.debug("Modified query for publisher search: " + modifiedQuery);

        result = searchPaginatedPublisherAPIs(searchQuery, requestedTenantDomain, start, offset);

        return result;
    }

    private PublisherAPISearchResult searchPaginatedPublisherAPIs(String searchQuery, String tenantDomain, int start, int offset) {
        int totalLength = 0;
        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
        List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();

        try {
            totalLength = PersistenceDAO.getInstance().getAllAPICount(tenantDomain);

            List<String> results = PersistenceDAO.getInstance().searchAPISchema(searchQuery, tenantDomain);

            for (String result: results) {
                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                apiInfo.setId(JsonUtils.safeGetAsString(jsonObject, "uuid"));
                apiInfo.setApiName(JsonUtils.safeGetAsString(jsonObject.getAsJsonObject("id"), "apiName"));
                apiInfo.setVersion(JsonUtils.safeGetAsString(jsonObject.getAsJsonObject("id"), "version"));
                apiInfo.setProviderName(JsonUtils.safeGetAsString(jsonObject.getAsJsonObject("id"), "providerName"));
                apiInfo.setContext(JsonUtils.safeGetAsString(jsonObject, "context"));
                apiInfo.setType(JsonUtils.safeGetAsString(jsonObject, "type"));
                apiInfo.setThumbnail(JsonUtils.safeGetAsString(jsonObject, "thumbnailUrl"));
                apiInfo.setCreatedTime(JsonUtils.safeGetAsString(jsonObject, "createdTime"));
                apiInfo.setUpdatedTime(jsonObject.has("lastUpdated") && !jsonObject.get("lastUpdated").isJsonNull()
                        ? new Date(jsonObject.get("lastUpdated").getAsString())
                        : null);
                apiInfo.setAudience(JsonUtils.safeGetAsString(jsonObject, "audience"));

                JsonArray audiencesJson = jsonObject.get("audiences").getAsJsonArray();
                apiInfo.setAudiences(jsonMapper.jsonArrayToSet(audiencesJson));

                apiInfo.setUpdatedBy(JsonUtils.safeGetAsString(jsonObject, "updatedBy"));
                apiInfo.setGatewayVendor(JsonUtils.safeGetAsString(jsonObject, "gatewayVendor"));
                apiInfo.setAdvertiseOnly(jsonObject.has("advertiseOnly") && !jsonObject.get("advertiseOnly").isJsonNull()
                        && jsonObject.get("advertiseOnly").getAsBoolean());
                apiInfo.setBusinessOwner(JsonUtils.safeGetAsString(jsonObject, "businessOwner"));
                apiInfo.setBusinessOwnerEmail(JsonUtils.safeGetAsString(jsonObject, "businessOwnerEmail"));
                apiInfo.setTechnicalOwner(JsonUtils.safeGetAsString(jsonObject, "technicalOwner"));
                apiInfo.setTechnicalOwnerEmail(JsonUtils.safeGetAsString(jsonObject, "technicalOwnerEmail"));
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
        PublisherContentSearchResult result = null;

        try {
            String requestedTenantDomain = org.getName();
            int totalLength = PersistenceDAO.getInstance().getAllAPICount(requestedTenantDomain);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
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
        log.info("Savind OAS definition");
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        String swaggerDefinition = null;
        try {
            swaggerDefinition = persistenceDAO.getSwaggerDefinitionByUUID(apiId, org.getName());
            swaggerDefinition = JsonUtils.getFormattedJsonString(swaggerDefinition);
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
        return null;
    }

    @Override
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation) throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId) throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public DocumentContent addDocumentationContent(Organization org, String apiId, String docId, DocumentContent content) throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset, String searchQuery, UserContext ctx) throws DocumentationPersistenceException {
        return null;
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
