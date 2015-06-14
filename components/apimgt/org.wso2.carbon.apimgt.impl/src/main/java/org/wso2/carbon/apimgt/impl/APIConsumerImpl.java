/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LoginPostExecutor;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APISubscription;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;
import org.wso2.carbon.apimgt.impl.workflow.AbstractApplicationRegistrationWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.keymgt.stub.types.carbon.ApplicationKeysDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
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
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class provides the core API store functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * programmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIConsumerImpl extends AbstractAPIManager implements APIConsumer {

    private static final Log log = LogFactory.getLog(APIConsumerImpl.class);

    /* Map to Store APIs against Tag */
    private Map<String, Set<API>> taggedAPIs;
    private boolean isTenantModeStoreView;
    private String requestedTenant;
    private boolean isTagCacheEnabled;
    private Set<Tag> tagSet;
    private long tagCacheValidityTime;
    private long lastUpdatedTime;
    private Object tagCacheMutex = new Object();
    private LRUCache<String,GenericArtifactManager> genericArtifactCache = new LRUCache<String,GenericArtifactManager>(5);
    private Set<API> recentlyAddedAPI;

    public APIConsumerImpl() throws APIManagementException {
        super();
        readTagCacheConfigs();
    }

    public APIConsumerImpl(String username) throws APIManagementException {
        super(username);
        readTagCacheConfigs();
    }

    private void readTagCacheConfigs() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration();
        String enableTagCache = config.getFirstProperty(APIConstants.API_STORE_TAG_CACHE_DURATION);
        if (enableTagCache == null) {
            isTagCacheEnabled = false;
            tagCacheValidityTime = 0;
        } else {
            isTagCacheEnabled = true;
            tagCacheValidityTime = Long.parseLong(enableTagCache);
        }
    }

    public Subscriber getSubscriber(String subscriberId) throws APIManagementException {
        Subscriber subscriber = null;
        try {
            subscriber = ApiMgtDAO.getSubscriber(subscriberId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscriber", e);
        }
        return subscriber;
    }


    /**
     * Returns the set of APIs with the given tag from the taggedAPIs Map
     *
     * @param tag
     * @return
     * @throws APIManagementException
     */
    public Set<API> getAPIsWithTag(String tag) throws APIManagementException {
        if (taggedAPIs != null) {
            return taggedAPIs.get(tag);
        }
        this.getAllTags(this.tenantDomain);
        if (taggedAPIs != null) {
            return taggedAPIs.get(tag);
        }
        return null;
    }

    /**
     * Returns the set of APIs with the given tag from the taggedAPIs Map
     *
     * @param tag
     * @return
     * @throws APIManagementException
     */
    public Map<String,Object> getPaginatedAPIsWithTag(String tag,int start,int end) throws APIManagementException {
        List<API> apiSet = new ArrayList<API>();
        Set<API> resultSet = new TreeSet<API>(new APIVersionComparator());
        Map<String, Object> results = new HashMap<String, Object>();
        Set<API> taggedAPISet = this.getAPIsWithTag(tag);
        if (taggedAPISet != null) {
            if (taggedAPISet.size() < end) {
                end = taggedAPISet.size();
            }
            int totalLength;

            apiSet.addAll(taggedAPISet);
            totalLength = apiSet.size();
            if (totalLength <= ((start + end) - 1)) {
                end = totalLength;
            } else {
                end = start + end;
            }
            for (int i = start; i < end; i++) {
                resultSet.add(apiSet.get(i));
            }

            results.put("apis", resultSet);
            results.put("length", taggedAPISet.size());
        } else {
            results.put("apis", null);
            results.put("length", 0);

        }
        return results;
    }


    /**
     * Returns the set of APIs with the given tag, retrieved from registry
     *
     * @param registry - Current registry; tenant/SuperTenant
     * @param tag
     * @return
     * @throws APIManagementException
     */
    private Set<API> getAPIsWithTag(Registry registry, String tag)
            throws APIManagementException {
        Set<API> apiSet = new TreeSet<API>(new APINameComparator());
        try {
            String resourceByTagQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/resource-by-tag";
            Map<String, String> params = new HashMap<String, String>();
            params.put("1", tag);
            params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.RESOURCE_UUID_RESULT_TYPE);
            Collection collection = registry.executeQuery(resourceByTagQueryPath, params);

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);

            for (String row : collection.getChildren()) {
                String uuid = row.substring(row.indexOf(";") + 1, row.length());
                GenericArtifact genericArtifact = artifactManager.getGenericArtifact(uuid);
                if (genericArtifact != null &&
                    genericArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS).equals(APIConstants.PUBLISHED)) {
                    API api = APIUtil.getAPI(genericArtifact);
                    if (api != null) {
                        apiSet.add(api);
                    }
                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get API for tag " + tag, e);
        }
        return apiSet;
    }

    /**
     * The method to get APIs to Store view      *
     *
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    public Set<API> getAllPublishedAPIs(String tenantDomain) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        try {
            Registry userRegistry;
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                userRegistry = registry;
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
            if (artifactManager != null) {
                GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    return apiSortedSet;
                }

                Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
                List<API> multiVersionedAPIs = new ArrayList<API>();
                Comparator<API> versionComparator = new APIVersionComparator();
                Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
                Boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
                for (GenericArtifact artifact : genericArtifacts) {
                    // adding the API provider can mark the latest API .
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    API api = null;
                    //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
                    if (!displayAPIsWithMultipleStatus) {
                        // then we are only interested in published APIs here...
                        if (status.equals(APIConstants.PUBLISHED)) {
                            api = APIUtil.getAPI(artifact);
                        }
                    } else {   // else we are interested in both deprecated/published APIs here...
                        if (status.equals(APIConstants.PUBLISHED) || status.equals(APIConstants.DEPRECATED)) {
                            api = APIUtil.getAPI(artifact);

                        }

                    }
                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                            API existingAPI = latestPublishedAPIs.get(key);
                            if (existingAPI != null) {
                                // If we have already seen an API with the same name, make sure
                                // this one has a higher version number
                                if (versionComparator.compare(api, existingAPI) > 0) {
                                    latestPublishedAPIs.put(key, api);
                                }
                            } else {
                                // We haven't seen this API before
                                latestPublishedAPIs.put(key, api);
                            }
                        } else { //If allow showing multiple versions of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                                    .getVersion();
                            multiVersionedAPIs.add(api);
                        }
                    }
                }
                if (!displayMultipleVersions) {
                    for (API api : latestPublishedAPIs.values()) {
                        apiSortedSet.add(api);
                    }
                    return apiSortedSet;
                } else {
                    for (API api : multiVersionedAPIs) {
                        apiVersionsSortedSet.add(api);
                    }
                    return apiVersionsSortedSet;
                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        }
        return apiSortedSet;

    }


    /**
     * The method to get APIs to Store view      *
     *
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    public Map<String,Object> getAllPaginatedPublishedAPIs(String tenantDomain,int start,int end) throws APIManagementException {

    	Boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
    	Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
        if (!displayAPIsWithMultipleStatus) {
            //Create the search attribute map
            listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
                add(APIConstants.PUBLISHED);
            }});
        } else{
            return getAllPaginatedAPIs(tenantDomain, start, end);
        }


        Map<String,Object> result=new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength=0;
        try {
            Registry userRegistry;
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                // explicitly load the tenant's registry
      	      	APIUtil.loadTenantRegistry(tenantId);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();

            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
            if (artifactManager != null) {
                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength=PaginationContext.getInstance().getLength();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    return result;
                }

                for (GenericArtifact artifact : genericArtifacts) {
                    // adding the API provider can mark the latest API .
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    API api  = APIUtil.getAPI(artifact);

                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                            API existingAPI = latestPublishedAPIs.get(key);
                            if (existingAPI != null) {
                                // If we have already seen an API with the same name, make sure
                                // this one has a higher version number
                                if (versionComparator.compare(api, existingAPI) > 0) {
                                    latestPublishedAPIs.put(key, api);
                                }
                            } else {
                                // We haven't seen this API before
                                latestPublishedAPIs.put(key, api);
                            }
                        } else { //If allow showing multiple versions of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                                    .getVersion();
                            multiVersionedAPIs.add(api);
                        }
                    }
                }
                if (!displayMultipleVersions) {
                    for (API api : latestPublishedAPIs.values()) {
                        apiSortedSet.add(api);
                    }
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    return result;

                } else {
                    for (API api : multiVersionedAPIs) {
                        apiVersionsSortedSet.add(api);
                    }
                    result.put("apis",apiVersionsSortedSet);
                    result.put("totalLength",totalLength);
                    return result;

                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        }finally {
            PaginationContext.destroy();
        }
        result.put("apis",apiSortedSet);
        result.put("totalLength",totalLength);
        return result;

    }

    /**
     * The method to get APIs by given status to Store view
     *
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    @Override
	public Map<String, Object> getAllPaginatedAPIsByStatus(String tenantDomain,
			int start, int end, final String apiStatus, boolean returnAPItags) throws APIManagementException {
    	Boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
    	Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
        if (APIConstants.PROTOTYPED.equals(apiStatus)) {
            listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
                add(apiStatus);
            }});
        } else {

            if (!displayAPIsWithMultipleStatus) {
                //Create the search attribute map
                listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
                    add(apiStatus);
                }});
            } else {
                return getAllPaginatedAPIs(tenantDomain, start, end);
            }
        }

        Map<String,Object> result=new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength=0;
        try {
            Registry userRegistry;
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();

            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
            if (artifactManager != null) {
                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength=PaginationContext.getInstance().getLength();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    return result;
                }

                for (GenericArtifact artifact : genericArtifacts) {
                    // adding the API provider can mark the latest API .
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    API api  = APIUtil.getAPI(artifact);
                    
                    if (api != null) {

                        if (returnAPItags) {
                            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
                            Set<String> tags = new HashSet<String>();
                            org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
                            for (org.wso2.carbon.registry.core.Tag tag1 : tag) {
                                tags.add(tag1.getTagName());
                            }
                            api.addTags(tags);
                        }

                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                            API existingAPI = latestPublishedAPIs.get(key);
                            if (existingAPI != null) {
                                // If we have already seen an API with the same name, make sure
                                // this one has a higher version number
                                if (versionComparator.compare(api, existingAPI) > 0) {
                                    latestPublishedAPIs.put(key, api);
                                }
                            } else {
                                // We haven't seen this API before
                                latestPublishedAPIs.put(key, api);
                            }
                        } else { //If allow showing multiple versions of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                                    .getVersion();
                            multiVersionedAPIs.add(api);
                        }
                    }
                }
                if (!displayMultipleVersions) {
                    for (API api : latestPublishedAPIs.values()) {
                        apiSortedSet.add(api);
                    }
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    return result;

                } else {
                    for (API api : multiVersionedAPIs) {
                        apiVersionsSortedSet.add(api);
                    }
                    result.put("apis",apiVersionsSortedSet);
                    result.put("totalLength",totalLength);
                    return result;

                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        }finally {
            PaginationContext.destroy();
        }
        result.put("apis",apiSortedSet);
        result.put("totalLength",totalLength);
        return result;
	}

    /**
     * Re-generates the access token.
     * @param oldAccessToken          Token to be revoked
     * @param clientId                Consumer Key for the Application
     * @param clientSecret            Consumer Secret for the Application
     * @param validityTime            Desired Validity time for the token
     * @param accessAllowDomains List of domains that this access token should be allowed to.
     * @param jsonInput               Additional parameters if Authorization server needs any.
     * @return Renewed Access Token.
     * @throws APIManagementException
     */
    @Override
    public AccessTokenInfo renewAccessToken(String oldAccessToken, String clientId, String clientSecret,
                                            String validityTime, String accessAllowDomains,String
            requestedScopesString, String jsonInput) throws APIManagementException {
        // Create Token Request with parameters provided from UI.
        String[] requestedScopes=requestedScopesString.split(" ");
        String[] accessAllowDomainsArray = accessAllowDomains.split(",");
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(clientId);
        tokenRequest.setClientSecret(clientSecret);
        tokenRequest.setValidityPeriod(Long.parseLong(validityTime));
        tokenRequest.setTokenToRevoke(oldAccessToken);
        tokenRequest.setScope(requestedScopes);
        boolean isRegenarateOptionEnabled = true;
        if (APIUtil.getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
            isRegenarateOptionEnabled = false;
        }
        tokenRequest.setRegenerateOptionEnabled(isRegenarateOptionEnabled);
        try {
            // Populating additional parameters.
            tokenRequest = ApplicationUtils.populateTokenRequest(jsonInput, tokenRequest);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            AccessTokenInfo tokenResponse = keyManager.getNewApplicationAccessToken(tokenRequest);
            return tokenResponse;

        } catch (APIManagementException e) {
            log.error("Error while re-generating AccessToken", e);
            throw e;
        }
    }


    /**
     * The method to get All PUBLISHED and DEPRECATED APIs, to Store view
     *
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    public Map<String,Object> getAllPaginatedAPIs(String tenantDomain,int start,int end) throws APIManagementException {
        Map<String,Object> result=new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength=0;
        try {
            Registry userRegistry;
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);

            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);


            boolean noPublishedAPIs = false;
            if (artifactManager != null) {

            	//Create the search attribute map for PUBLISHED APIs
            	Map<String, List<String>> listMap = new HashMap<String, List<String>>();
                listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
                        add(APIConstants.PUBLISHED);
                    }});

                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength = PaginationContext.getInstance().getLength();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                	noPublishedAPIs = true;
                }
                int publishedAPICount = 0;
                for (GenericArtifact artifact : genericArtifacts) {
                    // adding the API provider can mark the latest API .
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    API api  = APIUtil.getAPI(artifact);

                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                            API existingAPI = latestPublishedAPIs.get(key);
                            if (existingAPI != null) {
                                // If we have already seen an API with the same name, make sure
                                // this one has a higher version number
                                if (versionComparator.compare(api, existingAPI) > 0) {
                                    latestPublishedAPIs.put(key, api);
                                }
                            } else {
                                // We haven't seen this API before
                                latestPublishedAPIs.put(key, api);
                            }
                        } else { //If allow showing multiple versions of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                                    .getVersion();
                            multiVersionedAPIs.add(api);
                        }
                    }
                }
                if (!displayMultipleVersions) {
                	publishedAPICount = latestPublishedAPIs.size();
                } else {
                	publishedAPICount = multiVersionedAPIs.size();
                }
                if ((start + end) > publishedAPICount) {
                	if (publishedAPICount > 0) {
                		/*Starting to retrieve DEPRECATED APIs*/
                		start = 0;
                		/* publishedAPICount is always less than end*/
                		end = end - publishedAPICount;
                	} else {
                		start = start - totalLength;
                	}
                	PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);
	                //Create the search attribute map for DEPRECATED APIs
	                Map<String, List<String>> listMapForDeprecatedAPIs = new HashMap<String, List<String>>();
	                listMapForDeprecatedAPIs.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
	                        add(APIConstants.DEPRECATED);
	                    }});

	                GenericArtifact[] genericArtifactsForDeprecatedAPIs = artifactManager.findGenericArtifacts(listMapForDeprecatedAPIs);
	                totalLength = totalLength + PaginationContext.getInstance().getLength();
	                if ((genericArtifactsForDeprecatedAPIs == null || genericArtifactsForDeprecatedAPIs.length == 0) && noPublishedAPIs) {
	                	result.put("apis",apiSortedSet);
	                    result.put("totalLength",totalLength);
	                    return result;
	                }

	                for (GenericArtifact artifact : genericArtifactsForDeprecatedAPIs) {
	                    // adding the API provider can mark the latest API .
	                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

	                    API api  = APIUtil.getAPI(artifact);

	                    if (api != null) {
	                        String key;
	                        //Check the configuration to allow showing multiple versions of an API true/false
	                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
	                            key = api.getId().getProviderName() + ":" + api.getId().getApiName();
	                            API existingAPI = latestPublishedAPIs.get(key);
	                            if (existingAPI != null) {
	                                // If we have already seen an API with the same name, make sure
	                                // this one has a higher version number
	                                if (versionComparator.compare(api, existingAPI) > 0) {
	                                    latestPublishedAPIs.put(key, api);
	                                }
	                            } else {
	                                // We haven't seen this API before
	                                latestPublishedAPIs.put(key, api);
	                            }
	                        } else { //If allow showing multiple versions of an API
	                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
	                                    .getVersion();
	                            multiVersionedAPIs.add(api);
	                        }
	                    }
	                }
                }

                if (!displayMultipleVersions) {
                    for (API api : latestPublishedAPIs.values()) {
                        apiSortedSet.add(api);
                    }
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    return result;

                } else {
                    for (API api : multiVersionedAPIs) {
                        apiVersionsSortedSet.add(api);
                    }
                    result.put("apis",apiVersionsSortedSet);
                    result.put("totalLength",totalLength);
                    return result;

                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        }finally {
            PaginationContext.destroy();
        }
        result.put("apis",apiSortedSet);
        result.put("totalLength",totalLength);
        return result;

    }

    private <T> T[] concatArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    public Set<API> getTopRatedAPIs(int limit) throws APIManagementException {
        int returnLimit = 0;
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSortedSet;
            }
            for (GenericArtifact genericArtifact : genericArtifacts) {
                String status = genericArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                if (status.equals(APIConstants.PUBLISHED)) {
                    String artifactPath = genericArtifact.getPath();

                    float rating = registry.getAverageRating(artifactPath);
                    if (rating > APIConstants.TOP_TATE_MARGIN && (returnLimit < limit)) {
                        returnLimit++;
                        apiSortedSet.add(APIUtil.getAPI(genericArtifact, registry));
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get top rated API", e);
        }
        return apiSortedSet;
    }

    /**
     * Get the recently added APIs set
     *
     * @param limit no limit. Return everything else, limit the return list to specified value.
     * @return Set<API>
     * @throws APIManagementException
     */
    public Set<API> getRecentlyAddedAPIs(int limit, String tenantDomain)
            throws APIManagementException {
        SortedSet<API> recentlyAddedAPIs = new TreeSet<API>(new APINameComparator());
        SortedSet<API> recentlyAddedAPIsWithMultipleVersions = new TreeSet<API>(new APIVersionComparator());
        Registry userRegistry = null;
        String latestAPIQueryPath = null;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean isRecentlyAddedAPICacheEnabled =
              Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE));

        PrivilegedCarbonContext.startTenantFlow();
        boolean isTenantFlowStarted = false;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            isTenantFlowStarted = true;
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            isTenantFlowStarted = true;
        }

        try {
            boolean isTenantMode = (tenantDomain != null);
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant based store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                // explicitly load the tenant's registry
      	      	APIUtil.loadTenantRegistry(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
                isTenantFlowStarted = true;
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                userRegistry = registry;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
                isTenantFlowStarted = true;
            }
            if (isRecentlyAddedAPICacheEnabled) {
                boolean isStatusChanged = false;
                recentlyAddedAPI = (Set<API>) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).get(username + ":" + tenantDomain);
                if (recentlyAddedAPI != null) {

                    for (API api : recentlyAddedAPI) {
                        try {
                            if (!APIConstants.PUBLISHED.equalsIgnoreCase(userRegistry.get(APIUtil.getAPIPath(api.getId())).getProperty(APIConstants.API_OVERVIEW_STATUS))) {
                                isStatusChanged = true;
                                break;
                            }
                        } catch (Exception ex) {
                            log.error("Error while checking API status for APP " + api.getId().getApiName() + "-" + api.getId().getVersion());
                        }

                    }
                    if (!isStatusChanged) {
                        return recentlyAddedAPI;
                    }
                }
            }

            PaginationContext.init(0, limit, "DESC", "timestamp", Integer.MAX_VALUE);

        	Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        	listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
        		add(APIConstants.PUBLISHED);
        	}});

        	//Find UUID
        	GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
        	if (artifactManager != null) {
        		GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
        		SortedSet<API> allAPIs = new TreeSet<API>(new APINameComparator());
        		for (GenericArtifact artifact : genericArtifacts) {
                    API api = APIUtil.getAPI(artifact);
                    if (api != null) {
                        allAPIs.add(api);
                    }
                }

				if (!APIUtil.isAllowDisplayMultipleVersions()) {
					Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
					Comparator<API> versionComparator = new APIVersionComparator();
					String key;
					for (API api : allAPIs) {
						key = api.getId().getProviderName() + ":" + api.getId().getApiName();
						API existingAPI = latestPublishedAPIs.get(key);
						if (existingAPI != null) {
							// If we have already seen an API with the same
							// name, make sure this one has a higher version
							// number
							if (versionComparator.compare(api, existingAPI) > 0) {
								latestPublishedAPIs.put(key, api);
							}
						} else {
							// We haven't seen this API before
							latestPublishedAPIs.put(key, api);
						}
					}

					for (API api : latestPublishedAPIs.values()) {
						recentlyAddedAPIs.add(api);
					}
					if (isRecentlyAddedAPICacheEnabled) {
						Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
						       .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME)
						       .put(username + ":" + tenantDomain, allAPIs);
					}
					return recentlyAddedAPIs;
				} else {
        			recentlyAddedAPIsWithMultipleVersions.addAll(allAPIs);
					if (isRecentlyAddedAPICacheEnabled) {
						Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
						       .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME)
						       .put(username + ":" + tenantDomain, allAPIs);
					}
        			return recentlyAddedAPIsWithMultipleVersions;
        		}
        	 }
        } catch (RegistryException e) {
        	handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
        	handleException("Failed to get all published APIs", e);
        } finally {
        	PaginationContext.destroy();
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
        return recentlyAddedAPIs;
    }

    public Set<Tag> getAllTags(String requestedTenantDomain) throws APIManagementException {

        this.isTenantModeStoreView = (requestedTenantDomain != null);

        if(requestedTenantDomain != null){
            this.requestedTenant = requestedTenantDomain;
        }

        /* We keep track of the lastUpdatedTime of the TagCache to determine its freshness.
         */
        long lastUpdatedTimeAtStart = lastUpdatedTime;
        long currentTimeAtStart = System.currentTimeMillis();
        if(isTagCacheEnabled && ( (currentTimeAtStart- lastUpdatedTimeAtStart) < tagCacheValidityTime)){
            if(tagSet != null){
                return tagSet;
            }
        }

        Map<String, Set<API>> tempTaggedAPIs = new HashMap<String, Set<API>>();
        TreeSet<Tag> tempTagSet = new TreeSet<Tag>(new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Registry userRegistry = null;
        String tagsQueryPath = null;
        boolean isTenantFlowStarted = false;
        try {
            tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
            Map<String, String> params = new HashMap<String, String>();
            params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.TAG_SUMMARY_RESULT_TYPE);
            if ((this.isTenantModeStoreView && this.tenantDomain==null) || (this.isTenantModeStoreView && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant based store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(this.requestedTenant);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                userRegistry = registry;
            }
            if (requestedTenant != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenant)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenant, true);
            }
            Collection collection = userRegistry.executeQuery(tagsQueryPath, params);
            for (String fullTag : collection.getChildren()) {
                //remove hardcoded path value
                String tagName = fullTag.substring(fullTag.indexOf(";") + 1, fullTag.indexOf(":"));

                Set<API> apisWithTag = getAPIsWithTag(userRegistry, tagName);
                    /* Add the APIs against the tag name */
                    if (apisWithTag.size() != 0) {
                        if (tempTaggedAPIs.containsKey(tagName)) {
                            for (API api : apisWithTag) {
                                tempTaggedAPIs.get(tagName).add(api);
                            }
                        } else {
                            tempTaggedAPIs.put(tagName, apisWithTag);
                        }
                    }
            }

            if(tempTaggedAPIs != null){
                Iterator<Map.Entry<String,Set<API>>>  entryIterator = tempTaggedAPIs.entrySet().iterator();
                while (entryIterator.hasNext()){
                    Map.Entry<String,Set<API>> entry = entryIterator.next();
                    tempTagSet.add(new Tag(entry.getKey(),entry.getValue().size()));
                }
            }
            synchronized (tagCacheMutex) {
                lastUpdatedTime = System.currentTimeMillis();
                this.taggedAPIs = tempTaggedAPIs;
                this.tagSet = tempTagSet;
            }

        } catch (RegistryException e) {
        	try {
        		//Before a tenant login to the store or publisher at least one time,
        		//a registry exception is thrown when the tenant store is accessed in anonymous mode.
        		//This fix checks whether query resource available in the registry. If not
        		// give a warn.
				if (!userRegistry.resourceExists(tagsQueryPath)) {
					log.warn("Failed to retrieve tags query resource at " + tagsQueryPath);
					return tagSet;
				}
			} catch (RegistryException e1) {
				//ignore
			}
            handleException("Failed to get all the tags", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all the tags", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return tagSet;
    }

	@Override
	public Set<Tag> getTagsWithAttributes(String tenantDomain) throws APIManagementException {
		// Fetch the all the tags first.
		Set<Tag> tags = getAllTags(tenantDomain);
		// For each and every tag get additional attributes from the registry.
		String descriptionPathPattern =
		                                APIConstants.TAGS_INFO_ROOT_LOCATION +
		                                        "/%s/description.txt";
		String thumbnailPathPattern = APIConstants.TAGS_INFO_ROOT_LOCATION + "/%s/thumbnail.png";

		//if the tenantDomain is not specified super tenant domain is used
        if (tenantDomain == null || "".equals(tenantDomain.trim())) {
            try {
                tenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getSuperTenantDomain();
            } catch (org.wso2.carbon.user.core.UserStoreException e) {
                handleException("Cannot get super tenant domain name", e);
            }
        }

		//get the registry instance related to the tenant domain
		UserRegistry govRegistry = null;
		try {
			ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
			int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
			                                     .getTenantId(tenantDomain);
			RegistryService registryService =
					ServiceReferenceHolder.getInstance()
					                      .getRegistryService();
			govRegistry = registryService.getGovernanceSystemRegistry(tenantId);

		} catch (UserStoreException e) {
			handleException("Cannot get tenant id for tenant domain name:"+tenantDomain,e);
		} catch (RegistryException e) {
			handleException("Cannot get registry for tenant domain name:"+tenantDomain,e);
		}

		if (govRegistry != null) {
		for (Tag tag : tags) {
			// Get the description.
			Resource descriptionResource = null;
			String descriptionPath = String.format(descriptionPathPattern, tag.getName());
			try {
				if (govRegistry.resourceExists(descriptionPath)) {
					descriptionResource = govRegistry.get(descriptionPath);
				}
			} catch (RegistryException e) {
				//warn and proceed to the next tag
				log.warn(String.format("Error while querying the existence of the description for the tag '%s'", tag.getName()));
			}
			// The resource is assumed to be a byte array since its the content
			// of a text file.
			if (descriptionResource != null) {
				try {
					String description = new String((byte[]) descriptionResource.getContent());
					tag.setDescription(description);

				} catch (ClassCastException e) {
					//added warnings as it can then proceed to load rest of resources/tags
					log.warn(String.format("Cannot cast content of %s to byte[]", descriptionPath),
					                e);
				}catch (RegistryException e) {
					//added warnings as it can then proceed to load rest of resources/tags
					log.warn(String.format("Cannot read content of %s", descriptionPath),
					                e);
				}
			}
			// Checks whether the thumbnail exists.
			String thumbnailPath = String.format(thumbnailPathPattern, tag.getName());
			try {
				tag.setThumbnailExists(govRegistry.resourceExists(thumbnailPath));
			} catch (RegistryException e) {
				//warn and then proceed to load rest of tags
				log.warn(String.format("Error while querying the existence of %s", thumbnailPath),
				         e);
			}
		}
		}
		return tags;
	}

    public void rateAPI(APIIdentifier apiId, APIRating rating,
                        String user) throws APIManagementException {
        apiMgtDAO.addRating(apiId, rating.getRating(), user);

    }

    public void removeAPIRating(APIIdentifier apiId, String user) throws APIManagementException {
        apiMgtDAO.removeAPIRating(apiId, user);

    }

    public int getUserRating(APIIdentifier apiId, String user) throws APIManagementException {
        return apiMgtDAO.getUserRating(apiId, user);
    }

    public Set<API> getPublishedAPIsByProvider(String providerId, int limit)
            throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        try {
            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
            Boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
            String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    providerId;
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            Association[] associations = registry.getAssociations(providerPath,
                    APIConstants.PROVIDER_ASSOCIATION);
            if (associations.length < limit || limit == -1) {
                limit = associations.length;
            }
            for (int i = 0; i < limit; i++) {
                Association association = associations[i];
                String apiPath = association.getDestinationPath();
                Resource resource = registry.get(apiPath);
                String apiArtifactId = resource.getUUID();
                if (apiArtifactId != null) {
                    GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
                    // check the API status
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    API api = null;
                    //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
                    if (!displayAPIsWithMultipleStatus) {
                        // then we are only interested in published APIs here...
                        if (status.equals(APIConstants.PUBLISHED)) {
                            api = APIUtil.getAPI(artifact);
                        }
                    } else {   // else we are interested in both deprecated/published APIs here...
                        if (status.equals(APIConstants.PUBLISHED) || status.equals(APIConstants.DEPRECATED)) {
                            api = APIUtil.getAPI(artifact);

                        }

                    }
                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                            API existingAPI = latestPublishedAPIs.get(key);
                            if (existingAPI != null) {
                                // If we have already seen an API with the same name, make sure
                                // this one has a higher version number
                                if (versionComparator.compare(api, existingAPI) > 0) {
                                    latestPublishedAPIs.put(key, api);
                                }
                            } else {
                                // We haven't seen this API before
                                latestPublishedAPIs.put(key, api);
                            }
                        } else { //If allow showing multiple versions of an API
                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                                    .getVersion();
                            multiVersionedAPIs.add(api);
                        }
                    }
                } else {
                    throw new GovernanceException("artifact id is null of " + apiPath);
                }
            }
            if (!displayMultipleVersions) {
                for (API api : latestPublishedAPIs.values()) {
                    apiSortedSet.add(api);
                }
                return apiSortedSet;
            } else {
                for (API api : multiVersionedAPIs) {
                    apiVersionsSortedSet.add(api);
                }
                return apiVersionsSortedSet;
            }

        } catch (RegistryException e) {
            handleException("Failed to get Published APIs for provider : " + providerId, e);
            return null;
        }
    }

    public Set<API> getPublishedAPIsByProvider(String providerId, String loggedUsername, int limit, String apiOwner,
                                               String apiBizOwner) throws APIManagementException {

        try {
            Boolean allowMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
            Boolean showAllAPIs = APIUtil.isAllowDisplayAPIsWithMultipleStatus();

            String providerDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerId));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(providerDomain);
            Registry registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceSystemRegistry(tenantId);

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            int publishedAPICount = 0;

            Map<String, API> apiCollection = new HashMap<String, API>();

            if(apiBizOwner != null && !apiBizOwner.isEmpty()){

                try {

                    //PaginationContext.init(0, limit, "DESC", "timestamp", limit);

                    final String bizOwner = apiBizOwner;

                    GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
                        public boolean matches(GenericArtifact artifact) throws GovernanceException {
                            if (artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER) != null) {
                                return bizOwner.matches(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
                            }
                            return false;
                        }
                    });

                    if(genericArtifacts != null && genericArtifacts.length > 0){

                        for(GenericArtifact artifact : genericArtifacts){
                            if (publishedAPICount >= limit) {
                                break;
                            }

                            if(isCandidateAPI(artifact.getPath(), loggedUsername, artifactManager, tenantId, showAllAPIs,
                                              allowMultipleVersions, apiOwner, providerId, registry, apiCollection)){
                                publishedAPICount += 1;
                            }
                        }
                    }
                } catch (GovernanceException e) {
                    log.error("Error while finding APIs by business owner " + apiBizOwner, e);
                    return null;
                } finally {
                    //PaginationContext.destroy();
                }
            }
            else{
                String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        providerId;

                Association[] associations = registry.getAssociations(providerPath,
                        APIConstants.PROVIDER_ASSOCIATION);

                for (Association association1 : associations) {
                    if (publishedAPICount >= limit) {
                        break;
                    }

                    Association association = association1;
                    String apiPath = association.getDestinationPath();

                    if(isCandidateAPI(apiPath, loggedUsername, artifactManager, tenantId, showAllAPIs,
                            allowMultipleVersions, apiOwner, providerId, registry, apiCollection)){

                        publishedAPICount += 1;
                    }
                }
            }

            return new HashSet<API>(apiCollection.values());

        } catch (RegistryException e) {
            handleException("Failed to get Published APIs for provider : " + providerId, e);
            return null;
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            handleException("Failed to get Published APIs for provider : " + providerId, e);
            return null;
        } catch (UserStoreException e) {
            handleException("Failed to get Published APIs for provider : " + providerId, e);
            return null;
        }
    }

    private boolean isCandidateAPI(String apiPath, String loggedUsername, GenericArtifactManager artifactManager,
                                   int tenantId, boolean showAllAPIs, boolean allowMultipleVersions,
                                   String apiOwner, String providerId, Registry registry, Map<String, API> apiCollection)
            throws UserStoreException, RegistryException, APIManagementException {

        AuthorizationManager manager = ServiceReferenceHolder.getInstance().getRealmService().
                                                getTenantUserRealm(tenantId).getAuthorizationManager();
        Comparator<API> versionComparator = new APIVersionComparator();

        Resource resource;
        String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                        apiPath);
        boolean checkAuthorized = false;
        String userNameWithoutDomain = loggedUsername;

        String loggedDomainName = "";
        if (!"".equals(loggedUsername) &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(super.tenantDomain)) {
            String[] nameParts = loggedUsername.split("@");
            loggedDomainName = nameParts[1];
            userNameWithoutDomain = nameParts[0];
        }

        if (loggedUsername.equals("")) {
            // Anonymous user is viewing.
            checkAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
        } else {
            // Some user is logged in.
            checkAuthorized = manager.isUserAuthorized(userNameWithoutDomain, path, ActionConstants.GET);
        }

        String apiArtifactId = null;
        if (checkAuthorized) {
            resource = registry.get(apiPath);
            apiArtifactId = resource.getUUID();
        }

        if (apiArtifactId != null) {
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);

            // check the API status
            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

            API api = null;
            //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
            if (!showAllAPIs) {
                // then we are only interested in published APIs here...
                if (status.equals(APIConstants.PUBLISHED)) {
                    api = APIUtil.getAPI(artifact);
                }
            } else {   // else we are interested in both deprecated/published APIs here...
                if (status.equals(APIConstants.PUBLISHED) || status.equals(APIConstants.DEPRECATED)) {
                    api = APIUtil.getAPI(artifact);
                }

            }
            if (api != null) {
                // apiOwner is the value coming from front end and compared against the API instance
                if (apiOwner != null && !apiOwner.isEmpty()) {
                    if (APIUtil.replaceEmailDomainBack(providerId)
                            .equals(APIUtil.replaceEmailDomainBack(apiOwner)) &&
                            api.getApiOwner() != null && !api.getApiOwner().isEmpty() &&
                            !APIUtil.replaceEmailDomainBack(apiOwner)
                                    .equals(APIUtil.replaceEmailDomainBack(api.getApiOwner()))) {
                        return false; // reject remote APIs when local admin user's API selected
                    } else if (!APIUtil.replaceEmailDomainBack(providerId).equals(
                            APIUtil.replaceEmailDomainBack(apiOwner)) &&
                            !APIUtil.replaceEmailDomainBack(apiOwner)
                                    .equals(APIUtil.replaceEmailDomainBack(api.getApiOwner()))) {
                        return false; // reject local admin's APIs when remote API selected
                    }
                }
                String key;
                //Check the configuration to allow showing multiple versions of an API true/false
                if (!allowMultipleVersions) { //If allow only showing the latest version of an API
                    key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                    API existingAPI = apiCollection.get(key);
                    if (existingAPI != null) {
                        // If we have already seen an API with the same name, make sure
                        // this one has a higher version number
                        if (versionComparator.compare(api, existingAPI) > 0) {
                            apiCollection.put(key, api);
                            return true;
                        }
                    } else {
                        // We haven't seen this API before
                        apiCollection.put(key, api);
                        return true;
                    }
                } else { //If allow showing multiple versions of an API
                    key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                            .getVersion();
                    //we're not really interested in the key, so generate one for the sake of adding this element to
                    //the map.
                    key = key + "_" + apiCollection.size();
                    apiCollection.put(key, api);
                    return true;
                }
            }
        }
        return false;
    }

    private void getAPIsByBusinessOwner(Registry registry, final String businessOwner){

        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(APIConstants.API_OVERVIEW_BUSS_OWNER, new ArrayList<String>() {{
            add(businessOwner);
        }});

        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);

            if(genericArtifacts != null && genericArtifacts.length > 0){
                //Set<API>
                for(GenericArtifact artifact : genericArtifacts){
                    artifact.getPath();
                }
            }

        } catch (APIManagementException e) {
            e.printStackTrace();
        } catch (GovernanceException e) {
            e.printStackTrace();
        }
    }

    public Map<String,Object> searchPaginatedAPIs(String searchTerm, String searchType, String requestedTenantDomain,int start,int end)
            throws APIManagementException {
        Map<String,Object> result = new HashMap<String,Object>();
        try {
            Registry userRegistry;
            boolean isTenantMode=(requestedTenantDomain != null);
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

            if (APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchType)) {
            	Map<Documentation, API> apiDocMap = APIUtil.searchAPIsByDoc(userRegistry, tenantIDLocal, userNameLocal, searchTerm, searchType);
            	result.put("apis", apiDocMap);
            	/*Pagination for Document search results is not supported yet, hence length is sent as end-start*/
            	if (apiDocMap.size() == 0 ) {
            		result.put("length", 0);
            	} else {
            		result.put("length", end-start);
            	}
        	}
            else if ("subcontext".equalsIgnoreCase(searchType)) {
                result = APIUtil.searchAPIsByURLPattern(userRegistry, searchTerm, start,end);               ;

            }else {
            	result=searchPaginatedAPIs(userRegistry, searchTerm, searchType,start,end);
            }

        } catch (Exception e) {
            handleException("Failed to Search APIs", e);
        }
        return result;
    }

    /**
	 * Pagination API search based on solr indexing
	 *
	 * @param registry
	 * @param searchTerm
	 * @param searchType
	 * @return
	 * @throws APIManagementException
	 */

    public Map<String,Object> searchPaginatedAPIs(Registry registry, String searchTerm, String searchType,int start,int end) throws APIManagementException {
        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        List<API> apiList = new ArrayList<API>();

        searchTerm = searchTerm.trim();
        Map<String,Object> result=new HashMap<String, Object>();
        int totalLength=0;
        String criteria=APIConstants.API_OVERVIEW_NAME;
        try {

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            PaginationContext.init(0, 10000, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);
            if (artifactManager != null) {

                if (searchType.equalsIgnoreCase("Provider")) {
                    criteria=APIConstants.API_OVERVIEW_PROVIDER;
                    searchTerm = searchTerm.replaceAll("@", "-AT-");
                } else if (searchType.equalsIgnoreCase("Version")) {
                    criteria=APIConstants.API_OVERVIEW_VERSION;
                } else if (searchType.equalsIgnoreCase("Context")) {
                    criteria=APIConstants.API_OVERVIEW_CONTEXT;
                }else if (searchType.equalsIgnoreCase("Description")) {
                    criteria=APIConstants.API_OVERVIEW_DESCRIPTION;
                }

                //Create the search attribute map for PUBLISHED APIs
                final String searchValue = searchTerm;
                Map<String, List<String>> listMap = new HashMap<String, List<String>>();
                listMap.put(criteria, new ArrayList<String>() {{
                    add(searchValue);
                }});

                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength = PaginationContext.getInstance().getLength();

                boolean isFound = true;
                if (genericArtifacts == null || genericArtifacts.length == 0) {

                    if (criteria.equals(APIConstants.API_OVERVIEW_PROVIDER)) {
                        genericArtifacts = searchAPIsByOwner(artifactManager, searchValue);

                        if (genericArtifacts == null || genericArtifacts.length == 0) {
                            isFound = false;
                        }
                    }
                    else {
                        isFound = false;
                    }
                }

                if (!isFound) {
                    result.put("apis", apiSet);
                    result.put("length", 0);
                    return result;
                }

                for (GenericArtifact artifact : genericArtifacts) {
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    if (APIUtil.isAllowDisplayAPIsWithMultipleStatus()) {
                        if (status.equals(APIConstants.PUBLISHED) || status.equals(APIConstants.DEPRECATED)) {
                            apiList.add(APIUtil.getAPI(artifact, registry));
                        }
                    } else {
                        if (status.equals(APIConstants.PUBLISHED)) {
                            apiList.add(APIUtil.getAPI(artifact, registry));
                        }
                    }
                    totalLength=apiList.size();
                }
                if(totalLength<=((start+end)-1)){
                    end=totalLength;
                }
				for (int i = start; i < end; i++) {
					apiSet.add(apiList.get(i));

				}
            }
        } catch (RegistryException e) {
            handleException("Failed to search APIs with type", e);
        }
        result.put("apis",apiSet);
        result.put("length",totalLength);
        return result;
    }
    

    private  GenericArtifact[] searchAPIsByOwner(GenericArtifactManager artifactManager, final String searchValue) throws GovernanceException {
        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(APIConstants.API_OVERVIEW_OWNER, new ArrayList<String>() {
            {
                add(searchValue);
            }});
        return artifactManager.findGenericArtifacts(listMap);
    }

    /**
     *This method will delete application key mapping table and application registration table.
     *@param applicationId application id
     *@param tokenType Token Type.
     *@return
     *@throws APIManagementException
     */
    public void deleteFromApplicationRegistration(String applicationId ,String tokenType) throws
            APIManagementException{

        apiMgtDAO.deleteApplicationRegistration(applicationId , tokenType);
        apiMgtDAO.deleteApplicationKeyMappingByApplicationIdAndType(applicationId , tokenType);

    }

    /**
     *
     * @param jsonString this string will contain oAuth app details
     * @param userName user name of logged in user.
     * @param clientId this is the consumer key of oAuthApplication
     * @param applicationName this is the APIM appication name.
     * @param keyType
     *@param allowedDomainArray @return
     * @throws APIManagementException
     */
    public Map<String, Object> mapExistingOAuthClient(String jsonString, String userName, String clientId,
                                                      String applicationName, String keyType,
                                                      String[] allowedDomainArray) throws APIManagementException {

        String callBackURL = null;

        OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, clientId, callBackURL,
                                                                                 "default",
                                                                                  jsonString);

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        //createApplication on oAuthorization server.
        OAuthApplicationInfo oAuthApplication = keyManager.mapOAuthApplication(oauthAppRequest);

        AccessTokenRequest tokenRequest = ApplicationUtils.createAccessTokenRequest(oAuthApplication,null);
        AccessTokenInfo tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);

        //Do application mapping with consumerKey.
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(keyType, applicationName, userName, clientId);

        apiMgtDAO.addAccessAllowDomains(clientId, allowedDomainArray);

        //#TODO get actuall values from response and pass.
        Map<String, Object> keyDetails = new HashMap<String, Object>();

        if(tokenInfo != null){
            keyDetails.put("validityTime", Long.toString(tokenInfo.getValidityPeriod()));
            keyDetails.put("accessToken", tokenInfo.getAccessToken());
        }

        if(oAuthApplication != null){
            keyDetails.put("consumerKey", oAuthApplication.getClientId());
            keyDetails.put("consumerSecret", oAuthApplication.getParameter("client_secret"));
            keyDetails.put("appDetails",oAuthApplication.getJsonString());
        }

        return keyDetails;

    }
    
    /**
    *
    * @param jsonString this string will contain oAuth app details
    * @param userName user name of logged in user.
    * @param clientId this is the consumer key of oAuthApplication
    * @param applicationName this is the APIM appication name.
    * @return
    * @throws APIManagementException
    */
   /*public Map<String, Object> saveSemiManualClient(String jsonString, String userName, String clientId,
                                                   String applicationName) throws APIManagementException {

       String callBackURL = null;

       OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, callBackURL,"default",
                                                                                 jsonString);

       KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
       //createApplication on oAuthorization server.
       OAuthApplicationInfo oAuthApplication = keyManager.mapOAuthApplication(oauthAppRequest);

       AccessTokenRequest tokenRequest = ApplicationUtils.createAccessTokenRequest(oAuthApplication,null);
       AccessTokenInfo tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);

       //Do application mapping with consumerKey.
       apiMgtDAO.createApplicationKeyTypeMappingForManualClients(oauthAppRequest, applicationName, userName, clientId);

       //in semi manual mode we are allowing ALL domains.
       String[] domainArray = {"ALL"};
       apiMgtDAO.addAccessAllowDomains(clientId, domainArray);

       //#TODO get actuall values from response and pass.
       Map<String, Object> keyDetails = new HashMap<String, Object>();

       if(tokenInfo != null){
           keyDetails.put("validityTime", Long.toString(tokenInfo.getValidityPeriod()));
           keyDetails.put("accessToken", tokenInfo.getAccessToken());
       }

       if(oAuthApplication != null){
           keyDetails.put("consumerKey", oAuthApplication.getClientId());
           keyDetails.put("consumerSecret", oAuthApplication.getParameter("client_secret"));
           keyDetails.put("appDetails",oAuthApplication.getJsonString());
       }

       return keyDetails;

   }
*/
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = getSubscribedAPIs(subscriber, null);
        return subscribedAPIs;
    }

    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> originalSubscribedAPIs = null;
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        try {
            originalSubscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, groupingId);
            if (originalSubscribedAPIs != null && !originalSubscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : originalSubscribedAPIs) {
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi.getTier().getName());
                    subscribedAPIs.add(subscribedApi);
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName(), e);
        }
        return subscribedAPIs;
    }

     public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
        	subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, applicationName, groupingId);
            if(subscribedAPIs!=null && !subscribedAPIs.isEmpty()){
            	Map<String, Tier> tiers=APIUtil.getTiers(tenantId);
            	for(SubscribedAPI subscribedApi:subscribedAPIs) {
            		Tier tier=tiers.get(subscribedApi.getTier().getName());
	                subscribedApi.getTier().setDisplayName(tier!=null?tier.getDisplayName():subscribedApi.getTier().getName());
	                subscribedAPIs.add(subscribedApi);
	            }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName() + " under application " + applicationName, e);
        }
        return subscribedAPIs;
    }

    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber, String applicationName, int startSubIndex, int endSubIndex, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getPaginatedSubscribedAPIs(subscriber, applicationName, startSubIndex,endSubIndex, groupingId);
            if(subscribedAPIs!=null && !subscribedAPIs.isEmpty()){
                Map<String, Tier> tiers=APIUtil.getTiers(tenantId);
                for(SubscribedAPI subscribedApi:subscribedAPIs) {
                    Tier tier=tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier!=null?tier.getDisplayName():subscribedApi.getTier().getName());
                    subscribedAPIs.add(subscribedApi);
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName() + " under application " + applicationName, e);
        }
        return subscribedAPIs;
    }
    
    public Integer getSubscriptionCount(Subscriber subscriber,String applicationName,String groupingId)
            throws APIManagementException {
        return apiMgtDAO.getSubscriptionCount(subscriber,applicationName,groupingId);
    }

    public Set<APIIdentifier> getAPIByConsumerKey(String accessToken) throws APIManagementException {
        try {
            return apiMgtDAO.getAPIByConsumerKey(accessToken);
        } catch (APIManagementException e) {
            handleException("Error while obtaining API from API key", e);
        }
        return null;
    }

    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId)
            throws APIManagementException {
        boolean isSubscribed;
        try {
            isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, userId);
        } catch (APIManagementException e) {
            String msg = "Failed to check if user(" + userId + ") has subscribed to " + apiIdentifier;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return isSubscribed;
    }

    public String addSubscription(APIIdentifier apiId, String userId, int applicationId)
            throws APIManagementException {
        APIIdentifier identifier = getAPIIdentifier(apiId, userId);
        API api = getAPI(identifier);
        if (api.getStatus().equals(APIStatus.PUBLISHED)) {
            int subscriptionId = apiMgtDAO.addSubscription(identifier, api.getContext(), applicationId,
                                                           APIConstants.SubscriptionStatus.ON_HOLD);

            boolean isTenantFlowStarted = false;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            try {

                WorkflowExecutor addSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance().
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

                SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
                workflowDTO.setStatus(WorkflowStatus.CREATED);
                workflowDTO.setCreatedTime(System.currentTimeMillis());
                workflowDTO.setTenantDomain(tenantDomain);
                workflowDTO.setTenantId(tenantId);
                workflowDTO.setExternalWorkflowReference(addSubscriptionWFExecutor.generateUUID());
                workflowDTO.setWorkflowReference(String.valueOf(subscriptionId));
                workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
                workflowDTO.setCallbackUrl(addSubscriptionWFExecutor.getCallbackURL());
                workflowDTO.setApiName(identifier.getApiName());
                workflowDTO.setApiContext(api.getContext());
                workflowDTO.setApiVersion(identifier.getVersion());
                workflowDTO.setApiProvider(identifier.getProviderName());
                workflowDTO.setTierName(identifier.getTier());
                workflowDTO.setApplicationName(apiMgtDAO.getApplicationNameFromId(applicationId));
                workflowDTO.setSubscriber(userId);
                addSubscriptionWFExecutor.execute(workflowDTO);
            } catch (WorkflowException e) {
                //If the workflow execution fails, roll back transaction by removing the subscription entry.
                apiMgtDAO.removeSubscriptionById(subscriptionId);
                log.error("Could not execute Workflow", e);
                throw new APIManagementException("Could not execute Workflow", e);
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            if (APIUtil.isAPIGatewayKeyCacheEnabled()) {
                invalidateCachedKeys(applicationId);
            }
            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + identifier.getApiName() + ", API Version " + identifier.getVersion() + " subscribe by " + userId + " for app " + apiMgtDAO.getApplicationNameFromId(applicationId);
                log.debug(logMessage);
            }

            return apiMgtDAO.getSubscriptionStatusById(subscriptionId);
        } else {
            throw new APIManagementException("Subscriptions not allowed on APIs in the state: " +
                                             api.getStatus().getStatus());
        }
    }

    public boolean removeSubscription(APIIdentifier identifier, String userId, int applicationId)
	    throws APIManagementException {
	try {
	    apiMgtDAO.removeSubscription(identifier, applicationId);
	    if (APIUtil.isAPIGatewayKeyCacheEnabled()) {
		invalidateCachedKeys(applicationId);
	    }
	    
	    if (log.isDebugEnabled()) {
		String appName = apiMgtDAO.getApplicationNameFromId(applicationId);
		String logMessage = "API Name: " + identifier.getApiName() + ", API Version " + identifier.getVersion()
			+ " subscription removed by " + userId + " from app " + appName;
		log.debug(logMessage);
	    }
	    return true;
	} catch (APIManagementException e) {
	    handleException(
		    "Error while removing the subscription of" + identifier.getApiName() + "-"
			    + identifier.getVersion(), e);
	    return false;
	}

    }

    private APIIdentifier getAPIIdentifier(APIIdentifier apiIdentifier, String userId)
            throws APIManagementException {
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            // Validation for allowed throttling tiers
            API api = getAPI(apiIdentifier);
            Set<Tier> tiers = api.getAvailableTiers();
            String tier = apiIdentifier.getTier();
            Iterator<Tier> iterator = tiers.iterator();
            boolean isTierAllowed = false;
            List<String> allowedTierList = new ArrayList<String>();
            while (iterator.hasNext()) {
                Tier t = iterator.next();
                if (t.getName() != null && (t.getName()).equals(tier)) {
                    isTierAllowed = true;
                }
                allowedTierList.add(t.getName());
            }
            if (!isTierAllowed) {
                throw new APIManagementException("Tier " + tier + " is not allowed for API " + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion() + ". Only "
                                                 + Arrays.toString(allowedTierList.toArray()) + " Tiers are alllowed.");
            }
            if (isTierDeneid(tier)) {
                throw new APIManagementException("Tier " + tier + " is not allowed for user " + userId);
            }
            // Tenant based validation for subscription
            String userDomain = MultitenantUtils.getTenantDomain(userId);
            boolean subscriptionAllowed = false;
            if (!userDomain.equals(tenantDomain)) {
                String subscriptionAvailability = api.getSubscriptionAvailability();
                if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                    subscriptionAllowed = true;
                } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                    String subscriptionAllowedTenants = api.getSubscriptionAvailableTenants();
                    String allowedTenants[] = null;
                    if (subscriptionAllowedTenants != null) {
                        allowedTenants = subscriptionAllowedTenants.split(",");
                        if (allowedTenants != null) {
                            for (String tenant : allowedTenants) {
                                if (tenant != null && userDomain.equals(tenant.trim())) {
                                    subscriptionAllowed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                subscriptionAllowed = true;
            }
            if (!subscriptionAllowed) {
                throw new APIManagementException("Subscription is not allowed for " + userDomain);
            }
            apiIdentifier.setTier(tier);

            return apiIdentifier;
        } catch (APIManagementException e) {
            handleException("Error while adding subscription for user: " + userId + " Reason: " + e.getMessage(), e);
            return null;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    /**
     *
     * @param applicationId Application ID related cache keys to be cleared
     * @throws APIManagementException
     */
    private void invalidateCachedKeys(int applicationId) throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config.getApiGatewayEnvironments().size() <= 0) {
            return;
        }

        Set<String> consumerKeys = apiMgtDAO.getConsumerKeysOfApplication(applicationId);

        Set<String> activeTokens = new HashSet<String>();
        for (String consumerKey : consumerKeys) {
                Set<String> tempTokens = KeyManagerHolder.getKeyManagerInstance().
                        getActiveTokensByConsumerKey(consumerKey);
                if (tempTokens != null) {
                    activeTokens.addAll(tempTokens);
                }
        }

        if (activeTokens == null || activeTokens.isEmpty()) {
            return;
        }

        Map<String, Environment> gatewayEnvs = config.getApiGatewayEnvironments();
        try {
            for (Environment environment : gatewayEnvs.values()) {
                APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);
                client.invalidateCachedTokens(activeTokens);
            }
        } catch (AxisFault axisFault) {
            //log and ignore since we do not have to halt the user operation due to cache invalidation failures.
            log.error("Error occurred while invalidating the Gateway Token Cache ", axisFault);
        }
    }

    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException {
        throw new UnsupportedOperationException("Unsubscribe operation is not yet implemented");
    }

    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        API api = getAPI(identifier);
        apiMgtDAO.updateSubscriptions(identifier, api.getContext(), applicationId);
    }

    public void addComment(APIIdentifier identifier, String commentText, String user) throws APIManagementException {
        apiMgtDAO.addComment(identifier, commentText, user);
    }

    public org.wso2.carbon.apimgt.api.model.Comment[] getComments(APIIdentifier identifier)
            throws APIManagementException {
        return apiMgtDAO.getComments(identifier);
    }

    /**
     * Add a new Application from the store.
     * @param application - {@link org.wso2.carbon.apimgt.api.model.Application}
     * @param userId - {@link String}
     * @return {@link String}
     */

    public String addApplication(Application application, String userId)
            throws APIManagementException {
        
        if (APIUtil.isApplicationExist(userId, application.getName(), application.getGroupId())) {
            handleException("A duplicate application already exists by the name - " + application.getName());
        }
        
        int applicationId = apiMgtDAO.addApplication(application, userId);

        boolean isTenantFlowStarted = false;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isTenantFlowStarted = true;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }

        try {

            WorkflowExecutor appCreationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            ApplicationWorkflowDTO appWFDto = new ApplicationWorkflowDTO();
            appWFDto.setApplication(application);

            appWFDto.setExternalWorkflowReference(appCreationWFExecutor.generateUUID());
            appWFDto.setWorkflowReference(String.valueOf(applicationId));
            appWFDto.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            appWFDto.setCallbackUrl(appCreationWFExecutor.getCallbackURL());
            appWFDto.setStatus(WorkflowStatus.CREATED);
            appWFDto.setTenantDomain(tenantDomain);
            appWFDto.setTenantId(tenantId);
            appWFDto.setUserName(userId);
            appWFDto.setCreatedTime(System.currentTimeMillis());

            appCreationWFExecutor.execute(appWFDto);
        } catch (WorkflowException e) {
            //If the workflow execution fails, roll back transaction by removing the application entry.
            application.setId(applicationId);
            apiMgtDAO.deleteApplication(application);
            log.error("Unable to execute Application Creation Workflow", e);
            handleException("Unable to execute Application Creation Workflow", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        String status = apiMgtDAO.getApplicationStatus(application.getName(), userId);
        return status;
    }

    public void updateApplication(Application application) throws APIManagementException {
        Application app = apiMgtDAO.getApplicationById(application.getId());
        if(app != null && APIConstants.ApplicationStatus.APPLICATION_CREATED.equals(app.getStatus())){
            throw new APIManagementException("Cannot update the application while it is INACTIVE");
        }

        apiMgtDAO.updateApplication(application);

        try{
            invalidateCachedKeys(application.getId());
        }catch(APIManagementException ignore){
            //Log and ignore since we do not want to throw exceptions to the front end due to cache invalidation failure.
            log.warn("Failed to invalidate Gateway Cache " + ignore.getMessage());
        }
    }


    /**
     * Function to remove an Application from the API Store
     * @param application - The Application Object that represents the Application
     * @throws APIManagementException
     */
    public void removeApplication(Application application) throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        try {
            //Remove from cache first since we won't be able to find active access tokens once the application is removed.
            invalidateCachedKeys(application.getId());
        }catch(APIManagementException ignore){
            //Log and proceed since we do not want to halt the application delete due to cache invalidation failures.
            log.warn("Failed to invalidate Gateway Cache " + ignore.getMessage());
        }

        apiMgtDAO.deleteApplication(application);
    }

    /**
     * @param userId Subsriber name.
     * @param applicationName of the Application.
     * @param tokenType Token type (PRODUCTION | SANDBOX)
     * @param jsonString oAuthApplication parameters as a json string.
     * @return
     * @throws APIManagementException
     */
    @Override
    public Map<String, Object> requestApprovalForApplicationRegistration(String userId, String applicationName,
                                                                         String tokenType, String callbackUrl,
                                                                         String[] allowedDomains, String validityTime,
                                                                         String tokenScope, String groupingId,
                                                                         String jsonString)
            throws APIManagementException {

        boolean isTenantFlowStarted = false;
        // we should have unique names for applications. There for we will
        // append, the word 'production' or 'sandbox'
        // according to the token type.
        StringBuilder applicationNameAfterAppend = new StringBuilder(applicationName);

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            // initiate WorkflowExecutor
            WorkflowExecutor appRegistrationWorkflow = null;
            // initiate ApplicationRegistrationWorkflowDTO
            ApplicationRegistrationWorkflowDTO appRegWFDto = null;

            ApplicationKeysDTO appKeysDto = new ApplicationKeysDTO();

            // get APIM application by Application Name and userId.
            Application application = ApplicationUtils.retrieveApplication(applicationName, userId, groupingId);

            // if its a PRODUCTION application.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                // initiate workflow type. By default simple work flow will be
                // executed.
                appRegistrationWorkflow =
                        WorkflowExecutorFactory.getInstance()
                                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
                appRegWFDto =
                        (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                                .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
                applicationNameAfterAppend.append("_PRODUCTION");

            }// if it is a sandBox application.
            else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) { // if
                // its
                // a
                // SANDBOX
                // application.
                appRegistrationWorkflow =
                        WorkflowExecutorFactory.getInstance()
                                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
                appRegWFDto =
                        (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                                .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
                applicationNameAfterAppend.append("_SANDBOX");
            }
            // Build key manager instance and create oAuthAppRequest by
            // jsonString.
            OAuthAppRequest request =
                    ApplicationUtils.createOauthAppRequest(applicationNameAfterAppend.toString(), null,
                                                           callbackUrl, tokenScope, jsonString);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.VALIDITY_PERIOD, validityTime);

            // Setting request values in WorkflowDTO - In future we should keep
            // Application/OAuthApplication related
            // information in the respective entities not in the workflowDTO.
            appRegWFDto.setStatus(WorkflowStatus.CREATED);
            appRegWFDto.setCreatedTime(System.currentTimeMillis());
            appRegWFDto.setTenantDomain(tenantDomain);
            appRegWFDto.setTenantId(tenantId);
            appRegWFDto.setExternalWorkflowReference(appRegistrationWorkflow.generateUUID());
            appRegWFDto.setWorkflowReference(appRegWFDto.getExternalWorkflowReference());
            appRegWFDto.setApplication(application);
            request.setMappingId(appRegWFDto.getWorkflowReference());
            if (!application.getSubscriber().getName().equals(userId)) {
                appRegWFDto.setUserName(application.getSubscriber().getName());
            } else {
                appRegWFDto.setUserName(userId);
            }

            appRegWFDto.setCallbackUrl(appRegistrationWorkflow.getCallbackURL());
            appRegWFDto.setAppInfoDTO(request);
            appRegWFDto.setDomainList(allowedDomains);

            appRegWFDto.setKeyDetails(appKeysDto);
            appRegistrationWorkflow.execute(appRegWFDto);

            Map<String, Object> keyDetails = new HashMap<String, Object>();
            keyDetails.put("keyState", appRegWFDto.getStatus().toString());
            OAuthApplicationInfo applicationInfo = appRegWFDto.getApplicationInfo();

            if (applicationInfo != null) {
                keyDetails.put("consumerKey", applicationInfo.getClientId());
                keyDetails.put("consumerSecret", applicationInfo.getClientSecret());
                keyDetails.put("appDetails", APIUtil.convertToString(applicationInfo));
            }

            // There can be instances where generating the Application Token is
            // not required. In those cases,
            // token info will have nothing.
            AccessTokenInfo tokenInfo = appRegWFDto.getAccessTokenInfo();
            if (tokenInfo != null) {
                keyDetails.put("accessToken", tokenInfo.getAccessToken());
                keyDetails.put("validityTime", tokenInfo.getValidityPeriod());
                keyDetails.put("tokenDetails", APIUtil.convertToString(tokenInfo));
                keyDetails.put("tokenScope", StringUtils.join(tokenInfo.getScopes(),","));
            }
            return keyDetails;
        } catch (WorkflowException e) {
            log.error("Could not execute Workflow", e);
            throw new APIManagementException("Could not execute Workflow", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    public Map<String, String> completeApplicationRegistration(String userId, String applicationName, String tokenType,
                                                               String tokenScope, String groupingId)
                                                                                                    throws APIManagementException {

    	Application application = apiMgtDAO.getApplicationByName(applicationName, null, groupingId);
        String status = apiMgtDAO.getRegistrationApprovalState(application.getId(), tokenType);
        Map<String, String> keyDetails = null;
        if(!application.getSubscriber().getName().equals(userId)){
            userId = application.getSubscriber().getName();
        }
        String workflowReference = apiMgtDAO.getWorkflowReference(applicationName, userId);
        if (workflowReference != null) {
            WorkflowDTO workflowDTO = null;

            // Creating workflowDTO for the correct key type.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(WorkflowConstants
                                                                                              .WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            } else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) {
                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(WorkflowConstants
                                                                                              .WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
            }
            if (workflowDTO != null) {

                // Set the workflow reference in the workflow dto and the populate method will fill in other details
                // using the persisted request.
                ApplicationRegistrationWorkflowDTO registrationWorkflowDTO = (ApplicationRegistrationWorkflowDTO)
                        workflowDTO;
                registrationWorkflowDTO.setExternalWorkflowReference(workflowReference);

                if (APIConstants.AppRegistrationStatus.REGISTRATION_APPROVED.equals(status)) {
                    apiMgtDAO.populateAppRegistrationWorkflowDTO(registrationWorkflowDTO);
                    try {
                        AbstractApplicationRegistrationWorkflowExecutor.dogenerateKeysForApplication(registrationWorkflowDTO);
                        AccessTokenInfo tokenInfo = registrationWorkflowDTO.getAccessTokenInfo();
                        OAuthApplicationInfo oauthApp = registrationWorkflowDTO.getApplicationInfo();
                        keyDetails = new HashMap<String, String>();

                        if(tokenInfo != null){
                            keyDetails.put("accessToken", tokenInfo.getAccessToken());
                            keyDetails.put("validityTime", Long.toString(tokenInfo.getValidityPeriod()));
                            keyDetails.put("tokenDetails",tokenInfo.getJSONString());
                        }

                        keyDetails.put("consumerKey", oauthApp.getClientId());
                        keyDetails.put("consumerSecret", oauthApp.getClientSecret());
                        keyDetails.put("accessallowdomains", registrationWorkflowDTO.getDomainList());
                        keyDetails.put("appDetails", oauthApp.getJsonString());
                    } catch (APIManagementException e) {
                        APIUtil.handleException("Error occurred while Creating Keys.", e);
                    }
                }

            }
        }
        return keyDetails;
    }

    /**
     *
     * @param userId APIM subscriber user ID.
     * @param ApplicationName APIM application name.
     * @return
     * @throws APIManagementException
     */
    public Application getApplicationsByName(String userId, String ApplicationName, String groupingId) throws
            APIManagementException {

        return apiMgtDAO.getApplicationByName(ApplicationName, userId, groupingId);

    }

    public boolean isApplicationTokenExists(String accessToken) throws APIManagementException {
        return apiMgtDAO.isAccessTokenExists(accessToken);
    }

    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber, APIIdentifier identifier, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPISet = new HashSet<SubscribedAPI>();
        Set<SubscribedAPI> subscribedAPIs = getSubscribedAPIs(subscriber, groupingId);
        for (SubscribedAPI api : subscribedAPIs) {
            if (api.getApiId().equals(identifier)) {
                subscribedAPISet.add(api);
            }
        }
        return subscribedAPISet;
    }


    public void addAccessAllowDomains(String oauthConsumerKey, String[] accessAllowDomains)
            throws APIManagementException {
        apiMgtDAO.addAccessAllowDomains(oauthConsumerKey, accessAllowDomains);
    }


    public void updateAccessAllowDomains(String accessToken, String[] accessAllowDomains)
            throws APIManagementException {
        apiMgtDAO.updateAccessAllowDomains(accessToken, accessAllowDomains);
    }

    /**
     * Returns a list of tiers denied
     *
     * @return Set<Tier>
     */
    public Set<String> getDeniedTiers() throws APIManagementException {
        Set<String> deniedTiers = new HashSet<String>();
        String[] currentUserRoles = new String[0];
        try {
            if (tenantId != 0) {
                /* Get the roles of the Current User */
                currentUserRoles = ((UserRegistry) ((UserAwareAPIConsumer) this).registry).
                        getUserRealm().getUserStoreManager().getRoleListOfUser(((UserRegistry) this.registry).getUserName());

                Set<TierPermissionDTO> tierPermissions = apiMgtDAO.getTierPermissions(tenantId);
                for (TierPermissionDTO tierPermission : tierPermissions) {
                    String type = tierPermission.getPermissionType();

                    List<String> currentRolesList = new ArrayList<String>(Arrays.asList(currentUserRoles));
                    List<String> roles = new ArrayList<String>(Arrays.asList(tierPermission.getRoles()));
                    currentRolesList.retainAll(roles);

                    if (APIConstants.TIER_PERMISSION_ALLOW.equals(type)) {
                        /* Current User is not allowed for this Tier*/
                        if (currentRolesList.size() == 0) {
                            deniedTiers.add(tierPermission.getTierName());
                        }
                    } else {
                        /* Current User is denied for this Tier*/
                        if (currentRolesList.size() > 0) {
                            deniedTiers.add(tierPermission.getTierName());
                        }
                    }
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("cannot retrieve user role list for tenant" + tenantDomain);
        }
        return deniedTiers;
    }

    /**
     * Check whether given Tier is denied for the user
     *
     * @param tierName
     * @return
     * @throws APIManagementException if failed to get the tiers
     */
    public boolean isTierDeneid(String tierName) throws APIManagementException {
        String[] currentUserRoles = new String[0];
        try {
            if (tenantId != 0) {
                /* Get the roles of the Current User */
                currentUserRoles = ((UserRegistry) ((UserAwareAPIConsumer) this).registry).
                        getUserRealm().getUserStoreManager().getRoleListOfUser(((UserRegistry) this.registry).getUserName());
                TierPermissionDTO tierPermission = apiMgtDAO.getTierPermission(tierName, tenantId);
                if (tierPermission == null) {
                    return false;
                } else {
                    List<String> currentRolesList = new ArrayList<String>(Arrays.asList(currentUserRoles));
                    List<String> roles = new ArrayList<String>(Arrays.asList(tierPermission.getRoles()));
                    currentRolesList.retainAll(roles);
                    if (APIConstants.TIER_PERMISSION_ALLOW.equals(tierPermission.getPermissionType())) {
                        if (currentRolesList.size() == 0) {
                            return true;
                        }
                    } else {
                        if (currentRolesList.size() > 0) {
                            return true;
                        }
                    }
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("cannot retrieve user role list for tenant" + tenantDomain);
        }
        return false;
    }

    /**
     * Returned an API set from a set of registry paths
     *
     * @param registry Registry object from which the APIs retrieving,
     * @param limit    Specifies the number of APIs to add.
     * @param apiPaths Array of API paths.
     * @return Set<API> set of APIs
     * @throws RegistryException
     * @throws APIManagementException
     */
    private Set<API> getAPIs(Registry registry, int limit, String[] apiPaths)
            throws RegistryException, APIManagementException,
            org.wso2.carbon.user.api.UserStoreException {

        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        Boolean allowMultipleVersions =APIUtil.isAllowDisplayMultipleVersions();
        Boolean showAllAPIs = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
        Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
        List<API> multiVersionedAPIs = new ArrayList<API>();
        Comparator<API> versionComparator = new APIVersionComparator();

        //Find UUID
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                APIConstants.API_KEY);
        for (int a = 0; a < apiPaths.length; a++) {
            Resource resource = registry.get(apiPaths[a]);
            if (resource != null && artifactManager != null) {
                GenericArtifact genericArtifact = artifactManager.getGenericArtifact(resource.getUUID());
                API api = null;
                String status = genericArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
                if (!showAllAPIs) {
                    // then we are only interested in published APIs here...
                    if (status.equals(APIConstants.PUBLISHED)) {
                        api = APIUtil.getAPI(genericArtifact, registry);
                    }
                } else {   // else we are interested in both deprecated/published APIs here...
                    if (status.equals(APIConstants.PUBLISHED) || status.equals(APIConstants.DEPRECATED)) {
                        api = APIUtil.getAPI(genericArtifact, registry);

                    }

                }
                if (api != null) {
                    String key;
                    //Check the configuration to allow showing multiple versions of an API true/false
                    if (!allowMultipleVersions) { //If allow only showing the latest version of an API
                        key = api.getId().getProviderName() + ":" + api.getId().getApiName();
                        API existingAPI = latestPublishedAPIs.get(key);
                        if (existingAPI != null) {
                            // If we have already seen an API with the same name, make sure
                            // this one has a higher version number
                            if (versionComparator.compare(api, existingAPI) > 0) {
                                latestPublishedAPIs.put(key, api);
                            }
                        } else {
                            // We haven't seen this API before
                            latestPublishedAPIs.put(key, api);
                        }
                    } else { //If allow showing multiple versions of an API
                        key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
                                .getVersion();
                        multiVersionedAPIs.add(api);
                    }
                }

            }
        }
        if (!allowMultipleVersions) {
            for (API api : latestPublishedAPIs.values()) {
                apiSortedSet.add(api);
            }
            return apiSortedSet;
        } else {
            for (API api : multiVersionedAPIs) {
                apiVersionsSortedSet.add(api);
            }
            return apiVersionsSortedSet;
        }

    }

    private boolean isAllowDisplayAllAPIs() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String displayAllAPIs = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_ALL_APIS);
        if (displayAllAPIs == null) {
            log.warn("The configurations related to show deprecated APIs in APIStore " +
                    "are missing in api-manager.xml.");
            return false;
        }
        return Boolean.parseBoolean(displayAllAPIs);
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
    	if (this.tenantDomain != null) {
    		return !(this.tenantDomain.equals(tenantDomain));
    	}
    	return true;
    }

    public API getAPIInfo(APIIdentifier identifier)
            throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {


            Registry registry = getRegistry(identifier, apiPath);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : "+ apiPath);
            }
            GenericArtifactManager artifactManager = getGenericArtifactManager(identifier, registry);
            GovernanceArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPIInformation(apiArtifact, registry);
        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }

    }

    private GenericArtifactManager getGenericArtifactManager(APIIdentifier identifier, Registry registry)
            throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
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
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(id);
                passRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !this.tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    // explicitly load the tenant's registry
                    APIUtil.loadTenantRegistry(MultitenantConstants.SUPER_TENANT_ID);
                    passRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                            identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    passRegistry = this.registry;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get API from registry on path of : " +apiPath, e);
            return null;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get API from registry on path of : "+ apiPath, e);
            return null;
        }
        return passRegistry;
    }

	@Override
	public Set<API> searchAPI(String searchTerm, String searchType, String tenantDomain)
	                                                                                    throws APIManagementException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Scope> getScopesBySubscribedAPIs(List<APIIdentifier> identifiers)
			throws APIManagementException {
		return apiMgtDAO.getScopesBySubscribedAPIs(identifiers);
	}

	public String getScopesByToken(String accessToken) throws APIManagementException {
		return apiMgtDAO.getScopesByToken(accessToken);
	}

	public Set<Scope> getScopesByScopeKeys(String scopeKeys, int tenantId)
			throws APIManagementException {
		return apiMgtDAO.getScopesByScopeKeys(scopeKeys, tenantId);
	}

	@Override
    public String getGroupIds(String response) throws APIManagementException{
                String groupingExtractorClass = APIUtil.getGroupingExtractorImplementation();
                if (groupingExtractorClass != null) {
                        try {
                                LoginPostExecutor groupingExtractor = (LoginPostExecutor)Class.forName(groupingExtractorClass).newInstance();
                                return  groupingExtractor.getGroupingIdentifiers(response);
                            } catch (ClassNotFoundException e) {
                                handleException(groupingExtractorClass+" is not found in run time", e);
                                return null;
                            } catch (IllegalAccessException e) {
                                handleException("Error occurred while invocation of getGroupingIdentifier method", e);
                                return null;
                            } catch (InstantiationException e) {
                                handleException("Error occurred while instantiating "+groupingExtractorClass+" class", e);
                                return null;
                            }
                    }
                return null;
            }

	@Override
	public Application[] getApplications(Subscriber subscriber, String groupingId)
			throws APIManagementException {
		return apiMgtDAO.getApplications(subscriber, groupingId);
	}

    /**
     * @param userId Subsriber name.
     * @param applicationName of the Application.
     * @param tokenType Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl callback URL
     * @param allowedDomains allowedDomains for token.
     * @param validityTime validity time period.
     * @param groupingId APIM application id.
     * @param jsonString Callback URL for the Application.
     * @param tokenScope Scopes for the requested tokens.
     * @return
     * @throws APIManagementException
     */
    @Override
    public OAuthApplicationInfo updateAuthClient(String userId, String applicationName,
                                                String tokenType,
                                                String callbackUrl, String[] allowedDomains,
                                                String validityTime,
                                                String tokenScope,
                                                String groupingId,
                                                String jsonString) throws APIManagementException {

        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }
        //Create OauthAppRequest object by passing json String.
        OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, null, callbackUrl,
                                                                                 tokenScope, jsonString);

        String consumerKey = apiMgtDAO.getConsumerKeyForApplicationKeyType(applicationName, userId, tokenType,
                                                                           groupingId);

        oauthAppRequest.getOAuthApplicationInfo().setClientId(consumerKey);
        //get key manager instant.
        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        //call update method.
        return keyManager.updateApplication(oauthAppRequest);

    }

    /**
     * This method perform delete oAuth application.
     *
     * @param consumerKey
     * @throws APIManagementException
     */
    @Override
    public void deleteOAuthApplication(String consumerKey) throws APIManagementException {
        //get key manager instance.
        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        //delete oAuthApplication by calling key manager implementation
        keyManager.deleteApplication(consumerKey);

        Map<String, String> applicationIdAndTokenTypeMap =
                apiMgtDAO.getApplicationIdAndTokenTypeByConsumerKey(consumerKey);

        if (applicationIdAndTokenTypeMap != null) {
            String applicationId = applicationIdAndTokenTypeMap.get("application_id");
            String tokenType = applicationIdAndTokenTypeMap.get("token_type");

            if (applicationId != null && tokenType != null) {
                apiMgtDAO.deleteApplicationKeyMappingByConsumerKey(consumerKey);
                apiMgtDAO.deleteApplicationRegistration(applicationId, tokenType);
                apiMgtDAO.deleteAccessAllowDomains(consumerKey);
            }
        }
    }

   /*
   *  Generate a key for a subscribed Application - args[] list String subscriberID, String
   * application name, String keyType
   */
    public JSONObject generateApplicationKey(String username, String applicationName, String tokenType,
                                        String scopes, String validityPeriod, String callbackUrl,
                                        JSONArray accessAllowDomainsArr, String jsonParams,
                                        String groupingId)
            throws APIManagementException {

        String[] accessAllowDomainsArray = new String[accessAllowDomainsArr.size()];
        for (int i = 0; i < accessAllowDomainsArr.size(); i++) {
            accessAllowDomainsArray[i] = (String) accessAllowDomainsArr.get(i);
        }

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId =
                    ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                            .getTenantId(tenantDomain);

            if (null == validityPeriod || validityPeriod.isEmpty()) { // In case a validity period is unspecified
                long defaultValidityPeriod = APIUtil.getApplicationAccessTokenValidityPeriodInSeconds();

                if (defaultValidityPeriod < 0) {
                    validityPeriod = String.valueOf(Long.MAX_VALUE);
                } else {
                    validityPeriod = String.valueOf(defaultValidityPeriod);
                }
            }
            //checking for authorized scopes
            Set<Scope> scopeSet = new LinkedHashSet<Scope>();
            List<Scope> authorizedScopes = new ArrayList<Scope>();
            String authScopeString;
            if (scopes != null && scopes.length() != 0 &&
                !scopes.equals(APIConstants.OAUTH2_DEFAULT_SCOPE)) {
                scopeSet.addAll(getScopesByScopeKeys(scopes, tenantId));
                authorizedScopes = APIUtil.getAllowedScopesForUserApplication(username, scopeSet);
            }

            if (!authorizedScopes.isEmpty()) {
                StringBuilder scopeBuilder = new StringBuilder();
                for (Scope scope : authorizedScopes) {
                    scopeBuilder.append(scope.getKey()).append(" ");
                }
                authScopeString = scopeBuilder.toString();
            } else {
                authScopeString = APIConstants.OAUTH2_DEFAULT_SCOPE;
            }

            Map<String, Object> keyDetails = requestApprovalForApplicationRegistration(
                    username, applicationName, tokenType, callbackUrl,
                    accessAllowDomainsArray, validityPeriod, authScopeString,
                    groupingId, jsonParams);

            JSONObject row = new JSONObject();
            String authorizedDomains = "";
            boolean first = true;
            for (String anAccessAllowDomainsArray : accessAllowDomainsArray) {
                if (first) {
                    authorizedDomains = anAccessAllowDomainsArray;
                    first = false;
                } else {
                    authorizedDomains = authorizedDomains + ", " + anAccessAllowDomainsArray;
                }
            }

            Set<Map.Entry<String, Object>> entries = keyDetails.entrySet();

            for (Map.Entry<String, Object> entry : entries) {
                row.put(entry.getKey(), entry.getValue());
            }
            boolean isRegenarateOptionEnabled = true;
            if (APIUtil.getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                isRegenarateOptionEnabled = false;
            }
            row.put("enableRegenarate", isRegenarateOptionEnabled);
            row.put("accessallowdomains", authorizedDomains);
            return row;
        } catch (Exception e) {
            String msg = "Error while obtaining the application access token for the application:" + applicationName;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    @Override
    public Map<String, Object> getAllSubscriptions(String userName, String appName,
                                                   int startSubIndex, int endSubIndex,
                                                   String groupId) throws APIManagementException {
        boolean isTenantFlowStarted = false;

        Integer subscriptionCount = 0;
        Map<String, Object> result = new HashMap<String, Object>();
        List<APISubscription> subs = new ArrayList<APISubscription>();

        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
        try {
            if (tenantDomain != null &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            Subscriber subscriber = new Subscriber(username);
            Application[] applications;
            applications = getApplications(new Subscriber(username), groupId);
            if (applications != null) {
                int i = 0;
                for (Application application : applications) {
                    APISubscription subscription = new APISubscription();
                    Set<Map<String, Object>> apiSet = new HashSet<Map<String, Object>>();
                    long startLoop = 0;
                    if (log.isDebugEnabled()) {
                        startLoop = System.currentTimeMillis();
                    }

                    Set<Scope> scopeSet = new LinkedHashSet<Scope>();

                    if (StringUtils.isBlank(appName) || appName.equals(application.getName())) {

                        //get Number of subscriptions for the given application by the subscriber.
                        subscriptionCount = getSubscriptionCount(subscriber, application.getName(), groupId);
                        //get subscribed APIs set as per the starting and ending indexes for application.
                        Set<SubscribedAPI> subscribedAPIs;
                        subscribedAPIs = getPaginatedSubscribedAPIs(subscriber, application.getName(), startSubIndex, endSubIndex, groupId);

                        List<APIIdentifier> identifiers = new ArrayList<APIIdentifier>();
                        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                            apiSet.add(addAPIObj(subscribedAPI, application));
                            identifiers.add(subscribedAPI.getApiId());

                        }
                        if (!identifiers.isEmpty()) {
                            //get scopes for subscribed apis
                            scopeSet = getScopesBySubscribedAPIs(identifiers);
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("getSubscribedAPIs loop took : " +
                                  (System.currentTimeMillis() - startLoop) + "ms");
                    }


                    if (APIConstants.ApplicationStatus.APPLICATION_APPROVED.equals(application.getStatus())) {
                        subscription.setAppId(application.getId());
                        subscription.setAppName(application.getName());
                        subscription.setCallbackUrl(application.getCallbackUrl());
                        APIKey prodKey = getAppKey(application, APIConstants.API_KEY_TYPE_PRODUCTION);

                        OAuthApplicationInfo prodApp = application.getOAuthApp("PRODUCTION");

                        String prodKeyScope = "";
                        if (prodKey != null && prodKey.getTokenScope() != null) {
                            //convert scope keys to names
                            prodKeyScope = APIUtil.getScopeNamesByKey(prodKey.getTokenScope(), scopeSet);
                        }

                        boolean prodEnableRegenarateOption = true;

                        // When the Production Access Token is successfully created.
                        if (prodKey != null && prodKey.getAccessToken() != null && prodApp != null) {
                            String jsonString = prodApp.getJsonString();

                            String prodConsumerKey = prodApp.getClientId();
                            String prodConsumerSecret = prodApp.getClientSecret();
                            subscription.setProdKey(prodKey.getAccessToken());
                            subscription.setProdKeyScope(prodKeyScope);
                            subscription.setProdKeyScopeValue(prodKey.getTokenScope());
                            subscription.setProdConsumerKey(prodConsumerKey);
                            subscription.setProdConsumerSecret(prodConsumerSecret);
                            subscription.setProdJsonString(jsonString);

                            subscription.setProdAuthorizedDomains(prodKey.getAuthorizedDomains());
                            if (APIUtil.isApplicationAccessTokenNeverExpire(prodKey.getValidityPeriod())) {
                                subscription.setProdValidityTime(-1);
                                prodEnableRegenarateOption = false;
                            } else {
                                subscription.setProdValidityTime(prodKey.getValidityPeriod());
                            }
                            subscription.setProdRegenerateOption(prodEnableRegenarateOption);
                        } // Prod Token is not generated, but consumer key & secret is available
                        else if (prodKey != null && prodApp != null) {
                            String jsonString = prodApp.getJsonString();
                            String prodConsumerKey = prodApp.getClientId();
                            String prodConsumerSecret = prodApp.getClientSecret();
                            subscription.setProdKey(null);
                            subscription.setProdKeyScope(null);
                            subscription.setProdKeyScopeValue(null);
                            subscription.setProdConsumerKey(prodConsumerKey);
                            subscription.setProdConsumerSecret(prodConsumerSecret);
                            subscription.setProdRegenerateOption(prodEnableRegenarateOption);
                            subscription.setProdAuthorizedDomains(null);
                            subscription.setProdJsonString(jsonString);

                            if (APIUtil.isApplicationAccessTokenNeverExpire(
                                    APIUtil.getApplicationAccessTokenValidityPeriodInSeconds())) {
                                subscription.setProdValidityTime(-1);
                            } else {
                                subscription.setProdValidityTime(APIUtil.getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                            subscription.setProdKeyState(prodKey.getState());
                        } else {
                            subscription.setProdKey(null);
                            subscription.setProdKeyScope(null);
                            subscription.setProdKeyScopeValue(null);
                            subscription.setProdConsumerKey(null);
                            subscription.setProdConsumerSecret(null);
                            subscription.setProdRegenerateOption(prodEnableRegenarateOption);
                            subscription.setProdAuthorizedDomains(null);
                            if (APIUtil.isApplicationAccessTokenNeverExpire(
                                    APIUtil.getApplicationAccessTokenValidityPeriodInSeconds())) {
                                subscription.setProdValidityTime(-1);
                            } else {
                                subscription.setProdValidityTime(APIUtil.getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                            subscription.setProdJsonString(null);

                        }

                        APIKey sandboxKey = getAppKey(application, APIConstants.API_KEY_TYPE_SANDBOX);

                        OAuthApplicationInfo sandApp = application.getOAuthApp("SANDBOX");
                        boolean sandEnableRegenarateOption = true;


                        String sandKeyScope = "";
                        if (sandboxKey != null && sandboxKey.getTokenScope() != null) {
                            //convert scope keys to names
                            sandKeyScope = APIUtil.getScopeNamesByKey(sandboxKey.getTokenScope(), scopeSet);
                        }


                        if (sandboxKey != null && sandboxKey.getConsumerKey() != null && sandApp != null) {
                            String jsonString = sandApp.getJsonString();

                            String sandboxConsumerKey = sandApp.getClientId();
                            String sandboxConsumerSecret = sandApp.getClientSecret();
                            subscription.setSandKey(sandboxKey.getAccessToken());
                            subscription.setSandKeyScope(sandKeyScope);
                            subscription.setSandKeyScopeValue(sandboxKey.getTokenScope());
                            subscription.setSandConsumerKey(sandboxConsumerKey);
                            subscription.setSandConsumerSecret(sandboxConsumerSecret);
                            subscription.setSandKeyState(sandboxKey.getState());
                            subscription.setSandJsonString(jsonString);
                            subscription.setSandAuthorizedDomains(sandboxKey.getAuthorizedDomains());
                            if (APIUtil.isApplicationAccessTokenNeverExpire(sandboxKey.getValidityPeriod())) {
                                subscription.setSandValidityTime(-1);
                                sandEnableRegenarateOption = false;
                            } else {
                                subscription.setSandValidityTime(sandboxKey.getValidityPeriod());
                            }
                            subscription.setSandRegenerateOption(sandEnableRegenarateOption);
                        } else if (sandboxKey != null && sandApp != null) {
                            String jsonString = sandApp.getJsonString();
                            String sandboxConsumerKey = sandApp.getClientId();
                            String sandboxConsumerSecret = sandApp.getClientSecret();
                            subscription.setSandKey(null);
                            subscription.setSandKeyScope(null);
                            subscription.setSandKeyScopeValue(null);
                            subscription.setSandConsumerKey(sandboxConsumerKey);
                            subscription.setSandConsumerSecret(sandboxConsumerSecret);
                            subscription.setSandRegenerateOption(sandEnableRegenarateOption);
                            subscription.setSandAuthorizedDomains(null);
                            subscription.setSandKeyState(sandboxKey.getState());
                            subscription.setSandJsonString(jsonString);
                            if (APIUtil.isApplicationAccessTokenNeverExpire(
                                    APIUtil.getApplicationAccessTokenValidityPeriodInSeconds())) {
                                subscription.setSandValidityTime(-1);
                            } else {
                                subscription.setSandValidityTime(APIUtil.getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                        } else {
                            subscription.setSandKey(null);
                            subscription.setSandKeyScope(null);
                            subscription.setSandKeyScopeValue(null);
                            subscription.setSandConsumerKey(null);
                            subscription.setSandConsumerSecret(null);
                            subscription.setSandRegenerateOption(sandEnableRegenarateOption);
                            subscription.setSandAuthorizedDomains(null);
                            subscription.setSandJsonString(null);
                            if (APIUtil.isApplicationAccessTokenNeverExpire(
                                    APIUtil.getApplicationAccessTokenValidityPeriodInSeconds())) {
                                subscription.setSandValidityTime(-1);
                            } else {
                                subscription.setSandValidityTime(APIUtil.getApplicationAccessTokenValidityPeriodInSeconds());
                            }
                        }


                        if (appName == null || appName.isEmpty() || appName.equals(application.getName())) {

                            startLoop = 0;
                            if (log.isDebugEnabled()) {
                                startLoop = System.currentTimeMillis();
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("getSubscribedAPIs loop took : " +
                                          (System.currentTimeMillis() - startLoop) + "ms");
                            }
                        }
                        subscription.setSubscriptions(apiSet);
                        subscription.setScopes(scopeSet);
                        subs.add(subscription);
                        result.put("applications", subs);
                        result.put("totalLength", subscriptionCount);
                    }


                }
            }


        } catch (APIManagementException e)

        {
            handleException("Error while obtaining subscriptions data", e);
        } finally

        {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    private static APIKey getKey(SubscribedAPI api, String keyType) {
        List<APIKey> apiKeys = api.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    private static APIKey getKeyOfType(List<APIKey> apiKeys, String keyType) {
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key;
            }
        }
        return null;
    }

    private Map<String,Object> addAPIObj(SubscribedAPI subscribedAPI, Application appObject) throws APIManagementException {

        Map<String,Object> apiMap=new HashMap<String, Object>();
        try {
            API api = getAPIInfo(subscribedAPI.getApiId());
            apiMap.put("name",subscribedAPI.getApiId().getApiName());
            apiMap.put("provider",APIUtil.replaceEmailDomainBack(subscribedAPI.getApiId().getProviderName()));
            apiMap.put("version",subscribedAPI.getApiId().getVersion());
            apiMap.put("status",api.getStatus().toString());
            apiMap.put("tier",subscribedAPI.getTier().getDisplayName());
            apiMap.put("subStatus",subscribedAPI.getSubStatus());
            apiMap.put("thumburl",APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
            apiMap.put("context",api.getContext());
            //Read key from the appObject
            APIKey prodKey = getAppKey(appObject, APIConstants.API_KEY_TYPE_PRODUCTION);
            if (prodKey != null) {
                apiMap.put("prodKey", prodKey.getAccessToken());
                apiMap.put("prodConsumerKey", prodKey.getConsumerKey());
                apiMap.put("prodConsumerSecret", prodKey.getConsumerSecret());
                apiMap.put("prodAuthorizedDomains", prodKey.getAuthorizedDomains());
                if (APIUtil.isApplicationAccessTokenNeverExpire(prodKey.getValidityPeriod())) {
                    apiMap.put("prodValidityTime", -1);
                } else {
                    apiMap.put("prodValidityTime", prodKey.getValidityPeriod());
                }
                //apiObj.put("prodValidityRemainingTime", apiObj, apiMgtDAO.getApplicationAccessTokenRemainingValidityPeriod(prodKey.getAccessToken()));
            } else {
                apiMap.put("prodKey", null);
                apiMap.put("prodConsumerKey", null);
                apiMap.put("prodConsumerSecret", null);
                apiMap.put("prodAuthorizedDomains", null);
                if (APIUtil.isApplicationAccessTokenNeverExpire(APIUtil.getApplicationAccessTokenValidityPeriodInSeconds())) {
                    apiMap.put("prodValidityTime", -1);
                } else {
                    apiMap.put("prodValidityTime", APIUtil.getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                }
                // apiObj.put("prodValidityRemainingTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
            }

            APIKey sandboxKey = getAppKey(appObject, APIConstants.API_KEY_TYPE_SANDBOX);
            if (sandboxKey != null) {
                apiMap.put("sandboxKey", sandboxKey.getAccessToken());
                apiMap.put("sandboxConsumerKey", sandboxKey.getConsumerKey());
                apiMap.put("sandboxConsumerSecret", sandboxKey.getConsumerSecret());
                apiMap.put("sandAuthorizedDomains", sandboxKey.getAuthorizedDomains());
                if (APIUtil.isApplicationAccessTokenNeverExpire(sandboxKey.getValidityPeriod())) {
                    apiMap.put("sandValidityTime", -1);
                } else {
                    apiMap.put("sandValidityTime",sandboxKey.getValidityPeriod());
                }
                //apiObj.put("sandValidityRemainingTime", apiObj, apiMgtDAO.getApplicationAccessTokenRemainingValidityPeriod(sandboxKey.getAccessToken()));
            } else {
                apiMap.put("sandboxKey",null);
                apiMap.put("sandboxConsumerKey",  null);
                apiMap.put("sandboxConsumerSecret", null);
                apiMap.put("sandAuthorizedDomains", null);
                if (APIUtil.getApplicationAccessTokenValidityPeriodInSeconds() < 0) {
                    apiMap.put("sandValidityTime", -1);
                } else {
                    apiMap.put("sandValidityTime", APIUtil.getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
                }
                // apiObj.put("sandValidityRemainingTime", apiObj, getApplicationAccessTokenValidityPeriodInSeconds() * 1000);
            }
            apiMap.put("hasMultipleEndpoints", String.valueOf(api.getSandboxUrl() != null));

        } catch (APIManagementException e) {
            log.error("Error while obtaining application metadata", e);
        }
        return apiMap;
    }

    private APIKey getAppKey(Application app, String keyType) {
        List<APIKey> apiKeys = app.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    @Override
    public JSONArray getSubscriptions(String providerName, String apiName, String version, String user,String groupId) throws APIManagementException{
        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
        Subscriber subscriber = new Subscriber(user);
        JSONArray subscriptions = new JSONArray();
        Set<SubscribedAPI> apis = getSubscribedIdentifiers(subscriber, apiIdentifier,groupId);

        int i = 0;
        if (apis != null) {
            for (SubscribedAPI api : apis) {
                JSONObject row = new JSONObject();
                row.put("application", api.getApplication().getName());
                row.put("applicationId", api.getApplication().getId());
                row.put("prodKey", getKey(api, APIConstants.API_KEY_TYPE_PRODUCTION));
                row.put("sandboxKey", getKey(api, APIConstants.API_KEY_TYPE_SANDBOX));
                ArrayList<APIKey> keys = (ArrayList<APIKey>) api.getApplication().getKeys();
                for(APIKey key : keys){
                    row.put(key.getType()+"_KEY", key.getAccessToken());
                }
                subscriptions.add(i++, row);
            }
        }
        return subscriptions;

    }


}
