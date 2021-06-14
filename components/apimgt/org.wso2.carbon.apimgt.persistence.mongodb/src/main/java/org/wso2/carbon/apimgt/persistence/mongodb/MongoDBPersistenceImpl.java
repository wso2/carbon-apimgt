/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.result.InsertOneResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.persistence.dto.SearchContent;
import org.wso2.carbon.apimgt.persistence.dto.PublisherSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.MediationInfo;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProduct;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProductSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.PublisherContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.dto.DocumentationType;
import org.wso2.carbon.apimgt.persistence.exceptions.AsyncSpecPersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBThumbnail;
import org.wso2.carbon.apimgt.persistence.mongodb.mappers.DocumentationMapper;
import org.wso2.carbon.apimgt.persistence.mongodb.mappers.MongoAPIMapper;
import org.wso2.carbon.apimgt.persistence.mongodb.utils.MongoDBConnectionUtil;
import org.wso2.carbon.apimgt.persistence.mongodb.utils.MongoDBUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.APIDocumentation;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.GraphQLPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.MediationPolicyPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.ThumbnailPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.WSDLPersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.count;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Updates.unset;
import static org.wso2.carbon.apimgt.persistence.PersistenceConstants.DEFAULT_RETRY_COUNT;
import static org.wso2.carbon.apimgt.persistence.PersistenceConstants.DEFAULT_TREAD_COUNT;
import static org.wso2.carbon.apimgt.persistence.PersistenceConstants.REGISTRY_CONFIG_RETRY_COUNT;
import static org.wso2.carbon.apimgt.persistence.PersistenceConstants.REGISTRY_CONFIG_TREAD_COUNT;
import static org.wso2.carbon.apimgt.persistence.mongodb.MongoDBConstants.MONGODB_COLLECTION_DEFAULT_ORG;
import static org.wso2.carbon.apimgt.persistence.mongodb.MongoDBConstants.MONGODB_COLLECTION_SUR_FIX;
import static org.wso2.carbon.apimgt.persistence.mongodb.MongoDBConstants.MONGODB_GRIDFS_THMBNAIL_SUR_FIX;

@MethodStats
public class MongoDBPersistenceImpl implements APIPersistence {

    private static final Log log = LogFactory.getLog(MongoDBPersistenceImpl.class);
    private static Map<String, Boolean> indexCheckCache = new HashMap<>();
    private Map<String, String> configs = ServiceReferenceHolder.getInstance().getPersistenceConfigs();
    private ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_TREAD_COUNT);
    private int retryCount = DEFAULT_RETRY_COUNT;

    public MongoDBPersistenceImpl() {
        if (configs != null) {
            String threadCount = configs.get(REGISTRY_CONFIG_TREAD_COUNT);
            String retryCountConfig = configs.get(REGISTRY_CONFIG_RETRY_COUNT);
            int executorThreads = threadCount == null ? DEFAULT_TREAD_COUNT : Integer.parseInt(threadCount);
            executor = Executors.newFixedThreadPool(executorThreads);
            retryCount = retryCountConfig == null ? DEFAULT_RETRY_COUNT : Integer.parseInt(retryCountConfig);
        }
    }

    @Override
    public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        publisherAPI.setCreatedTime(String.valueOf(new Date().getTime()));
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);
        InsertOneResult insertOneResult = collection.insertOne(mongoDBPublisherAPI);
        MongoDBPublisherAPI createdDoc = collection.find(eq("_id", insertOneResult.getInsertedId())).first();

        //Create atlas search index in async manner
        executor.submit(() -> {
            isIndexCreated(org.getName());
        });
        return MongoAPIMapper.INSTANCE.toPublisherApi(createdDoc);
    }

    private Boolean isIndexCreated(String organizationName) {
        if (organizationName == null) {
            organizationName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        organizationName = organizationName + MONGODB_COLLECTION_SUR_FIX;

        //Check if index is created flag exists in cache
        Boolean indexCacheCheck = indexCheckCache.get(organizationName);
        if (indexCacheCheck != null && indexCacheCheck) {
            return true;
        }
        //Call mongodb atlas rest api to check if index exists
        boolean isIndexEmpty = MongoDBAtlasAPIConnector.getInstance()
                .getIndexes(organizationName).isEmpty();
        if (!isIndexEmpty) {
            indexCheckCache.put(organizationName, true);
            return true;
        }

        // Should we retry if we failed?
        // Create atlas search index
        Boolean searchIndexes;
        int currentTry = 1;
        do {
            searchIndexes = MongoDBAtlasAPIConnector.getInstance().createSearchIndexes(organizationName);
            if (searchIndexes) {
                break;
            }
            currentTry++;

        } while (currentTry < retryCount);
        indexCheckCache.put(organizationName, searchIndexes);
        return true;
    }

    @Override
    public String addAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException {
        MongoCollection<Document> genericCollection = MongoDBConnectionUtil.getGenericCollection(org.getName());

        FindIterable<Document> api = genericCollection.find(eq("_id", new ObjectId(apiUUID)));
        MongoCursor<Document> cursor = api.cursor();
        String createdId = null;
        while (cursor.hasNext()) {
            Document mongoDBAPIDocument = cursor.next();
            ObjectId newUuid = new ObjectId();
            mongoDBAPIDocument.put("_id", newUuid);
            mongoDBAPIDocument.put("revision", revisionId);
            genericCollection.insertOne(mongoDBAPIDocument);
            createdId = newUuid.toHexString();
        }
        return createdId;
    }

    @Override
    public void restoreAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId) throws
            APIPersistenceException {
        MongoCollection<Document> genericCollection = MongoDBConnectionUtil.getGenericCollection(org.getName());
        FindIterable<Document> revision = genericCollection.find(eq("_id", new ObjectId(revisionUUID)));
        MongoCursor<Document> cursor = revision.cursor();
        String lifecycleStatus = getLifecycleStatus(org, apiUUID);
        while (cursor.hasNext()) {
            Document mongoDBAPIDocument = cursor.next();
            mongoDBAPIDocument.put("_id", new ObjectId(apiUUID));
            mongoDBAPIDocument.remove("revision");
            mongoDBAPIDocument.put("status", lifecycleStatus);
            FindOneAndReplaceOptions options = new FindOneAndReplaceOptions();
            options.returnDocument(ReturnDocument.AFTER);
            genericCollection.findOneAndReplace(eq("_id", new ObjectId(apiUUID)), mongoDBAPIDocument, options);
            log.info("successfully restored the revision " + revisionUUID + " in mongodb");
        }
    }

    @Override
    public void deleteAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException {
        MongoCollection<Document> genericCollection = MongoDBConnectionUtil.getGenericCollection(org.getName());
        genericCollection.deleteOne(eq("_id", new ObjectId(apiUUID)));
        log.info("successfully deleted revision " + apiUUID + " from mongodb");
    }

    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI)
            throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);
        String apiId = mongoDBPublisherAPI.getId();

        try {
            MongoDBPublisherAPI mongoDBAPIDocument = getMongoDBPublisherAPIFromId(org, apiId, false);
            mongoDBPublisherAPI.setMongoDBThumbnail(mongoDBAPIDocument.getMongoDBThumbnail());
            mongoDBPublisherAPI.setDocumentationList(mongoDBAPIDocument.getDocumentationList());
            mongoDBPublisherAPI.setSwaggerDefinition(mongoDBAPIDocument.getSwaggerDefinition());
        } catch (APIPersistenceException e) {
            throw new APIPersistenceException("Error when getting API " + publisherAPI.getId(), e);
        }

        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions();
        options.returnDocument(ReturnDocument.AFTER);
        MongoDBPublisherAPI updatedDocument =
                collection.findOneAndReplace(eq("_id", new ObjectId(apiId)), mongoDBPublisherAPI, options);
        return MongoAPIMapper.INSTANCE.toPublisherApi(updatedDocument);
    }

    @Override
    public PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException {
        MongoDBPublisherAPI mongoDBAPIDocument = getMongoDBPublisherAPIFromId(org, apiId, true);
        PublisherAPI api = MongoAPIMapper.INSTANCE.toPublisherApi(mongoDBAPIDocument);
        return api;
    }

    @Override
    public DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException {
        MongoCollection<MongoDBDevPortalAPI> collection = MongoDBConnectionUtil.getDevPortalCollection(org.getName());
        MongoDBDevPortalAPI mongoDBAPIDocument = collection.find(eq("_id", new ObjectId(apiId))).first();
        if (mongoDBAPIDocument == null) {
            String msg = "Failed to get API. " + apiId + " does not exist in mongodb database";
            throw new APIPersistenceException(msg);
        }
        return MongoAPIMapper.INSTANCE.toDevPortalApi(mongoDBAPIDocument);
    }

    @Override
    public void deleteAPI(Organization org, String apiId) throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        collection.deleteOne(eq("_id", new ObjectId(apiId)));
        log.info("successfully deleted " + apiId + " from mongodb");
    }

    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException {
        int skip = start;
        int limit = offset;
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(ctx.getOrganization().getName());
        long totalCount = countTotalApi(org);
        MongoCursor<MongoDBPublisherAPI> aggregate = collection.aggregate(getPublisherSearchAggregate(searchQuery, skip, limit))
                .cursor();
        PublisherAPISearchResult publisherAPISearchResult = new PublisherAPISearchResult();
        List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<>();

        while (aggregate.hasNext()) {
            MongoDBPublisherAPI mongoDBAPIDocument = aggregate.next();
            PublisherAPIInfo api = MongoAPIMapper.INSTANCE.toPublisherApi(mongoDBAPIDocument);
            publisherAPIInfoList.add(api);
        }
        publisherAPISearchResult.setPublisherAPIInfoList(publisherAPIInfoList);
        publisherAPISearchResult.setReturnedAPIsCount(publisherAPIInfoList.size());
        publisherAPISearchResult.setTotalAPIsCount((int) totalCount);
        return publisherAPISearchResult;
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start,
                                                                  int offset, UserContext ctx) throws APIPersistenceException {
        int skip = start;
        int limit = offset;
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(ctx.getOrganization().getName());
        long totalCount = countTotalApi(org);
        MongoCursor<MongoDBPublisherAPI> aggregate = collection
                .aggregate(getPublisherSearchAggregate(searchQuery, skip, limit)).cursor();
        PublisherContentSearchResult contentSearchResult = new PublisherContentSearchResult();
        List<SearchContent> content = new ArrayList<>();
        while (aggregate.hasNext()) {
            MongoDBPublisherAPI mongoDBAPIDocument = aggregate.next();
            PublisherSearchContent api = MongoAPIMapper.INSTANCE.toPublisherContentApi(mongoDBAPIDocument);
            api.setType("API");
            content.add(api);
        }
        contentSearchResult.setResults(content);
        contentSearchResult.setReturnedCount(content.size());
        contentSearchResult.setTotalCount((int) totalCount);
        return contentSearchResult;
    }

    @Override
    public DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start,
                                                                  int offset, UserContext ctx) throws APIPersistenceException {
        int skip = start;
        int limit = offset;
        MongoCollection<MongoDBDevPortalAPI> collection = MongoDBConnectionUtil.getDevPortalCollection(ctx.getOrganization().getName());
        long totalCount = countTotalApi(org);
        MongoCursor<MongoDBDevPortalAPI> aggregate = collection
                .aggregate(getDevportalSearchAggregate(searchQuery, skip, limit)).cursor();
        DevPortalContentSearchResult contentSearchResult = new DevPortalContentSearchResult();
        List<SearchContent> content = new ArrayList<>();
        while (aggregate.hasNext()) {
            MongoDBDevPortalAPI mongoDBAPIDocument = aggregate.next();
            DevPortalSearchContent api = MongoAPIMapper.INSTANCE.toDevportalContentApi(mongoDBAPIDocument);
            api.setType("API");
            content.add(api);
        }
        contentSearchResult.setResults(content);
        contentSearchResult.setReturnedCount(content.size());
        contentSearchResult.setTotalCount((int) totalCount);
        return contentSearchResult;
    }

    private long countTotalApi(Organization org){
        MongoCollection<Document> genericCollection = MongoDBConnectionUtil.getGenericCollection(org.getName());
        Document doc = genericCollection.aggregate(Arrays.asList(match(exists("revision", false)), count("totalApis"))).first();
        long totalCount = 0;
        if (doc != null) {
            totalCount = Long.parseLong(doc.get("totalApis").toString());
        }
        return totalCount;
    }

    private Document buildSearchAggregate(String query) {
        String searchQuery;
        List<Document> mustArray = new ArrayList();
        if (query.contains(" ")) {
            String[] searchAreas = query.split(" ");
            for (String area : searchAreas) {
                List<String> paths = new ArrayList<>();
                if (area.contains(":")) {
                    String[] queryArray = area.split(":");
                    paths.addAll(getSearchField(queryArray[0]));
                    searchQuery = "*" + queryArray[1] + "*";
                } else {
                    searchQuery = "*" + query + "*";
                    paths.add("apiName");
                }
                Document wildCard = new Document();
                Document wildCardBody = new Document();
                wildCardBody.put("path", paths);
                wildCardBody.put("query", searchQuery);
                wildCardBody.put("allowAnalyzedField", true);
                wildCard.put("wildcard", wildCardBody);
                mustArray.add(wildCard);
            }
        } else {
            List<String> paths = new ArrayList<>();
            if (query.contains(":")) {
                String[] queryArray = query.split(":");
                paths.addAll(getSearchField(queryArray[0]));
                searchQuery = "*" + queryArray[1] + "*";
            } else {
                searchQuery = "*" + query + "*";
                if (query == "") {
                    searchQuery = "*";
                }
                paths.addAll(getSearchField("content"));
            }
            Document wildCard = new Document();
            Document wildCardBody = new Document();
            wildCardBody.put("path", paths);
            wildCardBody.put("query", searchQuery);
            wildCardBody.put("allowAnalyzedField", true);
            wildCard.put("wildcard", wildCardBody);
            mustArray.add(wildCard);
        }
        Document search = new Document();
        Document must = new Document();
        Document compound = new Document();

        must.put("must", mustArray);
        compound.put("compound", must);
        search.put("$search", compound);

        return search;
    }

    private List<Document> getPublisherSearchAggregate(String query, int skip, int limit) {
        Document searchDoc = buildSearchAggregate(query);
        Document skipDoc = new Document();
        Document limitDoc = new Document();

        skipDoc.put("$skip", skip);
        limitDoc.put("$limit", limit);

        Document matchDoc = new Document();
        List<Document> orMatchList = new ArrayList();
        Document orDoc = new Document();

        Document matchRevisions = new Document();
        Document matchExists = new Document();
        matchExists.put("$exists", false);
        matchRevisions.put("revision", matchExists);

        orMatchList.add(matchRevisions);
        orDoc.put("$or", orMatchList);
        matchDoc.put("$match", orDoc);

        List<Document> list = new ArrayList<>();
        list.add(searchDoc);
        list.add(matchDoc);
        list.add(skipDoc);
        list.add(limitDoc);
        return list;
    }

    private List<String> getSearchField(String queryCriteria) {
        List<String> fieldList = new ArrayList<>();
        if (queryCriteria.equalsIgnoreCase("name")) {
            fieldList.add("apiName");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("provider")) {
            fieldList.add("providerName");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("context")) {
            fieldList.add("context");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("status")) {
            fieldList.add("status");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("version")) {
            fieldList.add("version");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("description")) {
            fieldList.add("description");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("tags")) {
            fieldList.add("tags");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("api-category")) {
            fieldList.add("apiCategories");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("subcontext")) {
            fieldList.add("context");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("doc")) {
            fieldList.add("documentationList.name");
            fieldList.add("documentationList.summary");
            fieldList.add("documentationList.textContent");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("label")) {
            fieldList.add("gatewayLabels");
            return fieldList;
        }
        if (queryCriteria.equalsIgnoreCase("content")) {
            fieldList.add("apiName");
            fieldList.add("providerName");
            fieldList.add("version");
            fieldList.add("context");
            fieldList.add("status");
            fieldList.add("description");
            fieldList.add("swaggerDefinition");
            fieldList.add("tags");
            fieldList.add("gatewayLabels");
            fieldList.add("additionalProperties");
            fieldList.add("apiCategories");
            fieldList.add("documentationList.name");
            fieldList.add("documentationList.summary");
            fieldList.add("documentationList.textContent");
            return fieldList;
        }
        if (!queryCriteria.isEmpty()) {
            fieldList.add("additionalProperties" + "." + queryCriteria);
            return fieldList;
        }

        return fieldList;
    }

    private List<Document> getDevportalSearchAggregate(String query, int skip, int limit) {
        Document searchDoc = buildSearchAggregate(query);
        Document skipDoc = new Document();
        Document limitDoc = new Document();

        skipDoc.put("$skip", skip);
        limitDoc.put("$limit", limit);

        Document matchDoc = new Document();
        Document orDoc = new Document();
        Document andDoc = new Document();
        Document matchPublished = new Document();
        Document matchPrototyped = new Document();
        Document matchRevisions = new Document();
        Document matchExists = new Document();

        List<Document> orMatchList = new ArrayList();
        List<Document> andMatchList = new ArrayList();

        matchPublished.put("status", "PUBLISHED");
        matchPrototyped.put("status", "PROTOTYPED");
        orMatchList.add(matchPublished);
        orMatchList.add(matchPrototyped);
        orDoc.put("$or", orMatchList);

        matchExists.put("$exists", false);
        matchRevisions.put("revision", matchExists);

        andMatchList.add(orDoc);
        andMatchList.add(matchRevisions);

        andDoc.put("$and", andMatchList);
        matchDoc.put("$match", andDoc);

        List<Document> list = new ArrayList<>();
        list.add(searchDoc);
        list.add(matchDoc);
        list.add(skipDoc);
        list.add(limitDoc);
        return list;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException {
        int skip = start;
        int limit = offset;
        MongoCollection<MongoDBDevPortalAPI> collection = MongoDBConnectionUtil.getDevPortalCollection(ctx.getOrganization().getName());
        long totalCount = countTotalApi(org);
        MongoCursor<MongoDBDevPortalAPI> aggregate = collection.aggregate(getDevportalSearchAggregate(searchQuery, skip, limit))
                .cursor();
        DevPortalAPISearchResult devPortalAPISearchResult = new DevPortalAPISearchResult();
        List<DevPortalAPIInfo> devPortalAPIInfoList = new ArrayList<>();
        while (aggregate.hasNext()) {
            MongoDBDevPortalAPI mongoDBAPIDocument = aggregate.next();
            DevPortalAPI api = MongoAPIMapper.INSTANCE.toDevPortalApi(mongoDBAPIDocument);
            devPortalAPIInfoList.add(api);
        }
        devPortalAPISearchResult.setDevPortalAPIInfoList(devPortalAPIInfoList);
        devPortalAPISearchResult.setReturnedAPIsCount(devPortalAPIInfoList.size());
        devPortalAPISearchResult.setTotalAPIsCount((int) totalCount);
        return devPortalAPISearchResult;
    }

    @Override
    public void changeAPILifeCycle(Organization org, String apiId, String status)
            throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
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
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        collection.updateOne(eq("_id", new ObjectId(apiId)), set("swaggerDefinition", apiDefinition));
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        MongoDBPublisherAPI api = collection.find(eq("_id", new ObjectId(apiId)))
                .projection(include("swaggerDefinition")).first();
        if (api == null) {
            throw new OASPersistenceException("Failed to get api definition. Api " + apiId + " does not exist in " +
                    "mongodb");
        }
        return api.getSwaggerDefinition();
    }

    @Override
    public String getAsyncDefinition(Organization org, String apiId) throws AsyncSpecPersistenceException {
        return null;
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
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        ObjectId docId = new ObjectId();
        APIDocumentation apiDocumentation = DocumentationMapper.INSTANCE.toAPIDocumentation(documentation);
        apiDocumentation.setId(docId);
        if (documentation.getType() == DocumentationType.OTHER) {
            apiDocumentation.setOtherTypeName(documentation.getOtherTypeName());
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);
        MongoDBPublisherAPI updatedAPI = collection.findOneAndUpdate(eq("_id", new ObjectId(apiId)),
                push("documentationList", apiDocumentation), options);
        Set<APIDocumentation> documentationList = updatedAPI.getDocumentationList();
        for (APIDocumentation apiDoc : documentationList) {
            if (docId.toString().equalsIgnoreCase(apiDoc.getId().toString())) {
                return DocumentationMapper.INSTANCE.toDocumentation(apiDoc);
            }
        }
        throw new DocumentationPersistenceException("Error adding api documentation in mongodb ");
    }

    @Override
    public DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset,
                                                    String searchQuery, UserContext ctx)
            throws DocumentationPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        MongoDBPublisherAPI documentation = null;

        if (searchQuery == null) {
            documentation = collection.find(eq("_id", new ObjectId(apiId)))
                    .projection(include("documentationList")).first();
        } else {
            String[] splitQuery = searchQuery.split(":");
            if (splitQuery.length != 2) {
                throw new DocumentationPersistenceException("Invalid search query ");
            }
            MongoCursor<MongoDBPublisherAPI> cursor = collection.aggregate(Arrays.asList(
                    match(eq("_id", new ObjectId(apiId))),
                    unwind("$documentationList"),
                    match(eq("documentationList.name", splitQuery[1])),
                    project(include("documentationList")),
                    group(new ObjectId(), Accumulators.push("documentationList",
                            "$documentationList"))
            ))
                    .cursor();
            while (cursor.hasNext()) {
                documentation = cursor.next();
            }
        }

        DocumentSearchResult documentSearchResult = new DocumentSearchResult();
        List<Documentation> documentationList = new ArrayList<>();
        if (documentation == null || documentation.getDocumentationList() == null) {
            documentSearchResult.setDocumentationList(documentationList);
            documentSearchResult.setReturnedDocsCount(documentationList.size());
            documentSearchResult.setReturnedDocsCount(0);
            return documentSearchResult;
        }

        Set<APIDocumentation> apiDocumentationList = documentation.getDocumentationList();
        for (APIDocumentation apiDoc : apiDocumentationList) {
            documentationList.add(DocumentationMapper.INSTANCE.toDocumentation(apiDoc));
        }
        documentSearchResult.setDocumentationList(documentationList);
        documentSearchResult.setReturnedDocsCount(documentationList.size());
        documentSearchResult.setReturnedDocsCount(5);
        return documentSearchResult;
    }

    @Override
    public DocumentContent addDocumentationContent(Organization org, String apiId, String docId,
                                                   DocumentContent content) throws DocumentationPersistenceException {
        String sourceType = content.getSourceType().name();
        if (DocumentContent.ContentSourceType.FILE.name().equalsIgnoreCase(sourceType)) {
            handleFileTypeContent(org, apiId, docId, content);
            return content;
        }
        handleInlineMDTypeContent(org, apiId, docId, content);
        return content;
    }

    @Override
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        APIDocumentation apiDocumentation = DocumentationMapper.INSTANCE.toAPIDocumentation(documentation);
        if (documentation.getType() == DocumentationType.OTHER) {
            apiDocumentation.setOtherTypeName(documentation.getOtherTypeName());
        }
        ObjectId docId = apiDocumentation.getId();
        APIDocumentation mongodbAPIDocumentation = getMongodbAPIDocumentation(org, apiId, docId.toHexString());
        if (mongodbAPIDocumentation != null) {
            apiDocumentation.setTextContent(mongodbAPIDocumentation.getTextContent());
            apiDocumentation.setGridFsReference(mongodbAPIDocumentation.getGridFsReference());
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);
        MongoDBPublisherAPI updatedAPI = collection.findOneAndUpdate(
                and(
                        eq("_id", new ObjectId(apiId)),
                        eq("documentationList.docId", docId)
                ),
                set("documentationList.$", apiDocumentation)
                , options);
        Set<APIDocumentation> documentationList = updatedAPI.getDocumentationList();
        for (APIDocumentation apiDoc : documentationList) {
            if (docId.toString().equalsIgnoreCase(apiDoc.getId().toString())) {
                return DocumentationMapper.INSTANCE.toDocumentation(apiDoc);
            }
        }
        throw new DocumentationPersistenceException("Failed to update documentation" + docId.toString() + "in " +
                "mongodb for api " + apiId);
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {

        return DocumentationMapper.INSTANCE.toDocumentation(getMongodbAPIDocumentation(org, apiId, docId));
    }

    private APIDocumentation getMongodbAPIDocumentation(Organization org, String apiId, String docId) {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        MongoCursor<MongoDBPublisherAPI> cursor = collection.aggregate(Arrays.asList(
                match(eq("_id", new ObjectId(apiId))),
                unwind("$documentationList"),
                match(eq("documentationList.docId", new ObjectId(docId))),
                project(include("documentationList")),
                group(new ObjectId(), Accumulators.push("documentationList",
                        "$documentationList"))
        ))
                .cursor();
        while (cursor.hasNext()) {
            MongoDBPublisherAPI mongoDBAPIDocument = cursor.next();
            APIDocumentation apiDocumentation = (APIDocumentation) mongoDBAPIDocument.getDocumentationList()
                    .toArray()[0];
            return apiDocumentation;
        }
        return null;
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        String orgName = org.getName();
        if (orgName == null) {
            orgName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        APIDocumentation apiDocumentation = getMongodbDocUsingId(org, apiId, docId);
        String sourceType = apiDocumentation.getSourceType().name();
        ObjectId gridFsReference = apiDocumentation.getGridFsReference();

        DocumentContent documentContent = new DocumentContent();
        documentContent.setSourceType(DocumentContent.ContentSourceType.valueOf(sourceType));

        if (DocumentContent.ContentSourceType.FILE.name().equalsIgnoreCase(sourceType) && gridFsReference != null) {
            MongoDatabase database = MongoDBConnectionUtil.getDatabase();
            GridFSBucket gridFSBucket = GridFSBuckets.create(database, orgName);
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFsReference);
            ResourceFile resourceFile = new ResourceFile(downloadStream, apiDocumentation.getContentType());
            resourceFile.setName(downloadStream.getGridFSFile().getFilename());
            documentContent.setResourceFile(resourceFile);
            return documentContent;
        }
        documentContent.setTextContent(apiDocumentation.getTextContent());
        return documentContent;
    }

    @Override
    public void deleteDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        String orgName = org.getName();
        if (orgName == null) {
            orgName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(orgName);
        APIDocumentation apiDocumentation = getMongodbDocUsingId(org, apiId, docId);
        String sourceType = apiDocumentation.getSourceType().name();
        ObjectId gridFsReference = apiDocumentation.getGridFsReference();
        if (DocumentContent.ContentSourceType.FILE.name().equalsIgnoreCase(sourceType) && gridFsReference != null) {
            MongoDatabase database = MongoDBConnectionUtil.getDatabase();
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(database, orgName);
            gridFSFilesBucket.delete(apiDocumentation.getGridFsReference());
        }

        collection.updateOne(
                eq("_id", new ObjectId(apiId)),
                pull(
                        "documentationList",
                        eq("docId", new ObjectId(docId))
                )
        );
    }

    private APIDocumentation getMongodbDocUsingId(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        APIDocumentation apiDocumentation = null;
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());

        MongoCursor<MongoDBPublisherAPI> cursor = collection.aggregate(Arrays.asList(
                match(eq("_id", new ObjectId(apiId))),
                unwind("$documentationList"),
                match(eq("documentationList.docId", new ObjectId(docId))),
                project(include("documentationList")),
                group(new ObjectId(), Accumulators.push("documentationList",
                        "$documentationList"))
        ))
                .cursor();

        //This will only return one api and with one doc
        while (cursor.hasNext()) {
            MongoDBPublisherAPI mongoDBAPIDocument = cursor.next();
            apiDocumentation = (APIDocumentation) mongoDBAPIDocument.getDocumentationList()
                    .toArray()[0];
        }
        if (apiDocumentation == null) {
            throw new DocumentationPersistenceException("Failed to delete documentation. Cannot find " + docId + "in " +
                    "mongodb for api " + apiId);
        }
        return apiDocumentation;
    }

    private void handleFileTypeContent(Organization org, String apiId, String docId, DocumentContent content)
            throws DocumentationPersistenceException {
        MongoDatabase database = MongoDBConnectionUtil.getDatabase();
        String orgName = org.getName();
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(orgName);
        if (orgName == null) {
            orgName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        GridFSBucket gridFSFilesBucket = GridFSBuckets.create(database, orgName);
        ResourceFile resourceFile = content.getResourceFile();
        InputStream inputStream = resourceFile.getContent();
        GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(358400);
        String textContent = null;
        String contentType = resourceFile.getContentType();
        try {
            File file = MongoDBUtil.writeStream(inputStream, resourceFile.getName());
            InputStream extractStream = MongoDBUtil.readStream(file, resourceFile.getName());
            if (contentType.equalsIgnoreCase(APIConstants.DOCUMENTATION_PDF_CONTENT_TYPE)) {
                textContent = MongoDBUtil.extractPDFText(extractStream);
            }
            if (contentType.equalsIgnoreCase(APIConstants.DOCUMENTATION_DOC_CONTENT_TYPE)) {
                textContent = MongoDBUtil.extractDocText(extractStream);
            }
            if (contentType.equalsIgnoreCase(APIConstants.DOCUMENTATION_DOCX_CONTENT_TYPE)) {
                textContent = MongoDBUtil.extractDocXText(extractStream);
            }
            if (contentType.equalsIgnoreCase(APIConstants.DOCUMENTATION_TXT_CONTENT_TYPE)) {
                textContent = MongoDBUtil.extractPlainText(extractStream);
            }

            InputStream gridFsStream = MongoDBUtil.readStream(file, resourceFile.getName());
            ObjectId gridFsReference =
                    gridFSFilesBucket.uploadFromStream(resourceFile.getName(), gridFsStream, options);

            collection.updateOne(
                    and(
                            eq("_id", new ObjectId(apiId)),
                            eq("documentationList.docId", new ObjectId(docId))
                    ),
                    combine(
                            set("documentationList.$.gridFsReference", gridFsReference),
                            set("documentationList.$.contentType", contentType),
                            set("documentationList.$.textContent", textContent)
                    )
            );
        } catch (IOException | PersistenceException e) {
            throw new DocumentationPersistenceException("Failed to extract documentation content for " + docId +
                    " in mongodb, for api " + apiId, e);
        }
    }

    private void handleInlineMDTypeContent(Organization org, String apiId, String docId, DocumentContent content) {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        collection.updateOne(
                and(
                        eq("_id", new ObjectId(apiId)),
                        eq("documentationList.docId", new ObjectId(docId))
                ),
                set("documentationList.$.textContent", content.getTextContent())
        );
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
        MongoDatabase database = MongoDBConnectionUtil.getDatabase();
        String orgName = org.getName();
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(orgName);
        if (orgName == null) {
            orgName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        GridFSBucket gridFSFilesBucket = GridFSBuckets.create(database,
                orgName + MONGODB_GRIDFS_THMBNAIL_SUR_FIX);
        InputStream inputStream = resourceFile.getContent();
        GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(358400);
        try {
            String fileName = resourceFile.getName();
            if (fileName == null) {
                fileName = RandomStringUtils.randomAlphanumeric(10);
            }
            File file = MongoDBUtil.writeStream(inputStream, fileName);
            InputStream gridFsStream = MongoDBUtil.readStream(file, fileName);
            ObjectId gridFsReference =
                    gridFSFilesBucket.uploadFromStream(fileName, gridFsStream, options);
            MongoDBThumbnail mongoDBThumbnail = new MongoDBThumbnail();
            mongoDBThumbnail.setThumbnailReference(gridFsReference);
            mongoDBThumbnail.setContentType(resourceFile.getContentType());
            mongoDBThumbnail.setName(fileName);
            collection.updateOne(
                    eq("_id", new ObjectId(apiId)),
                    set("mongoDBThumbnail", mongoDBThumbnail)
            );
        } catch (PersistenceException e) {
            throw new ThumbnailPersistenceException("Failed to update thumbnail for in mongodb, for api " + apiId, e);
        }
    }

    @Override
    public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        String orgName = org.getName();
        if (orgName == null) {
            orgName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        MongoDBPublisherAPI mongoDBAPIDocument;
        try {
            mongoDBAPIDocument = getMongoDBPublisherAPIFromId(org, apiId, true);
        } catch (APIPersistenceException e) {
            throw new ThumbnailPersistenceException(e);
        }
        MongoDBThumbnail thumbnail = mongoDBAPIDocument.getMongoDBThumbnail();
        if (thumbnail == null) {
            return null;
        }
        MongoDatabase database = MongoDBConnectionUtil.getDatabase();
        GridFSBucket gridFSBucket = GridFSBuckets.create(database, orgName + MONGODB_GRIDFS_THMBNAIL_SUR_FIX);
        GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(thumbnail.getThumbnailReference());
        ResourceFile resourceFile = new ResourceFile(downloadStream, thumbnail.getContentType());
        resourceFile.setName(thumbnail.getName());
        return resourceFile;
    }

    @Override
    public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        MongoDatabase database = MongoDBConnectionUtil.getDatabase();
        String orgName = org.getName();
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(orgName);
        if (orgName == null) {
            orgName = MONGODB_COLLECTION_DEFAULT_ORG;
        }
        MongoDBPublisherAPI mongoDBAPIDocument;
        try {
            mongoDBAPIDocument = getMongoDBPublisherAPIFromId(org, apiId, true);
            MongoDBThumbnail thumbnail = mongoDBAPIDocument.getMongoDBThumbnail();
            GridFSBucket gridFSBucket = GridFSBuckets.create(database, orgName + MONGODB_GRIDFS_THMBNAIL_SUR_FIX);
            gridFSBucket.delete(thumbnail.getThumbnailReference());
            collection.updateOne(
                    eq("_id", new ObjectId(apiId)),
                    unset("mongoDBThumbnail")
            );
            log.info("Successfully deleted thumbnail for api " + apiId);
        } catch (APIPersistenceException e) {
            throw new ThumbnailPersistenceException("Error when deleting thumbnail for " + apiId);
        }
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

    private String getLifecycleStatus(Organization org, String apiId) throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        MongoDBPublisherAPI api = collection.find(eq("_id", new ObjectId(apiId)))
                .projection(include("status")).first();
        if (api == null) {
            throw new APIPersistenceException("Failed to get api status. Api " + apiId + " does not exist in " +
                    "mongodb");
        }
        return api.getStatus();
    }

    @Override
    public Set<Tag> getAllTags(Organization org) {
        Set<Tag> tagSet = new HashSet<>();
        MongoCollection<MongoDBDevPortalAPI> collection = MongoDBConnectionUtil.getDevPortalCollection(org.getName());
        Bson statusFilter = Filters.or(
                Filters.eq("status", "PUBLISHED"),
                Filters.eq("status", "PROTOTYPED")
        );
        Bson revisionFilter = Filters.exists("revision", false);

        MongoCursor<MongoDBDevPortalAPI> cursor = collection.aggregate(Arrays.asList(match(Filters.and(statusFilter,
                revisionFilter)), project(include("_id", "tags")))).cursor();

        Map<String, Integer> tagMap = new HashMap<>();
        while (cursor.hasNext()) {
            MongoDBDevPortalAPI mongoDBAPIDocument = cursor.next();
            List<String> mongoDBAPIDocumentTags = mongoDBAPIDocument.getTags();
            for (String mongoDBAPIDocumentTag : mongoDBAPIDocumentTags) {
                if (tagMap.containsKey(mongoDBAPIDocumentTag)) {
                    int count = tagMap.get(mongoDBAPIDocumentTag);
                    tagMap.put(mongoDBAPIDocumentTag, count + 1);
                } else {
                    tagMap.put(mongoDBAPIDocumentTag, 1);
                }
            }
        }
        if (!tagMap.isEmpty()) {
            for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                Tag tag = new Tag(entry.getKey());
                tag.setNoOfOccurrences(entry.getValue());
                tagSet.add(tag);
            }
        }
        return tagSet;
    }

    private MongoDBPublisherAPI getMongoDBPublisherAPIFromId(Organization org, String apiId, Boolean excludeSwagger)
            throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = MongoDBConnectionUtil.getPublisherCollection(org.getName());
        MongoDBPublisherAPI mongoDBAPIDocument;
        if (excludeSwagger) {
            mongoDBAPIDocument = collection.find(eq("_id", new ObjectId(apiId)))
                    .projection(exclude("swaggerDefinition")).first();
        } else {
            mongoDBAPIDocument = collection.find(eq("_id", new ObjectId(apiId))).first();
        }
        if (mongoDBAPIDocument == null) {
            String msg = "Failed to get API. " + apiId + " does not exist in mongodb database";
            throw new APIPersistenceException(msg);
        }
        return mongoDBAPIDocument;
    }

    @Override
    public List<APICategory> getAllCategories(Organization org) {
        List<APICategory> categoriesList = new ArrayList<>();
        MongoCollection<MongoDBDevPortalAPI> collection = MongoDBConnectionUtil.getDevPortalCollection(org.getName());
        Bson statusFilter = Filters.or(
                Filters.eq("status", "PUBLISHED"),
                Filters.eq("status", "PROTOTYPED")
        );
        Bson revisionFilter = Filters.exists("revision", false);
        MongoCursor<MongoDBDevPortalAPI> cursor = collection.aggregate(Arrays.asList(Aggregates.match(Filters.and(
                statusFilter, revisionFilter)), project(include("_id", "apiCategories")))).cursor();
        while (cursor.hasNext()) {
            MongoDBDevPortalAPI mongoDBAPIDocument = cursor.next();
            DevPortalAPI api = MongoAPIMapper.INSTANCE.toDevPortalApi(mongoDBAPIDocument);
            API mappedAPI = APIMapper.INSTANCE.toApi(api);
            List<APICategory> mappedAPIApiCategories = mappedAPI.getApiCategories();
            categoriesList.addAll(mappedAPIApiCategories);
        }
        return categoriesList;
    }
}
