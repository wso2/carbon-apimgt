/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.persistence.mongodb.mongodbMocks;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.EstimatedDocumentCountOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.RenameCollectionOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MockMongoDBCollection implements MongoCollection {

    @Override
    public MongoNamespace getNamespace() {
        return null;
    }

    @Override
    public Class getDocumentClass() {
        return null;
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return null;
    }

    @Override
    public ReadPreference getReadPreference() {
        return null;
    }

    @Override
    public WriteConcern getWriteConcern() {
        return null;
    }

    @Override
    public ReadConcern getReadConcern() {
        return null;
    }

    @Override
    public MongoCollection withCodecRegistry(CodecRegistry codecRegistry) {
        return null;
    }

    @Override
    public MongoCollection withReadPreference(ReadPreference readPreference) {
        return null;
    }

    @Override
    public MongoCollection withWriteConcern(WriteConcern writeConcern) {
        return null;
    }

    @Override
    public MongoCollection withReadConcern(ReadConcern readConcern) {
        return null;
    }

    @Override
    public long countDocuments() {
        return 0;
    }

    @Override
    public long countDocuments(Bson bson) {
        return 0;
    }

    @Override
    public long countDocuments(Bson bson, CountOptions countOptions) {
        return 0;
    }

    @Override
    public long countDocuments(ClientSession clientSession) {
        return 0;
    }

    @Override
    public long countDocuments(ClientSession clientSession, Bson bson) {
        return 0;
    }

    @Override
    public long countDocuments(ClientSession clientSession, Bson bson, CountOptions countOptions) {
        return 0;
    }

    @Override
    public long estimatedDocumentCount() {
        return 0;
    }

    @Override
    public long estimatedDocumentCount(EstimatedDocumentCountOptions estimatedDocumentCountOptions) {
        return 0;
    }

    @Override
    public FindIterable find() {
        return new MockFindIterable();
    }

    @Override
    public FindIterable find(Bson bson) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        BsonDocument bsonDocument = bson.toBsonDocument(Document.class, pojoCodecRegistry);
//        ObjectId id = bsonDocument.get("_id").asObjectId().getValue();
        return new MockFindIterable();
    }

    @Override
    public FindIterable find(ClientSession clientSession) {
        return new MockFindIterable();
    }

    @Override
    public FindIterable find(ClientSession clientSession, Bson bson) {
        return new MockFindIterable();
    }

    @Override
    public ChangeStreamIterable watch() {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(ClientSession clientSession) {
        return null;
    }

    @Override
    public MapReduceIterable mapReduce(String s, String s1) {
        return null;
    }

    @Override
    public MapReduceIterable mapReduce(ClientSession clientSession, String s, String s1) {
        return null;
    }

    @Override
    public InsertOneResult insertOne(Object o) {
        return new MockInsertOneResult();
    }

    @Override
    public InsertOneResult insertOne(Object o, InsertOneOptions insertOneOptions) {
        return null;
    }

    @Override
    public InsertOneResult insertOne(ClientSession clientSession, Object o) {
        return null;
    }

    @Override
    public InsertOneResult insertOne(ClientSession clientSession, Object o, InsertOneOptions insertOneOptions) {
        return null;
    }

    @Override
    public InsertManyResult insertMany(List list) {
        return null;
    }

    @Override
    public InsertManyResult insertMany(List list, InsertManyOptions insertManyOptions) {
        return null;
    }

    @Override
    public InsertManyResult insertMany(ClientSession clientSession, List list) {
        return null;
    }

    @Override
    public InsertManyResult insertMany(ClientSession clientSession, List list, InsertManyOptions insertManyOptions) {
        return null;
    }

    @Override
    public DeleteResult deleteOne(Bson bson) {
        return null;
    }

    @Override
    public DeleteResult deleteOne(Bson bson, DeleteOptions deleteOptions) {
        return null;
    }

    @Override
    public DeleteResult deleteOne(ClientSession clientSession, Bson bson) {
        return null;
    }

    @Override
    public DeleteResult deleteOne(ClientSession clientSession, Bson bson, DeleteOptions deleteOptions) {
        return null;
    }

    @Override
    public DeleteResult deleteMany(Bson bson) {
        return null;
    }

    @Override
    public DeleteResult deleteMany(Bson bson, DeleteOptions deleteOptions) {
        return null;
    }

    @Override
    public DeleteResult deleteMany(ClientSession clientSession, Bson bson) {
        return null;
    }

    @Override
    public DeleteResult deleteMany(ClientSession clientSession, Bson bson, DeleteOptions deleteOptions) {
        return null;
    }

    @Override
    public UpdateResult replaceOne(Bson bson, Object o) {
        return null;
    }

    @Override
    public UpdateResult replaceOne(Bson bson, Object o, ReplaceOptions replaceOptions) {
        return null;
    }

    @Override
    public UpdateResult replaceOne(ClientSession clientSession, Bson bson, Object o) {
        return null;
    }

    @Override
    public UpdateResult replaceOne(ClientSession clientSession, Bson bson, Object o, ReplaceOptions replaceOptions) {
        return null;
    }

    @Override
    public UpdateResult updateOne(Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public UpdateResult updateOne(Bson bson, Bson bson1, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson bson, Bson bson1, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateMany(Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public UpdateResult updateMany(Bson bson, Bson bson1, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson bson, Bson bson1, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public Object findOneAndDelete(Bson bson) {
        return null;
    }

    @Override
    public Object findOneAndDelete(Bson bson, FindOneAndDeleteOptions findOneAndDeleteOptions) {
        return null;
    }

    @Override
    public Object findOneAndDelete(ClientSession clientSession, Bson bson) {
        return null;
    }

    @Override
    public Object findOneAndDelete(ClientSession clientSession, Bson bson, FindOneAndDeleteOptions findOneAndDeleteOptions) {
        return null;
    }

    @Override
    public Object findOneAndReplace(Bson bson, Object o) {
        MockAPIs mockAPIs = new MockAPIs();
        return mockAPIs.getSingleMongoDBAPI();
    }

    @Override
    public Object findOneAndReplace(Bson bson, Object o, FindOneAndReplaceOptions findOneAndReplaceOptions) {
        MockAPIs mockAPIs = new MockAPIs();
        return mockAPIs.getSingleMongoDBAPI();
    }

    @Override
    public Object findOneAndReplace(ClientSession clientSession, Bson bson, Object o) {
        MockAPIs mockAPIs = new MockAPIs();
        return mockAPIs.getSingleMongoDBAPI();
    }

    @Override
    public Object findOneAndReplace(ClientSession clientSession, Bson bson, Object o, FindOneAndReplaceOptions findOneAndReplaceOptions) {
        MockAPIs mockAPIs = new MockAPIs();
        return mockAPIs.getSingleMongoDBAPI();
    }

    @Override
    public Object findOneAndUpdate(Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(Bson bson, Bson bson1, FindOneAndUpdateOptions findOneAndUpdateOptions) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(ClientSession clientSession, Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(ClientSession clientSession, Bson bson, Bson bson1, FindOneAndUpdateOptions findOneAndUpdateOptions) {
        return null;
    }

    @Override
    public void drop() {

    }

    @Override
    public void drop(ClientSession clientSession) {

    }

    @Override
    public String createIndex(Bson bson) {
        return null;
    }

    @Override
    public String createIndex(Bson bson, IndexOptions indexOptions) {
        return null;
    }

    @Override
    public String createIndex(ClientSession clientSession, Bson bson) {
        return null;
    }

    @Override
    public String createIndex(ClientSession clientSession, Bson bson, IndexOptions indexOptions) {
        return null;
    }

    @Override
    public ListIndexesIterable<Document> listIndexes() {
        return null;
    }

    @Override
    public ListIndexesIterable<Document> listIndexes(ClientSession clientSession) {
        return null;
    }

    @Override
    public void dropIndex(String s) {

    }

    @Override
    public void dropIndex(String s, DropIndexOptions dropIndexOptions) {

    }

    @Override
    public void dropIndex(Bson bson) {

    }

    @Override
    public void dropIndex(Bson bson, DropIndexOptions dropIndexOptions) {

    }

    @Override
    public void dropIndex(ClientSession clientSession, String s) {

    }

    @Override
    public void dropIndex(ClientSession clientSession, Bson bson) {

    }

    @Override
    public void dropIndex(ClientSession clientSession, String s, DropIndexOptions dropIndexOptions) {

    }

    @Override
    public void dropIndex(ClientSession clientSession, Bson bson, DropIndexOptions dropIndexOptions) {

    }

    @Override
    public void dropIndexes() {

    }

    @Override
    public void dropIndexes(ClientSession clientSession) {

    }

    @Override
    public void dropIndexes(DropIndexOptions dropIndexOptions) {

    }

    @Override
    public void dropIndexes(ClientSession clientSession, DropIndexOptions dropIndexOptions) {

    }

    @Override
    public void renameCollection(MongoNamespace mongoNamespace) {

    }

    @Override
    public void renameCollection(MongoNamespace mongoNamespace, RenameCollectionOptions renameCollectionOptions) {

    }

    @Override
    public void renameCollection(ClientSession clientSession, MongoNamespace mongoNamespace) {

    }

    @Override
    public void renameCollection(ClientSession clientSession, MongoNamespace mongoNamespace, RenameCollectionOptions renameCollectionOptions) {

    }

    @Override
    public ListIndexesIterable listIndexes(ClientSession clientSession, Class aClass) {
        return null;
    }

    @Override
    public ListIndexesIterable listIndexes(Class aClass) {
        return null;
    }

    @Override
    public List<String> createIndexes(ClientSession clientSession, List list, CreateIndexOptions createIndexOptions) {
        return null;
    }

    @Override
    public List<String> createIndexes(ClientSession clientSession, List list) {
        return null;
    }

    @Override
    public List<String> createIndexes(List list, CreateIndexOptions createIndexOptions) {
        return null;
    }

    @Override
    public List<String> createIndexes(List list) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(ClientSession clientSession, Bson bson, List list, FindOneAndUpdateOptions findOneAndUpdateOptions) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(ClientSession clientSession, Bson bson, List list) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(Bson bson, List list, FindOneAndUpdateOptions findOneAndUpdateOptions) {
        return null;
    }

    @Override
    public Object findOneAndUpdate(Bson bson, List list) {
        return null;
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson bson, List list, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateMany(ClientSession clientSession, Bson bson, List list) {
        return null;
    }

    @Override
    public UpdateResult updateMany(Bson bson, List list, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateMany(Bson bson, List list) {
        return null;
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson bson, List list, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateOne(ClientSession clientSession, Bson bson, List list) {
        return null;
    }

    @Override
    public UpdateResult updateOne(Bson bson, List list, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateOne(Bson bson, List list) {
        return null;
    }

    @Override
    public BulkWriteResult bulkWrite(ClientSession clientSession, List list, BulkWriteOptions bulkWriteOptions) {
        return null;
    }

    @Override
    public BulkWriteResult bulkWrite(ClientSession clientSession, List list) {
        return null;
    }

    @Override
    public BulkWriteResult bulkWrite(List list, BulkWriteOptions bulkWriteOptions) {
        return null;
    }

    @Override
    public BulkWriteResult bulkWrite(List list) {
        return null;
    }

    @Override
    public MapReduceIterable mapReduce(ClientSession clientSession, String s, String s1, Class aClass) {
        return null;
    }

    @Override
    public MapReduceIterable mapReduce(String s, String s1, Class aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(ClientSession clientSession, List list, Class aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(ClientSession clientSession, List list) {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(ClientSession clientSession, Class aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(List list, Class aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(List list) {
        return null;
    }

    @Override
    public ChangeStreamIterable watch(Class aClass) {
        return null;
    }

    @Override
    public AggregateIterable aggregate(ClientSession clientSession, List list, Class aClass) {
        return null;
    }

    @Override
    public AggregateIterable aggregate(ClientSession clientSession, List list) {
        return null;
    }

    @Override
    public AggregateIterable aggregate(List list, Class aClass) {
        return null;
    }

    @Override
    public AggregateIterable aggregate(List list) {
        return null;
    }

    @Override
    public FindIterable find(ClientSession clientSession, Bson bson, Class aClass) {
        return new MockFindIterable();
    }

    @Override
    public FindIterable find(ClientSession clientSession, Class aClass) {
        return new MockFindIterable();
    }

    @Override
    public FindIterable find(Bson bson, Class aClass) {
        return new MockFindIterable();
    }

    @Override
    public FindIterable find(Class aClass) {
        return new MockFindIterable();
    }

    @Override
    public DistinctIterable distinct(ClientSession clientSession, String s, Bson bson, Class aClass) {
        return null;
    }

    @Override
    public DistinctIterable distinct(ClientSession clientSession, String s, Class aClass) {
        return null;
    }

    @Override
    public DistinctIterable distinct(String s, Bson bson, Class aClass) {
        return null;
    }

    @Override
    public DistinctIterable distinct(String s, Class aClass) {
        return null;
    }

    @Override
    public MongoCollection withDocumentClass(Class aClass) {
        return null;
    }
}
