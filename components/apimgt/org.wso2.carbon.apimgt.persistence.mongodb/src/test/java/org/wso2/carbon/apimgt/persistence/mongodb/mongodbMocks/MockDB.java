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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateViewOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;

public class MockDB implements MongoDatabase {

    @Override
    public String getName() {
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
    public MongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
        return null;
    }

    @Override
    public MongoDatabase withReadPreference(ReadPreference readPreference) {
        return null;
    }

    @Override
    public MongoDatabase withWriteConcern(WriteConcern writeConcern) {
        return null;
    }

    @Override
    public MongoDatabase withReadConcern(ReadConcern readConcern) {
        return null;
    }

    @Override
    public MongoCollection<Document> getCollection(String s) {
        return null;
    }

    @Override
    public <TDocument> MongoCollection<TDocument> getCollection(String s, Class<TDocument> aClass) {
        return null;
    }

    @Override
    public Document runCommand(Bson bson) {
        return null;
    }

    @Override
    public Document runCommand(Bson bson, ReadPreference readPreference) {
        return null;
    }

    @Override
    public <TResult> TResult runCommand(Bson bson, Class<TResult> aClass) {
        return null;
    }

    @Override
    public <TResult> TResult runCommand(Bson bson, ReadPreference readPreference, Class<TResult> aClass) {
        return null;
    }

    @Override
    public Document runCommand(ClientSession clientSession, Bson bson) {
        return null;
    }

    @Override
    public Document runCommand(ClientSession clientSession, Bson bson, ReadPreference readPreference) {
        return null;
    }

    @Override
    public <TResult> TResult runCommand(ClientSession clientSession, Bson bson, Class<TResult> aClass) {
        return null;
    }

    @Override
    public <TResult> TResult runCommand(ClientSession clientSession, Bson bson, ReadPreference readPreference, Class<TResult> aClass) {
        return null;
    }

    @Override
    public void drop() {

    }

    @Override
    public void drop(ClientSession clientSession) {

    }

    @Override
    public MongoIterable<String> listCollectionNames() {
        return null;
    }

    @Override
    public ListCollectionsIterable<Document> listCollections() {
        return null;
    }

    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> aClass) {
        return null;
    }

    @Override
    public MongoIterable<String> listCollectionNames(ClientSession clientSession) {
        return null;
    }

    @Override
    public ListCollectionsIterable<Document> listCollections(ClientSession clientSession) {
        return null;
    }

    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(ClientSession clientSession, Class<TResult> aClass) {
        return null;
    }

    @Override
    public void createCollection(String s) {

    }

    @Override
    public void createCollection(String s, CreateCollectionOptions createCollectionOptions) {

    }

    @Override
    public void createCollection(ClientSession clientSession, String s) {

    }

    @Override
    public void createCollection(ClientSession clientSession, String s, CreateCollectionOptions createCollectionOptions) {

    }

    @Override
    public void createView(String s, String s1, List<? extends Bson> list) {

    }

    @Override
    public void createView(String s, String s1, List<? extends Bson> list, CreateViewOptions createViewOptions) {

    }

    @Override
    public void createView(ClientSession clientSession, String s, String s1, List<? extends Bson> list) {

    }

    @Override
    public void createView(ClientSession clientSession, String s, String s1, List<? extends Bson> list, CreateViewOptions createViewOptions) {

    }

    @Override
    public ChangeStreamIterable<Document> watch() {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(List<? extends Bson> list) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> list, Class<TResult> aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> aClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> list) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> list, Class<TResult> aClass) {
        return null;
    }

    @Override
    public AggregateIterable<Document> aggregate(List<? extends Bson> list) {
        return null;
    }

    @Override
    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> list, Class<TResult> aClass) {
        return null;
    }

    @Override
    public AggregateIterable<Document> aggregate(ClientSession clientSession, List<? extends Bson> list) {
        return null;
    }

    @Override
    public <TResult> AggregateIterable<TResult> aggregate(ClientSession clientSession, List<? extends Bson> list, Class<TResult> aClass) {
        return null;
    }
}
