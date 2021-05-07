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

package org.wso2.carbon.apimgt.impl;

import com.google.gson.Gson;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ApplicationNameWhiteSpaceValidationException;
import org.wso2.carbon.apimgt.api.ApplicationNameWithInvalidCharactersException;
import org.wso2.carbon.apimgt.api.BlockConditionNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.PolicyNotFoundException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Wsdl;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIAPIProductNameComparator;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIProductNameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.ContentSearchResultNameComparator;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.RegistryConfigLoader;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.service.ContentBasedSearchService;
import org.wso2.carbon.registry.indexing.service.SearchResultsBean;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants.WF_TYPE_AM_API_STATE;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {

    // API definitions from swagger v2.0
    protected Log log = LogFactory.getLog(getClass());
    protected Registry registry;
    protected UserRegistry configRegistry;
    protected ApiMgtDAO apiMgtDAO;
    protected ScopesDAO scopesDAO;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected String tenantDomain;
    protected String username;
    protected static final GraphQLSchemaDefinition schemaDef = new GraphQLSchemaDefinition();
    // Property to indicate whether access control restriction feature is enabled.
    protected boolean isAccessControlRestrictionEnabled = false;

    private LRUCache<String, GenericArtifactManager> genericArtifactCache = new LRUCache<String, GenericArtifactManager>(
            5);

    public AbstractAPIManager() throws APIManagementException {
    }

    public AbstractAPIManager(String username) throws APIManagementException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        scopesDAO = ScopesDAO.getInstance();
        try {
            if (username == null) {

                this.registry = getRegistryService().getGovernanceUserRegistry();
                this.configRegistry = getRegistryService().getConfigSystemRegistry();

                this.username = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                ServiceReferenceHolder.setUserRealm((ServiceReferenceHolder.getInstance().getRealmService().getBootstrapRealm()));
            } else {
                String tenantDomainName = MultitenantUtils.getTenantDomain(username);
                String tenantUserName = getTenantAwareUsername(username);
                int tenantId = getTenantManager().getTenantId(tenantDomainName);
                this.tenantId = tenantId;
                this.tenantDomain = tenantDomainName;
                this.username = tenantUserName;

                loadTenantRegistry(tenantId);

                this.registry = getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                this.configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);

                //load resources for each tenants.
                APIUtil.loadloadTenantAPIRXT(tenantUserName, tenantId);
                APIUtil.loadTenantAPIPolicy(tenantUserName, tenantId);

                //Check whether GatewayType is "Synapse" before attempting to load Custom-Sequences into registry
                APIManagerConfiguration configuration = getAPIManagerConfiguration();

                String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

                if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
                    APIUtil.writeDefinedSequencesToTenantRegistry(tenantId);
                }

                ServiceReferenceHolder.setUserRealm((UserRealm) (ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(tenantId)));
            }
            ServiceReferenceHolder.setUserRealm(getRegistryService().getConfigSystemRegistry().getUserRealm());
            registerCustomQueries(configRegistry, username);
        } catch (RegistryException e) {
            String msg = "Error while obtaining registry objects";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while getting user registry for user:" + username;
            throw new APIManagementException(msg, e);
        }

    }

    /**
     * method to register custom registry queries
     *
     * @param registry Registry instance to use
     * @throws RegistryException n error
     */
    protected void registerCustomQueries(UserRegistry registry, String username)
            throws RegistryException, APIManagementException {
        String tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
        String latestAPIsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/latest-apis";
        String resourcesByTag = RegistryConstants.QUERIES_COLLECTION_PATH + "/resource-by-tag";
        String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                        APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION);
        if (username == null) {
            try {
                UserRealm realm = ServiceReferenceHolder.getUserRealm();
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(realm);
                authorizationManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);

            } catch (UserStoreException e) {
                String msg = "Error while setting the permissions";
                throw new APIManagementException(msg, e);
            }
        } else if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            int tenantId;
            try {
                tenantId = getTenantManager().getTenantId(tenantDomain);
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantUserRealm(tenantId).getAuthorizationManager();
                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String msg = "Error while setting the permissions";
                throw new APIManagementException(msg, e);
            }

        }

        if (!registry.resourceExists(tagsQueryPath)) {
            Resource resource = registry.newResource();

            //Tag Search Query
            //'MOCK_PATH' used to bypass ChrootWrapper -> filterSearchResult. A valid registry path is
            // a must for executeQuery results to be passed to client side
            String sql1 =
                    "SELECT '" + APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                            APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION + "' AS MOCK_PATH, " +
                            "   RT.REG_TAG_NAME AS TAG_NAME, " +
                            "   COUNT(RT.REG_TAG_NAME) AS USED_COUNT " +
                            "FROM " +
                            "   REG_RESOURCE_TAG RRT, " +
                            "   REG_TAG RT, " +
                            "   REG_RESOURCE R, " +
                            "   REG_RESOURCE_PROPERTY RRP, " +
                            "   REG_PROPERTY RP " +
                            "WHERE " +
                            "   RT.REG_ID = RRT.REG_TAG_ID  " +
                            "   AND R.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
                            "   AND RRT.REG_VERSION = R.REG_VERSION " +
                            "   AND RRP.REG_VERSION = R.REG_VERSION " +
                            "   AND RP.REG_NAME = 'STATUS' " +
                            "   AND RRP.REG_PROPERTY_ID = RP.REG_ID " +
                            "   AND (RP.REG_VALUE !='DEPRECATED' AND RP.REG_VALUE !='CREATED' AND RP.REG_VALUE !='BLOCKED' AND RP.REG_VALUE !='RETIRED') " +
                            "GROUP BY " +
                            "   RT.REG_TAG_NAME";
            resource.setContent(sql1);
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.TAG_SUMMARY_RESULT_TYPE);
            registry.put(tagsQueryPath, resource);
        }
        if (!registry.resourceExists(latestAPIsQueryPath)) {
            //Recently added APIs
            Resource resource = registry.newResource();
            String sql =
                    "SELECT " +
                            "   RR.REG_PATH_ID AS REG_PATH_ID, " +
                            "   RR.REG_NAME AS REG_NAME " +
                            "FROM " +
                            "   REG_RESOURCE RR, " +
                            "   REG_RESOURCE_PROPERTY RRP, " +
                            "   REG_PROPERTY RP " +
                            "WHERE " +
                            "   RR.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
                            "   AND RRP.REG_VERSION = RR.REG_VERSION " +
                            "   AND RP.REG_NAME = 'STATUS' " +
                            "   AND RRP.REG_PROPERTY_ID = RP.REG_ID " +
                            "   AND (RP.REG_VALUE !='DEPRECATED' AND RP.REG_VALUE !='CREATED') " +
                            "ORDER BY " +
                            "   RR.REG_LAST_UPDATED_TIME " +
                            "DESC ";
            resource.setContent(sql);
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.RESOURCES_RESULT_TYPE);
            registry.put(latestAPIsQueryPath, resource);
        }
        if (!registry.resourceExists(resourcesByTag)) {
            Resource resource = registry.newResource();
            String sql =
                    "SELECT '" + APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                            APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION + "' AS MOCK_PATH, " +
                            "   R.REG_UUID AS REG_UUID " +
                            "FROM " +
                            "   REG_RESOURCE_TAG RRT, " +
                            "   REG_TAG RT, " +
                            "   REG_RESOURCE R, " +
                            "   REG_PATH RP " +
                            "WHERE " +
                            "   RT.REG_TAG_NAME = ? " +
                            "   AND R.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
                            "   AND RP.REG_PATH_ID = R.REG_PATH_ID " +
                            "   AND RT.REG_ID = RRT.REG_TAG_ID " +
                            "   AND RRT.REG_VERSION = R.REG_VERSION ";

            resource.setContent(sql);
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.RESOURCE_UUID_RESULT_TYPE);
            registry.put(resourcesByTag, resource);
        }
    }

    public void cleanup() {

    }

    public List<API> getAllAPIs() throws APIManagementException {
        List<API> apiSortedList = new ArrayList<API>();
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                startTenantFlow(tenantDomain);
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                API api = null;
                try {
                    api = APIUtil.getAPI(artifact);
                    if (api != null) {
                        try {
                            checkAccessControlPermission(api.getId());
                        } catch (APIManagementException e) {
                            // This is a second level of filter to get apis based on access control and visibility.
                            // Hence log is set as debug and continued.
                            if(log.isDebugEnabled()) {
                                log.debug("User is not authorized to view the api " + api.getId().getApiName(), e);
                            }
                            continue;
                        }
                    }
                } catch (APIManagementException e) {
                    //log and continue since we want to load the rest of the APIs.
                    log.error("Error while loading API " + artifact.getAttribute(APIConstants.API_OVERVIEW_NAME), e);
                }
                if (api != null) {
                    apiSortedList.add(api);
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get APIs from the registry";
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }

        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
    }

    /**
     * To check authorization of the API against current logged in user. If the user is not authorized an exception
     * will be thrown.
     *
     * @param identifier API identifier
     * @throws APIManagementException APIManagementException
     */
    protected void checkAccessControlPermission(Identifier identifier) throws APIManagementException {
       // Implementation different based on invocation come from publisher or store
    }


    protected API getApi(GovernanceArtifact artifact) throws APIManagementException {
        return APIUtil.getAPI(artifact);
    }

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        Registry registry;
        try {
            String apiTenantDomain = getTenantDomain(identifier);
            int apiTenantId = getTenantManager()
                    .getTenantId(apiTenantDomain);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(apiTenantDomain)) {
                APIUtil.loadTenantRegistry(apiTenantId);
            }

            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) { //cross tenant scenario
                registry = getRegistryService().getGovernanceUserRegistry(
                        getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName())), apiTenantId);
            } else {
                registry = this.registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);

            API api = APIUtil.getAPIForPublishing(apiArtifact, registry);
            APIUtil.updateAPIProductDependencies(api, registry);

            //check for API visibility
            if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) { //global api
                return api;
            }
            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) {
                throw new APIManagementException("User " + username + " does not have permission to view API : "
                        + api.getId().getApiName());
            }

            return api;

        } catch (RegistryException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        }
    }

    protected String getTenantAwareUsername(String username) {
        return MultitenantUtils.getTenantAwareUsername(username);
    }

    protected String getTenantDomain(Identifier identifier) {
        return MultitenantUtils.getTenantDomain(
                APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
    }

    protected API getApiForPublishing(Registry registry, GovernanceArtifact apiArtifact) throws APIManagementException {
        API api = APIUtil.getAPIForPublishing(apiArtifact, registry);
        APIUtil.updateAPIProductDependencies(api, registry);
        return api;
    }

    protected void loadTenantRegistry(int apiTenantId) throws RegistryException {
        APIUtil.loadTenantRegistry(apiTenantId);
    }

    /**
     * Get API by registry artifact id
     *
     * @param uuid                  Registry artifact id
     * @param requestedTenantDomain tenantDomain for the registry
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    public API getAPIbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        boolean tenantFlowStarted = false;
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (requestedTenantDomain)) {
                int id = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }

            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(uuid);
            if (apiArtifact != null) {
                API api = getApiForPublishing(registry, apiArtifact);
                APIIdentifier apiIdentifier = api.getId();
                WorkflowDTO workflowDTO = APIUtil.getAPIWorkflowStatus(apiIdentifier, WF_TYPE_AM_API_STATE);
                if (workflowDTO != null) {
                    WorkflowStatus status = workflowDTO.getStatus();
                    api.setWorkflowStatus(status.toString());
                }
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    /**
     * Get API or APIProduct by registry artifact id
     *
     * @param uuid                  Registry artifact id
     * @param requestedTenantDomain tenantDomain for the registry
     * @return ApiTypeWrapper wrapping the API or APIProduct of the provided artifact id
     * @throws APIManagementException
     */
    public ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String requestedTenantDomain)
            throws APIManagementException {
        boolean tenantFlowStarted = false;
        try {
            Registry registry;
            if (requestedTenantDomain != null) {
                int id = getTenantManager().getTenantId(requestedTenantDomain);
                startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                if (APIConstants.WSO2_ANONYMOUS_USER.equals(this.username)) {
                    registry = getRegistryService().getGovernanceUserRegistry(this.username, id);
                } else if (this.tenantDomain != null && !this.tenantDomain.equals(requestedTenantDomain)) {
                    registry = getRegistryService().getGovernanceSystemRegistry(id);
                } else {
                    registry = this.registry;
                }
                if (((UserRegistry) registry).getTenantId() != id) {
                    registry = getRegistryService().getGovernanceSystemRegistry(id);
                    this.registry = registry;
                }
            } else {
                registry = this.registry;
            }


            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(uuid);
            if (apiArtifact != null) {
                String type = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE);

                if (APIConstants.API_PRODUCT.equals(type)) {
                    APIProduct apiProduct = getApiProduct(registry, apiArtifact);
                    String productTenantDomain = getTenantDomain(apiProduct.getId());
                    if (APIConstants.API_GLOBAL_VISIBILITY.equals(apiProduct.getVisibility())) {
                        return new ApiTypeWrapper(apiProduct);
                    }

                    if (this.tenantDomain == null || !this.tenantDomain.equals(productTenantDomain)) {
                        throw new APIManagementException(
                                "User " + username + " does not have permission to view API Product : " + apiProduct
                                        .getId().getName());
                    }

                    return new ApiTypeWrapper(apiProduct);

                } else {
                    API api = getApiForPublishing(registry, apiArtifact);
                    String apiTenantDomain = getTenantDomain(api.getId());
                    if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) {
                        return new ApiTypeWrapper(api);
                    }

                    if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) {
                        throw new APIManagementException(
                                "User " + username + " does not have permission to view API : " + api.getId()
                                        .getApiName());
                    }

                    return new ApiTypeWrapper(api);
                }
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException | org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    protected TenantManager getTenantManager() {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid Registry artifact id
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    public API getLightweightAPIByUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (requestedTenantDomain)) {
                int id = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(uuid);
            if (apiArtifact != null) {
                return getApiInformation(registry, apiArtifact);
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, uuid));
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API with uuid " + uuid;
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get tenant Id while getting API with uuid " + uuid;
            throw new APIManagementException(msg, e);
        }
    }

    protected API getApiInformation(Registry registry, GovernanceArtifact apiArtifact) throws APIManagementException {
        return APIUtil.getAPIInformation(apiArtifact, registry);
    }

    /**
     * Get minimal details of API by API identifier
     *
     * @param identifier APIIdentifier object
     * @return API of the provided APIIdentifier
     * @throws APIManagementException
     */
    public API getLightweightAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);

        boolean tenantFlowStarted = false;

        try {
            String tenantDomain = getTenantDomain(identifier);

            startTenantFlow(tenantDomain);
            tenantFlowStarted = true;

            Registry registry = getRegistry(identifier, apiPath);
            if (registry != null) {
                Resource apiResource = registry.get(apiPath);
                String artifactId = apiResource.getUUID();
                if (artifactId == null) {
                    throw new APIManagementException("artifact id is null for : " + apiPath);
                }
                GenericArtifactManager artifactManager = getAPIGenericArtifactManager(identifier, registry);
                GovernanceArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
                return getApiInformation(registry,apiArtifact);
            } else {
                String msg = "Failed to get registry from api identifier: " + identifier;
                throw new APIManagementException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    /**
     * Get minimal details of API by API identifier without checking the permission
     *
     * @param identifier APIIdentifier object
     * @return API of the provided APIIdentifier
     * @throws APIManagementException
     */
    public API getLightweightAPIWithoutPermissionCheck(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);

        boolean tenantFlowStarted = false;

        try {
            String tenantDomain = getTenantDomain(identifier);

            startTenantFlow(tenantDomain);
            tenantFlowStarted = true;

            Registry registry = getSystemRegistry(identifier);

            if (registry != null) {
                Resource apiResource = registry.get(apiPath);
                String artifactId = apiResource.getUUID();
                if (artifactId == null) {
                    throw new APIManagementException("artifact id is null for : " + apiPath);
                }
                GenericArtifactManager artifactManager = getAPIGenericArtifactManager(identifier, registry);
                GovernanceArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
                return getApiInformation(registry,apiArtifact);
            } else {
                String msg = "Failed to get registry from api identifier: " + identifier;
                throw new APIManagementException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    public Registry getSystemRegistry(APIIdentifier identifier) throws APIManagementException{
        try {
            String tenantDomain = getTenantDomain(identifier);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = getTenantManager().getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(id);
                return getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                return getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            }
        } catch (RegistryException |org.wso2.carbon.user.api.UserStoreException  e) {
            String msg = "Failed to get the API from registry.";
            throw new APIManagementException(msg, e);
        }
    }

    protected void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    protected void startTenantFlow(String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    private GenericArtifactManager getAPIGenericArtifactManager(APIIdentifier identifier, Registry registry)
            throws APIManagementException {

        String tenantDomain = getTenantDomain(identifier);
        GenericArtifactManager manager = genericArtifactCache.get(tenantDomain);
        if (manager != null) {
            return manager;
        }
        manager = getAPIGenericArtifactManagerFromUtil(registry, APIConstants.API_KEY);
        genericArtifactCache.put(tenantDomain, manager);
        return manager;
    }

    private Registry getRegistry(APIIdentifier identifier, String apiPath)
            throws APIManagementException {
        Registry passRegistry;
        try {
            String tenantDomain = getTenantDomain(identifier);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = getTenantManager()
                        .getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(id);
                passRegistry = getRegistryService()
                        .getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // explicitly load the tenant's registry
                    APIUtil.loadTenantRegistry(MultitenantConstants.SUPER_TENANT_ID);
                    passRegistry = getRegistryService().getGovernanceUserRegistry(
                            identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    passRegistry = this.registry;
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API from registry on path of : " + apiPath;
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API from registry on path of : " + apiPath;
            throw new APIManagementException(msg, e);
        }
        return passRegistry;
    }

    public API getAPI(String apiPath) throws APIManagementException {
        try {
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return getApi(apiArtifact);

        } catch (RegistryException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        }
    }

    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            String msg = "Failed to check availability of api :" + path;
            throw new APIManagementException(msg, e);
        }
    }

    public boolean isAPIProductAvailable(APIProductIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            String msg = "Failed to check availability of API Product :" + path;
            throw new APIManagementException(msg, e);
        }
    }

    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException {

        Set<String> versionSet = new HashSet<String>();
        String apiPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                providerName + RegistryConstants.PATH_SEPARATOR + apiName;
        try {
            Resource resource = registry.get(apiPath);
            if (resource instanceof Collection) {
                Collection collection = (Collection) resource;
                String[] versionPaths = collection.getChildren();
                if (versionPaths == null || versionPaths.length == 0) {
                    return versionSet;
                }
                for (String path : versionPaths) {
                    versionSet.add(path.substring(apiPath.length() + 1));
                }
            } else {
                throw new APIManagementException("API version must be a collection " + apiName);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get versions for API: " + apiName;
            throw new APIManagementException(msg, e);
        }
        return versionSet;
    }

    /**
     * Returns list of global mediation policies available
     *
     * @return List of Mediation objects of global mediation policies
     * @throws APIManagementException If failed to get global mediation policies
     */
    @Override
    public List<Mediation> getAllGlobalMediationPolicies() throws APIManagementException {
        List<Mediation> mediationList = new ArrayList<Mediation>();
        Mediation mediation;
        String resourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        try {
            //Resource : customsequences
            Resource resource = registry.get(resourcePath);
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    //Resource : in / out / fault
                    Resource typeResource = registry.get(type);
                    if (typeResource instanceof Collection) {
                        String[] sequenceArray = ((Collection) typeResource).getChildren();
                        if (sequenceArray.length > 0) {
                            for (String sequence : sequenceArray) {
                                //Resource : actual resource eg : log_in_msg.xml
                                Resource sequenceResource = registry.get(sequence);
                                String resourceId = sequenceResource.getUUID();
                                try {
                                    String contentString = IOUtils.toString
                                            (sequenceResource.getContentStream(),
                                            RegistryConstants.DEFAULT_CHARSET_ENCODING);
                                    OMElement omElement = AXIOMUtil.stringToOM(contentString);
                                    OMAttribute attribute = omElement.getAttribute(new QName
                                            (PolicyConstants.MEDIATION_NAME_ATTRIBUTE));
                                    String mediationPolicyName = attribute.getAttributeValue();
                                    mediation = new Mediation();
                                    mediation.setUuid(resourceId);
                                    mediation.setName(mediationPolicyName);
                                    //Extract sequence type from the registry resource path
                                    String resourceType = type.substring(type.lastIndexOf("/") + 1);
                                    mediation.setType(resourceType);
                                    //Add mediation to the mediation list
                                    mediationList.add(mediation);
                                } catch (XMLStreamException e) {
                                    //If any exception been caught flow may continue with the next mediation policy
                                    log.error("Error occurred while getting omElement out of " +
                                            "mediation content from "+sequence, e);
                                } catch (IOException e) {
                                    log.error("Error occurred while converting resource " +
                                            "contentStream in to string in "+sequence,e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get global mediation policies";
            throw new APIManagementException(msg, e);
        }
        return mediationList;
    }

    /**
     * Return mediation policy corresponds to the given identifier
     *
     * @param mediationPolicyId uuid of the registry resource
     * @return Mediation object related to the given identifier or null
     * @throws APIManagementException If failed to get specified mediation policy
     */
    @Override
    public Mediation getGlobalMediationPolicy(String mediationPolicyId) throws APIManagementException {
        Mediation mediation = null;
        //Get registry resource correspond to identifier
        Resource mediationResource = this.getCustomMediationResourceFromUuid(mediationPolicyId);
        if (mediationResource != null) {
            //Get mediation config details
            try {
                //extracting content stream of mediation policy in to  string
                String contentString = IOUtils.toString(mediationResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                //Get policy name from the mediation config
                OMElement omElement = AXIOMUtil.stringToOM(contentString);
                OMAttribute attribute = omElement.getAttribute(new QName
                        (PolicyConstants.MEDIATION_NAME_ATTRIBUTE));
                String mediationPolicyName = attribute.getAttributeValue();
                mediation = new Mediation();
                mediation.setUuid(mediationResource.getUUID());
                mediation.setName(mediationPolicyName);
                String resourcePath = mediationResource.getPath();
                //Extracting mediation type from the registry resource path
                String[] path = resourcePath.split(RegistryConstants.PATH_SEPARATOR);
                String resourceType = path[(path.length - 2)];
                mediation.setType(resourceType);
                mediation.setConfig(contentString);

            } catch (RegistryException e) {
                log.error("Error occurred while getting content stream of the ,mediation policy ", e);
            } catch (IOException e) {
                log.error("Error occurred while converting content stream of mediation policy " +
                        "into string ", e);
            } catch (XMLStreamException e) {
                log.error("Error occurred while getting omElement out of mediation content ", e);
            }
        }
        return mediation;
    }

    /**
     * Returns list of wsdls
     *
     * @return list of wsdl objects or null
     * @throws APIManagementException If unable to return satisfied wsdl object list
     */
    @Override
    public List<Wsdl> getAllWsdls() throws APIManagementException {

        List<Wsdl> wsdlList = new ArrayList<Wsdl>();
        String resourcePath = APIConstants.API_WSDL_RESOURCE;
        try {
            if (registry.resourceExists(resourcePath)) {
                Resource wsdlResource = registry.get(resourcePath);
                if (wsdlResource instanceof Collection) {
                    String[] wsdlCollection = ((Collection) wsdlResource).getChildren();
                    if (wsdlCollection.length > 0) {
                        for (String wsdlFile : wsdlCollection) {
                            Resource wsdlResourceFile = registry.get(wsdlFile);
                            String uuid = wsdlResourceFile.getUUID();
                            Wsdl wsdl = new Wsdl();
                            String wsdlName = wsdlFile.substring(wsdlFile.lastIndexOf("/") + 1);
                            wsdl.setUuid(uuid);
                            wsdl.setName(wsdlName);
                            wsdlList.add(wsdl);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get wsdl list";
            throw new APIManagementException(msg, e);
        }
        return wsdlList;
    }

    /**
     * Return Wsdl specify by identifier
     *
     * @param wsdlId uuid of the wsdl resource
     * @return A Wsdl object related to the given identifier or null
     * @throws APIManagementException If failed to get specified wsdl
     */
    @Override
    public Wsdl getWsdlById(String wsdlId) throws APIManagementException {
        Wsdl wsdl = null;
        //Get registry resource correspond to identifier
        Resource wsdlResource = this.getWsdlResourceFromUuid(wsdlId);
        if (wsdlResource != null) {
            try {
                //extracting content stream of wsdl in to  string
                String contentString = IOUtils.toString(wsdlResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                wsdl = new Wsdl();
                String resourcePath = wsdlResource.getPath();
                String wsdlName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
                wsdl.setUuid(wsdlResource.getUUID());
                wsdl.setName(wsdlName);
                wsdl.setConfig(contentString);
            } catch (RegistryException e) {
                log.error("Error occurred while getting content stream of the wsdl " +
                        wsdlResource.getPath(), e);
            } catch (IOException e) {
                log.error("Error occurred while converting content stream of wsdl " +
                        wsdlResource.getPath() + " into string ", e);
            }
        }
        return wsdl;
    }

    /**
     * Returns the wsdl registry resource correspond to the given identifier
     *
     * @param wsdlId uuid of the wsdl resource
     * @return Registry resource of given identifier or null
     * @throws APIManagementException If failed to get the registry resource of given uuid
     */
    @Override
    public Resource getWsdlResourceFromUuid(String wsdlId) throws APIManagementException {
        String resourcePath = APIConstants.API_WSDL_RESOURCE;
        try {
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                //resource : /_system/governance/apimgt/applicationdata/wsdls

                if (resource instanceof Collection) {
                    Collection wsdlCollection = (Collection) resource;
                    String[] wsdlArray = wsdlCollection.getChildren();
                    for (String wsdl : wsdlArray) {
                        Resource wsdlResource = registry.get(wsdl);
                        String resourceId = wsdlResource.getUUID();
                        if (resourceId.equals(wsdlId)) {
                            return wsdlResource;
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while accessing registry objects";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Delete an existing wsdl
     *
     * @param wsdlId uuid of the wsdl
     * @return true if deleted successfully
     * @throws APIManagementException If failed to delete wsdl
     */
    @Override
    public boolean deleteWsdl(String wsdlId) throws APIManagementException {
        //Get registry resource correspond to the uuid
        Resource wsdlResource = this.getWsdlResourceFromUuid(wsdlId);
        if (wsdlResource != null) {
            //If resource exists
            String wsdlResourcePath = wsdlResource.getPath();
            try {
                if (registry.resourceExists(wsdlResourcePath)) {
                    ////TODO : validation if wsdl been use by any API
                    registry.delete(wsdlResourcePath);
                    //Verify if deleted successfully
                    if (!registry.resourceExists(wsdlResourcePath)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Wsdl " + wsdlResourcePath + " deleted successfully");
                        }
                        return true;
                    }
                }
            } catch (RegistryException e) {
                String msg = "Failed to delete wsdl " + wsdlResourcePath;
                throw new APIManagementException(msg, e);
            }
        }
        return false;
    }

    /**
     * Returns the wsdl content in registry specified by the wsdl name
     *
     * @param apiId Api Identifier
     * @return wsdl content matching name if exist else null
     */
    @Override
    public ResourceFile getWSDL(APIIdentifier apiId) throws APIManagementException {
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION +
                APIUtil.createWsdlFileName(apiId.getProviderName(), apiId.getApiName(), apiId.getVersion());
        String tenantDomain = getTenantDomain(apiId);
        boolean isTenantFlowStarted = false;
        try {
            Registry registry;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                startTenantFlow(tenantDomain);
                int id = getTenantManager().getTenantId(tenantDomain);
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    registry = getRegistryService().getGovernanceUserRegistry(apiId.getProviderName(),
                            MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    registry = this.registry;
                }
            }
            if (registry.resourceExists(wsdlResourcePath)) {
                Resource resource = registry.get(wsdlResourcePath);
                return new ResourceFile(resource.getContentStream(), resource.getMediaType());
            } else {
                wsdlResourcePath =
                        APIConstants.API_WSDL_RESOURCE_LOCATION + APIConstants.API_WSDL_ARCHIVE_LOCATION + apiId
                                .getProviderName() + APIConstants.WSDL_PROVIDER_SEPERATOR + apiId.getApiName() +
                                apiId.getVersion() + APIConstants.ZIP_FILE_EXTENSION;
                if (registry.resourceExists(wsdlResourcePath)) {
                    Resource resource = registry.get(wsdlResourcePath);
                    return new ResourceFile(resource.getContentStream(), resource.getMediaType());
                } else {
                    throw new APIManagementException("No WSDL found for the API: " + apiId,
                            ExceptionCodes.from(ExceptionCodes.NO_WSDL_AVAILABLE_FOR_API, apiId.getApiName(),
                                    apiId.getVersion()));
                }
            }
        } catch (RegistryException | org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while getting wsdl file from the registry for API: " + apiId.toString();
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    /**
     * Create a wsdl in the path specified.
     *
     * @param resourcePath   Registry path of the resource
     * @param wsdlDefinition wsdl content
     */
    @Override
    public void uploadWsdl(String resourcePath, String wsdlDefinition)
            throws APIManagementException {
        try {
            Resource resource = registry.newResource();
            resource.setContent(wsdlDefinition);
            resource.setMediaType(String.valueOf(ContentType.APPLICATION_XML));
            registry.put(resourcePath, resource);
        } catch (RegistryException e) {
            String msg = "Error while uploading wsdl to from the registry ";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Update a existing wsdl in the path specified
     *
     * @param resourcePath   Registry path of the resource
     * @param wsdlDefinition wsdl content
     */
    @Override
    public void updateWsdl(String resourcePath, String wsdlDefinition) throws APIManagementException {
        try {
            Resource resource = registry.get(resourcePath);
            resource.setContent(wsdlDefinition);
            registry.put(resourcePath,resource);
        } catch (RegistryException e) {
            String msg = "Error while updating the existing wsdl ";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Returns the graphQL content in registry specified by the wsdl name
     *
     * @param apiId Api Identifier
     * @return graphQL content matching name if exist else null
     */
    @Override
    public String getGraphqlSchemaDefinition(APIIdentifier apiId) throws APIManagementException {
        String apiTenantDomain = getTenantDomain(apiId);
        String schema;
        try {
            Registry registryType;
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if (this.tenantDomain == null || isTenantDomainNotMatching(apiTenantDomain)) {
                int tenantId = getTenantManager().getTenantId(apiTenantDomain);
                registryType = getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            schema = schemaDef.getGraphqlSchemaDefinition(apiId, registryType);
        } catch (org.wso2.carbon.user.api.UserStoreException | RegistryException e) {
            String msg = "Failed to get graphql schema definition of Graphql API : " + apiId;
            throw new APIManagementException(msg, e);
        }
        return schema;
    }

    /**
     * Returns the swagger 2.0 definition of the given API
     *
     * @param apiId id of the APIIdentifier
     * @return An String containing the swagger 2.0 definition
     * @throws APIManagementException
     */
    @Override
    public String getOpenAPIDefinition(Identifier apiId) throws APIManagementException {
        String apiTenantDomain = getTenantDomain(apiId);
        String swaggerDoc = null;
        boolean tenantFlowStarted = false;
        try {
            Registry registryType;
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if (this.tenantDomain == null || isTenantDomainNotMatching(apiTenantDomain)) {
                if (apiTenantDomain != null) {
                    startTenantFlow(apiTenantDomain);
                    tenantFlowStarted = true;
                }
                int tenantId = getTenantManager().getTenantId(
                        apiTenantDomain);
                registryType = getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            swaggerDoc = OASParserUtil.getAPIDefinition(apiId, registryType);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get swagger documentation of API : " + apiId;
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get swagger documentation of API : " + apiId;
            throw new APIManagementException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
        return swaggerDoc;
    }

    public String addResourceFile(Identifier identifier, String resourcePath, ResourceFile resourceFile) throws APIManagementException {
        try {
            Resource thumb = registry.newResource();
            thumb.setContentStream(resourceFile.getContent());
            thumb.setMediaType(resourceFile.getContentType());
            registry.put(resourcePath, thumb);
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
                return RegistryConstants.PATH_SEPARATOR + "registry"
                        + RegistryConstants.PATH_SEPARATOR + "resource"
                        + RegistryConstants.PATH_SEPARATOR + "_system"
                        + RegistryConstants.PATH_SEPARATOR + "governance"
                        + resourcePath;
            } else {
                return "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
                        + RegistryConstants.PATH_SEPARATOR + "resource"
                        + RegistryConstants.PATH_SEPARATOR + "_system"
                        + RegistryConstants.PATH_SEPARATOR + "governance"
                        + resourcePath;
            }
        } catch (RegistryException e) {
            String msg = "Error while adding the resource to the registry";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Checks whether the given document already exists for the given api/product
     *
     * @param identifier API/Product Identifier
     * @param docName    Name of the document
     * @return true if document already exists for the given api/product
     * @throws APIManagementException if failed to check existence of the documentation
     */
    public boolean isDocumentationExist(Identifier identifier, String docName) throws APIManagementException {
        String docPath = "";

            docPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName()
                    + RegistryConstants.PATH_SEPARATOR + identifier.getName() + RegistryConstants.PATH_SEPARATOR
                    + identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                    + RegistryConstants.PATH_SEPARATOR + docName;
        try {
            return registry.resourceExists(docPath);
        } catch (RegistryException e) {
            String msg = "Failed to check existence of the document :" + docPath;
            throw new APIManagementException(msg, e);
        }
    }

    public List<Documentation> getAllDocumentation(Identifier id) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String resourcePath = StringUtils.EMPTY;
        String docArtifactKeyType = StringUtils.EMPTY;
        if (id instanceof APIIdentifier) {
            resourcePath = APIUtil.getAPIPath((APIIdentifier) id);
        } else if (id instanceof APIProductIdentifier) {
            resourcePath = APIUtil.getAPIProductPath((APIProductIdentifier) id);
        }
        docArtifactKeyType = APIConstants.DOCUMENTATION_KEY;

        try {
            Association[] docAssociations = registry.getAssociations(resourcePath,
                    APIConstants.DOCUMENTATION_ASSOCIATION);
            if (docAssociations == null) {
                return documentationList;
            }
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();

                Resource docResource = registry.get(docPath);
                GenericArtifactManager artifactManager = getAPIGenericArtifactManager(registry,
                        docArtifactKeyType);
                GenericArtifact docArtifact = artifactManager.getGenericArtifact(docResource.getUUID());
                Documentation doc = APIUtil.getDocumentation(docArtifact);
                Date contentLastModifiedDate;
                Date docLastModifiedDate = docResource.getLastModified();
                if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType()) || Documentation.DocumentSourceType.MARKDOWN.equals(doc.getSourceType())) {
                    String contentPath = StringUtils.EMPTY;
                    if (id instanceof APIIdentifier) {
                        contentPath = APIUtil.getAPIDocContentPath((APIIdentifier) id, doc.getName());
                    } else if (id instanceof APIProductIdentifier) {
                        contentPath = APIUtil.getProductDocContentPath((APIProductIdentifier) id, doc.getName());
                    }

                    contentLastModifiedDate = registry.get(contentPath).getLastModified();
                    doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                            contentLastModifiedDate : docLastModifiedDate));
                } else {
                    doc.setLastUpdated(docLastModifiedDate);
                }
                documentationList.add(doc);
            }

        } catch (RegistryException e) {
            String msg = "Failed to get documentations for api/product " + id.getName();
            throw new APIManagementException(msg, e);
        }
        return documentationList;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId, String loggedUsername) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath = APIUtil.getAPIPath(apiId);
        try {
            String tenantDomain = getTenantDomain(apiId);
            Registry registryType;
            /* If the API provider is a tenant, load tenant registry*/
            boolean isTenantMode = (tenantDomain != null);
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = getTenantManager()
                        .getTenantId(tenantDomain);
                registryType = getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            Association[] docAssociations = registryType.getAssociations(apiResourcePath,
                    APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();
                Resource docResource = null;
                try {
                    docResource = registryType.get(docPath);
                } catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
                    //do nothing. Permission not allowed to access the doc.
                } catch (RegistryException e) {
                    String msg = "Failed to get documentations for api " + apiId.getApiName();
                    throw new APIManagementException(msg, e);
                }
                if (docResource != null) {
                    GenericArtifactManager artifactManager = getAPIGenericArtifactManager(registryType,
                            APIConstants.DOCUMENTATION_KEY);
                    GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                            docResource.getUUID());
                    Documentation doc = APIUtil.getDocumentation(docArtifact, apiId.getProviderName());
                    Date contentLastModifiedDate;
                    Date docLastModifiedDate = docResource.getLastModified();
                    if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {
                        String contentPath = APIUtil.getAPIDocContentPath(apiId, doc.getName());
                        try {
                            contentLastModifiedDate = registryType.get(contentPath).getLastModified();
                            doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                                    contentLastModifiedDate : docLastModifiedDate));
                        } catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
                            //do nothing. Permission not allowed to access the doc.
                        }

                    } else if (Documentation.DocumentSourceType.MARKDOWN.equals(doc.getSourceType())) {
                        String contentPath = APIUtil.getAPIDocContentPath(apiId, doc.getName());
                        try {
                            contentLastModifiedDate = registryType.get(contentPath).getLastModified();
                            doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                                    contentLastModifiedDate : docLastModifiedDate));
                        } catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
                            //do nothing. Permission not allowed to access the doc.
                        }
                    } else {
                        doc.setLastUpdated(docLastModifiedDate);
                    }
                    documentationList.add(doc);
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get documentations for api " + apiId.getApiName();
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get documentations for api " + apiId.getApiName();
            throw new APIManagementException(msg, e);
        }
        return documentationList;
    }

    protected GenericArtifactManager getAPIGenericArtifactManager(Registry registryType, String keyType) throws
            APIManagementException {
        try {
            return new GenericArtifactManager(registryType, keyType);
        } catch (RegistryException e) {
            handleException("Error while retrieving generic artifact manager object",e);
        }
        return null;
    }

    protected GenericArtifactManager getAPIGenericArtifactManagerFromUtil(Registry registry, String keyType)
            throws APIManagementException {
         return APIUtil.getArtifactManager(registry, keyType);
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    public Documentation getDocumentation(APIIdentifier apiId, DocumentationType docType,
                                          String docName) throws APIManagementException {
        Documentation documentation = null;
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;
        GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                APIConstants.DOCUMENTATION_KEY);
        try {
            Resource docResource = registry.get(docPath);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docResource.getUUID());
            documentation = APIUtil.getDocumentation(artifact);
        } catch (RegistryException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        }
        return documentation;
    }

    /**
     * Get a documentation by artifact Id
     *
     * @param docId                 artifact id of the document
     * @param requestedTenantDomain tenant domain of the registry where the artifact is located
     * @return Document object which represents the artifact id
     * @throws APIManagementException
     */
    public Documentation getDocumentation(String docId, String requestedTenantDomain) throws APIManagementException {
        Documentation documentation = null;
        try {
            Registry registryType;
            boolean isTenantMode = (requestedTenantDomain != null);
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(
                    requestedTenantDomain))) {
                int tenantId = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registryType, APIConstants
                    .DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            if(artifact == null) {
                return documentation;
            }
            if (null != artifact) {
                documentation = APIUtil.getDocumentation(artifact);
                documentation.setCreatedDate(registryType.get(artifact.getPath()).getCreatedTime());
                Date lastModified = registryType.get(artifact.getPath()).getLastModified();
                if (lastModified != null) {
                    documentation.setLastUpdated(registryType.get(artifact.getPath()).getLastModified());
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        }
        return documentation;
    }

    public String getDocumentationContent(Identifier identifier, String documentationName)
            throws APIManagementException {
        String contentPath = StringUtils.EMPTY;
        String identifierType = StringUtils.EMPTY;
        if (identifier instanceof APIIdentifier) {
            contentPath = APIUtil.getAPIDocPath((APIIdentifier) identifier) +
                    APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +
                    documentationName;
            identifierType = APIConstants.API_IDENTIFIER_TYPE;
        } else if (identifier instanceof APIProductIdentifier) {
            contentPath = APIUtil.getProductDocPath((APIProductIdentifier) identifier) +
                    APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +
                    documentationName;
            identifierType = APIConstants.API_PRODUCT_IDENTIFIER_TYPE;
        }
        String tenantDomain = getTenantDomain(identifier);
        Registry registry;

        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                startTenantFlow(tenantDomain);
                isTenantFlowStarted = true;
            }

	        /* If the API provider is a tenant, load tenant registry*/
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = getTenantManager().getTenantId(tenantDomain);
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    registry = getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    registry = this.registry;
                }
            }

            if (registry.resourceExists(contentPath)) {
                Resource docContent = registry.get(contentPath);
                Object content = docContent.getContent();
                if (content != null) {
                    return new String((byte[]) docContent.getContent(), Charset.defaultCharset());
                }
            }
        } catch (RegistryException e) {
            String msg = "No document content found for documentation: "
                    + documentationName + " of "+ identifierType + " : " + identifier.getName();
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get document content found for documentation: "
                    + documentationName + " of " + identifierType + " : " + identifier.getName();
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
        return null;
    }

    public GraphqlComplexityInfo getComplexityDetails(APIIdentifier apiIdentifier) throws APIManagementException {
        return apiMgtDAO.getComplexityDetails(apiIdentifier);
    }


    public void addOrUpdateComplexityDetails(APIIdentifier apiIdentifier, GraphqlComplexityInfo graphqlComplexityInfo) throws APIManagementException {
        apiMgtDAO.addOrUpdateComplexityDetails(apiIdentifier, graphqlComplexityInfo);
    }


    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        return null;
    }

    public boolean isContextExist(String context) throws APIManagementException {
        // Since we don't have tenant in the APIM table, we do the filtering using this hack
        if (context != null && context.startsWith("/t/"))
            context = context.replace("/t/" + getTenantDomainFromUrl(context), ""); //removing prefix
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            context = "/t/" + tenantDomain + context;
        }
        return apiMgtDAO.isContextExist(context);
    }

    protected String getTenantDomainFromUrl(String url) {
        return MultitenantUtils.getTenantDomainFromUrl(url);
    }

    /**
     * Check the scope exist in Tenant.
     *
     * @param scopeKey     candidate scope key
     * @param tenantid     tenant id
     * @return true if the scope key is already available
     * @throws APIManagementException if failed to check the context availability
     */
    @Override
    public boolean isScopeKeyExist(String scopeKey, int tenantid) throws APIManagementException {

        return scopesDAO.isScopeExist(scopeKey, tenantid);
    }

    /**
     * Check whether the given scope key is already assigned to any API under given tenant.
     *
     * @param scopeKey     Scope Key
     * @param tenantDomain Tenant Domain
     * @return whether scope is assigned or not
     * @throws APIManagementException if failed to check the scope assignment
     */
    @Override
    public boolean isScopeKeyAssignedToAPI(String scopeKey, String tenantDomain) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Checking whether the scope:" + scopeKey + " is attached to any API in tenant: " + tenantDomain);
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        return apiMgtDAO.isScopeKeyAssigned(scopeKey, tenantId);
    }

    /**
     * Check whether the given scope key is already assigned to an API as local scope under given tenant.
     * The different versions of the same API will not be take into consideration.
     *
     * @param apiIdentifier API Identifier
     * @param scopeKey   candidate scope key
     * @param tenantId   tenant id
     * @return true if the scope key is already attached as a local scope in any API
     * @throws APIManagementException if failed to check the local scope availability
     */
    public boolean isScopeKeyAssignedLocally(APIIdentifier apiIdentifier, String scopeKey, int tenantId)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Checking whether scope: " + scopeKey + " is assigned to another API as a local scope"
                    + " in tenant: " + tenantId);
        }
        return apiMgtDAO.isScopeKeyAssignedLocally(apiIdentifier, scopeKey, tenantId);
    }

    public boolean isApiNameExist(String apiName) throws APIManagementException {
        String tenantName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameExist(apiName, tenantName);
    }

    public boolean isApiNameWithDifferentCaseExist(String apiName) throws APIManagementException {
        String tenantName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameWithDifferentCaseExist(apiName, tenantName);
    }

    public void addSubscriber(String username, String groupingId)
            throws APIManagementException {

        Subscriber subscriber = new Subscriber(username);
        subscriber.setSubscribedDate(new Date());
        try {
            int tenantId = getTenantManager()
                    .getTenantId(getTenantDomain(username));
            SortedMap<String, String> claims = APIUtil.getClaims(username, tenantId, ClaimsRetriever
                    .DEFAULT_DIALECT_URI);
            String email = claims.get(APIConstants.EMAIL_CLAIM);
            if (StringUtils.isNotEmpty(email)) {
                subscriber.setEmail(email);
            } else {
                subscriber.setEmail(StringUtils.EMPTY);
            }
            subscriber.setTenantId(tenantId);
            apiMgtDAO.addSubscriber(subscriber, groupingId);
            //Add a default application once subscriber is added
            if (!APIUtil.isDefaultApplicationCreationDisabledForTenant(tenantId)) {
                addDefaultApplicationForSubscriber(subscriber);
            }
        } catch (APIManagementException e) {
            String msg = "Error while adding the subscriber " + subscriber.getName();
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while adding the subscriber " + subscriber.getName();
            throw new APIManagementException(msg, e);
        }
    }

    protected String getTenantDomain(String username) {
        return MultitenantUtils.getTenantDomain(username);
    }

    /**
     * Add default application on the first time a subscriber is added to the database
     *
     * @param subscriber Subscriber
     * @throws APIManagementException if an error occurs while adding default application
     */
    private void addDefaultApplicationForSubscriber(Subscriber subscriber) throws APIManagementException {
        Application defaultApp = new Application(APIConstants.DEFAULT_APPLICATION_NAME, subscriber);
        if (APIUtil.isEnabledUnlimitedTier()) {
            defaultApp.setTier(APIConstants.UNLIMITED_TIER);
        } else {
            Map<String, Tier> throttlingTiers = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE,
                    getTenantDomain(subscriber.getName()));
            Set<Tier> tierValueList = new HashSet<Tier>(throttlingTiers.values());
            List<Tier> sortedTierList = APIUtil.sortTiers(tierValueList);
            defaultApp.setTier(sortedTierList.get(0).getName());
        }
        //application will not be shared within the group
        defaultApp.setGroupId("");
        defaultApp.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        defaultApp.setUUID(UUID.randomUUID().toString());
        defaultApp.setDescription(APIConstants.DEFAULT_APPLICATION_DESCRIPTION);
        int applicationId = apiMgtDAO.addApplication(defaultApp, subscriber.getName());

        ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.APPLICATION_CREATE.name(), tenantId,
                tenantDomain, applicationId, defaultApp.getUUID(), defaultApp.getName(),
                defaultApp.getTokenType(),
                defaultApp.getTier(), defaultApp.getGroupId(), defaultApp.getApplicationAttributes(),
                subscriber.getName());
        APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
    }

    public void updateSubscriber(Subscriber subscriber)
            throws APIManagementException {
        apiMgtDAO.updateSubscriber(subscriber);
    }

    public Subscriber getSubscriber(int subscriberId)
            throws APIManagementException {
        return apiMgtDAO.getSubscriber(subscriberId);
    }

    public ResourceFile getIcon(APIIdentifier identifier) throws APIManagementException {
        String artifactPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        String tenantDomain = getTenantDomain(identifier);
        Registry registry;
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                startTenantFlow(tenantDomain);
                isTenantFlowStarted = true;
            }

	        /* If the API provider is a tenant, load tenant registry*/
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = getTenantManager().getTenantId(tenantDomain);
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    registry = getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    registry = this.registry;
                }
            }
            String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                return new ResourceFile(res.getContentStream(), res.getMediaType());
            }
        } catch (RegistryException e) {
            String msg = "Error while loading API icon of API " +  identifier.getApiName()
                    + ":" + identifier.getVersion() + " from the registry";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while loading API icon of API " + identifier.getApiName()
                    + ":" + identifier.getVersion();
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
        return null;
    }

    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Set<SubscribedAPI> subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, null);
        for (SubscribedAPI subscribedApi : subscribedAPIs) {
            Application application = subscribedApi.getApplication();
            if (application != null) {
                int applicationId = application.getId();
                Set<APIKey> keys = getApplicationKeys(applicationId);
                for (APIKey key : keys) {
                    application.addKey(key);
                }
            }
        }
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                startTenantFlow(tenantDomain);
            }
            for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                String apiPath = APIUtil.getAPIPath(subscribedAPI.getApiId());
                Resource resource;
                try {
                    resource = registry.get(apiPath);
                    GenericArtifactManager artifactManager = getAPIGenericArtifactManager(registry,
                            APIConstants.API_KEY);
                    GenericArtifact artifact = artifactManager.getGenericArtifact(
                            resource.getUUID());
                    API api = getAPI(artifact);
                    if (api != null) {
                        apiSortedSet.add(api);
                    }
                } catch (RegistryException e) {
                    String msg = "Failed to get APIs for subscriber: " + subscriber.getName();
                    throw  new APIManagementException(msg, e);
                }
            }
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
        return apiSortedSet;
    }


    /**
     * To get the API from generic artifact, if the user is authorized to view it.
     *
     * @param apiArtifact API Artifact.
     * @return API if the user is authorized  to view this.
     */
    protected API getAPI(GenericArtifact apiArtifact) throws APIManagementException {
        return APIUtil.getAPI(apiArtifact, registry);
    }

    /**
     * Returns the corresponding application given the uuid.
     *
     * @param uuid uuid of the Application.
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    public Application getApplicationByUUID(String uuid) throws APIManagementException {
        Application application = apiMgtDAO.getApplicationByUUID(uuid);
        if (application != null) {
            Set<APIKey> keys = getApplicationKeys(application.getId());
            for (APIKey key : keys) {
                if (APIConstants.JWT.equals(application.getTokenType())) {
                    key.setAccessToken("");
                }
                application.addKey(key);
            }
        }
        return application;
    }

    /**
     * Returns the corresponding application given the uuid with keys for a specific tenant.
     *
     * @param uuid uuid of the Application.
     * @param tenantDomain domain of the accessed store.
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    public Application getApplicationByUUID(String uuid, String tenantDomain) throws APIManagementException {
        Application application = apiMgtDAO.getApplicationByUUID(uuid);
        if (application != null) {
            Set<APIKey> keys = getApplicationKeys(application.getId(), tenantDomain);
            for (APIKey key : keys) {
                if (APIConstants.JWT.equals(application.getTokenType())) {
                    key.setAccessToken("");
                }
                application.addKey(key);
            }
        }
        return application;
    }

    @Override
    public Application getLightweightApplicationByUUID(String uuid) throws APIManagementException {
        return apiMgtDAO.getApplicationByUUID(uuid);
    }

    /**
     * returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Subscription
     * @return SubscribedAPI object which is related to the UUID
     * @throws APIManagementException
     */
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        return apiMgtDAO.getSubscriptionByUUID(uuid);
    }

    protected final void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }

    protected final void handleException(String msg) throws APIManagementException {
        throw new APIManagementException(msg);
    }

    protected final void handleResourceAlreadyExistsException(String msg) throws APIMgtResourceAlreadyExistsException {
        throw new APIMgtResourceAlreadyExistsException(msg);
    }

    protected final void handleResourceNotFoundException(String msg) throws APIMgtResourceNotFoundException {
        throw new APIMgtResourceNotFoundException(msg);
    }

    protected final void handlePolicyNotFoundException(String msg) throws PolicyNotFoundException {
        throw new PolicyNotFoundException(msg);
    }

    protected final void handleBlockConditionNotFoundException(String msg) throws BlockConditionNotFoundException {
        throw new BlockConditionNotFoundException(msg);
    }

    protected final void handleApplicationNameContainSpacesException(String msg)
                                                                throws ApplicationNameWhiteSpaceValidationException {
        throw new ApplicationNameWhiteSpaceValidationException(msg);
    }

    protected final void handleApplicationNameContainsInvalidCharactersException(String msg) throws
                                                                        ApplicationNameWithInvalidCharactersException{
        throw new ApplicationNameWithInvalidCharactersException(msg);
    }


    public Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException {
        return Collections.emptySet();
    }

    public API getAPI(APIIdentifier identifier, APIIdentifier oldIdentifier, String oldContext) throws
            APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact, registry, oldIdentifier, oldContext);

        } catch (RegistryException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public Set<Tier> getAllTiers() throws APIManagementException {
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Map<String, Tier> tierMap;

        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            tierMap = APIUtil.getAllTiers();
        } else {
            boolean isTenantFlowStarted = false;
            try {
                if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    startTenantFlow(tenantDomain);
                    isTenantFlowStarted = true;
                }
                tierMap = APIUtil.getAllTiers(tenantId);
            } finally {
                if (isTenantFlowStarted) {
                    endTenantFlow();
                }
            }
        }

        tiers.addAll(tierMap.values());
        return tiers;
    }

    @Override
    public Set<Tier> getAllTiers(String tenantDomain) throws APIManagementException {
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Map<String, Tier> tierMap;
        boolean isTenantFlowStarted = false;

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                startTenantFlow(tenantDomain);
                isTenantFlowStarted = true;
            }

            int requestedTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (requestedTenantId == MultitenantConstants.SUPER_TENANT_ID
                    || requestedTenantId == MultitenantConstants.INVALID_TENANT_ID) {
                tierMap = APIUtil.getAllTiers();
            } else {
                tierMap = APIUtil.getAllTiers(requestedTenantId);
            }
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }

        tiers.addAll(tierMap.values());
        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers() throws APIManagementException {
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());

        Map<String, Tier> tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
        tiers.addAll(tierMap.values());

        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers(String tenantDomain) throws APIManagementException {

        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Map<String, Tier> tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
        tiers.addAll(tierMap.values());
        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @param tierType type of the tiers (api,resource ot application)
     * @param username current logged user
     * @return Set<Tier> return list of tier names
     * @throws APIManagementException APIManagementException if failed to get the predefined tiers
     */
    public Set<Tier> getTiers(int tierType, String username) throws APIManagementException {
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());

        String tenantDomain = getTenantDomain(username);
        Map<String, Tier> tierMap;

        int tenantIdFromUsername = APIUtil.getTenantId(username);
        if (tierType == APIConstants.TIER_API_TYPE) {
            tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantIdFromUsername);
        } else if (tierType == APIConstants.TIER_RESOURCE_TYPE) {
            tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_API, tenantIdFromUsername);
        } else if (tierType == APIConstants.TIER_APPLICATION_TYPE) {
            tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_APP, tenantIdFromUsername);
        } else {
            throw new APIManagementException("No such a tier type : " + tierType);
        }
        tiers.addAll(tierMap.values());

        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Map<String, String>
     */
    public Map<String, String> getTenantDomainMappings(String tenantDomain, String apiType) throws APIManagementException {
        return APIUtil.getDomainMappings(tenantDomain, apiType);
    }


    public boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException {

        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            if (contextTemplate != null && contextTemplate.startsWith("/t/")) {
                contextTemplate =
                        contextTemplate.replace("/t/" + getTenantDomainFromUrl(contextTemplate), "");
            }
            contextTemplate = "/t/" + tenantDomain + contextTemplate;
        }
        return apiMgtDAO.isDuplicateContextTemplate(contextTemplate);
    }

    @Override
    public List<String> getApiNamesMatchingContext(String contextTemplate) throws APIManagementException {
        return apiMgtDAO.getAPINamesMatchingContext(contextTemplate);
    }


    public Policy[] getPolicies(String username, String level) throws APIManagementException {
        Policy[] policies = null;

        int tenantID = APIUtil.getTenantId(username);

        if (PolicyConstants.POLICY_LEVEL_API.equals(level)) {
            policies = apiMgtDAO.getAPIPolicies(tenantID);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(level)) {
            policies = apiMgtDAO.getApplicationPolicies(tenantID);
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(level)) {
            policies = apiMgtDAO.getSubscriptionPolicies(tenantID);
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(level)) {
            policies = apiMgtDAO.getGlobalPolicies(tenantID);
        }

        //Get the API Manager configurations and check whether the unlimited tier is disabled. If disabled, remove
        // the tier from the array.
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        ThrottleProperties throttleProperties = apiManagerConfiguration.getThrottleProperties();

        if (!throttleProperties.isEnableUnlimitedTier()) {
            List<Policy> policiesWithoutUnlimitedTier = new ArrayList<Policy>();

            if (policies != null) {
                for (Policy policy : policies) {
                    if (!APIConstants.UNLIMITED_TIER_NAME.equalsIgnoreCase(policy.getPolicyName())) {
                        policiesWithoutUnlimitedTier.add(policy);
                    }
                }
            }
            policies = policiesWithoutUnlimitedTier.toArray(new Policy[0]);
        }
        return policies;
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, String requestedTenantDomain,
                                                   int start, int end, boolean isLazyLoad) throws APIManagementException {
        return searchPaginatedAPIs(searchQuery, requestedTenantDomain, start, end, isLazyLoad, false);
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, String requestedTenantDomain, int start, int end,
            boolean isLazyLoad, boolean isPublisherListing) throws APIManagementException {
        Map<String, Object> result = new HashMap<String, Object>();
        boolean isTenantFlowStarted = false;
        String[] searchQueries = searchQuery.split("&");
        StringBuilder filteredQuery = new StringBuilder();
        String subQuery = null;

        if (log.isDebugEnabled()) {
            log.debug("Original search query received : " + searchQuery);
        }

        // Filtering the queries related with custom properties
        for (String query : searchQueries) {
            if (searchQuery.startsWith(APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX) ||
                    searchQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
                subQuery = query;
                break;
            }
            // If the query does not contains "=" then it is an errornous scenario.
            if (query.contains("=")) {
                String[] searchKeys = query.split("=");

                if (searchKeys.length >= 2) {
                    if (!Arrays.asList(APIConstants.API_SEARCH_PREFIXES).contains(searchKeys[0].toLowerCase())) {
                        if (log.isDebugEnabled()) {
                            log.debug(searchKeys[0] + " does not match with any of the reserved key words. Hence"
                                    + " appending " + APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX + " as prefix");
                        }
                        searchKeys[0] = (APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX + searchKeys[0]);
                    }

                    // Ideally query keys for label and  category searchs are as below
                    //      label -> labels_labelName
                    //      category -> apiCategories_categoryName
                    // Since these are not user friendly we allow to use prefixes label and api-category. And label and
                    // category search should only return results that exactly match.
                    if (searchKeys[0].equals(APIConstants.LABEL_SEARCH_TYPE_PREFIX)) {
                        searchKeys[0] = APIConstants.API_LABELS_GATEWAY_LABELS;
                        searchKeys[1] = searchKeys[1].replace("*", "");
                    } else if (searchKeys[0].equals(APIConstants.CATEGORY_SEARCH_TYPE_PREFIX)) {
                        searchKeys[0] = APIConstants.API_CATEGORIES_CATEGORY_NAME;
                        searchKeys[1] = searchKeys[1].replace("*", "");
                    }

                    if (filteredQuery.length() == 0) {
                        filteredQuery.append(searchKeys[0]).append("=").append(searchKeys[1]);
                    } else {
                        filteredQuery.append("&").append(searchKeys[0]).append("=").append(searchKeys[1]);
                    }
                }
            } else {
                filteredQuery.append(query);
            }
        }
        searchQuery = filteredQuery.toString();
        if (log.isDebugEnabled()) {
            log.debug("Final search query after the post processing for the custom properties : " + searchQuery);
        }
        try {
            boolean isTenantMode = (requestedTenantDomain != null);
            if (isTenantMode && !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                startTenantFlow(requestedTenantDomain);
            } else {
                requestedTenantDomain = org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                startTenantFlow(requestedTenantDomain);

            }

            Registry userRegistry;
            int tenantIDLocal = 0;
            String userNameLocal = this.username;
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant store anonymous mode
                tenantIDLocal = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                APIUtil.loadTenantRegistry(tenantIDLocal);
                userRegistry = getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantIDLocal);
                userNameLocal = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                if (!requestedTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    APIUtil.loadTenantConfigBlockingMode(requestedTenantDomain);
                }
            } else {
                userRegistry = this.registry;
                tenantIDLocal = tenantId;
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            if (subQuery != null && subQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
                Map<Documentation, API> apiDocMap =
                        searchAPIDoc(userRegistry, tenantIDLocal, userNameLocal, subQuery.split("=")[1]);
                result.put("apis", apiDocMap);
                /*Pagination for Document search results is not supported yet, hence length is sent as end-start*/
                if (apiDocMap.isEmpty()) {
                    result.put("length", 0);
                } else {
                    result.put("length", end - start);
                }
            } else if (subQuery != null && subQuery.startsWith(APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX)) {
                result = searchAPIsByURLPattern(userRegistry, subQuery.split("=")[1], start, end);
            } else if (searchQuery != null && searchQuery.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX)) {
                result = searchPaginatedAPIsByContent(userRegistry, tenantIDLocal, searchQuery, start, end, isLazyLoad);
            } else {
                result = searchPaginatedAPIs(userRegistry, tenantIDLocal, searchQuery, start, end, isLazyLoad,
                        isPublisherListing);
            }

        } catch (Exception e) {
            String msg = "Failed to Search APIs";
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
        return result;
    }

    /**
     * To search API With URL pattern
     * @param registry Registry to search.
     * @param searchTerm Term to be searched.
     * @param start Start index
     * @param end End index.
     * @return All the APIs, that matches given criteria
     * @throws APIManagementException API Management Exception.
     */
    protected Map<String, Object> searchAPIsByURLPattern(Registry registry, String searchTerm, int start, int end)
            throws APIManagementException {
        return APIUtil.searchAPIsByURLPattern(registry, searchTerm, start, end);
    }

    /**
     * Delete an existing global mediation policy
     *
     * @param mediationPolicyId uuid of the global mediation policy
     * @return true if deleted successfully
     * @throws APIManagementException If failed to delete mediation policy
     */
    @Override
    public boolean deleteGlobalMediationPolicy(String mediationPolicyId) throws APIManagementException {
        //Get registry resource correspond to the uuid
        Resource mediationResource = this.getCustomMediationResourceFromUuid(mediationPolicyId);
        if (mediationResource != null) {
            //If resource exists
            String mediationPath = mediationResource.getPath();
            try {
                if (registry.resourceExists(mediationPath)) {
                    ////TODO : validation if policy been use by any API
                    registry.delete(mediationPath);
                    //Verify if deleted successfully
                    if (!registry.resourceExists(mediationPath)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Mediation policy deleted successfully");
                        }
                        return true;
                    }
                }
            } catch (RegistryException e) {
                String msg = "Failed to delete global mediation policy " + mediationPolicyId;
                throw new APIManagementException(msg, e);
            }
        }
        return false;
    }

    /**
     * Returns the uuid of the updated/created mediation policy
     *
     * @param mediationPolicyPath path to the registry resource
     * @return uuid of the given registry resource or null
     */
    @Override
    public String getCreatedResourceUuid(String mediationPolicyPath) {
        try {
            Resource resource = registry.get(mediationPolicyPath);
            return resource.getUUID();
        } catch (RegistryException e) {
            log.error("error occurred while getting created mediation policy uuid", e);
        }
        return null;
    }

    /**
     * Returns the mediation policy registry resource correspond to the given identifier
     *
     * @param mediationPolicyId uuid of the mediation resource
     * @return Registry resource of given identifier or null
     * @throws APIManagementException If failed to get the registry resource of given uuid
     */
    @Override
    public Resource getCustomMediationResourceFromUuid(String mediationPolicyId)
            throws APIManagementException {
        String resourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        try {
            Resource resource = registry.get(resourcePath);
            //resource : customsequences
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    Resource typeResource = registry.get(type);
                    //typeResource: in/ out/ fault
                    if (typeResource instanceof Collection) {
                        String[] policyArray = ((Collection) typeResource).getChildren();
                        if (policyArray.length > 0) {
                            for (String policy : policyArray) {
                                Resource mediationResource = registry.get(policy);
                                //mediationResource: eg .log_in_msg.xml
                                String resourceId = mediationResource.getUUID();
                                if (resourceId.equals(mediationPolicyId)) {
                                    //If registry resource id matches given identifier returns that
                                    // registry resource
                                    return mediationResource;
                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while accessing registry objects";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Returns Registry resource matching given mediation policy identifier
     *
     * @param identifier API identifier
     * @param uuid         mediation policy identifier
     * @param resourcePath registry path to the API resource
     * @return Registry resource matches given identifier or null
     * @throws APIManagementException If fails to get the resource matching given identifier
     */
    @Override
    public Resource getApiSpecificMediationResourceFromUuid(Identifier identifier, String uuid, String resourcePath)
            throws APIManagementException {
        try {
            Resource resource = registry.get(resourcePath);
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    //Check for mediation policy resource
                    if ((type.equalsIgnoreCase(resourcePath + RegistryConstants.PATH_SEPARATOR +
                            APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN)) ||
                            (type.equalsIgnoreCase(resourcePath + RegistryConstants.PATH_SEPARATOR +
                                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT)) ||
                            (type.equalsIgnoreCase(resourcePath + RegistryConstants.PATH_SEPARATOR +
                                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT))) {
                        Resource sequenceType = registry.get(type);
                        //sequenceType eg: in / out /fault
                        if (sequenceType instanceof Collection) {
                            String[] mediationPolicyArr = ((Collection) sequenceType).getChildren();
                            for (String mediationPolicy : mediationPolicyArr) {
                                Resource mediationResource = registry.get(mediationPolicy);
                                String resourceId = mediationResource.getUUID();
                                if (resourceId.equalsIgnoreCase(uuid)) {
                                    return mediationResource;
                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while obtaining registry objects";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Returns a list of API specific mediation policies from registry
     *
     * @param apiIdentifier API identifier
     * @return List of api specific mediation objects available
     * @throws APIManagementException If unable to get mediation policies of specified API Id
     */
    @Override
    public List<Mediation> getAllApiSpecificMediationPolicies(APIIdentifier apiIdentifier) throws APIManagementException {
        List<Mediation> mediationList = new ArrayList<Mediation>();
        Mediation mediation;
        String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
        apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
        try {
            //Getting API registry resource
            Resource resource = registry.get(apiResourcePath);
            //resource eg: /_system/governance/apimgt/applicationdata/provider/admin/calculatorAPI/2.0
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    //Check for mediation policy sequences
                    if ((type.equalsIgnoreCase(apiResourcePath + RegistryConstants.PATH_SEPARATOR +
                            APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN)) ||
                            (type.equalsIgnoreCase(apiResourcePath + RegistryConstants.PATH_SEPARATOR +
                                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT)) ||
                            (type.equalsIgnoreCase(apiResourcePath + RegistryConstants.PATH_SEPARATOR +
                                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT))) {
                        Resource typeResource = registry.get(type);
                        //typeResource : in / out / fault
                        if (typeResource instanceof Collection) {
                            String[] mediationPolicyArr = ((Collection) typeResource).getChildren();
                            if (mediationPolicyArr.length > 0) {
                                for (String mediationPolicy : mediationPolicyArr) {
                                    Resource policyResource = registry.get(mediationPolicy);
                                    //policyResource eg: custom_in_message

                                    //Get uuid of the registry resource
                                    String resourceId = policyResource.getUUID();

                                    //Get mediation policy config
                                    try {
                                        String contentString = IOUtils.toString
                                                (policyResource.getContentStream(),
                                                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                                        //Extract name from the policy config
                                        OMElement omElement = AXIOMUtil.stringToOM(contentString);
                                        OMAttribute attribute = omElement.getAttribute(new QName("name"));
                                        String mediationPolicyName = attribute.getAttributeValue();
                                        mediation = new Mediation();
                                        mediation.setUuid(resourceId);
                                        mediation.setName(mediationPolicyName);
                                        //Extracting mediation policy type from the registry resource path
                                        String resourceType = type.substring(type.lastIndexOf("/") + 1);
                                        mediation.setType(resourceType);
                                        mediationList.add(mediation);
                                    } catch (XMLStreamException e) {
                                        // If exception been caught flow will continue with next mediation policy
                                        log.error("Error occurred while getting omElement out of" +
                                                " mediation content", e);
                                    } catch (IOException e) {
                                        log.error("Error occurred while converting the content " +
                                                "stream of mediation " + mediationPolicy + " to string", e);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error occurred  while getting Api Specific mediation policies ";
            throw new APIManagementException(msg, e);
        }
        return mediationList;
    }

    /**
     * Returns the mediation policy name specify inside mediation config
     *
     * @param config mediation config content
     * @return name of the mediation policy or null
     */
    @Override
    public String getMediationNameFromConfig(String config) {
        try {
            //convert xml content in to json
            String configInJson = XML.toJSONObject(config).toString();
            JSONParser parser = new JSONParser();
            //Extracting mediation policy name from the json string
            JSONObject jsonObject = (JSONObject) parser.parse(configInJson);
            JSONObject rootObject = (JSONObject) jsonObject.get(APIConstants.MEDIATION_SEQUENCE_ELEM);
            String name = rootObject.get(APIConstants.POLICY_NAME_ELEM).toString();
            //explicitly add .xml extension to the name and return
            return name + APIConstants.MEDIATION_CONFIG_EXT;
        } catch (JSONException e) {
            log.error("Error occurred while converting the mediation config string to json", e);
        } catch (ParseException e) {
            log.error("Error occurred while parsing config json string in to json object", e);
        }
        return null;
    }

    /**
     * Returns Mediation policy specify by given identifier
     * @param identifier API or Product identifier
     * @param apiResourcePath   registry path to the API resource
     * @param mediationPolicyId mediation policy identifier
     * @return Mediation object contains details of the mediation policy or null
     */
    @Override
    public Mediation getApiSpecificMediationPolicy(Identifier identifier, String apiResourcePath,
            String mediationPolicyId) throws APIManagementException {
        //Get registry resource correspond to given policy identifier
        Resource mediationResource =
                getApiSpecificMediationResourceFromUuid(identifier, mediationPolicyId, apiResourcePath);
        Mediation mediation = null;
        if (mediationResource != null) {
            try {
                //Get mediation policy config content
                String contentString = IOUtils.toString(mediationResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                //Extracting name specified in the mediation config
                OMElement omElement = AXIOMUtil.stringToOM(contentString);
                OMAttribute attribute = omElement.getAttribute(new QName("name"));
                String mediationPolicyName = attribute.getAttributeValue();
                mediation = new Mediation();
                mediation.setUuid(mediationResource.getUUID());
                mediation.setName(mediationPolicyName);
                //Extracting mediation policy type from registry path
                String resourcePath = mediationResource.getPath();
                String[] path = resourcePath.split(RegistryConstants.PATH_SEPARATOR);
                String resourceType = path[(path.length - 2)];
                mediation.setType(resourceType);
                mediation.setConfig(contentString);
            } catch (XMLStreamException e) {
                String errorMsg = "Error occurred while getting omElement out of mediation content";
                throw new APIManagementException(errorMsg, e);
            } catch (IOException e) {
                String errorMsg = "Error occurred while converting content stream into string ";
                throw new APIManagementException(errorMsg, e);
            } catch (RegistryException e) {
                String errorMsg = "Error occurred while accessing content stream of mediation" +
                        " policy";
                throw new APIManagementException(errorMsg, e);
            }
        }
        return mediation;
    }

    /**
     * Delete existing API specific mediation policy
     * @param identifier API or Product identifier
     * @param apiResourcePath   path to the API registry resource
     * @param mediationPolicyId mediation policy identifier
     */
    @Override
    public Boolean deleteApiSpecificMediationPolicy(Identifier identifier, String apiResourcePath,
            String mediationPolicyId) throws APIManagementException {
        Resource mediationResource =
                this.getApiSpecificMediationResourceFromUuid(identifier, mediationPolicyId, apiResourcePath);
        if (mediationResource != null) {
            //If resource exists
            String mediationPath = mediationResource.getPath();
            try {
                if (registry.resourceExists(mediationPath)) {
                    registry.delete(mediationPath);
                    if (!registry.resourceExists(mediationPath)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Mediation policy deleted successfully");
                        }
                        return true;
                    }
                }
            } catch (RegistryException e) {
                String msg = "Failed to delete specific mediation policy ";
                throw new APIManagementException(msg, e);
            }
        }
        return false;
    }


    /**
     * Returns true if resource already exists in registry
     *
     * @param mediationPolicyPath resource path
     * @return true, If resource exists
     */
    @Override
    public boolean checkIfResourceExists(String mediationPolicyPath) throws APIManagementException {
        boolean value = false;
        try {
            if (registry.resourceExists(mediationPolicyPath)) {
                value = true;
            }
        } catch (RegistryException e) {
            String msg = "Error while obtaining registry objects";
            throw new APIManagementException(msg, e);
        }
        return value;
    }

    @Override
    public List<String> getApiVersionsMatchingApiName(String apiName,String username) throws APIManagementException {
        return apiMgtDAO.getAPIVersionsMatchingApiName(apiName,username);
    }
    
    public Map<String, Object> searchPaginatedAPIs(Registry registry, int tenantId, String searchQuery, int start,
            int end, boolean limitAttributes) throws APIManagementException {
        return searchPaginatedAPIs(registry, tenantId, searchQuery, start, end, limitAttributes, false);
    }

    /**
     * Returns API Search result based on the provided query. This search method supports '&' based concatenate
     * search in multiple fields.
     *
     * @param registry
     * @param tenantId
     * @param searchQuery Ex: provider=*admin*&version=*1*
     * @return API result
     * @throws APIManagementException
     */

    public Map<String, Object> searchPaginatedAPIs(Registry registry, int tenantId, String searchQuery, int start,
            int end, boolean limitAttributes, boolean reducedPublisherAPIInfo) throws APIManagementException {

        SortedSet<Object> apiSet = new TreeSet<>(new APIAPIProductNameComparator());
        List<Object> apiList = new ArrayList<>();
        Map<String, Object> result = new HashMap<String, Object>();
        int totalLength = 0;
        boolean isMore = false;
        try {
            String paginationLimit = getAPIManagerConfiguration()
                    .getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);

            // If the Config exists use it to set the pagination limit
            final int maxPaginationLimit;
            if (paginationLimit != null) {
                // The additional 1 added to the maxPaginationLimit is to help us determine if more
                // APIs may exist so that we know that we are unable to determine the actual total
                // API count. We will subtract this 1 later on so that it does not interfere with
                // the logic of the rest of the application
                int pagination = Integer.parseInt(paginationLimit);

                // Because the store jaggery pagination logic is 10 results per a page we need to set pagination
                // limit to at least 11 or the pagination done at this level will conflict with the store pagination
                // leading to some of the APIs not being displayed
                if (pagination < 11) {
                    pagination = 11;
                    log.warn("Value of '" + APIConstants.API_STORE_APIS_PER_PAGE + "' is too low, defaulting to 11");
                }
                maxPaginationLimit = start + pagination + 1;
            }
            // Else if the config is not specified we go with default functionality and load all
            else {
                maxPaginationLimit = Integer.MAX_VALUE;
            }
            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                    .findGovernanceArtifacts(getSearchQuery(searchQuery), registry, APIConstants.API_RXT_MEDIA_TYPE,
                            true);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;
            if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                if (searchQuery.contains(APIConstants.API_OVERVIEW_PROVIDER)) {
                    searchQuery = searchQuery.replaceAll(APIConstants.API_OVERVIEW_PROVIDER, APIConstants.API_OVERVIEW_OWNER);
                    governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(getSearchQuery(searchQuery), registry,
                            APIConstants.API_RXT_MEDIA_TYPE, true);
                    if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                        isFound = false;
                    }
                } else {
                    isFound = false;
                }
            }

            if (!isFound) {
                result.put("apis", apiSet);
                result.put("length", 0);
                result.put("isMore", isMore);
                return result;
            }

            // Check to see if we can speculate that there are more APIs to be loaded
            if (maxPaginationLimit == totalLength) {
                isMore = true;  // More APIs exist, cannot determine total API count without incurring perf hit
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }

            int tempLength = 0;
            for (GovernanceArtifact artifact : governanceArtifacts) {
                String type = artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE);

                if (APIConstants.API_PRODUCT.equals(type)) {
                    APIProduct resultAPI = APIUtil.getAPIProduct(artifact, registry);

                    if (resultAPI != null) {
                        apiList.add(resultAPI);
                    }
                } else {
                    API resultAPI;
                    if (limitAttributes) {
                        resultAPI = APIUtil.getAPI(artifact);
                    } else {
                        if(reducedPublisherAPIInfo) {
                            resultAPI = APIUtil.getReducedPublisherAPIForListing(artifact, registry);
                        } else {
                            resultAPI = APIUtil.getAPI(artifact, registry);
                        }
                    }
                    if (resultAPI != null) {
                        apiList.add(resultAPI);
                    }
                }

                // Ensure the APIs returned matches the length, there could be an additional API
                // returned due incrementing the pagination limit when getting from registry
                tempLength++;
                if (tempLength >= totalLength) {
                    break;
                }
            }

            // Creating a apiIds string
            String apiIdsString = "";
            int apiCount = apiList.size();
            if (!reducedPublisherAPIInfo) {
                for (int i = 0; i < apiCount; i++) {
                    Object api = apiList.get(i);
                    String apiId = "";
                    if (api instanceof API) {
                        apiId = ((API) api).getId().getApplicationId();
                    } else if (api instanceof APIProduct) {
                        apiId = ((APIProduct) api).getId().getApplicationId();
                    }

                    if (apiId != null && !apiId.isEmpty()) {
                        if (apiIdsString.isEmpty()) {
                            apiIdsString = apiId;
                        } else {
                            apiIdsString = apiIdsString + "," + apiId;
                        }
                    }
                }
            }

            // setting scope
            if (!apiIdsString.isEmpty() && !reducedPublisherAPIInfo) {

                Map<String, Set<Scope>> apiScopeSet = getScopesForAPIS(apiIdsString);
                if (apiScopeSet.size() > 0) {
                    for (int i = 0; i < apiCount; i++) {
                        Object api = apiList.get(i);
                        String apiId = "";
                        if (api instanceof API) {
                            apiId = ((API) api).getId().getApplicationId();
                        } else if (api instanceof APIProduct) {
                            apiId = ((APIProduct) api).getId().getApplicationId();
                        }
                        if (apiId != null && apiId != "") {
                            Set<Scope> scopes = apiScopeSet.get(apiId);
                            if (api instanceof API) {
                                ((API) api).setScopes(scopes);
                            } else if (api instanceof APIProduct) {
                                ((APIProduct) api).setScopes(scopes);
                            }
                        }
                    }
                }
            }
            apiSet.addAll(apiList);
        } catch (RegistryException e) {
            String msg = "Failed to search APIs with type";
            throw new APIManagementException(msg, e);
        } finally {
            PaginationContext.destroy();
        }
        result.put("apis", apiSet);
        result.put("length", totalLength);
        result.put("isMore", isMore);
        return result;
    }

    private Map<String, Set<Scope>> getScopesForAPIS(String apiIdsString) throws APIManagementException {

        Map<String, Set<Scope>> apiToScopeMapping = new HashMap<>();
        Map<String, Set<String>> apiToScopeKeyMapping = apiMgtDAO.getScopesForAPIS(apiIdsString);
        for (String apiId : apiToScopeKeyMapping.keySet()) {
            Set<Scope> apiScopes = new LinkedHashSet<>();
            Set<String> scopeKeys = apiToScopeKeyMapping.get(apiId);
            for (String scopeKey : scopeKeys) {
                Scope scope = scopesDAO.getScope(scopeKey, tenantId);
                apiScopes.add(scope);
            }
            apiToScopeMapping.put(apiId, apiScopes);
        }
        return apiToScopeMapping;
    }

    /**
     * Search api resources by their content
     *
     * @param registry
     * @param searchQuery
     * @param start
     * @param end
     * @return
     * @throws APIManagementException
     */
    public Map<String, Object> searchPaginatedAPIsByContent(Registry registry, int tenantId, String searchQuery, int start, int end,
            boolean limitAttributes) throws APIManagementException {

        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        SortedSet<APIProduct> apiProductSet = new TreeSet<APIProduct>(new APIProductNameComparator());
        Map<Documentation, API> docMap = new HashMap<Documentation, API>();
        Map<Documentation, APIProduct> productDocMap = new HashMap<Documentation, APIProduct>();
        Map<String, Object> result = new HashMap<String, Object>();
        int totalLength = 0;
        boolean isMore = false;

        //SortedSet<Object> compoundResult = new TreeSet<Object>(new ContentSearchResultNameComparator());
        ArrayList<Object> compoundResult = new ArrayList<Object>();

        try {
            GenericArtifactManager apiArtifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifactManager docArtifactManager = APIUtil.getArtifactManager(registry, APIConstants.DOCUMENTATION_KEY);

            String paginationLimit = getAPIManagerConfiguration()
                    .getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);

            // If the Config exists use it to set the pagination limit
            final int maxPaginationLimit;
            if (paginationLimit != null) {
                // The additional 1 added to the maxPaginationLimit is to help us determine if more
                // APIs may exist so that we know that we are unable to determine the actual total
                // API count. We will subtract this 1 later on so that it does not interfere with
                // the logic of the rest of the application
                int pagination = Integer.parseInt(paginationLimit);

                // Because the store jaggery pagination logic is 10 results per a page we need to set pagination
                // limit to at least 11 or the pagination done at this level will conflict with the store pagination
                // leading to some of the APIs not being displayed
                if (pagination < 11) {
                    pagination = 11;
                    log.warn("Value of '" + APIConstants.API_STORE_APIS_PER_PAGE + "' is too low, defaulting to 11");
                }
                maxPaginationLimit = start + pagination + 1;
            }
            // Else if the config is not specified we go with default functionality and load all
            else {
                maxPaginationLimit = Integer.MAX_VALUE;
            }
            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            if (tenantId == -1) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }

            UserRegistry systemUserRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);
            ContentBasedSearchService contentBasedSearchService = new ContentBasedSearchService();
            String newSearchQuery = getSearchQuery(searchQuery);
            String[] searchQueries = newSearchQuery.split("&");

            String apiState = "";
            String publisherRoles = "";
            Map<String, String> attributes = new HashMap<String, String>();
            for (String searchCriterea : searchQueries) {
                String[] keyVal = searchCriterea.split("=");
                if (APIConstants.STORE_VIEW_ROLES.equals(keyVal[0])) {
                    attributes.put("propertyName", keyVal[0]);
                    attributes.put("rightPropertyValue", keyVal[1]);
                    attributes.put("rightOp", "eq");
                } else if (APIConstants.PUBLISHER_ROLES.equals(keyVal[0])) {
                    publisherRoles = keyVal[1];
                } else {
                    if (APIConstants.LCSTATE_SEARCH_KEY.equals(keyVal[0])) {
                        apiState = keyVal[1];
                        continue;
                    }
                    attributes.put(keyVal[0], keyVal[1]);
                }
            }

            //check whether the new document indexer is engaged
            RegistryConfigLoader registryConfig = RegistryConfigLoader.getInstance();
            Map<String, Indexer> indexerMap = registryConfig.getIndexerMap();
            Indexer documentIndexer = indexerMap.get(APIConstants.DOCUMENT_MEDIA_TYPE_KEY);
            String complexAttribute;
            if (documentIndexer != null && documentIndexer instanceof DocumentIndexer) {
                //field check on document_indexed was added to prevent unindexed(by new DocumentIndexer) from coming up as search results
                //on indexed documents this property is always set to true
                complexAttribute = ClientUtils.escapeQueryChars(APIConstants.API_RXT_MEDIA_TYPE) + " OR mediaType_s:("  + ClientUtils
                        .escapeQueryChars(APIConstants.DOCUMENT_RXT_MEDIA_TYPE) + " AND document_indexed_s:true)";

                //construct query such that publisher roles is checked in properties for api artifacts and in fields for document artifacts
                //this was designed this way so that content search can be fully functional if registry is re-indexed after engaging DocumentIndexer
                if (!StringUtils.isEmpty(publisherRoles)) {
                    complexAttribute =
                            "(" + ClientUtils.escapeQueryChars(APIConstants.API_RXT_MEDIA_TYPE) + " AND publisher_roles_ss:"
                                    + publisherRoles + ") OR mediaType_s:("  + ClientUtils
                                    .escapeQueryChars(APIConstants.DOCUMENT_RXT_MEDIA_TYPE) + " AND publisher_roles_s:" + publisherRoles + ")";
                }
            } else {
                //document indexer required for document content search is not engaged, therefore carry out the search only for api artifact contents
                complexAttribute = ClientUtils.escapeQueryChars(APIConstants.API_RXT_MEDIA_TYPE);
                if (!StringUtils.isEmpty(publisherRoles)) {
                    complexAttribute =
                            "(" + ClientUtils.escapeQueryChars(APIConstants.API_RXT_MEDIA_TYPE) + " AND publisher_roles_ss:"
                                    + publisherRoles + ")";
                }
            }


            attributes.put(APIConstants.DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD, complexAttribute);
            attributes.put(APIConstants.API_OVERVIEW_STATUS, apiState);

            SearchResultsBean resultsBean = contentBasedSearchService.searchByAttribute(attributes, systemUserRegistry);
            String errorMsg = resultsBean.getErrorMessage();
            if (errorMsg != null) {
                handleException(errorMsg);
            }

            ResourceData[] resourceData = resultsBean.getResourceDataList();

            if (resourceData == null || resourceData.length == 0) {
                result.put("apis", compoundResult);
                result.put("length", 0);
                result.put("isMore", isMore);
            }

            totalLength = PaginationContext.getInstance().getLength();

            // Check to see if we can speculate that there are more APIs to be loaded
            if (maxPaginationLimit == totalLength) {
                isMore = true;  // More APIs exist, cannot determine total API count without incurring perf hit
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }

            for (ResourceData data : resourceData) {
                String resourcePath = data.getResourcePath();
                int index = resourcePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
                resourcePath = resourcePath.substring(index);
                Resource resource = registry.get(resourcePath);
                if (APIConstants.DOCUMENT_RXT_MEDIA_TYPE.equals(resource.getMediaType())) {
                    Resource docResource = registry.get(resourcePath);
                    String docArtifactId = docResource.getUUID();
                    GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docArtifactId);
                    Documentation doc = APIUtil.getDocumentation(docArtifact);
                    Association[] docAssociations = registry
                            .getAssociations(resourcePath, APIConstants.DOCUMENTATION_ASSOCIATION);
                    API associatedAPI = null;
                    APIProduct associatedAPIProduct = null;
                    if (docAssociations.length > 0) { // a content can have one api association at most
                        String apiPath = docAssociations[0].getSourcePath();

                        Resource apiResource = registry.get(apiPath);
                        String apiArtifactId = apiResource.getUUID();
                        if (apiArtifactId != null) {
                            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiArtifactId);
                            if (APIConstants.API_PRODUCT.equals(
                                    apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE))) {
                                associatedAPIProduct = APIUtil.getAPIProduct(apiArtifact, registry);
                            } else {
                                associatedAPI = APIUtil.getAPI(apiArtifact, registry);
                            }
                        } else {
                            throw new GovernanceException("artifact id is null for " + apiPath);
                        }

                        if (associatedAPI != null && doc != null) {
                            docMap.put(doc, associatedAPI);
                        }
                        if (associatedAPIProduct != null && doc != null) {
                            productDocMap.put(doc, associatedAPIProduct);
                        }
                    }
                } else {
                    String apiArtifactId = resource.getUUID();
                    API api;
                    APIProduct apiProduct;
                    if (apiArtifactId != null) {
                        GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiArtifactId);
                        if (APIConstants.API_PRODUCT.
                                equals(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE))) {
                            apiProduct = APIUtil.getAPIProduct(apiArtifact, registry);
                            apiProductSet.add(apiProduct);
                        } else {
                            api = APIUtil.getAPI(apiArtifact, registry);
                            apiSet.add(api);
                        }
                    } else {
                        throw new GovernanceException("artifact id is null for " + resourcePath);
                    }
                }
            }

            compoundResult.addAll(apiSet);
            compoundResult.addAll(apiProductSet);
            compoundResult.addAll(docMap.entrySet());
            compoundResult.addAll(productDocMap.entrySet());
            compoundResult.sort(new ContentSearchResultNameComparator());
        } catch (RegistryException e) {
            handleException("Failed to search APIs by content", e);
        } catch (IndexerException e) {
            handleException("Failed to search APIs by content", e);
        }

        result.put("apis", compoundResult);
        result.put("length", totalLength);
        result.put("isMore", isMore);
        return result;
    }

    /**
     * gets the swagger definition timestamps as a map
     *
     * @param apiIdentifier
     * @return
     * @throws APIManagementException
     */
    public Map<String, String> getSwaggerDefinitionTimeStamps(APIIdentifier apiIdentifier) throws APIManagementException {
        String apiTenantDomain = getTenantDomain(apiIdentifier);
        try {
            Registry registryType;
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if (this.tenantDomain == null || isTenantDomainNotMatching(apiTenantDomain)) {
                int tenantId = getTenantManager().getTenantId(
                        apiTenantDomain);
                registryType = getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            return OASParserUtil.getAPIOpenAPIDefinitionTimeStamps(apiIdentifier, registryType);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while getting the lastUpdated time due to " + e.getMessage(), e);

        } catch (RegistryException e) {
            log.debug("Error while getting the lastUpdated time due to " + e.getMessage(), e);
        }
        return null;
    }

    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    /**
     * get the thumbnailLastUpdatedTime for a thumbnail for a given api
     *
     * @param apiIdentifier
     * @return
     * @throws APIManagementException
     */
    @Override
    public String getThumbnailLastUpdatedTime(APIIdentifier apiIdentifier) throws APIManagementException {
        String artifactPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();

        String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        try {
            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                Date lastModifiedTime = res.getLastModified();
                return lastModifiedTime == null ? String.valueOf(res.getCreatedTime().getTime()) : String.valueOf(lastModifiedTime.getTime());
            }
        } catch (RegistryException e) {
            String msg = "Error while loading API icon from the registry";
            throw new APIManagementException(msg, e);
        }
        return null;

    }

    /**
     * Search Apis by Doc Content
     *
     * @param registry     - Registry which is searched
     * @param tenantID     - Tenant id of logged in domain
     * @param username     - Logged in username
     * @param searchTerm   - Search value for doc
     * @return - Documentation to APIs map
     * @throws APIManagementException - If failed to get ArtifactManager for given tenant
     */
    public Map<Documentation, API> searchAPIDoc(Registry registry, int tenantID, String username,
            String searchTerm) throws APIManagementException {
        return APIUtil.searchAPIsByDoc(registry, tenantID, username, searchTerm, APIConstants.STORE_CLIENT);
    }

    /**
     * Returns API manager configurations.
     *
     * @return APIManagerConfiguration object
     */
    protected APIManagerConfiguration getAPIManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
    }

    /**
     * To get the search query.
     *
     * @param searchQuery Initial query
     */
    protected String getSearchQuery(String searchQuery) throws APIManagementException {
        return searchQuery;
    }

    /**
     * Returns the key associated with given application id.
     *
     * @param applicationId Id of the Application.
     * @return APIKey The key of the application.
     * @throws APIManagementException
     */
    protected Set<APIKey> getApplicationKeys(int applicationId) throws APIManagementException {
        return getApplicationKeys(applicationId, null);
    }

    /**
     * Returns the key associated with given application id.
     *
     * @param applicationId Id of the Application.
     * @return APIKey The key of the application.
     * @throws APIManagementException
     */
    protected Set<APIKey> getApplicationKeys(int applicationId, String xWso2Tenant) throws APIManagementException {

        Set<APIKey> apiKeyList = apiMgtDAO.getKeyMappingsFromApplicationId(applicationId);
        Set<APIKey> resultantApiKeyList = new HashSet<>();
        for (APIKey apiKey : apiKeyList) {
            String keyManagerName = apiKey.getKeyManager();
            String consumerKey = apiKey.getConsumerKey();
            String tenantDomain = this.tenantDomain;
            if (StringUtils.isNotEmpty(xWso2Tenant)) {
                tenantDomain = xWso2Tenant;
            }
            KeyManagerConfigurationDTO keyManagerConfigurationDTO = apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
            if (keyManagerConfigurationDTO == null) {
                keyManagerConfigurationDTO = apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerName);
                if (keyManagerConfigurationDTO != null) {
                    keyManagerName = keyManagerConfigurationDTO.getName();
                } else {
                    log.error("Key Manager: " + keyManagerName + " not found in database.");
                    continue;
                }
            }
            if (tenantDomain != null && !tenantDomain.equalsIgnoreCase(
                    keyManagerConfigurationDTO.getTenantDomain())) {
                continue;
            }
            KeyManager keyManager;
            if (keyManagerConfigurationDTO.isEnabled()) {
                keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
            } else {
                continue;
            }
            apiKey.setKeyManager(keyManagerConfigurationDTO.getName());

            if (StringUtils.isNotEmpty(consumerKey)) {
                if (keyManager != null) {
                    if (APIConstants.OAuthAppMode.MAPPED.name().equalsIgnoreCase(apiKey.getCreateMode())
                            && !isOauthAppValidationEnabled()) {
                        resultantApiKeyList.add(apiKey);
                    } else {
                        OAuthApplicationInfo oAuthApplicationInfo;
                        try {
                            oAuthApplicationInfo = keyManager.retrieveApplication(consumerKey);
                        } catch (APIManagementException e) {
                            log.error("Error while retrieving Application Information", e);
                            continue;
                        }
                        if (StringUtils.isNotEmpty(apiKey.getAppMetaData())) {
                            OAuthApplicationInfo storedOAuthApplicationInfo = new Gson()
                                    .fromJson(apiKey.getAppMetaData()
                                    , OAuthApplicationInfo.class);
                            if (oAuthApplicationInfo == null) {
                                oAuthApplicationInfo = storedOAuthApplicationInfo;
                            } else {

                                if (StringUtils.isEmpty(oAuthApplicationInfo.getCallBackURL())) {
                                    oAuthApplicationInfo.setCallBackURL(storedOAuthApplicationInfo.getCallBackURL());
                                }
                                if (oAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES) == null &&
                                        storedOAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES) !=
                                                null) {
                                    if (storedOAuthApplicationInfo
                                            .getParameter(APIConstants.JSON_GRANT_TYPES) instanceof String) {
                                        oAuthApplicationInfo.addParameter(APIConstants.JSON_GRANT_TYPES,
                                                ((String) storedOAuthApplicationInfo
                                                        .getParameter(APIConstants.JSON_GRANT_TYPES))
                                                        .replace(",", " "));
                                    } else {
                                        oAuthApplicationInfo.addParameter(APIConstants.JSON_GRANT_TYPES,
                                                storedOAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES));
                                    }
                                }
                                if (StringUtils.isEmpty(oAuthApplicationInfo.getClientSecret()) &&
                                        StringUtils.isNotEmpty(storedOAuthApplicationInfo.getClientSecret())) {
                                    oAuthApplicationInfo.setClientSecret(storedOAuthApplicationInfo.getClientSecret());
                                }
                            }
                        }
                        AccessTokenInfo tokenInfo = keyManager.getAccessTokenByConsumerKey(consumerKey);
                        if (oAuthApplicationInfo != null) {
                            apiKey.setConsumerSecret(oAuthApplicationInfo.getClientSecret());
                            apiKey.setCallbackUrl(oAuthApplicationInfo.getCallBackURL());
                            apiKey.setGrantTypes(
                                    (String) oAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES));
                            if (oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES) != null) {
                                apiKey.setAdditionalProperties(
                                        oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
                            }
                        }
                        if (tokenInfo != null) {
                            apiKey.setAccessToken(tokenInfo.getAccessToken());
                            apiKey.setValidityPeriod(tokenInfo.getValidityPeriod());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Access token does not exist for Consumer Key: " + consumerKey);
                            }
                        }
                        resultantApiKeyList.add(apiKey);
                    }
                } else {
                    log.error("Key Manager " + keyManagerName + " not initialized in tenant " + tenantDomain);
                }
            } else {
                resultantApiKeyList.add(apiKey);
            }
        }
        return resultantApiKeyList;
    }

    /**
     * Returns a single string containing the provided array of scopes.
     *
     * @param scopes The array of scopes.
     * @return String Single string containing the provided array of scopes.
     */
    private String getScopeString(String[] scopes) {
        return StringUtils.join(scopes, " ");
    }

    /**
     * Returns the corresponding application given the subscriberId and application name.
     *
     * @param subscriberId subscriberId of the Application
     * @param applicationName name of the Application
     * @throws APIManagementException
     */
    public Application getApplicationBySubscriberIdAndName(int subscriberId, String applicationName) throws
            APIManagementException {
        Application application = apiMgtDAO.getApplicationBySubscriberIdAndName(subscriberId, applicationName);
        if (application != null) {
            Set<APIKey> keys = getApplicationKeys(application.getId());
            for (APIKey key : keys) {
                if (APIConstants.JWT.equals(application.getTokenType())) {
                    key.setAccessToken("");
                }
                application.addKey(key);
            }
        }
        return application;
    }

    /**
     * Get API Product by registry artifact id
     *
     * @param uuid                  Registry artifact id
     * @param requestedTenantDomain tenantDomain for the registry
     * @return API Product of the provided artifact id
     * @throws APIManagementException
     */
    public APIProduct getAPIProductbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (requestedTenantDomain)) {
                int id = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }

            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiProductArtifact = artifactManager.getGenericArtifact(uuid);
            if (apiProductArtifact != null) {
                return getApiProduct(registry, apiProductArtifact);
            } else {
                String msg = "Failed to get API Product. API Product artifact corresponding to artifactId " + uuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API Product";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API Product";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Get API Product by product identifier
     *
     * @param identifier APIProductIdentifier
     * @return API product identified by provider identifier
     * @throws APIManagementException
     */
    public APIProduct getAPIProduct(APIProductIdentifier identifier) throws APIManagementException {
        String apiProductPath = APIUtil.getAPIProductPath(identifier);
        Registry registry;
        try {
            String productTenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            int productTenantId = getTenantManager()
                    .getTenantId(productTenantDomain);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(productTenantDomain)) {
                APIUtil.loadTenantRegistry(productTenantId);
            }

            if (this.tenantDomain == null || !this.tenantDomain.equals(productTenantDomain)) { //cross tenant scenario
                registry = getRegistryService().getGovernanceUserRegistry(
                        getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName())), productTenantId);
            } else {
                registry = this.registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);
            Resource productResource = registry.get(apiProductPath);
            String artifactId = productResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiProductPath);
            }
            GenericArtifact productArtifact = artifactManager.getGenericArtifact(artifactId);

            APIProduct apiProduct = APIUtil.getAPIProduct(productArtifact, registry);

            //todo : implement visibility

            return apiProduct;

        } catch (RegistryException e) {
            String msg = "Failed to get API Product from : " + apiProductPath;
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API Product from : " + apiProductPath;
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public Map<String, Object> searchPaginatedAPIProducts(String searchQuery, String requestedTenantDomain,
            int start, int end) throws APIManagementException {
        Map<String, Object> result = new HashMap<String, Object>();
        boolean isTenantFlowStarted = false;

        if (log.isDebugEnabled()) {
            log.debug("Original search query received : " + searchQuery);
        }

        try {
            boolean isTenantMode = (requestedTenantDomain != null);
            if (isTenantMode && !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                startTenantFlow(requestedTenantDomain);
            } else {
                requestedTenantDomain = org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                startTenantFlow(requestedTenantDomain);

            }

            Registry userRegistry;
            int tenantIDLocal = 0;
            String userNameLocal = this.username;
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant store anonymous mode
                tenantIDLocal = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                userRegistry = getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantIDLocal);
                userNameLocal = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
            } else {
                userRegistry = this.registry;
                tenantIDLocal = tenantId;
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            result = searchPaginatedAPIProducts(userRegistry, getSearchQuery(searchQuery), start, end);

        } catch (Exception e) {
            String msg = "Failed to Search APIs";
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
        return result;
    }

    /**
     * Returns APIProduct Search result based on the provided query.
     *
     * @param registry
     * @param searchQuery Ex: provider=*admin*
     * @return APIProduct result
     * @throws APIManagementException
     */

    public Map<String, Object> searchPaginatedAPIProducts(Registry registry, String searchQuery, int start, int end)
            throws APIManagementException {
        SortedSet<APIProduct> productSet = new TreeSet<APIProduct>(new APIProductNameComparator());
        List<APIProduct> productList = new ArrayList<APIProduct>();
        Map<String, Object> result = new HashMap<String, Object>();
        int totalLength = 0;
        boolean isMore = false;
        try {
            //for now will use the same config for api products too todo: change this
            String paginationLimit = getAPIManagerConfiguration()
                    .getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);

            // If the Config exists use it to set the pagination limit
            final int maxPaginationLimit;
            if (paginationLimit != null) {
                // The additional 1 added to the maxPaginationLimit is to help us determine if more
                // APIs may exist so that we know that we are unable to determine the actual total
                // API count. We will subtract this 1 later on so that it does not interfere with
                // the logic of the rest of the application
                int pagination = Integer.parseInt(paginationLimit);

                // Because the store jaggery pagination logic is 10 results per a page we need to set pagination
                // limit to at least 11 or the pagination done at this level will conflict with the store pagination
                // leading to some of the APIs not being displayed
                if (pagination < 11) {
                    pagination = 11;
                    log.warn("Value of '" + APIConstants.API_STORE_APIS_PER_PAGE + "' is too low, defaulting to 11");
                }
                maxPaginationLimit = start + pagination + 1;
            }
            // Else if the config is not specified we go with default functionality and load all
            else {
                maxPaginationLimit = Integer.MAX_VALUE;
            }
            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                    .findGovernanceArtifacts(getSearchQuery(searchQuery), registry, APIConstants.API_RXT_MEDIA_TYPE,
                            true);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;

            if (!isFound) {
                result.put("products", productSet);
                result.put("length", 0);
                result.put("isMore", isMore);
                return result;
            }

            // Check to see if we can speculate that there are more APIs to be loaded
            if (maxPaginationLimit == totalLength) {
                isMore = true;  // More APIs exist, cannot determine total API count without incurring perf hit
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }

            int tempLength = 0;
            for (GovernanceArtifact artifact : governanceArtifacts) {
                APIProduct resultAPIProduct = APIUtil.getAPIProduct(artifact, registry);
                if (resultAPIProduct != null) {
                    productList.add(resultAPIProduct);
                }

                // Ensure the APIs returned matches the length, there could be an additional API
                // returned due incrementing the pagination limit when getting from registry
                tempLength++;
                if (tempLength >= totalLength) {
                    break;
                }
            }

            productSet.addAll(productList);
        } catch (RegistryException e) {
            String msg = "Failed to search APIProducts with type";
            throw new APIManagementException(msg, e);
        } finally {
            PaginationContext.destroy();
        }
        result.put("products", productSet);
        result.put("length", totalLength);
        result.put("isMore", isMore);
        return result;
    }

    protected APIProduct getApiProduct(Registry registry, GovernanceArtifact apiArtifact) throws APIManagementException {
        return APIUtil.getAPIProduct(apiArtifact, registry);
    }

    public List<APIProductResource> getResourcesOfAPIProduct(APIProductIdentifier productIdentifier)
            throws APIManagementException {
        return apiMgtDAO.getAPIProductResourceMappings(productIdentifier);
    }

    public ResourceFile getProductIcon(APIProductIdentifier identifier) throws APIManagementException {
        String thumbPath = APIUtil.getProductIconPath(identifier);
        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
        Registry registry;
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                startTenantFlow(tenantDomain);
                isTenantFlowStarted = true;
            }

	        /* If the API provider is a tenant, load tenant registry*/
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = getTenantManager().getTenantId(tenantDomain);
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    registry = getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    registry = this.registry;
                }
            }

            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                return new ResourceFile(res.getContentStream(), res.getMediaType());
            }
        } catch (RegistryException e) {
            String msg = "Error while loading API Product icon of API Product " +  identifier.getName()
                    + ":" + identifier.getVersion() + " from the registry";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while loading API Product icon of API Product " + identifier.getName()
                    + ":" + identifier.getVersion();
            throw new APIManagementException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
        return null;
    }

    @Override
    public String addProductResourceFile(APIProductIdentifier identifier, String resourcePath,
            ResourceFile resourceFile) throws APIManagementException {
        //todo : implement access control checks here and move to userawareAPIProvider
        return addResourceFile(identifier, resourcePath, resourceFile);
    }


    /**
     * Get an api product documentation by artifact Id
     *
     * @param docId                 artifact id of the document
     * @param requestedTenantDomain tenant domain of the registry where the artifact is located
     * @return Document object which represents the artifact id
     * @throws APIManagementException
     */
    public Documentation getProductDocumentation(String docId, String requestedTenantDomain) throws APIManagementException {
        Documentation documentation = null;
        try {
            Registry registryType;
            boolean isTenantMode = (requestedTenantDomain != null);
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(
                    requestedTenantDomain))) {
                int tenantId = getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registryType, APIConstants
                    .DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            APIProductIdentifier productIdentifier = APIUtil.getProductIdentifier(artifact.getPath());
            checkAccessControlPermission(productIdentifier);
            if (null != artifact) {
                documentation = APIUtil.getDocumentation(artifact);
                documentation.setCreatedDate(registryType.get(artifact.getPath()).getCreatedTime());
                Date lastModified = registryType.get(artifact.getPath()).getLastModified();
                if (lastModified != null) {
                    documentation.setLastUpdated(registryType.get(artifact.getPath()).getLastModified());
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        }
        return documentation;
    }

    public APIProduct getAPIProduct(String productPath) throws APIManagementException {
        try {
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                    APIConstants.API_KEY);
            Resource productResource = registry.get(productPath);
            String artifactId = productResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + productPath);
            }
            GenericArtifact productArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPIProduct(productArtifact, registry);

        } catch (RegistryException e) {
            String msg = "Failed to get API Product from : " + productPath;
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public String getAPIDefinitionOfAPIProduct(APIProduct product) throws APIManagementException {
        String resourcePath = APIUtil.getAPIProductOpenAPIDefinitionFilePath(product.getId());

        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                parser.parse(apiDocContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at " + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving OpenAPI v2.0 or v3.0.0 Definition for " + product.getId().getName() + '-'
                            + product.getId().getProviderName(), e);
        } catch (ParseException e) {
            handleException(
                    "Error while parsing OpenAPI v2.0 or v3.0.0 Definition for " + product.getId().getName() + '-'
                    + product.getId().getProviderName() + " in " + resourcePath, e);
        }
        return apiDocContent;

    }

    protected boolean isOauthAppValidationEnabled() {
        String oauthAppValidation = getAPIManagerConfiguration()
                .getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENABLE_PROVISION_APP_VALIDATION);
        if (StringUtils.isNotEmpty(oauthAppValidation)) {
            return Boolean.parseBoolean(oauthAppValidation);
        }
        return true;
    }
}
