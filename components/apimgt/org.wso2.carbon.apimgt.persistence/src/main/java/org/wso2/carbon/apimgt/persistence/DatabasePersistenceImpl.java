package org.wso2.carbon.apimgt.persistence;

import com.google.gson.JsonObject;
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

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

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
        JsonObject json = jsonMapper.mapApiToJson(api);
        String jsonString = jsonMapper.formatJSONString(json.toString());
        try {
            String uuid = UUID.randomUUID().toString();
            api.setUuid(uuid);
            int apiSchemaId = persistenceDAO.addAPISchema(uuid, jsonString);
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
        return null;
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
        return null;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
        return null;
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start, int offset, UserContext ctx) throws APIPersistenceException {
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

    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        return "";
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
}
