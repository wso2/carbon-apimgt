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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.BlockConditionNotFoundException;
import org.wso2.carbon.apimgt.api.PolicyNotFoundException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.Limit;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
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
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {

    protected Log log = LogFactory.getLog(getClass());

    protected Registry registry;
    protected UserRegistry configRegistry;
    protected ApiMgtDAO apiMgtDAO;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected String tenantDomain;
    protected String username;
    private LRUCache<String, GenericArtifactManager> genericArtifactCache = new LRUCache<String, GenericArtifactManager>(
            5);

    // API definitions from swagger v2.0
    protected static final APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

    public AbstractAPIManager() throws APIManagementException {
    }

    public AbstractAPIManager(String username) throws APIManagementException {
        apiMgtDAO = ApiMgtDAO.getInstance();

        try {
            if (username == null) {

                this.registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry();
                this.configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry();

                this.username= CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                ServiceReferenceHolder.setUserRealm((ServiceReferenceHolder.getInstance().getRealmService().getBootstrapRealm()));
            } else {
                String tenantDomainName = MultitenantUtils.getTenantDomain(username);
                String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomainName);
                this.tenantId=tenantId;
                this.tenantDomain=tenantDomainName;
                this.username=tenantUserName;

                APIUtil.loadTenantRegistry(tenantId);

                this.registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                this.configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);

                //load resources for each tenants.
                APIUtil.loadloadTenantAPIRXT( tenantUserName, tenantId);
                APIUtil.loadTenantAPIPolicy( tenantUserName, tenantId);

                //Check whether GatewayType is "Synapse" before attempting to load Custom-Sequences into registry
                APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService().getAPIManagerConfiguration();

                String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

                if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
                    APIUtil.writeDefinedSequencesToTenantRegistry(tenantId);
                }

                ServiceReferenceHolder.setUserRealm((UserRealm) (ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(tenantId)));
            }
            ServiceReferenceHolder.setUserRealm(ServiceReferenceHolder.getInstance().
                    getRegistryService().getConfigSystemRegistry().getUserRealm());
            registerCustomQueries(configRegistry, username);
        } catch (RegistryException e) {
            handleException("Error while obtaining registry objects", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Error while getting user registry for user:"+username, e);
        }

    }

    /**
     * method to register custom registry queries
     * @param registry  Registry instance to use
     * @throws RegistryException n error
     */
    private void registerCustomQueries(UserRegistry registry, String username)
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
                handleException("Error while setting the permissions", e);
            }
        }else if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            int tenantId;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantUserRealm(tenantId).getAuthorizationManager();
                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleException("Error while setting the permissions", e);
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
        if(!registry.resourceExists(resourcesByTag)){
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
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                API api = null;
                try {
                    api = APIUtil.getAPI(artifact);
                } catch (APIManagementException e) {
                    //log and continue since we want to load the rest of the APIs.
                    log.error("Error while loading API " + artifact.getAttribute(APIConstants.API_OVERVIEW_NAME), e);
                }
                if (api != null) {
                    apiSortedList.add(api);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get APIs from the registry", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
    }

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        Registry registry;
        try {
            String apiTenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            int apiTenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(apiTenantDomain);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(apiTenantDomain)) {
                APIUtil.loadTenantRegistry(apiTenantId);
            }

            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) { //cross tenant scenario
                registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                        MultitenantUtils.getTenantAwareUsername(
                                APIUtil.replaceEmailDomainBack(identifier.getProviderName())), apiTenantId);
            } else {
                registry = this.registry;
            }
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);

            API api = APIUtil.getAPIForPublishing(apiArtifact, registry);

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
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }
    }

    /**
     * Get API by registry artifact id
     *
     * @param uuid  Registry artifact id
     * @param requestedTenantDomain tenantDomain for the registry
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    public API getAPIbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (requestedTenantDomain)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(uuid);
            if (apiArtifact != null) {
                return APIUtil.getAPIForPublishing(apiArtifact, registry);
            } else {
                handleResourceNotFoundException(
                        "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist");
                return null;
            }
        } catch (RegistryException e) {
            handleException("Failed to get API", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get API", e);
            return null;
        }
        return null;
    }

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid  Registry artifact id
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    public API getLightweightAPIByUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (requestedTenantDomain)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(uuid);
            if (apiArtifact != null) {
                return APIUtil.getAPIInformation(apiArtifact, registry);
            } else {
                handleResourceNotFoundException(
                        "Failed to get API. API artifact corresponding to artifactId " + uuid + " does not exist");
            }
        } catch (RegistryException e) {
            handleException("Failed to get API with uuid " + uuid, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get tenant Id while getting API with uuid " + uuid, e);
        }
        return null;
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
            String tenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(identifier.getProviderName()));

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            tenantFlowStarted = true;

            Registry registry = getRegistry(identifier, apiPath);
            if (registry != null) {
                Resource apiResource = registry.get(apiPath);
                String artifactId = apiResource.getUUID();
                if (artifactId == null) {
                    throw new APIManagementException("artifact id is null for : " + apiPath);
                }
                GenericArtifactManager artifactManager = getGenericArtifactManager(identifier, registry);
                GovernanceArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
                return APIUtil.getAPIInformation(apiArtifact, registry);
            } else {
                handleException("Failed to get registry from api identifier: " + identifier);
                return null;
            }
        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private GenericArtifactManager getGenericArtifactManager(APIIdentifier identifier, Registry registry)
            throws APIManagementException {

        String tenantDomain = MultitenantUtils
                .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
        GenericArtifactManager manager = genericArtifactCache.get(tenantDomain);
        if (manager != null) {
            return manager;
        }
        manager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
        genericArtifactCache.put(tenantDomain, manager);
        return manager;
    }

    private Registry getRegistry(APIIdentifier identifier, String apiPath)
            throws APIManagementException {
        Registry passRegistry;
        try {
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(id);
                passRegistry = ServiceReferenceHolder.getInstance().getRegistryService()
                        .getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    // explicitly load the tenant's registry
                    APIUtil.loadTenantRegistry(MultitenantConstants.SUPER_TENANT_ID);
                    passRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                            identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    passRegistry = this.registry;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get API from registry on path of : " + apiPath, e);
            return null;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get API from registry on path of : " + apiPath, e);
            return null;
        }
        return passRegistry;
    }

    public API getAPI(String apiPath) throws APIManagementException {
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }
    }

    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                      identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                      identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            handleException("Failed to check availability of api :" + path, e);
            return false;
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
            handleException("Failed to get versions for API: " + apiName, e);
        }
        return versionSet;
    }

    /**
     * Returns the swagger 2.0 definition of the given API
     *
     * @param apiId id of the APIIdentifier
     * @return An String containing the swagger 2.0 definition
     * @throws APIManagementException
     */
    @Override
    public String getSwagger20Definition(APIIdentifier apiId) throws APIManagementException {
        String apiTenantDomain = MultitenantUtils.getTenantDomain(
                APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
        String swaggerDoc = null;
        try {
            Registry registryType;
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if (this.tenantDomain == null || isTenantDomainNotMatching(apiTenantDomain)) {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                        apiTenantDomain);
                registryType = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            swaggerDoc = definitionFromSwagger20.getAPIDefinition(apiId, registryType);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get swagger documentation of API : " + apiId, e);
        } catch (RegistryException e) {
            handleException("Failed to get swagger documentation of API : " + apiId, e);
        }
        return swaggerDoc;
    }

    public String addResourceFile(String resourcePath, ResourceFile resourceFile) throws APIManagementException {
        try {
            Resource thumb = registry.newResource();
            thumb.setContentStream(resourceFile.getContent());
            thumb.setMediaType(resourceFile.getContentType());
            registry.put(resourcePath, thumb);
            if(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)){
            return RegistryConstants.PATH_SEPARATOR + "registry"
                   + RegistryConstants.PATH_SEPARATOR + "resource"
                   + RegistryConstants.PATH_SEPARATOR + "_system"
                   + RegistryConstants.PATH_SEPARATOR + "governance"
                   + resourcePath;
            }
            else{
                return "/t/"+tenantDomain+ RegistryConstants.PATH_SEPARATOR + "registry"
                        + RegistryConstants.PATH_SEPARATOR + "resource"
                        + RegistryConstants.PATH_SEPARATOR + "_system"
                        + RegistryConstants.PATH_SEPARATOR + "governance"
                        + resourcePath;
            }
        } catch (RegistryException e) {
            handleException("Error while adding the resource to the registry", e);
        }
        return null;
    }

    /**
     * Checks whether the given document already exists for the given api
     *
     * @param identifier API Identifier
     * @param docName Name of the document
     * @return true if document already exists for the given api
     * @throws APIManagementException if failed to check existence of the documentation
     */
    public boolean isDocumentationExist(APIIdentifier identifier, String docName) throws APIManagementException {
        String docPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                         identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                         identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                         identifier.getVersion() + RegistryConstants.PATH_SEPARATOR +
                         APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR + docName;
        try {
            return registry.resourceExists(docPath);
        } catch (RegistryException e) {
            handleException("Failed to check existence of the document :" + docPath, e);
        }
        return false;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath = APIUtil.getAPIPath(apiId);
        try {
        	Association[] docAssociations = registry.getAssociations(apiResourcePath,
                                                                     APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();

                Resource docResource = registry.get(docPath);
                GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                    APIConstants.DOCUMENTATION_KEY);
                GenericArtifact docArtifact = artifactManager.getGenericArtifact(docResource.getUUID());
                Documentation doc = APIUtil.getDocumentation(docArtifact);
                Date contentLastModifiedDate;
                Date docLastModifiedDate = docResource.getLastModified();
                if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {
                    String contentPath = APIUtil.getAPIDocContentPath(apiId, doc.getName());
                    contentLastModifiedDate = registry.get(contentPath).getLastModified();
                    doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                                        contentLastModifiedDate : docLastModifiedDate));
                } else  {
                    doc.setLastUpdated(docLastModifiedDate);
                }
                documentationList.add(doc);
            }

        } catch (RegistryException e) {
            handleException("Failed to get documentations for api " + apiId.getApiName(), e);
        }
        return documentationList;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId,String loggedUsername) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath = APIUtil.getAPIPath(apiId);
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            Registry registryType;
            /* If the API provider is a tenant, load tenant registry*/
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                registryType = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
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
                }catch (RegistryException e){
                    handleException("Failed to get documentations for api " + apiId.getApiName(), e);
                }
                if (docResource != null) {
                    GenericArtifactManager artifactManager = new GenericArtifactManager(registryType,
                                                                                        APIConstants.DOCUMENTATION_KEY);
                    GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                            docResource.getUUID());
                    Documentation doc = APIUtil.getDocumentation(docArtifact, apiId.getProviderName());
                    Date contentLastModifiedDate;
                    Date docLastModifiedDate = docResource.getLastModified();
                    if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {
                        String contentPath = APIUtil.getAPIDocContentPath(apiId, doc.getName());
                        try    {
                            contentLastModifiedDate = registryType.get(contentPath).getLastModified();
                            doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                                                contentLastModifiedDate : docLastModifiedDate));
                        } catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
                            //do nothing. Permission not allowed to access the doc.
                        }

                    } else  {
                        doc.setLastUpdated(docLastModifiedDate);
                    }
                    documentationList.add(doc);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get documentations for api " + apiId.getApiName(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get documentations for api " + apiId.getApiName(), e);
        }
        return documentationList;
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
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                            APIConstants.DOCUMENTATION_KEY);
        try {
            Resource docResource = registry.get(docPath);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docResource.getUUID());
            documentation = APIUtil.getDocumentation(artifact);
        } catch (RegistryException e) {
            handleException("Failed to get documentation details", e);
        }
        return documentation;
    }

    /**
     * Get a documentation by artifact Id
     * 
     * @param docId artifact id of the document
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
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(requestedTenantDomain);
                registryType = ServiceReferenceHolder.getInstance().
                        getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registryType,
                    APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            if (null != artifact) {
                documentation = APIUtil.getDocumentation(artifact);
                documentation.setCreatedDate(registryType.get(artifact.getPath()).getCreatedTime());
                Date lastModified = registryType.get(artifact.getPath()).getLastModified();
                if (lastModified != null) {
                    documentation.setLastUpdated(registryType.get(artifact.getPath()).getLastModified());
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get documentation details", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get documentation details", e);
        }
        return documentation;
    }

    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        String contentPath = APIUtil.getAPIDocPath(identifier) +
                             APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +
                             documentationName;
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
        Registry registry;

        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;

                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

	        /* If the API provider is a tenant, load tenant registry*/
	        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
	            int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
	            registry = ServiceReferenceHolder.getInstance().
	                    getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
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
                         + documentationName + " of API: "+identifier.getApiName();
            handleException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
        	handleException("Failed to get ddocument content found for documentation: "
        				 + documentationName + " of API: "+identifier.getApiName(), e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return null;
    }

    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        return apiMgtDAO.getSubscriberById(accessToken);
    }

    public boolean isContextExist(String context) throws APIManagementException {
        // Since we don't have tenant in the APIM table, we do the filtering using this hack
        if(context!=null && context.startsWith("/t/"))
            context = context.replace("/t/" + MultitenantUtils.getTenantDomainFromUrl(context),""); //removing prefix
    	if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            context = "/t/" + tenantDomain + context;
        }
        return apiMgtDAO.isContextExist(context);
    }
    
    public boolean isScopeKeyExist(String scopeKey, int tenantid) throws APIManagementException {
        return apiMgtDAO.isScopeKeyExist(scopeKey, tenantid);
    }

    public boolean isScopeKeyAssigned(APIIdentifier identifier, String scopeKey, int tenantid)
            throws APIManagementException {
        return apiMgtDAO.isScopeKeyAssigned(identifier, scopeKey, tenantid);
    }      


    public boolean isApiNameExist(String apiName) throws APIManagementException {
        String tenantName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameExist(apiName, tenantName);
    }

    public void addSubscriber(String username, String groupingId)
            throws APIManagementException {

        Subscriber subscriber = new Subscriber(username);
        subscriber.setSubscribedDate(new Date());
        //TODO : need to set the proper email
        subscriber.setEmail("");
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(MultitenantUtils.getTenantDomain(username));
            subscriber.setTenantId(tenantId);
            apiMgtDAO.addSubscriber(subscriber, groupingId);
            //Add a default application once subscriber is added
            addDefaultApplicationForSubscriber(subscriber);
        } catch (APIManagementException e) {
            handleException("Error while adding the subscriber " + subscriber.getName(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Error while adding the subscriber " + subscriber.getName(), e);
        }
    }

    /**
     * Add default application on the first time a subscriber is added to the database
     * @param subscriber Subscriber
     *
     * @throws APIManagementException if an error occurs while adding default application
     */
    private void addDefaultApplicationForSubscriber (Subscriber subscriber) throws APIManagementException {
        Application defaultApp = new Application(APIConstants.DEFAULT_APPLICATION_NAME, subscriber);
        if (APIUtil.isEnabledUnlimitedTier()) {
            defaultApp.setTier(APIConstants.UNLIMITED_TIER);
        } else {
            Map<String, Tier> throttlingTiers = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE,
                                                                 MultitenantUtils.getTenantDomain(subscriber.getName()));
            Set<Tier> tierValueList = new HashSet<Tier>(throttlingTiers.values());
            List<Tier> sortedTierList = APIUtil.sortTiers(tierValueList);
            defaultApp.setTier(sortedTierList.get(0).getName());
        }
        //application will not be shared within the group
        defaultApp.setGroupId("");
        apiMgtDAO.addApplication(defaultApp, subscriber.getName());
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

        String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        try {
            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                return new ResourceFile(res.getContentStream(), res.getMediaType());
            }
        } catch (RegistryException e) {
            handleException("Error while loading API icon from the registry", e);
        }
        return null;
    }

    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Set<SubscribedAPI> subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, null);
        boolean isTenantFlowStarted = false;
        try {
	        if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
	        	isTenantFlowStarted = true;
	            PrivilegedCarbonContext.startTenantFlow();
	            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
	        }
	        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
	            String apiPath = APIUtil.getAPIPath(subscribedAPI.getApiId());
	            Resource resource;
	            try {
	                resource = registry.get(apiPath);
	                GenericArtifactManager artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
	                GenericArtifact artifact = artifactManager.getGenericArtifact(
	                        resource.getUUID());
	                API api = APIUtil.getAPI(artifact, registry);
	                if (api != null) {
	                    apiSortedSet.add(api);
	                }
	            } catch (RegistryException e) {
	                handleException("Failed to get APIs for subscriber: " + subscriber.getName(), e);
	            }
	        }
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
        return apiSortedSet;
    }

    /**
     * Returns the corresponding application given the uuid
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    public Application getApplicationByUUID(String uuid) throws APIManagementException {
        return apiMgtDAO.getApplicationByUUID(uuid);
    }

    /** returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Subscription
     * @return SubscribedAPI object which is related to the UUID
     * @throws APIManagementException
     */
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        return apiMgtDAO.getSubscriptionByUUID(uuid);
    }

    protected final void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

    protected final void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    protected final void handleResourceAlreadyExistsException(String msg) throws APIMgtResourceAlreadyExistsException {
        log.error(msg);
        throw new APIMgtResourceAlreadyExistsException(msg);
    }

    protected final void handleResourceNotFoundException(String msg) throws APIMgtResourceNotFoundException {
        log.error(msg);
        throw new APIMgtResourceNotFoundException(msg);
    }

    protected final void handlePolicyNotFoundException(String msg) throws PolicyNotFoundException {
        log.error(msg);
        throw new PolicyNotFoundException(msg);
    }

    protected final void handleBlockConditionNotFoundException(String msg) throws BlockConditionNotFoundException {
        log.error(msg);
        throw new BlockConditionNotFoundException(msg);
    }

    public boolean isApplicationTokenExists(String accessToken) throws APIManagementException {
        return apiMgtDAO.isAccessTokenExists(accessToken);
    }

    public boolean isApplicationTokenRevoked(String accessToken) throws APIManagementException {
        return apiMgtDAO.isAccessTokenRevoked(accessToken);
    }


    public APIKey getAccessTokenData(String accessToken) throws APIManagementException {
        return apiMgtDAO.getAccessTokenData(accessToken);
    }

    public Map<Integer, APIKey> searchAccessToken(String searchType, String searchTerm, String loggedInUser)
            throws APIManagementException {
        if (searchType == null) {
            return apiMgtDAO.getAccessTokens(searchTerm);
        } else {
            if ("User".equalsIgnoreCase(searchType)) {
                return apiMgtDAO.getAccessTokensByUser(searchTerm, loggedInUser);
            } else if ("Before".equalsIgnoreCase(searchType)) {
                return apiMgtDAO.getAccessTokensByDate(searchTerm, false, loggedInUser);
            }  else if ("After".equalsIgnoreCase(searchType)) {
                return apiMgtDAO.getAccessTokensByDate(searchTerm, true, loggedInUser);
            } else {
                return apiMgtDAO.getAccessTokens(searchTerm);
            }
        }

    }
    public Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException{
        return apiMgtDAO.getAPIByAccessToken(accessToken);
    }
    public API getAPI(APIIdentifier identifier,APIIdentifier oldIdentifier, String oldContext) throws
                                                                                          APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact, registry,oldIdentifier, oldContext);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
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
                    PrivilegedCarbonContext.startTenantFlow();
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }
                tierMap = APIUtil.getAllTiers(tenantId);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
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
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
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
                PrivilegedCarbonContext.endTenantFlow();
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

        Map<String, Tier> tierMap;
        if(!APIUtil.isAdvanceThrottlingEnabled()) {
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                tierMap = APIUtil.getTiers();
            } else {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                tierMap = APIUtil.getTiers(tenantId);
                PrivilegedCarbonContext.endTenantFlow();
            }
            tiers.addAll(tierMap.values());
        } else {
            tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
            tiers.addAll(tierMap.values());
        }

        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers(String tenantDomain) throws APIManagementException {

        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Map<String, Tier> tierMap;
        if(!APIUtil.isAdvanceThrottlingEnabled()) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            int requestedTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (requestedTenantId == MultitenantConstants.SUPER_TENANT_ID
                    || requestedTenantId == MultitenantConstants.INVALID_TENANT_ID) {
                tierMap = APIUtil.getTiers();
            } else {
                tierMap = APIUtil.getTiers(requestedTenantId);
            }
            tiers.addAll(tierMap.values());
            PrivilegedCarbonContext.endTenantFlow();
        } else {
            tierMap = APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
            tiers.addAll(tierMap.values());
        }
        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @param tierType     type of the tiers (api,resource ot application)
     * @param username current logged user
     * @return Set<Tier> return list of tier names
     * @throws APIManagementException APIManagementException if failed to get the predefined tiers
     */
    public Set<Tier> getTiers(int tierType, String username) throws APIManagementException {
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        Map<String, Tier> tierMap;
        if(!APIUtil.isAdvanceThrottlingEnabled()) {
            tierMap = APIUtil.getTiers(tierType, tenantDomain);
            tiers.addAll(tierMap.values());
        } else {
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
        }

        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Map<String, String>
     */
    public Map<String,String> getTenantDomainMappings(String tenantDomain, String apiType) throws APIManagementException {
        return APIUtil.getDomainMappings(tenantDomain, apiType);
    }


    public boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException{

        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            if (contextTemplate != null && contextTemplate.startsWith("/t/")) {
                contextTemplate =
                    contextTemplate.replace("/t/" + MultitenantUtils.getTenantDomainFromUrl(contextTemplate), "");
            }
            contextTemplate = "/t/" + tenantDomain + contextTemplate;
        }
        return apiMgtDAO.isDuplicateContextTemplate(contextTemplate);
    }
    
    public Policy[] getPolicies(String username, String level) throws APIManagementException {
        Policy[] policies = null;      

        int tenantID = APIUtil.getTenantId(username);

        if(PolicyConstants.POLICY_LEVEL_API.equals(level)){
            policies = apiMgtDAO.getAPIPolicies(tenantID);
        } else if(PolicyConstants.POLICY_LEVEL_APP.equals(level)){
            policies = apiMgtDAO.getApplicationPolicies(tenantID);
        } else if(PolicyConstants.POLICY_LEVEL_SUB.equals(level)){
            policies = apiMgtDAO.getSubscriptionPolicies(tenantID);
        } else if(PolicyConstants.POLICY_LEVEL_GLOBAL.equals(level)){
            policies = apiMgtDAO.getGlobalPolicies(tenantID);
        }
        return policies;
    }
    
    @Override
    public Map<String,Object> searchPaginatedAPIs(String searchQuery, String requestedTenantDomain,
                                                  int start,int end, boolean isLazyLoad) throws APIManagementException {
        Map<String,Object> result = new HashMap<String,Object>();
        boolean isTenantFlowStarted = false;
        
        try {
            boolean isTenantMode=(requestedTenantDomain != null);
            if (isTenantMode && !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
            } else {
                requestedTenantDomain = org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);

            }

            Registry userRegistry;
            int tenantIDLocal = 0;
            String userNameLocal = this.username;
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant store anonymous mode
                tenantIDLocal = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(requestedTenantDomain);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantIDLocal);
                userNameLocal = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
            } else {
                userRegistry = this.registry;
                tenantIDLocal = tenantId;
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            if (searchQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
                Map<Documentation, API> apiDocMap =
                        APIUtil.searchAPIsByDoc(userRegistry, tenantIDLocal, userNameLocal, searchQuery.split("=")[1],
                                                APIConstants.STORE_CLIENT);
                result.put("apis", apiDocMap);
                /*Pagination for Document search results is not supported yet, hence length is sent as end-start*/
                if (apiDocMap.isEmpty()) {
                    result.put("length", 0);
                } else {
                    result.put("length", end-start);
                }
            }   else if (searchQuery.startsWith(APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX)) {
                result = APIUtil.searchAPIsByURLPattern(userRegistry, searchQuery.split("=")[1], start,end); 
            } else {
                result = searchPaginatedAPIs(userRegistry, searchQuery, start, end, isLazyLoad);
            }

        } catch (Exception e) {
            handleException("Failed to Search APIs", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }
    
    /**
     * Returns API Search result based on the provided query. This search method supports '&' based concatenate 
     * search in multiple fields. 
     * @param registry
     * @param searchQuery. Ex: provider=*admin*&version=*1*
     * @return API result
     * @throws APIManagementException
     */

    public Map<String,Object> searchPaginatedAPIs(Registry registry, String searchQuery, int start, int end, 
                                                  boolean limitAttributes) throws APIManagementException {
        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        List<API> apiList = new ArrayList<API>();
        Map<String,Object> result=new HashMap<String, Object>();
        int totalLength=0;
        boolean isMore = false;        

        try {
            String paginationLimit = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
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
            
            
            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(searchQuery, 
                                                                            registry, APIConstants.API_RXT_MEDIA_TYPE);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;
            if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                if (searchQuery.contains(APIConstants.API_OVERVIEW_PROVIDER)) {
                    searchQuery = searchQuery.replaceAll(APIConstants.API_OVERVIEW_PROVIDER, APIConstants.API_OVERVIEW_OWNER);
                    governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(searchQuery, 
                                                                               registry, APIConstants.API_RXT_MEDIA_TYPE);
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

           int tempLength =0;
           for (GovernanceArtifact artifact : governanceArtifacts) {
               API resultAPI;
               if (limitAttributes) {
                   resultAPI = APIUtil.getAPI(artifact);
               } else {
                   resultAPI = APIUtil.getAPI(artifact, registry);
               }
               if (resultAPI != null) {
                   apiList.add(resultAPI);
               }
               String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

               // Ensure the APIs returned matches the length, there could be an additional API
               // returned due incrementing the pagination limit when getting from registry
               tempLength++;
               if (tempLength >= totalLength){
                   break;
               }
           }

           apiSet.addAll(apiList);
        } catch (RegistryException e) {
            handleException("Failed to search APIs with type", e);
        } finally {
            PaginationContext.destroy();
        }
        result.put("apis",apiSet);
        result.put("length",totalLength);
        result.put("isMore", isMore);
        return result;
    }

    public Map<String, String> getSwaggerDefinitionTimeStamps(APIIdentifier apiIdentifier) throws APIManagementException {
        String apiTenantDomain = MultitenantUtils.getTenantDomain(
                APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
        try {
            Registry registryType;
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if (this.tenantDomain == null || isTenantDomainNotMatching(apiTenantDomain)) {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
                        apiTenantDomain);
                registryType = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            return definitionFromSwagger20.getAPISwaggerDefinitionTimeStamps(apiIdentifier,registryType);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return null;
    }

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
            handleException("Error while loading API icon from the registry", e);
        }
        return null;

    }
}
