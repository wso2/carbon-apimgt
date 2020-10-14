package org.wso2.carbon.apimgt.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIPersistence;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import netscape.javascript.JSObject;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.model.API;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.persistence.documents.APIProductIdentifierDocument;
import org.wso2.carbon.apimgt.persistence.documents.CORSConfigurationDocument;
import org.wso2.carbon.apimgt.persistence.documents.MongoDBAPIDocument;
import org.wso2.carbon.apimgt.persistence.documents.TiersDocument;
import org.wso2.carbon.apimgt.persistence.documents.URITemplateDocument;
import org.wso2.carbon.apimgt.persistence.utils.MongoDBPersistenceUtil;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBPersistenceImpl implements APIPersistence {

    private static APIPersistence instance = null;
    private static final Log log = LogFactory.getLog(MongoDBPersistenceImpl.class);

    @Override
    public API getAPI(String apiId) {
        MongoCollection<MongoDBAPIDocument> collection = getCollection();
        MongoDBAPIDocument mongoDBAPIDocument =
                collection.find(eq("_id", new ObjectId(apiId))).first();
        if (mongoDBAPIDocument == null) {
            String msg = "Failed to get API. " + apiId + " does not exist in mongodb database";
            log.error(msg);
            return null;
        }
        System.out.println(mongoDBAPIDocument.getApiSecurity());
        try {
            return fromMongoDocToAPI(mongoDBAPIDocument);
        } catch (APIManagementException e) {
            log.error("Error when converting API mongodb ", e);
        }
        return null;
    }

    @Override
    public API updateApi(API api) {
        MongoCollection<MongoDBAPIDocument> collection = getCollection();
        try {
            MongoDBAPIDocument mongoDBAPIDocument = fromAPIToMongoDoc(api);
            MongoDBAPIDocument updatedDocument =
                    collection.findOneAndReplace(eq("_id", new ObjectId(api.getUUID())), mongoDBAPIDocument);
            return fromMongoDocToAPI(updatedDocument);
        } catch (APIManagementException e) {
            log.error("Error when converting API mongodb ", e);
        }
        return null;
    }

    @Override
    public void updateWsdlFromUrl(String apiId, String wsdlUrl) {

    }

    @Override
    public void updateWsdlFromUrl(API api) {

    }

    @Override
    public void updateDocVisibility(String apiId, String visibility, String visibleRoles,
                                    Documentation documentation) {

    }

    @Override
    public void updateWsdlFromWsdlFile(API api, ResourceFile resourceFile) {

    }

    @Override
    public void addLifeCycle(API api) {

    }

    @Override
    public void createAPI(API api) {
        MongoCollection<MongoDBAPIDocument> collection = getCollection();
        MongoDBAPIDocument mongoDBAPIDocument = null;
        try {
            mongoDBAPIDocument = fromAPIToMongoDoc(api);
            collection.insertOne(mongoDBAPIDocument);
        } catch (APIManagementException e) {
            log.error("Error when creating api ", e);
        }
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                                   int end, boolean limitAttributes) {
        return null;
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                                   int end, boolean limitAttributes, boolean isPublisherListing) {
        return null;
    }

    @Override
    public Map<String, Object> searchPaginatedAPIsByContent(Organization requestedOrg, String searchQuery,
                                                            int start, int end, boolean limitAttributes) {
        return null;
    }

    @Override
    public String getGraphqlSchema(String apiId) {
        return null;
    }

    @Override
    public void saveGraphqlSchemaDefinition(API api, String schemaDefinition) {

    }

    @Override
    public void saveGraphqlSchemaDefinition(String apiId, String visibleRoles, String schemaDefinition) {

    }

    @Override
    public void deleteAPI(String apiId) {
        MongoCollection<MongoDBAPIDocument> collection = getCollection();
        collection.deleteOne(eq("_id", new ObjectId(apiId)));
    }

    @Override
    public Documentation getDocumentation(String docId, Organization requestedOrg) {
        return null;
    }

    @Override
    public Map<String, Object> getDocumentContent(String userName, Organization requestedOrg) {
        return null;
    }

    @Override
    public void removeDocumentation(String apiOrProductId, String docId) {

    }

    @Override
    public void updateDocumentation(String apiId, Documentation documentation) {

    }

    @Override
    public List<Documentation> getAllDocumentation(String apiOrProductId) {
        return null;
    }

    @Override
    public void addDocumentation(API api, Documentation documentation) {

    }

    @Override
    public API getLightweightAPIByUUID(String uuid, String requestedOrg) {
        return null;
    }

    @Override
    public Map<String, Object> getAPILifeCycleData(String apiId) {
        return null;
    }

    @Override
    public List<Mediation> getAllApiSpecificMediationPolicies(String apiId) {
        return null;
    }

    @Override
    public Mediation getApiSpecificMediationPolicyFromUUID(String apiOrProductId,
                                                           String mediationPolicyUUID) {
        return null;
    }

    @Override
    public Mediation getApiSpecificMediationPolicyFromUUID(String mediationPolicyUUID) {
        return null;
    }

    @Override
    public void updateApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override
    public void deleteApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override
    public boolean checkIfMediationPolicyExists(String mediationPolicyId) {
        return false;
    }

    @Override
    public void addApiSpecificMediationPolicy(String apiOrProductId, String type, ResourceFile contentFile) {

    }

    @Override
    public void configureMonetizationInAPI(API api) {

    }

    @Override
    public void configureMonetizationInAPI(String apiId, JSONObject monetizationProperties,
                                           boolean isMonetizationEnabled) {

    }

    @Override
    public boolean isSOAPToRESTApi(String apiOrProductId) {
        return false;
    }

    @Override
    public String getRestToSoapConvertedSequence(String apiOrProductId, String seqType) {
        return null;
    }

    @Override
    public String getResourcePolicyFromResourceId(String apiId, String resourceId) {
        return null;
    }

    @Override
    public void updateResourcePolicyFromResourceId(String apiId, String resourceId, String content) {

    }

    @Override
    public String getOASDefinitionOfAPI(String apiOrProductId) {
        return null;
    }

    @Override
    public void saveOASAPIDefinition(String apiId, String apiDefinitionJSON) {

    }

    @Override
    public ResourceFile getIcon(String apiId) {

        return null;
    }

    @Override
    public void saveAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail) {

    }

    @Override
    public boolean isDocumentationExist(String apiOrProductId, String docName) {
        return false;
    }

    @Override
    public ResourceFile getWSDL(String apiId) {
        return null;
    }

    @Override
    public void changeAPILifeCycle(String apiId, String status) {

    }

    @Override
    public int createNewAPIVersion(API api, String newVersion) {
        return 0;
    }

    @Override
    public void saveGraphQLSchemaDefinition(API api, String schemaDefinition) {

    }

    @Override
    public boolean isMediationPolicyExists(APIProvider apiProvider, String mediationPolicyUUID) {
        return false;
    }

    @Override
    public APIProduct getAPIProductbyUUID(String uuid, Organization requestedOrg) {
        return null;
    }

    @Override
    public APIProduct getAPIProduct(String apiProductId) {
        return null;
    }

    @Override
    public void deleteAPIProduct(String apiProductId) {

    }

    @Override
    public Documentation getProductDocumentation(String productId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override
    public boolean isApiExists(APIIdentifier apiIdentifier) {
        return false;
    }

    private MongoCollection<MongoDBAPIDocument> getCollection() {
        MongoClient mongoClient = MongoDBPersistenceUtil.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("APIM_DB");
        return database.getCollection("APIs", MongoDBAPIDocument.class);
    }

    private MongoDBAPIDocument fromAPIToMongoDoc(API api) throws APIManagementException {
        Set<String> visibleRoles = new HashSet<>(Arrays.asList(api.getVisibleRoles().split(",")));
        Set<String> subscriptionAvailableTenants =
                new HashSet<>(Arrays.asList(api.getSubscriptionAvailableTenants().split(",")));
        MongoDBAPIDocument mongoDBAPIDocument = new MongoDBAPIDocument();
//        String uuid = UUID.randomUUID().toString();
//        mongoDBAPIDocument.setUuid(api.getUUID());
        mongoDBAPIDocument.setName(api.getId().getName());
        mongoDBAPIDocument.setVersion(api.getId().getVersion());
        mongoDBAPIDocument.setDefaultVersion(api.isDefaultVersion());
        mongoDBAPIDocument.setProvider(api.getId().getProviderName());
        mongoDBAPIDocument.setContext(api.getContext());
        mongoDBAPIDocument.setDescription(api.getDescription());
        mongoDBAPIDocument.setWsdlUrl(api.getWsdlUrl());
        mongoDBAPIDocument.setWadlUrl(api.getWadlUrl());
        mongoDBAPIDocument.setThumbnailUrl(api.getThumbnailUrl());
        mongoDBAPIDocument.setStatus(api.getStatus());
        mongoDBAPIDocument.setTechnicalOwner(api.getTechnicalOwner());
        mongoDBAPIDocument.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        mongoDBAPIDocument.setBusinessOwner(api.getBusinessOwner());
        mongoDBAPIDocument.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        mongoDBAPIDocument.setVisibility(api.getVisibility());
        mongoDBAPIDocument.setVisibleRoles(visibleRoles);
        mongoDBAPIDocument.setEndpointSecured(api.isEndpointSecured());
        mongoDBAPIDocument.setEndpointAuthDigest(api.isEndpointAuthDigest());
        mongoDBAPIDocument.setEndpointUTUsername(api.getEndpointUTUsername());
        mongoDBAPIDocument.setEndpointUTPassword(api.getEndpointUTPassword());
        mongoDBAPIDocument.setTransports(api.getTransports());
        mongoDBAPIDocument.setInSequence(api.getInSequence());
        mongoDBAPIDocument.setOutSequence(api.getOutSequence());
        mongoDBAPIDocument.setFaultSequence(api.getFaultSequence());
        mongoDBAPIDocument.setResponseCache(api.getResponseCache());
        mongoDBAPIDocument.setCacheTimeout(api.getCacheTimeout());
        mongoDBAPIDocument.setRedirectURL(api.getRedirectURL());
        mongoDBAPIDocument.setApiOwner(api.getApiOwner());
        mongoDBAPIDocument.setAdvertiseOnly(api.isAdvertiseOnly());
        mongoDBAPIDocument.setEndpointConfig(api.getEndpointConfig());
        mongoDBAPIDocument.setSubscriptionAvailability(api.getSubscriptionAvailability());
        mongoDBAPIDocument.setSubscriptionAvailableTenants(subscriptionAvailableTenants);
        mongoDBAPIDocument.setImplementation(api.getImplementation());
        mongoDBAPIDocument.setProductionMaxTps(api.getProductionMaxTps());
        mongoDBAPIDocument.setSandboxMaxTps(api.getSandboxMaxTps());
        mongoDBAPIDocument.setAuthorizationHeader(api.getAuthorizationHeader());
        mongoDBAPIDocument.setApiSecurity(api.getApiSecurity());
        mongoDBAPIDocument.setEnableSchemaValidation(api.isEnabledSchemaValidation());
        mongoDBAPIDocument.setEnableStore(api.isEnableStore());
        mongoDBAPIDocument.setTestKey(api.getTestKey());

        //Validate if the API has an unsupported context before setting it in the artifact
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
            String invalidContext = File.separator + APIConstants.VERSION_PLACEHOLDER;
            if (invalidContext.equals(api.getContextTemplate())) {
                throw new APIManagementException(
                        "API : " + api.getId() + " has an unsupported context : " + api.getContextTemplate());
            }
        } else {
            String invalidContext =
                    APIConstants.TENANT_PREFIX + tenantDomain + File.separator + APIConstants.VERSION_PLACEHOLDER;
            if (invalidContext.equals(api.getContextTemplate())) {
                throw new APIManagementException(
                        "API : " + api.getId() + " has an unsupported context : " + api.getContextTemplate());
            }
        }
        mongoDBAPIDocument.setContextTemplate(api.getContextTemplate());
        mongoDBAPIDocument.setVersionType("context");
        mongoDBAPIDocument.setType(api.getType());

        Set<TiersDocument> tiersDocumentList = new LinkedHashSet<>();
        Set<Tier> availableTiers = api.getAvailableTiers();
        for (Tier tier : availableTiers) {
            TiersDocument tiersDocument = new TiersDocument();
            tiersDocument.setDisplayName(tier.getDisplayName());
            tiersDocument.setDescription(tier.getDescription());
            tiersDocument.setName(tier.getName());
            tiersDocument.setMonetizationAttributes(tier.getMonetizationAttributes());
            tiersDocument.setPolicyContent(tier.getPolicyContent());
            tiersDocument.setRequestCount(tier.getRequestCount());
            tiersDocument.setRequestsPerMin(tier.getRequestsPerMin());
            tiersDocument.setStopOnQuotaReached(tier.isStopOnQuotaReached());
            tiersDocument.setTierAttributes(tier.getTierAttributes());
            tiersDocument.setTierPermission(tier.getTierPermission());
            tiersDocument.setTierPlan(tier.getTierPlan());
            tiersDocument.setTimeUnit(tier.getTimeUnit());
            tiersDocument.setUnitTime(tier.getUnitTime());
            tiersDocumentList.add(tiersDocument);
        }
        mongoDBAPIDocument.setAvailableTiers(tiersDocumentList);
        if (APIConstants.PUBLISHED.equals(api.getStatus())) {
            mongoDBAPIDocument.setLatest(true);
        }

        Set<URITemplateDocument> uriTemplateDocumentList = new LinkedHashSet<>();
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        for (URITemplate uriTemplate : uriTemplates) {
            URITemplateDocument uriTemplateDocument = new URITemplateDocument();
            uriTemplateDocument.setAmznResourceName(uriTemplate.getAmznResourceName());
            uriTemplateDocument.setAmznResourceTimeout(uriTemplate.getAmznResourceTimeout());
            uriTemplateDocument.setApplicableLevel(uriTemplate.getApplicableLevel());
            uriTemplateDocument.setAuthType(uriTemplate.getAuthType());
            uriTemplateDocument.setAuthTypes(uriTemplate.getAuthTypesList());
            uriTemplateDocument.setConditionGroups(uriTemplate.getConditionGroups());
            uriTemplateDocument.setHttpVerb(uriTemplate.getHTTPVerb());
            uriTemplateDocument.setHttpVerbs(uriTemplate.getHttpVerbsList());
            uriTemplateDocument.setId(uriTemplate.getId());
            uriTemplateDocument.setMediationScript(uriTemplate.getMediationScript());
            uriTemplateDocument.setMediationScripts(uriTemplate.getMediationScriptMap());
            uriTemplateDocument.setResourceSandboxURI(uriTemplate.getResourceSandboxURI());
            uriTemplateDocument.setResourceURI(uriTemplate.getResourceURI());
            uriTemplateDocument.setScope(uriTemplate.getScope());
            uriTemplateDocument.setScopes(uriTemplate.getScopesList());
            uriTemplateDocument.setThrottlingConditions(uriTemplate.getThrottlingConditions());
            uriTemplateDocument.setThrottlingTier(uriTemplate.getThrottlingTier());
            uriTemplateDocument.setThrottlingTiers(uriTemplate.getThrottlingTiers());
            uriTemplateDocument.setUriTemplate(uriTemplate.getUriTemplate());

            Set<APIProductIdentifierDocument> usedByProductsList = new LinkedHashSet<>();
            Set<APIProductIdentifier> usedByProducts = uriTemplate.getUsedByProducts();
            for (APIProductIdentifier identifier : usedByProducts) {
                APIProductIdentifierDocument apiProductIdentifierDocument = new APIProductIdentifierDocument();
                apiProductIdentifierDocument.setApiProductName(identifier.getName());
                apiProductIdentifierDocument.setApplicationId(identifier.getApplicationId());
                apiProductIdentifierDocument.setProductId(identifier.getProductId());
                apiProductIdentifierDocument.setProviderName(identifier.getProviderName());
                apiProductIdentifierDocument.setTier(identifier.getTier());
                apiProductIdentifierDocument.setUuid(identifier.getUUID());
                apiProductIdentifierDocument.setVersion(identifier.getVersion());
                usedByProductsList.add(apiProductIdentifierDocument);
            }
            uriTemplateDocument.setUsedByProducts(usedByProductsList);

            uriTemplateDocumentList.add(uriTemplateDocument);
        }
        mongoDBAPIDocument.setUriTemplates(uriTemplateDocumentList);
        mongoDBAPIDocument.setEnvironments(api.getEnvironments());

        CORSConfigurationDocument corsConfigurationDocument = new CORSConfigurationDocument();
        CORSConfiguration corsConfiguration = api.getCorsConfiguration();
        corsConfigurationDocument.setAccessControlAllowCredentials(corsConfiguration.isAccessControlAllowCredentials());
        corsConfigurationDocument.setAccessControlAllowHeaders(corsConfiguration.getAccessControlAllowHeaders());
        corsConfigurationDocument.setAccessControlAllowMethods(corsConfiguration.getAccessControlAllowMethods());
        corsConfigurationDocument.setAccessControlAllowOrigins(corsConfiguration.getAccessControlAllowOrigins());
        mongoDBAPIDocument.setCorsConfiguration(corsConfigurationDocument);

        mongoDBAPIDocument.setGatewayLabels(api.getGatewayLabels());
        mongoDBAPIDocument.setApiCategories(api.getApiCategories());
        mongoDBAPIDocument.setMonetizationEnabled(api.getMonetizationStatus());
        mongoDBAPIDocument.setMonetizationProperties(api.getMonetizationProperties().toJSONString());
        mongoDBAPIDocument.setKeyManagers(api.getKeyManagers());
        mongoDBAPIDocument.setDeploymentEnvironments(api.getDeploymentEnvironments());

        return mongoDBAPIDocument;
    }

    private API fromMongoDocToAPI(MongoDBAPIDocument mongoDBAPIDocument) throws APIManagementException {
        APIIdentifier apiIdentifier = new APIIdentifier(mongoDBAPIDocument.getProvider(), mongoDBAPIDocument.getName()
                , mongoDBAPIDocument.getVersion());
        API api = new API(apiIdentifier);
        api.setUUID(mongoDBAPIDocument.getUuid().toString());
        api.setDescription(mongoDBAPIDocument.getDescription());
        api.setWsdlUrl(mongoDBAPIDocument.getWsdlUrl());
        api.setWadlUrl(mongoDBAPIDocument.getWadlUrl());
        api.setThumbnailUrl(mongoDBAPIDocument.getThumbnailUrl());
        api.setStatus(mongoDBAPIDocument.getStatus());
        api.setTechnicalOwner(mongoDBAPIDocument.getTechnicalOwner());
        api.setTechnicalOwnerEmail(mongoDBAPIDocument.getTechnicalOwnerEmail());
        api.setBusinessOwner(mongoDBAPIDocument.getBusinessOwner());
        api.setBusinessOwnerEmail(mongoDBAPIDocument.getBusinessOwnerEmail());
        api.setVisibility(mongoDBAPIDocument.getVisibility());
        api.setVisibleRoles(String.join(",", mongoDBAPIDocument.getVisibleRoles()));
        api.setEndpointSecured(mongoDBAPIDocument.isEndpointSecured());
        api.setEndpointAuthDigest(mongoDBAPIDocument.isEndpointAuthDigest());
        api.setEndpointUTUsername(mongoDBAPIDocument.getEndpointUTUsername());
        api.setEndpointUTPassword(mongoDBAPIDocument.getEndpointUTPassword());
        api.setTransports(mongoDBAPIDocument.getTransports());
        api.setInSequence(mongoDBAPIDocument.getInSequence());
        api.setOutSequence(mongoDBAPIDocument.getOutSequence());
        api.setFaultSequence(mongoDBAPIDocument.getFaultSequence());
        api.setResponseCache(mongoDBAPIDocument.getResponseCache());
        api.setCacheTimeout(mongoDBAPIDocument.getCacheTimeout());
        api.setRedirectURL(mongoDBAPIDocument.getRedirectURL());
        api.setApiOwner(mongoDBAPIDocument.getApiOwner());
        api.setAdvertiseOnly(mongoDBAPIDocument.isAdvertiseOnly());
        api.setEndpointConfig(mongoDBAPIDocument.getEndpointConfig());
        api.setSubscriptionAvailability(mongoDBAPIDocument.getSubscriptionAvailability());
        api.setSubscriptionAvailableTenants(String.join(",",
                mongoDBAPIDocument.getSubscriptionAvailableTenants()));
        api.setImplementation(mongoDBAPIDocument.getImplementation());
        api.setProductionMaxTps(mongoDBAPIDocument.getProductionMaxTps());
        api.setSandboxMaxTps(mongoDBAPIDocument.getSandboxMaxTps());
        api.setAuthorizationHeader(mongoDBAPIDocument.getAuthorizationHeader());
        api.setApiSecurity(mongoDBAPIDocument.getApiSecurity());
        api.setEnableSchemaValidation(mongoDBAPIDocument.isEnableSchemaValidation());
        api.setEnableStore(mongoDBAPIDocument.isEnableStore());
        api.setTestKey(mongoDBAPIDocument.getTestKey());

        api.setContextTemplate(mongoDBAPIDocument.getContextTemplate());
        api.setType(mongoDBAPIDocument.getType());

        Set<TiersDocument> tiersDocumentList = mongoDBAPIDocument.getAvailableTiers();
        Set<Tier> availableTiers = new LinkedHashSet<>();
        for (TiersDocument tiersDocument : tiersDocumentList) {
            Tier tier = new Tier(tiersDocument.getName());
            tier.setDisplayName(tiersDocument.getDisplayName());
            tier.setDescription(tiersDocument.getDescription());
            tier.setMonetizationAttributes(tiersDocument.getMonetizationAttributes());
            tier.setPolicyContent(tiersDocument.getPolicyContent());
            tier.setRequestCount(tiersDocument.getRequestCount());
            tier.setRequestsPerMin(tiersDocument.getRequestsPerMin());
            tier.setStopOnQuotaReached(tiersDocument.isStopOnQuotaReached());
            tier.setTierAttributes(tiersDocument.getTierAttributes());
            tier.setTierPermission(tiersDocument.getTierPermission());
            tier.setTierPlan(tiersDocument.getTierPlan());
            tier.setTimeUnit(tiersDocument.getTimeUnit());
            tier.setUnitTime(tiersDocument.getUnitTime());
            availableTiers.add(tier);
        }
        api.addAvailableTiers(availableTiers);
        api.setLatest(mongoDBAPIDocument.isLatest());

        Set<URITemplateDocument> uriTemplateDocumentList = mongoDBAPIDocument.getUriTemplates();
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        for (URITemplateDocument uriTemplateDocument : uriTemplateDocumentList) {
            URITemplate uriTemplate = new URITemplate();
            uriTemplate.setAmznResourceName(uriTemplateDocument.getAmznResourceName());
            uriTemplate.setAmznResourceTimeout(uriTemplateDocument.getAmznResourceTimeout());
            uriTemplate.setApplicableLevel(uriTemplateDocument.getApplicableLevel());
            uriTemplate.setAuthType(uriTemplateDocument.getAuthType());

            List<String> authTypesList = uriTemplateDocument.getAuthTypes();
            for (String authType : authTypesList) {
                uriTemplate.setAuthTypes(authType);
            }
            uriTemplate.setConditionGroups(uriTemplateDocument.getConditionGroups());
            uriTemplate.setHTTPVerb(uriTemplateDocument.getHttpVerb());

            Set<String> httpVerbsList = uriTemplateDocument.getHttpVerbs();
            for (String httpVerb : httpVerbsList) {
                uriTemplate.setHttpVerbs(httpVerb);
            }
            uriTemplate.setId(uriTemplateDocument.getId());
            uriTemplate.setMediationScript(uriTemplateDocument.getMediationScript());

            Map<String, String> mediationScripts = uriTemplateDocument.getMediationScripts();
            for (String key : mediationScripts.keySet()) {
                uriTemplate.setMediationScripts(key, mediationScripts.get(key));
            }
            uriTemplate.setResourceSandboxURI(uriTemplateDocument.getResourceSandboxURI());
            uriTemplate.setResourceURI(uriTemplateDocument.getResourceURI());
            uriTemplate.setScope(uriTemplateDocument.getScope());

            List<Scope> scopesList = uriTemplateDocument.getScopes();
            for (Scope scope : scopesList) {
                uriTemplate.setScopes(scope);
            }
            uriTemplate.setThrottlingConditions(uriTemplateDocument.getThrottlingConditions());
            uriTemplate.setThrottlingTier(uriTemplateDocument.getThrottlingTier());
            uriTemplate.setThrottlingTiers(uriTemplateDocument.getThrottlingTiers());
            uriTemplate.setUriTemplate(uriTemplateDocument.getUriTemplate());

            Set<APIProductIdentifierDocument> usedByProductsList = uriTemplateDocument.getUsedByProducts();
            for (APIProductIdentifierDocument identifierDoc : usedByProductsList) {
                APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(identifierDoc.getProviderName(),
                        identifierDoc.getApiProductName(), identifierDoc.getVersion());
                apiProductIdentifier.setApplicationId(identifierDoc.getApplicationId());
                apiProductIdentifier.setProductId(identifierDoc.getProductId());
                apiProductIdentifier.setTier(identifierDoc.getTier());
                apiProductIdentifier.setUUID(identifierDoc.getUuid());
                uriTemplate.addUsedByProduct(apiProductIdentifier);
            }
            uriTemplates.add(uriTemplate);

        }
        api.setUriTemplates(uriTemplates);
        api.setEnvironments(mongoDBAPIDocument.getEnvironments());

        CORSConfigurationDocument corsConfigDoc = mongoDBAPIDocument.getCorsConfiguration();
        if (corsConfigDoc != null) {
            CORSConfiguration corsConfiguration = new CORSConfiguration(corsConfigDoc.isCorsConfigurationEnabled(),
                    corsConfigDoc.getAccessControlAllowOrigins(), corsConfigDoc.isAccessControlAllowCredentials(),
                    corsConfigDoc.getAccessControlAllowHeaders(), corsConfigDoc.getAccessControlAllowMethods());
            api.setCorsConfiguration(corsConfiguration);
        }

        api.setGatewayLabels(api.getGatewayLabels());
        api.setApiCategories(api.getApiCategories());

        api.setMonetizationStatus(mongoDBAPIDocument.isMonetizationEnabled());
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(mongoDBAPIDocument.getMonetizationProperties());
            api.setMonetizationProperties(jsonObj);
        } catch (ParseException e) {
            throw new APIManagementException("Error when parsing monetization properties ", e);
        }

        api.setKeyManagers(api.getKeyManagers());
        api.setDeploymentEnvironments(api.getDeploymentEnvironments());
        return api;
    }

    /**
     * Method to get the instance of the Mongo DB Persistance .
     *
     * @return {@link MongoDBPersistenceImpl} instance
     */
    public static APIPersistence getInstance() {
        if (instance == null) {
            instance = new MongoDBPersistenceImpl();
        }
        return instance;
    }
}
