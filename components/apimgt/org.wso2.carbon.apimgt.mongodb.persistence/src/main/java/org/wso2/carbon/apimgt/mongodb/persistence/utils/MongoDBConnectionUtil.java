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

package org.wso2.carbon.apimgt.mongodb.persistence.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import net.consensys.cava.toml.Toml;
import net.consensys.cava.toml.TomlParseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.wso2.carbon.apimgt.mongodb.persistence.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.mongodb.persistence.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.APIDocumentation;
import org.wso2.carbon.apimgt.persistence.dto.CORSConfiguration;
import org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBConnectionUtil {

    private static final Log log = LogFactory.getLog(MongoDBConnectionUtil.class);
    private static MongoClient mongoClient = null;
    private static TomlParseResult tomlParseResult = null;
    private static String database = null;
    private static final String DEFAULT_DATABASE = "APIM_DB";

    /**
     * Initializes the datasource for mongodb
     */
    public static void initialize() {
        if (mongoClient != null) {
            return;
        }

        synchronized (MongoDBConnectionUtil.class) {
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
                ConnectionString connectionString = new ConnectionString(parsedConnectionString);
                database = connectionString.getDatabase();
                ClassModel<MongoDBPublisherAPI> mongoDBAPIDocument = ClassModel.builder(MongoDBPublisherAPI.class)
                        .enableDiscriminator(false).build();
                ClassModel<PublisherAPI> publisherAPI = ClassModel.builder(PublisherAPI.class)
                        .enableDiscriminator(false).build();
                ClassModel<DeploymentEnvironments> deploymentEnv = ClassModel.builder(DeploymentEnvironments.class)
                        .enableDiscriminator(false).build();
                ClassModel<MongoDBDevPortalAPI> mongoDBDevPortalAPI = ClassModel.builder(MongoDBDevPortalAPI.class)
                        .enableDiscriminator(false).build();
                ClassModel<DevPortalAPI> devPortalAPI = ClassModel.builder(DevPortalAPI.class)
                        .enableDiscriminator(false).build();
                ClassModel<CORSConfiguration> corsConfiguration =
                        ClassModel.builder(CORSConfiguration.class).enableDiscriminator(false).build();
                ClassModel<APIDocumentation> apiDocumentation =
                        ClassModel.builder(APIDocumentation.class).enableDiscriminator(false).build();
                CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder()
                        .register(publisherAPI, deploymentEnv, corsConfiguration, mongoDBAPIDocument,
                                mongoDBDevPortalAPI, devPortalAPI, apiDocumentation).build());
                CodecRegistry codecRegistry = fromRegistries(MongoClientSettings
                        .getDefaultCodecRegistry(), pojoCodecRegistry);

                MongoClientSettings clientSettings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString)
                        .codecRegistry(codecRegistry)
                        .build();
                mongoClient = MongoClients.create(clientSettings);
                log.info("mongodb client created ");
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

    public static MongoDatabase getDatabase(){
        MongoClient mongoClient = getMongoClient();
        if (database != null) {
            return mongoClient.getDatabase(database);
        } else {
            return mongoClient.getDatabase(DEFAULT_DATABASE);
        }
    }
}
