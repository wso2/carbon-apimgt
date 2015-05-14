/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.Icon;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Provider;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.Usage;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.observers.APIStatusObserverList;
import org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.APIVersionStringComparator;
import org.wso2.carbon.apimgt.impl.utils.CommonUtil;
import org.wso2.carbon.apimgt.keymgt.client.ProviderKeyMgtClient;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides the core API provider functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * programmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIProviderImpl extends AbstractAPIManager implements APIProvider {
	
	private static final Log log = LogFactory.getLog(APIProviderImpl.class);

    public APIProviderImpl(String username) throws APIManagementException {
        super(username);
    }

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws APIManagementException {
        Set<Provider> providerSet = new HashSet<Provider>();
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                            APIConstants.PROVIDER_KEY);
        try {
            GenericArtifact[] genericArtifact = artifactManager.getAllGenericArtifacts();
            if (genericArtifact == null || genericArtifact.length == 0) {
                return providerSet;
            }
            for (GenericArtifact artifact : genericArtifact) {
                Provider provider =
                        new Provider(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_NAME));
                provider.setDescription(APIConstants.PROVIDER_OVERVIEW_DESCRIPTION);
                provider.setEmail(APIConstants.PROVIDER_OVERVIEW_EMAIL);
                providerSet.add(provider);
            }
        } catch (GovernanceException e) {
            handleException("Failed to get all providers", e);
        }
        return providerSet;
    }

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get set of API
     */
    public JSONArray getAPIsByProvider(String providerId) throws APIManagementException {
        return getJSONfyAPIsByProvider(getAPIsByProviderSet(providerId));

    }

	private List<API> getAPIsByProviderSet(String providerId) throws APIManagementException {
		List<API> apiSortedList = new ArrayList<API>();
		if (providerId != null) {
			boolean isTenantFlowStarted = false;
			try {
				String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerId));
				if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
					isTenantFlowStarted = true;
					PrivilegedCarbonContext.startTenantFlow();
					PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
				}


				providerId = APIUtil.replaceEmailDomain(providerId);
				String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
				                      providerId;
				GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
						APIConstants.API_KEY);
				Association[] associations = registry.getAssociations(providerPath,
						APIConstants.PROVIDER_ASSOCIATION);
				for (Association association : associations) {
					String apiPath = association.getDestinationPath();
					Resource resource = registry.get(apiPath);
					String apiArtifactId = resource.getUUID();
					if (apiArtifactId != null) {
						GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactId);
						apiSortedList.add(APIUtil.getAPI(apiArtifact, registry));
					} else {
						throw new GovernanceException("artifact id is null of " + apiPath);
					}
				}

			} catch (RegistryException e) {
				handleException("Failed to get APIs for provider : " + providerId, e);
			} finally {
				if (isTenantFlowStarted) {
					PrivilegedCarbonContext.endTenantFlow();
				}
			}
		}
		Collections.sort(apiSortedList, new APINameComparator());
		return apiSortedList;
	}
    private JSONArray getJSONfyAPIsByProvider(List<API> apiList) {
        JSONArray result = new JSONArray();
        if (apiList != null) {
            Iterator it = apiList.iterator();
            int i = 0;
            while (it.hasNext()) {
                JSONObject row = new JSONObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("name", apiIdentifier.getApiName());
                row.put("version", apiIdentifier.getVersion());
                row.put("provider", APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
                row.put("lastUpdatedDate", api.getLastUpdated().toString());
                result.add(i, row);
                i++;
            }
        }
        return result;
    }


    /**
         * Get a list of all the consumers for all APIs
         *
         * @param providerId if of the provider
         * @return Set<Subscriber>
         * @throws org.wso2.carbon.apimgt.api.APIManagementException
         *          if failed to get subscribed APIs of given provider
         */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfProvider(providerId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscribers for : " + providerId, e);
        }
        return subscriberSet;
    }

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Provider
     */
    public Provider getProvider(String providerName) throws APIManagementException {
        Provider provider = null;
        String providerPath = APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                     RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                              APIConstants.PROVIDERS_PATH +
                              RegistryConstants.PATH_SEPARATOR + providerName;
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.PROVIDER_KEY);
            Resource providerResource = registry.get(providerPath);
            String artifactId =
                    providerResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact it is null");
            }
            GenericArtifact providerArtifact = artifactManager.getGenericArtifact(artifactId);
            provider = APIUtil.getProvider(providerArtifact);

        } catch (RegistryException e) {
            handleException("Failed to get Provider form : " + providerName, e);
        }
        return provider;
    }

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier) {
        return null;
    }

    /**
     * Return Usage of given provider and API
     *
     * @param providerId if of the provider
     * @param apiName    name of the API
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName) {
        return null;
    }

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerName Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    public JSONArray getAllAPIUsageByProvider(
            String providerName) throws APIManagementException {
        UserApplicationAPIUsage[] result = apiMgtDAO.getAllAPIUsageByProvider(providerName);
        return getJSONfyUsageByProviderResult(result);
    }

    private JSONArray getJSONfyUsageByProviderResult(UserApplicationAPIUsage[] apiUsages) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < apiUsages.length; i++) {
            JSONObject row = new JSONObject();
            row.put("userName", apiUsages[i].getUserId());
            row.put("application", apiUsages[i].getApplicationName());
            row.put("appId", "" + apiUsages[i].getAppId());
            row.put("token", apiUsages[i].getAccessToken());
            row.put("tokenStatus", apiUsages[i].getAccessTokenStatus());
            row.put("subStatus", apiUsages[i].getSubStatus());

            StringBuilder apiSet = new StringBuilder("");
            for (int k = 0; k < apiUsages[i].getApiSubscriptions().length; k++) {
                apiSet.append(apiUsages[i].getApiSubscriptions()[k].getSubStatus());
                apiSet.append("::");
                apiSet.append(apiUsages[i].getApiSubscriptions()[k].getApiId().getApiName());
                apiSet.append("::");
                apiSet.append(apiUsages[i].getApiSubscriptions()[k].getApiId().getVersion());
                if (k != apiUsages[i].getApiSubscriptions().length - 1) {
                    apiSet.append(",");
                }
            }
            row.put("apis", apiSet.toString());
            result.add(i, row);
        }
        return result;
    }

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail) {
        return null;
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws APIManagementException {
        long count = 0L;
        try {
            count = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get APISubscriptionCount for: " + identifier.getApiName(), e);
        }
        return count;
    }

    public void addTier(Tier tier) throws APIManagementException {
        addOrUpdateTier(tier, false);
    }

    public void updateTier(Tier tier) throws APIManagementException {
        addOrUpdateTier(tier, true);
    }

    private void addOrUpdateTier(Tier tier, boolean update) throws APIManagementException {
        if (APIConstants.UNLIMITED_TIER.equals(tier.getName())) {
            throw new APIManagementException("Changes on the '" + APIConstants.UNLIMITED_TIER + "' " +
                                             "tier are not allowed");
        }

        Set<Tier> tiers = getTiers();
        if (update && !tiers.contains(tier)) {
            throw new APIManagementException("No tier exists by the name: " + tier.getName());
        }

        Set<Tier> finalTiers = new HashSet<Tier>();
        for (Tier t : tiers) {
            if (!t.getName().equals(tier.getName())) {
                finalTiers.add(t);
            }
        }
        finalTiers.add(tier);
        saveTiers(finalTiers);
    }

    private void saveTiers(Collection<Tier> tiers) throws APIManagementException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement root = fac.createOMElement(APIConstants.POLICY_ELEMENT);
        OMElement assertion = fac.createOMElement(APIConstants.ASSERTION_ELEMENT);
        try {
            Resource resource = registry.newResource();
            for (Tier tier : tiers) {
                String policy = new String(tier.getPolicyContent());
                assertion.addChild(AXIOMUtil.stringToOM(policy));
                // if (tier.getDescription() != null && !"".equals(tier.getDescription())) {
                //     resource.setProperty(APIConstants.TIER_DESCRIPTION_PREFIX + tier.getName(),
                //              tier.getDescription());
                //  }
            }
            //resource.setProperty(APIConstants.TIER_DESCRIPTION_PREFIX + APIConstants.UNLIMITED_TIER,
            //        APIConstants.UNLIMITED_TIER_DESC);
            root.addChild(assertion);
            resource.setContent(root.toString());
            registry.put(APIConstants.API_TIER_LOCATION, resource);
        } catch (XMLStreamException e) {
            handleException("Error while constructing tier policy file", e);
        } catch (RegistryException e) {
            handleException("Error while saving tier configurations to the registry", e);
        }
    }

    public void removeTier(Tier tier) throws APIManagementException {
        if (APIConstants.UNLIMITED_TIER.equals(tier.getName())) {
            throw new APIManagementException("Changes on the '" + APIConstants.UNLIMITED_TIER + "' " +
                                             "tier are not allowed");
        }

        Set<Tier> tiers = getTiers();
        if (tiers.remove(tier)) {
            saveTiers(tiers);
        } else {
            throw new APIManagementException("No tier exists by the name: " + tier.getName());
        }
    }

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add API
     */
    public String addAPI(API api) throws APIManagementException {
        try {
            String id=createAPI(api);
            int tenantId = -1234;
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new APIManagementException("Error in retrieving Tenant Information while adding api :"
                        +api.getId().getApiName(),e);
            }
            apiMgtDAO.addAPI(api,tenantId);
            if (APIUtil.isAPIManagementEnabled()) {
            	Cache contextCache = APIUtil.getAPIContextCache();
            	Boolean apiContext = null;
            	if (contextCache.get(api.getContext()) != null) {
            		apiContext = Boolean.parseBoolean(contextCache.get(api.getContext()).toString());
            	} 
            	if (apiContext == null) {
                    contextCache.put(api.getContext(), true);
                }
            }
            return id;
        } catch (APIManagementException e) {          
            throw new APIManagementException("Error in adding API :"+api.getId().getApiName(),e);
        }
    }

    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus Current status of the API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException on error
     */
    private void saveAPIStatus(String artifactId, String apiStatus) throws APIManagementException{
        try{
            Resource resource = registry.get(artifactId);
            if (resource != null) {
                String propValue = resource.getProperty(APIConstants.API_STATUS);
                if (propValue == null) {
                    resource.addProperty(APIConstants.API_STATUS, apiStatus);
                } else {
                    resource.setProperty(APIConstants.API_STATUS, apiStatus);
                }
                registry.put(artifactId,resource);
            }
        }catch (RegistryException e) {
            handleException("Error while adding API", e);
        }
    }


    public String getDefaultVersion(JSONObject apiObj) throws APIManagementException {
        String provider = (String) apiObj.get("provider");
        provider = APIUtil.replaceEmailDomain(provider);
        String apiname = (String) apiObj.get("name");
        String version = ""; // unused attribute
        APIIdentifier apiid = new APIIdentifier(provider, apiname, version);
        return getDefaultVersion(apiid);
    }

    private String getDefaultVersion(APIIdentifier apiid) throws APIManagementException {
        String defaultVersion = null;
        try {
            defaultVersion = apiMgtDAO.getDefaultVersion(apiid);
        } catch (APIManagementException e) {
            handleException("Error while getting default version :" + apiid.getApiName(), e);
        }
        return defaultVersion;
    }

    public String getPublishedDefaultVersion(APIIdentifier apiid) throws APIManagementException{

        String defaultVersion=null;
        try{
            defaultVersion=apiMgtDAO.getPublishedDefaultVersion(apiid);
        } catch (APIManagementException e) {
            handleException("Error while getting published default version :" +apiid.getApiName(),e);
        }
        return defaultVersion;
    }


    /**
     * Updates an existing API
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update API
     * @return map of failed environments
     */
    public Map<String, List<String>> updateAPI(API api) throws APIManagementException {
        Map<String, List<String>> failedGateways = new HashMap<String, List<String>>();
        API oldApi = getAPI(api.getId());
        if (oldApi.getStatus().equals(api.getStatus())) {
            try {

                String previousDefaultVersion = getDefaultVersion(api.getId());
                String publishedDefaultVersion = getPublishedDefaultVersion(api.getId());

                if(previousDefaultVersion!=null){

                    APIIdentifier defaultAPIId=new APIIdentifier(api.getId().getProviderName(),api.getId().getApiName(),previousDefaultVersion);
                    if(api.isDefaultVersion() ^ api.getId().getVersion().equals(previousDefaultVersion)){ //A change has happen
                        //Remove the previous default API entry from the Registry
                        updateDefaultAPIInRegistry(defaultAPIId,false);
                        if(!api.isDefaultVersion()){//default api tick is removed
                            //todo: if it is ok, these two variables can be put to the top of the function to remove duplication
                            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
                            String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
                            if (gatewayType.equalsIgnoreCase(APIConstants.API_GATEWAY_TYPE_SYNAPSE)) {
                                removeDefaultAPIFromGateway(api);
                            }
                        }
                    }
                }

                boolean updatePermissions = false;
                if(!oldApi.getVisibility().equals(api.getVisibility()) || (oldApi.getVisibility().equals(APIConstants.API_RESTRICTED_VISIBILITY) && !api.getVisibleRoles().equals(oldApi.getVisibleRoles()))){
                    updatePermissions = true;
                }
                updateApiArtifact(api, true,updatePermissions);
                if (!oldApi.getContext().equals(api.getContext())) {
                    api.setApiHeaderChanged(true);
                }

                int tenantId = -1234;
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                try {
                    tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    throw new APIManagementException("Error in retrieving Tenant Information while updating api :"
                            +api.getId().getApiName(),e);
                }
                apiMgtDAO.updateAPI(api,tenantId);

                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
                String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
                boolean isAPIPublished = false;
                // gatewayType check is required when API Management is deployed on other servers to avoid synapse
                if (gatewayType.equalsIgnoreCase(APIConstants.API_GATEWAY_TYPE_SYNAPSE)) {
                    isAPIPublished = isAPIPublished(api);
                    if (gatewayExists) {
                        if (isAPIPublished) {
                            API apiPublished = getAPI(api.getId());
                            apiPublished.setAsDefaultVersion(api.isDefaultVersion());
                            if(api.getId().getVersion().equals(previousDefaultVersion) && !api.isDefaultVersion()){
                                //default version tick has been removed so a default api for current should not be added/updated
                                apiPublished.setAsPublishedDefaultVersion(false);
                            }else{
                                apiPublished.setAsPublishedDefaultVersion(api.getId().getVersion().equals(publishedDefaultVersion));
                            }
                            apiPublished.setOldInSequence(oldApi.getInSequence());
                            apiPublished.setOldOutSequence(oldApi.getOutSequence());
                            Set<String> environmentsToRemove =
                                    new HashSet<String>(oldApi.getEnvironments());
                            Set<String> environmentsToPublish =
                                    new HashSet<String>(apiPublished.getEnvironments());
                            Set<String> environmentsRemoved =
                                    new HashSet<String>(oldApi.getEnvironments());
                            if (!environmentsToPublish.isEmpty() && !environmentsToRemove.isEmpty()) {
                                environmentsRemoved.retainAll(environmentsToPublish);
                                environmentsToRemove.removeAll(environmentsRemoved);
                            }
                            List<String> failedToPublishEnvironments =
                                    publishToGateway(apiPublished);
                            apiPublished.setEnvironments(environmentsToRemove);
                            List<String> failedToRemoveEnvironments =
                                    removeFromGateway(apiPublished);
                            environmentsToPublish.removeAll(failedToPublishEnvironments);
                            environmentsToPublish.addAll(failedToRemoveEnvironments);
                            apiPublished.setEnvironments(environmentsToPublish);
                            updateApiArtifact(apiPublished, true, false);
                            failedGateways.clear();
                            failedGateways.put("UNPUBLISHED", failedToRemoveEnvironments);
                            failedGateways.put("PUBLISHED", failedToPublishEnvironments);
                        } else if (api.getStatus() == APIStatus.PUBLISHED) {
                            List<String> failedToPublishEnvironments = publishToGateway(api);
                            if (!failedToPublishEnvironments.isEmpty()) {
                                Set<String> publishedEnvironments =
                                        new HashSet<String>(api.getEnvironments());
                                publishedEnvironments.removeAll(failedToPublishEnvironments);
                                api.setEnvironments(publishedEnvironments);
                                updateApiArtifact(api, true, false);
                                failedGateways.clear();
                                failedGateways.put("PUBLISHED", failedToPublishEnvironments);
                                failedGateways.put("UNPUBLISHED", Collections.EMPTY_LIST);
                            }
                        }
                    } else {
                        log.debug("Gateway is not existed for the current API Provider");
                    }
                }

                //If gateway(s) exist, remove resource paths saved on the cache.
                if (gatewayExists) {
                    if (isAPIPublished && !oldApi.getUriTemplates().equals(api.getUriTemplates())) {
                        Set<URITemplate> resourceVerbs = api.getUriTemplates();

                        Map<String, Environment> gatewayEns = config.getApiGatewayEnvironments();
                        for (Environment environment : gatewayEns.values()) {
                            try {
                            APIAuthenticationAdminClient client =
                                    new APIAuthenticationAdminClient(environment);
                            if(resourceVerbs != null){
                                for(URITemplate resourceVerb : resourceVerbs){
                                    String resourceURLContext = resourceVerb.getUriTemplate();
                                    //If url context ends with the '*' character.
                                    //if(resourceURLContext.endsWith("*")){
                                        //Remove the ending '*'
                                    //    resourceURLContext = resourceURLContext.substring(0, resourceURLContext.length() - 1);
                                    //}
                                    client.invalidateResourceCache(api.getContext(),api.getId().getVersion(),resourceURLContext,resourceVerb.getHTTPVerb());
                                    if (log.isDebugEnabled()) {
                                        log.debug("Calling invalidation cache");
                                    }
                                }
                            }
                            } catch (AxisFault ex) {
                                log.error("Error while invalidating from environment " +
                                          environment.getName(), ex);
                            }
                        }

                    }
                }

                /* Create API Definition for Swagger Console if not created already*/
                String apiDefinitionFilePath = APIUtil.getAPIDefinitionFilePath(api.getId().getApiName(), api.getId().getVersion(), api.getId().getProviderName());
                if (!registry.resourceExists(apiDefinitionFilePath)) {
                	createUpdateAPIDefinition(api);
                }

                //update apiContext cache
                if (APIUtil.isAPIManagementEnabled()) {
                    Cache contextCache = APIUtil.getAPIContextCache();
                    contextCache.remove(oldApi.getContext());
                    contextCache.put(api.getContext(), true);
                }

            } catch (APIManagementException e) {
            	handleException("Error while updating the API :" +api.getId().getApiName(),e);
            } catch (RegistryException e) {
            	handleException("Error while creating swagger 1.1 API definition:" + api.getId().getApiName(),e);
			}

        } else {
            // We don't allow API status updates via this method.
            // Use changeAPIStatus for that kind of updates.
            throw new APIManagementException("Invalid API update operation involving API status changes");
        }
        return failedGateways;
    }



    private void updateApiArtifact(API api, boolean updateMetadata,boolean updatePermissions) throws APIManagementException {

        //Validate Transports
        validateAndSetTransports(api);

        try {
        	//registry.beginTransaction();
            String apiArtifactId = registry.get(APIUtil.getAPIPath(api.getId())).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            GenericArtifact updateApiArtifact = APIUtil.createAPIArtifactContent(artifact, api);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, updateApiArtifact.getId());
            org.wso2.carbon.registry.core.Tag[] oldTags = registry.getTags(artifactPath);
            if (oldTags != null) {
                for (org.wso2.carbon.registry.core.Tag tag : oldTags) {
                    registry.removeTag(artifactPath, tag.getTagName());
                }
            }

            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }

            if(api.isDefaultVersion()){
                updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "true");
            }else{
                updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "false");
            }

            if (updateMetadata && api.getEndpointConfig() != null) {
                // If WSDL URL get change only we update registry WSDL resource. If its registry resource patch we
                // will skip registry update. Only if this API created with WSDL end point type we need to update wsdls for each update.
                //check for wsdl endpoint
                org.json.JSONObject response1 = new org.json.JSONObject(api.getEndpointConfig());
                String wsdlURL = api.getWsdlUrl();
                if(response1.get("endpoint_type").toString().equalsIgnoreCase("wsdl")
                        && response1.has("production_endpoints")){
                    wsdlURL = response1.getJSONObject("production_endpoints").get("url").toString();
                    
                    if (APIUtil.isValidWSDLURL(wsdlURL, true)) {
                        String path = APIUtil.createWSDL(registry, api);
                        if (path != null) {
                            registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                            // reset the wsdl path to permlink
                            updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl());
                        }
                    }
                }                

                if (api.getUrl() != null && !"".equals(api.getUrl())){
                    String path = APIUtil.createEndpoint(api.getUrl(), registry);
                    if (path != null) {
                        registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                    }
                }
            }
            
            artifactManager.updateGenericArtifact(updateApiArtifact);
            
            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            String[] visibleRoles = new String[0];
            if(updatePermissions){
                clearResourcePermissions(artifactPath, api.getId());
                String visibleRolesList = api.getVisibleRoles();

                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,artifactPath);


            }
            //registry.commitTransaction();
            if(updatePermissions){
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                                             getAPIManagerConfigurationService().getAPIManagerConfiguration();
            boolean isSetDocLevelPermissions = Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS));
            String docRootPath=  APIUtil.getAPIDocPath(api.getId());
            if(isSetDocLevelPermissions){
                // Retain the docs
                List<Documentation> docs = getAllDocumentation(api.getId());

                for (Documentation doc : docs) {
                    if(APIConstants.API_DEFINITION_DOC_NAME.equals(doc.getName())){
                        String swaggerPath=APIUtil.getAPIDefinitionFilePath(api.getId().getApiName(),api.getId().getVersion(),api.getId().getProviderName());
                        Resource resource = registry.get(swaggerPath);
                        String visibility=resource.getProperty(APIConstants.VISIBILITY);
                        if ((APIConstants.DOC_API_BASED_VISIBILITY).equalsIgnoreCase(visibility)) {
                            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,swaggerPath);
                        }
                    }else{
                        if ((APIConstants.DOC_API_BASED_VISIBILITY).equalsIgnoreCase(doc.getVisibility().name())) {
                            String documentationPath = APIUtil.getAPIDocPath(api.getId()) + doc.getName();
                            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,documentationPath);
                            if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {
                                String contentPath = APIUtil.getAPIDocContentPath(api.getId(), doc.getName());
                                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,contentPath);
                            }else if (Documentation.DocumentSourceType.FILE.equals(doc.getSourceType()) && doc.getFilePath()!=null) {
                                String filePath = APIUtil.getDocumentationFilePath(api.getId(), doc.getFilePath().split("files"+RegistryConstants.PATH_SEPARATOR)[1]);
                                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,filePath);
                            }
                        }

                    }

            }}else{
                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,docRootPath);
            }
            }
        } catch (Exception e) {
        	 try {
                 registry.rollbackTransaction();
             } catch (RegistryException re) {
                 handleException("Error while rolling back the transaction for API: " +
                                 api.getId().getApiName(), re);
             }
             handleException("Error while performing registry transaction operation", e);
           
        }
    }
    
    /**
     * Create API Definition in JSON and save in the registry
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to generate the content and save
     */
    private void createUpdateAPIDefinition(API api) throws APIManagementException {
    	APIIdentifier identifier = api.getId(); 
    	
    	try{
    		String jsonText = APIUtil.createSwaggerJSONContent(api);
    		
    		String resourcePath = APIUtil.getAPIDefinitionFilePath(identifier.getApiName(), identifier.getVersion(),identifier.getProviderName());
    		
    		Resource resource = registry.newResource();
    		    		
    		resource.setContent(jsonText);
    		resource.setMediaType("application/json");
    		registry.put(resourcePath, resource);
    		
    		/*Set permissions to anonymous role */
    		APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, resourcePath);
    			    
    	} catch (RegistryException e) {
    		handleException("Error while adding API Definition for " + identifier.getApiName() + "-" + identifier.getVersion(), e);
		} catch (APIManagementException e) {
			handleException("Error while adding API Definition for " + identifier.getApiName() + "-" + identifier.getVersion(), e);
		}
    }

    public Map<String, List<String>> changeAPIStatus(API api, APIStatus status, String userId,
                                                     boolean updateGatewayConfig) throws APIManagementException {
        Map<String, List<String>> failedGateways = new ConcurrentHashMap<String, List<String>>();
        APIStatus currentStatus = api.getStatus();
        api.setStatus(status);
        MultitenantUtils.getTenantDomain(username);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        try {
            //If API status changed to publish we should add it to recently added APIs list
            //this should happen in store-publisher cluster domain if deployment is distributed
            //IF new API published we will add it to recently added APIs
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).removeAll();
            APIStatusObserverList observerList = APIStatusObserverList.getInstance();
            observerList.notifyObservers(currentStatus, status, api);
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

            api.setAsPublishedDefaultVersion(api.getId().getVersion().equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));

            if (gatewayType.equalsIgnoreCase(APIConstants.API_GATEWAY_TYPE_SYNAPSE) && updateGatewayConfig) {
                if (status.equals(APIStatus.PUBLISHED) || status.equals(APIStatus.DEPRECATED) ||
                    status.equals(APIStatus.BLOCKED) || status.equals(APIStatus.PROTOTYPED)) {
                    List<String> failedToPublishEnvironments = publishToGateway(api);
                    if (!failedToPublishEnvironments.isEmpty()) {
                        Set<String> publishedEnvironments =
                                new HashSet<String>(api.getEnvironments());
                        publishedEnvironments.removeAll(failedToPublishEnvironments);
                        api.setEnvironments(publishedEnvironments);
                        updateApiArtifact(api, true, false);
                        failedGateways.clear();
                        failedGateways.put("UNPUBLISHED", Collections.EMPTY_LIST);
                        failedGateways.put("PUBLISHED", failedToPublishEnvironments);
                    }
                } else {
                    List<String> failedToRemoveEnvironments = removeFromGateway(api);
                    if (!failedToRemoveEnvironments.isEmpty()) {
                        Set<String> publishedEnvironments =
                                new HashSet<String>(api.getEnvironments());
                        publishedEnvironments.addAll(failedToRemoveEnvironments);
                        api.setEnvironments(publishedEnvironments);
                        updateApiArtifact(api, true, false);
                        failedGateways.clear();
                        failedGateways.put("UNPUBLISHED", failedToRemoveEnvironments);
                        failedGateways.put("PUBLISHED", Collections.EMPTY_LIST);
                    }
                }
            }

            updateApiArtifact(api, false,false);
            apiMgtDAO.recordAPILifeCycleEvent(api.getId(), currentStatus, status, userId);

            if(api.isDefaultVersion() || api.isPublishedDefaultVersion()){ //published default version need to be changed
                apiMgtDAO.updateDefaultAPIPublishedVersion(api.getId(), currentStatus, status);
            }

        } catch (APIManagementException e) {
        	handleException("Error occured in the status change : " + api.getId().getApiName() , e);
        }
        finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
      
        return failedGateways;
    }

    /**
     * Function returns true if the specified API already exists in the registry
     * @param apiName API Name
     * @param version Version
     * @param providerName Provider
     * @return
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public boolean checkIfAPIExists(String providerName,String apiName,String version) throws APIManagementException {
        APIIdentifier identifier=new APIIdentifier(providerName,apiName,version);
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            Registry registry;
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !this.tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    if (this.tenantDomain != null && !this.tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        registry = ServiceReferenceHolder.getInstance().
                                getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                    } else {
                        registry = this.registry;
                    }
                }
            }
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            boolean result = registry.resourceExists(apiPath);
            return result;
        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return false;
        } catch (UserStoreException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return false;
        }
    }

    public void makeAPIKeysForwardCompatible(API api) throws APIManagementException {
        String provider = api.getId().getProviderName();
        String apiName = api.getId().getApiName();
        Set<String> versions = getAPIVersions(provider, apiName);
        APIVersionComparator comparator = new APIVersionComparator();
        for (String version : versions) {
            API otherApi = getAPI(new APIIdentifier(provider, apiName, version));
            if (comparator.compare(otherApi, api) < 0 && !otherApi.getStatus().equals(APIStatus.RETIRED)) {
                apiMgtDAO.makeKeysForwardCompatible(provider, apiName, version,
                                                    api.getId().getVersion(), api.getContext());
            }
        }
    }

    private List<String> publishToGateway(API api) throws APIManagementException {
        List<String> failedEnvironment;
        APITemplateBuilder builder = null;
        String tenantDomain = null;
        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
        }

        try{
            builder = getAPITemplateBuilder(api);
        }catch(Exception e){
            handleException("Error while publishing to Gateway ", e);
        }


        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        failedEnvironment = gatewayManager.publishToGateway(api, builder, tenantDomain);
        if (log.isDebugEnabled()) {
        	String logMessage = "API Name: " + api.getId().getApiName() + ", API Version "+api.getId().getVersion()+" published to gateway";
        	log.debug(logMessage);
        }
        return failedEnvironment;
    }

    private void validateAndSetTransports(API api) throws APIManagementException {
        String transports = api.getTransports();
        if(transports != null && !("null".equalsIgnoreCase(transports))){
            if (transports.contains(",")) {
                StringTokenizer st = new StringTokenizer(transports, ",");
                while (st.hasMoreTokens()) {
                    checkIfValidTransport(st.nextToken());
                }
            }else{
                checkIfValidTransport(transports);
            }
        }else{
            api.setTransports(Constants.TRANSPORT_HTTP+","+Constants.TRANSPORT_HTTPS);
            return;
        }
    }

    private void checkIfValidTransport(String transport) throws APIManagementException {
        if(!Constants.TRANSPORT_HTTP.equalsIgnoreCase(transport) && !Constants.TRANSPORT_HTTPS.equalsIgnoreCase(transport)){
            handleException("Unsupported Transport [" + transport + "]");
        }
    }

    private List<String> removeFromGateway(API api) {
        String tenantDomain = null;
        List<String> failedEnvironment;
        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
        }

        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        failedEnvironment = gatewayManager.removeFromGateway(api, tenantDomain);
        if(log.isDebugEnabled()){
        	String logMessage = "API Name: " + api.getId().getApiName() + ", API Version "+api.getId().getVersion()+" deleted from gateway";
        	log.debug(logMessage);
        }
        return failedEnvironment;
    }

    public List<String> removeDefaultAPIFromGateway(API api) {
        String tenantDomain = null;
        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
        }

        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        return gatewayManager.removeDefaultAPIFromGateway(api, tenantDomain);

    }

    private boolean isAPIPublished(API api) {
            String tenantDomain = null;
			if (api.getId().getProviderName().contains("AT")) {
				String provider = api.getId().getProviderName().replace("-AT-", "@");
				tenantDomain = MultitenantUtils.getTenantDomain( provider);
			}
            APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
            return gatewayManager.isAPIPublished(api, tenantDomain);
        }

    private APITemplateBuilder getAPITemplateBuilder(API api){
        APITemplateBuilderImpl vtb = new APITemplateBuilderImpl(api);

        if(!api.getStatus().equals(APIStatus.PROTOTYPED)) {
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler", Collections.EMPTY_MAP);

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("id", "A");
            properties.put("policyKey", "gov:" + APIConstants.API_TIER_LOCATION);
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleHandler", properties);

            vtb.addHandler("org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler", Collections.EMPTY_MAP);

            properties = new HashMap<String, String>();
            properties.put("configKey", "gov:" + APIConstants.GA_CONFIGURATION_LOCATION);
            vtb.addHandler("org.wso2.carbon.apimgt.usage.publisher.APIMgtGoogleAnalyticsTrackingHandler", properties);

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String extensionHandlerPosition = config.getFirstProperty(APIConstants.EXTENSION_HANDLER_POSITION);
            if (extensionHandlerPosition != null && "top".equalsIgnoreCase(extensionHandlerPosition)) {
                vtb.addHandlerPriority("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler", Collections.EMPTY_MAP, 0);
            } else {
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler", Collections.EMPTY_MAP);
            }
        }
        return vtb;
    }

    public void updateDefaultAPIInRegistry(APIIdentifier apiIdentifier,boolean value) throws APIManagementException{
        try {

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            String defaultAPIPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    apiIdentifier.getProviderName() +
                    RegistryConstants.PATH_SEPARATOR + apiIdentifier.getApiName() +
                    RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion() +
                    APIConstants.API_RESOURCE_NAME;

            Resource defaultAPISourceArtifact = registry.get(defaultAPIPath);
            GenericArtifact defaultAPIArtifact = artifactManager.getGenericArtifact(
                    defaultAPISourceArtifact.getUUID());
            defaultAPIArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, String.valueOf(value));
            artifactManager.updateGenericArtifact(defaultAPIArtifact);

        } catch (RegistryException e) {
            String msg = "Failed to update default API version : " + apiIdentifier.getVersion() + " of : "
                    + apiIdentifier.getApiName();
            handleException(msg, e);
        }
    }

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param apiObj       The API to be copied
     * @param newVersion The version of the new API
     * @throws org.wso2.carbon.apimgt.api.model.DuplicateAPIException
     *          If the API trying to be created already exists
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If an error occurs while trying to create
     *          the new version of the API
     */
    public boolean createNewAPIVersion(JSONObject apiObj, String newVersion) throws DuplicateAPIException,
                                                                       APIManagementException {
        String providerName = (String) apiObj.get("provider");
        String apiName = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        String dVersion=(String) apiObj.get("defaultVersion");

        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
        API api = new API(apiId);
        api.setAsDefaultVersion(dVersion.equals("default_version"));

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

        String apiSourcePath = APIUtil.getAPIPath(api.getId());

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                            api.getId().getProviderName() +
                            RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                            RegistryConstants.PATH_SEPARATOR + newVersion +
                            APIConstants.API_RESOURCE_NAME;

            if (registry.resourceExists(targetPath)) {
                throw new DuplicateAPIException("API version already exist with version :"
                                                + newVersion);
            }
            Resource apiSourceArtifact = registry.get(apiSourcePath);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());

            //Create new API version
            artifact.setId(UUID.randomUUID().toString());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, newVersion);

            //Check the status of the existing api,if its not in 'CREATED' status set
            //the new api status as "CREATED"
            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            if (!status.equals(APIConstants.CREATED)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, APIConstants.CREATED);
            }

            if(api.isDefaultVersion()){
                artifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "true");
                //Check whether an existing API is set as default version.
                String defaultVersion = getDefaultVersion(api.getId());

                //if so, change its DefaultAPIVersion attribute to false

                if(defaultVersion!=null){
                    APIIdentifier defaultAPIId=new APIIdentifier(api.getId().getProviderName(),api.getId().getApiName(),defaultVersion);
                    updateDefaultAPIInRegistry(defaultAPIId,false);
                }
            }else{
                artifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "false");
            }
            //Check whether the existing api has its own thumbnail resource and if yes,add that image
            //thumb to new API                                       thumbnail path as well.
            String thumbUrl = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                              api.getId().getProviderName() + RegistryConstants.PATH_SEPARATOR +
                              api.getId().getApiName() + RegistryConstants.PATH_SEPARATOR +
                              api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
            if (registry.resourceExists(thumbUrl)) {
                Resource oldImage = registry.get(thumbUrl);
                apiSourceArtifact.getContentStream();
                APIIdentifier newApiId = new APIIdentifier(api.getId().getProviderName(),
                                                           api.getId().getApiName(), newVersion);
                Icon icon = new Icon(oldImage.getContentStream(), oldImage.getMediaType());
                artifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL,
                                      addIcon(APIUtil.getIconPath(newApiId), icon));
            }
            // Here we keep the old context
            String oldContext =  artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT);

            // We need to change the context by setting the new version
            // This is a change that is coming with the context version strategy
            String contextTemplate = artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE);
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT,
                                  contextTemplate.replace(APIConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE,
                                                          newVersion));

            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            registry.addAssociation(APIUtil.getAPIProviderPath(api.getId()), targetPath,
                                    APIConstants.PROVIDER_ASSOCIATION);
            String roles=artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
            String[] rolesSet = new String[0];
            if (roles != null) {
                rolesSet = roles.split(",");
            }
            APIUtil.setResourcePermissions(api.getId().getProviderName(), 
            		artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY), rolesSet, artifactPath);
            //Here we have to set permission specifically to image icon we added
            String iconPath = artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL);
            if (iconPath != null) {
            	iconPath=iconPath.substring(iconPath.lastIndexOf("/apimgt"));
                APIUtil.copyResourcePermissions(api.getId().getProviderName(),thumbUrl,iconPath);
            }
            // Retain the tags
            org.wso2.carbon.registry.core.Tag[] tags = registry.getTags(apiSourcePath);
            if (tags != null) {
                for (org.wso2.carbon.registry.core.Tag tag : tags) {
                    registry.applyTag(targetPath, tag.getTagName());
                }
            }

            // Retain the docs
            List<Documentation> docs = getAllDocumentation(api.getId());
            APIIdentifier newId = new APIIdentifier(api.getId().getProviderName(),
                                                    api.getId().getApiName(), newVersion);
            API newAPI = getAPI(newId,api.getId(), oldContext);

            if(api.isDefaultVersion()){
                newAPI.setAsDefaultVersion(true);
            }else{
                newAPI.setAsDefaultVersion(false);
            }

            for (Documentation doc : docs) {
            	/* If the document is API Definition for swagger */
            	if (doc.getName().equals(APIConstants.API_DEFINITION_DOC_NAME)) {
            		/* Create the JSON Content again for API with new definition */
            		String content = APIUtil.createSwaggerJSONContent(newAPI);
            		addAPIDefinitionContent(newId, doc.getName(), content);
            		setPermissionToAPIDefinition(newAPI, doc);
				} else {
					/* copying the file in registry for new api */
					Documentation.DocumentSourceType sourceType = doc.getSourceType();
					if (sourceType == Documentation.DocumentSourceType.FILE) {
						String absoluteSourceFilePath = doc.getFilePath();
						// extract the prepend
						// ->/registry/resource/_system/governance/ and for
						// tenant
						// /t/my.com/registry/resource/_system/governance/
						int prependIndex =
						                   absoluteSourceFilePath.indexOf(APIConstants.API_LOCATION);
						String prependPath = absoluteSourceFilePath.substring(0, prependIndex);
						// get the file name from absolute file path
						int fileNameIndex =
						                    absoluteSourceFilePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR);
						String fileName = absoluteSourceFilePath.substring(fileNameIndex + 1);
						// create relative file path of old location
						String sourceFilePath = absoluteSourceFilePath.substring(prependIndex);
						// create the relative file path where file should be
						// copied
						String targetFilePath =
						                        APIConstants.API_LOCATION +
						                                RegistryConstants.PATH_SEPARATOR +
						                                newId.getProviderName() +
						                                RegistryConstants.PATH_SEPARATOR +
						                                newId.getApiName() +
						                                RegistryConstants.PATH_SEPARATOR +
						                                newId.getVersion() +
						                                RegistryConstants.PATH_SEPARATOR +
						                                APIConstants.DOC_DIR +
						                                RegistryConstants.PATH_SEPARATOR +
						                                APIConstants.DOCUMENT_FILE_DIR +
						                                RegistryConstants.PATH_SEPARATOR + fileName;
						// copy the file from old location to new location(for
						// new api)
						registry.copy(sourceFilePath, targetFilePath);
						// update the filepath attribute in doc artifact to
						// create new doc artifact for new version of api
						doc.setFilePath(prependPath + targetFilePath);
					}
					createDocumentation(newAPI, doc);
					String content = getDocumentationContent(api.getId(), doc.getName());
					if (content != null) {
						addDocumentationContent(newAPI, doc.getName(), content);
					}
            	}
            }
            
            //copy only if there are swagger 1.2 data. APIs created before APIM 1.7 do not have swagger 1.2
            //data entry in the registry. When copying a migrated API just ignore the swagger 1.2. 
            //getSwagger12Definition() method will handle displaying the swagger 1.1 apis           
            String resourcePath = APIUtil.getSwagger12DefinitionFilePath(api.getId().getApiName(),
            		api.getId().getVersion(), api.getId().getProviderName());            
            if (registry.resourceExists(resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME)) {            	
            	 copySwagger12Resources(api.getId(), newId);            	 
			}
            
            // Make sure to unset the isLatest flag on the old version
            GenericArtifact oldArtifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());
            oldArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "false");
            artifactManager.updateGenericArtifact(oldArtifact);

            int tenantId = -1234;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new APIManagementException("Error in retrieving Tenant Information while adding api :"
                        +api.getId().getApiName(),e);
            }

            apiMgtDAO.addAPI(newAPI,tenantId);

        } catch (RegistryException e) {
            String msg = "Failed to create new version : " + newVersion + " of : "
                         + api.getId().getApiName();
            handleException(msg, e);
            return false;
        } catch (DuplicateAPIException e) {
            handleException("Error occurred while creating a new API version. A duplicate API " +
                            "already exists by the same name.", e);
            return false;
        } catch (Exception e) {
            handleException("Error occurred while creating a new API version- " + newVersion, e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return true;
    }

    /**
     * Set the permission to api definition document
     * @param newAPI
     * @param documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */	
	private void setPermissionToAPIDefinition(API newAPI, Documentation documentation)
	                                                                                  throws APIManagementException {
		
		APIIdentifier identifier=newAPI.getId();
		API api = newAPI;
		String apiDefinitionFilePath =
		                               APIUtil.getAPIDefinitionFilePath(identifier.getApiName(),
		                                                                identifier.getVersion(),
		                                                                identifier.getProviderName());
		try {
			String docVisibility = documentation.getVisibility().name();
			String apiPath = APIUtil.getAPIPath(identifier);
			String[] authorizedRoles = getAuthorizedRoles(apiPath);
			String visibility = api.getVisibility();
			if (docVisibility != null) {
				if (APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)) {
					authorizedRoles = null;
					visibility = APIConstants.DOC_SHARED_VISIBILITY;
				} else if (APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)) {
					authorizedRoles = null;
					visibility = APIConstants.DOC_OWNER_VISIBILITY;
				}
			}
			APIUtil.setResourcePermissions(api.getId().getProviderName(), visibility,
			                               authorizedRoles, apiDefinitionFilePath);
		} catch (UserStoreException e) {
            handleException("Failed to set permission to copied swagger api definition of:"
                    + api.getId().getApiName() + " version :" + api.getId().getVersion(), e);
        }

	}
	
    private void copySwagger12Resources(APIIdentifier apiId, APIIdentifier newAPIId) throws APIManagementException{
    	String resourcePath = APIUtil.getSwagger12DefinitionFilePath(apiId.getApiName(),
                apiId.getVersion(), apiId.getProviderName());
		
		JSONParser parser = new JSONParser();
		JSONObject apiJSON = null;
		try {
			Resource apiDocResource = registry.get(resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME);
			String apiDocContent = new String((byte []) apiDocResource.getContent());
			apiJSON = (JSONObject) parser.parse(apiDocContent);
			updateSwagger12Definition(newAPIId, APIConstants.API_DOC_1_2_RESOURCE_NAME, apiJSON.toJSONString());
			
			JSONArray pathConfigs = (JSONArray) apiJSON.get("apis");
			
			for (int k = 0; k < pathConfigs.size(); k++) {
				JSONObject pathConfig = (JSONObject) pathConfigs.get(k);
				String pathName = (String) pathConfig.get("path");
				pathName = pathName.startsWith("/") ? pathName : ("/" + pathName);
				
				Resource pathResource = registry.get(resourcePath + pathName);
				String pathContent = new String((byte []) pathResource.getContent());
				JSONObject pathJSON = (JSONObject) parser.parse(pathContent);
				updateSwagger12Definition(newAPIId, pathName, pathJSON.toJSONString());
			}
		} catch (RegistryException e) {
			handleException("Error while retrieving Swagger Definition for " + apiId.getApiName() + "-" + 
											apiId.getVersion(), e);
		} catch (ParseException e) {
			handleException("Error while parsing Swagger Definition for " + apiId.getApiName() + "-" + 
											apiId.getVersion() + " in " + resourcePath, e);
		} 
    }

    /**
     * Removes a given documentation
     *
     * @param apiIdObj   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to remove documentation
     */
    public boolean removeDocumentation(JSONObject apiIdObj, String docName, String docType)
            throws APIManagementException {
        boolean success = false;
        String providerName = (String) apiIdObj.get("provider");
        String apiName = (String) apiIdObj.get("name");
        String version = (String) apiIdObj.get("version");
        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            String docPath = APIUtil.getAPIDocPath(apiId) + docName;


            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);

            if (docFilePath != null) {
                File tempFile = new File(docFilePath);
                String fileName = tempFile.getName();
                docFilePath = APIUtil.getDocumentationFilePath(apiId, fileName);
                if (registry.resourceExists(docFilePath)) {
                    registry.delete(docFilePath);
                }
            }

            Association[] associations = registry.getAssociations(docPath,
                                                                  APIConstants.DOCUMENTATION_KEY);
            for (Association association : associations) {
                registry.delete(association.getDestinationPath());
            }
            success = true;
        } catch (RegistryException e) {
            handleException("Failed to delete documentation", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    /**
     * Adds Documentation to an API
     *
     * @param apiObj API json object
     * @param docObj Documentation json object
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add documentation
     */
    public void addDocumentation(JSONObject apiObj, JSONObject docObj)
            throws APIManagementException {

        String providerName = (String) apiObj.get("provider");
        String apiName = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        String docName = (String) docObj.get("docName");
        String docType = (String) docObj.get("docType");
        String summary = (String) docObj.get("summary");
        String sourceType = (String) docObj.get("sourceType");
        String visibility = (String) docObj.get("visibility");
        //------------ FileHostObject fileHostObject = null;
        String sourceURL = null;

        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
        Documentation doc = new Documentation(null, docName);  //---------replace null with getDocType(docType)
        if (doc.getType() == DocumentationType.OTHER) {
            String docOtherTypeName = (String) docObj.get("docOtherTypeName");
            doc.setOtherTypeName(docOtherTypeName);
        }

        if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.URL.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.URL);
            sourceURL = (String) docObj.get("docUrl");
        } else if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.FILE.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.FILE);
            //---------   fileHostObject = (FileHostObject) args[8];
        } else {
            doc.setSourceType(Documentation.DocumentSourceType.INLINE);
        }

        doc.setSummary(summary);
        doc.setSourceUrl(sourceURL);
        if (visibility == null) {
            visibility = APIConstants.DOC_API_BASED_VISIBILITY;
        }
        if (visibility.equalsIgnoreCase(Documentation.DocumentVisibility.API_LEVEL.toString())) {
            doc.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
        } else if (visibility.equalsIgnoreCase(Documentation.DocumentVisibility.PRIVATE.toString())) {
            doc.setVisibility(Documentation.DocumentVisibility.PRIVATE);
        } else {
            doc.setVisibility(Documentation.DocumentVisibility.OWNER_ONLY);
        }
        try {

       /*-----     if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {
                String contentType = (String) args[10];
                Icon icon = new Icon(fileHostObject.getInputStream(), contentType);
                String fname = fileHostObject.getName();
                int index = fname.lastIndexOf("//");
                fname = fname.substring(index + 1);
                String filePath = APIUtil.getDocumentationFilePath(apiId, fname);
                API api = getAPI(apiId);
                String apiPath = APIUtil.getAPIPath(apiId);
                String visibleRolesList = api.getVisibleRoles();
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                APIUtil.setResourcePermissions(api.getId().getProviderName(),
                                               api.getVisibility(), visibleRoles, filePath);
                doc.setFilePath(addIcon(filePath, icon));
            } else if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.FILE.toString())) {
                throw new APIManagementException("Empty File Attachment.");
            } -----*/
        } catch (Exception e) {
            handleException("Error while creating an attachment for Document- " + docName + "-" + version + ". " +
                            e.getMessage(), e);

        }
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        API api = getAPI(apiId);
        createDocumentation(api, doc);


    }

    /**
     * This method used to save the documentation content
     *
     * @param apiObj,     API json object
     * @param docName,    name of the inline documentation
     * @param text, content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(JSONObject apiObj, String docName, String text)
            throws APIManagementException {
        String providerName = (String) apiObj.get("provider");
        String apiName = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        if (text != null) {
            text = text.replaceAll("\n", "");
        }
        APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName,
                                                version);
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
        boolean isTenantFlowStarted = false;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isTenantFlowStarted = true;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }
        try {
            if (docName.equals(APIConstants.API_DEFINITION_DOC_NAME)) {
                addAPIDefinitionContent(apiId, docName, text);
            } else {
                API api = getAPI(apiId);
                addDocumentationContent(api, docName, text);
            }


        } catch (APIManagementException e) {
            handleException("Error occurred while adding the content of the documentation- " + docName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void addDocumentationContent(API api, String docName, String text)
            throws APIManagementException {
        APIIdentifier identifier = api.getId();
        String documentationPath = APIUtil.getAPIDocPath(identifier) + docName;
        String contentPath = APIUtil.getAPIDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
                             RegistryConstants.PATH_SEPARATOR + docName;
        try {
            Resource docResource = registry.get(documentationPath);
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                    docResource.getUUID());
            Documentation doc = APIUtil.getDocumentation(docArtifact);

            Resource docContent = null;

            if (!registry.resourceExists(contentPath)) {
                docContent = registry.newResource();
            } else {
                docContent = registry.get(contentPath);
            }

            /* This is a temporary fix for doc content replace issue. We need to add
             * separate methods to add inline content resource in document update */
            if (!APIConstants.NO_CONTENT_UPDATE.equals(text)) {
                docContent.setContent(text);
            }
            docContent.setMediaType(APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE);
            registry.put(contentPath, docContent);
            registry.addAssociation(documentationPath, contentPath,
                                    APIConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
            String apiPath = APIUtil.getAPIPath(identifier);
            String[] authorizedRoles = getAuthorizedRoles(apiPath);
            String docVisibility = doc.getVisibility().name();
            String visibility = api.getVisibility();
            if (docVisibility != null) {
                if (APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                    authorizedRoles = null;
                    visibility = APIConstants.DOC_SHARED_VISIBILITY;
                } else if (APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                    authorizedRoles = null;
                    visibility = APIConstants.DOC_OWNER_VISIBILITY;
                }
            }

            APIUtil.setResourcePermissions(api.getId().getProviderName(), visibility
                    , authorizedRoles, contentPath);
        } catch (RegistryException e) {
            String msg = "Failed to add the documentation content of : "
                         + docName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to add the documentation content of : "
                         + docName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        }
    }

    /**
     * This method used to update the API definition content - Swagger
     *
     * @param identifier,        API identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addAPIDefinitionContent(APIIdentifier identifier, String documentationName, String text) 
    					throws APIManagementException {
    	String contentPath = APIUtil.getAPIDefinitionFilePath(identifier.getApiName(), identifier.getVersion(),identifier.getProviderName());
    	
    	try {
            Resource docContent = registry.newResource();
            docContent.setContent(text);
            docContent.setMediaType("text/plain");
            docContent.setProperty(APIConstants.VISIBILITY,APIConstants.DOC_API_BASED_VISIBILITY);
            registry.put(contentPath, docContent);

            //Commented below section as to set same permissions set we are giving to /_system/governance/apimgt/applicationdata/api-docs/api_name-version-provider location to apply with json content
           /* String apiPath = APIUtil.getAPIPath(identifier);
            API api = getAPI(apiPath);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
    		APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, contentPath);  */
    	} catch (RegistryException e) {
            String msg = "Failed to add the API Definition content of : "
                         + documentationName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        } 
    }

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {

        String apiPath = APIUtil.getAPIPath(apiId);
        API api=getAPI(apiPath);
        if (documentation.getName().equals(APIConstants.API_DEFINITION_DOC_NAME)) {
        try{
        String swaggerDocPath = APIUtil.getAPIDefinitionFilePath(apiId.getApiName(), apiId.getVersion(), apiId.getProviderName());
        String[] authorizedRoles = getAuthorizedRoles(swaggerDocPath);
        String docVisibility=documentation.getVisibility().name();
        Resource resource = registry.get(swaggerDocPath);
        resource.setProperty(APIConstants.VISIBILITY,docVisibility);
        registry.put(swaggerDocPath,resource);

        String visibility= api.getVisibility();
        if(docVisibility!=null){
        if(APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)){
        authorizedRoles=null;
        visibility=APIConstants.DOC_SHARED_VISIBILITY;
        } else if(APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)){
        authorizedRoles = null;
        visibility=APIConstants.DOC_OWNER_VISIBILITY;
        }
        }
        APIUtil.setResourcePermissions(api.getId().getProviderName(),
                    visibility, authorizedRoles, swaggerDocPath);
        } catch (Exception e) {
            handleException("Failed to update swagger documentation permission", e);
        }
        }else{
        String docPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                         apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                         RegistryConstants.PATH_SEPARATOR + apiId.getVersion() + RegistryConstants.PATH_SEPARATOR +
                         APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR + documentation.getName();

        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String docVisibility=documentation.getVisibility().name();
            String[] authorizedRoles = new String[0];
            String visibleRolesList = api.getVisibleRoles();
            if (visibleRolesList != null) {
                authorizedRoles = visibleRolesList.split(",");
            }
            String visibility= api.getVisibility();
            if(docVisibility!=null){
                if(APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)){
                    authorizedRoles=null;
                    visibility=APIConstants.DOC_SHARED_VISIBILITY;
                } else if(APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)){
                    authorizedRoles = null;
                    visibility=APIConstants.DOC_OWNER_VISIBILITY;
                }
            }
            if (!documentation.getName().equals(APIConstants.API_DEFINITION_DOC_NAME)) {
            GenericArtifact updateApiArtifact = APIUtil.createDocArtifactContent(artifact, apiId, documentation);
            artifactManager.updateGenericArtifact(updateApiArtifact);
            clearResourcePermissions(docPath, apiId);
            }

            APIUtil.setResourcePermissions(api.getId().getProviderName(),
                    visibility, authorizedRoles, artifact.getPath());
            
            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if(docFilePath != null && !docFilePath.equals("")) {
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                APIUtil.setResourcePermissions(api.getId().getProviderName(),
                        visibility,authorizedRoles, filePath);
            }

        } catch (RegistryException e) {
            handleException("Failed to update documentation", e);
        }
        }
    }

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws APIManagementException {

        String oldVersion = APIUtil.getAPIDocPath(apiId);
        String newVersion = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                            apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                            RegistryConstants.PATH_SEPARATOR + toVersion + RegistryConstants.PATH_SEPARATOR +
                            APIConstants.DOC_DIR;

        try {
            Resource resource = registry.get(oldVersion);
            if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                String[] docsPaths = ((org.wso2.carbon.registry.core.Collection) resource).getChildren();
                for (String docPath : docsPaths) {
                    registry.copy(docPath, newVersion);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to copy docs to new version : " + newVersion, e);
        }
    }

    /**
     * Create an Api
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to create API
     */
    private String createAPI(API api) throws APIManagementException {
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                            APIConstants.API_KEY);
        String id = null;
        //Validate Transports
        validateAndSetTransports(api);
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = APIUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            id=artifact.getId();
            String providerPath = APIUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null && tagSet.size() > 0) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }           
            if (APIUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
                String path = APIUtil.createWSDL(registry, api);
                if (path != null) {
                    registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                    artifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl()); //reset the wsdl path to permlink
                    artifactManager.updateGenericArtifact(artifact); //update the  artifact
                }
            }

            if (api.getUrl() != null && !"".equals(api.getUrl())){
                String path = APIUtil.createEndpoint(api.getUrl(), registry);
                if (path != null) {
                    registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                }
            }
            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, artifactPath);
            registry.commitTransaction();

            /* Generate API Definition for Swagger Console if any URI templates are available */
            if (api.getUriTemplates().size() > 0) {
            	createUpdateAPIDefinition(api);
            }            
            if(log.isDebugEnabled()){
            	String logMessage = "API Name: " + api.getId().getApiName() + ", API Version "+api.getId().getVersion()+" created";
            	log.debug(logMessage);
            }
        } catch (Exception e) {
        	 try {
                 registry.rollbackTransaction();
             } catch (RegistryException re) {
                 handleException("Error while rolling back the transaction for API: " +
                                 api.getId().getApiName(), re);
             }
             handleException("Error while performing registry transaction operation", e);
        }
        return id;
    }

    /**
     * This function is to set resource permissions based on its visibility
     *
     * @param artifactPath API resource path
     * @throws org.wso2.carbon.apimgt.api.APIManagementException Throwing exception
     */
    private void clearResourcePermissions(String artifactPath, APIIdentifier apiId)
            throws APIManagementException {
        try {
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                       RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                artifactPath);
            String tenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            if (!tenantDomain.equals(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(((UserRegistry) registry).getTenantId()).
                        getAuthorizationManager();
                authManager.clearResourceAuthorizations(resourcePath);
            } else {
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(ServiceReferenceHolder.getUserRealm());
                authorizationManager.clearResourceAuthorizations(resourcePath);
            }
        } catch (UserStoreException e) {
            handleException("Error while adding role permissions to API", e);
        }
    }
    /**
     * Create a documentation
     *
     * @param api         API
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to add documentation
     */
    private void createDocumentation(API api, Documentation documentation)
            throws APIManagementException {
        try {
        	APIIdentifier apiId = api.getId();
        	GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
        			APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            artifactManager.addGenericArtifact(
                    APIUtil.createDocArtifactContent(artifact, apiId, documentation));
            String apiPath = APIUtil.getAPIPath(apiId);
            //Adding association from api to documentation . (API -----> doc)
            registry.addAssociation(apiPath, artifact.getPath(),
                                    APIConstants.DOCUMENTATION_ASSOCIATION);
            String docVisibility=documentation.getVisibility().name();
            String[] authorizedRoles=getAuthorizedRoles(apiPath);
            String visibility=api.getVisibility();
            if(docVisibility!=null){
            if(APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)){
            authorizedRoles=null;
            visibility=APIConstants.DOC_SHARED_VISIBILITY;
            } else if(APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)){
            authorizedRoles=null;
            visibility=APIConstants.DOC_OWNER_VISIBILITY;
            }
            }
            APIUtil.setResourcePermissions(api.getId().getProviderName(),
                   visibility, authorizedRoles, artifact.getPath());
            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if(docFilePath != null && !docFilePath.equals("")){
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                APIUtil.setResourcePermissions(api.getId().getProviderName(),
                        visibility,authorizedRoles, filePath);
                registry.addAssociation(artifact.getPath(), filePath,
                        APIConstants.DOCUMENTATION_FILE_ASSOCIATION);
            }
        } catch (RegistryException e) {
            handleException("Failed to add documentation", e);
        } catch (UserStoreException e) {
            handleException("Failed to add documentation", e);
        }
    }
    
    
   
    private String[] getAuthorizedRoles(String artifactPath) throws UserStoreException {
        String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                            APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                   RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                            artifactPath);
        if (!tenantDomain.equals(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantManager().getTenantId(tenantDomain);
        AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                                           getTenantUserRealm(tenantId).getAuthorizationManager();
        return authManager.getAllowedRolesForResource(resourcePath,ActionConstants.GET);
        }else{
        RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                (ServiceReferenceHolder.getUserRealm());
        return authorizationManager.getAllowedRolesForResource(resourcePath,ActionConstants.GET);
        }
    }

    /**
     * Returns the details of all the life-cycle changes done per api
     *
     * @param apiId API Identifier
     * @return List of lifecycle events per given api
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get Lifecycle Events
     */
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws APIManagementException {
        return apiMgtDAO.getLifeCycleEvents(apiId);
    }

    /**
     * Update the subscription status
     *
     * @param apiIdObj API Identifier
     * @param subStatus Subscription Status
     * @param appId Application Id              *
     * @return int value with subscription id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public boolean updateSubscription(JSONObject apiIdObj, String subStatus, int appId)
            throws APIManagementException {
        boolean success = false;
        String providerName = (String) apiIdObj.get("provider");
        String apiName = (String) apiIdObj.get("name");
        String version = (String) apiIdObj.get("version");
        try {
            APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
            apiMgtDAO.updateSubscription(apiId, subStatus, appId);
            success = true;
        } catch (APIManagementException e) {
            handleException("Error while updating subscription status", e);
        }
        return success;
    }

    /* Remove an API
     *
     * @param apiId API Identifier json object              *
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to remove the API
     **/

    public boolean deleteAPI(JSONObject id) throws APIManagementException {
        String providerName = (String) id.get("provider");
        providerName = APIUtil.replaceEmailDomain(providerName);
        String apiName = (String) id.get("name");
        String version = (String) id.get("version");
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                      providerName + RegistryConstants.PATH_SEPARATOR +
                      apiName + RegistryConstants.PATH_SEPARATOR + version;
        APIIdentifier identifier = new APIIdentifier(providerName, apiName, version);
        String apiArtifactPath = APIUtil.getAPIPath(identifier);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            long subsCount = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
            if (subsCount > 0) {
                handleException("Cannot remove the API. Active Subscriptions Exist", null);
            }

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(path);
            String artifactId = apiResource.getUUID();

            Resource apiArtifactResource = registry.get(apiArtifactPath);
            String apiArtifactResourceId = apiArtifactResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + path);
            }

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactResourceId);
            String inSequence = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE);
            String outSequence = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE);

            //Delete the dependencies associated  with the api artifact
            GovernanceArtifact[] dependenciesArray = apiArtifact.getDependencies();

            if (dependenciesArray.length > 0) {
                for (int i = 0; i < dependenciesArray.length; i++) {
                    registry.delete(dependenciesArray[i].getPath());
                }
            }
            String isDefaultVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION);
            artifactManager.removeGenericArtifact(artifactId);

            String thumbPath = APIUtil.getIconPath(identifier);
            if (registry.resourceExists(thumbPath)) {
                registry.delete(thumbPath);
            }
            
            /*Remove API Definition Resource - swagger*/
            String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                           identifier.getApiName() + "-" + identifier.getVersion() + "-" + identifier.getProviderName();
            if (registry.resourceExists(apiDefinitionFilePath)) {
                registry.delete(apiDefinitionFilePath);
            }

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
            String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

            API api = new API(identifier);
            api.setAsDefaultVersion(Boolean.valueOf(isDefaultVersion));
            api.setAsPublishedDefaultVersion(api.getId().getVersion().equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));

            // gatewayType check is required when API Management is deployed on other servers to avoid synapse
            if (gatewayExists && gatewayType.equals("Synapse")) {
                //if (isAPIPublished(api)) {
                api.setInSequence(inSequence); //need to remove the custom sequences
                api.setOutSequence(outSequence);
                removeFromGateway(api);
                if (api.isDefaultVersion()) {
                    removeDefaultAPIFromGateway(api);
                }
                //}
            } else {
                log.debug("Gateway is not existed for the current API Provider");
            }
            //Check if there are already published external APIStores.If yes,removing APIs from them.
            Set<APIStore> apiStoreSet = getPublishedExternalAPIStores(api.getId());
            if (apiStoreSet != null && apiStoreSet.size() != 0) {
                for (APIStore store : apiStoreSet) {
                    new WSO2APIPublisher().deleteFromStore(api.getId(), APIUtil.getExternalAPIStore(store.getName(), tenantId));
                }
            }
            apiMgtDAO.deleteAPI(identifier);
            //if manageAPIs == true
 //TODO this is commented out temporaly.
            /*if (APIUtil.isAPIManagementEnabled()) {
                Cache contextCache = APIUtil.getAPIContextCache();
                contextCache.remove(api.getContext());
                contextCache.put(api.getContext(), false);
            } */
            /*remove empty directories*/
            String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                       identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                                       identifier.getApiName();
            if (registry.resourceExists(apiCollectionPath)) {
                Resource apiCollection = registry.get(apiCollectionPath);
                CollectionImpl collection = (CollectionImpl) apiCollection;
                //if there is no other versions of apis delete the directory of the api
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more versions of the API found, removing API collection from registry");
                    }
                    registry.delete(apiCollectionPath);
                }
            }

            String apiProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                     identifier.getProviderName();
            if (registry.resourceExists(apiProviderPath)) {
                Resource providerCollection = registry.get(apiProviderPath);
                CollectionImpl collection = (CollectionImpl) providerCollection;
                //if there is no api for given provider delete the provider directory
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more APIs from the provider " + identifier.getProviderName() + " found. " +
                                  "Removing provider collection from registry");
                    }
                    registry.delete(apiProviderPath);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to remove the API from : " + path, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        //Indicate successful deletion
        return true;
    }
  
    public Map<Documentation, API> searchAPIsByDoc(String searchTerm, String searchType) throws APIManagementException {
    	return APIUtil.searchAPIsByDoc(registry, tenantId, username, searchTerm, searchType);
    }
    
    /**
     * Search APIs based on given search term
     * @param searchTerm
     * @param searchType
     * @param providerId
     * 
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    
	public List<API> searchAPIs(String searchTerm, String searchType, String providerId)
	                                                                                    throws APIManagementException {
		List<API> foundApiList = new ArrayList<API>();
		String regex = "(?i)[\\w.|-]*" + searchTerm.trim() + "[\\w.|-]*";
		Pattern pattern;
		Matcher matcher = null;
		String apiConstant = null;
		try {
			if (providerId != null) {
                List<API> apiList = getAPIsByProvider(providerId);
				if (apiList == null || apiList.size() == 0) {
					return apiList;
				}
				pattern = Pattern.compile(regex);
				for (API api : apiList) {
					if (searchType.equalsIgnoreCase("Name")) {
						apiConstant = api.getId().getApiName();
					} else if (searchType.equalsIgnoreCase("Provider")) {
						apiConstant = api.getId().getProviderName();
					} else if (searchType.equalsIgnoreCase("Version")) {
						apiConstant = api.getId().getVersion();
					} else if (searchType.equalsIgnoreCase("Context")) {
						apiConstant = api.getContext();
					} else if (searchType.equalsIgnoreCase("Status")) {
						apiConstant = api.getStatus().getStatus();
					} else if (searchType.equalsIgnoreCase("Description")) {
						apiConstant = api.getDescription();
					}
					if (apiConstant != null) {
						matcher = pattern.matcher(apiConstant);
						if (matcher != null && matcher.find()) {
                            foundApiList.add(api);
						}
					}
					if (searchType.equalsIgnoreCase("Subcontext")) {
						Set<URITemplate> urls = api.getUriTemplates();
						if (urls.size() > 0) {
							for (URITemplate url : urls) {
								matcher = pattern.matcher(url.getUriTemplate());
								if (matcher != null && matcher.find()) {
                                    foundApiList.add(api);
									break;
								}
							}
						}
					}
				}
			} else {
                foundApiList = searchAPIs(searchTerm, searchType);
			}
		} catch (APIManagementException e) {
			handleException("Failed to search APIs with type", e);
		}
		Collections.sort(foundApiList, new APINameComparator());
		return foundApiList;
	}

	/**
	 * Search APIs 
	 * @param searchTerm
	 * @param searchType
	 * @return
	 * @throws org.wso2.carbon.apimgt.api.APIManagementException
	 */
	 
	private List<API> searchAPIs(String searchTerm, String searchType) throws APIManagementException {
		List<API> apiList = new ArrayList<API>();
//		final String searchValue = searchTerm.trim();
		
		Pattern pattern;
		Matcher matcher = null;
		String searchCriteria = APIConstants.API_OVERVIEW_NAME;
		boolean isTenantFlowStarted = false;
		String userName = this.username;
		try {
			if (tenantDomain != null &&
			    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				isTenantFlowStarted = true;
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
				                                                                      true);
			}
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
			GenericArtifactManager artifactManager =
			                                         APIUtil.getArtifactManager(registry,
			                                                                    APIConstants.API_KEY);
			if (artifactManager != null) {
				if (searchType.equalsIgnoreCase("Name")) {
					searchCriteria = APIConstants.API_OVERVIEW_NAME;
				} else if (searchType.equalsIgnoreCase("Version")) {
					searchCriteria = APIConstants.API_OVERVIEW_VERSION;
				} else if (searchType.equalsIgnoreCase("Context")) {
					searchCriteria = APIConstants.API_OVERVIEW_CONTEXT;
				} else if (searchType.equalsIgnoreCase("Description")) {
					searchCriteria = APIConstants.API_OVERVIEW_DESCRIPTION;
				} else if (searchType.equalsIgnoreCase("Provider")) {
					searchCriteria = APIConstants.API_OVERVIEW_PROVIDER;
					searchTerm = searchTerm.replaceAll("@", "-AT-");
				} else if (searchType.equalsIgnoreCase("Status")) {
					searchCriteria = APIConstants.API_OVERVIEW_STATUS;
				} 
				
//				Map<String, List<String>> listMap = new HashMap<String, List<String>>();
//				listMap.put(searchCriteria, new ArrayList<String>() {
//					{
//						add(searchValue);
//					}
//				});
//				GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
//				if (genericArtifacts == null || genericArtifacts.length == 0) {
//					return apiList;
//				}
//				for (GenericArtifact artifact : genericArtifacts) {
//					apiList.add(APIUtil.getAPI(artifact, registry));
//				}
				String regex = "(?i)[\\w.|-]*" + searchTerm.trim() + "[\\w.|-]*";
				pattern = Pattern.compile(regex);
				
				if (searchType.equalsIgnoreCase("Subcontext")) {
					
					List<API> allAPIs = getAllAPIs();
					for (API api : allAPIs) {
						Set<URITemplate> urls = api.getUriTemplates();
						if (urls.size() > 0) {
							for (URITemplate url : urls) {
								matcher = pattern.matcher(url.getUriTemplate());
								if (matcher != null && matcher.find()) {
									apiList.add(api);
									break;
								}
							}
						}
					}					
					
				} else {					
					GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
					if (genericArtifacts == null || genericArtifacts.length == 0) {
						return apiList;
					}
					
					for (GenericArtifact artifact : genericArtifacts) {
						String value = artifact.getAttribute(searchCriteria);
						
						if (value != null) {
							matcher = pattern.matcher(value);
							if (matcher != null && matcher.find()) {
								apiList.add(APIUtil.getAPI(artifact, registry));
							}
						}				
				    }	
				} 

			}
		} catch (RegistryException e) {
			handleException("Failed to search APIs with type", e);
		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
		return apiList;
	}

    /**
     * Update the Tier Permissions
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles          
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public void updateTierPermissions(String tierName, String permissionType, String roles) throws APIManagementException {
        apiMgtDAO.updateTierPermissions(tierName, permissionType, roles, tenantId);
    }

	@Override
	public Set<TierPermissionDTO> getTierPermissions() throws APIManagementException {
		Set<TierPermissionDTO> tierPermissions = apiMgtDAO.getTierPermissions(tenantId);
		return tierPermissions;
	}

    /**
     * When enabled publishing to external APIStores support,publish the API to external APIStores
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    @Override
    public void publishToExternalAPIStores(API api, Set<APIStore> apiStoreSet)
            throws APIManagementException {

        Set<APIStore> publishedStores = new HashSet<APIStore>();
        if (apiStoreSet.size() > 0) {
            for (APIStore store : apiStoreSet) {
                org.wso2.carbon.apimgt.api.model.APIPublisher publisher = store.getPublisher();
                boolean published=publisher.publishToStore(api, store);//First trying to publish the API to external APIStore

                if (published) { //If published,then save to database.
                    publishedStores.add(store);
                }
            }
            if (publishedStores.size() != 0) {
                addExternalAPIStoresDetails(api.getId(), publishedStores);
            }
        }

    }
    /**
     * Update the API to external APIStores and database
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    @Override
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet) throws APIManagementException {
        boolean updated=false;
        Set<APIStore> publishedStores=getPublishedExternalAPIStores(api.getId());
        Set<APIStore> notPublishedAPIStores = new HashSet<APIStore>();
        Set<APIStore> modifiedPublishedApiStores = new HashSet<APIStore>();
        Set<APIStore> updateApiStores = new HashSet<APIStore>();
        Set<APIStore> removedApiStores = new HashSet<APIStore>();
        if(publishedStores != null){
            removedApiStores.addAll(publishedStores);
            removedApiStores.removeAll(apiStoreSet);
        }
        for (APIStore apiStore: apiStoreSet) {
            boolean publishedToStore=false;
            for (APIStore store : publishedStores) {  //If selected external store in edit page is already saved in db
            	if (store.equals(apiStore)) { //Check if there's a modification happened in config file external store definition
                    if (!isAPIAvailableInExternalAPIStore(api, apiStore)) {
                    // API is not available
            	    continue;
                    }
                    if (!store.getEndpoint().equals(apiStore.getEndpoint()) || !store.getType().equals((apiStore.getType()))||!store.getDisplayName().equals(apiStore.getDisplayName())) {
                        //Include the store definition to update the db stored APIStore set
                    	modifiedPublishedApiStores.add(APIUtil.getExternalAPIStore(store.getName(), tenantId));
                    }
                    publishedToStore=true; //Already the API has published to external APIStore

                    //In this case,the API is already added to external APIStore,thus we don't need to publish it again.
                    //We need to update the API in external Store.
                    //Include to update API in external APIStore
                    updateApiStores.add(APIUtil.getExternalAPIStore(store.getName(), tenantId));


                }

            }
            if (!publishedToStore) {  //If the API has not yet published to selected external APIStore
                notPublishedAPIStores.add(APIUtil.getExternalAPIStore(apiStore.getName(), tenantId));
            }

        }
        //Publish API to external APIStore which are not yet published
        try {
            publishToExternalAPIStores(api, notPublishedAPIStores);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        //Update the APIs which are already exist in the external APIStore
        updateAPIInExternalAPIStores(api,updateApiStores);
        updateExternalAPIStoresDetails(api.getId(),modifiedPublishedApiStores); //Update database saved published APIStore details,if there are any
        //modifications in api-manager.xml

        deleteFromExternalAPIStores(api, removedApiStores);
        updated=true;
        return updated;
    }

    private void deleteFromExternalAPIStores(API api, Set<APIStore> removedApiStores)  throws APIManagementException {
        Set<APIStore> removalCompletedStores = new HashSet<APIStore>();
        if (removedApiStores.size() > 0) {
            for (APIStore store : removedApiStores) {

                org.wso2.carbon.apimgt.api.model.APIPublisher publisher = store.getPublisher();
                boolean deleted=publisher.deleteFromStore(api.getId(), APIUtil.getExternalAPIStore(store.getName(), tenantId));
                if (deleted) {
                    //If the attempt is successful, database will be changed deleting the External store mappings.
                    removalCompletedStores.add(store);
                }

            }
            if (removalCompletedStores.size() != 0) {
                removeExternalAPIStoreDetails(api.getId(), removalCompletedStores);
            }
        }
    }

    private void removeExternalAPIStoreDetails(APIIdentifier id, Set<APIStore> removalCompletedStores)
            throws APIManagementException {
        apiMgtDAO.deleteExternalAPIStoresDetails(id, removalCompletedStores);
    }

    private boolean isAPIAvailableInExternalAPIStore(API api, APIStore store) throws APIManagementException {
        org.wso2.carbon.apimgt.api.model.APIPublisher publisher = store.getPublisher();
        return publisher.isAPIAvailable(api, store);

    }


    /**
     * When enabled publishing to external APIStores support,updating the API existing in external APIStores
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */

    private void updateAPIInExternalAPIStores(API api, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        if (apiStoreSet != null && apiStoreSet.size() > 0) {
            for (APIStore store : apiStoreSet) {
                org.wso2.carbon.apimgt.api.model.APIPublisher publisher = store.getPublisher();
                boolean published=publisher.updateToStore(api, store);
            }
        }


    }
    /**
     * When enabled publishing to external APIStores support,update external apistores data in db
     * @param apiId The API Identifier which need to update in db
     * @param apiStoreSet The APIStores set which need to update in db
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */

    private void updateExternalAPIStoresDetails(APIIdentifier apiId, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        apiMgtDAO.updateExternalAPIStoresDetails(apiId, apiStoreSet);


    }


    private boolean addExternalAPIStoresDetails(APIIdentifier apiId,Set<APIStore> apiStoreSet) throws APIManagementException {
        return apiMgtDAO.addExternalAPIStoresDetails(apiId,apiStoreSet);
    }
    /**
     * When enabled publishing to external APIStores support,get only the published external apistore details which are
     * stored in db
     * @param apiId The API Identifier which need to update in db
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    @Override
    public Set<APIStore> getPublishedExternalAPIStores(APIIdentifier apiId)
            throws APIManagementException {
        if (APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            return apiMgtDAO.getExternalAPIStoresDetails(apiId);

        } else {
            return null;
        }
    }

	/**
	 * Get stored custom inSequences from governanceSystem registry
	 * 
	 * @throws org.wso2.carbon.apimgt.api.APIManagementException
	 */

	public JSONArray getCustomInSequences() throws APIManagementException {

		JSONArray sequenceArr = new JSONArray();
		try {
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
	            org.wso2.carbon.registry.api.Collection inSeqCollection =
	                                                                      (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION);
	            if (inSeqCollection != null) {
	             //   SequenceMediatorFactory factory = new SequenceMediatorFactory();
	                String[] inSeqChildPaths = inSeqCollection.getChildren();
	                for (int i = 0; i < inSeqChildPaths.length; i++) {
		                Resource inSequence = registry.get(inSeqChildPaths[i]);
		                OMElement seqElment = APIUtil.buildOMElement(inSequence.getContentStream());
		                sequenceArr.add(seqElment.getAttributeValue(new QName("name")));
	                }
                }
            }

		} catch (Exception e) {
			handleException("Issue is in getting custom InSequences from the Registry", e);
		}
		return sequenceArr;
	}

	/**
	 * Get stored custom outSequences from governanceSystem registry
	 * 
	 * @throws org.wso2.carbon.apimgt.api.APIManagementException
	 */

    public JSONArray getCustomOutSequences() throws APIManagementException {

        JSONArray sequenceArr = new JSONArray();
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)) {
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (int i = 0; i < outSeqChildPaths.length; i++) {
                        Resource outSequence = registry.get(outSeqChildPaths[i]);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());

                        sequenceArr.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

        } catch (Exception e) {
            handleException("Issue is in getting custom OutSequences from the Registry", e);
        }
        return sequenceArr;
    }

    /**
     * Get stored custom fault sequences from governanceSystem registry
     *
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */

    public JSONArray getCustomFaultSequences() throws APIManagementException {
        JSONArray sequenceArr = new JSONArray();
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION)) {
                org.wso2.carbon.registry.api.Collection faultSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION);
                if (faultSeqCollection != null) {
                    String[] faultSeqChildPaths = faultSeqCollection.getChildren();
                    for (String faultSeqChildPath : faultSeqChildPaths) {
                        Resource outSequence = registry.get(faultSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceArr.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

        } catch (Exception e) {
            handleException("Issue is in getting custom Fault Sequences from the Registry", e);
        }
        return sequenceArr;
    }

    @Override
	public boolean isSynapseGateway() throws APIManagementException {
		APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
		String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
        if (!gatewayType.equalsIgnoreCase(APIConstants.API_GATEWAY_TYPE_SYNAPSE)) {
        	return false;
        }
		return true;
	}
	
	@Override
	public void updateSwagger12Definition(APIIdentifier apiId, String fileName, 
									String jsonText) throws APIManagementException {
		try{
    		String resourcePath = APIUtil.getSwagger12DefinitionFilePath(apiId.getApiName(), 
    				apiId.getVersion(), apiId.getProviderName());
    		
    		resourcePath = resourcePath + fileName;
    		
    		Resource resource = registry.newResource();
    		    		
    		resource.setContent(jsonText);
    		resource.setMediaType("application/json");
    		registry.put(resourcePath, resource);
    		
    		/*Set permissions to anonymous role */
    		APIUtil.setResourcePermissions(apiId.getProviderName(), null, null, resourcePath);
    			    
    	} catch (RegistryException e) {
    		handleException("Error while adding Swagger Definition for " + apiId.getApiName() + "-" + apiId.getVersion(), e);
		} catch (APIManagementException e) {
			handleException("Error while adding Swagger Definition for " + apiId.getApiName() + "-" + apiId.getVersion(), e);
		}
		
	}

	/**
	 * //TODO rearrange this in a proper way
	 * @param apiIdentifier
	 * @return
	 * @throws APIManagementException
	 */
	public JSONObject getSwagger12Definition(APIIdentifier apiIdentifier) throws APIManagementException {
		if (apiIdentifier == null) {
			handleException("Invalid number of input parameters.");
		}

		String provider = apiIdentifier.getProviderName();
		String name = apiIdentifier.getApiName();
		String version = apiIdentifier.getVersion();

		if (provider != null) {
			provider = APIUtil.replaceEmailDomain(provider);
		}
		provider = (provider != null ? provider.trim() : null);
		name = (name != null ? name.trim() : null);
		version = (version != null ? version.trim() : null);
		APIIdentifier apiId = new APIIdentifier(provider, name, version);

		boolean isTenantFlowStarted = false;
		JSONObject apiJSON = null;
		try {
			String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
			if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				isTenantFlowStarted = true;
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			}
			String resourcePath = APIUtil.getSwagger12DefinitionFilePath(apiId.getApiName(),
					apiId.getVersion(), apiId.getProviderName());
			JSONParser parser = new JSONParser();
			try {
				if (!registry.resourceExists(resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME)) {
					return APIUtil.createSwagger12JSONContent(getAPI(apiId));
				}
				Resource apiDocResource = registry.get(resourcePath + APIConstants.API_DOC_1_2_RESOURCE_NAME);
				String apiDocContent = new String((byte []) apiDocResource.getContent());
				apiJSON = (JSONObject) parser.parse(apiDocContent);
				JSONArray pathConfigs = (JSONArray) apiJSON.get("apis");

				for (int k = 0; k < pathConfigs.size(); k++) {
					JSONObject pathConfig = (JSONObject) pathConfigs.get(k);
					String pathName = (String) pathConfig.get("path");
					pathName = pathName.startsWith("/") ? pathName : ("/" + pathName);

					Resource pathResource = registry.get(resourcePath + pathName);
					String pathContent = new String((byte []) pathResource.getContent());
					JSONObject pathJSON = (JSONObject) parser.parse(pathContent);
					pathConfig.put("file", pathJSON);
				}
			} catch (RegistryException e) {
				handleException("Error while retrieving Swagger Definition for " + apiId.getApiName() + "-" +
				                apiId.getVersion(), e);
			} catch (ParseException e) {
				handleException("Error while parsing Swagger Definition for " + apiId.getApiName() + "-" +
				                apiId.getVersion() + " in " + resourcePath, e);
			}
			return apiJSON;
		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
	}
    /**
     * Returns the all the Consumer keys of applications which are subscribed to the given API
     *
     * @param apiIdentifier APIIdentifier
     * @return a String array of ConsumerKeys
     * @throws APIManagementException
     */
    public String[] getConsumerKeys(APIIdentifier apiIdentifier) throws APIManagementException {

        return apiMgtDAO.getConsumerKeys(apiIdentifier);
    }

    public String designAPI(JSONObject apiObj) throws APIManagementException {
        String provider = (String) apiObj.get("provider");
        String contextVal = (String) apiObj.get("context");
        String name = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");

        String providerDomain = MultitenantUtils.getTenantDomain(provider);

        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        if (!org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        if (isAPIAvailable(apiId)) {
            handleException("Error occurred while adding the API. A duplicate API already exists for " +
                            name + "-" + version);
        }
        API api = new API(apiId);
        api.setStatus(APIStatus.CREATED);

        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        api.setContextTemplate(context);

        context = updateContextWithVersion(version, contextVal, context);

        api.setContext(context);
        api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        api.setLastUpdated(new Date());
        Map<String,String> results=new HashMap<String, String>();
        results=saveAPI(api, true);
        return results.get("id");

    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if(!context.contains(APIConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE)){
            if(!context.endsWith("/")){
                context = context + "/";
            }
            context = context + APIConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE;
        }
        return context;
    }

    private static String updateContextWithVersion(String version, String contextVal, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        if (version == null) {
            // context template patterns - /{version}/foo or /foo/{version}
            // if the version is null, then we remove the /{version} part from the context
            context = contextVal.replace("/" + APIConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE, "");
        }else{
            context = context.replace(APIConstants.SYNAPSE_REST_CONTEXT_VERSION_VARIABLE, version);
        }
        return context;
    }

    public String manageAPI(JSONObject apiObj)
            throws APIManagementException {
        String provider = (String) apiObj.get("provider");
        String name = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        String swaggerContent = (String) apiObj.get("swagger");
        JSONParser parser = new JSONParser();
        JSONObject resourceConfigs = null;
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        try {
            JSONObject apiDocument = (JSONObject) parser.parse(swaggerContent);
            if (apiDocument.get("api_doc") != null) {
                resourceConfigs = (JSONObject) apiDocument.get("api_doc");
                if (resourceConfigs.get("authorizations") != null) {
                    JSONObject authorizations = (JSONObject) resourceConfigs.get("authorizations");
                    if (authorizations.get("oauth2") != null) {
                        JSONObject oauth2 = (JSONObject) authorizations.get("oauth2");
                        if (oauth2.get("scopes") != null) {
                            JSONArray scopes = (JSONArray) oauth2.get("scopes");

                            if (scopes != null) {
                                for (int i = 0; i < scopes.size(); i++) {
                                    Map scope = (Map) scopes.get(i);
                                    if (scope.get("key") != null) {
                                        Scope scopeObj = new Scope();
                                        scopeObj.setKey((String) scope.get("key"));
                                        scopeObj.setName((String) scope.get("name"));
                                        scopeObj.setRoles((String) scope.get("roles"));
                                        scopeObj.setDescription((String) scope.get("description"));
                                        scopeList.add(scopeObj);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Error while processing Api Definition.");
        }

        String subscriptionAvailability = (String) apiObj.get("subscriptionAvailability");
        String subscriptionAvailableTenants = "";
        if (subscriptionAvailability != null && subscriptionAvailability.equals(APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS)) {
            subscriptionAvailableTenants = (String) apiObj.get("subscriptionTenants");
        }

        String defaultVersion = (String) apiObj.get("defaultVersion");
        String transport = APIUtil.getTransports(apiObj);

        String tier = (String) apiObj.get("tier");

        String inSequence = (String) apiObj.get("inSequence");
        String outSequence = (String) apiObj.get("outSequence");
        String faultSequence = (String) apiObj.get("faultSequence");
        String businessOwner = (String) apiObj.get("bizOwner");
        String businessOwnerEmail = (String) apiObj.get("bizOwnerMail");
        String technicalOwner = (String) apiObj.get("techOwner");
        String technicalOwnerEmail = (String) apiObj.get("techOwnerMail");
        String environments = (String) apiObj.get("environments");
        String responseCache = (String) apiObj.get("responseCache");
        int cacheTimeOut = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
        if (APIConstants.ENABLED.equalsIgnoreCase(responseCache)) {
            responseCache = APIConstants.ENABLED;
            try {
                cacheTimeOut = Integer.parseInt((String) apiObj.get("cacheTimeout"));
            } catch (NumberFormatException e) {
                //ignore
            }
        } else {
            responseCache = APIConstants.DISABLED;
        }


        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        API api = null;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = getAPI(apiId);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        api.setTransports(transport);
        api.setSubscriptionAvailability(subscriptionAvailability);
        api.setSubscriptionAvailableTenants(subscriptionAvailableTenants);
        api.setResponseCache(responseCache);
        api.setCacheTimeout(cacheTimeOut);
        api.setAsDefaultVersion("default_version".equals(defaultVersion));
        api.setScopes(scopeList);

        api.removeCustomSequences();
        if (!"none".equals(inSequence)) {
            api.setInSequence(inSequence);
        }
        if (!"none".equals(outSequence)) {
            api.setOutSequence(outSequence);
        }
        if (!"none".equals(faultSequence)) {
            api.setFaultSequence(faultSequence);
        }

        if (!"none".equals(businessOwner)) {
            api.setBusinessOwner(businessOwner);
        }
        if (!"none".equals(businessOwnerEmail)) {
            api.setBusinessOwnerEmail(businessOwnerEmail);
        }
        if (!"none".equals(technicalOwner)) {
            api.setTechnicalOwner(technicalOwner);
        }
        if (!"none".equals(technicalOwnerEmail)) {
            api.setTechnicalOwnerEmail(technicalOwnerEmail);
        }
        api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environments));

        Set<Tier> availableTier = new HashSet<Tier>();
        String[] tierNames;
        if (tier != null) {
            tierNames = tier.split(",");
            for (String tierName : tierNames) {
                availableTier.add(new Tier(tierName));
            }
            api.removeAllTiers();
            api.addAvailableTiers(availableTier);
        }
        api.setLastUpdated(new Date());

        if (swaggerContent != null) {
            Set<URITemplate> uriTemplates = parseResourceConfig(apiId, (String) apiObj.get("swagger"), api);
            api.setUriTemplates(uriTemplates);
        }

        // removing scopes from cache
	    // removing scopes from cache
	    ProviderKeyMgtClient providerClient = APIUtil.getProviderClient(ServiceReferenceHolder.getInstance()
			    .getAPIManagerConfigurationService().getAPIManagerConfiguration());
	    try {
		    String[] consumerKeys = getConsumerKeys(new APIIdentifier(provider, name, version));
		    if (consumerKeys != null && consumerKeys.length != 0) {
			    providerClient.removeScopeCache(consumerKeys);
		    }

	    } catch (APIManagementException e) {
		    //swallowing the excepion since the api update should happen even if cache update fails
		    log.error("Error while removing the scope cache", e);
	    }

        Map<String,String> results=new HashMap<String, String>();
        results= saveAPI(api, false);
        return results.get("failedGateways");
    }

    public String updateDesignAPI(JSONObject apiObj)
            throws APIManagementException {
        String provider = (String) apiObj.get("provider");
        String contextVal = (String) apiObj.get("context");
        String name = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        //---------FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
        String description = (String) apiObj.get("description");

        /* Business Information*/
        String techOwner = (String) apiObj.get("techOwner");
        String techOwnerEmail = (String) apiObj.get("techOwnerEmail");
        String bizOwner = (String) apiObj.get("bizOwner");
        String bizOwnerEmail = (String) apiObj.get("bizOwnerEmail");

        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        String tags = (String) apiObj.get("tags");
        Set<String> tag = new HashSet<String>();

        if (tags != null) {
            if (tags.contains(",")) {
                String[] userTag = tags.split(",");
                tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
            } else {
                tag.add(tags);
            }
        }

        String visibility = (String) apiObj.get("visibility");
        String visibleRoles = "";


        if (visibility != null && visibility.equals(APIConstants.API_RESTRICTED_VISIBILITY)) {
            visibleRoles = (String) apiObj.get("visibleRoles");
        }

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);
        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        API api = null;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = getAPI(apiId);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        String swaggerContent = (String) apiObj.get("swagger");
        if (swaggerContent != null) {
            Set<URITemplate> uriTemplates = parseResourceConfig(apiId, (String) swaggerContent, api);
            api.setUriTemplates(uriTemplates);
        }

        api.setDescription(StringEscapeUtils.escapeHtml(description));
        HashSet<String> deletedTags = new HashSet<String>(api.getTags());
        deletedTags.removeAll(tag);
        api.removeTags(deletedTags);
        api.addTags(tag);
        api.setBusinessOwner(bizOwner);
        api.setBusinessOwnerEmail(bizOwnerEmail);
        api.setTechnicalOwner(techOwner);
        api.setTechnicalOwnerEmail(techOwnerEmail);
        api.setVisibility(visibility);
        api.setVisibleRoles(visibleRoles != null ? visibleRoles.trim() : null);
        api.setLastUpdated(new Date());
        Map<String,String> results=new HashMap<String, String>();
        results=saveAPI(api, false);
        return results.get("failedGateways");
    }

    /**
     * This method parses the JSON resource config and returns the UriTemplates. Also it saves the swagger
     * 1.2 resources in the registry
     *
     * @param resourceConfigsJSON
     * @return set of Uri Templates according to api
     * @throws APIManagementException
     */
    public Set<URITemplate> parseResourceConfig(APIIdentifier apiId,
                                                String resourceConfigsJSON, API api)
            throws APIManagementException {
        JSONParser parser = new JSONParser();
        JSONObject resourceConfigs = null;
        JSONObject api_doc = null;
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        boolean isTenantFlowStarted = false;
        try {
            resourceConfigs = (JSONObject) parser.parse(resourceConfigsJSON);
            api_doc = (JSONObject) resourceConfigs.get("api_doc");
            String apiJSON = api_doc.toJSONString();

            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            if (tenantDomain != null
                && !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                    .equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            updateSwagger12Definition(apiId, APIConstants.API_DOC_1_2_RESOURCE_NAME, apiJSON);

            /* Get Scopes*/
            if (api_doc.get("authorizations") != null) {
                JSONObject authorizations = (JSONObject) api_doc.get("authorizations");
                if (authorizations.get("oauth2") != null) {
                    JSONObject oauth2 = (JSONObject) authorizations.get("oauth2");
                    if (oauth2.get("scopes") != null) {
                        JSONArray scopes = (JSONArray) oauth2.get("scopes");

                        if (scopes != null) {
                            for (int i = 0; i < scopes.size(); i++) {
                                Map scope = (Map) scopes.get(i);
                                if (scope.get("key") != null) {
                                    Scope scopeObj = new Scope();
                                    scopeObj.setKey((String) scope.get("key"));
                                    scopeObj.setName((String) scope.get("name"));
                                    scopeObj.setRoles((String) scope.get("roles"));
                                    scopeObj.setDescription((String) scope.get("description"));
                                    scopeList.add(scopeObj);
                                }
                            }
                        }
                    }
                }
            }

            JSONArray resources = (JSONArray) resourceConfigs.get("resources");

            //Iterating each resourcePath config
            for (int i = 0; i < resources.size(); i++) {
                JSONObject resourceConfig = (JSONObject) resources.get(i);
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                Map<String, Environment> environments = config.getApiGatewayEnvironments();
                Environment env = null;
                String endpoint = "";
                if (environments != null) {
                    Set<String> publishedEnvironments = api.getEnvironments();
                    if (publishedEnvironments.isEmpty() || publishedEnvironments.contains("none")) {

                        env = environments.values().toArray(new Environment[environments.size()])[0];
                        String gatewayEndpoint = env.getApiGatewayEndpoint();
                        if (gatewayEndpoint.contains(",")) {
                            endpoint = gatewayEndpoint.split(",")[0];
                        } else {
                            endpoint = gatewayEndpoint;
                        }
                    } else {
                        for (String environmentName : publishedEnvironments) {
                            // find environment that have hybrid type
                            if (APIConstants.GATEWAY_ENV_TYPE_HYBRID
                                    .equals(environments.get(environmentName).getType())) {
                                env = environments.get(environmentName);
                                break;
                            }
                        }
                        //if not having any hybrid environment give 1st environment in api published list
                        if (env == null) {
                            env = environments.get(publishedEnvironments.toArray()[0]);
                        }
                        String gatewayEndpoint = env.getApiGatewayEndpoint();
                        if (gatewayEndpoint != null && gatewayEndpoint.contains(",")) {
                            endpoint = gatewayEndpoint.split(",")[0];
                        } else {
                            endpoint = gatewayEndpoint;
                        }
                    }
                }
                String apiPath = APIUtil.getAPIPath(apiId);
                if (StringUtils.isNotBlank(endpoint) && endpoint.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpoint.substring(0, endpoint.length() - 1);
                }
                // We do not need the version in the base path since with the context version strategy, the version is
                // embedded in the context
                String basePath = endpoint + api.getContext();
                // String basePath = ep+api.getContext()+RegistryConstants.PATH_SEPARATOR+apiId.getVersion();
                resourceConfig.put("basePath", basePath);
                String resourceJSON = resourceConfig.toJSONString();

                String resourcePath = (String) resourceConfig.get("resourcePath");

                updateSwagger12Definition(apiId, resourcePath, resourceConfig.toJSONString());

                JSONArray resource_configs = (JSONArray) resourceConfig.get("apis");

                //Iterating each Sub resourcePath config
                for (int j = 0; j < resource_configs.size(); j++) {
                    JSONObject resource = (JSONObject) resource_configs.get(j);
                    String uriTempVal = (String) resource.get("path");
                    uriTempVal = uriTempVal.startsWith("/") ? uriTempVal : ("/" + uriTempVal);

                    JSONArray operations = (JSONArray) resource.get("operations");
                    //Iterating each operation config
                    for (int k = 0; k < operations.size(); k++) {
                        JSONObject operation = (JSONObject) operations.get(k);
                        String httpVerb = (String) operation.get("method");
                        /* Right Now PATCH is not supported. Need to remove
                         * this check when PATCH is supported*/
                        if (!"PATCH".equals(httpVerb)) {
                            URITemplate template = new URITemplate();
                            Scope scope = APIUtil.findScopeByKey(scopeList, (String) operation.get("scope"));

                            String authType = (String) operation.get("auth_type");
                            if (authType != null) {
                                if (APIConstants.AUTH_APPLICATION_AND_APPLICATION_USER_LABEL.equals(authType)) {
                                    authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                                }
                                if (APIConstants.AUTH_APPLICATION_USER_LABEL.equals(authType)) {
                                    authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
                                }
                            } else {
                                authType = APIConstants.AUTH_NO_AUTHENTICATION;
                            }
                            template.setThrottlingTier((String) operation.get("throttling_tier"));
                            template.setMediationScript((String) operation.get("mediation_script"));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb);
                            template.setAuthType(authType);
                            template.setScope(scope);

                            uriTemplates.add(template);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource config", e);
        } catch (ClassCastException e) {
            handleException("Unable to create JSON object from resource config", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return uriTemplates;
    }

	    public String implementAPI(JSONObject apiObj) throws APIManagementException {
        String provider = (String) apiObj.get("provider");
        String name = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        String implementationType = (String) apiObj.get("implementation_type");

        if (provider != null) {
            provider = APIUtil.replaceEmailDomain(provider);
        }
        provider = (provider != null ? provider.trim() : null);
        name = (name != null ? name.trim() : null);
        version = (version != null ? version.trim() : null);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        API api = null;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            api = getAPI(apiId);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        api.setLastUpdated(new Date());

        api.setImplementation(implementationType);

        String wsdl = (String) apiObj.get("wsdl");
        String wadl = (String) apiObj.get("wadl");
        String endpointSecured = (String) apiObj.get("endpointSecured");
        String endpointUTUsername = (String) apiObj.get("endpointUTUsername");
        String endpointUTPassword = (String) apiObj.get("endpointUTPassword");

        api.setWadlUrl(wadl);
        api.setWsdlUrl(wsdl);
        api.setEndpointConfig((String) apiObj.get("endpoint_config"));

        // Validate endpoint URI format
        //---------------validateEndpointURI(api.getEndpointConfig());

        String destinationStats = (String) apiObj.get("destinationStats");
        if (APIConstants.ENABLED.equalsIgnoreCase(destinationStats)) {
            destinationStats = APIConstants.ENABLED;
        } else {
            destinationStats = APIConstants.DISABLED;
        }
        api.setDestinationStatsEnabled(destinationStats);

        //set secured endpoint parameters
        if ("secured".equals(endpointSecured)) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(endpointUTUsername);
            api.setEndpointUTPassword(endpointUTPassword);
        }
        String swaggerContent = (String) apiObj.get("swagger");

        if (swaggerContent != null) {
            // Set<URITemplate> uriTemplates = parseResourceConfig(apiProvider, apiId, (String) swaggerContent, api);
            // api.setUriTemplates(uriTemplates);
        }
        Map<String,String> results= new HashMap<String, String>();
        results=saveAPI(api, false);
        return results.get("id");
    }

    /**
     * This method save or update the API object
     *
     * @param api    API object
     * @param create
     * @return json string of failed Environments
     * @throws APIManagementException
     */
    private Map<String,String> saveAPI(API api,
                           boolean create) throws APIManagementException {
        Map<String,String> results=new HashMap<String,String>();
        String success = null;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain =
                    MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

          /*------------  if (fileHostObject != null && fileHostObject.getJavaScriptFile().getLength() != 0) {
                Icon icon = new Icon(fileHostObject.getInputStream(),
                                     fileHostObject.getJavaScriptFile().getContentType());
                String thumbPath = APIUtil.getIconPath(api.getId());

                String thumbnailUrl = apiProvider.addIcon(thumbPath, icon);
                api.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, api.getId().getProviderName()));

                /*Set permissions to anonymous role for thumbPath*/
            // APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, thumbPath);
            //}------------
            String id;
            if (create) {
                id=addAPI(api);
                success = createFailedGatewaysAsJsonString(Collections.<String, List<String>>emptyMap());
                results.put("id",id);
                results.put("failedGateways",success);
            } else {
                Map<String, List<String>> failedGateways = updateAPI(api);
                success = createFailedGatewaysAsJsonString(failedGateways);
                results.put("id",null);
                results.put("failedGateways",success);
            }

        } catch (APIManagementException e) {
            handleException("Error while adding the API- " + api.getId().getApiName() + "-" + api.getId().getVersion(),
                            e);
            results.put("id",null);
            results.put("failedGateways",createFailedGatewaysAsJsonString(Collections.<String, List<String>>emptyMap()));
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return results;
    }

    /**
     * @param failedGateways map of failed environments
     * @return json string of input map
     */
    private static String createFailedGatewaysAsJsonString(Map<String, List<String>> failedGateways) {
        String failedJson = "";
        if (failedGateways != null) {
	        failedJson = "{\"PUBLISHED\" : \"\" ,\"UNPUBLISHED\":\"\"}";
            if (!failedGateways.isEmpty()) {
                StringBuilder failedToPublish = new StringBuilder();
                StringBuilder failedToUnPublish = new StringBuilder();
                for (String environmentName : failedGateways.get("PUBLISHED")) {
                    failedToPublish.append(environmentName + ",");
                }
                for (String environmentName : failedGateways.get("UNPUBLISHED")) {
                    failedToUnPublish.append(environmentName + ",");
                }
                if (!"".equals(failedToPublish.toString())) {
                    failedToPublish.deleteCharAt(failedToPublish.length() - 1);
                }
                if (!"".equals(failedToUnPublish.toString())) {
                    failedToUnPublish.deleteCharAt(failedToUnPublish.length() - 1);
                }
                failedJson = "{\"PUBLISHED\" : \"" + failedToPublish.toString() + "\" ,\"UNPUBLISHED\":\"" +
                             failedToUnPublish.toString() + "\"}";
            }
        }
        return failedJson;
    }


    /**
     * This method check whether API older version exist or not
     *
     * @param apiObj    json object
     * @return apiOlderVersionExist
     * @throws APIManagementException
     */
    private boolean isAPIOlderVersionExist(NativeObject apiObj) throws APIManagementException {
        boolean apiOlderVersionExist = false;
        if (apiObj == null) {
            handleException("Invalid number of input parameters.");
        }
        String provider = (String) apiObj.get("provider", apiObj);
        provider = APIUtil.replaceEmailDomain(provider);
        String name = (String) apiObj.get("name", apiObj);
        String currentVersion = (String) apiObj.get("version", apiObj);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            Set<String> versions = getAPIVersions(provider, name);
            APIVersionStringComparator comparator = new APIVersionStringComparator();
            for (String version : versions) {
                if (comparator.compare(version, currentVersion) < 0) {
                    apiOlderVersionExist = true;
                    break;
                }
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return apiOlderVersionExist;
    }

    /**
     * This method check whether URL valid or not
     *
     * @param type
     * @param urlVal
     * @return response
     * @throws APIManagementException
     */
    public String isURLValid(String type, String urlVal) throws APIManagementException {
        String response = "";
        if (type == null || urlVal == null) {
            handleException("Invalid input parameters.");
        }

        if (urlVal != null && !urlVal.isEmpty()) {
            try {
                URL url = new URL(urlVal);
                if (type != null && "wsdl".equals(type)) {
                    CommonUtil.validateWsdl(urlVal);
                    response = "success";
                }
                // checking http,https endpoints up to resource level by doing http HEAD.
                // Other endpoint validation do through basic url connect.
                else if (url.getProtocol().matches(APIConstants.PROTOCOL_HTTPS)) {
                    ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
                    String trustStorePath = serverConfig.getFirstProperty("Security.TrustStore.Location");
                    String trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password");
                    System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

                    return CommonUtil.sendHttpHEADRequest(urlVal);
                } else if (url.getProtocol().matches(APIConstants.PROTOCOL_HTTP)) {
                    return CommonUtil.sendHttpHEADRequest(urlVal);
                } else {
                    return "error while connecting";
                }
            } catch (MalformedURLException e) {
                handleException("Invalid URL : " + urlVal, e);
            } catch (IOException e) {
                handleException("Error occurred while validating the wsdl", e);
            }
        }
        return response;
    }

	/**
	 * retrieves active tenant domains and return true or false to display private
	 * visibility
	 *
	 * @return boolean true If display private visibility
	 */
	public boolean isMultipleTenantsAvailable() {
		int tenantsDomainSize;
		Object cacheObj = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
				getCache(APIConstants.APIPROVIDER_HOSTCACHE).get(APIConstants.TENANTCOUNT_CACHEKEY);
		//if tenantDomainSize is not in the cache, Then the cache object is null
		if (cacheObj == null) {
			tenantsDomainSize = 0;
		} else {
			tenantsDomainSize = Integer.parseInt(cacheObj.toString());
		}
		//if there only super tenant in the system, tenantDomainSize is 1
		if (tenantsDomainSize < 2) {
			try {
				Set<String> tenantDomains = APIUtil.getActiveTenantDomains();
				//if there is more than than one tenant
				if (tenantDomains.size() > 1) {
					Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
							getCache(APIConstants.APIPROVIDER_HOSTCACHE).
							put(APIConstants.TENANTCOUNT_CACHEKEY, String.valueOf(tenantDomains.size()));
					return true;
				} else {
					return false;
				}
			} catch (UserStoreException e) {
                /*If there are errors in getting active tenant domains from user store,
                 Minimum privileges are allocated to the user
                */
				log.error("Errors in getting active tenants form UserStore " + e.getMessage(), e);
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * This method is to functionality of get list of environments that list in api-manager.xml
	 *
	 * @return list of environments with details of environments
	 */
	public JSONArray getEnvironments() {
		JSONArray result = new JSONArray();
		APIManagerConfiguration config =
				ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
						.getAPIManagerConfiguration();
		Map<String, Environment> environments = config.getApiGatewayEnvironments();
		int i = 0;
		if (environments != null) {
			for (Environment environment : environments.values()) {
				JSONObject row = new JSONObject();
				row.put("name", environment.getName());
				row.put("description", environment.getDescription());
				row.put("type", environment.getType());
				result.add(i, row);
				i++;
			}
		}
		return result;
	}

	public boolean validateRoles(String rolesSet) {
		boolean valid=false;
		String inputRolesSet = rolesSet;
		String[] inputRoles=null;
		if (inputRolesSet != null) {
			inputRoles = inputRolesSet.split(",");
		}

		try {
			String[] roles = APIUtil.getRoleNames(username);

			if (roles != null && inputRoles != null) {
				for (String inputRole : inputRoles) {
					for (String role : roles) {
						valid= (inputRole.equals(role));
						if(valid){ //If we found a match for the input role,then no need to process the for loop further
							break;
						}
					}
					//If the input role doesn't match with any of the role existing in the system
					if(!valid){
						return valid;
					}

				}
				return valid;
			}
		}catch (Exception e) {
			log.error("Error while validating the input roles.",e);
		}
		return valid;
	}

	/**
	 * @return json string of failed environments
	 * @throws APIManagementException
	 */
	public String updateAPIStatus(JSONObject apiData) throws APIManagementException {
		String success = null;
		String provider = (String) apiData.get("provider");
		provider = APIUtil.replaceEmailDomain(provider);
		String name = (String) apiData.get("apiName");
		String version = (String) apiData.get("version");
		String status = (String) apiData.get("status");
		boolean publishToGateway = (Boolean) apiData.get("publishToGateway");
		boolean deprecateOldVersions = (Boolean) apiData.get("deprecateOldVersions");
		boolean makeKeysForwardCompatible = (Boolean) apiData.get("makeKeysForwardCompatible");
		APIIdentifier apiId = new APIIdentifier(provider, name, version);
		return updateAPIStatus(apiId, status, publishToGateway, deprecateOldVersions,
				makeKeysForwardCompatible);
	}

	public String updateAPIStatus(APIIdentifier apiId, String status, boolean
		publishToGateway, boolean deprecateOldVersions, boolean makeKeysForwardCompatible) throws APIManagementException {
		Map<String, List<String>> failedGateways = new ConcurrentHashMap<String, List<String>>();
		boolean isTenantFlowStarted = false;
		try {
			String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
			if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				isTenantFlowStarted = true;
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			}
			API api = getAPI(apiId);
			if (api != null) {
				APIStatus oldStatus = api.getStatus();
				APIStatus newStatus = APIUtil.getApiStatus(status);
				String currentUser = this.username;
				failedGateways = changeAPIStatus(api, newStatus, currentUser, publishToGateway);

				if ((oldStatus.equals(APIStatus.CREATED) || oldStatus.equals(APIStatus.PROTOTYPED)) && newStatus.equals(APIStatus
						.PUBLISHED)) {
					if (makeKeysForwardCompatible) {
						makeAPIKeysForwardCompatible(api);
					}

					if (deprecateOldVersions) {
						List<API> apiList = getAPIsByProviderSet(apiId.getProviderName());
						APIVersionComparator versionComparator = new APIVersionComparator();
						for (API oldAPI : apiList) {
							if (oldAPI.getId().getApiName().equals(apiId.getApiName()) &&
							    versionComparator.compare(oldAPI, api) < 0 &&
							    (oldAPI.getStatus().equals(APIStatus.PUBLISHED))) {
								failedGateways = changeAPIStatus(oldAPI, APIStatus.DEPRECATED,
										currentUser, publishToGateway);
							}
						}
					}
				}

			} else {
				handleException("Couldn't find an API with the name-" + apiId.getApiName() + "version-" + apiId.getVersion());
			}
		} catch (APIManagementException e) {
			handleException("Error while updating API status", e);
			return createFailedGatewaysAsJsonString(failedGateways);
		}finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
		return createFailedGatewaysAsJsonString(failedGateways);
	}

	//TODO implement
	public boolean changeLifeCycleState(APIIdentifier identifier, String nextState) throws APIManagementException {
		try {
			GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
			String apiPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
			                        identifier.getProviderName() +
			                        RegistryConstants.PATH_SEPARATOR + identifier.getApiName() +
			                        RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
			                        APIConstants.API_RESOURCE_NAME;
			Resource apiResource = registry.get(apiPath);
			GenericArtifact apiArtifact = artifactManager.getGenericArtifact(
					apiResource.getUUID());
		} catch (RegistryException e) {
			String msg = "Failed to update default API version : " + identifier.getVersion() + " of : "
			             + identifier.getApiName();
			handleException(msg, e);
		}
		return false;
	}
}


