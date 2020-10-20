package org.wso2.carbon.apimgt.persistence.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIEndpoint;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.DeploymentEnvironments;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.persistence.documents.APIProductIdentifierDocument;
import org.wso2.carbon.apimgt.persistence.documents.CORSConfigurationDocument;
import org.wso2.carbon.apimgt.persistence.documents.MongoDBAPIDocument;
import org.wso2.carbon.apimgt.persistence.documents.OrganizationDocument;
import org.wso2.carbon.apimgt.persistence.documents.TiersDocument;
import org.wso2.carbon.apimgt.persistence.documents.URITemplateDocument;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBPersistenceUtil {

    private static final Log log = LogFactory.getLog(MongoDBPersistenceUtil.class);
    private static MongoClient mongoClient = null;

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
                ConnectionString connectionString = new ConnectionString("mongodb+srv://admin:admin@wso2-apim" +
                        "-cluster.eowdj.azure.mongodb.net/test?retryWrites=true&w=majority");

                ClassModel<MongoDBAPIDocument> mongoDBAPIDocument = ClassModel.builder(MongoDBAPIDocument.class)
                        .enableDiscriminator(true).build();
                ClassModel<Label> label = ClassModel.builder(Label.class)
                        .enableDiscriminator(true).build();
                ClassModel<Scope> Scope = ClassModel.builder(Scope.class)
                        .enableDiscriminator(true).build();
                ClassModel<DeploymentEnvironments> deploymentEnv = ClassModel.builder(DeploymentEnvironments.class)
                        .enableDiscriminator(true).build();
                ClassModel<APIEndpoint> apiEndpoint = ClassModel.builder(APIEndpoint.class)
                        .enableDiscriminator(true).build();
                ClassModel<APICategory> apiCategory = ClassModel.builder(APICategory.class)
                        .enableDiscriminator(true).build();
                ClassModel<Policy> policy = ClassModel.builder(Policy.class)
                        .enableDiscriminator(true).build();
                ClassModel<TiersDocument> tier = ClassModel.builder(TiersDocument.class)
                        .enableDiscriminator(true).build();
                ClassModel<CORSConfigurationDocument> corsConfiguration =
                        ClassModel.builder(CORSConfigurationDocument.class).enableDiscriminator(true).build();
                ClassModel<APIProductIdentifierDocument> apiProductIdentifier =
                        ClassModel.builder(APIProductIdentifierDocument.class).enableDiscriminator(true).build();
                ClassModel<Scope> scope = ClassModel.builder(Scope.class).enableDiscriminator(true).build();
                ClassModel<URITemplateDocument> uriTemplate = ClassModel.builder(URITemplateDocument.class)
                        .enableDiscriminator(true).build();
                ClassModel<OrganizationDocument> organization = ClassModel.builder(OrganizationDocument.class)
                        .enableDiscriminator(true).build();
                CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder()
                        .register(tier, policy, mongoDBAPIDocument, Scope, corsConfiguration,
                                label, deploymentEnv, apiEndpoint, apiCategory, apiProductIdentifier, scope,
                                uriTemplate, organization).build());
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

    /**
     * Close mongo client connections
     */
    public static void closeClientConnections() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
