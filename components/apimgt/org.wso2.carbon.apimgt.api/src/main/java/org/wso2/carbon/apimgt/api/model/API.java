/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.model.policy.Policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Provider's & system's view of API
 */
@SuppressWarnings("unused")
public class API implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(API.class);

    private APIIdentifier id;

    // uuid of registry artifact
    // this id is provider's username independent
    private String uuid;

    private String description;
    private String url;
    private String sandboxUrl;
    private String wsdlUrl;
    private String wsdlArchivePath;
    private String wadlUrl;
    private String swaggerDefinition;
    private String graphQLSchema;
    private String asyncApiDefinition;
    private String type;
    private String context;
    private String contextTemplate;
    private String thumbnailUrl;
    private ResourceFile wsdlResource;
    private Set<String> tags = new LinkedHashSet<String>();
    private Set<Documentation> documents = new LinkedHashSet<Documentation>();
    private String httpVerb;
    private Date lastUpdated;
    private String updatedBy;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private Set<Policy> availableSubscriptionLevelPolicies = new LinkedHashSet<Policy>();
    private String apiLevelPolicy;
    private AuthorizationPolicy authorizationPolicy;
    private Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
    private String organization;
    private String versionTimestamp;

    //dirty pattern to identify which parts to be updated
    private boolean apiHeaderChanged;
    private boolean apiResourcePatternsChanged;

    private String status;

    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;

    // Used for keeping Production & Sandbox Throttling limits.
    private String productionMaxTps;
    private String sandboxMaxTps;

    private String visibility;
    private String visibleRoles;
    private String visibleTenants;

    private boolean endpointSecured = false;
    private boolean endpointAuthDigest = false;
    private String endpointUTUsername;
    private String endpointUTPassword;

    private String transports;
    private String inSequence;
    private String outSequence;
    private String faultSequence;
    
    private Mediation inSequenceMediation;
    private Mediation outSequenceMediation;
    private Mediation faultSequenceMediation;

    private String oldInSequence;
    private String oldOutSequence;
    private String oldFaultSequence;

    private boolean advertiseOnly;
    private String apiExternalProductionEndpoint;
    private String apiExternalSandboxEndpoint;
    private String apiOwner;
    private String redirectURL;
    private String vendor;

    private String subscriptionAvailability;
    private String subscriptionAvailableTenants;
    private CORSConfiguration corsConfiguration;
    private String endpointConfig;
    private WebsubSubscriptionConfiguration websubSubscriptionConfiguration;
    private WebSocketTopicMappingConfiguration webSocketTopicMappingConfiguration;

    private Map<String, String> wsUriMapping;

    private String responseCache;
    private int cacheTimeout;

    private String implementation = "ENDPOINT";

    private String monetizationCategory;
    
    private List<SOAPToRestSequence> soapToRestSequences;

    public List<SOAPToRestSequence> getSoapToRestSequences() {
        return soapToRestSequences;
    }

    public void setSoapToRestSequences(List<SOAPToRestSequence> soapToRestSequences) {
        this.soapToRestSequences = soapToRestSequences;
    }

    //Custom authorization header specific to the API
    private String authorizationHeader;
    private Set<Scope> scopes;

    private boolean isDefaultVersion = false;
    private boolean isPublishedDefaultVersion = false;
    private List<String> keyManagers = new ArrayList<>();
    private JSONObject serviceInfo = new JSONObject();
    /**
     * Used to set the workflow status in lifecycle state change workflow
     */
    private String workflowStatus = null;

    private Set<String> environments;

    private String createdTime;
    /**
     * Customized properties relevant to the particular API.
     */
    private JSONObject additionalProperties;

    /**
     * Properties relevant to monetization of the particular API.
     */
    private JSONObject monetizationProperties = new JSONObject();

    /**
     * Property to indicate the monetization status of the particular API.
     */
    private boolean isMonetizationEnabled = false;

    // Used for endpoint environments configured with non empty URLs
    private Set<String> environmentList;

    // API security at the gateway level.
    private String apiSecurity = "oauth2";

    private static final String NULL_VALUE = "NULL";

    private List<APIEndpoint> endpoints = new ArrayList<APIEndpoint>();

    /**
     * Property to hold the enable/disable status of the json schema validation.
     */
    private boolean enableSchemaValidation = false;

    /**
     * Property to enable/disable WebSub intent verification
     */
    private boolean enableSubscriberVerification = false;

    private List<APICategory> apiCategories;

    /**
     * Property to hold enable/disable status of the store visibility.
     */
    private boolean enableStore = true;

    private String testKey;

    /**
     * Property to indicate whether this is a revision.
     */
    private boolean isRevision = false;

    /**
     * Property to hold api id of a revision.
     */
    private String revisionedApiId;

    /**
     * Property to hold revision id
     */
    private int revisionId;
    
    private String audience;

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public void setEnvironmentList(Set<String> environmentList) {
        this.environmentList = environmentList;
    }

    public Set<String> getEnvironmentList() {
        return environmentList;
    }

    /**
     * To get the additional properties
     *
     * @return additional properties of the API
     */
    public JSONObject getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * To assign a set of customized properties to the API.
     *
     * @param properties Properties that need to be assigned to.
     */
    public void setAdditionalProperties(JSONObject properties) {
        this.additionalProperties = properties;
    }

    public JSONObject getServiceInfoObject() { return serviceInfo; }

    public void setServiceInfo(String key, String value) { this.serviceInfo.put(key, value); }

    public void setServiceInfo(JSONObject serviceInfo) { this.serviceInfo = serviceInfo; }

    public String getServiceInfo(String key) {
        if (serviceInfo != null && serviceInfo.get(key) != null) {
            return serviceInfo.get(key).toString();
        } else {
            return null;
        }
    }
    /**
     * This method is used to get the properties related to monetization
     *
     * @return properties related to monetization
     */
    public JSONObject getMonetizationProperties() {
        return monetizationProperties;
    }

    /**
     * This method is used to get the monetization status (true or false)
     *
     * @return flag to indicate the monetization status (true or false)
     */
    @Deprecated
    public boolean getMonetizationStatus() {
        return isMonetizationEnabled;
    }

    /**
     * This method is used to set the monetization status (true or false)
     *
     * @param monetizationStatus flag to indicate the monetization status (true or false)
     */
    @Deprecated
    public void setMonetizationStatus(boolean monetizationStatus) {
        this.isMonetizationEnabled = monetizationStatus;
    }
    
    /**
     * This method is used to get the properties related to monetization
     *
     * @return properties related to monetization
     */
    public boolean isMonetizationEnabled() {
        return isMonetizationEnabled;
    }

    /**
     * This method is used to set the monetization status (true or false)
     *
     * @param isMonetizationEnabled flag to indicate the monetization status (true or false)
     */
    public void setMonetizationEnabled(boolean isMonetizationEnabled) {
        this.isMonetizationEnabled = isMonetizationEnabled;
    }

    /**
     * This method is used to set the monetization properties
     *
     * @param monetizationProperties properties related to monetization
     */
    public void setMonetizationProperties(JSONObject monetizationProperties) {
        this.monetizationProperties = monetizationProperties;
    }

    /**
     * This method is used to add monetization property
     *
     * @param key   key of the monetization property
     * @param value applicable value of the monetization property
     */
    public void addMonetizationProperty(String key, String value) {
        monetizationProperties.put(key, value);
    }

    /**
     * To add a new property to additional properties list.
     *
     * @param key   Name of the property.
     * @param value Value of the property.
     */
    public void addProperty(String key, String value) {
        additionalProperties.put(key, value);
    }

    /**
     * To get the value of the property.
     *
     * @param key Name of the property
     * @return value of the property.
     */
    public String getProperty(String key) {
        return additionalProperties.get(key).toString();
    }

    /**
     * Publisher access control related parameters.
     * AccessControl -> Specifies whether that particular API is restricted to certain set of publishers and creators.
     * AccessControlRoles -> Specifies the roles that the particular API is visible to.
     */
    private String accessControl;
    private String accessControlRoles;

    public String getSwaggerDefinition() {
        return swaggerDefinition;
    }

    public void setSwaggerDefinition(String swaggerDefinition) {
        this.swaggerDefinition = swaggerDefinition;
    }

    public void setGraphQLSchema(String graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public String getGraphQLSchema() {
        return graphQLSchema;
    }

    public String getAsyncApiDefinition() {
        return asyncApiDefinition;
    }

    public void setAsyncApiDefinition(String asyncApiDefinition) {
        this.asyncApiDefinition = asyncApiDefinition;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    /**
     * Contains flag indicating whether dummy backend or not
     *
     * @return
     */
    public String getImplementation() {
        return implementation;
    }

    /**
     * Returns flag indicating whether dummy backend or not
     *
     * @param implementation
     */
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    /**
     * The average rating provided by the API subscribers
     */
    private float rating;

    private boolean isLatest;

    //TODO: missing - total user count, up time statistics,tier
    @Deprecated
    public String getUUID() {
        return uuid;
    }
    
    @Deprecated
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    } 
    
    public String getProductionMaxTps() {
        return productionMaxTps;
    }

    public void setProductionMaxTps(String productionMaxTps) {
        this.productionMaxTps = productionMaxTps;
    }

    public String getSandboxMaxTps() {
        return sandboxMaxTps;
    }

    public void setSandboxMaxTps(String sandboxMaxTps) {
        this.sandboxMaxTps = sandboxMaxTps;
    }

    public boolean isAdvertiseOnly() {
        return advertiseOnly;
    }

    public void setAdvertiseOnly(boolean advertiseOnly) {
        this.advertiseOnly = advertiseOnly;
    }

    public String getApiExternalProductionEndpoint() {
        return apiExternalProductionEndpoint;
    }

    public void setApiExternalProductionEndpoint(String apiExternalProductionEndpoint) {
        this.apiExternalProductionEndpoint = apiExternalProductionEndpoint;
    }

    public String getApiExternalSandboxEndpoint() {
        return apiExternalSandboxEndpoint;
    }

    public void setApiExternalSandboxEndpoint(String apiExternalSandboxEndpoint) {
        this.apiExternalSandboxEndpoint = apiExternalSandboxEndpoint;
    }

    public String getApiOwner() {
        return apiOwner;
    }

    public void setApiOwner(String apiOwner) {
        this.apiOwner = apiOwner;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getAdvertiseOnlyAPIVendor() {
        return vendor;
    }

    public void setAdvertiseOnlyAPIVendor(String advertiseOnlyAPIVendor) {
        this.vendor = advertiseOnlyAPIVendor;
    }

    public API(APIIdentifier id) {
        this.id = id;
        additionalProperties = new JSONObject();
    }

    public APIIdentifier getId() {
        return id;
    }

    public void setId(APIIdentifier id) {
        this.id = id;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSandboxUrl() {
        return sandboxUrl;
    }

    public void setSandboxUrl(String sandboxUrl) {
        this.sandboxUrl = sandboxUrl;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }
    
    @Deprecated
    public void addTags(Set<String> tags) {
        this.tags.addAll(tags);
    }
    
    public void setTags(Set<String> tags) {
        this.tags.addAll(tags);
    }

    public void removeTags(Set<String> tags) {
        this.tags.removeAll(tags);
    }

    public Set<Documentation> getDocuments() {
        return Collections.unmodifiableSet(documents);
    }

    public void addDocuments(Set<Documentation> documents) {
        this.documents.addAll(documents);
    }

    public void removeDocuments(Set<Documentation> documents) {
        this.documents.removeAll(documents);
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public Date getLastUpdated() {
        if (lastUpdated != null) {
            return new Date(lastUpdated.getTime());
        }
        return null;
    }

    public void setLastUpdated(Date lastUpdated) {
        if (lastUpdated != null) {
            this.lastUpdated = new Date(lastUpdated.getTime());
        }
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Set<Tier> getAvailableTiers() {
        return Collections.unmodifiableSet(availableTiers);
    }

    @Deprecated
    public void addAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.addAll(availableTiers);
    }

    public void setAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
        this.availableTiers.addAll(availableTiers);
    } 
    /**
     * Removes all Tiers from the API object.
     */
    public void removeAllTiers() {
        availableTiers.clear();
    }

    /**
     * Removes all Policies from the API object.
     */
    public void removeAllPolicies() {
        availableSubscriptionLevelPolicies.clear();
    }

    public void removeAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
    }

    public Set<URITemplate> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(Set<URITemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(APIStatus status) {
        this.status = status.getStatus();
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }

    /**
     * @return true if the current version of the API is the latest
     */
    public boolean isLatest() {
        return isLatest;
    }

    public AuthorizationPolicy getAuthorizationPolicy() {
        return authorizationPolicy;
    }

    public void setAuthorizationPolicy(AuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
    }

    public String getWadlUrl() {
        return wadlUrl;
    }

    public void setWadlUrl(String wadlUrl) {
        this.wadlUrl = wadlUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(String visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public String getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(String visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    public boolean isApiHeaderChanged() {
        return apiHeaderChanged;
    }

    public void setApiHeaderChanged(boolean apiHeaderChanged) {
        this.apiHeaderChanged = apiHeaderChanged;
    }

    public boolean isApiResourcePatternsChanged() {
        return apiResourcePatternsChanged;
    }

    public void setApiResourcePatternsChanged(boolean apiResourcePatternsChanged) {
        this.apiResourcePatternsChanged = apiResourcePatternsChanged;
    }

    /**
     * @return the endpointUTUsername
     */
    public String getEndpointUTUsername() {
        return endpointUTUsername;
    }

    /**
     * @param endpointUTUsername the endpointUTUsername to set
     */
    public void setEndpointUTUsername(String endpointUTUsername) {
        this.endpointUTUsername = endpointUTUsername;
    }

    /**
     * @return the endpointUTPassword
     */
    public String getEndpointUTPassword() {
        return endpointUTPassword;
    }

    /**
     * @param endpointUTPassword the endpointUTPassword to set
     */
    public void setEndpointUTPassword(String endpointUTPassword) {
        this.endpointUTPassword = endpointUTPassword;
    }

    /**
     * @return the endpointSecured
     */
    public boolean isEndpointSecured() {
        return endpointSecured;
    }

    /**
     * @param endpointSecured the endpointSecured to set
     */
    public void setEndpointSecured(boolean endpointSecured) {
        this.endpointSecured = endpointSecured;
    }

    /**
     * @return the endpointAuthDigest
     */
    public boolean isEndpointAuthDigest() {
        return endpointAuthDigest;
    }

    /**
     * @param endpointAuthDigest the endpointAuthDigest to set
     */
    public void setEndpointAuthDigest(boolean endpointAuthDigest) {
        this.endpointAuthDigest = endpointAuthDigest;
    }

    public String getInSequence() {
        return inSequence;
    }

    /**
     * @param inSeq insequence for the API
     */
    public void setInSequence(String inSeq) {
        this.inSequence = inSeq;
    }

    public String getOutSequence() {
        return outSequence;
    }

    /**
     * @param outSeq outSequence for the API
     */
    public void setOutSequence(String outSeq) {
        this.outSequence = outSeq;
    }

    /**
     * remove custom sequences from api object
     */
    public void removeCustomSequences() {
        this.inSequence = null;
        this.outSequence = null;
        this.faultSequence = null;
    }

    public String getOldInSequence() {
        return oldInSequence;
    }

    public void setOldInSequence(String oldInSequence) {
        this.oldInSequence = oldInSequence;
    }

    public String getOldOutSequence() {
        return oldOutSequence;
    }

    public void setOldOutSequence(String oldOutSequence) {
        this.oldOutSequence = oldOutSequence;
    }

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public String getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }

    public void setSubscriptionAvailableTenants(String subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
    }

    public String getEndpointConfig() {
        // This is to support new Endpoint object
        if ((endpointConfig == null || StringUtils.isAllEmpty(endpointConfig) && endpoints.size() > 0)) {
            return getEndpointConfigString(endpoints);
        }

        if (isEndpointSecured()) {
            try {
                JSONParser parser = new JSONParser();
                ObjectMapper objectMapper = new ObjectMapper();
                JSONObject endpointConfigJson = (JSONObject) parser.parse(endpointConfig);
                EndpointSecurity productionAndSandbox = new EndpointSecurity();
                if (endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) == null) {
                    JSONObject epSecurity = new JSONObject();
                    productionAndSandbox.setEnabled(true);
                    if (isEndpointAuthDigest()) {
                        productionAndSandbox.setType(APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST);
                    } else {
                        productionAndSandbox.setType(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC);
                    }
                    productionAndSandbox.setUsername(getEndpointUTUsername());
                    productionAndSandbox.setPassword(getEndpointUTPassword());
                    Object productionAndSandboxSecurity = parser.parse(
                            objectMapper.writeValueAsString(productionAndSandbox));
                    epSecurity.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, productionAndSandboxSecurity);
                    epSecurity.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, productionAndSandboxSecurity);
                    endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, epSecurity);
                } else {
                    JSONObject endpointSecurity = (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY);
                    if (endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) == null) {
                        EndpointSecurity production = new EndpointSecurity();
                        production.setEnabled(true);
                        if (isEndpointAuthDigest()) {
                            productionAndSandbox.setType(APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST);
                        } else {
                            productionAndSandbox.setType(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC);
                        }
                        production.setUsername(getEndpointUTUsername());
                        production.setPassword(getEndpointUTPassword());
                        String productionSecurity = objectMapper.writeValueAsString(production);
                        endpointSecurity.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION,
                                parser.parse(productionSecurity));
                        endpointConfigJson.replace(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    }
                    if (endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) == null) {
                        EndpointSecurity sandbox = new EndpointSecurity();
                        sandbox.setEnabled(true);
                        if (isEndpointAuthDigest()) {
                            productionAndSandbox.setType(APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST);
                        } else {
                            productionAndSandbox.setType(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC);
                        }
                        sandbox.setUsername(getEndpointUTUsername());
                        sandbox.setPassword(getEndpointUTPassword());
                        String sandboxSecurity = objectMapper.writeValueAsString(sandbox);
                        endpointSecurity.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, parser.parse(sandboxSecurity));
                        endpointConfigJson.replace(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    }
                }
                endpointConfig = objectMapper.writeValueAsString(endpointConfigJson);
            } catch (ParseException e) {
                log.error("Error while retrieving endpoint config for API : " + getUUID(), e);
            } catch (JsonProcessingException e) {
                log.error("Error while processing endpoint config JSON for API : " + getUUID(), e);
            } catch (Exception e) {
                log.error("Error while processing endpoint config for API : " + getUUID(), e);
            }
        }
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getResponseCache() {
        return responseCache;
    }

    public void setResponseCache(String responseCache) {
        this.responseCache = responseCache;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public String getFaultSequence() {
        return faultSequence;
    }

    public void setFaultSequence(String faultSequence) {
        this.faultSequence = faultSequence;
    }

    public String getOldFaultSequence() {
        return oldFaultSequence;
    }

    public void setOldFaultSequence(String oldFaultSequence) {
        this.oldFaultSequence = oldFaultSequence;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    @Deprecated
    public void setAsDefaultVersion(boolean value) {
        isDefaultVersion = value;
    }

    public void setDefaultVersion(boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public void setAsPublishedDefaultVersion(boolean value) {
        isPublishedDefaultVersion = value;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public boolean isPublishedDefaultVersion() {
        return isPublishedDefaultVersion;
    }

    public CORSConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CORSConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public String getMonetizationCategory() {
        return this.monetizationCategory;
    }

    public void setMonetizationCategory(String monetizationCategory) {
        this.monetizationCategory = monetizationCategory;
    }

    public WebsubSubscriptionConfiguration getWebsubSubscriptionConfiguration() {
        return websubSubscriptionConfiguration;
    }

    public void setWebsubSubscriptionConfiguration(WebsubSubscriptionConfiguration websubSubscriptionConfiguration) {
        this.websubSubscriptionConfiguration = websubSubscriptionConfiguration;
    }

    public WebSocketTopicMappingConfiguration getWebSocketTopicMappingConfiguration() {
        return webSocketTopicMappingConfiguration;
    }

    public void setWebSocketTopicMappingConfiguration(WebSocketTopicMappingConfiguration webSocketTopicMappingConfiguration) {
        this.webSocketTopicMappingConfiguration = webSocketTopicMappingConfiguration;
    }

    public Map<String, String> getWsUriMapping() {
        return wsUriMapping;
    }

    public void setWsUriMapping(Map<String, String> wsUriMapping) {
        this.wsUriMapping = wsUriMapping;
    }

    public String getApiLevelPolicy() {
        return apiLevelPolicy;
    }

    public void setApiLevelPolicy(String apiLevelPolicy) {
        this.apiLevelPolicy = apiLevelPolicy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (StringUtils.isEmpty(type) || NULL_VALUE.equalsIgnoreCase(StringUtils.trim(type))) {
            this.type = "HTTP";
        } else {
            this.type = StringUtils.trim(type).toUpperCase();
        }
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(String accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    /**
     * Check the status of the Json schema validation property.
     *
     * @return Status of the validator property.
     */
    @Deprecated
    public boolean isEnabledSchemaValidation() {
        return enableSchemaValidation;
    }

    /**
     * To set the JSON schema validation enable/disable.
     *
     * @param enableSchemaValidation Given Status.
     */
    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
    }

    /**
     * Check the status of the enableSubscriberVerification property
     *
     * @return status of the property
     */
    public boolean isEnableSubscriberVerification() {
        return enableSubscriberVerification;
    }

    /**
     * To set enableSubscriberVerification property
     *
     * @param enableSubscriberVerification Given status
     */
    public void setEnableSubscriberVerification(boolean enableSubscriberVerification) {
        this.enableSubscriberVerification = enableSubscriberVerification;
    }

    /**
     * Check the status of the Json schema validation property.
     *
     * @return Status of the validator property.
     */
    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }

    /**
     * To set the gateway security for the relevant API.
     *
     * @param apiSecurity Relevant type of gateway security for the API.
     */
    public void setApiSecurity(String apiSecurity) {
        if (apiSecurity != null) {
            this.apiSecurity = apiSecurity;
        }
    }

    /**
     * To get the gateway level security specific to the relevant API.
     *
     * @return Relevant type of gateway security.
     */
    public String getApiSecurity() {
        return apiSecurity;
    }

    public String getWsdlArchivePath() {
        return wsdlArchivePath;
    }

    public void setWsdlArchivePath(String wsdlArchivePath) {
        this.wsdlArchivePath = wsdlArchivePath;
    }

    public ResourceFile getWsdlResource() {
        return wsdlResource;
    }

    public void setWsdlResource(ResourceFile wsdl) {
        this.wsdlResource = wsdl;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public List<APIEndpoint> getEndpoint() {

        return endpoints;
    }

    public void setEndpoint(List<APIEndpoint> endpoint) {

        this.endpoints = endpoint;
    }

    public boolean isEnableStore() {
        return enableStore;
    }

    public void setEnableStore(boolean enableStore) {
        this.enableStore = enableStore;
    }


    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    /**
     * This method returns endpoints according to the given endpoint config
     *
     * @param endpoints list of endpoints given
     * @return String endpoint config
     */
    public static String getEndpointConfigString(List<APIEndpoint> endpoints) {
        //todo improve this logic to support multiple endpoints such as failorver and load balance
        StringBuilder sb = new StringBuilder();
        if (endpoints != null && endpoints.size() > 0) {
            sb.append("{");
            for (APIEndpoint endpoint : endpoints) {
                sb.append("\"")
                        .append(endpoint.getType())
                        .append("\": {\"url\":\"")
                        .append(endpoint.getInline().getEndpointConfig().getList().get(0).getUrl())
                        .append("\",\"timeout\":\"")
                        .append(endpoint.getInline().getEndpointConfig().getList().get(0).getTimeout())
                        .append("\",\"key\":\"")
                        .append(endpoint.getKey())
                        .append("\"},");
            }
            sb.append("\"endpoint_type\" : \"")
                    .append(endpoints.get(0).getInline().getType())//assuming all the endpoints are same type
                    .append("\"}\n");
        }
        return sb.toString();
    }

    public void setApiCategories(List<APICategory> apiCategories) {
        this.apiCategories = apiCategories;
    }

    public List<APICategory> getApiCategories() {
        return apiCategories;
    }

    public List<String> getKeyManagers() {

        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {

        this.keyManagers = keyManagers;
    }

    public Mediation getInSequenceMediation() {
        return inSequenceMediation;
    }

    public void setInSequenceMediation(Mediation inSequenceMediation) {
        this.inSequenceMediation = inSequenceMediation;
    }

    public Mediation getOutSequenceMediation() {
        return outSequenceMediation;
    }

    public void setOutSequenceMediation(Mediation outSequenceMediation) {
        this.outSequenceMediation = outSequenceMediation;
    }

    public Mediation getFaultSequenceMediation() {
        return faultSequenceMediation;
    }

    public void setFaultSequenceMediation(Mediation faultSequenceMediation) {
        this.faultSequenceMediation = faultSequenceMediation;
    }

    public boolean isRevision() {
        return isRevision;
    }

    public void setRevision(boolean revision) {
        isRevision = revision;
    }

    public String getRevisionedApiId() {
        return revisionedApiId;
    }

    public void setRevisionedApiId(String revisionedApiId) {
        this.revisionedApiId = revisionedApiId;
    }

    public int getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getVersionTimestamp() {

        return versionTimestamp;
    }

    public void setVersionTimestamp(String versionTimestamp) {

        this.versionTimestamp = versionTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof API)) {
            return false;
        }

        return Objects.equals(id, ((API) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isAsync() {
        return "WS".equals(type) || "WEBSUB".equals(type) || "SSE".equals(type) || "ASYNC".equals(type);
    }

    /**
     * Property to indicate the gateway vendor to deploy API
     */
    private String gatewayVendor;

    public String getGatewayVendor() {
        return gatewayVendor;
    }

    public void setGatewayVendor(String gatewayVendor) {
        this.gatewayVendor = gatewayVendor;
    }

    /**
     * Property to hold the gateway type relevant to the policies
     */
    private String gatewayType;

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    /**
     * Property to hold Async API transport protocols
     */
    private String asyncTransportProtocols;

    public String getAsyncTransportProtocols() {
        return asyncTransportProtocols;
    }

    public void setAsyncTransportProtocols(String asyncTransportProtocols) {
        this.asyncTransportProtocols = asyncTransportProtocols;
    }

    public List<OperationPolicy> apiPolicies;

    public List<OperationPolicy> getApiPolicies() {
        return apiPolicies;
    }

    public void setApiPolicies(List<OperationPolicy> apiPolicies) {
        this.apiPolicies = apiPolicies;
    }
}
