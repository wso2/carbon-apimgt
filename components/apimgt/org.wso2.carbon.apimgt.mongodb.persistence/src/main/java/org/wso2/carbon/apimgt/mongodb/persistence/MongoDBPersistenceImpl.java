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

package org.wso2.carbon.apimgt.mongodb.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.mongodb.persistence.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.mongodb.persistence.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.mongodb.persistence.mappers.DocumentationMapper;
import org.wso2.carbon.apimgt.mongodb.persistence.mappers.MongoAPIMapper;
import org.wso2.carbon.apimgt.mongodb.persistence.utils.MongoDBConnectionUtil;
import org.wso2.carbon.apimgt.mongodb.persistence.utils.MongoDBUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.mongodb.persistence.dto.APIDocumentation;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Projections.exclude;

public class MongoDBPersistenceImpl implements APIPersistence {

    private static final Log log = LogFactory.getLog(MongoDBPersistenceImpl.class);
    private static Map<String, Boolean> indexCheckCache = new HashMap<>();

    @Override
    public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {

        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        publisherAPI.setCreatedTime(String.valueOf(new Date().getTime()));
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);
        InsertOneResult insertOneResult = collection.insertOne(mongoDBPublisherAPI);
        MongoDBPublisherAPI createdDoc = collection.find(eq("_id", insertOneResult.getInsertedId())).first();

        //Create atlas search index in async manner
        Runnable runnable = () -> {
            isIndexCreated(org.getName());
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return MongoAPIMapper.INSTANCE.toPublisherApi(createdDoc);
    }

    private Boolean isIndexCreated(String organizationName) {
        //Check if index is created flag exists in cache
        Boolean indexCacheCheck = indexCheckCache.get(organizationName);
        if (indexCacheCheck != null && indexCacheCheck) {
            return true;
        }
        //Call mongodb atlas rest api to check if index exists
        boolean isIndexEmpty = MongoDBAtlasAPIConnector.getInstance().getIndexes(organizationName).isEmpty();
        if (!isIndexEmpty) {
            indexCheckCache.put(organizationName, true);
            return true;
        }

        // Should we retry if we failed?
        // Create atlas search index
        Boolean searchIndexes = MongoDBAtlasAPIConnector.getInstance().createSearchIndexes(organizationName);
        indexCheckCache.put(organizationName, searchIndexes);
        return true;
    }

    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI)
            throws APIPersistenceException {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);
        String swaggerDefinition = mongoDBPublisherAPI.getSwaggerDefinition();
        String apiId = mongoDBPublisherAPI.getId();

        //Temporary check
        if (swaggerDefinition == null) {
            try {
                mongoDBPublisherAPI.setSwaggerDefinition(getOASDefinition(org, apiId));
            } catch (OASPersistenceException e) {
                log.error("Error when getting swagger ", e);
                throw new APIPersistenceException("Error when updating api");
            }
        }
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions();
        options.returnDocument(ReturnDocument.AFTER);
        MongoDBPublisherAPI updatedDocument =
                collection.findOneAndReplace(eq("_id", new ObjectId(apiId)), mongoDBPublisherAPI, options);
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
        searchQuery = "";
        int skip = start;
        int limit = offset;
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(ctx.getOrganization().getName());
        long totalCount = collection.countDocuments();
        MongoCursor<MongoDBPublisherAPI> aggregate = collection.aggregate(getSearchAggregate(searchQuery, skip, limit))
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

    private String getSearchQuery(String query) {

        if (!query.contains(":")) {
            return "*" + query + "*";
        }
        String[] queryArray = query.split(":");
        String searchCriteria = queryArray[0];
        String searchQuery = queryArray[1];
        return "*" + searchQuery + "*";
    }

    private List<Document> getSearchAggregate(String query, int skip, int limit) {
        String searchQuery = getSearchQuery(query);
        List<String> paths = new ArrayList<>();
        paths.add("apiName");
        paths.add("providerName");
        paths.add("version");
        paths.add("context");
//        paths.add("status");
//        paths.add("description");
//        paths.add("tags");
//        paths.add("gatewayLabels");
//        paths.add("additionalProperties");

        Document search = new Document();
        Document wildCard = new Document();
        Document wildCardBody = new Document();
        Document skipDoc = new Document();
        Document limitDoc = new Document();
        wildCardBody.put("path", "apiName");
        wildCardBody.put("query", "*");
        wildCardBody.put("allowAnalyzedField", true);
        wildCard.put("wildcard", wildCardBody);
        search.put("$search", wildCard);
        skipDoc.put("$skip", skip);
        limitDoc.put("$limit", limit);

        List<Document> list = new ArrayList<>();
        list.add(search);
        list.add(skipDoc);
        list.add(limitDoc);
        return list;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException {
        int skip = start;
        int limit = offset;
        //published prototyped only
        searchQuery = "";
        MongoCollection<MongoDBDevPortalAPI> collection = getDevPortalCollection(ctx.getOrganization().getName());
        long totalCount = collection.countDocuments();
        MongoCursor<MongoDBDevPortalAPI> aggregate = collection.aggregate(getSearchAggregate(searchQuery, skip, limit))
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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        ObjectId docId = new ObjectId();
        APIDocumentation apiDocumentation = DocumentationMapper.INSTANCE.toAPIDocumentation(documentation);
        apiDocumentation.setId(docId);
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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        APIDocumentation apiDocumentation = DocumentationMapper.INSTANCE.toAPIDocumentation(documentation);
        ObjectId docId = apiDocumentation.getId();
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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
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
            return DocumentationMapper.INSTANCE.toDocumentation(apiDocumentation);
        }
        return null;
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        APIDocumentation apiDocumentation = getMongodbDocUsingId(org, apiId, docId);
        String sourceType = apiDocumentation.getSourceType().name();
        ObjectId gridFsReference = apiDocumentation.getGridFsReference();

        DocumentContent documentContent = new DocumentContent();
        documentContent.setSourceType(DocumentContent.ContentSourceType.valueOf(sourceType));

        if (DocumentContent.ContentSourceType.FILE.name().equalsIgnoreCase(sourceType) && gridFsReference != null) {
            MongoDatabase database = MongoDBConnectionUtil.getDatabase();
            GridFSBucket gridFSBucket = GridFSBuckets.create(database, org.getName());
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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        APIDocumentation apiDocumentation = getMongodbDocUsingId(org, apiId, docId);
        String sourceType = apiDocumentation.getSourceType().name();
        ObjectId gridFsReference = apiDocumentation.getGridFsReference();
        if (DocumentContent.ContentSourceType.FILE.name().equalsIgnoreCase(sourceType) && gridFsReference != null) {
            MongoDatabase database = MongoDBConnectionUtil.getDatabase();
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(database, org.getName());
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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());

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
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
        GridFSBucket gridFSFilesBucket = GridFSBuckets.create(database, org.getName());
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
            log.error("Error when extracting text content from file ", e);
            throw new DocumentationPersistenceException("Failed to documentation content for " + docId +
                    " in mongodb, for api " + apiId);
        }
    }

    private void handleInlineMDTypeContent(Organization org, String apiId, String docId, DocumentContent content) {
        MongoCollection<MongoDBPublisherAPI> collection = getPublisherCollection(org.getName());
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

    private MongoCollection<MongoDBPublisherAPI> getPublisherCollection(String orgName) {
        MongoDatabase database = MongoDBConnectionUtil.getDatabase();
        return database.getCollection(orgName, MongoDBPublisherAPI.class);
    }

    private MongoCollection<MongoDBDevPortalAPI> getDevPortalCollection(String orgName) {
        MongoDatabase database = MongoDBConnectionUtil.getDatabase();
        return database.getCollection(orgName, MongoDBDevPortalAPI.class);
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start,
                                                                  int offset, UserContext ctx) throws APIPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start,
                                                                  int offset, UserContext ctx) throws APIPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }
}
