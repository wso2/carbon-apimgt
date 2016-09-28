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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LoginPostExecutor;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidator;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.*;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.*;
import org.wso2.carbon.apimgt.impl.workflow.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import org.wso2.carbon.registry.common.TermData;

import javax.cache.Caching;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    public static final char COLON_CHAR = ':';
    public static final String EMPTY_STRING = "";

    /* Map to Store APIs against Tag */
    private ConcurrentMap<String, Set<API>> taggedAPIs = new ConcurrentHashMap<String, Set<API>>();
    private boolean isTenantModeStoreView;
    private String requestedTenant;
    private boolean isTagCacheEnabled;
    private Set<Tag> tagSet;
    private long tagCacheValidityTime;
    private volatile long lastUpdatedTime;
    private volatile long lastUpdatedTimeForTagApi;
    private final Object tagCacheMutex = new Object();
    private final Object tagWithAPICacheMutex = new Object();
    private APIMRegistryService apimRegistryService;

    public APIConsumerImpl() throws APIManagementException {
        super();
        readTagCacheConfigs();
    }

    public APIConsumerImpl(String username, APIMRegistryService apimRegistryService) throws APIManagementException {
        super(username);
        readTagCacheConfigs();
        this.apimRegistryService = apimRegistryService;
    }

    private void readTagCacheConfigs() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration();
        String enableTagCache = config.getFirstProperty(APIConstants.STORE_TAG_CACHE_DURATION);
        if (enableTagCache == null) {
            isTagCacheEnabled = false;
            tagCacheValidityTime = 0;
        } else {
            isTagCacheEnabled = true;
            tagCacheValidityTime = Long.parseLong(enableTagCache);
        }
    }

    @Override
    public Subscriber getSubscriber(String subscriberId) throws APIManagementException {
        Subscriber subscriber = null;
        try {
            subscriber = apiMgtDAO.getSubscriber(subscriberId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscriber", e);
        }
        return subscriber;
    }


    /**
     * Returns the set of APIs with the given tag from the taggedAPIs Map
     *
     * @param tagName The name of the tag
     * @return Set of {@link API} with the given tag
     * @throws APIManagementException
     */
    @Override
	public Set<API> getAPIsWithTag(String tagName, String requestedTenantDomain) throws APIManagementException {
    	
    	 /* We keep track of the lastUpdatedTime of the TagCache to determine its freshness.
         */
        long lastUpdatedTimeAtStart = lastUpdatedTimeForTagApi;
        long currentTimeAtStart = System.currentTimeMillis();
        if(isTagCacheEnabled && ( (currentTimeAtStart- lastUpdatedTimeAtStart) < tagCacheValidityTime)){
        	if (taggedAPIs != null && taggedAPIs.containsKey(tagName)) {
    			return taggedAPIs.get(tagName);
    		}
        }else{
        	synchronized (tagWithAPICacheMutex) {
        		lastUpdatedTimeForTagApi = System.currentTimeMillis();
                taggedAPIs = new ConcurrentHashMap<String, Set<API>>();
            }
        	
        }

        boolean isTenantMode = requestedTenantDomain != null && !"null".equalsIgnoreCase(requestedTenantDomain);
		this.isTenantModeStoreView = isTenantMode;

		if (requestedTenantDomain != null && !"null".equals(requestedTenantDomain)) {
			this.requestedTenant = requestedTenantDomain;
		}

		Registry userRegistry;
		boolean isTenantFlowStarted = false;
		Set<API> apisWithTag = null;
		try {
            //start the tenant flow prior to loading registry
            if (requestedTenant != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenant)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenant, true);
                isTenantFlowStarted = true;
            }

            if ((isTenantMode && this.tenantDomain == null) ||
                (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                     .getTenantId(requestedTenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                        getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().
                        setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            }

            apisWithTag = getAPIsWithTag(userRegistry, tagName);

			/* Add the APIs against the tag name */
			if (!apisWithTag.isEmpty()) {
				if (taggedAPIs.containsKey(tagName)) {
					for (API api : apisWithTag) {
						taggedAPIs.get(tagName).add(api);
					}
				} else {
					taggedAPIs.putIfAbsent(tagName, apisWithTag);					
				}
			}

		} catch (RegistryException e) {
			handleException("Failed to get api by the tag", e);
		} catch (UserStoreException e) {
			handleException("Failed to get api by the tag", e);
		} finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

		return apisWithTag;
	}

    /**
     * Returns the set of APIs with the given tag from the taggedAPIs Map.
     *
     * @param tag   The name of the tag
     * @param start The starting index of the return result set
     * @param end   The end index of the return result set
     * @return A {@link Map} of APIs(between the given indexes) and the total number indicating all the available
     * APIs count
     * @throws APIManagementException
     */
    @Override
    public Map<String, Object> getPaginatedAPIsWithTag(String tag, int start, int end, String tenantDomain) throws APIManagementException {
        List<API> apiList = new ArrayList<API>();
        Set<API> resultSet = new TreeSet<API>(new APIVersionComparator());
        Map<String, Object> results = new HashMap<String, Object>();
        Set<API> taggedAPISet = this.getAPIsWithTag(tag,tenantDomain);
        if (taggedAPISet != null) {
            if (taggedAPISet.size() < end) {
                end = taggedAPISet.size();
            }
            int totalLength;

            apiList.addAll(taggedAPISet);
            totalLength = apiList.size();
            if (totalLength <= ((start + end) - 1)) {
                end = totalLength;
            } else {
                end = start + end;
            }
            for (int i = start; i < end; i++) {
                resultSet.add(apiList.get(i));
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
     * @param tag - The tag name
     * @return A {@link Set} of {@link API} objects.
     * @throws APIManagementException
     */
    private Set<API> getAPIsWithTag(Registry registry, String tag)
            throws APIManagementException {
        Set<API> apiSet = new TreeSet<API>(new APINameComparator());
        try {
            List<GovernanceArtifact> genericArtifacts =
                    GovernanceUtils.findGovernanceArtifacts(APIConstants.TAG_SEARCH_TYPE_PREFIX2 + tag, registry,
                                                            APIConstants.API_RXT_MEDIA_TYPE);
            for (GovernanceArtifact genericArtifact : genericArtifacts) {
                try {
                    String apiStatus = genericArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                    if (genericArtifact != null && (APIConstants.PUBLISHED.equals(apiStatus)
                         || APIConstants.PROTOTYPED.equals(apiStatus))) {
                        API api = APIUtil.getAPI(genericArtifact);
                        if (api != null) {
                            apiSet.add(api);
                        }
                    }
                } catch (RegistryException e) {
                    log.warn("User is not authorized to get an API with tag " + tag, e);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get API for tag " + tag, e);
        }
        return apiSet;
    }

    /**
     * The method to get APIs to Store view
     *
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    @Override
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
                        if (APIConstants.PUBLISHED.equals(status)) {
                            api = APIUtil.getAPI(artifact);
                        }
                    } else {   // else we are interested in both deprecated/published APIs here...
                        if (APIConstants.PUBLISHED.equals(status) || APIConstants.DEPRECATED.equals(status)) {
                            api = APIUtil.getAPI(artifact);
                        }
                    }
                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                            multiVersionedAPIs.add(api);
                        }
                    }
                }
                if (!displayMultipleVersions) {
                    apiSortedSet.addAll(latestPublishedAPIs.values());
                    return apiSortedSet;
                } else {
                    apiVersionsSortedSet.addAll(multiVersionedAPIs);
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
    @Override
    @Deprecated
    public Map<String,Object> getAllPaginatedPublishedAPIs(String tenantDomain,int start,int end)
            throws APIManagementException {
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

        Map<String, Object> result = new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength = 0;
        try {
            Registry userRegistry;
            boolean isTenantMode = (tenantDomain != null);
            if ((isTenantMode && this.tenantDomain == null) ||
                (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                                                                       tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
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
                totalLength = PaginationContext.getInstance().getLength();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    result.put("apis", apiSortedSet);
                    result.put("totalLength", totalLength);
                    return result;
                }

                for (GenericArtifact artifact : genericArtifacts) {
                    // adding the API provider can mark the latest API .
                    API api = APIUtil.getAPI(artifact);
                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                            multiVersionedAPIs.add(api);
                        }
                    }
                }
                if (!displayMultipleVersions) {
                    apiSortedSet.addAll(latestPublishedAPIs.values());
                    result.put("apis", apiSortedSet);
                    result.put("totalLength", totalLength);
                    return result;
                } else {
                    apiVersionsSortedSet.addAll(multiVersionedAPIs);
                    result.put("apis", apiVersionsSortedSet);
                    result.put("totalLength", totalLength);
                    return result;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        } finally {
            PaginationContext.destroy();
        }
        result.put("apis", apiSortedSet);
        result.put("totalLength", totalLength);
        return result;
    }
    
    /**
     * The method to get APIs in any of the given LC status array
     *
     * @return Map<String, Object>  API result set with pagination information
     * @throws APIManagementException
     */
    @Override
    public Map<String, Object> getAllPaginatedAPIsByStatus(String tenantDomain,
            int start, int end, final String[] apiStatus, boolean returnAPITags) throws APIManagementException {

        Map<String,Object> result=new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength=0;
        boolean isMore = false;
        String criteria = "lcState=";
        
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
            
            
            criteria = criteria + APIUtil.getORBasedSearchCriteria(apiStatus);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
            if (artifactManager != null) {
                if (apiStatus != null && apiStatus.length > 0) {
                    List<GovernanceArtifact> genericArtifacts = GovernanceUtils.findGovernanceArtifacts(criteria, userRegistry,
                            APIConstants.API_RXT_MEDIA_TYPE);
                    totalLength = PaginationContext.getInstance().getLength();
                    if (genericArtifacts == null || genericArtifacts.size() == 0) {
                        result.put("apis", apiSortedSet);
                        result.put("totalLength", totalLength);
                        result.put("isMore", isMore);
                        return result;
                    }

                    // Check to see if we can speculate that there are more APIs to be loaded
                    if (maxPaginationLimit == totalLength) {
                        isMore = true;  // More APIs exist so we cannot determine the total API count without incurring a
                        // performance hit
                        --totalLength; // Remove the additional 1 we added earlier when setting max pagination limit
                    }
                    int tempLength = 0;
                    for (GovernanceArtifact artifact : genericArtifacts) {

                        API api = null;
                        try {
                            api = APIUtil.getAPI(artifact);
                        } catch (APIManagementException e) {
                            //log and continue since we want to load the rest of the APIs.
                            log.error("Error while loading API " + artifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                                    e);
                        }
                        if (api != null) {
                            if (returnAPITags) {
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
                                key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                                multiVersionedAPIs.add(api);
                            }
                        }
                        tempLength++;
                        if (tempLength >= totalLength) {
                            break;
                        }
                    }
                    if (!displayMultipleVersions) {
                        apiSortedSet.addAll(latestPublishedAPIs.values());
                        result.put("apis", apiSortedSet);
                        result.put("totalLength", totalLength);
                        result.put("isMore", isMore);
                        return result;
                    } else {
                        apiVersionsSortedSet.addAll(multiVersionedAPIs);
                        result.put("apis", apiVersionsSortedSet);
                        result.put("totalLength", totalLength);
                        result.put("isMore", isMore);
                        return result;
                    }
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        } finally {
            PaginationContext.destroy();
        }
        result.put("apis", apiSortedSet);
        result.put("totalLength", totalLength);
        result.put("isMore", isMore);
        return result;
        
    }

    /**
     * The method to get APIs by given status to Store view
     *
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    @Override
    @Deprecated
	public Map<String, Object> getAllPaginatedAPIsByStatus(String tenantDomain,
			int start, int end, final String apiStatus, boolean returnAPITags) throws APIManagementException {
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
        boolean isMore = false;
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
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
            if (artifactManager != null) {
                
                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength=PaginationContext.getInstance().getLength();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    result.put("isMore", isMore);
                    return result;
                }

                // Check to see if we can speculate that there are more APIs to be loaded
                if (maxPaginationLimit == totalLength) {
                    isMore = true;  // More APIs exist so we cannot determine the total API count without incurring a
                            // performance hit
                    --totalLength; // Remove the additional 1 we added earlier when setting max pagination limit
                }
                int tempLength=0;
                for (GenericArtifact artifact : genericArtifacts) {

                    API api  = null;
                    try {
                        api = APIUtil.getAPI(artifact);
                    } catch (APIManagementException e) {
                        //log and continue since we want to load the rest of the APIs.
                        log.error("Error while loading API " + artifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                                e);
                    }
                    if (api != null) {
                        if (returnAPITags) {
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
                            key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                            multiVersionedAPIs.add(api);
                        }
                    }
                    tempLength++;
                    if (tempLength >= totalLength){
                        break;
                    }
                }
                if (!displayMultipleVersions) {
                    apiSortedSet.addAll(latestPublishedAPIs.values());
                    result.put("apis",apiSortedSet);
                    result.put("totalLength",totalLength);
                    result.put("isMore", isMore);
                    return result;
                } else {
                    apiVersionsSortedSet.addAll(multiVersionedAPIs);
                    result.put("apis",apiVersionsSortedSet);
                    result.put("totalLength",totalLength);
                    result.put("isMore", isMore);
                    return result;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all published APIs", e);
        } finally {
            PaginationContext.destroy();
        }
        result.put("apis", apiSortedSet);
        result.put("totalLength", totalLength);
        result.put("isMore", isMore);
        return result;
	}

    /**
     * Re-generates the access token.
     * @param oldAccessToken          Token to be revoked
     * @param clientId                Consumer Key for the Application
     * @param clientSecret            Consumer Secret for the Application
     * @param validityTime            Desired Validity time for the token
     * @param jsonInput               Additional parameters if Authorization server needs any.
     * @return Renewed Access Token.
     * @throws APIManagementException
     */
    @Override
    public AccessTokenInfo renewAccessToken(String oldAccessToken, String clientId, String clientSecret,
                                            String validityTime, String
                                                    requestedScopes[], String jsonInput) throws APIManagementException {
        // Create Token Request with parameters provided from UI.
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(clientId);
        tokenRequest.setClientSecret(clientSecret);
        tokenRequest.setValidityPeriod(Long.parseLong(validityTime));
        tokenRequest.setTokenToRevoke(oldAccessToken);
        tokenRequest.setScope(requestedScopes);

        try {
            // Populating additional parameters.
            tokenRequest = ApplicationUtils.populateTokenRequest(jsonInput, tokenRequest);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            return keyManager.getNewApplicationAccessToken(tokenRequest);
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
    @Deprecated
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
                int publishedAPICount;
                if (genericArtifacts != null) {
                    for (GenericArtifact artifact : genericArtifacts) {
                        // adding the API provider can mark the latest API .
//                        String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                        API api  = APIUtil.getAPI(artifact);
                        if (api != null) {
                            String key;
                            //Check the configuration to allow showing multiple versions of an API true/false
                            if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                                key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
    //                            key = api.getId().getProviderName() + ":" + api.getId().getApiProductName() + ":" + api.getId()
    //                                    .getVersion();
                                multiVersionedAPIs.add(api);
                            }
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

                    if (genericArtifactsForDeprecatedAPIs != null) {
                        for (GenericArtifact artifact : genericArtifactsForDeprecatedAPIs) {
                            // adding the API provider can mark the latest API .

                            API api  = APIUtil.getAPI(artifact);

                            if (api != null) {
                                String key;
                                //Check the configuration to allow showing multiple versions of an API true/false
                                if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                                    key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                                    multiVersionedAPIs.add(api);
                                }
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
                    apiVersionsSortedSet.addAll(multiVersionedAPIs);
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
        result.put("apis", apiSortedSet);
        result.put("totalLength", totalLength);
        return result;
    }

    @Override
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
                if (APIConstants.PUBLISHED.equals(status)) {
                    String artifactPath = genericArtifact.getPath();

                    float rating = registry.getAverageRating(artifactPath);
                    if (rating > APIConstants.TOP_TATE_MARGIN && (returnLimit < limit)) {
                        returnLimit++;
                        API api = APIUtil.getAPI(genericArtifact, registry);
                        if (api != null) {
                            apiSortedSet.add(api);
                        }
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
    @Override
    public Set<API> getRecentlyAddedAPIs(int limit, String tenantDomain)
            throws APIManagementException {
        SortedSet<API> recentlyAddedAPIs = new TreeSet<API>(new APINameComparator());
        SortedSet<API> recentlyAddedAPIsWithMultipleVersions = new TreeSet<API>(new APIVersionComparator());
        Registry userRegistry;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        boolean isRecentlyAddedAPICacheEnabled =
              Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE));

        PrivilegedCarbonContext.startTenantFlow();
        boolean isTenantFlowStarted ;
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
                Set<API> recentlyAddedAPI = (Set<API>) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).get(username + COLON_CHAR + tenantDomain);
                if (recentlyAddedAPI != null) {
                    for (API api : recentlyAddedAPI) {
                        try {
                            if (!APIConstants.PUBLISHED.equalsIgnoreCase(userRegistry.get(APIUtil.getAPIPath(api.getId())).getProperty(APIConstants.API_OVERVIEW_STATUS))) {
                                isStatusChanged = true;
                                break;
                            }
                        } catch (Exception ex) {
                            log.error("Error while checking API status for APP " + api.getId().getApiName() + '-' +
                                      api.getId().getVersion(), ex);
                        }
                    }
                    if (!isStatusChanged) {
                        return recentlyAddedAPI;
                    }
                }
            }

            PaginationContext.init(0, limit, APIConstants.REGISTRY_ARTIFACT_SEARCH_DESC_ORDER,
                    APIConstants.REGISTRY_ARTIFACT_SORT_BY_CREATED_TIME, Integer.MAX_VALUE);
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

                    API api = null;
                    try {
                        api = APIUtil.getAPI(artifact);
                    } catch (APIManagementException e) {
                        //just log and continue since we want to go through the other APIs as well.
                        log.error("Error loading API " + artifact.getAttribute(APIConstants.API_OVERVIEW_NAME), e);
                    }
                    if (api != null) {
                        allAPIs.add(api);
                    }
                }

				if (!APIUtil.isAllowDisplayMultipleVersions()) {
					Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
					Comparator<API> versionComparator = new APIVersionComparator();
					String key;
					for (API api : allAPIs) {
						key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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

                    recentlyAddedAPIs.addAll(latestPublishedAPIs.values());
					if (isRecentlyAddedAPICacheEnabled) {
						Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
						       .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME)
						       .put(username + COLON_CHAR + tenantDomain, allAPIs);
					}
					return recentlyAddedAPIs;
				} else {
        			recentlyAddedAPIsWithMultipleVersions.addAll(allAPIs);
					if (isRecentlyAddedAPICacheEnabled) {
						Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
						       .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME)
						       .put(username + COLON_CHAR + tenantDomain, allAPIs);
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

    @Override
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

        TreeSet<Tag> tempTagSet = new TreeSet<Tag>(new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Registry userRegistry = null;
        boolean isTenantFlowStarted = false;
        String tagsQueryPath = null;
        try {
        	tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
            Map<String, String> params = new HashMap<String, String>();
            params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.TAG_SUMMARY_RESULT_TYPE);
            //as a tenant, I'm browsing my own Store or I'm browsing a Store of another tenant..
            if ((this.isTenantModeStoreView && this.tenantDomain==null) || (this.isTenantModeStoreView && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant based store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(this.requestedTenant);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                userRegistry = registry;
            }

            Map<String, Tag> tagsData = new HashMap<String, Tag>();
            try {
            	PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(((UserRegistry)userRegistry).getUserName());
                if (requestedTenant != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenant)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenant, true);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(((UserRegistry)userRegistry).getUserName());
                }

                Map <String, List<String>> criteriaPublished = new HashMap<String, List<String>>();
                criteriaPublished.put(APIConstants.LCSTATE_SEARCH_KEY, new ArrayList<String>() {{
                    add(APIConstants.PUBLISHED);
                }});
                //rxt api media type
                List<TermData> termsPublished = GovernanceUtils
                        .getTermDataList(criteriaPublished, APIConstants.API_OVERVIEW_TAG,
                                         APIConstants.API_RXT_MEDIA_TYPE, true);

                if(termsPublished != null){
                    for(TermData data : termsPublished){
                        tempTagSet.add(new Tag(data.getTerm(), (int)data.getFrequency()));
                    }
                }

                Map<String, List<String>> criteriaPrototyped = new HashMap<String, List<String>>();
                criteriaPrototyped.put(APIConstants.LCSTATE_SEARCH_KEY, new ArrayList<String>() {{
                    add(APIConstants.PROTOTYPED);
                }});
                //rxt api media type
                List<TermData> termsPrototyped = GovernanceUtils
                        .getTermDataList(criteriaPrototyped, APIConstants.API_OVERVIEW_TAG,
                                         APIConstants.API_RXT_MEDIA_TYPE, true);

                if(termsPrototyped != null){
                    for(TermData data : termsPrototyped){
                        tempTagSet.add(new Tag(data.getTerm(), (int)data.getFrequency()));
                    }
                }


            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            synchronized (tagCacheMutex) {
                lastUpdatedTime = System.currentTimeMillis();
                this.tagSet = tempTagSet;
            }

        } catch (RegistryException e) {
        	try {
        		//Before a tenant login to the store or publisher at least one time,
        		//a registry exception is thrown when the tenant store is accessed in anonymous mode.
        		//This fix checks whether query resource available in the registry. If not
        		// give a warn.
				if (userRegistry != null && !userRegistry.resourceExists(tagsQueryPath)) {
					log.warn("Failed to retrieve tags query resource at " + tagsQueryPath);
					return tagSet == null ? Collections.EMPTY_SET : tagSet;
				}
			} catch (RegistryException e1) {
                // Even if we should ignore this exception, we are logging this as a warn log.
                // The reason is that, this error happens when we try to add some additional logs in an error
                // scenario and it does not affect the execution path.
                log.warn("Unable to execute the resource exist method for tags query resource path : " + tagsQueryPath,
                         e1);
            }
            handleException("Failed to get all the tags", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all the tags", e);
        }
 
        return tagSet;
    }

    @Override
    public Set<Tag> getTagsWithAttributes(String tenantDomain) throws APIManagementException {
        // Fetch the all the tags first.
        Set<Tag> tags = getAllTags(tenantDomain);
        // For each and every tag get additional attributes from the registry.
        String descriptionPathPattern = APIConstants.TAGS_INFO_ROOT_LOCATION + "/%s/description.txt";
        String thumbnailPathPattern = APIConstants.TAGS_INFO_ROOT_LOCATION + "/%s/thumbnail.png";

        //if the tenantDomain is not specified super tenant domain is used
        if (StringUtils.isBlank(tenantDomain)) {
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
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            govRegistry = registryService.getGovernanceSystemRegistry(tenantId);
        } catch (UserStoreException e) {
            handleException("Cannot get tenant id for tenant domain name:" + tenantDomain, e);
        } catch (RegistryException e) {
            handleException("Cannot get registry for tenant domain name:" + tenantDomain, e);
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
                    log.warn(String.format("Error while querying the existence of the description for the tag '%s'",
                            tag.getName()), e);
                }
                // The resource is assumed to be a byte array since its the content
                // of a text file.
                if (descriptionResource != null) {
                    try {
                        String description = new String((byte[]) descriptionResource.getContent(),
                                                        Charset.defaultCharset());
                        tag.setDescription(description);
                    } catch (ClassCastException e) {
                        //added warnings as it can then proceed to load rest of resources/tags
                        log.warn(String.format("Cannot cast content of %s to byte[]", descriptionPath), e);
                    } catch (RegistryException e) {
                        //added warnings as it can then proceed to load rest of resources/tags
                        log.warn(String.format("Cannot read content of %s", descriptionPath), e);
                    }
                }
                // Checks whether the thumbnail exists.
                String thumbnailPath = String.format(thumbnailPathPattern, tag.getName());
                try {
                    boolean isThumbnailExists = govRegistry.resourceExists(thumbnailPath);
                    tag.setThumbnailExists(isThumbnailExists);
                    if (isThumbnailExists) {
                        tag.setThumbnailUrl(APIUtil.getRegistryResourcePathForUI(
                                APIConstants.RegistryResourceTypesForUI.TAG_THUMBNAIL, tenantDomain, thumbnailPath));
                    }
                } catch (RegistryException e) {
                    //warn and then proceed to load rest of tags
                    log.warn(String.format("Error while querying the existence of %s", thumbnailPath), e);
                }
            }
        }
        return tags;
    }

    @Override
    public void rateAPI(APIIdentifier apiId, APIRating rating,
                        String user) throws APIManagementException {
        apiMgtDAO.addRating(apiId, rating.getRating(), user);
    }

    @Override
    public void removeAPIRating(APIIdentifier apiId, String user) throws APIManagementException {
        apiMgtDAO.removeAPIRating(apiId, user);
    }

    @Override
    public int getUserRating(APIIdentifier apiId, String user) throws APIManagementException {
        return apiMgtDAO.getUserRating(apiId, user);
    }

    @Override
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
            String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            Association[] associations = registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION);
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
                        if (APIConstants.PUBLISHED.equals(status)) {
                            api = APIUtil.getAPI(artifact);
                        }
                    } else {   // else we are interested in both deprecated/published APIs here...
                        if (APIConstants.PUBLISHED.equals(status) || APIConstants.DEPRECATED.equals(status)) {
                            api = APIUtil.getAPI(artifact);
                        }
                    }
                    if (api != null) {
                        String key;
                        //Check the configuration to allow showing multiple versions of an API true/false
                        if (!displayMultipleVersions) { //If allow only showing the latest version of an API
                            key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                            multiVersionedAPIs.add(api);
                        }
                    }
                } else {
                    throw new GovernanceException("artifact id is null of " + apiPath);
                }
            }
            if (!displayMultipleVersions) {
                apiSortedSet.addAll(latestPublishedAPIs.values());
                return apiSortedSet;
            } else {
                apiVersionsSortedSet.addAll(multiVersionedAPIs);
                return apiVersionsSortedSet;
            }

        } catch (RegistryException e) {
            handleException("Failed to get Published APIs for provider : " + providerId, e);
        }
        return null;
    }

    @Override
    public Set<API> getPublishedAPIsByProvider(String providerId, String loggedUsername, int limit, String apiOwner,
                                               String apiBizOwner) throws APIManagementException {
        try {
            Boolean allowMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
            Boolean showAllAPIs = APIUtil.isAllowDisplayAPIsWithMultipleStatus();

            String providerDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerId));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(providerDomain);
            final Registry registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceSystemRegistry(tenantId);

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            int publishedAPICount = 0;
            Map<String, API> apiCollection = new HashMap<String, API>();

            if(apiBizOwner != null && !apiBizOwner.isEmpty()){
                try {
                    final String bizOwner = apiBizOwner;
                    GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
                        public boolean matches(GenericArtifact artifact) throws GovernanceException {
                            return artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER) != null &&
                                   bizOwner.matches(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
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
                }
            }
            else{
                String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;
                Association[] associations = registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION);

                for (Association association : associations) {
                    if (publishedAPICount >= limit) {
                        break;
                    }
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
        boolean checkAuthorized;
        String userNameWithoutDomain = loggedUsername;

        if (!loggedUsername.isEmpty() && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(super.tenantDomain)) {
            String[] nameParts = loggedUsername.split("@");
            userNameWithoutDomain = nameParts[0];
        }

        int loggedInUserTenantDomain = -1;
        if(!StringUtils.isEmpty(loggedUsername)) {
            loggedInUserTenantDomain = APIUtil.getTenantId(loggedUsername);
        }

        if (loggedUsername.isEmpty()) {
            // Anonymous user is viewing.
            checkAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
        } else if (tenantId != loggedInUserTenantDomain) {
            //Cross tenant scenario
            providerId = APIUtil.replaceEmailDomainBack(providerId);
            String[] nameParts = providerId.split("@");
            String provideNameWithoutDomain = nameParts[0];
            checkAuthorized = manager.isUserAuthorized(provideNameWithoutDomain, path, ActionConstants.GET);
        } else {
            // Some user is logged in also user and api provider tenant domain are same.
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
                if (APIConstants.PUBLISHED.equals(status)) {
                    api = APIUtil.getAPI(artifact);
                }
            } else {   // else we are interested in both deprecated/published APIs here...
                if (APIConstants.PUBLISHED.equals(status) || APIConstants.DEPRECATED.equals(status)) {
                    api = APIUtil.getAPI(artifact);
                }

            }
            if (api != null) {
                String apiVisibility = api.getVisibility();
                if(!StringUtils.isEmpty(apiVisibility) && !APIConstants.API_GLOBAL_VISIBILITY.equalsIgnoreCase(apiVisibility)) {
                    String providerDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerId));
                    String loginUserDomain = MultitenantUtils.getTenantDomain(loggedUsername);
                    if(!StringUtils.isEmpty(providerDomain) && !StringUtils.isEmpty(loginUserDomain)
                            && !providerDomain.equals(loginUserDomain)){
                        return false;
                    }
                }
                // apiOwner is the value coming from front end and compared against the API instance
                if (apiOwner != null && !apiOwner.isEmpty()) {
                    if (APIUtil.replaceEmailDomainBack(providerId).equals(APIUtil.replaceEmailDomainBack(apiOwner)) &&
                        api.getApiOwner() != null && !api.getApiOwner().isEmpty() &&
                        !APIUtil.replaceEmailDomainBack(apiOwner)
                                .equals(APIUtil.replaceEmailDomainBack(api.getApiOwner()))) {
                        return false; // reject remote APIs when local admin user's API selected
                    } else if (!APIUtil.replaceEmailDomainBack(providerId).equals(APIUtil.replaceEmailDomainBack(apiOwner)) &&
                               !APIUtil.replaceEmailDomainBack(apiOwner)
                                       .equals(APIUtil.replaceEmailDomainBack(api.getApiOwner()))) {
                        return false; // reject local admin's APIs when remote API selected
                    }
                }
                String key;
                //Check the configuration to allow showing multiple versions of an API true/false
                if (!allowMultipleVersions) { //If allow only showing the latest version of an API
                    key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
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
                    key = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName() + COLON_CHAR + api.getId()
                            .getVersion();
                    //we're not really interested in the key, so generate one for the sake of adding this element to
                    //the map.
                    key = key + '_' + apiCollection.size();
                    apiCollection.put(key, api);
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Map<String,Object> searchPaginatedAPIs(String searchTerm, String searchType, String requestedTenantDomain,int start,int end, boolean isLazyLoad)
            throws APIManagementException {
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

            if (APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchType)) {
                Map<Documentation, API> apiDocMap =
                        APIUtil.searchAPIsByDoc(userRegistry, tenantIDLocal, userNameLocal, searchTerm,
                                                APIConstants.STORE_CLIENT);
                result.put("apis", apiDocMap);
            	/*Pagination for Document search results is not supported yet, hence length is sent as end-start*/
            	if (apiDocMap.isEmpty()) {
            		result.put("length", 0);
            	} else {
            		result.put("length", end-start);
            	}
        	}
            else if ("subcontext".equalsIgnoreCase(searchType)) {
                result = APIUtil.searchAPIsByURLPattern(userRegistry, searchTerm, start,end);               ;

            }else {
            	result=searchPaginatedAPIs(userRegistry, searchTerm, searchType,start,end,isLazyLoad);
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
	 * Pagination API search based on solr indexing
	 *
	 * @param registry
	 * @param searchTerm
	 * @param searchType
	 * @return
	 * @throws APIManagementException
	 */

    public Map<String,Object> searchPaginatedAPIs(Registry registry, String searchTerm, String searchType,int start,int end, boolean limitAttributes) throws APIManagementException {
        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        List<API> apiList = new ArrayList<API>();

        searchTerm = searchTerm.trim();
        Map<String,Object> result=new HashMap<String, Object>();
        int totalLength=0;
        boolean isMore = false;
        String criteria=APIConstants.API_OVERVIEW_NAME;
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
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            PaginationContext.init(start, end, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);
            if (artifactManager != null) {

                if (APIConstants.API_PROVIDER.equalsIgnoreCase(searchType)) {
                    criteria = APIConstants.API_OVERVIEW_PROVIDER;
                    searchTerm = searchTerm.replaceAll("@", "-AT-");
                } else if (APIConstants.API_VERSION_LABEL.equalsIgnoreCase(searchType)) {
                    criteria = APIConstants.API_OVERVIEW_VERSION;
                } else if (APIConstants.API_CONTEXT.equalsIgnoreCase(searchType)) {
                    criteria = APIConstants.API_OVERVIEW_CONTEXT;
                } else if (APIConstants.API_DESCRIPTION.equalsIgnoreCase(searchType)) {
                    criteria = APIConstants.API_OVERVIEW_DESCRIPTION;
                } else if (APIConstants.API_TAG.equalsIgnoreCase(searchType)) {
                    criteria = APIConstants.API_OVERVIEW_TAG;
                }

                //Create the search attribute map for PUBLISHED APIs
                final String searchValue = searchTerm;
                Map<String, List<String>> listMap = new HashMap<String, List<String>>();
                listMap.put(criteria, new ArrayList<String>() {{
                    add(searchValue);
                }});

                boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();

                //This is due to take only the published APIs from the search if there is no need to return APIs with
                //multiple status. This is because pagination is breaking when we do a another filtering with the API Status
                if (!displayAPIsWithMultipleStatus) {
                    listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
                        add(APIConstants.PUBLISHED);
                    }});
                }

                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength = PaginationContext.getInstance().getLength();

                boolean isFound = true;
                if (genericArtifacts == null || genericArtifacts.length == 0) {

                    if (APIConstants.API_OVERVIEW_PROVIDER.equals(criteria)) {
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
                    result.put("isMore", isMore);
                    return result;
                }

                // Check to see if we can speculate that there are more APIs to be loaded
                if (maxPaginationLimit == totalLength) {
                    isMore = true;  // More APIs exist, cannot determine total API count without incurring perf hit
                    --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
                }

                int tempLength =0;
                for (GenericArtifact artifact : genericArtifacts) {
                    String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

                    if (APIUtil.isAllowDisplayAPIsWithMultipleStatus()) {
                        if (APIConstants.PROTOTYPED.equals(status) || APIConstants.PUBLISHED.equals(status)
                            || APIConstants.DEPRECATED.equals(status)) {
                            API resultAPI;
                            if (limitAttributes) {
                                resultAPI = APIUtil.getAPI(artifact);
                            } else {
                                resultAPI = APIUtil.getAPI(artifact, registry);
                            }
                            if (resultAPI != null) {
                                apiList.add(resultAPI);
                            }
                        }
                    } else {
                        if (APIConstants.PROTOTYPED.equals(status) || APIConstants.PUBLISHED.equals(status)) {
                            API resultAPI;
                            if (limitAttributes) {
                                resultAPI = APIUtil.getAPI(artifact);
                            } else {
                                resultAPI = APIUtil.getAPI(artifact, registry);
                            }
                            if (resultAPI != null) {
                                apiList.add(resultAPI);
                            }
                        }
                    }
                    // Ensure the APIs returned matches the length, there could be an additional API
                    // returned due incrementing the pagination limit when getting from registry
                    tempLength++;
                    if (tempLength >= totalLength){
                        break;
                    }
                }

                apiSet.addAll(apiList);
            }
        } catch (RegistryException e) {
            handleException("Failed to search APIs with type", e);
        }
        result.put("apis",apiSet);
        result.put("length",totalLength);
        result.put("isMore", isMore);
        return result;
    }


    private  GenericArtifact[] searchAPIsByOwner(GenericArtifactManager artifactManager, final String searchValue) throws GovernanceException {
        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        listMap.put(APIConstants.API_OVERVIEW_OWNER, new ArrayList<String>() {
            {
                add(searchValue);
            }
        });
        return artifactManager.findGenericArtifacts(listMap);
    }

    /**
     *This method will delete application key mapping table and application registration table.
     *@param applicationName application Name
     *@param tokenType Token Type.
     *@param groupId group id.
     *@param userName user name.
     *@return
     *@throws APIManagementException
     */
    @Override
    public void cleanUpApplicationRegistration(String applicationName ,String tokenType ,String groupId ,String
            userName) throws APIManagementException{

        Application application = apiMgtDAO.getApplicationByName(applicationName, userName, groupId);
        String applicationId = String.valueOf(application.getId());
        apiMgtDAO.deleteApplicationRegistration(applicationId , tokenType);
        apiMgtDAO.deleteApplicationKeyMappingByApplicationIdAndType(applicationId, tokenType);
        String consumerKey = apiMgtDAO.getConsumerkeyByApplicationIdAndKeyType(applicationId,tokenType);
    }

    /**
     *
     * @param jsonString this string will contain oAuth app details
     * @param userName user name of logged in user.
     * @param clientId this is the consumer key of oAuthApplication
     * @param applicationName this is the APIM appication name.
     * @param keyType
     * @return
     * @throws APIManagementException
     */
    @Override
    public Map<String, Object> mapExistingOAuthClient(String jsonString, String userName, String clientId,
                                                      String applicationName, String keyType)
                                                                        throws APIManagementException {

        String callBackURL = null;

        OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, clientId, callBackURL,
                                                                                 "default",
                                                                                  jsonString);

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();

        // Checking if clientId is mapped with another application.
        if (apiMgtDAO.isMappingExistsforConsumerKey(clientId)) {
            String message = "Consumer Key " + clientId + " is used for another Application.";
            log.error(message);
            throw new APIManagementException(message);
        }
        log.debug("Client ID not mapped previously with another application.");

        //createApplication on oAuthorization server.
        OAuthApplicationInfo oAuthApplication = keyManager.mapOAuthApplication(oauthAppRequest);

        //Do application mapping with consumerKey.
        apiMgtDAO.createApplicationKeyTypeMappingForManualClients(keyType, applicationName, userName, clientId);

        AccessTokenRequest tokenRequest = ApplicationUtils.createAccessTokenRequest(oAuthApplication, null);
        AccessTokenInfo tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);

        //#TODO get actuall values from response and pass.
        Map<String, Object> keyDetails = new HashMap<String, Object>();

        if (tokenInfo != null) {
            keyDetails.put("validityTime", Long.toString(tokenInfo.getValidityPeriod()));
            keyDetails.put("accessToken", tokenInfo.getAccessToken());
        }

        if (oAuthApplication != null) {
            keyDetails.put("consumerKey", oAuthApplication.getClientId());
            keyDetails.put("consumerSecret", oAuthApplication.getParameter("client_secret"));
            keyDetails.put("appDetails", oAuthApplication.getJsonString());
        }

        return keyDetails;

    }

    /** returns the SubscribedAPI object which is related to the subscriptionId
     *
     * @param subscriptionId subscription id
     * @return
     * @throws APIManagementException
     */
    @Override
    public SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException {
        return apiMgtDAO.getSubscriptionById(subscriptionId);
    }

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException {
        return getSubscribedAPIs(subscriber, null);
    }

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> originalSubscribedAPIs;
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

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, applicationName, groupingId);
            if (subscribedAPIs != null && !subscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : subscribedAPIs) {
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi
                            .getTier().getName());
                    // We do not need to add the modified object again.
                }
            }
        } catch (APIManagementException e) {
            handleException("Failed to get APIs of " + subscriber.getName() + " under application " + applicationName, e);
        }
        return subscribedAPIs;
    }

    @Override
    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber, String applicationName,
                                                         int startSubIndex, int endSubIndex, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getPaginatedSubscribedAPIs(subscriber, applicationName, startSubIndex,
                                                                  endSubIndex, groupingId);
            if (subscribedAPIs != null && !subscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : subscribedAPIs) {
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi
                            .getTier().getName());
                    // We do not need to add the modified object again.
                    // subscribedAPIs.add(subscribedApi);
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

    @Override
    public Set<APIIdentifier> getAPIByConsumerKey(String accessToken) throws APIManagementException {
        try {
            return apiMgtDAO.getAPIByConsumerKey(accessToken);
        } catch (APIManagementException e) {
            handleException("Error while obtaining API from API key", e);
        }
        return null;
    }

    @Override
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

    @Override
    public SubscriptionResponse addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        API api = getAPI(identifier);
        WorkflowResponse workflowResponse = null;
        int subscriptionId;
        if (api.getStatus().equals(APIStatus.PUBLISHED)) {
            subscriptionId = apiMgtDAO.addSubscription(identifier, api.getContext(), applicationId,
                    APIConstants.SubscriptionStatus.ON_HOLD, userId);

            boolean isTenantFlowStarted = false;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            String applicationName = apiMgtDAO.getApplicationNameFromId(applicationId);

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
                workflowResponse = addSubscriptionWFExecutor.execute(workflowDTO);
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

            SubscribedAPI addedSubscription = getSubscriptionById(subscriptionId);

            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + identifier.getApiName() + ", API Version " + identifier.getVersion()
                        + ", Subscription Status: " + addedSubscription.getSubStatus() + " subscribe by " + userId
                        + " for app " + applicationName;
                log.debug(logMessage);
            }

            JSONObject subsLogObject = new JSONObject();
            subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getApiName());
            subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, applicationId);
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, applicationName);
            subsLogObject.put(APIConstants.AuditLogConstants.TIER, identifier.getTier());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                    APIConstants.AuditLogConstants.CREATED, this.username);

            return new SubscriptionResponse(addedSubscription.getSubStatus(), addedSubscription.getUUID(),
                    workflowResponse);
        } else {
            throw new APIManagementException("Subscriptions not allowed on APIs in the state: " +
                    api.getStatus().getStatus());
        }
    }

    @Override
    public String getSubscriptionStatusById(int subscriptionId) throws APIManagementException {
        return apiMgtDAO.getSubscriptionStatusById(subscriptionId);
    }

    @Override
    public void removeSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        boolean isTenantFlowStarted = false;

        String providerTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.
                replaceEmailDomainBack(identifier.getProviderName()));

        String applicationName = apiMgtDAO.getApplicationNameFromId(applicationId);

        try {
            if (providerTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                    .equals(providerTenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(providerTenantDomain, true);
                isTenantFlowStarted = true;
            }

            API api = getAPI(identifier);
            SubscriptionWorkflowDTO workflowDTO;
            WorkflowExecutor createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor removeSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            String workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceForSubscription(identifier, applicationId);

            // in a normal flow workflowExtRef is null when workflows are not enabled
            if (workflowExtRef == null) {
                workflowDTO = new SubscriptionWorkflowDTO();
            } else {
                workflowDTO = (SubscriptionWorkflowDTO) apiMgtDAO.retrieveWorkflow(workflowExtRef);

                // set tiername to the workflowDTO only when workflows are enabled
                SubscribedAPI subscription = apiMgtDAO
                        .getSubscriptionById(Integer.parseInt(workflowDTO.getWorkflowReference()));
                workflowDTO.setTierName(subscription.getTier().getName());
            }
            workflowDTO.setApiProvider(identifier.getProviderName());
            workflowDTO.setApiContext(api.getContext());
            workflowDTO.setApiName(identifier.getApiName());
            workflowDTO.setApiVersion(identifier.getVersion());
            workflowDTO.setApplicationName(applicationName);
            workflowDTO.setTenantDomain(tenantDomain);
            workflowDTO.setTenantId(tenantId);
            workflowDTO.setExternalWorkflowReference(workflowExtRef);
            workflowDTO.setSubscriber(userId);
            workflowDTO.setCallbackUrl(removeSubscriptionWFExecutor.getCallbackURL());

            String status = apiMgtDAO.getSubscriptionStatus(identifier, applicationId);
            if (APIConstants.SubscriptionStatus.ON_HOLD.equals(status)) {
                try {
                    createSubscriptionWFExecutor.cleanUpPendingTask(workflowExtRef);
                } catch (WorkflowException ex) {

                    // failed cleanup processes are ignored to prevent failing the deletion process
                    log.warn("Failed to clean pending subscription approval task");
                }
            }

            // update attributes of the new remove workflow to be created
            workflowDTO.setStatus(WorkflowStatus.CREATED);
            workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
            workflowDTO.setCreatedTime(System.currentTimeMillis());
            workflowDTO.setExternalWorkflowReference(removeSubscriptionWFExecutor.generateUUID());
            removeSubscriptionWFExecutor.execute(workflowDTO);

            JSONObject subsLogObject = new JSONObject();
            subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getApiName());
            subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, applicationId);
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, applicationName);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

        } catch (WorkflowException e) {
            String errorMsg = "Could not execute Workflow, " + WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION +
                              " for apiID " + identifier.getApiName();
            handleException(errorMsg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        if (APIUtil.isAPIGatewayKeyCacheEnabled()) {
            invalidateCachedKeys(applicationId);
        }
        if (log.isDebugEnabled()) {
            String logMessage = "API Name: " + identifier.getApiName() + ", API Version " +
                    identifier.getVersion() + " subscription removed from app " + applicationName + " by " + userId;
            log.debug(logMessage);
        }
    }

    /**
     * Removes a subscription specified by SubscribedAPI object
     *
     * @param subscription SubscribedAPI object
     * @throws APIManagementException
     */
    @Override
    public void removeSubscription(SubscribedAPI subscription) throws APIManagementException {
        String uuid = subscription.getUUID();
        SubscribedAPI subscribedAPI = apiMgtDAO.getSubscriptionByUUID(uuid);
        if (subscribedAPI != null) {
            Application application = subscribedAPI.getApplication();
            APIIdentifier identifier = subscribedAPI.getApiId();
            String userId = application.getSubscriber().getName();
            removeSubscription(identifier, userId, application.getId());
            if (log.isDebugEnabled()) {
                String appName = application.getName();
                String logMessage =
                        "API Name: " + identifier.getApiName() + ", API Version " + identifier.getVersion() +
                                " subscription (uuid : " + uuid + ") removed from app " + appName;
                log.debug(logMessage);
            }
        } else {
            throw new APIManagementException("Subscription for UUID:" + uuid +" does not exist.");
        }
    }

    /**
     *
     * @param applicationId Application ID related cache keys to be cleared
     * @throws APIManagementException
     */
    private void invalidateCachedKeys(int applicationId) throws APIManagementException {
        CacheInvalidator.getInstance().invalidateCacheForApp(applicationId);
    }

    @Override
    public void removeSubscriber(APIIdentifier identifier, String userId)
            throws APIManagementException {
        throw new UnsupportedOperationException("Unsubscribe operation is not yet implemented");
    }

    @Override
    public void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException {
        API api = getAPI(identifier);
        apiMgtDAO.updateSubscriptions(identifier, api.getContext(), applicationId, userId);
    }

    @Override
    public void addComment(APIIdentifier identifier, String commentText, String user) throws APIManagementException {
        apiMgtDAO.addComment(identifier, commentText, user);
    }

    @Override
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
    @Override
    public int addApplication(Application application, String userId)
            throws APIManagementException {

        if (APIUtil.isApplicationExist(userId, application.getName(), application.getGroupId())) {
            handleResourceAlreadyExistsException(
                    "A duplicate application already exists by the name - " + application.getName());
        }
        //check whether callback url is empty and set null
        if (StringUtils.isBlank(application.getCallbackUrl())) {
            application.setCallbackUrl(null);
        }
        int applicationId = apiMgtDAO.addApplication(application, userId);

        JSONObject appLogObject = new JSONObject();
        appLogObject.put(APIConstants.AuditLogConstants.NAME, application.getName());
        appLogObject.put(APIConstants.AuditLogConstants.TIER, application.getTier());
        appLogObject.put(APIConstants.AuditLogConstants.CALLBACK, application.getCallbackUrl());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                APIConstants.AuditLogConstants.CREATED, this.username);

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

        if (log.isDebugEnabled()) {
            log.debug("Application Name: " + application.getName() +" added successfully.");
        }

        return applicationId;
    }

    /** Updates an Application identified by its id
     *
     * @param application Application object to be updated
     * @throws APIManagementException
     */
    @Override
    public void updateApplication(Application application) throws APIManagementException {

        Application existingApp;
        String uuid = application.getUUID();
        if (!StringUtils.isEmpty(uuid)) {
            existingApp = apiMgtDAO.getApplicationByUUID(uuid);
            application.setId(existingApp.getId());
        } else {
            existingApp = apiMgtDAO.getApplicationById(application.getId());
        }

        if (existingApp != null && APIConstants.ApplicationStatus.APPLICATION_CREATED.equals(existingApp.getStatus())) {
            throw new APIManagementException("Cannot update the application while it is INACTIVE");
        }
        //validate callback url
        if(!APIUtil.isValidURL(application.getCallbackUrl())){
            log.warn("Invalid Call Back URL "+ application.getCallbackUrl());
        }

        apiMgtDAO.updateApplication(application);
        if (log.isDebugEnabled()) {
            log.debug("Successfully updated the Application: " + application.getId() +" in the database.");
        }

        JSONObject appLogObject = new JSONObject();
        appLogObject.put(APIConstants.AuditLogConstants.NAME, application.getName());
        appLogObject.put(APIConstants.AuditLogConstants.TIER, application.getTier());
        appLogObject.put(APIConstants.AuditLogConstants.STATUS, existingApp != null ? existingApp.getStatus() : "");
        appLogObject.put(APIConstants.AuditLogConstants.CALLBACK, application.getCallbackUrl());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                APIConstants.AuditLogConstants.UPDATED, this.username);

        APIKey[] apiKeys = null;

        // Update on OAuthApps are performed by
        if ((application.getCallbackUrl() != null && existingApp != null &&
                !application.getCallbackUrl().equals(existingApp.getCallbackUrl())) ||
                (existingApp != null && !application.getName().equals(existingApp.getName()))) {

            // Only the OauthApps created from UI will be changed. Mapped Clients won't be touched.
            apiKeys = apiMgtDAO.getConsumerKeysWithMode(application.getId(),
                                                        APIConstants.OAuthAppMode.CREATED.toString());
        }
        if (apiKeys != null && apiKeys.length > 0) {
            for (APIKey apiKey : apiKeys) {
                OAuthApplicationInfo applicationInfo = new OAuthApplicationInfo();
                applicationInfo.setClientId(apiKey.getConsumerKey());

                if (application.getCallbackUrl() != null &&
                    application.getCallbackUrl().equals(existingApp.getCallbackUrl())) {
                    applicationInfo.setCallBackURL(application.getCallbackUrl());
                }

                if (application.getName() != null && !application.getName().equals(existingApp.getName())) {
                    applicationInfo.setClientName(application.getName() + '_' + apiKey.getType());
                }
                applicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, application.getSubscriber().getName());

                // This parameter is set as a way of indicating from which point updateApplication was called. When
                // integrating with different OAuthProviders, if the implementers do not wish to change CallBackUrl
                // when an update is performed on the AM_Application, then using this variable that update can be
                // ignored.
                applicationInfo.addParameter("executing_mode", "AM_APPLICATION_UPDATE");

                OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
                oAuthAppRequest.setOAuthApplicationInfo(applicationInfo);
                KeyManagerHolder.getKeyManagerInstance().updateApplication(oAuthAppRequest);

            }
        }
        try {
            invalidateCachedKeys(application.getId());
        } catch (APIManagementException ignore) {
            //Log and ignore since we do not want to throw exceptions to the front end due to cache invalidation failure.
            log.warn("Failed to invalidate Gateway Cache " + ignore.getMessage(), ignore);
        }
    }

    /**
     * Function to remove an Application from the API Store
     *
     * @param application - The Application Object that represents the Application
     * @throws APIManagementException
     */
    @Override
    public void removeApplication(Application application) throws APIManagementException {
        String uuid = application.getUUID();
        if (application.getId() == 0 && !StringUtils.isEmpty(uuid)) {
            application = apiMgtDAO.getApplicationByUUID(uuid);
        }
        boolean isTenantFlowStarted = false;
        int applicationId = application.getId();

        try {
            String workflowExtRef;
            ApplicationWorkflowDTO workflowDTO;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            WorkflowExecutor createApplicationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            WorkflowExecutor createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor createProductionRegistrationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            WorkflowExecutor createSandboxRegistrationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
            WorkflowExecutor removeApplicationWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);

            workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceByApplicationID(application.getId());

            // in a normal flow workflowExtRef is null when workflows are not enabled
            if (workflowExtRef == null) {
                workflowDTO = new ApplicationWorkflowDTO();
            } else {
                workflowDTO = (ApplicationWorkflowDTO) apiMgtDAO.retrieveWorkflow(workflowExtRef);
            }
            workflowDTO.setApplication(application);
            workflowDTO.setCallbackUrl(removeApplicationWFExecutor.getCallbackURL());
            workflowDTO.setUserName(this.username);
            workflowDTO.setTenantDomain(tenantDomain);
            workflowDTO.setTenantId(tenantId);

            // Remove from cache first since we won't be able to find active access tokens
            // once the application is removed.
            invalidateCachedKeys(application.getId());

            // clean up pending subscription tasks
            Set<Integer> pendingSubscriptions = apiMgtDAO.getPendingSubscriptionsByApplicationId(applicationId);
            for (int subscription : pendingSubscriptions) {
                try {
                    workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceForSubscription(subscription);
                    createSubscriptionWFExecutor.cleanUpPendingTask(workflowExtRef);
                } catch (APIManagementException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to get external workflow reference for subscription " + subscription);
                } catch (WorkflowException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to clean pending subscription approval task: " + subscription);
                }
            }

            // cleanup pending application registration tasks
            String productionKeyStatus = apiMgtDAO
                    .getRegistrationApprovalState(applicationId, APIConstants.API_KEY_TYPE_PRODUCTION);
            String sandboxKeyStatus = apiMgtDAO
                    .getRegistrationApprovalState(applicationId, APIConstants.API_KEY_TYPE_SANDBOX);
            if (WorkflowStatus.CREATED.toString().equals(productionKeyStatus)) {
                try {
                    workflowExtRef = apiMgtDAO
                            .getRegistrationWFReference(applicationId, APIConstants.API_KEY_TYPE_PRODUCTION);
                    createProductionRegistrationWFExecutor.cleanUpPendingTask(workflowExtRef);
                } catch (APIManagementException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to get external workflow reference for production key of application "
                            + applicationId);
                } catch (WorkflowException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to clean pending production key approval task of " + applicationId);
                }
            }
            if (WorkflowStatus.CREATED.toString().equals(sandboxKeyStatus)) {
                try {
                    workflowExtRef = apiMgtDAO
                            .getRegistrationWFReference(applicationId, APIConstants.API_KEY_TYPE_SANDBOX);
                    createSandboxRegistrationWFExecutor.cleanUpPendingTask(workflowExtRef);
                } catch (APIManagementException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to get external workflow reference for sandbox key of application "
                            + applicationId);
                } catch (WorkflowException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to clean pending sandbox key approval task of " + applicationId);
                }
            }
            if (workflowExtRef != null) {
                try {
                    createApplicationWFExecutor.cleanUpPendingTask(workflowExtRef);
                } catch (WorkflowException ex) {

                    // failed cleanup processes are ignored to prevent failing the application removal process
                    log.warn("Failed to clean pending application approval task of " + applicationId);
                }
            }

            // update attributes of the new remove workflow to be created
            workflowDTO.setStatus(WorkflowStatus.CREATED);
            workflowDTO.setCreatedTime(System.currentTimeMillis());
            workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
            workflowDTO.setExternalWorkflowReference(removeApplicationWFExecutor.generateUUID());

            removeApplicationWFExecutor.execute(workflowDTO);

            JSONObject appLogObject = new JSONObject();
            appLogObject.put(APIConstants.AuditLogConstants.NAME, application.getName());
            appLogObject.put(APIConstants.AuditLogConstants.TIER, application.getTier());
            appLogObject.put(APIConstants.AuditLogConstants.CALLBACK, application.getCallbackUrl());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

        } catch (WorkflowException e) {
            String errorMsg = "Could not execute Workflow, " + WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION + " " +
                    "for applicationID " + application.getId();
            handleException(errorMsg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        if (log.isDebugEnabled()) {
            String logMessage = "Application Name: " + application.getName() + " successfully removed";
            log.debug(logMessage);
        }
    }

    /**
     * This method specifically implemented for REST API by removing application and data access logic
     * from host object layer. So as per new implementation we need to pass requested scopes to this method
     * as tokenScope. So we will do scope related other logic here in this method.
     * So host object should only pass required 9 parameters.
     * */
     @Override
    public Map<String, Object> requestApprovalForApplicationRegistration(String userId, String applicationName,
                                                                         String tokenType, String callbackUrl,
                                                                         String[] allowedDomains, String validityTime,
                                                                         String tokenScope, String groupingId,
                                                                         String jsonString
                                                                         )
            throws APIManagementException {

        boolean isTenantFlowStarted = false;

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Unable to retrieve the tenant information of the current user.", e);
        }
        //checking for authorized scopes
        Set<Scope> scopeSet = new LinkedHashSet<Scope>();
        List<Scope> authorizedScopes = new ArrayList<Scope>();
        String authScopeString;
        if (tokenScope != null && tokenScope.length() != 0 &&
                !APIConstants.OAUTH2_DEFAULT_SCOPE.equals(tokenScope)) {
            scopeSet.addAll(getScopesByScopeKeys(tokenScope, tenantId));
            authorizedScopes = getAllowedScopesForUserApplication(userId, scopeSet);
        }

        if (!authorizedScopes.isEmpty()) {
            StringBuilder scopeBuilder = new StringBuilder();
            for (Scope scope : authorizedScopes) {
                scopeBuilder.append(scope.getKey()).append(' ');
            }
            authScopeString = scopeBuilder.toString();
        } else {
            authScopeString = APIConstants.OAUTH2_DEFAULT_SCOPE;
        }

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
            } else {
                throw new APIManagementException("Invalid Token Type '" + tokenType + "' requested.");
            }

            //check whether callback url is empty and set null
            if (StringUtils.isBlank(callbackUrl)) {
                callbackUrl = null;
            }
            // Build key manager instance and create oAuthAppRequest by jsonString.
            OAuthAppRequest request =
                    ApplicationUtils.createOauthAppRequest(applicationName, null,
                            callbackUrl, authScopeString, jsonString);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.VALIDITY_PERIOD, validityTime);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);

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
                keyDetails.put("appDetails", applicationInfo.getJsonString());
            }

            // There can be instances where generating the Application Token is
            // not required. In those cases,
            // token info will have nothing.
            AccessTokenInfo tokenInfo = appRegWFDto.getAccessTokenInfo();
            if (tokenInfo != null) {
                keyDetails.put("accessToken", tokenInfo.getAccessToken());
                keyDetails.put("validityTime", tokenInfo.getValidityPeriod());
                keyDetails.put("tokenDetails", tokenInfo.getJSONString());
                keyDetails.put("tokenScope", tokenInfo.getScopes());
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

    private static List<Scope> getAllowedScopesForUserApplication(String username,
                                                                  Set<Scope> reqScopeSet) {
        String[] userRoles = null;
        org.wso2.carbon.user.api.UserStoreManager userStoreManager = null;

        List<Scope> authorizedScopes = new ArrayList<Scope>();
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(MultitenantUtils.getTenantDomain(username));
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            userRoles = userStoreManager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            // Log and return since we do not want to stop issuing the token in
            // case of scope validation failures.
            log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
        }

        List<String> userRoleList;
        if (userRoles != null) {
            userRoleList = new ArrayList<String>(Arrays.asList(userRoles));
        } else {
            userRoleList = Collections.emptyList();
        }

        //Iterate the requested scopes list.
        for (Scope scope : reqScopeSet) {
            //Get the set of roles associated with the requested scope.
            String roles = scope.getRoles();

            //If the scope has been defined in the context of the App and if roles have been defined for the scope
            if (roles != null && roles.length() != 0) {
                List<String> roleList =
                        new ArrayList<String>(Arrays.asList(roles.replaceAll(" ", EMPTY_STRING).split(",")));
                //Check if user has at least one of the roles associated with the scope
                roleList.retainAll(userRoleList);
                if (!roleList.isEmpty()) {
                    authorizedScopes.add(scope);
                }
            }
        }

        return authorizedScopes;
    }

    @Override
    public Map<String, String> completeApplicationRegistration(String userId, String applicationName, String tokenType,
                                                               String tokenScope, String groupingId)
            throws APIManagementException {

        Application application = apiMgtDAO.getApplicationByName(applicationName, userId, groupingId);
        String status = apiMgtDAO.getRegistrationApprovalState(application.getId(), tokenType);
        Map<String, String> keyDetails = null;
        if (!application.getSubscriber().getName().equals(userId)) {
            userId = application.getSubscriber().getName();
        }
        String workflowReference = apiMgtDAO.getWorkflowReference(applicationName, userId);
        if (workflowReference != null) {
            WorkflowDTO workflowDTO = null;

            // Creating workflowDTO for the correct key type.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            } else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) {
                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
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
                        AbstractApplicationRegistrationWorkflowExecutor.dogenerateKeysForApplication
                                (registrationWorkflowDTO);
                        AccessTokenInfo tokenInfo = registrationWorkflowDTO.getAccessTokenInfo();
                        OAuthApplicationInfo oauthApp = registrationWorkflowDTO.getApplicationInfo();
                        keyDetails = new HashMap<String, String>();

                        if (tokenInfo != null) {
                            keyDetails.put("accessToken", tokenInfo.getAccessToken());
                            keyDetails.put("validityTime", Long.toString(tokenInfo.getValidityPeriod()));
                            keyDetails.put("tokenDetails", tokenInfo.getJSONString());
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
    @Override
    public Application getApplicationsByName(String userId, String ApplicationName, String groupingId) throws
            APIManagementException {

        return apiMgtDAO.getApplicationByName(ApplicationName, userId, groupingId);
    }

    /**
     * Returns the corresponding application given the Id
     * @param id Id of the Application
     * @return it will return Application corresponds to the id.
     * @throws APIManagementException
     */
    @Override
    public Application getApplicationById(int id) throws APIManagementException {
        return apiMgtDAO.getApplicationById(id);
    }

    /** get the status of the Application creation process given the application Id
     *
     * @param applicationId Id of the Application
     * @return
     * @throws APIManagementException
     */
    @Override
    public String getApplicationStatusById(int applicationId) throws APIManagementException {
        return apiMgtDAO.getApplicationStatusById(applicationId);
    }

    @Override
    public boolean isApplicationTokenExists(String accessToken) throws APIManagementException {
        return apiMgtDAO.isAccessTokenExists(accessToken);
    }

    @Override
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

    /**
     * Returns a list of tiers denied
     *
     * @return Set<Tier>
     */
    @Override
    public Set<String> getDeniedTiers() throws APIManagementException {
        Set<String> deniedTiers = new HashSet<String>();
        String[] currentUserRoles;
        try {
            if (tenantId != 0) {
                /* Get the roles of the Current User */
                currentUserRoles = ((UserRegistry) ((UserAwareAPIConsumer) this).registry).
                        getUserRealm().getUserStoreManager().getRoleListOfUser(((UserRegistry) this.registry)
                        .getUserName());

                Set<TierPermissionDTO> tierPermissions;

                if(APIUtil.isAdvanceThrottlingEnabled()){
                    tierPermissions = apiMgtDAO.getThrottleTierPermissions(tenantId);
                }else{
                    tierPermissions = apiMgtDAO.getTierPermissions(tenantId);
                }

                for (TierPermissionDTO tierPermission : tierPermissions) {
                    String type = tierPermission.getPermissionType();

                    List<String> currentRolesList = new ArrayList<String>(Arrays.asList(currentUserRoles));
                    List<String> roles = new ArrayList<String>(Arrays.asList(tierPermission.getRoles()));
                    currentRolesList.retainAll(roles);

                    if (APIConstants.TIER_PERMISSION_ALLOW.equals(type)) {
                        /* Current User is not allowed for this Tier*/
                        if (currentRolesList.isEmpty()) {
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
            log.error("cannot retrieve user role list for tenant" + tenantDomain, e);
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
    @Override
    public boolean isTierDeneid(String tierName) throws APIManagementException {
        String[] currentUserRoles;
        try {
            if (tenantId != 0) {
                /* Get the roles of the Current User */
                currentUserRoles = ((UserRegistry) ((UserAwareAPIConsumer) this).registry).
                        getUserRealm().getUserStoreManager().getRoleListOfUser(((UserRegistry) this.registry).getUserName());
                TierPermissionDTO tierPermission;

                if(APIUtil.isAdvanceThrottlingEnabled()){
                    tierPermission = apiMgtDAO.getThrottleTierPermission(tierName, tenantId);
                }else{
                    tierPermission = apiMgtDAO.getTierPermission(tierName, tenantId);
                }
                if (tierPermission == null) {
                    return false;
                } else {
                    List<String> currentRolesList = new ArrayList<String>(Arrays.asList(currentUserRoles));
                    List<String> roles = new ArrayList<String>(Arrays.asList(tierPermission.getRoles()));
                    currentRolesList.retainAll(roles);
                    if (APIConstants.TIER_PERMISSION_ALLOW.equals(tierPermission.getPermissionType())) {
                        if (currentRolesList.isEmpty()) {
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
            log.error("cannot retrieve user role list for tenant" + tenantDomain, e);
        }
        return false;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
    	if (this.tenantDomain != null) {
    		return !(this.tenantDomain.equals(tenantDomain));
    	}
    	return true;
    }

	@Override
	public Set<API> searchAPI(String searchTerm, String searchType, String tenantDomain)
	                                                                                    throws APIManagementException {
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
    public String getGroupIds(String response) throws APIManagementException {
        String groupingExtractorClass = APIUtil.getGroupingExtractorImplementation();
        if (groupingExtractorClass != null) {
            try {
                LoginPostExecutor groupingExtractor = (LoginPostExecutor) APIUtil.getClassForName
                        (groupingExtractorClass).newInstance();
                return groupingExtractor.getGroupingIdentifiers(response);
            } catch (ClassNotFoundException e) {
                handleException(groupingExtractorClass + " is not found in run time", e);
                return null;
            } catch (IllegalAccessException e) {
                handleException("Error occurred while invocation of getGroupingIdentifier method", e);
                return null;
            } catch (InstantiationException e) {
                handleException("Error occurred while instantiating " + groupingExtractorClass + " class", e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId,int start , int offset
            , String search, String sortColumn, String sortOrder)
            throws APIManagementException {
        return apiMgtDAO.getApplicationsWithPagination(subscriber, groupingId, start, offset, search,sortColumn,sortOrder);
    }

    @Override
    public Application[] getApplications(Subscriber subscriber, String groupingId)
			throws APIManagementException {
		return apiMgtDAO.getApplications(subscriber, groupingId);
	}

    /**
     * @param userId Subscriber name.
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

        oauthAppRequest.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);

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
            }
        }
    }

	public JSONObject resumeWorkflow(Object[] args) {
    	JSONObject row = new JSONObject();

        if (args != null && APIUtil.isStringArray(args)) {

            String workflowReference = (String) args[0];
            String status = (String) args[1];
            String description = null;
            if (args.length > 2 && args[2] != null) {
                description = (String) args[2];
            }

            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            boolean isTenantFlowStarted = false;

            try {
                //                if (workflowReference != null) {
                WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReference);
                if (workflowDTO == null) {
                    log.error("Could not find workflow for reference " + workflowReference);

                    row.put("error", Boolean.TRUE);
                    row.put("statusCode", 500);
                    row.put("message", "Could not find workflow for reference " + workflowReference);
                    return row;
                }

                String tenantDomain = workflowDTO.getTenantDomain();
                if (tenantDomain != null && !org.wso2.carbon.utils.multitenancy.MultitenantConstants
                        .SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }

                workflowDTO.setWorkflowDescription(description);
                workflowDTO.setStatus(WorkflowStatus.valueOf(status));

                String workflowType = workflowDTO.getWorkflowType();
                WorkflowExecutor workflowExecutor;
                try {
                    workflowExecutor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
                    workflowExecutor.complete(workflowDTO);
                } catch (WorkflowException e) {
                    throw new APIManagementException(e);
                }
                row.put("error", Boolean.FALSE);
                row.put("statusCode", 200);
                row.put("message", "Invoked workflow completion successfully.");
                //                }
            } catch (IllegalArgumentException e) {
                String msg = "Illegal argument provided. Valid values for status are APPROVED and REJECTED.";
                log.error(msg, e);

                row.put("error", Boolean.TRUE);
                row.put("statusCode", 500);
                row.put("message", msg);

            } catch (APIManagementException e) {
                String msg = "Error while resuming the workflow. ";
                log.error(msg, e);

                row.put("error", Boolean.TRUE);
                row.put("statusCode", 500);
                row.put("message", msg + e.getMessage());
            } finally {
                if (isTenantFlowStarted) {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
        return row;
    }

    @Override
    public boolean isMonetizationEnabled(String tenantDomain) throws APIManagementException {
        JSONObject apiTenantConfig = null;
        try {
            String content = apimRegistryService.getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);

            if (content != null) {
                JSONParser parser = new JSONParser();
                apiTenantConfig = (JSONObject) parser.parse(content);
            }

        } catch (UserStoreException e) {
            handleException("UserStoreException thrown when getting API tenant config from registry", e);
        } catch (RegistryException e) {
            handleException("RegistryException thrown when getting API tenant config from registry", e);
        } catch (ParseException e) {
            handleException("ParseException thrown when passing API tenant config from registry", e);
        }

        return getTenantConfigValue(tenantDomain, apiTenantConfig, APIConstants.API_TENANT_CONF_ENABLE_MONITZATION_KEY);
    }

    private boolean getTenantConfigValue(String tenantDomain, JSONObject apiTenantConfig, String configKey) throws APIManagementException {
        if (apiTenantConfig != null) {
            Object value = apiTenantConfig.get(configKey);

            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            }
            else {
                throw new APIManagementException(configKey + " config does not exist for tenant " + tenantDomain);
            }
        }
        return false;
    }

}
