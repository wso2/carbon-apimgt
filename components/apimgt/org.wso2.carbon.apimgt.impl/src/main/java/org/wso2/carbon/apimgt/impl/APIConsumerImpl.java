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

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidator;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.monetization.DefaultMonetizationImpl;
import org.wso2.carbon.apimgt.impl.token.ApiKeyGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.ApplicationUtils;
import org.wso2.carbon.apimgt.impl.utils.ContentSearchResultNameComparator;
import org.wso2.carbon.apimgt.impl.workflow.AbstractApplicationRegistrationWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.impl.wsdl.WSDLProcessor;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
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
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import javax.wsdl.Definition;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class APIConsumerImpl extends AbstractAPIManager implements APIConsumer {

    private static final Log log = LogFactory.getLog(APIConsumerImpl.class);

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    public static final char COLON_CHAR = ':';
    public static final String EMPTY_STRING = "";

    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String ENVIRONMENT_TYPE = "environmentType";
    public static final String API_NAME = "apiName";
    public static final String API_VERSION = "apiVersion";
    public static final String API_PROVIDER = "apiProvider";
    private static final String PRESERVED_CASE_SENSITIVE_VARIABLE = "preservedCaseSensitive";

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
    protected APIMRegistryService apimRegistryService;
    protected String userNameWithoutChange;

    public APIConsumerImpl() throws APIManagementException {
        super();
        readTagCacheConfigs();
    }

    public APIConsumerImpl(String username, APIMRegistryService apimRegistryService) throws APIManagementException {
        super(username);
        userNameWithoutChange = username;
        readTagCacheConfigs();
        this.apimRegistryService = apimRegistryService;
    }

    private void readTagCacheConfigs() {
        APIManagerConfiguration config = getAPIManagerConfiguration();
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
                isTenantFlowStarted = startTenantFlowForTenantDomain(requestedTenantDomain);
            }

            if ((isTenantMode && this.tenantDomain == null) ||
                (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {//Tenant store anonymous mode
                int tenantId = getTenantId(requestedTenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = getGovernanceUserRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
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
                endTenantFlow();
            }
        }

		return apisWithTag;
	}

    protected void setUsernameToThreadLocalCarbonContext(String username) {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
    }

    protected UserRegistry getGovernanceUserRegistry(int tenantId) throws RegistryException {
        return ServiceReferenceHolder.getInstance().getRegistryService().
                getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
    }

    protected int getTenantId(String requestedTenantDomain) throws UserStoreException {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                             .getTenantId(requestedTenantDomain);
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
                    GovernanceUtils.findGovernanceArtifacts(getSearchQuery(APIConstants.TAGS_EQ_SEARCH_TYPE_PREFIX + tag), registry,
                                                            APIConstants.API_RXT_MEDIA_TYPE);
            for (GovernanceArtifact genericArtifact : genericArtifacts) {
                try {
                    String apiStatus = APIUtil.getLcStateFromArtifact(genericArtifact);
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
                int tenantId = getTenantId(tenantDomain);
                userRegistry = getGovernanceUserRegistry(tenantId);
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
                    String status = APIUtil.getLcStateFromArtifact(artifact);

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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain
                        + " when retrieving APIs for store. User : " + PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getUsername();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
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
        Boolean displayAPIsWithMultipleStatus = false;
        try {
            if (tenantDomain != null) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
        }finally {
            endTenantFlow();
        }
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
                int tenantId = getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = getGovernanceUserRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
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
                    if (artifact == null) {
                        log.error("Failed to retrieve artifact when getting paginated published API.");
                        continue;
                    }
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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain
                        + " when retrieving all Published APIs.";
                log.error(errorMessage);
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
     * The method to get Light Weight APIs to Store view
     * @param tenantDomain tenant domain
     * @param start start limit
     * @param end end limit
     * @return Set<API>  Set of APIs
     * @throws APIManagementException
     */
    public Map<String, Object> getAllPaginatedPublishedLightWeightAPIs(String tenantDomain, int start, int end)
            throws APIManagementException {
        Boolean displayAPIsWithMultipleStatus = false;
        try {
            if (tenantDomain != null) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();
        } finally {
            endTenantFlow();
        }
        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        //Check the api-manager.xml config file entry <DisplayAllAPIs> value is false
        if (!displayAPIsWithMultipleStatus) {
            //Create the search attribute map
            listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
                add(APIConstants.PUBLISHED);
            }});
        } else {
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
                int tenantId = getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = getGovernanceUserRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
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
                    if (artifact == null) {
                        log.error("Failed to retrieve artifact when getting paginated published API.");
                        continue;
                    }
                    // adding the API provider can mark the latest API .
                    API api = APIUtil.getLightWeightAPI(artifact);
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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain +
                        " when retrieving all Published APIs.";
                log.error(errorMessage);
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
    public Map<String, Object> getAllPaginatedLightWeightAPIsByStatus(String tenantDomain,
                                                                      int start, int end, final String[] apiStatus,
                                                                      boolean returnAPITags)
            throws APIManagementException {

        Map<String, Object> result = new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength = 0;
        boolean isMore = false;
        String criteria = "lcState=";

        try {
            Registry userRegistry;
            boolean isTenantMode = (tenantDomain != null);
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {
                //Tenant store anonymous mode
                int tenantId = getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                        getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
            String paginationLimit = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);

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
                    List<GovernanceArtifact> genericArtifacts = GovernanceUtils.findGovernanceArtifacts
                            (getSearchQuery(criteria), userRegistry, APIConstants.API_RXT_MEDIA_TYPE);
                    totalLength = PaginationContext.getInstance().getLength();
                    if (genericArtifacts == null || genericArtifacts.size() == 0) {
                        result.put("apis", apiSortedSet);
                        result.put("totalLength", totalLength);
                        result.put("isMore", isMore);
                        return result;
                    }

                    // Check to see if we can speculate that there are more APIs to be loaded
                    if (maxPaginationLimit == totalLength) {
                        isMore = true;  // More APIs exist so we cannot determine the total API count without
                        // incurring a performance hit
                        --totalLength; // Remove the additional 1 we added earlier when setting max pagination limit
                    }
                    int tempLength = 0;
                    for (GovernanceArtifact artifact : genericArtifacts) {
                        API api = null;
                        try {
                            api = APIUtil.getLightWeightAPI(artifact);
                        } catch (APIManagementException e) {
                            //log and continue since we want to load the rest of the APIs.
                            log.error("Error while loading API " + artifact.getAttribute(
                                    APIConstants.API_OVERVIEW_NAME),
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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain +
                        " when retrieving all paginated APIs by status.";
                log.error(errorMessage);
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
     * Regenerate consumer secret.
     *
     * @param clientId For which consumer key we need to regenerate consumer secret.
     * @return New consumer secret.
     * @throws APIManagementException This is the custom exception class for API management.
     */
    public String renewConsumerSecret(String clientId) throws APIManagementException {
        // Create Token Request with parameters provided from UI.
        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId(clientId);

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        return keyManager.getNewApplicationConsumerSecret(tokenRequest);
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

        Map<String, Object> result = new HashMap<String, Object>();
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        SortedSet<API> apiVersionsSortedSet = new TreeSet<API>(new APIVersionComparator());
        int totalLength = 0;
        boolean isMore = false;
        String criteria = APIConstants.LCSTATE_SEARCH_TYPE_KEY;

        try {
            Registry userRegistry;
            boolean isTenantMode = (tenantDomain != null);
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = getGovernanceUserRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
            String paginationLimit = getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);

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
                    List<GovernanceArtifact> genericArtifacts = GovernanceUtils.findGovernanceArtifacts
                            (getSearchQuery(criteria), userRegistry, APIConstants.API_RXT_MEDIA_TYPE);
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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain
                        + " when retrieving all paginated APIs by status.";
                log.error(errorMessage);
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
        try {
            if (tenantDomain != null) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
        }finally {
            endTenantFlow();
        }
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
                int tenantId = getTenantId(tenantDomain);
                // explicitly load the tenant's registry
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = getGovernanceUserRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
            }
            this.isTenantModeStoreView = isTenantMode;
            this.requestedTenant = tenantDomain;

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            List<API> multiVersionedAPIs = new ArrayList<API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
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

                    if (artifact == null) {
                        log.error("Failed to retrieve artifact when getting all paginated APIs by status.");
                        continue;
                    }
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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain
                        + " when retrieving APIs by status.";
                log.error(errorMessage);
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

            JSONObject appLogObject = new JSONObject();
            appLogObject.put("Re-Generated Keys for application with client Id", clientId);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);

            return keyManager.getNewApplicationAccessToken(tokenRequest);
        } catch (APIManagementException e) {
            log.error("Error while re-generating AccessToken", e);
            throw e;
        }
    }

    @Override
    public String generateApiKey(Application application, String userName, long validityPeriod) throws APIManagementException {

        JwtTokenInfoDTO jwtTokenInfoDTO = APIUtil.getJwtTokenInfoDTO(application, userName,
                MultitenantUtils.getTenantDomain(userName));

        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setId(application.getId());
        applicationDTO.setName(application.getName());
        applicationDTO.setOwner(application.getOwner());
        applicationDTO.setTier(application.getTier());
        applicationDTO.setUuid(application.getUUID());
        jwtTokenInfoDTO.setApplication(applicationDTO);

        jwtTokenInfoDTO.setSubscriber(userName);
        jwtTokenInfoDTO.setExpirationTime(validityPeriod);
        jwtTokenInfoDTO.setKeyType(application.getKeyType());

        return ApiKeyGenerator.generateToken(jwtTokenInfoDTO);
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
                int tenantId = getTenantId(tenantDomain);
                userRegistry = getGovernanceUserRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
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
                        if (artifact == null) {
                            log.error("Failed to retrieve artifact when getting all paginated APIs.");
                            continue;
                        }
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
    //                            key = api.getId().getProviderName() + ":" + api.getId().getApiName() + ":" + api.getId()
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
                            if (artifact == null) {
                                log.error("Failed to retrieve artifact when getting deprecated APIs.");
                                continue;
                            }
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
            } else {
                String errorMessage = "Artifact manager is null for tenant domain " + tenantDomain
                        + " when retrieving all paginated APIs.";
                log.error(errorMessage);
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
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when retrieving top rated APIs.";
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
            if (genericArtifacts == null || genericArtifacts.length == 0) {
                return apiSortedSet;
            }
            for (GenericArtifact genericArtifact : genericArtifacts) {
                String status = APIUtil.getLcStateFromArtifact(genericArtifact);
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
        APIManagerConfiguration config = getAPIManagerConfiguration();
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
                int tenantId = getTenantId(tenantDomain);
                // explicitly load the tenant's registry
      	      	APIUtil.loadTenantRegistry(tenantId);
                setUsernameToThreadLocalCarbonContext(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
                isTenantFlowStarted = true;
                userRegistry = getGovernanceUserRegistry(tenantId);
            } else {
                userRegistry = registry;
                setUsernameToThreadLocalCarbonContext(this.username);
                isTenantFlowStarted = true;
            }
            if (isRecentlyAddedAPICacheEnabled) {
                boolean isStatusChanged = false;
                Set<API> recentlyAddedAPI = (Set<API>) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).get(username + COLON_CHAR + tenantDomain);
                if (recentlyAddedAPI != null) {
                    for (API api : recentlyAddedAPI) {
                        try {
                            if (!APIConstants.PUBLISHED.equalsIgnoreCase(userRegistry.get(APIUtil.getAPIPath(api.getId())).getProperty(APIConstants.API_STATUS))) {
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
                    APIConstants.CREATED_DATE, Integer.MAX_VALUE);
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        	listMap.put(APIConstants.API_OVERVIEW_STATUS, new ArrayList<String>() {{
        		add(APIConstants.PUBLISHED);
        	}});
            listMap.put(APIConstants.STORE_VIEW_ROLES, getUserRoleList());
            String searchCriteria = APIConstants.LCSTATE_SEARCH_KEY + "= (" + APIConstants.PUBLISHED + ")";

        	//Find UUID
        	GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
        	if (artifactManager != null) {
                GenericArtifact[] genericArtifacts = artifactManager.findGovernanceArtifacts(getSearchQuery(searchCriteria));
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
            } else {
                String errorMessage = "Artifact manager is null when retrieving recently added APIs for tenant domain "
                        + tenantDomain;
                log.error(errorMessage);
            }
        } catch (RegistryException e) {
        	handleException("Failed to get all published APIs", e);
        } catch (UserStoreException e) {
        	handleException("Failed to get all published APIs", e);
        } finally {
        	PaginationContext.destroy();
        	if (isTenantFlowStarted) {
                endTenantFlow();
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
                int tenantId = getTenantId(this.requestedTenant);
                userRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                        getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                userRegistry = registry;
            }

            Map<String, Tag> tagsData = new HashMap<String, Tag>();
            try {
            	PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(((UserRegistry)userRegistry).getUserName());
                if (requestedTenant != null ) {
                    isTenantFlowStarted = startTenantFlowForTenantDomain(requestedTenant);
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
                    endTenantFlow();
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
            int tenantId = getTenantId(tenantDomain);
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
    public void rateAPI(Identifier id, APIRating rating, String user) throws APIManagementException {
        apiMgtDAO.addRating(id, rating.getRating(), user);
    }

    @Override
    public void removeAPIRating(Identifier id, String user) throws APIManagementException {
        apiMgtDAO.removeAPIRating(id, user);
    }

    @Override
    public int getUserRating(Identifier apiId, String user) throws APIManagementException {
        return apiMgtDAO.getUserRating(apiId, user);
    }

    @Override
    public JSONObject getUserRatingInfo(Identifier id, String user) throws APIManagementException {
        JSONObject obj = apiMgtDAO.getUserRatingInfo(id, user);
        if (obj == null || obj.isEmpty()) {
            String msg = "Failed to get API ratings for API " + id.getName() + " for user " + user;
            log.error(msg);
            throw new APIMgtResourceNotFoundException(msg);
        }
        return obj;
    }

    @Override
    public JSONArray getAPIRatings(Identifier apiId) throws APIManagementException {
        return apiMgtDAO.getAPIRatings(apiId);
    }

    @Override
    public float getAverageAPIRating(Identifier apiId) throws APIManagementException {
        return apiMgtDAO.getAverageRating(apiId);
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
            if (artifactManager == null) {
                String errorMessage =
                        "Artifact manager is null when retrieving published APIs by provider ID " + providerId;
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
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
                    String status = APIUtil.getLcStateFromArtifact(artifact);

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
            int tenantId = getTenantId(providerDomain);
            final Registry registry = ServiceReferenceHolder.getInstance().
                    getRegistryService().getGovernanceSystemRegistry(tenantId);

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage =
                        "Artifact manager is null when retrieving all published APIs by provider ID " + providerId;
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            int publishedAPICount = 0;
            Map<String, API> apiCollection = new HashMap<String, API>();

            if(apiBizOwner != null && !apiBizOwner.isEmpty()){
                try {
                    final String bizOwner = apiBizOwner;
                    Map<String, List<String>> listMap = new HashMap<String, List<String>>();
                    listMap.put(APIConstants.API_OVERVIEW_BUSS_OWNER, new ArrayList<String>() {{
                        add(bizOwner);
                    }});
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
                    GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);

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
            String status = APIUtil.getLcStateFromArtifact(artifact);

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

    @Override
    public Map<String, Object> searchPaginatedAPIs(String searchQuery, String requestedTenantDomain, int start, int end,
            boolean isLazyLoad) throws APIManagementException {
        Map<String, Object> searchResults =
                super.searchPaginatedAPIs(searchQuery, requestedTenantDomain, start, end, isLazyLoad);
        if (APIUtil.isAllowDisplayMultipleVersions()) {
            return searchResults;
        }
        return filterMultipleVersionedAPIs(searchResults);
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
        cleanUpApplicationRegistrationByApplicationId(applicationId, tokenType);
    }

    /*
     * @see super.cleanUpApplicationRegistrationByApplicationId
     * */
    @Override
    public void cleanUpApplicationRegistrationByApplicationId(String applicationId, String tokenType) throws APIManagementException {
        apiMgtDAO.deleteApplicationRegistration(applicationId , tokenType);
        apiMgtDAO.deleteApplicationKeyMappingByApplicationIdAndType(applicationId, tokenType);
        apiMgtDAO.getConsumerkeyByApplicationIdAndKeyType(applicationId, tokenType);
    }

    /**
     *
     * @param jsonString this string will contain oAuth app details
     * @param userName user name of logged in user.
     * @param clientId this is the consumer key of oAuthApplication
     * @param applicationName this is the APIM appication name.
     * @param keyType
     * @param tokenType this is theApplication Token Type. This can be either default or jwt.
     * @return
     * @throws APIManagementException
     */
    @Override
    public Map<String, Object> mapExistingOAuthClient(String jsonString, String userName, String clientId,
                                                      String applicationName, String keyType, String tokenType)
                                                                        throws APIManagementException {

        String callBackURL = null;

        OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, clientId, callBackURL,
                                                                                 "default",
                                                                                  jsonString, tokenType);

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

        AccessTokenInfo tokenInfo;
        if (oAuthApplication.getJsonString().contains(APIConstants.GRANT_TYPE_CLIENT_CREDENTIALS)) {
            AccessTokenRequest tokenRequest = ApplicationUtils.createAccessTokenRequest(oAuthApplication, null);
            tokenInfo = keyManager.getNewApplicationAccessToken(tokenRequest);
        } else {
            tokenInfo = new AccessTokenInfo();
            tokenInfo.setAccessToken("");
            tokenInfo.setValidityPeriod(0L);
            String[] noScopes = new String[] {"N/A"};
            tokenInfo.setScope(noScopes);
            oAuthApplication.addParameter("tokenScope", Arrays.toString(noScopes));
        }

        Map<String, Object> keyDetails = new HashMap<String, Object>();

        if (tokenInfo != null) {
            keyDetails.put("validityTime", tokenInfo.getValidityPeriod());
            keyDetails.put("accessToken", tokenInfo.getAccessToken());
            keyDetails.put("tokenDetails", tokenInfo.getJSONString());
        }

        keyDetails.put("consumerKey", oAuthApplication.getClientId());
        keyDetails.put("consumerSecret", oAuthApplication.getParameter("client_secret"));
        keyDetails.put("appDetails", oAuthApplication.getJsonString());

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

    private Set<SubscribedAPI> getLightWeightSubscribedAPIs(Subscriber subscriber, String groupingId) throws
            APIManagementException {
        Set<SubscribedAPI> originalSubscribedAPIs;
        Set<SubscribedAPI> subscribedAPIs = new HashSet<SubscribedAPI>();
        try {
            originalSubscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber, groupingId);
            if (originalSubscribedAPIs != null && !originalSubscribedAPIs.isEmpty()) {
                Map<String, Tier> tiers = APIUtil.getTiers(tenantId);
                for (SubscribedAPI subscribedApi : originalSubscribedAPIs) {
                    Application application = subscribedApi.getApplication();
                    if (application != null) {
                        int applicationId = application.getId();
                    }
                    Tier tier = tiers.get(subscribedApi.getTier().getName());
                    subscribedApi.getTier().setDisplayName(tier != null ? tier.getDisplayName() : subscribedApi
                            .getTier().getName());
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

    public JSONArray getScopesForApplicationSubscription(String username, int applicationId)
            throws APIManagementException {
        Set<Scope> scopeSet;
        JSONArray scopeArray = new JSONArray();

        Subscriber subscriber = new Subscriber(username);
        scopeSet = apiMgtDAO.getScopesForApplicationSubscription(subscriber, applicationId);

        for (Scope scope : scopeSet) {
            JSONObject scopeObj = new JSONObject();
            scopeObj.put("scopeKey", scope.getKey());
            scopeObj.put("scopeName", scope.getName());
            scopeArray.add(scopeObj);
        }
        return scopeArray;
    }

    /*
     *@see super.getSubscribedAPIsByApplicationId
     *
     */
    @Override
    public Set<SubscribedAPI> getSubscribedAPIsByApplicationId(Subscriber subscriber, int applicationId, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getSubscribedAPIsByApplicationId(subscriber, applicationId, groupingId);
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
            handleException("Failed to get APIs of " + subscriber.getName() + " under application " + applicationId, e);
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

    @Override
    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber, int applicationId, int startSubIndex,
            int endSubIndex, String groupingId) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = null;
        try {
            subscribedAPIs = apiMgtDAO.getPaginatedSubscribedAPIs(subscriber, applicationId, startSubIndex,
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
            String msg = "Failed to get APIs of " + subscriber.getName() + " under application " + applicationId;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return subscribedAPIs;
    }

    public Integer getSubscriptionCount(Subscriber subscriber,String applicationName,String groupingId)
            throws APIManagementException {
        return apiMgtDAO.getSubscriptionCount(subscriber,applicationName,groupingId);
    }

    public Integer getSubscriptionCountByApplicationId(Subscriber subscriber, int applicationId, String groupingId)
            throws APIManagementException {
        return apiMgtDAO.getSubscriptionCountByApplicationId(subscriber, applicationId, groupingId);
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

    /**
     * This methods loads the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    public Monetization getMonetizationImplClass() throws APIManagementException {

        APIManagerConfiguration configuration = getAPIManagerConfiguration();
        Monetization monetizationImpl = null;
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            String monetizationImplClass = configuration.getFirstProperty(APIConstants.Monetization.MONETIZATION_IMPL);
            if (monetizationImplClass == null) {
                monetizationImpl = new DefaultMonetizationImpl();
            } else {
                try {
                    monetizationImpl = (Monetization) APIUtil.getClassForName(monetizationImplClass).newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    APIUtil.handleException("Failed to load monetization implementation class.", e);
                }
            }
        }
        return monetizationImpl;
    }

    @Override
    public SubscriptionResponse addSubscription(ApiTypeWrapper apiTypeWrapper, String userId, int applicationId)
            throws APIManagementException {

        API api = null;
        APIProduct product = null;
        Identifier identifier = null;
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userId);
        String tenantDomain = MultitenantUtils.getTenantDomain(tenantAwareUsername);
        final boolean isApiProduct = apiTypeWrapper.isAPIProduct();
        String state;
        String apiContext;
        
        if (isApiProduct) {
            product = apiTypeWrapper.getApiProduct();
            state = product.getState();
            identifier = product.getId();
            apiContext = product.getContext();
        } else {
            api = apiTypeWrapper.getApi();
            state = api.getStatus();
            identifier = api.getId();
            apiContext = api.getContext();
        }

        WorkflowResponse workflowResponse = null;
        int subscriptionId;
        if (APIConstants.PUBLISHED.equals(state)) {
            subscriptionId = apiMgtDAO.addSubscription(apiTypeWrapper, applicationId,
                    APIConstants.SubscriptionStatus.ON_HOLD);

            boolean isTenantFlowStarted = false;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }

            String applicationName = apiMgtDAO.getApplicationNameFromId(applicationId);

            try {
                WorkflowExecutor addSubscriptionWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);

                SubscriptionWorkflowDTO workflowDTO = new SubscriptionWorkflowDTO();
                workflowDTO.setStatus(WorkflowStatus.CREATED);
                workflowDTO.setCreatedTime(System.currentTimeMillis());
                workflowDTO.setTenantDomain(tenantDomain);
                workflowDTO.setTenantId(tenantId);
                workflowDTO.setExternalWorkflowReference(addSubscriptionWFExecutor.generateUUID());
                workflowDTO.setWorkflowReference(String.valueOf(subscriptionId));
                workflowDTO.setWorkflowType(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
                workflowDTO.setCallbackUrl(addSubscriptionWFExecutor.getCallbackURL());
                workflowDTO.setApiName(identifier.getName());
                workflowDTO.setApiContext(apiContext);
                workflowDTO.setApiVersion(identifier.getVersion());
                workflowDTO.setApiProvider(identifier.getProviderName());
                workflowDTO.setTierName(identifier.getTier());
                workflowDTO.setApplicationName(apiMgtDAO.getApplicationNameFromId(applicationId));
                workflowDTO.setApplicationId(applicationId);
                workflowDTO.setSubscriber(userId);

                Tier tier = null;
                Set<Tier> policies = Collections.emptySet();
                if (!isApiProduct) {
                    policies = api.getAvailableTiers();
                } else {
                    policies = product.getAvailableTiers();
                }

                for (Tier policy : policies) {
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
                boolean isMonetizationEnabled = false;

                if (api != null) {
                    isMonetizationEnabled = api.getMonetizationStatus();
                    //check whether monetization is enabled for API and tier plan is commercial
                    if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                        workflowResponse = addSubscriptionWFExecutor.monetizeSubscription(workflowDTO, api);
                    } else {
                        workflowResponse = addSubscriptionWFExecutor.execute(workflowDTO);
                    }
                } else {
                    isMonetizationEnabled = product.getMonetizationStatus();
                    //check whether monetization is enabled for API and tier plan is commercial
                    if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                        workflowResponse = addSubscriptionWFExecutor.monetizeSubscription(workflowDTO, product);
                    } else {
                        workflowResponse = addSubscriptionWFExecutor.execute(workflowDTO);
                    }
                }
            } catch (WorkflowException e) {
                //If the workflow execution fails, roll back transaction by removing the subscription entry.
                apiMgtDAO.removeSubscriptionById(subscriptionId);
                log.error("Could not execute Workflow", e);
                throw new APIManagementException("Could not execute Workflow", e);
            } finally {
                if (isTenantFlowStarted) {
                    endTenantFlow();
                }
            }

            if (APIUtil.isAPIGatewayKeyCacheEnabled()) {
                invalidateCachedKeys(applicationId);
            }

            //to handle on-the-fly subscription rejection (and removal of subscription entry from the database)
            //the response should have {"Status":"REJECTED"} in the json payload for this to work.
            boolean subscriptionRejected = false;
            String subscriptionStatus = null;
            String subscriptionUUID = "";

            if (workflowResponse != null && workflowResponse.getJSONPayload() != null
                    && !workflowResponse.getJSONPayload().isEmpty()) {
                try {
                    JSONObject wfResponseJson = (JSONObject) new JSONParser().parse(workflowResponse.getJSONPayload());
                    if (APIConstants.SubscriptionStatus.REJECTED.equals(wfResponseJson.get("Status"))) {
                        subscriptionRejected = true;
                        subscriptionStatus = APIConstants.SubscriptionStatus.REJECTED;
                    }
                } catch (ParseException e) {
                    log.error('\'' + workflowResponse.getJSONPayload() + "' is not a valid JSON.", e);
                }
            }

            if (!subscriptionRejected) {
                SubscribedAPI addedSubscription = getSubscriptionById(subscriptionId);
                subscriptionStatus = addedSubscription.getSubStatus();
                subscriptionUUID = addedSubscription.getUUID();

                JSONObject subsLogObject = new JSONObject();
                subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getName());
                subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
                subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, applicationId);
                subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, applicationName);
                subsLogObject.put(APIConstants.AuditLogConstants.TIER, identifier.getTier());

                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                        APIConstants.AuditLogConstants.CREATED, this.username);

                if (workflowResponse == null) {
                    workflowResponse = new GeneralWorkflowResponse();
                }
            }

            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + identifier.getName() + ", API Version " + identifier.getVersion()
                        + ", Subscription Status: " + subscriptionStatus + " subscribe by " + userId + " for app "
                        + applicationName;
                log.debug(logMessage);
            }

            return new SubscriptionResponse(subscriptionStatus, subscriptionUUID, workflowResponse);
        } else {
            throw new APIMgtResourceNotFoundException("Subscriptions not allowed on APIs/API Products in the state: " +
                    state);
        }
    }

    @Override
    public SubscriptionResponse addSubscription(ApiTypeWrapper apiTypeWrapper, String userId, int applicationId,
                                                String groupId) throws APIManagementException {

        boolean isValid = validateApplication(userId, applicationId, groupId);
        if (!isValid) {
            log.error("Application " + applicationId + " is not accessible to user " + userId);
            throw new APIManagementException("Application is not accessible to user " + userId);
        }
        return addSubscription(apiTypeWrapper, userId, applicationId);
    }

    /**
     * Check whether the application is accessible to the specified user
     * @param userId username
     * @param applicationId application ID
     * @param groupId GroupId list of the application
     * @return true if the application is accessible by the specified user
     */
    private boolean validateApplication(String userId, int applicationId, String groupId) {
        try {
            return apiMgtDAO.isAppAllowed(applicationId, userId, groupId);
        } catch (APIManagementException e) {
            log.error("Error occurred while getting user group id for user: " + userId, e);
        }
        return false;
    }

    @Override
    public String getSubscriptionStatusById(int subscriptionId) throws APIManagementException {
        return apiMgtDAO.getSubscriptionStatusById(subscriptionId);
    }

    @Override
    public void removeSubscription(Identifier identifier, String userId, int applicationId)
            throws APIManagementException {

        boolean isTenantFlowStarted = false;
        APIIdentifier apiIdentifier = null;
        APIProductIdentifier apiProdIdentifier = null;
        if (identifier instanceof APIIdentifier) {
            apiIdentifier = (APIIdentifier) identifier;
        }
        if (identifier instanceof APIProductIdentifier) {
            apiProdIdentifier = (APIProductIdentifier) identifier;
        }
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


            SubscriptionWorkflowDTO workflowDTO;
            WorkflowExecutor createSubscriptionWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor removeSubscriptionWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION);
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
            API api = null;
            APIProduct product = null;
            String context = null;
            if (apiIdentifier != null) {
                api = getAPI(apiIdentifier);
                context = api.getContext();
            } else if (apiProdIdentifier != null) {
                product = getAPIProduct(apiProdIdentifier);
                context = product.getContext();
            }
            workflowDTO.setApiContext(context);
            workflowDTO.setApiName(identifier.getName());
            workflowDTO.setApiVersion(identifier.getVersion());
            workflowDTO.setApplicationName(applicationName);
            workflowDTO.setTenantDomain(tenantDomain);
            workflowDTO.setTenantId(tenantId);
            workflowDTO.setExternalWorkflowReference(workflowExtRef);
            workflowDTO.setSubscriber(userId);
            workflowDTO.setCallbackUrl(removeSubscriptionWFExecutor.getCallbackURL());
            workflowDTO.setApplicationId(applicationId);

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

            Tier tier = null;
            if (api != null) {
                Set<Tier> policies = api.getAvailableTiers();
                Iterator<Tier> iterator = policies.iterator();
                boolean isPolicyAllowed = false;
                while (iterator.hasNext()) {
                    Tier policy = iterator.next();
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
            } else if (product != null) {
                Set<Tier> policies = product.getAvailableTiers();
                Iterator<Tier> iterator = policies.iterator();
                boolean isPolicyAllowed = false;
                while (iterator.hasNext()) {
                    Tier policy = iterator.next();
                    if (policy.getName() != null && (policy.getName()).equals(workflowDTO.getTierName())) {
                        tier = policy;
                    }
                }
            }
            if (api != null) {
                //check whether monetization is enabled for API and tier plan is commercial
                if (api.getMonetizationStatus() && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    removeSubscriptionWFExecutor.deleteMonetizedSubscription(workflowDTO, api);
                } else {
                    removeSubscriptionWFExecutor.execute(workflowDTO);
                }
            } else if (product != null) {
                //check whether monetization is enabled for API product and tier plan is commercial
                if (product.getMonetizationStatus() && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    removeSubscriptionWFExecutor.deleteMonetizedSubscription(workflowDTO, product);
                } else {
                    removeSubscriptionWFExecutor.execute(workflowDTO);
                }
            }
            JSONObject subsLogObject = new JSONObject();
            subsLogObject.put(APIConstants.AuditLogConstants.API_NAME, identifier.getName());
            subsLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_ID, applicationId);
            subsLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, applicationName);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.SUBSCRIPTION, subsLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

        } catch (WorkflowException e) {
            String errorMsg = "Could not execute Workflow, " + WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION
                    + " for resource " + identifier.toString();
            handleException(errorMsg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }

        if (APIUtil.isAPIGatewayKeyCacheEnabled()) {
            invalidateCachedKeys(applicationId);
        }
        if (log.isDebugEnabled()) {
            String logMessage = "Subscription removed from app " + applicationName + " by " + userId + " For Id: "
                    + identifier.toString();
            log.debug(logMessage);
        }
    }

    @Override
    public void removeSubscription(APIIdentifier identifier, String userId, int applicationId, String groupId) throws
            APIManagementException {
        //check application is viewable to logged user
        boolean isValid = validateApplication(userId, applicationId, groupId);
        if (!isValid) {
            log.error("Application " + applicationId + " is not accessible to user " + userId);
            throw new APIManagementException("Application is not accessible to user " + userId);
        }
        removeSubscription(identifier, userId, applicationId);
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
            Identifier identifier = subscribedAPI.getApiId() != null ? subscribedAPI.getApiId()
                    : subscribedAPI.getProductId();
            String userId = application.getSubscriber().getName();
            removeSubscription(identifier, userId, application.getId());
            if (log.isDebugEnabled()) {
                String appName = application.getName();
                String logMessage = "Identifier:  " + identifier.toString() + " subscription (uuid : " + uuid
                        + ") removed from app " + appName;
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
        apiMgtDAO.updateSubscriptions(new ApiTypeWrapper(api), applicationId);
    }

    /**
     * @deprecated
     * This method needs to be removed once the Jaggery web apps are removed.
     *
     */
    @Override
    public void addComment(APIIdentifier identifier, String commentText, String user) throws APIManagementException {
        apiMgtDAO.addComment(identifier, commentText, user);
    }

    @Override
    public String addComment(Identifier identifier, Comment comment, String user) throws APIManagementException {
        return apiMgtDAO.addComment(identifier, comment, user);
    }

    @Override
    public org.wso2.carbon.apimgt.api.model.Comment[] getComments(APIIdentifier identifier)
            throws APIManagementException {
        return apiMgtDAO.getComments(identifier);
    }

    @Override
    public Comment getComment(Identifier identifier, String commentId) throws APIManagementException {
        return apiMgtDAO.getComment(identifier, commentId);
    }

    @Override
    public org.wso2.carbon.apimgt.api.model.Comment[] getComments(ApiTypeWrapper apiTypeWrapper)
            throws APIManagementException {
        return apiMgtDAO.getComments(apiTypeWrapper);
    }

    @Override
    public void deleteComment(APIIdentifier identifier, String commentId) throws APIManagementException {
        apiMgtDAO.deleteComment(identifier, commentId);
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

        if (application.getName() != null && (application.getName().length() != application.getName().trim().length())) {
            handleApplicationNameContainSpacesException("Application name " +
                                                            "cannot contain leading or trailing white spaces");
        }

        JSONArray applicationAttributesFromConfig = getAppAttributesFromConfig(userId);
        Map<String, String> applicationAttributes = application.getApplicationAttributes();
        if (applicationAttributes == null) {
            /*
             * This empty Hashmap is set to avoid throwing a null pointer exception, in case no application attributes
             * are set when creating an application
             */
            applicationAttributes = new HashMap<String, String>();
        }
        Set<String> configAttributes = new HashSet<>();

        if (applicationAttributesFromConfig != null) {

            for (Object object : applicationAttributesFromConfig) {
                JSONObject attribute = (JSONObject) object;
                Boolean hidden = (Boolean) attribute.get(APIConstants.ApplicationAttributes.HIDDEN);
                Boolean required = (Boolean) attribute.get(APIConstants.ApplicationAttributes.REQUIRED);
                String attributeName = (String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE);
                String defaultValue = (String) attribute.get(APIConstants.ApplicationAttributes.DEFAULT);
                if (BooleanUtils.isTrue(hidden) && BooleanUtils.isTrue(required) && StringUtils.isEmpty(defaultValue)) {
                    /*
                     * In case a default value is not provided for a required hidden attribute, an exception is thrown,
                     * we don't do this validation in server startup to support multi tenancy scenarios
                     */
                    handleException("Default value not provided for hidden required attribute. Please check the " +
                            "configuration");
                }
                configAttributes.add(attributeName);
                if (BooleanUtils.isTrue(required)) {
                    if (BooleanUtils.isTrue(hidden)) {
                        /*
                         * If a required hidden attribute is attempted to be populated, we replace it with
                         * the default value.
                         */
                        String oldValue = applicationAttributes.put(attributeName, defaultValue);
                        if (StringUtils.isNotEmpty(oldValue)) {
                            log.info("Replaced provided value: " + oldValue + " with default the value" +
                                    " for the hidden application attribute: " + attributeName);
                        }
                    } else if (!applicationAttributes.keySet().contains(attributeName)) {
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            /*
                             * If a required attribute is not provided and a default value is given, we replace it with
                             * the default value.
                             */
                            applicationAttributes.put(attributeName, defaultValue);
                            log.info("Added default value: " + defaultValue +
                                    " as required attribute: " + attributeName + "is not provided");
                        } else {
                            /*
                             * If a required attribute is not provided but a default value not given, we throw a bad
                             * request exception.
                             */
                            handleException("Bad Request. Required application attribute not provided");
                        }
                    }
                } else if (BooleanUtils.isTrue(hidden)) {
                    /*
                     * If an optional hidden attribute is provided, we remove it and leave it blank, and leave it for
                     * an extension to populate it.
                     */
                    applicationAttributes.remove(attributeName);
                }
            }
            application.setApplicationAttributes(validateApplicationAttributes(applicationAttributes, configAttributes));
        } else {
            application.setApplicationAttributes(null);
        }

        String regex = "^[a-zA-Z0-9 ._-]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(application.getName());
        if (!matcher.find()) {
            handleApplicationNameContainsInvalidCharactersException("Application name contains invalid characters");
        }

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
        appLogObject.put(APIConstants.AuditLogConstants.GROUPS, application.getGroupId());
        appLogObject.put(APIConstants.AuditLogConstants.OWNER, application.getSubscriber().getName());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                APIConstants.AuditLogConstants.CREATED, this.username);

        boolean isTenantFlowStarted = false;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
        }

        try {

            WorkflowExecutor appCreationWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
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
                endTenantFlow();
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
            if (existingApp != null) {
                Set<APIKey> keys = getApplicationKeys(existingApp.getId());

                for (APIKey key : keys) {
                    existingApp.addKey(key);
                }
            }
            application.setId(existingApp.getId());
        } else {
            existingApp = apiMgtDAO.getApplicationById(application.getId());
        }

        if (existingApp != null && APIConstants.ApplicationStatus.APPLICATION_CREATED.equals(existingApp.getStatus())) {
            throw new APIManagementException("Cannot update the application while it is INACTIVE");
        }

        boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

        boolean isUserAppOwner;
        if (isCaseInsensitiveComparisons) {
            isUserAppOwner = application.getSubscriber().getName().
                    equalsIgnoreCase(existingApp.getSubscriber().getName());
        } else {
            isUserAppOwner = application.getSubscriber().getName().equals(existingApp.getSubscriber().getName());
        }

        if (!isUserAppOwner) {
            throw new APIManagementException("user: " + application.getSubscriber().getName() + ", " +
                    "attempted to update application owned by: " + existingApp.getSubscriber().getName());
        }

        if (application.getName() != null && (application.getName().length() != application.getName().trim().length())) {
            handleApplicationNameContainSpacesException("Application name " +
                    "cannot contain leading or trailing white spaces");
        }

        String regex = "^[a-zA-Z0-9 ._-]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(application.getName());
        if (!matcher.find()) {
            handleApplicationNameContainsInvalidCharactersException("Application name contains invalid characters");
        }

        Subscriber subscriber = application.getSubscriber();

        JSONArray applicationAttributesFromConfig = getAppAttributesFromConfig(subscriber.getName());
        Map<String, String> applicationAttributes = application.getApplicationAttributes();
        Map<String, String> existingApplicationAttributes = existingApp.getApplicationAttributes();
        if (applicationAttributes == null) {
            /*
             * This empty Hashmap is set to avoid throwing a null pointer exception, in case no application attributes
             * are set when updating an application
             */
            applicationAttributes = new HashMap<String, String>();
        }
        Set<String> configAttributes = new HashSet<>();

        if (applicationAttributesFromConfig != null) {

            for (Object object : applicationAttributesFromConfig) {
                boolean isExistingValue = false;
                JSONObject attribute = (JSONObject) object;
                Boolean hidden = (Boolean) attribute.get(APIConstants.ApplicationAttributes.HIDDEN);
                Boolean required = (Boolean) attribute.get(APIConstants.ApplicationAttributes.REQUIRED);
                String attributeName = (String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE);
                String defaultValue = (String) attribute.get(APIConstants.ApplicationAttributes.DEFAULT);
                if (BooleanUtils.isTrue(hidden) && BooleanUtils.isTrue(required) && StringUtils.isEmpty(defaultValue)) {
                    /*
                     * In case a default value is not provided for a required hidden attribute, an exception is thrown,
                     * we don't do this validation in server startup to support multi tenancy scenarios
                     */
                    handleException("Default value not provided for hidden required attribute. Please check the " +
                            "configuration");
                }
                configAttributes.add(attributeName);
                if (existingApplicationAttributes.containsKey(attributeName)) {
                    /*
                     * If a there is an existing attribute value, that is used as the default value.
                     */
                    isExistingValue = true;
                    defaultValue = existingApplicationAttributes.get(attributeName);
                }
                if (BooleanUtils.isTrue(required)) {
                    if (BooleanUtils.isTrue(hidden)) {
                        String oldValue = applicationAttributes.put(attributeName, defaultValue);
                        if (StringUtils.isNotEmpty(oldValue)) {
                            log.info("Replaced provided value: " + oldValue + " with the default/existing value for" +
                                    " the hidden application attribute: " + attributeName);
                        }
                    } else if (!applicationAttributes.keySet().contains(attributeName)) {
                        if (StringUtils.isNotEmpty(defaultValue)) {
                            applicationAttributes.put(attributeName, defaultValue);
                        } else {
                            handleException("Bad Request. Required application attribute not provided");
                        }
                    }
                } else if (BooleanUtils.isTrue(hidden)) {
                    if (isExistingValue) {
                        applicationAttributes.put(attributeName, defaultValue);
                    } else {
                        applicationAttributes.remove(attributeName);
                    }
                }
            }
            application.setApplicationAttributes(validateApplicationAttributes(applicationAttributes, configAttributes));
        } else {
            application.setApplicationAttributes(null);
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
        appLogObject.put(APIConstants.AuditLogConstants.GROUPS, application.getGroupId());
        appLogObject.put(APIConstants.AuditLogConstants.OWNER, application.getSubscriber().getName());

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                APIConstants.AuditLogConstants.UPDATED, this.username);

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
     * @param username
     * @throws APIManagementException
     */
    @Override
    public void removeApplication(Application application, String username) throws APIManagementException {
        String uuid = application.getUUID();
        if (application.getId() == 0 && !StringUtils.isEmpty(uuid)) {
            application = apiMgtDAO.getApplicationByUUID(uuid);
            if (application != null) {
                Set<APIKey> keys = getApplicationKeys(application.getId());

                for (APIKey key : keys) {
                    application.addKey(key);
                }
            }
        }
        boolean isTenantFlowStarted = false;
        int applicationId = application.getId();

        boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

        boolean isUserAppOwner;
        if (isCaseInsensitiveComparisons) {
            isUserAppOwner = application.getSubscriber().getName().equalsIgnoreCase(username);
        } else {
            isUserAppOwner = application.getSubscriber().getName().equals(username);
        }

        if (!isUserAppOwner) {
            throw new APIManagementException("user: " + username + ", " +
                    "attempted to remove application owned by: " + application.getSubscriber().getName());
        }

        try {
            String workflowExtRef;
            ApplicationWorkflowDTO workflowDTO;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            WorkflowExecutor createApplicationWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
            WorkflowExecutor createSubscriptionWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            WorkflowExecutor createProductionRegistrationWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            WorkflowExecutor createSandboxRegistrationWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
            WorkflowExecutor removeApplicationWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);

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
            appLogObject.put(APIConstants.AuditLogConstants.GROUPS, application.getGroupId());
            appLogObject.put(APIConstants.AuditLogConstants.OWNER, application.getSubscriber().getName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

        } catch (WorkflowException e) {
            String errorMsg = "Could not execute Workflow, " + WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION + " " +
                    "for applicationID " + application.getId();
            handleException(errorMsg, e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
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
            Set<Scope> authorizedScopeSet = new HashSet<Scope>(authorizedScopes);
            StringBuilder scopeBuilder = new StringBuilder();
            for (Scope scope : authorizedScopeSet) {
                scopeBuilder.append(scope.getKey()).append(' ');
            }
            authScopeString = scopeBuilder.toString();
        } else {
            authScopeString = APIConstants.OAUTH2_DEFAULT_SCOPE;
        }

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }
            // initiate WorkflowExecutor
            WorkflowExecutor appRegistrationWorkflow = null;
            // initiate ApplicationRegistrationWorkflowDTO
            ApplicationRegistrationWorkflowDTO appRegWFDto = null;

            ApplicationKeysDTO appKeysDto = new ApplicationKeysDTO();

            // get APIM application by Application Name and userId.
            Application application = ApplicationUtils.retrieveApplication(applicationName, userId, groupingId);

            boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

            boolean isUserAppOwner;
            if (isCaseInsensitiveComparisons) {
                isUserAppOwner = application.getSubscriber().getName().equalsIgnoreCase(userId);
            } else {
                isUserAppOwner = application.getSubscriber().getName().equals(userId);
            }

            if (!isUserAppOwner) {
                throw new APIManagementException("user: " + application.getSubscriber().getName() + ", " +
                        "attempted to generate tokens for application owned by: " + userId);
            }

            // if its a PRODUCTION application.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                // initiate workflow type. By default simple work flow will be
                // executed.
                appRegistrationWorkflow =
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
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
                        getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
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
            String applicationTokenType = application.getTokenType();
            if (StringUtils.isEmpty(application.getTokenType())) {
                applicationTokenType = APIConstants.DEFAULT_TOKEN_TYPE;
            }
            // Build key manager instance and create oAuthAppRequest by jsonString.
            OAuthAppRequest request =
                    ApplicationUtils.createOauthAppRequest(applicationName, null,
                            callbackUrl, authScopeString, jsonString, applicationTokenType);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.VALIDITY_PERIOD, validityTime);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_CALLBACK_URL, callbackUrl);

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

            JSONObject appLogObject = new JSONObject();
            appLogObject.put("Generated keys for application", application.getName());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);

            return keyDetails;
        } catch (WorkflowException e) {
            log.error("Could not execute Workflow", e);
            throw new APIManagementException(e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    @Override
    public Map<String, Object> requestApprovalForApplicationRegistrationByApplicationId(
            Map<String, Object> appInfo) throws APIManagementException {
        if (appInfo == null || appInfo.isEmpty()) {
            log.error("Application information is not provided to request approval For Application Registration");
            return new HashMap<String, Object>(0);
        }
        boolean isTenantFlowStarted = false;
        String username = appInfo.get("username").toString();
        String scopes = appInfo.get("scopes").toString();
        String applicationName = appInfo.get("applicationName").toString();
        String groupingId = appInfo.get("groupingId").toString();
        String tokenType = appInfo.get("tokenType").toString();
        String callbackUrl = appInfo.get("callbackUrl").toString();
        String jsonParams = appInfo.get("jsonParams").toString();
        String[] allowedDomains = (String[]) appInfo.get("allowedDomains");
        String validityTime = appInfo.get("validityPeriod").toString();
        int applicationId = Integer.valueOf(appInfo.get("applicationId").toString());
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Unable to retrieve the tenant information of the current user.";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        //checking for authorized scopes
        Set<Scope> scopeSet = new LinkedHashSet<Scope>();
        List<Scope> authorizedScopes = new ArrayList<Scope>();
        String authScopeString;
        if (scopes != null && scopes.length() != 0 && !APIConstants.OAUTH2_DEFAULT_SCOPE.equals(scopes)) {
            scopeSet.addAll(getScopesByScopeKeys(scopes, tenantId));
            authorizedScopes = getAllowedScopesForUserApplication(username, scopeSet);
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
                isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }
            // initiate WorkflowExecutor
            WorkflowExecutor appRegistrationWorkflow = null;
            // initiate ApplicationRegistrationWorkflowDTO
            ApplicationRegistrationWorkflowDTO appRegWFDto = null;
            ApplicationKeysDTO appKeysDto = new ApplicationKeysDTO();
            // get APIM application by Application Id.
            Application application = ApplicationUtils.retrieveApplicationById(applicationId);
            // if its a PRODUCTION application.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                // initiate workflow type. By default simple work flow will be
                // executed.
                appRegistrationWorkflow = getWorkflowExecutor(
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
                appRegWFDto = (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                        .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            }// if it is a sandBox application.
            else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) {
                appRegistrationWorkflow = getWorkflowExecutor(
                        WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
                appRegWFDto = (ApplicationRegistrationWorkflowDTO) WorkflowExecutorFactory.getInstance()
                        .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
            } else {
                throw new APIManagementException("Invalid Token Type '" + tokenType + "' requested.");
            }
            //check whether callback url is empty and set null
            if (StringUtils.isBlank(callbackUrl)) {
                callbackUrl = null;
            }

            String applicationTokenType = application.getTokenType();
            if (StringUtils.isEmpty(application.getTokenType())) {
                applicationTokenType = APIConstants.DEFAULT_TOKEN_TYPE;
            }

            // Build key manager instance and create oAuthAppRequest by jsonString.
            OAuthAppRequest request = ApplicationUtils
                    .createOauthAppRequest(applicationName, null, callbackUrl, authScopeString, jsonParams,
                            applicationTokenType);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.VALIDITY_PERIOD, validityTime);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);
            request.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_CALLBACK_URL, callbackUrl);
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
            if (!application.getSubscriber().getName().equals(username)) {
                appRegWFDto.setUserName(application.getSubscriber().getName());
            } else {
                appRegWFDto.setUserName(username);
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
            JSONObject appLogObject = new JSONObject();
            appLogObject.put("Generated keys for application", application.getName());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);
            return keyDetails;
        } catch (WorkflowException e) {
            log.error("Could not execute Workflow", e);
            throw new APIManagementException("Could not execute Workflow", e);
        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    private static List<Scope> getAllowedScopesForUserApplication(String username,
                                                                  Set<Scope> reqScopeSet) {
        String[] userRoles = null;
        org.wso2.carbon.user.api.UserStoreManager userStoreManager = null;
        String preservedCaseSensitiveValue = System.getProperty(PRESERVED_CASE_SENSITIVE_VARIABLE);
        boolean preservedCaseSensitive = JavaUtils.isTrueExplicitly(preservedCaseSensitiveValue);

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
            if (preservedCaseSensitive) {
                userRoleList = Arrays.asList(userRoles);
            } else {
                userRoleList = new ArrayList<String>();
                for (String userRole : userRoles) {
                    userRoleList.add(userRole.toLowerCase());
                }
            }
        } else {
            userRoleList = Collections.emptyList();
        }

        //Iterate the requested scopes list.
        for (Scope scope : reqScopeSet) {
            //Get the set of roles associated with the requested scope.
            String roles = scope.getRoles();

            //If the scope has been defined in the context of the App and if roles have been defined for the scope
            if (roles != null && roles.length() != 0) {
                List<String> roleList = new ArrayList<String>();
                for (String scopeRole : roles.split(",")) {
                    if (preservedCaseSensitive) {
                        roleList.add(scopeRole.trim());
                    } else {
                        roleList.add(scopeRole.trim().toLowerCase());
                    }
                }
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
                        keyDetails.put("appDetails", oauthApp.getJsonString());
                    } catch (APIManagementException e) {
                        APIUtil.handleException("Error occurred while Creating Keys.", e);
                    }
                }

            }
        }
        return keyDetails;
    }

    @Override
    public Map<String, String> completeApplicationRegistration(String userId, int applicationId,
            String tokenType, String tokenScope, String groupingId) throws APIManagementException {
        Application application = apiMgtDAO.getApplicationById(applicationId);
        String status = apiMgtDAO.getRegistrationApprovalState(application.getId(), tokenType);
        Map<String, String> keyDetails = null;
        if (!application.getSubscriber().getName().equals(userId)) {
            userId = application.getSubscriber().getName();
        }
        //todo get workflow reference by appId
        String workflowReference = apiMgtDAO.getWorkflowReferenceByApplicationId(application.getId(), userId);
        if (workflowReference != null) {
            WorkflowDTO workflowDTO = null;
            // Creating workflowDTO for the correct key type.
            if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(tokenType)) {
                workflowDTO = WorkflowExecutorFactory.getInstance()
                        .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION);
            } else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(tokenType)) {
                workflowDTO = WorkflowExecutorFactory.getInstance()
                        .createWorkflowDTO(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX);
            }
            if (workflowDTO != null) {
                // Set the workflow reference in the workflow dto and the populate method will fill in other details
                // using the persisted request.
                ApplicationRegistrationWorkflowDTO registrationWorkflowDTO = (ApplicationRegistrationWorkflowDTO) workflowDTO;
                registrationWorkflowDTO.setExternalWorkflowReference(workflowReference);
                if (APIConstants.AppRegistrationStatus.REGISTRATION_APPROVED.equals(status)) {
                    apiMgtDAO.populateAppRegistrationWorkflowDTO(registrationWorkflowDTO);
                    try {
                        AbstractApplicationRegistrationWorkflowExecutor
                                .dogenerateKeysForApplication(registrationWorkflowDTO);
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

        Application application = apiMgtDAO.getApplicationByName(ApplicationName, userId,groupingId);
        if (application != null) {
            checkAppAttributes(application, userId);
        }
        application = apiMgtDAO.getApplicationWithOAuthApps(ApplicationName, userId, groupingId);

        if (application != null) {
            Set<APIKey> keys = getApplicationKeys(application.getId());

            for (APIKey key : keys) {
                application.addKey(key);
            }
        }

        return application;
    }

    /**
     * Returns the corresponding application given the Id
     * @param id Id of the Application
     * @return it will return Application corresponds to the id.
     * @throws APIManagementException
     */
    @Override
    public Application getApplicationById(int id) throws APIManagementException {

        Application application = apiMgtDAO.getApplicationById(id);
        if (application != null) {
            Set<APIKey> keys = getApplicationKeys(application.getId());
            for (APIKey key : keys) {
                application.addKey(key);
            }
        }
        return application;
    }

    /*
    * @see super.getApplicationById(int id, String userId, String groupId)
    * */
    @Override
    public Application getApplicationById(int id, String userId, String groupId) throws APIManagementException {
        Application application = apiMgtDAO.getApplicationById(id, userId, groupId);
        if (application != null) {
            checkAppAttributes(application, userId);
            Set<APIKey> keys = getApplicationKeys(application.getId());
            for (APIKey key : keys) {
                application.addKey(key);
            }
        }
        return application;
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
    public String getGraphqlSchema(APIIdentifier apiId) throws APIManagementException {
        return getGraphqlSchemaDefinition(apiId);
    }

    @Override
    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber, Identifier identifier, String groupingId)
            throws APIManagementException {
        Set<SubscribedAPI> subscribedAPISet = new HashSet<>();
        Set<SubscribedAPI> subscribedAPIs = getSubscribedAPIs(subscriber, groupingId);
        for (SubscribedAPI api : subscribedAPIs) {
            if (identifier instanceof APIIdentifier && identifier.equals(api.getApiId())) {
                Set<APIKey> keys = getApplicationKeys(api.getApplication().getId());
                for (APIKey key : keys) {
                    api.addKey(key);
                }
                subscribedAPISet.add(api);
            } else if (identifier instanceof APIProductIdentifier && identifier.equals(api.getProductId())) {
                Set<APIKey> keys = getApplicationKeys(api.getApplication().getId());
                for (APIKey key : keys) {
                    api.addKey(key);
                }
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
        // '0' is passed as argument whenever tenant id of logged in user is needed
        return getDeniedTiers(0);
    }

    /**
     * Returns a list of tiers denied
     * @param apiProviderTenantId tenant id of API provider
     * @return Set<Tier>
     */
    @Override
    public Set<String> getDeniedTiers(int apiProviderTenantId) throws APIManagementException {
        Set<String> deniedTiers = new HashSet<String>();
        String[] currentUserRoles;
        if (apiProviderTenantId == 0) {
            apiProviderTenantId = tenantId;
        }
        try {
            if (apiProviderTenantId != 0) {
                /* Get the roles of the Current User */
                currentUserRoles = ((UserRegistry) ((UserAwareAPIConsumer) this).registry).
                        getUserRealm().getUserStoreManager().getRoleListOfUser(((UserRegistry) this.registry)
                        .getUserName());

                Set<TierPermissionDTO> tierPermissions;

                if (APIUtil.isAdvanceThrottlingEnabled()) {
                    tierPermissions = apiMgtDAO.getThrottleTierPermissions(apiProviderTenantId);
                } else {
                    tierPermissions = apiMgtDAO.getTierPermissions(apiProviderTenantId);
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

    @Override
    public Set<TierPermission> getTierPermissions() throws APIManagementException {

        Set<TierPermission> tierPermissions = new HashSet<TierPermission>();
        if (tenantId != 0) {
            Set<TierPermissionDTO> tierPermissionDtos;
            if (APIUtil.isAdvanceThrottlingEnabled()) {
                tierPermissionDtos = apiMgtDAO.getThrottleTierPermissions(tenantId);
            } else {
                tierPermissionDtos = apiMgtDAO.getTierPermissions(tenantId);
            }
            for (TierPermissionDTO tierDto : tierPermissionDtos) {
                TierPermission tierPermission = new TierPermission(tierDto.getTierName());
                tierPermission.setRoles(tierDto.getRoles());
                tierPermission.setPermissionType(tierDto.getPermissionType());
                tierPermissions.add(tierPermission);
            }
        }
        return tierPermissions;
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
		return null;
	}

	public Set<Scope> getScopesByScopeKeys(String scopeKeys, int tenantId)
			throws APIManagementException {
		return apiMgtDAO.getScopesByScopeKeys(scopeKeys, tenantId);
    }

    @Override
    public String getGroupId(int appId) throws APIManagementException {
        return apiMgtDAO.getGroupId(appId);
    }

    @Override
    public String[] getGroupIds(String response) throws APIManagementException {
        String groupingExtractorClass = APIUtil.getGroupingExtractorImplementation();
        return APIUtil.getGroupIdsFromExtractor(response, groupingExtractorClass);
    }

    /**
     * Returns all applications associated with given subscriber, groupingId and search criteria.
     *
     * @param subscriber Subscriber
     * @param groupingId The groupId to which the applications must belong.
     * @param offset     The offset.
     * @param search     The search string.
     * @param sortColumn The sort column.
     * @param sortOrder  The sort order.
     * @return Application[] The Applications.
     * @throws APIManagementException
     */
    @Override
    public Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId, int start , int offset
            , String search, String sortColumn, String sortOrder)
            throws APIManagementException {
        return apiMgtDAO.getApplicationsWithPagination(subscriber, groupingId, start, offset,
                search, sortColumn, sortOrder);
    }

    /**
     * Returns all applications associated with given subscriber and groupingId.
     *
     * @param subscriber The subscriber.
     * @param groupingId The groupId to which the applications must belong.
     * @return Application[] Array of applications.
     * @throws APIManagementException
     */
    @Override
    public Application[] getApplications(Subscriber subscriber, String groupingId)
			throws APIManagementException {
        Application[] applications = apiMgtDAO.getApplications(subscriber, groupingId);
        for (Application application : applications) {
            Set<APIKey> keys = getApplicationKeys(application.getId());

            for (APIKey key : keys) {
                application.addKey(key);
            }
        }
        return applications;
    }

    /**
     * Returns all API keys associated with given application id.
     *
     * @param applicationId The id of the application.
     * @return Set<APIKey>  Set of API keys of the application.
     * @throws APIManagementException
     */
    protected Set<APIKey> getApplicationKeys(int applicationId) throws APIManagementException {
        Set<APIKey> apiKeys = new HashSet<APIKey>();
        APIKey productionKey = getApplicationKey(applicationId, APIConstants.API_KEY_TYPE_PRODUCTION);
        if (productionKey != null) {
            apiKeys.add(productionKey);
        } else {
            productionKey = apiMgtDAO.getKeyStatusOfApplication(APIConstants.API_KEY_TYPE_PRODUCTION, applicationId);
            if (productionKey != null) {
                productionKey.setType(APIConstants.API_KEY_TYPE_PRODUCTION);
                apiKeys.add(productionKey);
            }
        }

        APIKey sandboxKey = getApplicationKey(applicationId, APIConstants.API_KEY_TYPE_SANDBOX);
        if (sandboxKey != null) {
            apiKeys.add(sandboxKey);
        } else {
            sandboxKey = apiMgtDAO.getKeyStatusOfApplication(APIConstants.API_KEY_TYPE_SANDBOX, applicationId);
            if (sandboxKey != null) {
                sandboxKey.setType(APIConstants.API_KEY_TYPE_SANDBOX);
                apiKeys.add(sandboxKey);
            }
        }
        return apiKeys;
    }

    /**
     * Returns the key associated with given application id and key type.
     *
     * @param applicationId Id of the Application.
     * @param keyType The type of key.
     * @return APIKey The key of the application.
     * @throws APIManagementException
     */
    protected APIKey getApplicationKey(int applicationId, String keyType) throws APIManagementException {
        String consumerKey = apiMgtDAO.getConsumerkeyByApplicationIdAndKeyType(String.valueOf(applicationId), keyType);
        if (StringUtils.isNotEmpty(consumerKey)) {
            String consumerKeyStatus = apiMgtDAO.getKeyStatusOfApplication(keyType, applicationId).getState();
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            OAuthApplicationInfo oAuthApplicationInfo = keyManager.retrieveApplication(consumerKey);
            AccessTokenInfo tokenInfo = keyManager.getAccessTokenByConsumerKey(consumerKey);
            APIKey apiKey = new APIKey();
            apiKey.setConsumerKey(consumerKey);
            apiKey.setType(keyType);
            apiKey.setState(consumerKeyStatus);
            if (oAuthApplicationInfo != null) {
                apiKey.setConsumerSecret(oAuthApplicationInfo.getClientSecret());
                apiKey.setCallbackUrl(oAuthApplicationInfo.getCallBackURL());
                if (oAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES) != null) {
                    apiKey.setGrantTypes(oAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES).toString());
                }
            }
            if (tokenInfo != null) {
                apiKey.setAccessToken(tokenInfo.getAccessToken());
                apiKey.setValidityPeriod(tokenInfo.getValidityPeriod());
                apiKey.setTokenScope(getScopeString(tokenInfo.getScopes()));
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Access token does not exist for Consumer Key: " + consumerKey);
                }
            }
            return apiKey;
        }
        if (log.isDebugEnabled()) {
            log.debug("Consumer key does not exist for Application Id: " + applicationId + " Key Type: " + keyType);
        }
        return null;
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

    @Override
    public Application[] getLightWeightApplications(Subscriber subscriber, String groupingId) throws
            APIManagementException {
        return apiMgtDAO.getLightWeightApplications(subscriber, groupingId);
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
        boolean tenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                tenantFlowStarted = true;
            }

            Application application = ApplicationUtils.retrieveApplication(applicationName, userId, groupingId);

            final String subscriberName = application.getSubscriber().getName();

            boolean isCaseInsensitiveComparisons = Boolean.parseBoolean(getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS));

            boolean isUserAppOwner;
            if (isCaseInsensitiveComparisons) {
                isUserAppOwner = subscriberName.equalsIgnoreCase(userId);
            } else {
                isUserAppOwner = subscriberName.equals(userId);
            }

            if (!isUserAppOwner) {
                throw new APIManagementException("user: " + userId + ", attempted to update OAuth application " +
                        "owned by: " + subscriberName);
            }

            //Create OauthAppRequest object by passing json String.
            OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, null, callbackUrl,
                    tokenScope, jsonString, application.getTokenType());

            oauthAppRequest.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);

            String consumerKey = apiMgtDAO.getConsumerKeyForApplicationKeyType(applicationName, userId, tokenType,
                    groupingId);

            oauthAppRequest.getOAuthApplicationInfo().setClientId(consumerKey);
            //get key manager instance.
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            //call update method.

            OAuthApplicationInfo updatedAppInfo = keyManager.updateApplication(oauthAppRequest);

            JSONObject appLogObject = new JSONObject();
            appLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, updatedAppInfo.getClientName());
            appLogObject.put("Updated Oauth app with Call back URL", callbackUrl);
            appLogObject.put("Updated Oauth app with grant types", jsonString);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);
            return updatedAppInfo;
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }

    }

    /**
     * @param userId Subscriber name.
     * @param applicationName of the Application.
     * @param applicationId of the Application.
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
    public OAuthApplicationInfo updateAuthClientByAppId(String userId, String applicationName, int applicationId,
            String tokenType, String callbackUrl, String[] allowedDomains, String validityTime, String tokenScope,
            String groupingId, String jsonString) throws APIManagementException {
        boolean tenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                tenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
            }

            Application application = ApplicationUtils.retrieveApplicationById(applicationId);
            //Create OauthAppRequest object by passing json String.
            OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(applicationName, null, callbackUrl,
                    tokenScope, jsonString, application.getTokenType());
            oauthAppRequest.getOAuthApplicationInfo().addParameter(ApplicationConstants.APP_KEY_TYPE, tokenType);
            String consumerKey = apiMgtDAO.getConsumerKeyForApplicationKeyType(applicationId, userId, tokenType,
                    groupingId);
            oauthAppRequest.getOAuthApplicationInfo().setClientId(consumerKey);
            //get key manager instance.
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            //call update method.
            OAuthApplicationInfo updatedAppInfo = keyManager.updateApplication(oauthAppRequest);
            JSONObject appLogObject = new JSONObject();
            appLogObject.put(APIConstants.AuditLogConstants.APPLICATION_NAME, updatedAppInfo.getClientName());
            appLogObject.put("Updated Oauth app with Call back URL", callbackUrl);
            appLogObject.put("Updated Oauth app with grant types", jsonString);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.APPLICATION, appLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);
            return updatedAppInfo;
        } finally {
            if (tenantFlowStarted) {
                endTenantFlow();
            }
        }
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

    @Override
    public Application[] getApplicationsByOwner(String userId) throws APIManagementException {
        return apiMgtDAO.getApplicationsByOwner(userId);
    }

    public boolean isSubscriberValid(String userId)
            throws APIManagementException {
        boolean isSubscribeValid = false;
        if (apiMgtDAO.getSubscriber(userId) != null) {
            isSubscribeValid = true;
        } else {
            return false;
        }
        return isSubscribeValid;
    }

    public boolean updateApplicationOwner(String userId, Application application) throws APIManagementException {
        boolean isAppUpdated;
        String consumerKey;
        String oldUserName = application.getSubscriber().getName();
        String oldTenantDomain = MultitenantUtils.getTenantDomain(oldUserName);
        String newTenantDomain = MultitenantUtils.getTenantDomain(userId);
        if (oldTenantDomain.equals(newTenantDomain)) {
            if (isSubscriberValid(userId)) {
                String applicationName = application.getName();
                if (!APIUtil.isApplicationOwnedBySubscriber(userId, applicationName)) {
                    for (int i = 0; i < application.getKeys().size(); i++) {
                        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
                             /* retrieving OAuth application information for specific consumer key */
                        consumerKey = ((APIKey) ((ArrayList) application.getKeys()).get(i)).getConsumerKey();
                        OAuthApplicationInfo oAuthApplicationInfo = keyManager.retrieveApplication(consumerKey);
                        if (oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_NAME) != null) {
                            OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(oAuthApplicationInfo.
                                            getParameter(ApplicationConstants.OAUTH_CLIENT_NAME).toString(), null,
                                    oAuthApplicationInfo.getCallBackURL(), null,
                                    null, application.getTokenType());
                            oauthAppRequest.getOAuthApplicationInfo().setAppOwner(userId);
                            oauthAppRequest.getOAuthApplicationInfo().setClientId(consumerKey);
                             /* updating the owner of the OAuth application with userId */
                            OAuthApplicationInfo updatedAppInfo = keyManager.updateApplicationOwner(oauthAppRequest,
                                    oldUserName);
                            isAppUpdated = true;
                            audit.info("Successfully updated the owner of application " + application.getName() +
                                    " from " + oldUserName + " to " + userId + ".");
                        } else {
                            throw new APIManagementException("Unable to retrieve OAuth application information.");
                        }
                    }
                } else {
                    throw new APIManagementException("Unable to update application owner to " + userId +
                            " as this user has an application with the same name. Update owner to another user.");
                }
            } else {
                throw new APIManagementException(userId + " is not a subscriber");
            }
        } else {
            throw new APIManagementException("Unable to update application owner to " +
                    userId + " as this user does not belong to " + oldTenantDomain + " domain.");
        }

            isAppUpdated = apiMgtDAO.updateApplicationOwner(userId, application);
            return isAppUpdated;
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
                    isTenantFlowStarted = startTenantFlowForTenantDomain(tenantDomain);
                }

                workflowDTO.setWorkflowDescription(description);
                workflowDTO.setStatus(WorkflowStatus.valueOf(status));

                String workflowType = workflowDTO.getWorkflowType();
                WorkflowExecutor workflowExecutor;
                try {
                    workflowExecutor = getWorkflowExecutor(workflowType);
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
                    endTenantFlow();
                }
            }
        }
        return row;
    }

    protected void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    protected boolean startTenantFlowForTenantDomain(String tenantDomain) {
        boolean isTenantFlowStarted = true;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        return isTenantFlowStarted;
    }

    /**
     * Returns a workflow executor
     *
     * @param workflowType Workflow executor type
     * @return WorkflowExecutor of given type
     * @throws WorkflowException if an error occurred while getting WorkflowExecutor
     */
    protected WorkflowExecutor getWorkflowExecutor(String workflowType) throws WorkflowException {
        return WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
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

    /**
     * To get the query to retrieve user role list query based on current role list.
     *
     * @return the query with user role list.
     * @throws APIManagementException API Management Exception.
     */
    private String getUserRoleListQuery() throws APIManagementException {
        StringBuilder rolesQuery = new StringBuilder();
        rolesQuery.append('(');
        rolesQuery.append(APIConstants.NULL_USER_ROLE_LIST);
        String[] userRoles = APIUtil.getListOfRoles((userNameWithoutChange != null)? userNameWithoutChange: username);
        String skipRolesByRegex = APIUtil.getSkipRolesByRegex();
        if (StringUtils.isNotEmpty(skipRolesByRegex)) {
            List<String> filteredUserRoles = new ArrayList<>(Arrays.asList(userRoles));
            String[] regexList = skipRolesByRegex.split(",");
            for (int i = 0; i < regexList.length; i++) {
                Pattern p = Pattern.compile(regexList[i]);
                Iterator<String> itr = filteredUserRoles.iterator();
                while(itr.hasNext()) {
                    String role = itr.next();
                    Matcher m = p.matcher(role);
                    if (m.matches()) {
                        itr.remove();
                    }
                }
            }
            userRoles = filteredUserRoles.toArray(new String[0]);
        }
        if (userRoles != null) {
            for (String userRole : userRoles) {
                rolesQuery.append(" OR ");
                rolesQuery.append(ClientUtils.escapeQueryChars(APIUtil.sanitizeUserRole(userRole.toLowerCase())));
            }
        }
        rolesQuery.append(")");
        if(log.isDebugEnabled()) {
        	log.debug("User role list solr query " + APIConstants.STORE_VIEW_ROLES + "=" + rolesQuery.toString());
        }
        return  APIConstants.STORE_VIEW_ROLES + "=" + rolesQuery.toString();
    }

    /**
     * To get the current user's role list.
     *
     * @return user role list.
     * @throws APIManagementException API Management Exception.
     */
    private List<String> getUserRoleList() throws APIManagementException {
        List<String> userRoleList;
        if (userNameWithoutChange == null) {
            userRoleList = new ArrayList<String>() {{
                add(APIConstants.NULL_USER_ROLE_LIST);
            }};
        } else {
            userRoleList = new ArrayList<String>(Arrays.asList(APIUtil.getListOfRoles(userNameWithoutChange)));
        }
        return userRoleList;
    }

    @Override
    protected String getSearchQuery(String searchQuery) throws APIManagementException {
        if (!isAccessControlRestrictionEnabled || ( userNameWithoutChange != null &&
                APIUtil.hasPermission(userNameWithoutChange, APIConstants.Permissions
                .APIM_ADMIN))) {
            return searchQuery;
        }
        String criteria = getUserRoleListQuery();
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            criteria = criteria + "&" + searchQuery;
        }
        return criteria;
    }

    @Deprecated // Remove this method once the jaggery store app is removed.
    @Override
    public String getWSDLDocument(String username, String tenantDomain, String resourceUrl,
            Map environmentDetails, Map apiDetails) throws APIManagementException {

        if (username == null) {
            username = APIConstants.END_USER_ANONYMOUS;
        }
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        Map<String, Object> docResourceMap = APIUtil.getDocument(username, resourceUrl, tenantDomain);
        String wsdlContent = "";
        if (log.isDebugEnabled()) {
            log.debug("WSDL document resource availability: " + docResourceMap.isEmpty());
        }
        if (!docResourceMap.isEmpty()) {
            try {
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                IOUtils.copy((InputStream) docResourceMap.get("Data"), arrayOutputStream);
                String apiName = (String) apiDetails.get(API_NAME);
                String apiVersion = (String) apiDetails.get(API_VERSION);
                String apiProvider = (String) apiDetails.get(API_PROVIDER);
                String environmentName = (String) environmentDetails.get(ENVIRONMENT_NAME);
                String environmentType = (String) environmentDetails.get(ENVIRONMENT_TYPE);
                if (log.isDebugEnabled()) {
                    log.debug("Published SOAP api gateway environment name: " + environmentName + " environment type: "
                            + environmentType);
                }
                if (resourceUrl.endsWith(APIConstants.ZIP_FILE_EXTENSION)) {
                    WSDLArchiveInfo archiveInfo = APIMWSDLReader
                            .extractAndValidateWSDLArchive((InputStream) docResourceMap.get("Data"))
                            .getWsdlArchiveInfo();
                    File folderToImport = new File(
                            archiveInfo.getLocation() + File.separator + APIConstants.API_WSDL_EXTRACTED_DIRECTORY);
                    Collection<File> wsdlFiles = APIFileUtil
                            .searchFilesWithMatchingExtension(folderToImport, APIFileUtil.WSDL_FILE_EXTENSION);
                    Collection<File> xsdFiles = APIFileUtil
                            .searchFilesWithMatchingExtension(folderToImport, APIFileUtil.XSD_FILE_EXTENSION);
                    if (wsdlFiles != null) {
                        for (File foundWSDLFile : wsdlFiles) {
                            Path fileLocation = Paths.get(foundWSDLFile.getAbsolutePath());
                            byte[] updatedWSDLContent = this
                                    .getUpdatedWSDLByEnvironment(resourceUrl, Files.readAllBytes(fileLocation),
                                            environmentName, environmentType, apiName, apiVersion, apiProvider);
                            File updatedWSDLFile = new File(foundWSDLFile.getPath());
                            wsdlFiles.remove(foundWSDLFile);
                            FileUtils.writeByteArrayToFile(updatedWSDLFile, updatedWSDLContent);
                            wsdlFiles.add(updatedWSDLFile);
                        }
                        wsdlFiles.addAll(xsdFiles);
                        ZIPUtils.zipFiles(folderToImport.getCanonicalPath() + APIConstants.UPDATED_WSDL_ZIP,
                                wsdlFiles);
                        wsdlContent = folderToImport.getCanonicalPath() + APIConstants.UPDATED_WSDL_ZIP;
                    }
                } else {
                    arrayOutputStream = new ByteArrayOutputStream();
                    IOUtils.copy((InputStream) docResourceMap.get("Data"), arrayOutputStream);
                    byte[] updatedWSDLContent = this
                            .getUpdatedWSDLByEnvironment(resourceUrl, arrayOutputStream.toByteArray(), environmentName,
                                    environmentType, apiName, apiVersion, apiProvider);
                    wsdlContent = new String(updatedWSDLContent);
                }
            } catch (IOException e) {
                handleException("Error occurred while copying wsdl content into byte array stream for resource: "
                        + resourceUrl, e);
            }
        } else {
            handleException("No wsdl resource found for resource path: " + resourceUrl);
        }
        JSONObject data = new JSONObject();
        data.put(APIConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE,
                docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE));
        data.put(APIConstants.DOCUMENTATION_RESOURCE_MAP_NAME,
                docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_NAME));
        data.put(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA, wsdlContent);
        if (log.isDebugEnabled()) {
            log.debug("Updated wsdl content details for wsdl resource: " + docResourceMap.get("name") + " is " +
                    data.toJSONString());
        }
        return data.toJSONString();
    }

    @Override
    public ResourceFile getWSDL(APIIdentifier apiIdentifier, String environmentName, String environmentType)
            throws APIManagementException {
        WSDLValidationResponse validationResponse;
        ResourceFile resourceFile = getWSDL(apiIdentifier);
        if (resourceFile.getContentType().contains(APIConstants.APPLICATION_ZIP)) {
            validationResponse = APIMWSDLReader.extractAndValidateWSDLArchive(resourceFile.getContent());
        } else {
            validationResponse = APIMWSDLReader.validateWSDLFile(resourceFile.getContent());
        }
        if (validationResponse.isValid()) {
            API api = getAPI(apiIdentifier);
            WSDLProcessor wsdlProcessor = validationResponse.getWsdlProcessor();
            wsdlProcessor.updateEndpoints(api, environmentName, environmentType);
            InputStream wsdlDataStream = wsdlProcessor.getWSDL();
            return new ResourceFile(wsdlDataStream, resourceFile.getContentType());
        } else {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.CORRUPTED_STORED_WSDL,
                    apiIdentifier.toString()));
        }
    }

    @Override
    public Set<SubscribedAPI> getLightWeightSubscribedIdentifiers(Subscriber subscriber, APIIdentifier apiIdentifier,
            String groupingId) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPISet = new HashSet<SubscribedAPI>();
        Set<SubscribedAPI> subscribedAPIs = getLightWeightSubscribedAPIs(subscriber, groupingId);
        for (SubscribedAPI api : subscribedAPIs) {
            if (api.getApiId().equals(apiIdentifier)) {
                subscribedAPISet.add(api);
            }
        }
        return subscribedAPISet;
    }

    public Set<APIKey> getApplicationKeysOfApplication(int applicationId) throws APIManagementException {
        Set<APIKey> apikeys = getApplicationKeys(applicationId);
        return apikeys;
    }



    /**
     * To check authorization of the API against current logged in user. If the user is not authorized an exception
     * will be thrown.
     *
     * @param identifier API identifier
     * @throws APIManagementException APIManagementException
     */
    protected void checkAccessControlPermission(Identifier identifier) throws APIManagementException {
        if (identifier == null || !isAccessControlRestrictionEnabled) {
            if (!isAccessControlRestrictionEnabled && log.isDebugEnabled() && identifier != null) {
                log.debug(
                        "Publisher access control restriction is not enabled. Hence the API/Product " + identifier.getName()
                                + " should not be checked for further permission. Registry permission check "
                                + "is sufficient");
            }
            return;
        }
        String resourcePath = StringUtils.EMPTY;
        String identifierType = StringUtils.EMPTY;
        if (identifier instanceof APIIdentifier) {
            resourcePath = APIUtil.getAPIPath((APIIdentifier) identifier);
            identifierType = APIConstants.API_IDENTIFIER_TYPE;
        } else if (identifier instanceof APIProductIdentifier) {
            resourcePath = APIUtil.getAPIProductPath((APIProductIdentifier) identifier);
            identifierType = APIConstants.API_PRODUCT_IDENTIFIER_TYPE;
        }
        Registry registry;
        try {
            // Need user name with tenant domain to get correct domain name from
            // MultitenantUtils.getTenantDomain(username)
            String userNameWithTenantDomain = (userNameWithoutChange != null) ? userNameWithoutChange : username;
            String apiTenantDomain = getTenantDomain(identifier);
            int apiTenantId = getTenantManager().getTenantId(apiTenantDomain);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(apiTenantDomain)) {
                APIUtil.loadTenantRegistry(apiTenantId);
            }

            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) { //cross tenant scenario
                registry = getRegistryService().getGovernanceUserRegistry(
                        getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName())),
                        apiTenantId);
            } else {
                registry = this.registry;
            }
            Resource resource = registry.get(resourcePath);
            String accessControlProperty = resource.getProperty(APIConstants.ACCESS_CONTROL);
            if (accessControlProperty == null || accessControlProperty.trim().isEmpty() || accessControlProperty
                    .equalsIgnoreCase(APIConstants.NO_ACCESS_CONTROL)) {
                if (log.isDebugEnabled()) {
                    log.debug(identifierType + " in the path  " + resourcePath + " does not have any access control restriction");
                }
                return;
            }
            if (APIUtil.hasPermission(userNameWithTenantDomain, APIConstants.Permissions.APIM_ADMIN)) {
                return;
            }
            String storeVisibilityRoles = resource.getProperty(APIConstants.STORE_VIEW_ROLES);
            if (storeVisibilityRoles != null && !storeVisibilityRoles.trim().isEmpty()) {
                String[] storeVisibilityRoleList = storeVisibilityRoles.split(",");
                if (log.isDebugEnabled()) {
                    log.debug(identifierType + " has restricted access to users with the roles : " + Arrays
                            .toString(storeVisibilityRoleList));
                }
                String[] userRoleList = APIUtil.getListOfRoles(userNameWithTenantDomain);
                if (log.isDebugEnabled()) {
                    log.debug("User " + username + " has roles " + Arrays.toString(userRoleList));
                }
                for (String role : storeVisibilityRoleList) {
                    role = role.trim();
                    if (role.equalsIgnoreCase(APIConstants.NULL_USER_ROLE_LIST) || APIUtil
                            .compareRoleList(userRoleList, role)) {
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug(identifierType + " " + identifier + " cannot be accessed by user '" + username + "'. It "
                            + "has a store visibility  restriction");
                }
                throw new APIMgtAuthorizationFailedException(
                        APIConstants.UN_AUTHORIZED_ERROR_MESSAGE + " view  the " + identifierType + " " + identifier);
            }
        } catch (RegistryException e) {
            throw new APIManagementException(
                    "Registry Exception while trying to check the store visibility restriction of " + identifierType + " " + identifier
                            .getName(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get " + identifierType + " from : " + resourcePath;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * This method is used to get the updated wsdl with the respective environment apis are published
     *
     * @param wsdlResourcePath registry resource path to the wsdl
     * @param wsdlContent      wsdl resource content as byte array
     * @param environmentType  gateway environment type
     * @return updated wsdl content with environment endpoints
     * @throws APIManagementException
     */
    private byte[] getUpdatedWSDLByEnvironment(String wsdlResourcePath, byte[] wsdlContent, String environmentName,
            String environmentType, String apiName, String apiVersion, String apiProvider)
            throws APIManagementException {
        APIMWSDLReader apimwsdlReader = new APIMWSDLReader(wsdlResourcePath);
        Definition definition = apimwsdlReader.getWSDLDefinitionFromByteContent(wsdlContent, false);

        byte[] updatedWSDLContent = null;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiProvider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
            int tenantId;
            UserRegistry registry;

            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
                registry = registryService.getGovernanceSystemRegistry(tenantId);
                API api = null;
                if (!StringUtils.isEmpty(apiName) && !StringUtils.isEmpty(apiVersion)) {
                    APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(apiProvider), apiName, apiVersion);
                    if (log.isDebugEnabled()) {
                        log.debug("Api identifier for the soap api artifact: " + apiIdentifier + "for api name: "
                                          + apiName + ", version: " + apiVersion);
                    }
                    GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);
                    api = APIUtil.getAPI(apiArtifact);
                    if (log.isDebugEnabled()) {
                        if (api != null) {
                            log.debug(
                                    "Api context for the artifact with id:" + api.getId() + " is " + api.getContext());
                        } else {
                            log.debug("Api does not exist for api name: " + apiIdentifier.getApiName());
                        }
                    }
                } else {
                    handleException("Artifact does not exist in the registry for api name: " + apiName +
                                            " and version: " + apiVersion);
                }

                if (api != null) {
                    try {
                        apimwsdlReader.setServiceDefinition(definition, api, environmentName, environmentType);
                        if (log.isDebugEnabled()) {
                            log.debug("Soap api with context:" + api.getContext() + " in " + environmentName
                                              + " with environment type" + environmentType);
                        }
                        updatedWSDLContent = apimwsdlReader.getWSDL(definition);
                    } catch (APIManagementException e) {
                        handleException("Error occurred while processing the wsdl for api: [" + api.getId() + "]", e);
                    }
                } else {
                    handleException("Error while getting API object for wsdl artifact");
                }
            } catch (UserStoreException e) {
                handleException("Error while reading tenant information", e);
            } catch (RegistryException e) {
                handleException("Error when create registry instance", e);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return updatedWSDLContent;
    }

    /**
     * This method is used to get keys of custom attributes, configured by user
     *
     * @param userId user name of logged in user
     * @return Array of JSONObject, contains keys of attributes
     * @throws APIManagementException
     */
    public JSONArray getAppAttributesFromConfig(String userId) throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        int tenantId = 0;
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error in getting tenantId of " + tenantDomain, e);
        }
        JSONArray applicationAttributes = null;
        JSONObject applicationConfig = APIUtil.getAppAttributeKeysFromRegistry(tenantId);
        if (applicationConfig != null) {
            applicationAttributes = (JSONArray) applicationConfig.get(APIConstants.ApplicationAttributes.ATTRIBUTES);
        } else {
            APIManagerConfiguration configuration = getAPIManagerConfiguration();
            applicationAttributes = configuration.getApplicationAttributes();
        }
        return applicationAttributes;
    }

    /**
     * This method is used to validate keys of custom attributes, configured by user
     *
     * @param application
     * @param userId user name of logged in user
     * @throws APIManagementException
     */
    public void checkAppAttributes(Application application, String userId) throws APIManagementException {

        JSONArray applicationAttributesFromConfig = getAppAttributesFromConfig(userId);
        Map<String, String> applicationAttributes = application.getApplicationAttributes();
        List attributeKeys = new ArrayList<String>();
        int applicationId = application.getId();
        int tenantId = 0;
        Map<String, String> newApplicationAttributes = new HashMap<>();
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        try {
            tenantId = getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Error in getting tenantId of " + tenantDomain, e);
        }

        for (Object object : applicationAttributesFromConfig) {
            JSONObject attribute = (JSONObject) object;
            attributeKeys.add(attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE));
        }

        for (Object key : applicationAttributes.keySet()) {
            if (!attributeKeys.contains(key)) {
                apiMgtDAO.deleteApplicationAttributes((String) key, applicationId);
                if (log.isDebugEnabled()) {
                    log.debug("Removing " + key + "from application - " + application.getName());
                }
            }
        }

        for (Object key : attributeKeys) {
            if (!applicationAttributes.keySet().contains(key)) {
                newApplicationAttributes.put((String) key, "");
            }
        }
        apiMgtDAO.addApplicationAttributes(newApplicationAttributes, applicationId, tenantId);
    }

    /**
     * Store specific implementation of search paginated apis by content
     * @param registry
     * @param searchQuery
     * @param start
     * @param end
     * @return
     * @throws APIManagementException
     */
    public Map<String, Object> searchPaginatedAPIsByContent(Registry registry, int tenantId, String searchQuery,
            int start, int end, boolean limitAttributes) throws APIManagementException {

        Map<String, Object> searchResults = super
                .searchPaginatedAPIsByContent(registry, tenantId, searchQuery, start, end, limitAttributes);
        return filterMultipleVersionedAPIs(searchResults);
    }


    @Override
    public String getOpenAPIDefinition(Identifier apiId) throws APIManagementException {
        String definition = super.getOpenAPIDefinition(apiId);
        return APIUtil.removeXMediationScriptsFromSwagger(definition);
    }

    @Override
    public String getOpenAPIDefinitionForEnvironment(Identifier apiId, String environmentName)
            throws APIManagementException {
        String apiTenantDomain;
        String updatedDefinition = null;
        Map<String,String> hostsWithSchemes;
        String definition = super.getOpenAPIDefinition(apiId);
        APIDefinition oasParser = OASParserUtil.getOASParser(definition);
        if (apiId instanceof APIIdentifier) {
            API api = getLightweightAPI((APIIdentifier) apiId);
            //todo: use get api by id, so no need to set scopes or uri templates
            api.setScopes(oasParser.getScopes(definition));
            api.setUriTemplates(oasParser.getURITemplates(definition));
            apiTenantDomain = MultitenantUtils.getTenantDomain(api.getId().getProviderName());
            hostsWithSchemes = getHostWithSchemeMappingForEnvironment(apiTenantDomain, environmentName);
            api.setContext(getBasePath(apiTenantDomain, api.getContext()));
            updatedDefinition = oasParser.getOASDefinitionForStore(api, definition, hostsWithSchemes);
        } else if (apiId instanceof APIProductIdentifier) {
            APIProduct apiProduct = getAPIProduct((APIProductIdentifier) apiId);
            apiTenantDomain = MultitenantUtils.getTenantDomain(apiProduct.getId().getProviderName());
            hostsWithSchemes = getHostWithSchemeMappingForEnvironment(apiTenantDomain, environmentName);
            apiProduct.setContext(getBasePath(apiTenantDomain, apiProduct.getContext()));
            updatedDefinition = oasParser.getOASDefinitionForStore(apiProduct, definition, hostsWithSchemes);
        }
        return updatedDefinition;
    }

    @Override
    public String getOpenAPIDefinitionForLabel(Identifier apiId, String labelName) throws APIManagementException {
        List<Label> gatewayLabels;
        String updatedDefinition = null;
        Map<String,String> hostsWithSchemes;
        String definition = super.getOpenAPIDefinition(apiId);
        APIDefinition oasParser = OASParserUtil.getOASParser(definition);
        if (apiId instanceof APIIdentifier) {
            API api = getLightweightAPI((APIIdentifier) apiId);
            gatewayLabels = api.getGatewayLabels();
            hostsWithSchemes = getHostWithSchemeMappingForLabel(gatewayLabels, labelName);
            updatedDefinition = oasParser.getOASDefinitionForStore(api, definition, hostsWithSchemes);
        } else if (apiId instanceof APIProductIdentifier) {
            APIProduct apiProduct = getAPIProduct((APIProductIdentifier) apiId);
            gatewayLabels = apiProduct.getGatewayLabels();
            hostsWithSchemes = getHostWithSchemeMappingForLabel(gatewayLabels, labelName);
            updatedDefinition = oasParser.getOASDefinitionForStore(apiProduct, definition, hostsWithSchemes);
        }
        return updatedDefinition;
    }

    public void revokeAPIKey(String apiKey, long expiryTime, String tenantDomain) throws APIManagementException {
        String baseUrl = APIConstants.HTTPS_PROTOCOL_URL_PREFIX + System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.UTILITY_WEB_APP_EP;
        String apiKeyRevokeEp = baseUrl + APIConstants.API_KEY_REVOKE_PATH;
        HttpPost method = new HttpPost(apiKeyRevokeEp);
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        URL keyMgtURL = null;
        try {
            keyMgtURL = new URL(apiKeyRevokeEp);
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
            byte[] credentials = Base64.encodeBase64((username + ":" + password).getBytes
                    (StandardCharsets.UTF_8));
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            JSONObject revokeRequestPayload = new JSONObject();
            revokeRequestPayload.put("apikey", apiKey);
            revokeRequestPayload.put("expiryTime", expiryTime);
            revokeRequestPayload.put("tenantId", tenantId);
            StringEntity requestEntity = new StringEntity(revokeRequestPayload.toString());
            requestEntity.setContentType(APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            method.setEntity(requestEntity);
            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(method);
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("API Key revocation is unsuccessful with token signature " + APIUtil.getMaskedToken(apiKey));
                throw new APIManagementException("Error while revoking API Key");
            }
        } catch (MalformedURLException e) {
            String msg = "Error while constructing key manager URL " + apiKeyRevokeEp;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (IOException e) {
            String msg = "Error while executing the http client " + apiKeyRevokeEp;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    private Map<String, Object> filterMultipleVersionedAPIs(Map<String, Object> searchResults) {
        Object apiObj = searchResults.get("apis");
        ArrayList<Object> apiSet;
        ArrayList<APIProduct> apiProductSet = new ArrayList<>();
        if (apiObj instanceof Set) {
            apiSet = new ArrayList<>(((Set) apiObj));
        } else {
            apiSet = (ArrayList<Object>) apiObj;
        }

        //filter store results if displayMultipleVersions is set to false
        Boolean displayMultipleVersions = APIUtil.isAllowDisplayMultipleVersions();
        if (!displayMultipleVersions) {
            SortedSet<API> resultApis = new TreeSet<API>(new APINameComparator());

            for (Object result : apiSet) {
                if (result instanceof API) {
                    resultApis.add((API)result);
                } else if (result instanceof Map.Entry) {
                    Map.Entry<Documentation, API> entry = (Map.Entry<Documentation, API>)result;
                    resultApis.add(entry.getValue());
                } else if (result instanceof APIProduct) {
                    apiProductSet.add((APIProduct)result);
                }
            }

            Map<String, API> latestPublishedAPIs = new HashMap<String, API>();
            Comparator<API> versionComparator = new APIVersionComparator();
            String key;

            //Run the result api list through API version comparator and filter out multiple versions
            for (API api : resultApis) {
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
            }

            //filter apiSet
            ArrayList<Object> tempApiSet = new ArrayList<Object>();
            for (Object result : apiSet) {
                API api = null;
                String mapKey;
                API latestAPI;
                if (result instanceof API) {
                    api = (API) result;
                    mapKey = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
                    if (latestPublishedAPIs.containsKey(mapKey)) {
                        latestAPI = latestPublishedAPIs.get(mapKey);
                        if (latestAPI.getId().equals(api.getId())) {
                            tempApiSet.add(api);
                        }
                    }
                } else if (result instanceof Map.Entry) {
                    Map.Entry<Documentation, API> docEntry = (Map.Entry<Documentation, API>) result;
                    api = docEntry.getValue();
                    mapKey = api.getId().getProviderName() + COLON_CHAR + api.getId().getApiName();
                    if (latestPublishedAPIs.containsKey(mapKey)) {
                        latestAPI = latestPublishedAPIs.get(mapKey);
                        if (latestAPI.getId().equals(api.getId())) {
                            tempApiSet.add(docEntry);
                        }
                    }
                }
            }
            apiSet = tempApiSet;
            ArrayList<Object> resultAPIandProductSet = new ArrayList<>();
            resultAPIandProductSet.addAll(apiSet);
            resultAPIandProductSet.addAll(apiProductSet);
            resultAPIandProductSet.sort(new ContentSearchResultNameComparator());

            if (apiObj instanceof Set) {
                searchResults.put("apis", new HashSet<>(resultAPIandProductSet));
            } else {
                searchResults.put("apis", resultAPIandProductSet);
            }
        }
        return searchResults;
    }

    /**
     * Validate application attributes and remove attributes that does not exist in the config
     *
     * @param applicationAttributes Application attributes provided
     * @param keys Application attribute keys in config
     * @return Validated application attributes
     */
    private Map<String, String> validateApplicationAttributes(Map<String, String> applicationAttributes, Set keys) {

        Iterator iterator = applicationAttributes.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (!keys.contains(key)) {
                iterator.remove();
                applicationAttributes.remove(key);
            }
        }
        return applicationAttributes;
    }

    /**
     * Get host names with transport scheme mapping from Gateway Environments in api-manager.xml or from the tenant
     * custom url config in registry.
     *
     * @param apiTenantDomain Tenant domain
     * @param environmentName Environment name
     * @return Host name to transport scheme mapping
     * @throws APIManagementException if an error occurs when getting host names with schemes
     */
    private Map<String, String> getHostWithSchemeMappingForEnvironment(String apiTenantDomain, String environmentName)
            throws APIManagementException {

        Map<String, String> domains = getTenantDomainMappings(apiTenantDomain, APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
        Map<String, String> hostsWithSchemes = new HashMap<>();
        if (!domains.isEmpty()) {
            String customUrl = domains.get(APIConstants.CUSTOM_URL);
            if (customUrl.startsWith(APIConstants.HTTP_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.HTTP_PROTOCOL, customUrl);
            } else {
                hostsWithSchemes.put(APIConstants.HTTPS_PROTOCOL, customUrl);
            }
        } else {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            Map<String, Environment> allEnvironments = config.getApiGatewayEnvironments();
            Environment environment = allEnvironments.get(environmentName);

            if (environment == null) {
                handleException(
                        "Could not find provided environment '" + environmentName);
            }

            assert environment != null;
            String[] hostsWithScheme = environment.getApiGatewayEndpoint().split(",");
            for (String url : hostsWithScheme) {
                if (url.startsWith(APIConstants.HTTPS_PROTOCOL_URL_PREFIX)) {
                    hostsWithSchemes.put(APIConstants.HTTPS_PROTOCOL, url);
                }
                if (url.startsWith(APIConstants.HTTP_PROTOCOL_URL_PREFIX)) {
                    hostsWithSchemes.put(APIConstants.HTTP_PROTOCOL, url);
                }
            }
        }
        return hostsWithSchemes;
    }

    /**
     * Get gateway host names with transport scheme mapping.
     *
     * @param gatewayLabels gateway label list
     * @param labelName     Label name
     * @return Hostname with transport schemes
     * @throws APIManagementException If an error occurs when getting gateway host names.
     */
    private Map<String, String> getHostWithSchemeMappingForLabel(List<Label> gatewayLabels, String labelName)
            throws APIManagementException {

        Map<String, String> hostsWithSchemes = new HashMap<>();
        Label labelObj = null;
        for (Label label : gatewayLabels) {
            if (label.getName().equals(labelName)) {
                labelObj = label;
                break;
            }
        }
        if (labelObj == null) {
            handleException(
                    "Could not find provided label '" + labelName);
            return null;
        }

        List<String> accessUrls = labelObj.getAccessUrls();
        for (String url : accessUrls) {
            if (url.startsWith(APIConstants.HTTPS_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.HTTPS_PROTOCOL, url);
            }
            if (url.startsWith(APIConstants.HTTP_PROTOCOL_URL_PREFIX)) {
                hostsWithSchemes.put(APIConstants.HTTP_PROTOCOL, url);
            }
        }
        return hostsWithSchemes;
    }

    private String getBasePath(String apiTenantDomain, String basePath) throws APIManagementException {
        Map<String, String> domains =
                getTenantDomainMappings(apiTenantDomain, APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
        if (!domains.isEmpty()) {
            return basePath.replace("/t/" + apiTenantDomain, "");
        }
        return basePath;
    }
}
