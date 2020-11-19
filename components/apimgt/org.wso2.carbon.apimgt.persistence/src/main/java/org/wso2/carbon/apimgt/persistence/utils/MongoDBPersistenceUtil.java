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

package org.wso2.carbon.apimgt.persistence.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlParseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIEndpoint;
import org.wso2.carbon.apimgt.persistence.dto.CORSConfiguration;
import org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
//import org.wso2.carbon.apimgt.persistence.documents.APIProductIdentifierDocument;
//import org.wso2.carbon.apimgt.persistence.documents.CORSConfigurationDocument;
//import org.wso2.carbon.apimgt.persistence.documents.MongoDBAPIDocument;
//import org.wso2.carbon.apimgt.persistence.documents.OrganizationDocument;
//import org.wso2.carbon.apimgt.persistence.documents.TiersDocument;
//import org.wso2.carbon.apimgt.persistence.documents.URITemplateDocument;
import org.wso2.carbon.apimgt.persistence.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBPersistenceUtil {

    private static final Log log = LogFactory.getLog(MongoDBPersistenceUtil.class);
    private static MongoClient mongoClient = null;
    private static TomlParseResult tomlParseResult = null;

    /**
     * Initializes the datasource for mongodb
     */
    public static void initialize() {
        if (mongoClient != null) {
            return;
        }

        synchronized (MongoDBPersistenceUtil.class) {
            if (mongoClient == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing mongodb datasource");
                }

                if (tomlParseResult == null) {
                    Path source = Paths.get(CarbonUtils.getCarbonConfigDirPath() + File.separator + "deployment.toml");
                    try {
                        tomlParseResult = Toml.parse(source);
                    } catch (IOException e) {
                        log.error("error when parsing toml ");
                    }
                }

                String parsedConnectionString = tomlParseResult.getString("database.reg_db.connectionString");
                log.info("mongodb connection string" + parsedConnectionString);
                ConnectionString connectionString = new ConnectionString(parsedConnectionString);

                ClassModel<MongoDBPublisherAPI> mongoDBAPIDocument = ClassModel.builder(MongoDBPublisherAPI.class)
                        .enableDiscriminator(true).build();
                ClassModel<PublisherAPI> publisherAPI = ClassModel.builder(PublisherAPI.class)
                        .enableDiscriminator(true).build();
//                ClassModel<Label> label = ClassModel.builder(Label.class)
//                        .enableDiscriminator(true).build();
//                ClassModel<Scope> Scope = ClassModel.builder(Scope.class)
//                        .enableDiscriminator(true).build();
                ClassModel<DeploymentEnvironments> deploymentEnv = ClassModel.builder(DeploymentEnvironments.class)
                        .enableDiscriminator(true).build();
//                ClassModel<APIEndpoint> apiEndpoint = ClassModel.builder(APIEndpoint.class)
//                        .enableDiscriminator(true).build();
//                ClassModel<APICategory> apiCategory = ClassModel.builder(APICategory.class)
//                        .enableDiscriminator(true).build();
//                ClassModel<Policy> policy = ClassModel.builder(Policy.class)
//                        .enableDiscriminator(true).build();
//                ClassModel<TiersDocument> tier = ClassModel.builder(TiersDocument.class)
//                        .enableDiscriminator(true).build();
                ClassModel<CORSConfiguration> corsConfiguration =
                        ClassModel.builder(CORSConfiguration.class).enableDiscriminator(true).build();
//                ClassModel<APIProductIdentifierDocument> apiProductIdentifier =
//                        ClassModel.builder(APIProductIdentifierDocument.class).enableDiscriminator(true).build();
//                ClassModel<Scope> scope = ClassModel.builder(Scope.class).enableDiscriminator(true).build();
//                ClassModel<URITemplateDocument> uriTemplate = ClassModel.builder(URITemplateDocument.class)
//                        .enableDiscriminator(true).build();
//                ClassModel<OrganizationDocument> organization = ClassModel.builder(OrganizationDocument.class)
//                        .enableDiscriminator(true).build();
                ClassModel<JSONObject> jsonObject = ClassModel.builder(JSONObject.class)
                        .enableDiscriminator(true).build();
                CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder()
                        .register(publisherAPI, deploymentEnv, corsConfiguration, mongoDBAPIDocument,
                                jsonObject).build());
                CodecRegistry codecRegistry = fromRegistries(MongoClientSettings
                        .getDefaultCodecRegistry(), pojoCodecRegistry);

                MongoClientSettings clientSettings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString)
                        .codecRegistry(codecRegistry)
                        .build();
                mongoClient = MongoClients.create(clientSettings);
            }
        }
    }

    /**
     * Utility method to get a new mongodb database connection
     *
     * @return MongoClient
     */
    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            initialize();
        }
        return mongoClient;
    }
}
