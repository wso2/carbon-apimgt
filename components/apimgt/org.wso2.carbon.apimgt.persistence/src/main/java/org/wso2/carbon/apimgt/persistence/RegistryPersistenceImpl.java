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

package org.wso2.carbon.apimgt.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.RegistryConfigLoader;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.service.ContentBasedSearchService;
import org.wso2.carbon.registry.indexing.service.SearchResultsBean;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;
import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.attachLabelsToAPIArtifact;
import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.updateWSDLUriInAPIArtifact;

public class RegistryPersistenceImpl implements APIPersistence {
    private static final Log log = LogFactory.getLog(RegistryPersistenceImpl.class);
    private static APIPersistence instance;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected Registry registry;
    protected String tenantDomain;
    protected UserRegistry configRegistry;
    protected String username;
    protected Organization organization;
    private RegistryService registryService;
    private GenericArtifactManager apiGenericArtifactManager;

    public RegistryPersistenceImpl(String username) {
        //super(username);
        this.registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            // is it ok to reuse artifactManager object TODO : resolve this concern
            // this.registry = getRegistryService().getGovernanceUserRegistry();


            if (username == null) {

                this.registry = getRegistryService().getGovernanceUserRegistry();
                this.configRegistry = getRegistryService().getConfigSystemRegistry();

                this.username = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                ServiceReferenceHolder.setUserRealm((ServiceReferenceHolder.getInstance().getRealmService()
                                                .getBootstrapRealm()));
                this.apiGenericArtifactManager = RegistryPersistenceUtil.getArtifactManager(this.registry,
                                                APIConstants.API_KEY);
            } else {
                String tenantDomainName = MultitenantUtils.getTenantDomain(username);
                String tenantUserName = getTenantAwareUsername(username);
                int tenantId = getTenantManager().getTenantId(tenantDomainName);
                this.tenantId = tenantId;
                this.tenantDomain = tenantDomainName;
                this.organization = new Organization(tenantDomain, tenantId, "registry");
                this.username = tenantUserName;

                loadTenantRegistry(tenantId);

                this.registry = getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                this.configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);

                //load resources for each tenants.
                loadloadTenantAPIRXT(tenantUserName, tenantId);
                loadTenantAPIPolicy(tenantUserName, tenantId);

                // ===== Below  calls should be called at impls module
                //                //Check whether GatewayType is "Synapse" before attempting to load Custom-Sequences into registry
                //                APIManagerConfiguration configuration = getAPIManagerConfiguration();
                //
                //                String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
                //
                //                if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
                //                    APIUtil.writeDefinedSequencesToTenantRegistry(tenantId);
                //                }

                ServiceReferenceHolder.setUserRealm((UserRealm) (ServiceReferenceHolder.getInstance().
                                                getRealmService().getTenantUserRealm(tenantId)));
                this.apiGenericArtifactManager = RegistryPersistenceUtil.getArtifactManager(this.registry,
                                                APIConstants.API_KEY);
            }
        } catch (RegistryException e) {

        } catch (UserStoreException e) {
            e.printStackTrace();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
    }

    protected String getTenantAwareUsername(String username) {
        return MultitenantUtils.getTenantAwareUsername(username);
    }

    protected void loadTenantRegistry(int apiTenantId) throws RegistryException {
        TenantRegistryLoader tenantRegistryLoader = PersistenceManagerComponent.getTenantRegistryLoader();
        ServiceReferenceHolder.getInstance().getIndexLoaderService().loadTenantIndex(apiTenantId);
        tenantRegistryLoader.loadTenantRegistry(apiTenantId);
    }

    protected TenantManager getTenantManager() {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }

    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    /**
     * Load the  API RXT to the registry for tenants
     *
     * @param tenant
     * @param tenantID
     * @throws APIManagementException
     */

    public static void loadloadTenantAPIRXT(String tenant, int tenantID) throws APIManagementException {

        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry registry = null;
        try {

            registry = registryService.getGovernanceSystemRegistry(tenantID);
        } catch (RegistryException e) {
            throw new APIManagementException("Error when create registry instance ", e);
        }

        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                                        + File.separator + "rxts";
        File file = new File(rxtDir);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                // if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);

        if (rxtFilePaths == null) {
            throw new APIManagementException("rxt files not found in directory " + rxtDir);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH + RegistryConstants.PATH_SEPARATOR + rxtPath;

            //This is  "registry" is a governance registry instance, therefore calculate the relative path to governance.
            String govRelativePath = RegistryUtils.getRelativePathToOriginal(resourcePath,
                                            getMountedPath(RegistryContext.getBaseInstance(),
                                                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH));
            try {
                // calculate resource path
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(
                                                ServiceReferenceHolder.getUserRealm());
                resourcePath = authorizationManager.computePathOnMount(resourcePath);

                org.wso2.carbon.user.api.AuthorizationManager authManager = ServiceReferenceHolder.getInstance()
                                                .getRealmService().
                                                                                getTenantUserRealm(tenantID)
                                                .getAuthorizationManager();

                if (registry.resourceExists(govRelativePath)) {
                    // set anonymous user permission to RXTs
                    authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    continue;
                }

                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes(Charset.defaultCharset()));
                resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
                registry.put(govRelativePath, resource);

                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);

            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new APIManagementException("Error while adding role permissions to API", e);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new APIManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new APIManagementException(msg, e);
            }
        }

    }

    public static void loadTenantAPIPolicy(String tenant, int tenantID) throws APIManagementException {

        String tierBasePath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                                        + File.separator + "default-tiers" + File.separator;

        String apiTierFilePath = tierBasePath + APIConstants.DEFAULT_API_TIER_FILE_NAME;
        String appTierFilePath = tierBasePath + APIConstants.DEFAULT_APP_TIER_FILE_NAME;
        String resTierFilePath = tierBasePath + APIConstants.DEFAULT_RES_TIER_FILE_NAME;

        loadTenantAPIPolicy(tenantID, APIConstants.API_TIER_LOCATION, apiTierFilePath);
        loadTenantAPIPolicy(tenantID, APIConstants.APP_TIER_LOCATION, appTierFilePath);
        loadTenantAPIPolicy(tenantID, APIConstants.RES_TIER_LOCATION, resTierFilePath);
    }

    /**
     * Load the throttling policy  to the registry for tenants
     *
     * @param tenantID
     * @param location
     * @param fileName
     * @throws APIManagementException
     */
    private static void loadTenantAPIPolicy(int tenantID, String location, String fileName)
                                    throws APIManagementException {

        InputStream inputStream = null;

        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();

            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(location)) {
                if (log.isDebugEnabled()) {
                    log.debug("Tier policies already uploaded to the tenant's registry space");
                }
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding API tier policies to the tenant's registry");
            }
            File defaultTiers = new File(fileName);
            if (!defaultTiers.exists()) {
                log.info("Default tier policies not found in : " + fileName);
                return;
            }
            inputStream = FileUtils.openInputStream(defaultTiers);
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            govRegistry.put(location, resource);

        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving policy information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading policy file content", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error when closing input stream", e);
                }
            }
        }
    }

    /**
     * This method will return mounted path of the path if the path
     * is mounted. Else path will be returned.
     *
     * @param registryContext Registry Context instance which holds path mappings
     * @param path            default path of the registry
     * @return mounted path or path
     */
    public static String getMountedPath(RegistryContext registryContext, String path) {

        if (registryContext != null && path != null) {
            List<Mount> mounts = registryContext.getMounts();
            if (mounts != null) {
                for (Mount mount : mounts) {
                    if (path.equals(mount.getPath())) {
                        return mount.getTargetPath();
                    }
                }
            }
        }
        return path;
    }

    //    @Override public API getAPI(String apiUUID) {
    //        return null;
    //    }

    /* Setting WorkflowStatus property to the API object returned by this methods will be useful in some occations. Set
     it when required on the value returned from this method */
    @Override
    public API getAPIbyId(String apiUuid, String requestedTenantDomain) throws APIManagementException {
        boolean tenantFlowStarted = false;
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                            .equals(requestedTenantDomain)) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                registry = getRegistryService().getGovernanceSystemRegistry(tenantId);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                                .equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }

            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                                            APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiUuid);
            if (apiArtifact != null) {
                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);

                /* Setting this  property will be useful in some occations. Set it whe required on the value returned
                  from this method API getAPIbyId(..)
                APIIdentifier apiIdentifier = api.getId();
                WorkflowDTO workflowDTO = APIUtil.getAPIWorkflowStatus(apiIdentifier, WF_TYPE_AM_API_STATE);
                if (workflowDTO != null) {
                    WorkflowStatus status = workflowDTO.getStatus();
                    api.setWorkflowStatus(status.toString());
                }*/
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + apiUuid + " does not exist";
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
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }

    @Override public API updateApi(API api) {
        return null;
    }

    @Override public void updateWsdlFromUrl(String apiId, String wsdlUrl) {

    }

    @Override public void updateWsdlFromUrl(API api) {

    }

    @Override public void updateDocVisibility(String apiId, String visibility, String visibleRoles,
                                    Documentation documentation) {

    }

    @Override public void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile) {

    }

    @Override public void addLifeCycle(API api) {

    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                    int end, boolean limitAttributes) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIs(String paginatedSearchQuery, Organization requestedOrg, int start,
                                    int end, boolean isLazyLoad, boolean isPublisherListing)
                                    throws APIManagementException {

        String requestedTenantDomain = requestedOrg.getName();
        // >> Shifted from org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ApisApiServiceImpl.apisGet
        String searchQuery = RegistryPersistenceUtil.constructApisGetQuery(paginatedSearchQuery);

        //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
//        if (searchQuery.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=")) {
//            searchQuery = searchQuery.replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=",
//                                            APIConstants.NAME_TYPE_PREFIX + "=");
//        }
        // Shifted from ApisApi class <<<

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
            if (searchQuery.startsWith(APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX) || searchQuery
                                            .startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
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
                                                            + " appending "
                                                            + APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX
                                                            + " as prefix");
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
            if (isTenantMode && !org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                            .equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
            } else {
                requestedTenantDomain = org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);

            }

            Registry userRegistry;
            int tenantIDLocal = 0;
            String userNameLocal = this.username;
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(
                                            requestedTenantDomain))) {//Tenant store anonymous mode
                tenantIDLocal = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.loadTenantRegistry(tenantIDLocal);
                userRegistry = getRegistryService()
                                                .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                                                                                tenantIDLocal);
                userNameLocal = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                if (!requestedTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    PersistenceUtil.loadTenantConfigBlockingMode(requestedTenantDomain);
                }
            } else {
                userRegistry = this.registry;
                tenantIDLocal = tenantId;
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            if (subQuery != null && subQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
                Map<Documentation, API> apiDocMap = searchAPIDoc(userRegistry, tenantIDLocal, userNameLocal,
                                                subQuery.split("=")[1]);
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
                RegistryPersistenceUtil.endTenantFlow();
            }
        }

        return result;

        //return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIsByContent(Organization requestedOrg, String searchQuery,
                                    int start, int end, boolean limitAttributes) {
        return null;
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
            String paginationLimit = null; /*getAPIManagerConfiguration()
                                            .getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);*/

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
                                            .findGovernanceArtifacts(PersistenceUtil.getSearchQuery(searchQuery,
                                                                            username),
                                                                            registry,
                                                                            APIConstants.API_RXT_MEDIA_TYPE,
                                                                            true);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;
            if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                if (searchQuery.contains(APIConstants.API_OVERVIEW_PROVIDER)) {
                    searchQuery = searchQuery.replaceAll(APIConstants.API_OVERVIEW_PROVIDER, APIConstants.API_OVERVIEW_OWNER);
                    governanceArtifacts =
                                                    GovernanceUtils.findGovernanceArtifacts(PersistenceUtil.getSearchQuery(searchQuery, username),
                                                    registry,
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
                    APIProduct resultAPI = RegistryPersistenceUtil.getAPIProduct(artifact, registry);

                    if (resultAPI != null) {
                        apiList.add(resultAPI);
                    }
                } else {
                    API resultAPI;
                    if (limitAttributes) {
                        resultAPI = RegistryPersistenceUtil.getAPI(artifact);
                    } else {
                        if(reducedPublisherAPIInfo) {
                            resultAPI = RegistryPersistenceUtil.getReducedPublisherAPIForListing(artifact, registry);
                        } else {
                            resultAPI = RegistryPersistenceUtil.getAPI(artifact, registry);
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
//            if (!apiIdsString.isEmpty() && !reducedPublisherAPIInfo) {
//
//                Map<String, Set<Scope>> apiScopeSet = getScopesForAPIS(apiIdsString);
//                if (apiScopeSet.size() > 0) {
//                    for (int i = 0; i < apiCount; i++) {
//                        Object api = apiList.get(i);
//                        String apiId = "";
//                        if (api instanceof API) {
//                            apiId = ((API) api).getId().getApplicationId();
//                        } else if (api instanceof APIProduct) {
//                            apiId = ((APIProduct) api).getId().getApplicationId();
//                        }
//                        if (apiId != null && apiId != "") {
//                            Set<Scope> scopes = apiScopeSet.get(apiId);
//                            if (api instanceof API) {
//                                ((API) api).setScopes(scopes);
//                            } else if (api instanceof APIProduct) {
//                                ((APIProduct) api).setScopes(scopes);
//                            }
//                        }
//                    }
//                }
//            }
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
        return searchAPIsByURLPatternUtilMeth(registry, searchTerm, start, end);
    }

    public Map<String, Object> searchAPIsByURLPatternUtilMeth(Registry registry, String searchTerm, int start, int end)
                                    throws APIManagementException {

        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        List<API> apiList = new ArrayList<API>();
        final String searchValue = searchTerm.trim();
        Map<String, Object> result = new HashMap<String, Object>();
        int totalLength = 0;
        String criteria;
        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        GenericArtifact[] genericArtifacts = new GenericArtifact[0];
        GenericArtifactManager artifactManager = null;
        try {
            artifactManager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when searching APIs by URL pattern " + searchTerm;
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            PaginationContext.init(0, 10000, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);
            if (artifactManager != null) {
                for (int i = 0; i < 20; i++) { //This need to fix in future.We don't have a way to get max value of
                    // "url_template" entry stores in registry,unless we search in each API
                    criteria = APIConstants.API_URI_PATTERN + i;
                    listMap.put(criteria, new ArrayList<String>() {
                        {
                            add(searchValue);
                        }
                    });
                    genericArtifacts = (GenericArtifact[]) ArrayUtils.addAll(genericArtifacts, artifactManager
                                                    .findGenericArtifacts(listMap));
                }
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    result.put("apis", apiSet);
                    result.put("length", 0);
                    return result;
                }
                totalLength = genericArtifacts.length;
                StringBuilder apiNames = new StringBuilder();
                for (GenericArtifact artifact : genericArtifacts) {
                    if (artifact == null) {
                        log.error("Failed to retrieve an artifact when searching APIs by URL pattern : " + searchTerm +
                                                        " , continuing with next artifact.");
                        continue;
                    }
                    if (apiNames.indexOf(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME)) < 0) {
                        String status = RegistryPersistenceUtil.getLcStateFromArtifact(artifact);
                        if (PersistenceUtil.isAllowDisplayAPIsWithMultipleStatus()) {
                            if (APIConstants.PUBLISHED.equals(status) || APIConstants.DEPRECATED.equals(status)) {
                                API api = RegistryPersistenceUtil.getAPI(artifact, registry);
                                if (api != null) {
                                    apiList.add(api);
                                    apiNames.append(api.getId().getApiName());
                                }
                            }
                        } else {
                            if (APIConstants.PUBLISHED.equals(status)) {
                                API api = RegistryPersistenceUtil.getAPI(artifact, registry);
                                if (api != null) {
                                    apiList.add(api);
                                    apiNames.append(api.getId().getApiName());
                                }
                            }
                        }
                    }
                    totalLength = apiList.size();
                }
                if (totalLength <= ((start + end) - 1)) {
                    end = totalLength;
                }
                for (int i = start; i < end; i++) {
                    apiSet.add(apiList.get(i));
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to search APIs with input url-pattern", e);
        } catch (GovernanceException e) {
            handleException("Failed to search APIs with input url-pattern", e);
        }
        result.put("apis", apiSet);
        result.put("length", totalLength);
        return result;
    }

//    @Override public Map<String, Object> searchPaginatedAPIsByContent(Organization requestedOrg, String searchQuery,
//                                    int start, int end, boolean limitAttributes) {
//        return null;
//    }

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
            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifactManager docArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.DOCUMENTATION_KEY);

            String paginationLimit =  null;// getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);

            /*String paginationLimit = getAPIManagerConfiguration()
                                            .getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);*/

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
            String newSearchQuery = PersistenceUtil.getSearchQuery(searchQuery, this.username);
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
//            if (documentIndexer != null && documentIndexer instanceof DocumentIndexer) {
            // DocumentIndexer needs to be implemented in Persistence module
             if (true) {

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
                PersistenceUtil.handleException(errorMsg);
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
                    Documentation doc = RegistryPersistenceUtil.getDocumentation(docArtifact);
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
                            if (apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE).
                                                            equals(APIConstants.AuditLogConstants.API_PRODUCT)) {
                                associatedAPIProduct = RegistryPersistenceUtil.getAPIProduct(apiArtifact, registry);
                            } else {
                                associatedAPI = RegistryPersistenceUtil.getAPI(apiArtifact, registry);
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
                        if (apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE).
                                                        equals(APIConstants.API_PRODUCT)) {
                            apiProduct = RegistryPersistenceUtil.getAPIProduct(apiArtifact, registry);
                            apiProductSet.add(apiProduct);
                        } else {
                            api = RegistryPersistenceUtil.getAPI(apiArtifact, registry);
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


    @Override public String getGraphqlSchema(String apiId) {
        return null;
    }

    @Override public void saveGraphqlSchemaDefinition(String apiId, String schemaDefinition) {

    }

    @Override public void deleteAPI(String apiId) {

    }

    @Override public Documentation getDocumentation(String apiId, String docId, Organization requestedOrg)
                                    throws APIManagementException {
        String requestedTenantDomain = requestedOrg.getName();
        Documentation documentation = null;
        try {
            Registry registryType;
            boolean isTenantMode = (requestedOrg.getName() != null);
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(
                                            requestedTenantDomain))) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                                                .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                                                                                tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registryType,
                                            APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            if (artifact == null) {
                return documentation;
            }
            if (null != artifact) {
                documentation = getDocumentation(artifact);
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
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return documentation;
    }

    // this method should be at somewhere else
    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    protected GenericArtifactManager getAPIGenericArtifactManagerFromUtil(Registry registry, String keyType)
                                    throws APIManagementException {
        return getArtifactManager(registry, keyType);
    }

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param registry Registry
     * @param key      , key name of the key
     * @return GenericArtifactManager
     * @throws APIManagementException if failed to initialized GenericArtifactManager
     */
    public static GenericArtifactManager getArtifactManager(Registry registry, String key)
                                    throws APIManagementException {

        GenericArtifactManager artifactManager = null;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
                artifactManager = new GenericArtifactManager(registry, key);
            } else {
                log.warn("Couldn't find GovernanceArtifactConfiguration of RXT: " + key
                                                + ". Tenant id set in registry : " + ((UserRegistry) registry)
                                                .getTenantId() + ", Tenant domain set in PrivilegedCarbonContext: "
                                                + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifactManager;
    }

    /**
     * Create the Documentation from artifact
     *
     * @param artifact Documentation artifact
     * @return Documentation
     * @throws APIManagementException if failed to create Documentation from artifact
     */
    public static Documentation getDocumentation(GenericArtifact artifact) throws APIManagementException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setId(artifact.getId());
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));
            String visibilityAttr = artifact.getAttribute(APIConstants.DOC_VISIBILITY);
            Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

            if (visibilityAttr != null) {
                if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                    documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                    documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                    documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                }
            }
            documentation.setVisibility(documentVisibility);

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.URL;
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.FILE;
                documentation.setFilePath(prependWebContextRoot(artifact.getAttribute(APIConstants.DOC_FILE_PATH)));
            } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.MARKDOWN;
            }
            documentation.setSourceType(docSourceType);
            if (documentation.getType() == DocumentationType.OTHER) {
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new APIManagementException("Failed to get documentation from artifact", e);
        }
        return documentation;
    }

    /**
     * Prepends the webcontextroot to a registry path.
     *
     * @param postfixUrl path to be prepended.
     * @return Path prepended with he WebContext root.
     */
    public static String prependWebContextRoot(String postfixUrl) {

        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (webContext != null && !"/".equals(webContext)) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }

    @Override public void removeDocumentation(String apiOrProductId, String docId) {

    }

    @Override public void updateDocumentation(String apiId, Documentation documentation) {

    }

    @Override public List<Documentation> getAllDocumentation(String apiOrProductId) {
        return null;
    }

    @Override public void addDocumentation(String apiId, Documentation documentation) {

    }


    @Override public API getLightweightAPIByUUID(String uuid, String requestedOrg) {
        return null;
    }

    @Override public Map<String, Object> getAPILifeCycleData(String apiId) {
        return null;
    }

    @Override public List<Mediation> getAllApiSpecificMediationPolicies(String apiId) {
        return null;
    }

    @Override public Mediation getApiSpecificMediationPolicyFromUUID(String apiOrProductId,
                                    String mediationPolicyUUID) {
        return null;
    }

    @Override public Mediation getApiSpecificMediationPolicyFromUUID(String mediationPolicyUUID) {
        return null;
    }

    @Override public void updateApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override public void deleteApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override public boolean isMediationPolicyExists(String mediationPolicyId) {
        return false;
    }

    @Override public void addApiSpecificMediationPolicy(String apiOrProductId, String type, ResourceFile contentFile) {

    }

    @Override public void configureMonetizationInAPI(API api) {

    }

    @Override public void configureMonetizationInAPI(String apiId, JSONObject monetizationProperties,
                                    boolean isMonetizationEnabled) {

    }

    @Override public boolean isSOAPToRESTApi(String apiOrProductId) {
        return false;
    }

    @Override public String getRestToSoapConvertedSequence(String apiOrProductId, String seqType) {
        return null;
    }

    @Override public String getResourcePolicyFromResourceId(String apiId, String resourceId) {
        return null;
    }

    @Override public void updateResourcePolicyFromResourceId(String apiId, String resourceId, String content) {

    }

    @Override public String getOASDefinitionOfAPI(String apiOrProductId) {
        return null;
    }

    @Override public void saveOASAPIDefinition(String apiId, String apiDefinitionJSON) {

    }

    @Override public ResourceFile getIcon(String apiId) {
        return null;
    }

    @Override public void saveAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail) {

    }

    @Override public boolean isDocumentationExists(String apiOrProductId, String docName) {
        return false;
    }

    @Override public void changeAPILifeCycle(String apiId, String status) {

    }

    @Override public int createNewAPIVersion(API api, String newVersion) {
        return 0;
    }


    @Override public void deleteAPIProduct(String apiProductId) {

    }

    @Override public Documentation getProductDocumentation(String productId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override public Map<String, Object> getDocumentContent(String apiId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override public boolean isApiExists(APIIdentifier apiIdentifier) {
        return false;
    }

//    @Override public void createAPI(API api) throws APIManagementException {
//    }

    @Override public API createAPI(API api) throws APIManagementException {
        //Add default API LC if it is not there
        try {
            if (!CommonUtil.lifeCycleExists(APIConstants.API_LIFE_CYCLE,
                                            registryService.getConfigSystemRegistry(tenantId))) {
                String defaultLifecyclePath = CommonUtil.getDefaltLifecycleConfigLocation() + File.separator
                                                + APIConstants.API_LIFE_CYCLE + APIConstants.XML_EXTENSION;
                File file = new File(defaultLifecyclePath);
                String content = null;
                if (file != null && file.exists()) {
                    content = FileUtils.readFileToString(file);
                }
                if (content != null) {
                    CommonUtil.addLifecycle(content, registryService.getConfigSystemRegistry(tenantId),
                                                    CommonUtil.getRootSystemRegistry(tenantId));
                }
            }
        } catch (RegistryException e) {
            handleException("Error occurred while adding default APILifeCycle.", e);
        } catch (IOException e) {
            handleException("Error occurred while loading APILifeCycle.xml.", e);
        } catch (XMLStreamException e) {
            handleException("Error occurred while adding default API LifeCycle.", e);
        }

        if (apiGenericArtifactManager == null) {
            String errorMessage = "Failed to retrieve artifact manager when creating API " + api.getId().getApiName();
            log.error(errorMessage);
            throw new APIManagementException(errorMessage);
        }

        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact = apiGenericArtifactManager
                                            .newGovernanceArtifact(new QName(api.getId().getApiName()));
            if (genericArtifact == null) {
                String errorMessage = "Generic artifact is null when creating API " + api.getId().getApiName();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact artifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            apiGenericArtifactManager.addGenericArtifact(artifact);
            //Attach the API lifecycle
            artifact.attachLifecycle(APIConstants.API_LIFE_CYCLE);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = RegistryPersistenceUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            //            if (PersistenceUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
            //                String path = RegistryPersistenceUtil.createWSDL(registry, api);
            //                //updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            //            }
            //
            //            if (api.getWsdlResource() != null) {
            //                String path = APIUtil.saveWSDLResource(registry, api);
            //                //updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            //            }
           // // RegistryPersistenceUtil.attachLabelsToAPIArtifact(artifact, api, tenantDomain, gatewayLabelList);
            String apiStatus = api.getStatus();
            saveAPIStatus(artifactPath, apiStatus);

            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }

            String publisherAccessControlRoles = api.getAccessControlRoles();
            updateRegistryResources(artifactPath, publisherAccessControlRoles, api.getAccessControl(),
                                            api.getAdditionalProperties());
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                            visibleRoles, artifactPath, registry);

            registry.commitTransaction();
            api.setUUID(artifact.getId());
            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + api.getId().getApiName() + ", API Version " + api.getId()
                                                .getVersion() + " created";
                log.debug(logMessage);
            }
            if (log.isDebugEnabled()) {
                log.debug("API details successfully added to the registry. API Name: " + api.getId().getApiName()
                                                + ", API Version : " + api.getId().getVersion() + ", API context : "
                                                + api.getContext());
            }
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API: " + api.getId().getApiName(), re);
            }
            handleException("Error while performing registry transaction operation", e);
        } catch (APIManagementException e) {
            handleException("Error while creating API", e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                handleException("Error while rolling back the transaction for API: " + api.getId().getApiName(), ex);
            }
        }
        return api;

    }

    /**
     * To add API/Product roles restrictions and add additional properties.
     *
     * @param artifactPath                Path of the API/Product artifact.
     * @param publisherAccessControlRoles Role specified for the publisher access control.
     * @param publisherAccessControl      Publisher Access Control restriction.
     * @param additionalProperties        Additional properties that is related with an API/Product.
     * @throws RegistryException Registry Exception.
     */
    private void updateRegistryResources(String artifactPath, String publisherAccessControlRoles,
                                    String publisherAccessControl, Map<String, String> additionalProperties)
                                    throws RegistryException {
        publisherAccessControlRoles = (publisherAccessControlRoles == null || publisherAccessControlRoles.trim()
                                        .isEmpty()) ? APIConstants.NULL_USER_ROLE_LIST : publisherAccessControlRoles;
        if (publisherAccessControlRoles.equalsIgnoreCase(APIConstants.NULL_USER_ROLE_LIST)) {
            publisherAccessControl = APIConstants.NO_ACCESS_CONTROL;
        }
        if (!registry.resourceExists(artifactPath)) {
            return;
        }

        Resource apiResource = registry.get(artifactPath);
        if (apiResource != null) {
            if (additionalProperties != null) {
                // Removing all the properties, before updating new properties.
                Properties properties = apiResource.getProperties();
                if (properties != null) {
                    Enumeration propertyNames = properties.propertyNames();
                    while (propertyNames.hasMoreElements()) {
                        String propertyName = (String) propertyNames.nextElement();
                        if (propertyName.startsWith(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX)) {
                            apiResource.removeProperty(propertyName);
                        }
                    }
                }
            }
            // We are changing to lowercase, as registry search only supports lower-case characters.
            apiResource.setProperty(APIConstants.PUBLISHER_ROLES, publisherAccessControlRoles.toLowerCase());

            // This property will be only used for display proposes in the Publisher UI so that the original case of
            // the roles that were specified can be maintained.
            apiResource.setProperty(APIConstants.DISPLAY_PUBLISHER_ROLES, publisherAccessControlRoles);
            apiResource.setProperty(APIConstants.ACCESS_CONTROL, publisherAccessControl);
            apiResource.removeProperty(APIConstants.CUSTOM_API_INDEXER_PROPERTY);
            if (additionalProperties != null && additionalProperties.size() != 0) {
                for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
                    apiResource.setProperty((APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX + entry.getKey()),
                                                    entry.getValue());
                }
            }
            registry.put(artifactPath, apiResource);
        }
    }

    @Override public String createWsdl(API api, InputStream wsdlContent, OMElement wsdlContentEle) {
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
                                        .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
                                                                        api.getId().getVersion());

        String absoluteWSDLResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + wsdlResourcePath;

        //        APIMWSDLReader wsdlReader = new APIMWSDLReader();
        //        OMElement wsdlContentEle;
        String wsdlRegistryPath;

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                        .equalsIgnoreCase(tenantDomain)) {
            wsdlRegistryPath = RegistryConstants.PATH_SEPARATOR + "registry" + RegistryConstants.PATH_SEPARATOR
                                            + "resource" + absoluteWSDLResourcePath;
        } else {
            wsdlRegistryPath = "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
                                            + RegistryConstants.PATH_SEPARATOR + "resource" + absoluteWSDLResourcePath;
        }

        try {
            Resource wsdlResource = registry.newResource();
            // isWSDL2Document(api.getWsdlUrl()) method only understands http or file system urls.
            // Hence if this is a registry url, should not go in to the following if block
            if (!api.getWsdlUrl().matches(wsdlRegistryPath) && (api.getWsdlUrl().startsWith("http:") || api.getWsdlUrl()
                                            .startsWith("https:") || api.getWsdlUrl().startsWith("file:") || api
                                            .getWsdlUrl().startsWith("/t"))) {
                // Get the WSDL 1.1 or 2.0 processor and process the content based on the version
                wsdlResource.setContentStream(wsdlContent);

            } else {
                byte[] wsdl = (byte[]) registry.get(wsdlResourcePath).getContent();
                if (PersistenceUtil.isWSDL2Resource(wsdl)) {
                    wsdlResource.setContent(wsdlContentEle.toString());
                } else {
                    wsdlResource.setContent(wsdlContentEle.toString());
                }
            }
            registry.put(wsdlResourcePath, wsdlResource);
            //set the anonymous role for wsld resource to avoid basicauth security.
            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                            visibleRoles, wsdlResourcePath);

            //Delete any WSDL archives if exists
            String wsdlArchivePath = RegistryPersistenceUtil.getWsdlArchivePath(api.getId());
            if (registry.resourceExists(wsdlArchivePath)) {
                registry.delete(wsdlArchivePath);
            }

            //check availability of below 2 lines
            //set the wsdl resource permlink as the wsdlURL.
            api.setWsdlUrl(RegistryPersistenceUtil.getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));

            GenericArtifactManager artifactManager = RegistryPersistenceUtil
                                            .getArtifactManager(registry, APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "";
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }

            GenericArtifact genericArtifact = artifactManager
                                            .newGovernanceArtifact(new QName(api.getId().getApiName()));

            GenericArtifact artifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());

            if (PersistenceUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
                updateWSDLUriInAPIArtifact(wsdlRegistryPath, artifactManager, artifact, artifactPath);
            }

            //            if (api.getWsdlResource() != null) {
            //                String path = APIUtil.saveWSDLResource(registry, api);
            //                updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            //            }

        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public ResourceFile getWSDL(String apiId) {
        try {
            API api = getAPIbyId(apiId, tenantDomain);
            String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
                                            .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
                                                                            api.getId().getVersion());

            Resource wsdlResource = registry.get(wsdlResourcePath);
            return new ResourceFile(wsdlResource.getContentStream(), wsdlResource.getMediaType());
        } catch (APIManagementException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public String updateWsdlFromWsdlFile(String apiId, ResourceFile wsdlResourceFile)
                                    throws APIManagementException {
        API api = null;
        try {
            // GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);

            api = getAPIbyId(apiId, tenantDomain);

            GenericArtifact genericArtifact = null;
            GenericArtifact apiArtifact = null;
            String apiArtifactPath = null;
            genericArtifact = apiGenericArtifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));

            if (genericArtifact == null) {
                String errorMessage = "";
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            apiArtifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            apiArtifactPath = GovernanceUtils.getArtifactPath(registry, apiArtifact.getId());

            String wsdlResourcePath;
            boolean isZip = false;

            String wsdlResourcePathArchive = APIConstants.API_WSDL_RESOURCE_LOCATION
                                            + APIConstants.API_WSDL_ARCHIVE_LOCATION + api.getId().getProviderName()
                                            + APIConstants.WSDL_PROVIDER_SEPERATOR + api.getId().getApiName() + api
                                            .getId().getVersion() + APIConstants.ZIP_FILE_EXTENSION;
            String wsdlResourcePathFile = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
                                            .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
                                                                            api.getId().getVersion());

            if (wsdlResourceFile.getContentType().equals(APIConstants.APPLICATION_ZIP)) {
                wsdlResourcePath = wsdlResourcePathArchive;
                isZip = true;
            } else {
                wsdlResourcePath = wsdlResourcePathFile;
            }

            String absoluteWSDLResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + wsdlResourcePath;

            Resource wsdlResourceToUpdate = registry.newResource();
            wsdlResourceToUpdate.setContentStream(api.getWsdlResource().getContent());
            wsdlResourceToUpdate.setMediaType(api.getWsdlResource().getContentType());
            registry.put(wsdlResourcePath, wsdlResourceToUpdate);
            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                            visibleRoles, wsdlResourcePath);

            if (isZip) {
                //Delete any WSDL file if exists
                if (registry.resourceExists(wsdlResourcePathFile)) {
                    registry.delete(wsdlResourcePathFile);
                }
            } else {
                //Delete any WSDL archives if exists
                if (registry.resourceExists(wsdlResourcePathArchive)) {
                    registry.delete(wsdlResourcePathArchive);
                }
            }

            api.setWsdlUrl(RegistryPersistenceUtil.getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));

            RegistryPersistenceUtil.updateWSDLUriInAPIArtifact(wsdlResourcePath, apiGenericArtifactManager, apiArtifact,
                                            apiArtifactPath);
            return wsdlResourcePath;

        } catch (RegistryException e) {
            String msg = "Failed to add WSDL Archive " + api.getWsdlUrl() + " to the registry";
            log.error(msg, e);
            //throw new RegistryException(msg, e);
        } catch (APIManagementException e) {
            String msg = "Failed to process the WSDL Archive: " + api.getWsdlUrl();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus  Current status of the API
     * @throws APIManagementException on error
     */
    // HAS REG USAGE
    private void saveAPIStatus(String artifactId, String apiStatus) throws APIManagementException {
        try {
            Resource resource = registry.get(artifactId);
            if (resource != null) {
                String propValue = resource.getProperty(APIConstants.API_STATUS);
                if (propValue == null) {
                    resource.addProperty(APIConstants.API_STATUS, apiStatus);
                } else {
                    resource.setProperty(APIConstants.API_STATUS, apiStatus);
                }
                registry.put(artifactId, resource);
            }
        } catch (RegistryException e) {
            handleException("Error while adding API", e);
        }
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
        return searchAPIsByDoc(registry, tenantID, username, searchTerm, APIConstants.STORE_CLIENT);
    }

    /**
     * Search Apis by Doc Content
     *
     * @param registry     - Registry which is searched
     * @param tenantID     - Tenant id of logged in domain
     * @param username     - Logged in username
     * @param searchTerm   - Search value for doc
     * @param searchClient - Search client
     * @return - Documentation to APIs map
     * @throws APIManagementException - If failed to get ArtifactManager for given tenant
     */
    public Map<Documentation, API> searchAPIsByDoc(Registry registry, int tenantID, String username,
                                    String searchTerm, String searchClient) throws APIManagementException {

        Map<Documentation, API> apiDocMap = new HashMap<Documentation, API>();

        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                                            APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when searching APIs by docs in tenant ID " + tenantID;
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifactManager docArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                                            APIConstants.DOCUMENTATION_KEY);
            if (docArtifactManager == null) {
                String errorMessage = "Doc artifact manager is null when searching APIs by docs in tenant ID " +
                                                tenantID;
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            SolrClient client = SolrClient.getInstance();
            Map<String, String> fields = new HashMap<String, String>();
            fields.put(APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, "*" + APIConstants.API_ROOT_LOCATION + "*");
            fields.put(APIConstants.DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD, "*");

            if (tenantID == -1) {
                tenantID = org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
            }
            //PaginationContext.init(0, 10000, "ASC", APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, Integer.MAX_VALUE);
            SolrDocumentList documentList = client.query(searchTerm, tenantID, fields);

            org.wso2.carbon.user.api.AuthorizationManager manager = ServiceReferenceHolder.getInstance().
                                            getRealmService().getTenantUserRealm(tenantID).
                                            getAuthorizationManager();

            username = MultitenantUtils.getTenantAwareUsername(username);

            for (SolrDocument document : documentList) {
                String filePath = (String) document.getFieldValue("path_s");
                int index = filePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
                filePath = filePath.substring(index);
                Association[] associations = registry.getAllAssociations(filePath);
                API api = null;
                Documentation doc = null;
                for (Association association : associations) {
                    boolean isAuthorized;
                    String documentationPath = association.getSourcePath();
                    String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                    RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + documentationPath);
                    if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
                        isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
                    } else {
                        isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
                    }

                    if (isAuthorized) {
                        Resource docResource = registry.get(documentationPath);
                        String docArtifactId = docResource.getUUID();
                        if (docArtifactId != null) {
                            GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docArtifactId);
                            doc = RegistryPersistenceUtil.getDocumentation(docArtifact);
                        }

                        Association[] docAssociations = registry.getAssociations(documentationPath, APIConstants.DOCUMENTATION_ASSOCIATION);
                        /* There will be only one document association, for a document path which is by its owner API*/
                        if (docAssociations.length > 0) {

                            String apiPath = docAssociations[0].getSourcePath();
                            path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                            RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + apiPath);
                            if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
                                isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
                            } else {
                                isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
                            }

                            if (isAuthorized) {
                                Resource resource = registry.get(apiPath);
                                String apiArtifactId = resource.getUUID();
                                if (apiArtifactId != null) {
                                    GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactId);
                                    api = RegistryPersistenceUtil.getAPI(apiArtifact, registry);
                                } else {
                                    throw new GovernanceException("artifact id is null of " + apiPath);
                                }
                            }
                        }
                    }

                    if (doc != null && api != null) {
                        if (APIConstants.STORE_CLIENT.equals(searchClient)) {
                            if (APIConstants.PUBLISHED.equals(api.getStatus()) ||
                                                            APIConstants.PROTOTYPED.equals(api.getStatus())) {
                                apiDocMap.put(doc, api);
                            }
                        } else {
                            apiDocMap.put(doc, api);
                        }
                    }
                }
            }
        } catch (IndexerException e) {
            handleException("Failed to search APIs with type Doc", e);
        } catch (RegistryException e) {
            handleException("Failed to search APIs with type Doc", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to search APIs with type Doc", e);
        }
        return apiDocMap;
    }

}
