package org.wso2.carbon.apimgt.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.MediationInfo;
import org.wso2.carbon.apimgt.persistence.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.GraphQLPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.MediationPolicyPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.ThumbnailPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.WSDLPersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.mapper.MongoAPIMapper;
import org.wso2.carbon.apimgt.persistence.utils.MongoDBPersistenceUtil;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Projections.exclude;

public class MongoDBPersistenceImpl implements APIPersistence {

    private static APIPersistence instance = null;
    private static final Log log = LogFactory.getLog(MongoDBPersistenceImpl.class);

    public MongoDBPersistenceImpl(String userName) {

    }

    @Override
    public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);
        InsertOneResult insertOneResult = collection.insertOne(mongoDBPublisherAPI);
        MongoDBPublisherAPI createdDoc = collection.find(eq("_id",
                insertOneResult.getInsertedId())).first();
        getDevPortalAPI(org, createdDoc.getMongodbUuId().toString());
        return MongoAPIMapper.INSTANCE.toPublisherApi(createdDoc);
    }

    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI)
            throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);
        String apiId = mongoDBPublisherAPI.getId();
        MongoDBPublisherAPI updatedDocument =
                collection.findOneAndReplace(eq("_id", new ObjectId(apiId)), mongoDBPublisherAPI);
        return MongoAPIMapper.INSTANCE.toPublisherApi(updatedDocument);
    }

    @Override
    public PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        MongoDBPublisherAPI mongoDBAPIDocument =
                collection.find(eq("_id", new ObjectId(apiId)))
                        .projection(exclude("swaggerDefinition")).first();
        if (mongoDBAPIDocument == null) {
            String msg = "Failed to get API. " + apiId + " does not exist in mongodb database";
            log.error(msg);
            throw new APIPersistenceException(msg);
        }
        PublisherAPI api = MongoAPIMapper.INSTANCE.toPublisherApi(mongoDBAPIDocument);
        return api;
    }

    @Override
    public DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException {
        MongoCollection<MongoDBDevPortalAPI> collection = getDevPortalCollection(org.getName());
        MongoDBDevPortalAPI mongoDBAPIDocument = collection.find(eq("_id", new ObjectId(apiId))).first();
        if (mongoDBAPIDocument == null) {
            String msg = "Failed to get API. " + apiId + " does not exist in mongodb database";
            log.error(msg);
            throw new APIPersistenceException(msg);
        }
        return MongoAPIMapper.INSTANCE.toDevPortalApi(mongoDBAPIDocument);
    }

    @Override
    public void deleteAPI(Organization org, String apiId) throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        collection.deleteOne(eq("_id", new ObjectId(apiId)));
        log.info("successfully deleted " + apiId + " from mongodb");
    }

    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException {
        return null;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException {
        //published prototyped only
        return null;
    }

    @Override
    public void changeAPILifeCycle(Organization org, String apiId, String status)
            throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        collection.updateOne(eq("_id", new ObjectId(apiId)), set("status", status));
    }

    @Override
    public void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile)
            throws WSDLPersistenceException {

    }

    @Override
    public ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException {
        return null;
    }

    @Override
    public void saveOASDefinition(Organization org, String apiId, String apiDefinition)
            throws OASPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        collection.updateOne(eq("_id", new ObjectId(apiId)), set("swaggerDefinition", apiDefinition));
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        MongoDBPublisherAPI api = collection.find(eq("_id", new ObjectId(apiId)))
                .projection(include("swaggerDefinition")).first();
        if (api == null) {
            throw new OASPersistenceException("Failed to get api definition. Api " + apiId + " does not exist in " +
                    "mongodb");
        }
        return api.getSwaggerDefinition();
    }

    @Override
    public void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition)
            throws GraphQLPersistenceException {

    }

    @Override
    public String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException {
        return null;
    }

    @Override
    public Documentation addDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset,
                                                    String searchQuery, UserContext ctx)
            throws DocumentationPersistenceException {
        return null;
    }

    @Override
    public void deleteDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {

    }

    @Override
    public Mediation addMediationPolicy(Organization org, String apiId, Mediation mediation)
            throws MediationPolicyPersistenceException {
        return null;
    }

    @Override
    public Mediation updateMediationPolicy(Organization org, String apiId, Mediation mediation)
            throws MediationPolicyPersistenceException {
        return null;
    }

    @Override
    public Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId)
            throws MediationPolicyPersistenceException {
        return null;
    }

    @Override
    public List<MediationInfo> getAllMediationPolicies(Organization org, String apiId)
            throws MediationPolicyPersistenceException {
        return null;
    }

    @Override
    public void deleteMediationPolicy(Organization org, String apiId, String mediationPolicyId)
            throws MediationPolicyPersistenceException {

    }

    @Override
    public void saveThumbnail(Organization org, String apiId, ResourceFile resourceFile)
            throws ThumbnailPersistenceException {

    }

    @Override
    public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        return null;
    }

    @Override
    public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {

    }

    private MongoCollection<MongoDBPublisherAPI> getPublisherCollection(String orgName) {
        MongoClient mongoClient = MongoDBPersistenceUtil.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("APIM_DB");

        return database.getCollection(orgName, MongoDBPublisherAPI.class);
    }

    private MongoCollection<MongoDBDevPortalAPI> getDevPortalCollection(String orgName) {
        MongoClient mongoClient = MongoDBPersistenceUtil.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("APIM_DB");

        return database.getCollection(orgName, MongoDBDevPortalAPI.class);
    }

}
