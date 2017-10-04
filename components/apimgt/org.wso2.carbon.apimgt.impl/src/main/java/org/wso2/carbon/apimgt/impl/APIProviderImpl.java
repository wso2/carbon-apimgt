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
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.PolicyDeploymentFailureException;
import org.wso2.carbon.apimgt.api.UnsupportedPolicyTypeException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Provider;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.Usage;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.clients.RegistryCacheInvalidationClient;
import org.wso2.carbon.apimgt.impl.clients.TierCacheInvalidationClient;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.NotificationExecutor;
import org.wso2.carbon.apimgt.impl.notification.NotifierConstants;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.template.ThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIStoreNameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.StatUpdateClusterMessage;
import org.wso2.carbon.apimgt.impl.workflow.APIStateWorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.statsupdate.stub.GatewayStatsUpdateServiceAPIManagementExceptionException;
import org.wso2.carbon.apimgt.statsupdate.stub.GatewayStatsUpdateServiceClusteringFaultException;
import org.wso2.carbon.apimgt.statsupdate.stub.GatewayStatsUpdateServiceExceptionException;
import org.wso2.carbon.apimgt.statsupdate.stub.GatewayStatsUpdateServiceStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.beans.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.CheckListItem;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.Property;
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
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.cache.Cache;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class provides the core API provider functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * pragmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIProviderImpl extends AbstractAPIManager implements APIProvider {

	private static final Log log = LogFactory.getLog(APIProviderImpl.class);

	private final String userNameWithoutChange;

    public APIProviderImpl(String username) throws APIManagementException {
        super(username);
        this.userNameWithoutChange = username;
    }

    protected String getUserNameWithoutChange() {
		return userNameWithoutChange;
	}

	/**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Providers
     */
    @Override
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
                Provider provider = new Provider(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_NAME));
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
    @Override
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException {

        List<API> apiSortedList = new ArrayList<API>();

        try {
            providerId = APIUtil.replaceEmailDomain(providerId);
            String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + providerId;
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            Association[] associations = registry.getAssociations(providerPath, APIConstants.PROVIDER_ASSOCIATION);
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
        }
        Collections.sort(apiSortedList, new APINameComparator());

        return apiSortedList;

    }


    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get subscribed APIs of given provider
     */
    @Override
    public Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException {

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
    @Override
    public Provider getProvider(String providerName) throws APIManagementException {
        Provider provider = null;
        String providerPath = APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                     RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                              APIConstants.PROVIDERS_PATH + RegistryConstants.PATH_SEPARATOR + providerName;
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.PROVIDER_KEY);
            Resource providerResource = registry.get(providerPath);
            String artifactId = providerResource.getUUID();
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
    @Override
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
    @Override
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
    @Override
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException {
        return apiMgtDAO.getAllAPIUsageByProvider(providerName);
    }

    /**
     * Returns usage details of a particular API
     *
     * @param apiId API identifier
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    @Override
    public List<SubscribedAPI> getAPIUsageByAPIId(APIIdentifier apiId) throws APIManagementException {
        APIIdentifier apiIdEmailReplaced = new APIIdentifier(APIUtil.replaceEmailDomain(apiId.getProviderName()),
                apiId.getApiName(), apiId.getVersion());
        UserApplicationAPIUsage[] allApiResult = apiMgtDAO.getAllAPIUsageByProvider(apiId.getProviderName());
        List<SubscribedAPI> subscribedAPIs = new ArrayList<SubscribedAPI>();
        for (UserApplicationAPIUsage usage : allApiResult) {
            for (SubscribedAPI apiSubscription : usage.getApiSubscriptions()) {
                APIIdentifier subsApiId = apiSubscription.getApiId();
                APIIdentifier subsApiIdEmailReplaced = new APIIdentifier(
                        APIUtil.replaceEmailDomain(subsApiId.getProviderName()), subsApiId.getApiName(),
                        subsApiId.getVersion());
                if (subsApiIdEmailReplaced.equals(apiIdEmailReplaced)) {
                    subscribedAPIs.add(apiSubscription);
                }
            }
        }
        return subscribedAPIs;
    }

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    @Override
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail) {
        return null;
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscribers
     */
    @Override
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get subscribers for API : " + identifier.getApiName(), e);
        }
        return subscriberSet;
    }

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get APISubscriptionCountByAPI
     */
    @Override
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier) throws APIManagementException {
        long count = 0L;
        try {
            count = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get APISubscriptionCount for: " + identifier.getApiName(), e);
        }
        return count;
    }

    @Override
    public void addTier(Tier tier) throws APIManagementException {
        addOrUpdateTier(tier, false);
    }

    @Override
    public void updateTier(Tier tier) throws APIManagementException {
        addOrUpdateTier(tier, true);
    }

    private void addOrUpdateTier(Tier tier, boolean update) throws APIManagementException {
        if (APIConstants.UNLIMITED_TIER.equals(tier.getName())) {
            throw new APIManagementException("Changes on the '" + APIConstants.UNLIMITED_TIER + "' " +
                                             "tier are not allowed");
        }

        Set<Tier> tiers = getAllTiers();
        if (update && !tiers.contains(tier)) {
            throw new APIManagementException("No tier exists by the name: " + tier.getName());
        }

        Set<Tier> finalTiers = new HashSet<Tier>();
        for (Tier t : tiers) {
            if (!t.getName().equals(tier.getName())) {
                finalTiers.add(t);
            }
        }

        invalidateTierCache();

        finalTiers.add(tier);
        saveTiers(finalTiers);
    }

    /**
     * This method is to cleanup tier cache when update or deletion is performed
     */
    private void invalidateTierCache() {

        try {
            // Note that this call happens to store node in a distributed setup.
            TierCacheInvalidationClient tierCacheInvalidationClient = new TierCacheInvalidationClient();
            tierCacheInvalidationClient.clearCaches(tenantDomain);

            // Clear registry cache. Note that this call happens to gateway node in a distributed setup.
            RegistryCacheInvalidationClient registryCacheInvalidationClient = new RegistryCacheInvalidationClient();
            registryCacheInvalidationClient.clearTiersResourceCache(tenantDomain);
        } catch (APIManagementException e) {
            // This means that there is an exception when trying to clear the cache.
            // But we should not break the flow in such scenarios.
            // Hence we log the exception and continue to the flow
            log.error("Error while invalidating the tier cache", e);
        }
    }

    private void saveTiers(Collection<Tier> tiers) throws APIManagementException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement root = fac.createOMElement(APIConstants.POLICY_ELEMENT);
        OMElement assertion = fac.createOMElement(APIConstants.ASSERTION_ELEMENT);
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            Resource resource = registry.newResource();
            for (Tier tier : tiers) {
                // This is because we do not save the unlimited tier to the tiers.xml file.
                if(APIConstants.UNLIMITED_TIER.equals(tier.getName())){
                    continue;
                }
                // This is a new tier. Hence the policyContent will be null
                if(tier.getPolicyContent() == null){
                    // This means we have to create the policy from scratch.
                    assertion.addChild(createThrottlePolicy(tier));
                }else {
                    String policy = new String(tier.getPolicyContent(), Charset.defaultCharset());
                    assertion.addChild(AXIOMUtil.stringToOM(policy));
                }
            }
            root.addChild(assertion);
            resource.setContent(root.toString());
            registry.put(APIConstants.API_TIER_LOCATION, resource);
        } catch (XMLStreamException e) {
            handleException("Error while constructing tier policy file", e);
        } catch (RegistryException e) {
            handleException("Error while saving tier configurations to the registry", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private OMElement createThrottlePolicy(Tier tier) throws APIManagementException {
        OMElement throttlePolicy = null;
        String policy = APIConstants.THROTTLE_POLICY_TEMPLATE;

        StringBuilder attributeBuilder = new StringBuilder();
        Map<String, Object> tierAttributes = tier.getTierAttributes();

        if(tierAttributes != null){
            for (Map.Entry<String, Object> entry : tierAttributes.entrySet()) {
                if(entry.getValue() instanceof String){
                    String attributeName = entry.getKey().trim();
                    String attributeValue = ((String)entry.getValue()).trim();

                    // We see whether the attribute name is empty.
                    if (!attributeName.isEmpty()) {
                        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                                                              attributeName, attributeValue, attributeName));
                    }
                }else {
                    if(log.isDebugEnabled()){
                        log.debug("Unrecognized throttle attribute value : " + entry.getValue() +
                                  " of attribute name : " + entry.getKey());
                    }
                }
            }
        }

        // We add the "description", "billing plan" and "stop on quota reach" as custom attributes
        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                                              APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE,
                                              tier.getDescription().trim(),
                                              APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE));

        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                                              APIConstants.THROTTLE_TIER_PLAN_ATTRIBUTE,
                                              tier.getTierPlan().trim(),
                                              APIConstants.THROTTLE_TIER_PLAN_ATTRIBUTE));

        attributeBuilder.append(String.format(APIConstants.THROTTLE_POLICY_ATTRIBUTE_TEMPLATE,
                                              APIConstants.THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE,
                                              String.valueOf(tier.isStopOnQuotaReached()),
                                              APIConstants.THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE));

        // Note: We assume that the unit time is in milliseconds.
        policy = String.format(policy, tier.getName(), tier.getRequestCount(), tier.getUnitTime(),
                               attributeBuilder.toString());

        try {
            throttlePolicy = AXIOMUtil.stringToOM(policy);
        } catch (XMLStreamException e) {
            handleException("Invalid policy xml generated", e);
        }
        return throttlePolicy;
    }

    @Override
    public void removeTier(Tier tier) throws APIManagementException {
        if (APIConstants.UNLIMITED_TIER.equals(tier.getName())) {
            handleException("Changes on the '" + APIConstants.UNLIMITED_TIER + "' " +
                                             "tier are not allowed");
        }

        Set<Tier> tiers = getAllTiers();
        // We need to see whether this used in any of the APIs
        GenericArtifact[] tierArtifacts = null;
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            try {
                // The search name pattern is this
                // tier=Gold|| OR ||Gold||
                String query = "tier=\"" + tier.getName() + "\\||\" \"\\||" + tier.getName() + "\\||\" \"\\||" + tier
                        .getName() + '\"';
                tierArtifacts = artifactManager.findGovernanceArtifacts(query);
            } catch (GovernanceException e) {
                handleException("Unable to check the usage of the tier ", e);
            }
        } catch (APIManagementException e) {
            handleException("Unable to delete the tier", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        if (tierArtifacts != null && tierArtifacts.length > 0) {
            // This means that there is at least one API that is using this tier. Hence we can not delete.
            handleException("Unable to remove this tier. Tier in use");
        }

        if (tiers.remove(tier)) {
            saveTiers(tiers);
            invalidateTierCache();
        } else {
            handleException("No tier exists by the name: " + tier.getName());
        }
    }

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add API
     */
    @Override
    public void addAPI(API api) throws APIManagementException {
        try {
            createAPI(api);

            if (log.isDebugEnabled()) {
                log.debug("API details successfully added to the registry. API Name: " + api.getId().getApiName()
                        + ", API Version : " + api.getId().getVersion() + ", API context : " + api.getContext());
            }

            int tenantId;
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new APIManagementException(
                        "Error in retrieving Tenant Information while adding api :" + api.getId().getApiName(), e);
            }
            apiMgtDAO.addAPI(api, tenantId);

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, api.getContext());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.CREATED, this.username);

            if (log.isDebugEnabled()) {
                log.debug("API details successfully added to the API Manager Database. API Name: " + api.getId()
                        .getApiName() + ", API Version : " + api.getId().getVersion() + ", API context : " + api
                        .getContext());
            }

            if (APIUtil.isAPIManagementEnabled()) {
            	Cache contextCache = APIUtil.getAPIContextCache();
            	Boolean apiContext = null;

                Object cachedObject = contextCache.get(api.getContext());
                if (cachedObject != null) {
            		apiContext = Boolean.valueOf(cachedObject.toString());
            	}
            	if (apiContext == null) {
                    contextCache.put(api.getContext(), Boolean.TRUE);
                }
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error in adding API :" + api.getId().getApiName(), e);
        }
    }

    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus Current status of the API
     * @throws APIManagementException on error
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

    @Override
    public String getDefaultVersion(APIIdentifier apiid) throws APIManagementException{

        String defaultVersion=null;
        try{
            defaultVersion=apiMgtDAO.getDefaultVersion(apiid);
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
            handleException("Error while getting published default version :" + apiid.getApiName(), e);
        }
        return defaultVersion;
    }


    /**
     * This method is used to save the wsdl file in the registry
     * This is used when user starts api creation with a soap endpoint
     *
     * @param api api object
     * @throws APIManagementException
     * @throws RegistryException
     */
    private void updateWsdl(API api) throws APIManagementException {

        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            String apiArtifactId = registry.get(APIUtil.getAPIPath(api.getId())).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            GenericArtifact apiArtifact = APIUtil.createAPIArtifactContent(artifact, api);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, apiArtifact.getId());
            if (APIUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
                String path = APIUtil.createWSDL(registry, api);
                if (path != null) {
                    registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                    apiArtifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl()); //reset the wsdl path
                    artifactManager.updateGenericArtifact(apiArtifact); //update the  artifact
                }
            }
            registry.commitTransaction();
            transactionCommitted = true;
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException ex) {
                handleException("Error occurred while rolling back the transaction.", ex);
            }
            throw new APIManagementException("Error occurred while saving the wsdl in the registry.", e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                handleException("Error occurred while rolling back the transaction.", ex);
            }
        }
    }

    public boolean isAPIUpdateValid(API api) throws APIManagementException{
    	String apiSourcePath = APIUtil.getAPIPath(api.getId());
    	boolean isValid = false;

    	try{
    		Resource apiSourceArtifact = registry.get(apiSourcePath);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiSourceArtifact.getUUID());
            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);

            if (!APIConstants.CREATED.equals(status) && !APIConstants.PROTOTYPED.equals(status)) {
            	//api at least is in published status
            	if(APIUtil.hasPermission(getUserNameWithoutChange(), APIConstants.Permissions.API_PUBLISH)){
            		//user has publish permission
            		isValid = true;
            	}
            }else if(APIConstants.CREATED.equals(status) || APIConstants.PROTOTYPED.equals(status)){
            	//api in create status
            	if(APIUtil.hasPermission(getUserNameWithoutChange(), APIConstants.Permissions.API_CREATE) || APIUtil.hasPermission(getUserNameWithoutChange(), APIConstants.Permissions.API_PUBLISH)){
            		//user has creat or publish permission
            		isValid = true;
            	}
            }

    	}catch(RegistryException ex){
    		 handleException("Error while validate user for API publishing", ex);
    	}
    	return isValid;

    }


    /**
     * Updates an existing API
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update API
     * @throws org.wso2.carbon.apimgt.api.FaultGatewaysException on Gateway Failure
     */
    @Override
    public void updateAPI(API api) throws APIManagementException, FaultGatewaysException {

    	boolean isValid = isAPIUpdateValid(api);
    	if(!isValid){
    		throw new APIManagementException(" User doesn't have permission for update");
    	}

        Map<String, Map<String, String>> failedGateways = new ConcurrentHashMap<String, Map<String, String>>();
        API oldApi = getAPI(api.getId());
        if (oldApi.getStatus().equals(api.getStatus())) {

                String previousDefaultVersion = getDefaultVersion(api.getId());
                String publishedDefaultVersion = getPublishedDefaultVersion(api.getId());

                if(previousDefaultVersion!=null){

                APIIdentifier defaultAPIId = new APIIdentifier(api.getId().getProviderName(), api.getId().getApiName(),
                        previousDefaultVersion);
                if (api.isDefaultVersion() ^ api.getId().getVersion().equals(previousDefaultVersion)) { // A change has
                                                                                                        // happen
                    // Remove the previous default API entry from the Registry
                    updateDefaultAPIInRegistry(defaultAPIId, false);
                    if (!api.isDefaultVersion()) {// default api tick is removed
                        // todo: if it is ok, these two variables can be put to the top of the function to remove
                        // duplication
                        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                        String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
                        if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
                            removeDefaultAPIFromGateway(api);
                        }
                    }
                }
                }

                //Update WSDL in the registry
                if (api.getWsdlUrl() != null) {
                    updateWsdl(api);
                }

                boolean updatePermissions = false;
                if(!oldApi.getVisibility().equals(api.getVisibility()) ||
                   (APIConstants.API_RESTRICTED_VISIBILITY.equals(oldApi.getVisibility()) &&
                    !api.getVisibleRoles().equals(oldApi.getVisibleRoles()))){
                    updatePermissions = true;
                }
                updateApiArtifact(api, true,updatePermissions);
                if (!oldApi.getContext().equals(api.getContext())) {
                    api.setApiHeaderChanged(true);
                }

                int tenantId;
                String tenantDomain = MultitenantUtils
                        .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                try {
                    tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                            .getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    throw new APIManagementException(
                            "Error in retrieving Tenant Information while updating api :" + api.getId().getApiName(), e);
                }
                apiMgtDAO.updateAPI(api, tenantId);

                if (log.isDebugEnabled()) {
                    log.debug("Successfully updated the API: " + api.getId() + " in the database");
                }

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, api.getContext());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());
            JSONObject apiLogObjectAPIInfo = new JSONObject();
            apiLogObjectAPIInfo.put("api level policy=", api.getApiLevelPolicy() );
            apiLogObjectAPIInfo.put("api- is default version=", api.isDefaultVersion());
            apiLogObjectAPIInfo.put("scopes=", api.getScopes());

            apiLogObject.put("API Info: " , apiLogObjectAPIInfo.toString());
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);

                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
                String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
                boolean isAPIPublished = false;
                // gatewayType check is required when API Management is deployed on other servers to avoid synapse
                if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
                    isAPIPublished = isAPIPublished(api);
                    if (gatewayExists) {
                        if (isAPIPublished) {
                            API apiPublished = getAPI(api.getId());
                            apiPublished.setAsDefaultVersion(api.isDefaultVersion());
                            if (api.getId().getVersion().equals(previousDefaultVersion) && !api.isDefaultVersion()) {
                                // default version tick has been removed so a default api for current should not be
                                // added/updated
                                apiPublished.setAsPublishedDefaultVersion(false);
                            } else {
                                apiPublished.setAsPublishedDefaultVersion(
                                        api.getId().getVersion().equals(publishedDefaultVersion));
                            }
                            apiPublished.setOldInSequence(oldApi.getInSequence());
                            apiPublished.setOldOutSequence(oldApi.getOutSequence());
                            //old api contain what environments want to remove
                            Set<String> environmentsToRemove = new HashSet<String>(oldApi.getEnvironments());
                            //updated api contain what environments want to add
                            Set<String> environmentsToPublish = new HashSet<String>(apiPublished.getEnvironments());
                            Set<String> environmentsRemoved = new HashSet<String>(oldApi.getEnvironments());
                            if (!environmentsToPublish.isEmpty() && !environmentsToRemove.isEmpty()) {
                                // this block will sort what gateways have to remove and published
                                environmentsRemoved.retainAll(environmentsToPublish);
                                environmentsToRemove.removeAll(environmentsRemoved);
                            }
                            // map contain failed to publish Environments
                            Map<String, String> failedToPublishEnvironments = publishToGateway(apiPublished);
                            apiPublished.setEnvironments(environmentsToRemove);
                            // map contain failed to remove Environments
                            Map<String, String> failedToRemoveEnvironments = removeFromGateway(apiPublished);
                            environmentsToPublish.removeAll(failedToPublishEnvironments.keySet());
                            environmentsToPublish.addAll(failedToRemoveEnvironments.keySet());
                            apiPublished.setEnvironments(environmentsToPublish);
                            updateApiArtifact(apiPublished, true, false);
                            failedGateways.clear();
                            failedGateways.put("UNPUBLISHED", failedToRemoveEnvironments);
                            failedGateways.put("PUBLISHED", failedToPublishEnvironments);
                        } else if (api.getStatus() != APIStatus.CREATED && api.getStatus() != APIStatus.RETIRED) {
                            if ("INLINE".equals(api.getImplementation()) && api.getEnvironments().isEmpty()){
                                api.setEnvironments(
                                        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                              .getAPIManagerConfiguration().getApiGatewayEnvironments()
                                                              .keySet());
                            }
                            Map<String, String> failedToPublishEnvironments = publishToGateway(api);
                            if (!failedToPublishEnvironments.isEmpty()) {
                                Set<String> publishedEnvironments =
                                        new HashSet<String>(api.getEnvironments());
                                publishedEnvironments.removeAll(failedToPublishEnvironments.keySet());
                                api.setEnvironments(publishedEnvironments);
                                updateApiArtifact(api, true, false);
                                failedGateways.clear();
                                failedGateways.put("PUBLISHED", failedToPublishEnvironments);
                                failedGateways.put("UNPUBLISHED", Collections.<String,String>emptyMap());
                            }
                        }
                    } else {
                        log.debug("Gateway is not existed for the current API Provider");
                    }
                }

                //If gateway(s) exist, remove resource paths saved on the cache.

                if (gatewayExists && isAPIPublished && !oldApi.getUriTemplates().equals(api.getUriTemplates())) {
                    Set<URITemplate> resourceVerbs = api.getUriTemplates();

                    Map<String, Environment> gatewayEns = config.getApiGatewayEnvironments();
                    for (Environment environment : gatewayEns.values()) {
                        try {
                        APIAuthenticationAdminClient client =
                                new APIAuthenticationAdminClient(environment);
                        if(resourceVerbs != null){
                            for (URITemplate resourceVerb : resourceVerbs) {
                                String resourceURLContext = resourceVerb.getUriTemplate();
                                client.invalidateResourceCache(api.getContext(), api.getId().getVersion(),
                                        resourceURLContext, resourceVerb.getHTTPVerb());
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling invalidation cache");
                                }
                            }
                        }
                        } catch (AxisFault ex) {
                             /*
                            didn't throw this exception to handle multiple gateway publishing feature therefore
                            this didn't break invalidating cache from the all the gateways if one gateway is
                            unreachable
                             */
                            log.error("Error while invalidating from environment " +
                                      environment.getName(), ex);
                        }
                    }

                }


                // update apiContext cache
                if (APIUtil.isAPIManagementEnabled()) {
                    Cache contextCache = APIUtil.getAPIContextCache();
                    contextCache.remove(oldApi.getContext());
                    contextCache.put(api.getContext(), Boolean.TRUE);
                }


        } else {
            // We don't allow API status updates via this method.
            // Use changeAPIStatus for that kind of updates.
            throw new APIManagementException("Invalid API update operation involving API status changes");
        }
        if (!failedGateways.isEmpty() &&
            (!failedGateways.get("UNPUBLISHED").isEmpty() || !failedGateways.get("PUBLISHED").isEmpty())) {
            throw new FaultGatewaysException(failedGateways);
        }
    }

    @Override
    public void manageAPI(API api) throws APIManagementException, FaultGatewaysException {
        updateAPI(api);
    }

    private void updateApiArtifact(API api, boolean updateMetadata, boolean updatePermissions)
            throws APIManagementException {

        //Validate Transports
        validateAndSetTransports(api);
        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            String apiArtifactId = registry.get(APIUtil.getAPIPath(api.getId())).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            
            //This is a fix for broken APIs after migrating from 1.10 to 2.0.0.
            //This sets the endpoint security of the APIs based on the old artifact. 
            APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
            if (gatewayManager.isAPIPublished(api, tenantDomain)) {
                if ((!api.isEndpointSecured() && !api.isEndpointAuthDigest())) {
                    boolean isSecured = Boolean.parseBoolean(
                                                 artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED));
                    boolean isDigestSecured = Boolean.parseBoolean(
                                                 artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST));
                    String userName = artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME);
                    String password = artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD);
                    
                    //Check for APIs marked as non-secured, but username is set. 
                    if (!isSecured && !isDigestSecured && userName != null) {
                        String epAuthType = gatewayManager.getAPIEndpointSecurityType(api, tenantDomain);
                        if (APIConstants.APIEndpointSecurityConstants.DIGEST_AUTH.equalsIgnoreCase(epAuthType)) {
                            api.setEndpointSecured(true);
                            api.setEndpointAuthDigest(true);                            
                        } else if (APIConstants.APIEndpointSecurityConstants.BASIC_AUTH.equalsIgnoreCase(epAuthType)) {
                            api.setEndpointSecured(true);
                        }
                        api.setEndpointUTUsername(userName);
                        api.setEndpointUTPassword(password);
                    }                   
                    
                }
            }
            
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
            if (api.isDefaultVersion()) {
                updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "true");
            } else {
                updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "false");
            }

            if (updateMetadata && api.getEndpointConfig() != null && !api.getEndpointConfig().isEmpty()) {
                // If WSDL URL get change only we update registry WSDL resource. If its registry resource patch we
                // will skip registry update. Only if this API created with WSDL end point type we need to update
                // wsdls for each update.
                //check for wsdl endpoint
                org.json.JSONObject response1 = new org.json.JSONObject(api.getEndpointConfig());
                boolean isWSAPI = APIConstants.APIType.WS.toString().equals(api.getType());
                String wsdlURL;
                if (!isWSAPI && "wsdl".equalsIgnoreCase(response1.get("endpoint_type").toString()) && response1.has
                        ("production_endpoints")) {
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
            }
            artifactManager.updateGenericArtifact(updateApiArtifact);
            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            String[] visibleRoles = new String[0];
            if (updatePermissions) {
                clearResourcePermissions(artifactPath, api.getId());
                String visibleRolesList = api.getVisibleRoles();

                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles,
                                               artifactPath);
            }
            registry.commitTransaction();
            transactionCommitted = true;
            if (updatePermissions) {
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                boolean isSetDocLevelPermissions = Boolean.parseBoolean(
                        config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS));
                String docRootPath = APIUtil.getAPIDocPath(api.getId());
                if (isSetDocLevelPermissions) {
                    // Retain the docs
                    List<Documentation> docs = getAllDocumentation(api.getId());

                    for (Documentation doc : docs) {
                        if ((APIConstants.DOC_API_BASED_VISIBILITY).equalsIgnoreCase(doc.getVisibility().name())) {
                            String documentationPath = APIUtil.getAPIDocPath(api.getId()) + doc.getName();
                            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                                           visibleRoles, documentationPath);
                            if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {

                                String contentPath = APIUtil.getAPIDocContentPath(api.getId(), doc.getName());
                                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                                               visibleRoles, contentPath);
                            } else if (Documentation.DocumentSourceType.FILE.equals(doc.getSourceType()) &&
                                       doc.getFilePath() != null) {
                                String filePath = APIUtil.getDocumentationFilePath(api.getId(), doc.getFilePath()
                                        .split("files" + RegistryConstants.PATH_SEPARATOR)[1]);
                                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                                               visibleRoles, filePath);
                            }
                        }
                    }
                } else {
                    APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles,
                                                   docRootPath);
                }
            }
        } catch (Exception e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error from this level will mask the original exception
                log.error("Error while rolling back the transaction for API: " + api.getId().getApiName(), re);
            }
            handleException("Error while performing registry transaction operation", e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                handleException("Error occurred while rolling back the transaction.", ex);
            }
        }
    }

    /**
     *
     * @return true if the API was added successfully
     * @throws APIManagementException
     */
    @Override
    public  boolean updateAPIStatus(APIIdentifier identifier, String status, boolean publishToGateway, boolean
            deprecateOldVersions
            ,boolean makeKeysForwardCompatible)
            throws APIManagementException, FaultGatewaysException {
        boolean success = false;
        String provider = identifier.getProviderName();
        String providerTenantMode = identifier.getProviderName();
        provider = APIUtil.replaceEmailDomain(provider);
        String name = identifier.getApiName();
        String version = identifier.getVersion();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = getAPI(apiId);
            if (api != null) {
                APIStatus oldStatus = api.getStatus();
                APIStatus newStatus = APIUtil.getApiStatus(status);
                String currentUser = this.username;
                changeAPIStatus(api, newStatus, APIUtil.appendDomainWithUser(currentUser, tenantDomain), publishToGateway);

                if ((oldStatus.equals(APIStatus.CREATED) || oldStatus.equals(APIStatus.PROTOTYPED))
                        && newStatus.equals(APIStatus.PUBLISHED)) {
                    if (makeKeysForwardCompatible) {
                        makeAPIKeysForwardCompatible(api);
                    }
                    if (deprecateOldVersions) {
                        List<API> apiList = getAPIsByProvider(provider);
                        APIVersionComparator versionComparator = new APIVersionComparator();
                        for (API oldAPI : apiList) {
                            if (oldAPI.getId().getApiName().equals(name) &&
                                    versionComparator.compare(oldAPI, api) < 0 &&
                                    (oldAPI.getStatus().equals(APIStatus.PUBLISHED))) {
                                changeLifeCycleStatus(oldAPI.getId(), APIConstants.API_LC_ACTION_DEPRECATE);
                            }
                        }
                    }
                }
                success = true;
                if (log.isDebugEnabled()) {
                    log.debug("API status successfully updated to: " + newStatus + " in API Name: " + api.getId()
                            .getApiName() + ", API Version : " + api.getId().getVersion() + ", API context : " + api
                            .getContext());
                }
            } else {
                handleException("Couldn't find an API with the name-" + name + "version-" + version);
            }
        } catch (FaultGatewaysException e) {
            handleException("Error while publishing to/un-publishing from  API gateway", e);
            return false;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    @Override
    public void changeAPIStatus(API api, APIStatus status, String userId, boolean updateGatewayConfig)
            throws APIManagementException, FaultGatewaysException {
        Map<String, Map<String,String>> failedGateways = new ConcurrentHashMap<String, Map<String, String>>();
        APIStatus currentStatus = api.getStatus();
        if (!currentStatus.equals(status)) {
            api.setStatus(status);
            try {
                //If API status changed to publish we should add it to recently added APIs list
                //this should happen in store-publisher cluster domain if deployment is distributed
                //IF new API published we will add it to recently added APIs
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).removeAll();

                APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

                api.setAsPublishedDefaultVersion(
                        api.getId().getVersion().equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));

                if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType) && updateGatewayConfig) {
                    if (APIStatus.PUBLISHED.equals(status) || APIStatus.DEPRECATED.equals(status) ||
                        APIStatus.BLOCKED.equals(status) || APIStatus.PROTOTYPED.equals(status)) {
                        Map<String, String> failedToPublishEnvironments = publishToGateway(api);
                        if (!failedToPublishEnvironments.isEmpty()) {
                            Set<String> publishedEnvironments = new HashSet<String>(api.getEnvironments());
                            publishedEnvironments.removeAll(new ArrayList<String>(failedToPublishEnvironments.keySet()));
                            api.setEnvironments(publishedEnvironments);
                            updateApiArtifact(api, true, false);
                            failedGateways.clear();
                            failedGateways.put("UNPUBLISHED", Collections.<String,String>emptyMap());
                            failedGateways.put("PUBLISHED", failedToPublishEnvironments);
                        }
                    } else { // API Status : RETIRED or CREATED
                        Map<String, String> failedToRemoveEnvironments = removeFromGateway(api);
                        if(!APIStatus.CREATED.equals(status)){
                            apiMgtDAO.removeAllSubscriptions(api.getId());
                        }
                        if (!failedToRemoveEnvironments.isEmpty()) {
                            Set<String> publishedEnvironments = new HashSet<String>(api.getEnvironments());
                            publishedEnvironments.addAll(failedToRemoveEnvironments.keySet());
                            api.setEnvironments(publishedEnvironments);
                            updateApiArtifact(api, true, false);
                            failedGateways.clear();

                            failedGateways.put("UNPUBLISHED", failedToRemoveEnvironments);
                            failedGateways.put("PUBLISHED", Collections.<String,String>emptyMap());
                        }
                    }
                }

                updateApiArtifact(api, false,false);
                apiMgtDAO.recordAPILifeCycleEvent(api.getId(), currentStatus, status, userId, this.tenantId);

                if(api.isDefaultVersion() || api.isPublishedDefaultVersion()){ //published default version need to be changed
                    apiMgtDAO.updateDefaultAPIPublishedVersion(api.getId(), currentStatus, status);
                }

            } catch (APIManagementException e) {
            	handleException("Error occurred in the status change : " + api.getId().getApiName() + ". "
            	                                                                                + e.getMessage(), e);
            }
        }
        if (!failedGateways.isEmpty() &&
            (!failedGateways.get("UNPUBLISHED").isEmpty() || !failedGateways.get("PUBLISHED").isEmpty())) {
            throw new FaultGatewaysException(failedGateways);
        }
    }

    @Override
    public Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier identifier, APIStatus newStatus)
            throws APIManagementException {
        Map<String, String> failedGateways = new HashMap<String, String>();
        String provider = identifier.getProviderName();
        String providerTenantMode = identifier.getProviderName();
        provider = APIUtil.replaceEmailDomain(provider);
        String name = identifier.getApiName();
        String version = identifier.getVersion();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = getAPI(apiId);
            if (api != null) {
                APIStatus currentStatus = api.getStatus();

                if (!currentStatus.equals(newStatus)) {
                    api.setStatus(newStatus);

                    APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                    String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

                    api.setAsPublishedDefaultVersion(api.getId().getVersion()
                            .equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));

                    if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
                        if (APIStatus.PUBLISHED.equals(newStatus) || APIStatus.DEPRECATED.equals(newStatus)
                            || APIStatus.BLOCKED.equals(newStatus) || APIStatus.PROTOTYPED.equals(newStatus)) {
                            failedGateways = publishToGateway(api);
                        } else { // API Status : RETIRED or CREATED
                            failedGateways = removeFromGateway(api);
                        }
                    }

                }
            } else {
                handleException("Couldn't find an API with the name-" + name + "version-" + version);
            }

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return failedGateways;
    }

    @Override
    public boolean updateAPIforStateChange(APIIdentifier identifier, APIStatus newStatus,
            Map<String, String> failedGatewaysMap) throws APIManagementException, FaultGatewaysException {

        boolean isSuccess = false;
        Map<String, Map<String, String>> failedGateways = new ConcurrentHashMap<String, Map<String, String>>();
        String provider = identifier.getProviderName();
        String providerTenantMode = identifier.getProviderName();
        provider = APIUtil.replaceEmailDomain(provider);
        String name = identifier.getApiName();
        String version = identifier.getVersion();

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = getAPI(apiId);
            if (api != null) {
                APIStatus currentStatus = api.getStatus();

                if (!currentStatus.equals(newStatus)) {
                    api.setStatus(newStatus);

                    // If API status changed to publish we should add it to recently added APIs list
                    // this should happen in store-publisher cluster domain if deployment is distributed
                    // IF new API published we will add it to recently added APIs
                    Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                            .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).removeAll();


                    api.setAsPublishedDefaultVersion(api.getId().getVersion()
                            .equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));

                    if (failedGatewaysMap != null) {

                        if (APIStatus.PUBLISHED.equals(newStatus) || APIStatus.DEPRECATED.equals(newStatus)
                            || APIStatus.BLOCKED.equals(newStatus) || APIStatus.PROTOTYPED.equals(newStatus)) {
                            Map<String, String> failedToPublishEnvironments = failedGatewaysMap;
                            if (!failedToPublishEnvironments.isEmpty()) {
                                Set<String> publishedEnvironments = new HashSet<String>(api.getEnvironments());
                                publishedEnvironments.removeAll(new ArrayList<String>(failedToPublishEnvironments
                                        .keySet()));
                                api.setEnvironments(publishedEnvironments);
                                updateApiArtifact(api, true, false);
                                failedGateways.clear();
                                failedGateways.put("UNPUBLISHED", Collections.<String, String> emptyMap());
                                failedGateways.put("PUBLISHED", failedToPublishEnvironments);

                            }
                        } else { // API Status : RETIRED or CREATED
                            Map<String, String> failedToRemoveEnvironments = failedGatewaysMap;
                            if(!APIStatus.CREATED.equals(newStatus)) {
                                apiMgtDAO.removeAllSubscriptions(api.getId());
                            }
                            if (!failedToRemoveEnvironments.isEmpty()) {
                                Set<String> publishedEnvironments = new HashSet<String>(api.getEnvironments());
                                publishedEnvironments.addAll(failedToRemoveEnvironments.keySet());
                                api.setEnvironments(publishedEnvironments);
                                updateApiArtifact(api, true, false);
                                failedGateways.clear();
                                failedGateways.put("UNPUBLISHED", failedToRemoveEnvironments);
                                failedGateways.put("PUBLISHED", Collections.<String, String> emptyMap());

                            }
                        }
                    }

                    updateApiArtifact(api, false, false);

                    if (api.isDefaultVersion() || api.isPublishedDefaultVersion()) { // published default version need
                                                                                     // to be changed
                        apiMgtDAO.updateDefaultAPIPublishedVersion(api.getId(), currentStatus, newStatus);
                    }
                }
                isSuccess = true;
            } else {
                handleException("Couldn't find an API with the name-" + name + "version-" + version);
            }

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        if (!failedGateways.isEmpty()
                && (!failedGateways.get("UNPUBLISHED").isEmpty() || !failedGateways.get("PUBLISHED").isEmpty())) {
            throw new FaultGatewaysException(failedGateways);
        }
        return isSuccess;
    }

    /**
     * Function returns true if the specified API already exists in the registry
     * @param identifier
     * @return
     * @throws APIManagementException
     */
    public boolean checkIfAPIExists(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            Registry registry;
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null
                        && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                            identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    if (this.tenantDomain != null
                            && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                        registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(
                                identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                    } else {
                        registry = this.registry;
                    }
                }
            }

            return registry.resourceExists(apiPath);
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

    private Map<String, String> publishToGateway(API api) throws APIManagementException {
        Map<String, String> failedEnvironment;
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
            String logMessage = "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                    + " published to gateway";
            log.debug(logMessage);
        }
        return failedEnvironment;
    }

    private void validateAndSetTransports(API api) throws APIManagementException {
        String transports = api.getTransports();
        if(transports != null && !("null".equalsIgnoreCase(transports)))    {
            if (transports.contains(",")) {
                StringTokenizer st = new StringTokenizer(transports, ",");
                while (st.hasMoreTokens()) {
                    checkIfValidTransport(st.nextToken());
                }
            } else  {
                checkIfValidTransport(transports);
            }
        } else  {
            api.setTransports(Constants.TRANSPORT_HTTP + ',' + Constants.TRANSPORT_HTTPS);
        }
    }

    private void checkIfValidTransport(String transport) throws APIManagementException {
        if(!Constants.TRANSPORT_HTTP.equalsIgnoreCase(transport) && !Constants.TRANSPORT_HTTPS.equalsIgnoreCase(transport)){
            handleException("Unsupported Transport [" + transport + ']');
        }
    }

    private Map<String, String> removeFromGateway(API api) {
        String tenantDomain = null;
        Map<String, String> failedEnvironment;
        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
        }

        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        failedEnvironment = gatewayManager.removeFromGateway(api, tenantDomain);
        if (log.isDebugEnabled()) {
            String logMessage = "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                    + " deleted from gateway";
            log.debug(logMessage);
        }
        return failedEnvironment;
    }

    public Map<String, String> removeDefaultAPIFromGateway(API api) {
        String tenantDomain = null;
        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
        }

        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        return gatewayManager.removeDefaultAPIFromGateway(api, tenantDomain);

    }

    private boolean isAPIPublished(API api)throws APIManagementException {
            String tenantDomain = null;
			if (api.getId().getProviderName().contains("AT")) {
				String provider = api.getId().getProviderName().replace("-AT-", "@");
				tenantDomain = MultitenantUtils.getTenantDomain( provider);
			}
            APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
            return gatewayManager.isAPIPublished(api, tenantDomain);
        }

    private APITemplateBuilder getAPITemplateBuilder(API api) throws APIManagementException {
        APITemplateBuilderImpl vtb = new APITemplateBuilderImpl(api);
        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.common.APIMgtLatencyStatsHandler", Collections
                .<String, String>emptyMap());
        Map<String, String> corsProperties = new HashMap<String, String>();
        corsProperties.put(APIConstants.CORSHeaders.IMPLEMENTATION_TYPE_HANDLER_VALUE, api.getImplementation());

        if (api.getCorsConfiguration() != null && api.getCorsConfiguration().isCorsConfigurationEnabled()) {
            CORSConfiguration corsConfiguration = api.getCorsConfiguration();
            if (corsConfiguration.getAccessControlAllowHeaders() != null) {
                StringBuilder allowHeaders = new StringBuilder();
                for (String header : corsConfiguration.getAccessControlAllowHeaders()) {
                    allowHeaders.append(header).append(',');
                }
                if (allowHeaders.length() != 0) {
                    allowHeaders.deleteCharAt(allowHeaders.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_HEADERS_HANDLER_VALUE, allowHeaders.toString());
                }
            }
            if (corsConfiguration.getAccessControlAllowOrigins() != null) {
                StringBuilder allowOrigins = new StringBuilder();
                for (String origin : corsConfiguration.getAccessControlAllowOrigins()) {
                    allowOrigins.append(origin).append(',');
                }
                if (allowOrigins.length() != 0) {
                    allowOrigins.deleteCharAt(allowOrigins.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_ORIGIN_HANDLER_VALUE, allowOrigins.toString());
                }
            }
            if (corsConfiguration.getAccessControlAllowMethods() != null) {
                StringBuilder allowedMethods = new StringBuilder();
                for (String methods : corsConfiguration.getAccessControlAllowMethods()) {
                    allowedMethods.append(methods).append(',');
                }
                if (allowedMethods.length() != 0) {
                    allowedMethods.deleteCharAt(allowedMethods.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_METHODS_HANDLER_VALUE, allowedMethods.toString());
                }
            }
            if (corsConfiguration.isAccessControlAllowCredentials()) {
                corsProperties.put(APIConstants.CORSHeaders.ALLOW_CREDENTIALS_HANDLER_VALUE,
                                   String.valueOf(corsConfiguration.isAccessControlAllowCredentials()));
            }
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler", corsProperties);
        } else if (APIUtil.isCORSEnabled()) {
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler", corsProperties);
        }
        if(!api.getStatus().equals(APIStatus.PROTOTYPED)) {

            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler",
                           Collections.<String,String>emptyMap());

            Map<String, String> properties = new HashMap<String, String>();

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            boolean isGlobalThrottlingEnabled =  APIUtil.isAdvanceThrottlingEnabled();
            if (api.getProductionMaxTps() != null) {
                properties.put("productionMaxCount", api.getProductionMaxTps());
            }

            if (api.getSandboxMaxTps() != null) {
                properties.put("sandboxMaxCount", api.getSandboxMaxTps());
            }

            if(isGlobalThrottlingEnabled){
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.throttling.ThrottleHandler", properties);
            } else {
                properties.put("id", "A");
                properties.put("policyKey", "gov:" + APIConstants.API_TIER_LOCATION);
                properties.put("policyKeyApplication", "gov:" + APIConstants.APP_TIER_LOCATION);
                properties.put("policyKeyResource", "gov:" + APIConstants.RES_TIER_LOCATION);

                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleHandler", properties);
            }

            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtUsageHandler", Collections.<String,String>emptyMap());

            properties = new HashMap<String, String>();
            properties.put("configKey", "gov:" + APIConstants.GA_CONFIGURATION_LOCATION);
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtGoogleAnalyticsTrackingHandler", properties);

            String extensionHandlerPosition = getExtensionHandlerPosition();
            if (extensionHandlerPosition != null && "top".equalsIgnoreCase(extensionHandlerPosition)) {
                vtb.addHandlerPriority("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                                       Collections.<String,String>emptyMap(), 0);
            } else {
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                               Collections.<String,String>emptyMap());
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
     * Add a file to a document of source type FILE
     *
     * @param apiId API identifier the document belongs to
     * @param documentation document
     * @param filename name of the file
     * @param content content of the file as an Input Stream
     * @param contentType content type of the file
     * @throws APIManagementException if failed to add the file
     */
    public void addFileToDocumentation(APIIdentifier apiId, Documentation documentation, String filename,
            InputStream content, String contentType) throws APIManagementException {
        if (Documentation.DocumentSourceType.FILE.equals(documentation.getSourceType())) {
            ResourceFile icon = new ResourceFile(content, contentType);
            String filePath = APIUtil.getDocumentationFilePath(apiId, filename);
            API api;
            try {
                api = getAPI(apiId);
                String visibleRolesList = api.getVisibleRoles();
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles,
                        filePath);
                documentation.setFilePath(addResourceFile(filePath, icon));
                APIUtil.setFilePermission(filePath);
            } catch (APIManagementException e) {
                handleException("Failed to add file to document " + documentation.getName(), e);
            }
        } else {
            String errorMsg = "Cannot add file to the Document. Document " + documentation.getName()
                    + "'s Source type is not FILE.";
            handleException(errorMsg);
        }
    }

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws org.wso2.carbon.apimgt.api.model.DuplicateAPIException
     *          If the API trying to be created already exists
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If an error occurs while trying to create
     *          the new version of the API
     */
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException, APIManagementException {
        String apiSourcePath = APIUtil.getAPIPath(api.getId());

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                            api.getId().getProviderName() +
                            RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                            RegistryConstants.PATH_SEPARATOR + newVersion +
                            APIConstants.API_RESOURCE_NAME;

        boolean transactionCommitted = false;
        try {
            if (registry.resourceExists(targetPath)) {
                throw new DuplicateAPIException("API already exists with version: " + newVersion);
            }
            registry.beginTransaction();
            Resource apiSourceArtifact = registry.get(apiSourcePath);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiSourceArtifact.getUUID());

            //Create new API version
            artifact.setId(UUID.randomUUID().toString());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, newVersion);

            //Check the status of the existing api,if its not in 'CREATED' status set
            //the new api status as "CREATED"
            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            if (!APIConstants.CREATED.equals(status)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, APIConstants.CREATED);
            }

            if(api.isDefaultVersion())  {
                artifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "true");
                //Check whether an existing API is set as default version.
                String defaultVersion = getDefaultVersion(api.getId());

                //if so, change its DefaultAPIVersion attribute to false

                if(defaultVersion!=null)    {
                    APIIdentifier defaultAPIId = new APIIdentifier(api.getId().getProviderName(),api.getId().getApiName(),
                                                                   defaultVersion);
                    updateDefaultAPIInRegistry(defaultAPIId,false);
                }
            } else  {
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
                ResourceFile icon = new ResourceFile(oldImage.getContentStream(), oldImage.getMediaType());
                artifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL,
                                      addResourceFile(APIUtil.getIconPath(newApiId), icon));
            }
            // If the API has custom mediation policy, copy it to new version.

            String inSeqFilePath = APIUtil.getSequencePath(api.getId(), "in");

            if (registry.resourceExists(inSeqFilePath)) {

                APIIdentifier newApiId = new APIIdentifier(api.getId().getProviderName(),
                                                           api.getId().getApiName(), newVersion);

                String inSeqNewFilePath = APIUtil.getSequencePath(newApiId, "in");
                org.wso2.carbon.registry.api.Collection inSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(inSeqFilePath);
                if (inSeqCollection != null) {
                    String[] inSeqChildPaths = inSeqCollection.getChildren();
                    for (String inSeqChildPath : inSeqChildPaths)    {
                        Resource inSequence = registry.get(inSeqChildPath);

                        ResourceFile seqFile = new ResourceFile(inSequence.getContentStream(), inSequence.getMediaType());
                        OMElement seqElment = APIUtil.buildOMElement(inSequence.getContentStream());
                        String seqFileName = seqElment.getAttributeValue(new QName("name"));
                        addResourceFile(inSeqNewFilePath + seqFileName, seqFile);
                    }
                }
            }


            String outSeqFilePath = APIUtil.getSequencePath(api.getId(), "out");

            if (registry.resourceExists(outSeqFilePath)) {

                APIIdentifier newApiId = new APIIdentifier(api.getId().getProviderName(),
                                                           api.getId().getApiName(), newVersion);

                String outSeqNewFilePath = APIUtil.getSequencePath(newApiId, "out");
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(outSeqFilePath);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String outSeqChildPath : outSeqChildPaths)    {
                        Resource outSequence = registry.get(outSeqChildPath);

                        ResourceFile seqFile = new ResourceFile(outSequence.getContentStream(), outSequence.getMediaType());
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        String seqFileName = seqElment.getAttributeValue(new QName("name"));
                        addResourceFile(outSeqNewFilePath + seqFileName, seqFile);
                    }
                }
            }



            // Here we keep the old context
            String oldContext =  artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT);

            // We need to change the context by setting the new version
            // This is a change that is coming with the context version strategy
            String contextTemplate = artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE);
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT, contextTemplate.replace("{version}", newVersion));

            if ("true".equalsIgnoreCase(artifact.getAttribute(APIConstants.API_OVERVIEW_WEBSOCKET))) {
                APIGatewayManager.getInstance().createNewWebsocketApiVersion(artifact, api);
            }
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            //Attach the API lifecycle
            artifact.attachLifecycle(APIConstants.API_LIFE_CYCLE);
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
            if (iconPath != null && iconPath.lastIndexOf("/apimgt") != -1) {
                iconPath = iconPath.substring(iconPath.lastIndexOf("/apimgt"));
                APIUtil.copyResourcePermissions(api.getId().getProviderName(), thumbUrl, iconPath);
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
                /* copying the file in registry for new api */
                Documentation.DocumentSourceType sourceType = doc.getSourceType();
                if (sourceType == Documentation.DocumentSourceType.FILE) {
                    String absoluteSourceFilePath = doc.getFilePath();
                    // extract the prepend
                    // ->/registry/resource/_system/governance/ and for
                    // tenant
                    // /t/my.com/registry/resource/_system/governance/
                    int prependIndex = absoluteSourceFilePath.indexOf(APIConstants.API_LOCATION);
                    String prependPath = absoluteSourceFilePath.substring(0, prependIndex);
                    // get the file name from absolute file path
                    int fileNameIndex = absoluteSourceFilePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR);
                    String fileName = absoluteSourceFilePath.substring(fileNameIndex + 1);
                    // create relative file path of old location
                    String sourceFilePath = absoluteSourceFilePath.substring(prependIndex);
                    // create the relative file path where file should be
                    // copied
                    String targetFilePath =
                                            APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                                    newId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                                                    newId.getApiName() + RegistryConstants.PATH_SEPARATOR +
                                                    newId.getVersion() + RegistryConstants.PATH_SEPARATOR +
                                                    APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR +
                                                    APIConstants.DOCUMENT_FILE_DIR + RegistryConstants.PATH_SEPARATOR +
                                                    fileName;
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

            //Copy Swagger 2.0 resources for New version. 
            String resourcePath = APIUtil.getSwagger20DefinitionFilePath(api.getId().getApiName(),
                                                                         api.getId().getVersion(),
                                                                         api.getId().getProviderName());
            if (registry.resourceExists(resourcePath + APIConstants.API_DOC_2_0_RESOURCE_NAME)) {
                JSONObject swaggerObject = (JSONObject) new JSONParser()
                        .parse(definitionFromSwagger20.getAPIDefinition(api.getId(), registry));
                JSONObject infoObject = (JSONObject) swaggerObject.get("info");
                infoObject.remove("version");
                infoObject.put("version", newAPI.getId().getVersion());
                definitionFromSwagger20.saveAPIDefinition(newAPI, swaggerObject.toJSONString(), registry);
            }

            // Make sure to unset the isLatest flag on the old version
            GenericArtifact oldArtifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());
            oldArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "false");
            artifactManager.updateGenericArtifact(oldArtifact);

            int tenantId;
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new APIManagementException("Error in retrieving Tenant Information while adding api :"
                        +api.getId().getApiName(),e);
            }

            apiMgtDAO.addAPI(newAPI, tenantId);
            registry.commitTransaction();
            transactionCommitted = true;

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, newAPI.getId().getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, newAPI.getContext());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, newAPI.getId().getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, newAPI.getId().getProviderName());
            apiLogObject.put(APIConstants.AuditLogConstants.IS_API_DEFAULT_VERSION, api.isDefaultVersion());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.CREATED, this.username);


            if(log.isDebugEnabled()) {
                String logMessage = "Successfully created new version : " + newVersion + " of : " + api.getId().getApiName();
                log.debug(logMessage);
            }

            //Sending Notifications to existing subscribers
            try {
                String isNotificationEnabled = "false";
                Registry configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                        getConfigSystemRegistry(tenantId);
                if (configRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                    Resource resource = configRegistry.get(APIConstants.API_TENANT_CONF_LOCATION);
                    String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                    if(content !=null ){
                        JSONObject tenantConfig= (JSONObject) new JSONParser().parse(content);
                        isNotificationEnabled = (String) tenantConfig.get(NotifierConstants.NOTIFICATIONS_ENABLED);
                    }
                }

                if (JavaUtils.isTrueExplicitly(isNotificationEnabled)){

                    Properties prop = new Properties();
                    prop.put(NotifierConstants.API_KEY, api.getId());
                    prop.put(NotifierConstants.NEW_API_KEY, newAPI.getId());

                    Set<Subscriber> subscribersOfAPI = apiMgtDAO.getSubscribersOfAPI(api.getId());
                    prop.put(NotifierConstants.SUBSCRIBERS_PER_API, subscribersOfAPI);

                    Set<Subscriber> subscribersOfProvider = apiMgtDAO.getSubscribersOfProvider(api.getId()
                            .getProviderName());
                    prop.put(NotifierConstants.SUBSCRIBERS_PER_API, subscribersOfProvider);

                    NotificationDTO notificationDTO=new NotificationDTO(prop,NotifierConstants
                            .NOTIFICATION_TYPE_NEW_VERSION);
                    notificationDTO.setTenantID(tenantId);
                    notificationDTO.setTenantDomain(tenantDomain);
                    new NotificationExecutor().sendAsyncNotifications(notificationDTO);

                }
            } catch (NotificationException e) {
                log.error(e.getMessage(), e);
            }

        } catch (DuplicateAPIException e)   {
            throw e;
        } catch (ParseException e) {
            String msg = "Couldn't Create json Object from Swagger object for version" + newVersion + " of : " +
                                 api.getId().getApiName();
            handleException(msg, e);
        } catch (Exception e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                handleException("Error while rolling back the transaction for API: " + api.getId(), re);
            }
            String msg = "Failed to create new version : " + newVersion + " of : " + api.getId().getApiName();
            handleException(msg, e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                handleException("Error while rolling back the transaction for API: " + api.getId(), ex);
            }
        }
    }

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType) throws APIManagementException {
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;

        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String docFilePath =  artifact.getAttribute(APIConstants.DOC_FILE_PATH);

            if(docFilePath!=null)   {
                File tempFile = new File(docFilePath);
                String fileName = tempFile.getName();
                docFilePath = APIUtil.getDocumentationFilePath(apiId,fileName);
                if(registry.resourceExists(docFilePath))    {
                    registry.delete(docFilePath);
                }
            }

            Association[] associations = registry.getAssociations(docPath, APIConstants.DOCUMENTATION_KEY);
            for (Association association : associations) {
                registry.delete(association.getDestinationPath());
            }
        } catch (RegistryException e) {
            handleException("Failed to delete documentation", e);
        }
    }

    /**
     *
     * @param apiId   APIIdentifier
     * @param docId UUID of the doc
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docId)
            throws APIManagementException {
        String docPath ;

        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            docPath = artifact.getPath();
            String docFilePath =  artifact.getAttribute(APIConstants.DOC_FILE_PATH);

            if(docFilePath!=null)
            {
                File tempFile = new File(docFilePath);
                String fileName = tempFile.getName();
                docFilePath = APIUtil.getDocumentationFilePath(apiId,fileName);
                if(registry.resourceExists(docFilePath))
                {
                    registry.delete(docFilePath);
                }
            }

            Association[] associations = registry.getAssociations(docPath,
                                                                  APIConstants.DOCUMENTATION_KEY);

            for (Association association : associations) {
                registry.delete(association.getDestinationPath());
            }
        } catch (RegistryException e) {
            handleException("Failed to delete documentation", e);
        }
    }


    /**
     * Adds Documentation to an API
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add documentation
     */
    public void addDocumentation(APIIdentifier apiId, Documentation documentation) throws APIManagementException {
    	API api = getAPI(apiId);
    	createDocumentation(api, documentation);
    }

    /**
     * This method used to save the documentation content
     *
     * @param api,        API
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(API api, String documentationName, String text) throws APIManagementException {

    	APIIdentifier identifier = api.getId();
    	String documentationPath = APIUtil.getAPIDocPath(identifier) + documentationName;
    	String contentPath = APIUtil.getAPIDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
    			RegistryConstants.PATH_SEPARATOR + documentationName;
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;

                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            Resource docResource = registry.get(documentationPath);
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                         APIConstants.DOCUMENTATION_KEY);
            GenericArtifact docArtifact = artifactManager.getGenericArtifact(docResource.getUUID());
            Documentation doc = APIUtil.getDocumentation(docArtifact);

            Resource docContent;

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
            registry.addAssociation(documentationPath, contentPath, APIConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
            String apiPath = APIUtil.getAPIPath(identifier);
            String[] authorizedRoles = getAuthorizedRoles(apiPath);
            String docVisibility=doc.getVisibility().name();
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

            APIUtil.setResourcePermissions(api.getId().getProviderName(),visibility, authorizedRoles,contentPath);

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, api.getContext());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());
            apiLogObject.put("Added Documentation ", documentationName);

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);

        } catch (RegistryException e) {
            String msg = "Failed to add the documentation content of : "
                         + documentationName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to add the documentation content of : "
                         + documentationName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
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
    public void updateDocumentation(APIIdentifier apiId, Documentation documentation) throws APIManagementException {

        String apiPath = APIUtil.getAPIPath(apiId);
        API api = getAPI(apiPath);
        String docPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiId.getProviderName() +
                                 RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                                 RegistryConstants.PATH_SEPARATOR + apiId.getVersion() +
                                 RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR +
                                 RegistryConstants.PATH_SEPARATOR + documentation.getName();

        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String docVisibility = documentation.getVisibility().name();
            String[] authorizedRoles = new String[0];
            String visibleRolesList = api.getVisibleRoles();
            if (visibleRolesList != null) {
                authorizedRoles = visibleRolesList.split(",");
            }
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

            GenericArtifact updateApiArtifact = APIUtil.createDocArtifactContent(artifact, apiId, documentation);
            artifactManager.updateGenericArtifact(updateApiArtifact);
            clearResourcePermissions(docPath, apiId);

            APIUtil.setResourcePermissions(api.getId().getProviderName(), visibility, authorizedRoles,
                                           artifact.getPath());

            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if (docFilePath != null && !"".equals(docFilePath)) {
                // The docFilePatch comes as
                // /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                // We need to remove the
                // /t/tenanatdoman/registry/resource/_system/governance section
                // to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                APIUtil.setResourcePermissions(api.getId().getProviderName(), visibility, authorizedRoles, filePath);
            }

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, api.getId().getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.CONTEXT, api.getContext());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, api.getId().getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, api.getId().getProviderName());
            apiLogObject.put("Updated Documentation ", documentation.getName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);

        } catch (RegistryException e) {
            handleException("Failed to update documentation", e);
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
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {

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
     * @throws APIManagementException if failed to create API
     */
    private void createAPI(API api) throws APIManagementException {
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);

        //Validate Transports
        validateAndSetTransports(api);
        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = APIUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            //Attach the API lifecycle
            artifact.attachLifecycle(APIConstants.API_LIFE_CYCLE);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
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

            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().getStatus();
            saveAPIStatus(artifactPath, apiStatus);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles,
                    artifactPath);
            registry.commitTransaction();
            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                String logMessage =
                        "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                                + " created";
                log.debug(logMessage);
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
    }

    /**
     * This function is to set resource permissions based on its visibility
     *
     * @param artifactPath API resource path
     * @throws APIManagementException Throwing exception
     */
    private void clearResourcePermissions(String artifactPath, APIIdentifier apiId) throws APIManagementException {
        try {
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                    APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + artifactPath);
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService()
                        .getTenantUserRealm(((UserRegistry) registry).getTenantId()).getAuthorizationManager();
                authManager.clearResourceAuthorizations(resourcePath);
            } else {
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(
                        ServiceReferenceHolder.getUserRealm());
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
     * @throws APIManagementException if failed to add documentation
     */
    private void createDocumentation(API api, Documentation documentation) throws APIManagementException {
        try {
            APIIdentifier apiId = api.getId();
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            artifactManager.addGenericArtifact(APIUtil.createDocArtifactContent(artifact, apiId, documentation));
            String apiPath = APIUtil.getAPIPath(apiId);

            //Adding association from api to documentation . (API -----> doc)
            registry.addAssociation(apiPath, artifact.getPath(),APIConstants.DOCUMENTATION_ASSOCIATION);
            String docVisibility = documentation.getVisibility().name();
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
            APIUtil.setResourcePermissions(api.getId().getProviderName(),visibility, authorizedRoles, artifact.getPath());
            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if (docFilePath != null && !"".equals(docFilePath)) {
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                APIUtil.setResourcePermissions(api.getId().getProviderName(),visibility, authorizedRoles, filePath);
                registry.addAssociation(artifact.getPath(), filePath,APIConstants.DOCUMENTATION_FILE_ASSOCIATION);
            }
            documentation.setId(artifact.getId());
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

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantManager().getTenantId(tenantDomain);
            AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getAuthorizationManager();
            return authManager.getAllowedRolesForResource(resourcePath,ActionConstants.GET);
        } else {
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
     * @param apiId API Identifier
     * @param subStatus Subscription Status
     * @param appId Application Id              *
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public void updateSubscription(APIIdentifier apiId,String subStatus,int appId) throws APIManagementException {
        apiMgtDAO.updateSubscription(apiId,subStatus,appId);
    }

    /**
     * This method is used to update the subscription
     *
     * @param subscribedAPI subscribedAPI object that represents the new subscription detals
     * @throws APIManagementException if failed to update subscription
     */
    public void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException {
        apiMgtDAO.updateSubscription(subscribedAPI);
    }

    public void deleteAPI(APIIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                      identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                      identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();

        String apiArtifactPath = APIUtil.getAPIPath(identifier);

        try {
            int apiId = apiMgtDAO.getAPIID(identifier, null);
            long subsCount = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
            if(subsCount > 0){
                //Logging as a WARN since this isn't an error scenario. 
                String message = "Cannot remove the API as active subscriptions exist.";
                log.warn(message);
                throw new APIManagementException(message);
            }

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
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
            String faultSequence =  apiArtifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE);
            String environments = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            String type = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE);
            String context_val = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
            //Delete the dependencies associated  with the api artifact
			GovernanceArtifact[] dependenciesArray = apiArtifact.getDependencies();

			if (dependenciesArray.length > 0) {
                for (GovernanceArtifact artifact : dependenciesArray)   {
                    registry.delete(artifact.getPath());
                }
			}
            String isDefaultVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION);
            artifactManager.removeGenericArtifact(apiArtifact);
            artifactManager.removeGenericArtifact(artifactId);


            String thumbPath = APIUtil.getIconPath(identifier);
            if (registry.resourceExists(thumbPath)) {
                registry.delete(thumbPath);
            }
            
            /*Remove API Definition Resource - swagger*/
            String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
            		identifier.getApiName() + '-'  + identifier.getVersion() + '-' + identifier.getProviderName();
            if (registry.resourceExists(apiDefinitionFilePath)) {
            	registry.delete(apiDefinitionFilePath);
            }

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
            boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
            String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

            API api = new API(identifier);
            api.setAsDefaultVersion(Boolean.parseBoolean(isDefaultVersion));
            api.setAsPublishedDefaultVersion(api.getId().getVersion().equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));
            api.setType(type);
            api.setContext(context_val);
            // gatewayType check is required when API Management is deployed on
            // other servers to avoid synapse
            if (gatewayExists && "Synapse".equals(gatewayType)) {

                api.setInSequence(inSequence); // need to remove the custom sequences
                api.setOutSequence(outSequence);
                api.setFaultSequence(faultSequence);

                api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environments));
                removeFromGateway(api);
                if (api.isDefaultVersion()) {
                    removeDefaultAPIFromGateway(api);
                }

            } else {
                log.debug("Gateway is not existed for the current API Provider");
            }
            //Check if there are already published external APIStores.If yes,removing APIs from them.
            Set<APIStore> apiStoreSet = getPublishedExternalAPIStores(api.getId());
            WSO2APIPublisher wso2APIPublisher = new WSO2APIPublisher();
            if (apiStoreSet != null && !apiStoreSet.isEmpty()) {
                for (APIStore store : apiStoreSet) {
                    wso2APIPublisher.deleteFromStore(api.getId(), APIUtil.getExternalAPIStore(store.getName(), tenantId));
                }
            }

            //if manageAPIs == true
            if (APIUtil.isAPIManagementEnabled()) {
                Cache contextCache = APIUtil.getAPIContextCache();
                String context = apiMgtDAO.getAPIContext(identifier);
                contextCache.remove(context);
                contextCache.put(context, Boolean.FALSE);
            }

            apiMgtDAO.deleteAPI(identifier);

            if (log.isDebugEnabled()) {
                String logMessage =
                        "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                                + " successfully removed from the database.";
                log.debug(logMessage);
            }

            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, identifier.getApiName());
            apiLogObject.put(APIConstants.AuditLogConstants.VERSION, identifier.getVersion());
            apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, identifier.getProviderName());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.DELETED, this.username);

            /*remove empty directories*/
            String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName();
            if(registry.resourceExists(apiCollectionPath)){
            	Resource apiCollection=registry.get(apiCollectionPath);
            	CollectionImpl collection=(CollectionImpl)apiCollection;
            	//if there is no other versions of apis delete the directory of the api
            	if(collection.getChildCount() == 0){
                    if(log.isDebugEnabled()){
                        log.debug("No more versions of the API found, removing API collection from registry");
                    }
            		registry.delete(apiCollectionPath);
            	}
            }

            String apiProviderPath=APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                   identifier.getProviderName();

            if(registry.resourceExists(apiProviderPath)){
            	Resource providerCollection=registry.get(apiProviderPath);
            	CollectionImpl collection=(CollectionImpl)providerCollection;
            	//if there is no api for given provider delete the provider directory
            	if(collection.getChildCount() == 0){
                    if(log.isDebugEnabled()){
                        log.debug("No more APIs from the provider " + identifier.getProviderName() + " found. " +
                            "Removing provider collection from registry");
                    }
            		registry.delete(apiProviderPath);
            	}
            }
            cleanUpPendingAPIStateChangeTask(apiId);
            //Run cleanup task for workflow
            /*
            WorkflowExecutor apiStateChangeWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);
  
            WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                    WorkflowConstants.WF_TYPE_AM_API_STATE);
            if(wfDTO != null && WorkflowStatus.CREATED == wfDTO.getStatus()){
                apiStateChangeWFExecutor.cleanUpPendingTask(wfDTO.getExternalWorkflowReference());
            }
            */
        } catch (RegistryException e) {
            handleException("Failed to remove the API from : " + path, e);
        } catch (WorkflowException e) {
            handleException("Failed to execute workflow cleanup task ", e);
        }
    }

    public Map<Documentation, API> searchAPIsByDoc(String searchTerm, String searchType) throws APIManagementException {
        return APIUtil.searchAPIsByDoc(registry, tenantId, username, searchTerm, APIConstants.PUBLISHER_CLIENT);
    }

    /**
     * Search APIs based on given search term
     * @param searchTerm
     * @param searchType
     * @param providerId
     *
     * @throws APIManagementException
     */

	public List<API> searchAPIs(String searchTerm, String searchType, String providerId) throws APIManagementException {
		List<API> foundApiList = new ArrayList<API>();
		String regex = "(?i)[\\w.|-]*" + searchTerm.trim() + "[\\w.|-]*";
		Pattern pattern;
		Matcher matcher;
		String apiConstant = null;
		try {
			if (providerId != null) {
                List<API> apiList = getAPIsByProvider(providerId);
				if (apiList == null || apiList.isEmpty()) {
					return apiList;
				}
				pattern = Pattern.compile(regex);
				for (API api : apiList) {
					if ("Name".equalsIgnoreCase(searchType)) {
						apiConstant = api.getId().getApiName();
					} else if ("Provider".equalsIgnoreCase(searchType)) {
						apiConstant = api.getId().getProviderName();
					} else if ("Version".equalsIgnoreCase(searchType)) {
						apiConstant = api.getId().getVersion();
					} else if ("Context".equalsIgnoreCase(searchType)) {
						apiConstant = api.getContext();
					} else if ("Status".equalsIgnoreCase(searchType)) {
						apiConstant = api.getStatus().getStatus();
					} else if (APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE.equalsIgnoreCase(searchType)) {
						apiConstant = api.getDescription();
					}
					if (apiConstant != null) {
						matcher = pattern.matcher(apiConstant);
						if (matcher.find()) {
                            foundApiList.add(api);
						}
					}
					if ("Subcontext".equalsIgnoreCase(searchType)) {
						Set<URITemplate> urls = api.getUriTemplates();
						for (URITemplate url : urls) {
                            matcher = pattern.matcher(url.getUriTemplate());
                            if (matcher.find()) {
                                foundApiList.add(api);
                                break;
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
	 * @throws APIManagementException
	 */

	private List<API> searchAPIs(String searchTerm, String searchType) throws APIManagementException {
		List<API> apiList = new ArrayList<API>();

		Pattern pattern;
		Matcher matcher;
		String searchCriteria = APIConstants.API_OVERVIEW_NAME;
		boolean isTenantFlowStarted = false;
		String userName = this.username;
		try {
			if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				isTenantFlowStarted = true;
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			}
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
			GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
			if (artifactManager != null) {
				if ("Name".equalsIgnoreCase(searchType)) {
					searchCriteria = APIConstants.API_OVERVIEW_NAME;
				} else if ("Version".equalsIgnoreCase(searchType)) {
					searchCriteria = APIConstants.API_OVERVIEW_VERSION;
				} else if ("Context".equalsIgnoreCase(searchType)) {
					searchCriteria = APIConstants.API_OVERVIEW_CONTEXT;
				} else if (APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE.equalsIgnoreCase(searchType)) {
					searchCriteria = APIConstants.API_OVERVIEW_DESCRIPTION;
				} else if ("Provider".equalsIgnoreCase(searchType)) {
					searchCriteria = APIConstants.API_OVERVIEW_PROVIDER;
					searchTerm = searchTerm.replaceAll("@", "-AT-");
				} else if ("Status".equalsIgnoreCase(searchType)) {
					searchCriteria = APIConstants.API_OVERVIEW_STATUS;
				}

				String regex = "(?i)[\\w.|-]*" + searchTerm.trim() + "[\\w.|-]*";
				pattern = Pattern.compile(regex);

				if ("Subcontext".equalsIgnoreCase(searchType)) {

					List<API> allAPIs = getAllAPIs();
					for (API api : allAPIs) {
						Set<URITemplate> urls = api.getUriTemplates();
						for (URITemplate url : urls) {
                            matcher = pattern.matcher(url.getUriTemplate());
                            if (matcher.find()) {
                                apiList.add(api);
                                break;
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
							if (matcher.find()) {
                                API resultAPI = APIUtil.getAPI(artifact, registry);
                                if (resultAPI != null) {
                                    apiList.add(resultAPI);
                                }
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
     * Retrieves Extension Handler Position from the tenant-config.json
     *
     * @return ExtensionHandlerPosition
     * @throws APIManagementException
     */
    private String getExtensionHandlerPosition() throws APIManagementException {
        String extensionHandlerPosition = null;
        APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();
        try {
            String content = apimRegistryService.getConfigRegistryResourceContent(tenantDomain, APIConstants
                    .API_TENANT_CONF_LOCATION);
            if (content != null) {
                JSONParser jsonParser = new JSONParser();
                JSONObject tenantConf = (JSONObject) jsonParser.parse(content);
                extensionHandlerPosition = (String) tenantConf.get(APIConstants.EXTENSION_HANDLER_POSITION);
            }
        } catch (RegistryException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        }catch (UserStoreException  e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (ParseException e) {
            handleException("Couldn't parse tenant configuration for reading extension handler position", e);
        }
        return extensionHandlerPosition;
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
		return apiMgtDAO.getTierPermissions(tenantId);
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
    public void updateThrottleTierPermissions(String tierName, String permissionType, String roles) throws
            APIManagementException {
        apiMgtDAO.updateThrottleTierPermissions(tierName, permissionType, roles, tenantId);
    }

    @Override
    public Set<TierPermissionDTO> getThrottleTierPermissions() throws APIManagementException {
        return apiMgtDAO.getThrottleTierPermissions(tenantId);
    }

    /**
     * When enabled publishing to external APIStores support,publish the API to external APIStores
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    @Override
    public void publishToExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {

        Set<APIStore> publishedStores = new HashSet<APIStore>();
        StringBuilder errorStatus = new StringBuilder("Failure to publish to External Stores : ");
        boolean failure = false;

        for (APIStore store : apiStoreSet) {
            org.wso2.carbon.apimgt.api.model.APIPublisher publisher = store.getPublisher();

            try {
                // First trying to publish the API to external APIStore
                boolean published;
                String version = ApiMgtDAO.getInstance().getLastPublishedAPIVersionFromAPIStore(api.getId(),
                                                                                                store.getName());

                if (apiOlderVersionExist && version != null) {
                    published = publisher.createVersionedAPIToStore(api, store, version);
                    publisher.updateToStore(api, store);
                } else {
                    published = publisher.publishToStore(api, store);
                }

                if (published) { // If published,then save to database.
                    publishedStores.add(store);
                }
            } catch (APIManagementException e) {
                failure = true;
                log.error(e);
                errorStatus.append(store.getDisplayName()).append(',');
            }
        }
        if (!publishedStores.isEmpty()) {
            addExternalAPIStoresDetails(api.getId(), publishedStores);
        }

        if (failure) {
            throw new APIManagementException(errorStatus.substring(0, errorStatus.length() -2));
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
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {
        Set<APIStore> publishedStores=getPublishedExternalAPIStores(api.getId());
        Set<APIStore> notPublishedAPIStores = new HashSet<APIStore>();
        Set<APIStore> modifiedPublishedApiStores = new HashSet<APIStore>();
        Set<APIStore> updateApiStores = new HashSet<APIStore>();
        Set<APIStore> removedApiStores = new HashSet<APIStore>();
        StringBuilder errorStatus = new StringBuilder("Failed to update External Stores : ");
        boolean failure = false;
        if(publishedStores != null){
            removedApiStores.addAll(publishedStores);
            removedApiStores.removeAll(apiStoreSet);
        }
        for (APIStore apiStore: apiStoreSet) {
            boolean publishedToStore = false;
            if (publishedStores != null) {
                for (APIStore store : publishedStores) {  //If selected external store in edit page is already saved in db
                    if (store.equals(apiStore)) { //Check if there's a modification happened in config file external store definition
                        try {
                            if (!isAPIAvailableInExternalAPIStore(api, apiStore)) {
                                // API is not available
                                continue;
                            }
                        } catch (APIManagementException e) {
                            failure = true;
                            log.error(e);
                            errorStatus.append(store.getDisplayName()).append(',');
                        }
                        if (!store.getEndpoint().equals(apiStore.getEndpoint())
                            || !store.getType().equals(apiStore.getType())
                            || !store.getDisplayName().equals(apiStore.getDisplayName())) {
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
            }
            if (!publishedToStore) {  //If the API has not yet published to selected external APIStore
                notPublishedAPIStores.add(APIUtil.getExternalAPIStore(apiStore.getName(), tenantId));
            }
        }
        //Publish API to external APIStore which are not yet published
        try {
            publishToExternalAPIStores(api, notPublishedAPIStores, apiOlderVersionExist);
        } catch (APIManagementException e) {
            handleException("Failed to publish API to external Store. ", e);
        }
        //Update the APIs which are already exist in the external APIStore
        updateAPIInExternalAPIStores(api,updateApiStores);
        updateExternalAPIStoresDetails(api.getId(),modifiedPublishedApiStores); //Update database saved published APIStore details,if there are any
        //modifications in api-manager.xml

        deleteFromExternalAPIStores(api, removedApiStores);
        if (failure) {
            throw new APIManagementException(errorStatus.substring(0, errorStatus.length() -2));
        }
        return true;
    }

    private void deleteFromExternalAPIStores(API api, Set<APIStore> removedApiStores)  throws APIManagementException {
        Set<APIStore> removalCompletedStores = new HashSet<APIStore>();
        StringBuilder errorStatus = new StringBuilder("Failed to delete from External Stores : ");
        boolean failure = false;
        for (APIStore store : removedApiStores) {
            org.wso2.carbon.apimgt.api.model.APIPublisher publisher =
                    APIUtil.getExternalAPIStore(store.getName(), tenantId).getPublisher();
            try {
                boolean deleted = publisher.deleteFromStore(
                        api.getId(), APIUtil.getExternalAPIStore(store.getName(), tenantId));
                if (deleted) {
                    // If the attempt is successful, database will be
                    // changed deleting the External store mappings.
                    removalCompletedStores.add(store);
                }
            } catch (APIManagementException e) {
                failure = true;
                log.error(e);
                errorStatus.append(store.getDisplayName()).append(',');
            }
        }
        if (!removalCompletedStores.isEmpty()) {
            removeExternalAPIStoreDetails(api.getId(), removalCompletedStores);
        }

        if (failure) {
            throw new APIManagementException(errorStatus.substring(0, errorStatus.length() - 2));
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
        if (apiStoreSet != null && !apiStoreSet.isEmpty()) {
            StringBuilder errorStatus = new StringBuilder("Failed to update External Stores : ");
            boolean failure = false;
            for (APIStore store : apiStoreSet) {
                try {
                    org.wso2.carbon.apimgt.api.model.APIPublisher publisher = store.getPublisher();
                    publisher.updateToStore(api, store);
                } catch (APIManagementException e) {
                    failure = true;
                    log.error(e);
                    errorStatus.append(store.getDisplayName()).append(',');
                }
            }

            if (failure) {
                throw new APIManagementException(errorStatus.substring(0, errorStatus.length() -2));
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
     * When enabled publishing to external APIStores support,get all the external apistore details which are
     * published and stored in db and which are not unpublished
     * @param apiId The API Identifier which need to update in db
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    @Override
    public Set<APIStore> getExternalAPIStores(APIIdentifier apiId)
            throws APIManagementException {
        if (APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            SortedSet<APIStore> sortedApiStores = new TreeSet<APIStore>(new APIStoreNameComparator());
            Set<APIStore> publishedStores = apiMgtDAO.getExternalAPIStoresDetails(apiId);
            sortedApiStores.addAll(publishedStores);
            return APIUtil.getExternalAPIStores(sortedApiStores, tenantId);
        } else {
            return null;
        }
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
        Set<APIStore> storesSet;
        SortedSet<APIStore> configuredAPIStores = new TreeSet<APIStore>(new APIStoreNameComparator());
        configuredAPIStores.addAll(APIUtil.getExternalStores(tenantId));
        if (APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            storesSet =  apiMgtDAO.getExternalAPIStoresDetails(apiId);
            //Retains only the stores that contained in configuration
            storesSet.retainAll(configuredAPIStores);
            return storesSet;
        } else {
            return null;
        }
    }

	/**
	 * Get stored custom inSequences from governanceSystem registry
	 *
	 * @throws APIManagementException
	 */

	public List<String> getCustomInSequences(APIIdentifier apiIdentifier) throws APIManagementException {

		List<String> sequenceList = new ArrayList<String>();
		boolean isTenantFlowStarted = false;
		try {
			String tenantDomain = null;
			if (apiIdentifier.getProviderName().contains("-AT-")) {
				String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
				tenantDomain = MultitenantUtils.getTenantDomain(provider);
			}
			PrivilegedCarbonContext.startTenantFlow();
			isTenantFlowStarted = true;
			if (!StringUtils.isEmpty(tenantDomain)) {
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			} else {
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
						(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
			}
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
	            org.wso2.carbon.registry.api.Collection inSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION);
	            if (inSeqCollection != null) {
	                String[] inSeqChildPaths = inSeqCollection.getChildren();
                    for (String inSeqChildPath : inSeqChildPaths)    {
                        Resource inSequence = registry.get(inSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(inSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

            String customInSeqFileLocation = APIUtil.getSequencePath(apiIdentifier, "in");

            if(registry.resourceExists(customInSeqFileLocation))    {
                org.wso2.carbon.registry.api.Collection inSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(customInSeqFileLocation);
                if (inSeqCollection != null) {
                    String[] inSeqChildPaths = inSeqCollection.getChildren();
                    for (String inSeqChildPath : inSeqChildPaths)    {
                        Resource inSequence = registry.get(inSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(inSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }
		} catch (Exception e) {
			handleException("Issue is in getting custom InSequences from the Registry", e);
		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
		return sequenceList;
	}

	/**
	 * Get stored custom outSequences from governanceSystem registry
	 *
	 * @throws APIManagementException
	 */

	public List<String> getCustomOutSequences(APIIdentifier apiIdentifier) throws APIManagementException {

		List<String> sequenceList = new ArrayList<String>();
		boolean isTenantFlowStarted = false;
		try {
			String tenantDomain = null;
			if (apiIdentifier.getProviderName().contains("-AT-")) {
				String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
				tenantDomain = MultitenantUtils.getTenantDomain(provider);
			}
			PrivilegedCarbonContext.startTenantFlow();
			isTenantFlowStarted = true;
			if (!StringUtils.isEmpty(tenantDomain)) {
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			} else {
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
						(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
			}
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)) {
	            org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
	            if (outSeqCollection !=null) {
	                String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String childPath : outSeqChildPaths)   {
                        Resource outSequence = registry.get(childPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

            String customOutSeqFileLocation = APIUtil.getSequencePath(apiIdentifier, "out");

            if(registry.resourceExists(customOutSeqFileLocation))    {
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(customOutSeqFileLocation);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String outSeqChildPath : outSeqChildPaths)    {
                        Resource outSequence = registry.get(outSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

		} catch (Exception e) {
			handleException("Issue is in getting custom OutSequences from the Registry", e);
		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
		return sequenceList;
	}

    /**
     * Get the list of Custom InSequences including API defined in sequences.
     * @return List of available sequences
     * @throws APIManagementException
     */
    public List<String> getCustomInSequences()  throws APIManagementException {
        List<String> sequenceList = new ArrayList<String>();
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
                org.wso2.carbon.registry.api.Collection faultSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION);
                if (faultSeqCollection !=null) {
                    String[] faultSeqChildPaths = faultSeqCollection.getChildren();
                    for (String faultSeqChildPath : faultSeqChildPaths) {
                        Resource outSequence = registry.get(faultSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }

                }
            }

        }  catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        }
        return sequenceList;
    }


    /**
     * Get the list of Custom InSequences including API defined in sequences.
     * @return List of available sequences
     * @throws APIManagementException
     */
    public List<String> getCustomOutSequences()  throws APIManagementException {
        List<String> sequenceList = new ArrayList<String>();
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)) {
                org.wso2.carbon.registry.api.Collection faultSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
                if (faultSeqCollection !=null) {
                    String[] faultSeqChildPaths = faultSeqCollection.getChildren();
                    for (String faultSeqChildPath : faultSeqChildPaths) {
                        Resource outSequence = registry.get(faultSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }

                }
            }

        }  catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        }
        return sequenceList;
    }

    /**
     * Get stored custom fault sequences from governanceSystem registry
     *
     * @throws APIManagementException
     */
    @Deprecated
    public List<String> getCustomFaultSequences() throws APIManagementException {

        List<String> sequenceList = new ArrayList<String>();
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION)) {
                org.wso2.carbon.registry.api.Collection faultSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION);
                if (faultSeqCollection !=null) {
                    String[] faultSeqChildPaths = faultSeqCollection.getChildren();
                    for (String faultSeqChildPath : faultSeqChildPaths) {
                        Resource outSequence = registry.get(faultSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }

                }
            }

        }  catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        }
        return sequenceList;
    }

    /**
     * Get stored custom fault sequences from governanceSystem registry
     *
     * @throws APIManagementException
     */

    public List<String> getCustomFaultSequences(APIIdentifier apiIdentifier) throws APIManagementException {

        List<String> sequenceList = new ArrayList<String>();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = null;
            if (apiIdentifier.getProviderName().contains("-AT-")) {
                String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
                tenantDomain = MultitenantUtils.getTenantDomain(provider);
            }
            PrivilegedCarbonContext.startTenantFlow();
            isTenantFlowStarted = true;
            if (!StringUtils.isEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                        (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION)) {
                org.wso2.carbon.registry.api.Collection faultSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(
                                                                       APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION);
                if (faultSeqCollection !=null) {
                    String[] faultSeqChildPaths = faultSeqCollection.getChildren();
                    for (String faultSeqChildPath : faultSeqChildPaths) {
                        Resource outSequence = registry.get(faultSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }

                }
            }

            String customOutSeqFileLocation = APIUtil.getSequencePath(apiIdentifier,
                                                                      APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT);

            if(registry.resourceExists(customOutSeqFileLocation))    {
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(customOutSeqFileLocation);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String outSeqChildPath : outSeqChildPaths)    {
                        Resource outSequence = registry.get(outSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

        } catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT
                                                            + " sequences of " + apiIdentifier + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return sequenceList;
    }


    /**
     * Get the list of Custom in sequences of API.
     * @return List of in sequences
     * @throws APIManagementException
     */

    public List<String> getCustomApiInSequences(APIIdentifier apiIdentifier)  throws APIManagementException {
        List<String> sequenceList = new ArrayList<String>();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = null;
            if (apiIdentifier.getProviderName().contains("-AT-")) {
                String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
                tenantDomain = MultitenantUtils.getTenantDomain(provider);
            }
            PrivilegedCarbonContext.startTenantFlow();
            isTenantFlowStarted = true;
            if (!StringUtils.isEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                        (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            String customOutSeqFileLocation = APIUtil.getSequencePath(apiIdentifier,
                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
            if(registry.resourceExists(customOutSeqFileLocation))    {
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(customOutSeqFileLocation);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String outSeqChildPath : outSeqChildPaths)    {
                        Resource outSequence = registry.get(outSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN
                    + " sequences of " + apiIdentifier + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return sequenceList;
    }

    /**
     * Get the list of Custom out Sequences of API
     *
     * @return List of available out sequences
     * @throws APIManagementException
     */

    public List<String> getCustomApiOutSequences(APIIdentifier apiIdentifier)  throws APIManagementException {
        List<String> sequenceList = new ArrayList<String>();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = null;
            if (apiIdentifier.getProviderName().contains("-AT-")) {
                String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
                tenantDomain = MultitenantUtils.getTenantDomain(provider);
            }
            PrivilegedCarbonContext.startTenantFlow();
            isTenantFlowStarted = true;
            if (!StringUtils.isEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                        (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            String customOutSeqFileLocation = APIUtil.getSequencePath(apiIdentifier,
                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT);
            if(registry.resourceExists(customOutSeqFileLocation))    {
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(customOutSeqFileLocation);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String outSeqChildPath : outSeqChildPaths)    {
                        Resource outSequence = registry.get(outSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT
                    + " sequences of " + apiIdentifier + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return sequenceList;
    }

    /**
     * Get the list of Custom Fault Sequences of API.
     *
     * @return List of available fault sequences
     * @throws APIManagementException
     */
    public List<String> getCustomApiFaultSequences(APIIdentifier apiIdentifier)  throws APIManagementException {
        List<String> sequenceList = new ArrayList<String>();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = null;
            if (apiIdentifier.getProviderName().contains("-AT-")) {
                String provider = apiIdentifier.getProviderName().replace("-AT-", "@");
                tenantDomain = MultitenantUtils.getTenantDomain(provider);
            }
            PrivilegedCarbonContext.startTenantFlow();
            isTenantFlowStarted = true;
            if (!StringUtils.isEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain
                        (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            String customOutSeqFileLocation = APIUtil.getSequencePath(apiIdentifier,
                    APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT);
            if(registry.resourceExists(customOutSeqFileLocation))    {
                org.wso2.carbon.registry.api.Collection outSeqCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.get(customOutSeqFileLocation);
                if (outSeqCollection != null) {
                    String[] outSeqChildPaths = outSeqCollection.getChildren();
                    for (String outSeqChildPath : outSeqChildPaths)    {
                        Resource outSequence = registry.get(outSeqChildPath);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT
                    + " sequences of " + apiIdentifier + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return sequenceList;
    }

    /**
     * This method is used to initiate the web service calls and cluster messages related to stats publishing status
     *
     * @param receiverUrl   event receiver url
     * @param user          username of the event receiver
     * @param password      password of the event receiver
     * @param updatedStatus status of the stat publishing state
     */
    public void callStatUpdateService(String receiverUrl, String user, String password, boolean updatedStatus) {

        //all mandatory parameters should not be null in order to start the process
        if (receiverUrl != null && user != null && password != null) {

            if (log.isDebugEnabled()) {
                log.debug("Updating Stats publishing status of Store/Publisher domain to : " + updatedStatus);
            }

            //get the cluster message agent to publisher-store domain
            ClusteringAgent clusteringAgent = ServiceReferenceHolder.getContextService().getServerConfigContext().
                    getAxisConfiguration().getClusteringAgent();

            if (clusteringAgent != null) {
                //changing stat publishing status at other nodes via a cluster message
                try {
                    clusteringAgent.sendMessage(new StatUpdateClusterMessage(updatedStatus,receiverUrl,user,password), true);
                } catch (ClusteringFault clusteringFault) {
                    //error is only logged because initially gateway has modified the status
                    String errorMessage = "Failed to send cluster message to Publisher/Store domain and " +
                            "update stats publishing status.";
                    log.error(errorMessage, clusteringFault);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Successfully updated Stats publishing status to : " + updatedStatus);
                }
            }

            Map<String, Environment> gatewayEnvironments = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration().getApiGatewayEnvironments();

            Set gatewayEntries = gatewayEnvironments.entrySet();
            Iterator<Map.Entry<String,Environment>> gatewayIterator = gatewayEntries.iterator();

            while(gatewayIterator.hasNext()){

                Environment currentGatewayEnvironment = gatewayIterator.next().getValue();
                String gatewayServiceUrl = currentGatewayEnvironment.getServerURL();
                String gatewayUserName = currentGatewayEnvironment.getUserName();
                String gatewayPassword = currentGatewayEnvironment.getPassword();

                try {
                    //get the stub and the call the admin service with the credentials
                    GatewayStatsUpdateServiceStub stub =
                            new GatewayStatsUpdateServiceStub(gatewayServiceUrl + APIConstants.GATEWAY_STATS_SERVICE);
                    ServiceClient gatewayServiceClient = stub._getServiceClient();
                    CarbonUtils.setBasicAccessSecurityHeaders(gatewayUserName, gatewayPassword, gatewayServiceClient);
                    stub.updateStatPublishGateway(receiverUrl, user, password, updatedStatus);
                } catch (AxisFault e) {
                    //error is only logged because the process should be executed in all gateway environments
                    log.error("Error in calling Stats update web service in Gateway Environment : " +
                            currentGatewayEnvironment.getName(), e);
                } catch (RemoteException e) {
                    //error is only logged because the change is affected in gateway environments,
                    // and the process should be executed in all environments and domains
                    log.error("Error in updating Stats publish status in Gateway : " +
                            currentGatewayEnvironment.getName(), e);
                } catch (GatewayStatsUpdateServiceAPIManagementExceptionException e) {
                    //error is only logged because the process should continue in other gateways
                    log.error("Error in Stat Update web service call to Gateway : " +
                            currentGatewayEnvironment.getName(), e);
                } catch (GatewayStatsUpdateServiceClusteringFaultException e) {
                    //error is only logged because the status should be updated in other gateways
                    log.error("Failed to send cluster message to update stats publishing status in Gateway : " +
                            currentGatewayEnvironment.getName(), e);
                } catch (GatewayStatsUpdateServiceExceptionException e) {
                    //error is only logged because the process should continue in other gateways
                    log.error("Updating EventingConfiguration failed, a dirty Stat publishing status exists in : " +
                            currentGatewayEnvironment.getName(), e);
                }
            }
        } else {
            //if at least one mandatory parameter is null, the process is not initiated
            log.error("Event receiver URL and username and password all should not be null.");
        }
    }

	@Override
	public boolean isSynapseGateway() throws APIManagementException {
		APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
		String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
        return APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType);
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

    @Override
    public void saveSwagger20Definition(APIIdentifier apiId, String jsonText) throws APIManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            definitionFromSwagger20.saveAPIDefinition(getAPI(apiId), jsonText, registry);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public APIStateChangeResponse changeLifeCycleStatus(APIIdentifier apiIdentifier, String action)
            throws APIManagementException, FaultGatewaysException{
        
        APIStateChangeResponse response = new APIStateChangeResponse();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain, true);

            GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);        
            String targetStatus;
            if (apiArtifact != null) {
                           
                String providerName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
                String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
                String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);                
                String currentStatus = apiArtifact.getLifecycleState();
                
                int apiId = apiMgtDAO.getAPIID(apiIdentifier, null);
                
                WorkflowStatus apiWFState = null;
                WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                        WorkflowConstants.WF_TYPE_AM_API_STATE);
                if (wfDTO != null) {
                    apiWFState = wfDTO.getStatus();
                }

                // if the workflow has started, then executor should not fire again
                if (!WorkflowStatus.CREATED.equals(apiWFState)) {

                    try {
                        WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance()
                                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
                        WorkflowExecutor apiStateWFExecutor = WorkflowExecutorFactory.getInstance()
                                .getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);
                        APIStateWorkflowDTO apiStateWorkflow = new APIStateWorkflowDTO();
                        apiStateWorkflow.setApiCurrentState(currentStatus);
                        apiStateWorkflow.setApiLCAction(action);
                        apiStateWorkflow.setApiName(apiName);
                        apiStateWorkflow.setApiVersion(apiVersion);
                        apiStateWorkflow.setApiProvider(providerName);
                        apiStateWorkflow.setCallbackUrl(workflowProperties.getWorkflowCallbackAPI());
                        apiStateWorkflow.setExternalWorkflowReference(apiStateWFExecutor.generateUUID());
                        apiStateWorkflow.setTenantId(tenantId);
                        apiStateWorkflow.setTenantDomain(this.tenantDomain);
                        apiStateWorkflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_API_STATE);
                        apiStateWorkflow.setStatus(WorkflowStatus.CREATED);
                        apiStateWorkflow.setCreatedTime(System.currentTimeMillis());
                        apiStateWorkflow.setWorkflowReference(Integer.toString(apiId));
                        apiStateWorkflow.setInvoker(this.username);
                        String workflowDescription = "Pending lifecycle state change action: " + action;
                        apiStateWorkflow.setWorkflowDescription(workflowDescription);
                       
                        WorkflowResponse workflowResponse = apiStateWFExecutor.execute(apiStateWorkflow);
                        response.setWorkflowResponse(workflowResponse);
                    } catch (Exception e) {
                        handleException("Failed to execute workflow for life cycle status change : " + e.getMessage(),
                                e);
                    }

                    // get the workflow state once the executor is executed.
                    wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                            WorkflowConstants.WF_TYPE_AM_API_STATE);
                    if (wfDTO != null) {
                        apiWFState = wfDTO.getStatus();
                        response.setStateChangeStatus(apiWFState.toString());
                    }
                }
                
                // only change the lifecycle if approved
                // apiWFState is null when simple wf executor is used because wf state is not stored in the db. 
                if (WorkflowStatus.APPROVED.equals(apiWFState) || apiWFState == null) {
                    targetStatus = "";
                    apiArtifact.invokeAction(action, APIConstants.API_LIFE_CYCLE);
                    targetStatus = apiArtifact.getLifecycleState();
                    if (!currentStatus.equals(targetStatus)) {
                        apiMgtDAO.recordAPILifeCycleEvent(apiIdentifier, currentStatus.toUpperCase(),
                                targetStatus.toUpperCase(), this.username, this.tenantId);
                    }

                    JSONObject apiLogObject = new JSONObject();
                    apiLogObject.put(APIConstants.AuditLogConstants.NAME, apiIdentifier.getApiName());
                    apiLogObject.put(APIConstants.AuditLogConstants.VERSION, apiIdentifier.getVersion());
                    apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, apiIdentifier.getProviderName());
                    apiLogObject.put("New life cycle status", targetStatus);

                    APIUtil.logAuditMessage(APIConstants.AuditLogConstants.API, apiLogObject.toString(),
                            APIConstants.AuditLogConstants.UPDATED, this.username);

                    if (log.isDebugEnabled()) {
                        String logMessage = "API Status changed successfully. API Name: " + apiIdentifier.getApiName()
                                + ", API Version " + apiIdentifier.getVersion() + ", New Status : " + targetStatus;
                        log.debug(logMessage);
                    }
                    return response;
                }
            }
        } catch (GovernanceException e) {
            String cause = e.getCause().getMessage();
            if (!StringUtils.isEmpty(cause)) {
                if (cause.contains("FaultGatewaysException:")) {
                    Map<String, Map<String, String>> faultMap = new HashMap<String, Map<String, String>>();
                    String faultJsonString;
                    if (!StringUtils.isEmpty(cause) && cause.split("FaultGatewaysException:").length > 1) {
                        faultJsonString = cause.split("FaultGatewaysException:")[1];
                        try {
                            JSONObject faultGatewayJson = (JSONObject) new JSONParser().parse(faultJsonString);
                            faultMap.putAll(faultGatewayJson);
                            throw new FaultGatewaysException(faultMap);
                        } catch (ParseException e1) {
                            log.error("Couldn't parse the Failed Environment json", e);
                            handleException("Couldn't parse the Failed Environment json : " + e.getMessage(), e);
                        }
                    }
                } else if (cause.contains("APIManagementException:")) {
                    // This exception already logged from APIExecutor class hence this no need to logged again
                    handleException(
                            "Failed to change the life cycle status : " + cause.split("APIManagementException:")[1], e);
                } else {
                    /* This exception already logged from APIExecutor class hence this no need to logged again
                    This block handles the all the exception which not have custom cause message*/
                    handleException("Failed to change the life cycle status : " + e.getMessage(), e);
                }
            }
            return response;
        }
         finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return response;
    }

    @Override
    public boolean changeAPILCCheckListItems(APIIdentifier apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException {

        String providerTenantMode = apiIdentifier.getProviderName();

        boolean success = false;
        boolean isTenantFlowStarted = false;
        try {

            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);
            String status = null;
            try {
                if (apiArtifact != null) {
                    if (checkItemValue && !apiArtifact.isLCItemChecked(checkItem, APIConstants.API_LIFE_CYCLE)) {
                        apiArtifact.checkLCItem(checkItem, APIConstants.API_LIFE_CYCLE);
                        status ="checked";
                    } else if (!checkItemValue && apiArtifact.isLCItemChecked(checkItem, APIConstants.API_LIFE_CYCLE)) {
                        apiArtifact.uncheckLCItem(checkItem, APIConstants.API_LIFE_CYCLE);
                        status = "unchecked";
                    }
                    success = true;
                }
                String[] checkListItems = apiArtifact.getAllCheckListItemNames(APIConstants.API_LIFE_CYCLE);
                String checkListItem = null;
                if(checkListItems != null && checkItem >= 0 && checkItem < checkListItems.length) {
                    checkListItem = checkListItems[checkItem];
                }
                JSONObject apiLogObject = new JSONObject();
                apiLogObject.put(APIConstants.AuditLogConstants.NAME, apiIdentifier.getApiName());
                apiLogObject.put(APIConstants.AuditLogConstants.VERSION, apiIdentifier.getVersion());
                apiLogObject.put(APIConstants.AuditLogConstants.PROVIDER, apiIdentifier.getProviderName());
                apiLogObject.put(checkListItem + " checkbox has been ", status);

                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.LIFE_CYCLE, apiLogObject.toString(),
                        APIConstants.AuditLogConstants.UPDATED, this.username);
            } catch (GovernanceException e) {
                handleException("Error while setting registry lifecycle checklist items for the API: " +
                        apiIdentifier.getApiName(), e);
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return success;
    }

    /**
     * This method is to set a lifecycle check list item given the APIIdentifier and the checklist item name.
     * If the given item not in the allowed lifecycle check items list or item is already checked, this will stay
     * silent and return false. Otherwise, the checklist item will be updated and returns true.
     *
     * @param apiIdentifier APIIdentifier
     * @param checkItemName Name of the checklist item
     * @param checkItemValue Value to be set to the checklist item
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    @Override
    public boolean checkAndChangeAPILCCheckListItem(APIIdentifier apiIdentifier, String checkItemName,
            boolean checkItemValue)
            throws APIManagementException {
        Map<String, Object> lifeCycleData = getAPILifeCycleData(apiIdentifier);
        if (lifeCycleData != null && lifeCycleData.get(APIConstants.LC_CHECK_ITEMS) != null && lifeCycleData
                .get(APIConstants.LC_CHECK_ITEMS) instanceof ArrayList) {
            List checkListItems = (List) lifeCycleData.get(APIConstants.LC_CHECK_ITEMS);
            for (Object item : checkListItems) {
                if (item instanceof CheckListItem) {
                    CheckListItem checkListItem = (CheckListItem) item;
                    int index = Integer.parseInt(checkListItem.getOrder());
                    if (checkListItem.getName().equals(checkItemName)) {
                        changeAPILCCheckListItems(apiIdentifier, index, checkItemValue);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    /*
    * This method returns the lifecycle data for an API including current state,next states.
    *
    * @param apiId APIIdentifier
    * @return Map<String,Object> a map with lifecycle data
    */
    public Map<String, Object> getAPILifeCycleData(APIIdentifier apiId) throws APIManagementException {
        String path = APIUtil.getAPIPath(apiId);
        Map<String, Object> lcData = new HashMap<String, Object>();


        String providerTenantMode = apiId.getProviderName();

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            Resource apiSourceArtifact = registry.get(path);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());
            //Get all the actions corresponding to current state of the api artifact
            String[] actions = artifact.getAllLifecycleActions(APIConstants.API_LIFE_CYCLE);
            //Put next states into map
            lcData.put(APIConstants.LC_NEXT_STATES, actions);
            String lifeCycleState = artifact.getLifecycleState();
            LifecycleBean bean;

            bean = LifecycleBeanPopulator.getLifecycleBean(path, (UserRegistry) registry, configRegistry);
            if (bean != null) {
                ArrayList<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
                ArrayList<String> permissionList = new ArrayList<String>();
                //Get lc properties
                Property[] lifecycleProps = bean.getLifecycleProperties();
                //Get roles of the current session holder
                String[] roleNames = bean.getRolesOfUser();
                for (Property property : lifecycleProps) {
                    String propName = property.getKey();
                    String[] propValues = property.getValues();
                    //Check for permission properties if any exists
                    if (propValues != null && propValues.length != 0) {
                        if (propName.startsWith(APIConstants.LC_PROPERTY_CHECKLIST_PREFIX) &&
                                propName.endsWith(APIConstants.LC_PROPERTY_PERMISSION_SUFFIX) &&
                                propName.contains(APIConstants.API_LIFE_CYCLE)) {
                            for (String role : roleNames) {
                                for (String propValue : propValues) {
                                    String key = propName.replace(APIConstants.LC_PROPERTY_CHECKLIST_PREFIX, "")
                                                 .replace(APIConstants.LC_PROPERTY_PERMISSION_SUFFIX, "");
                                    if (propValue.equals(role)) {
                                        permissionList.add(key);
                                    } else if (propValue.startsWith(APIConstants.LC_PROPERTY_CHECKLIST_PREFIX) &&
                                               propValue.endsWith(APIConstants.LC_PROPERTY_PERMISSION_SUFFIX)) {
                                        permissionList.add(key);
                                    }
                                }
                            }
                        }
                    }
                }
                //Check for lifecycle checklist item properties defined
                for (Property property : lifecycleProps) {
                    String propName = property.getKey();
                    String[] propValues = property.getValues();

                    if (propValues != null && propValues.length != 0) {

                        CheckListItem checkListItem = new CheckListItem();
                        checkListItem.setVisible("false");
                        if (propName.startsWith(APIConstants.LC_PROPERTY_CHECKLIST_PREFIX) &&
                                propName.endsWith(APIConstants.LC_PROPERTY_ITEM_SUFFIX) &&
                                propName.contains(APIConstants.API_LIFE_CYCLE)) {
                            if (propValues.length > 2) {
                                for (String param : propValues) {
                                    if (param.startsWith(APIConstants.LC_STATUS)) {
                                        checkListItem.setLifeCycleStatus(param.substring(7));
                                    } else if (param.startsWith(APIConstants.LC_CHECK_ITEM_NAME)) {
                                        checkListItem.setName(param.substring(5));
                                    } else if (param.startsWith(APIConstants.LC_CHECK_ITEM_VALUE)) {
                                        checkListItem.setValue(param.substring(6));
                                    } else if (param.startsWith(APIConstants.LC_CHECK_ITEM_ORDER)) {
                                        checkListItem.setOrder(param.substring(6));
                                    }
                                }
                            }

                            String key = propName.replace(APIConstants.LC_PROPERTY_CHECKLIST_PREFIX, "").
                                    replace(APIConstants.LC_PROPERTY_ITEM_SUFFIX, "");
                            if (permissionList.contains(key)) { //Set visible to true if the checklist item permits
                                checkListItem.setVisible("true");
                            }
                        }

                        if (checkListItem.matchLifeCycleStatus(lifeCycleState)) {
                            checkListItems.add(checkListItem);
                        }
                    }
                }
                lcData.put("items", checkListItems);
            }
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return lcData;
    }

    @Override
    public String getAPILifeCycleStatus(APIIdentifier apiIdentifier) throws APIManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(this.tenantDomain, true);
            GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);
            return apiArtifact.getLifecycleState();
        } catch (GovernanceException e) {
            handleException("Failed to get the life cycle status : " + e.getMessage(), e);
            return null;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public Map<String, Object> getAllPaginatedAPIs(String tenantDomain, int start, int end)
            throws APIManagementException {
        Map<String, Object> result = new HashMap<String, Object>();
        List<API> apiSortedList = new ArrayList<API>();
        int totalLength = 0;
        boolean isTenantFlowStarted = false;

        try {
            String paginationLimit = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                                           .getAPIManagerConfiguration()
                                                           .getFirstProperty(APIConstants.API_PUBLISHER_APIS_PER_PAGE);

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
                    log.warn(
                            "Value of '" + APIConstants.API_PUBLISHER_APIS_PER_PAGE + "' is too low, defaulting to 11");
                }

                maxPaginationLimit = start + pagination + 1;
            }
            // Else if the config is not specifed we go with default functionality and load all
            else {
                maxPaginationLimit = Integer.MAX_VALUE;
            }
            Registry userRegistry;
            boolean isTenantMode = (tenantDomain != null);
            if ((isTenantMode && this.tenantDomain == null) ||
                (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    isTenantFlowStarted = true;
                }
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                     .getTenantId(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
                userRegistry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                                                                       tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                       .setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            } else {
                userRegistry = registry;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            }
            PaginationContext.init(start, end, "ASC", APIConstants.PROVIDER_OVERVIEW_NAME, maxPaginationLimit);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(userRegistry, APIConstants.API_KEY);
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();

            if (artifactManager != null) {
                GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
                totalLength = PaginationContext.getInstance().getLength();
                if (genericArtifacts == null || genericArtifacts.length == 0) {
                    result.put("apis", apiSortedList);
                    result.put("totalLength", totalLength);
                    return result;
                }
                // Check to see if we can speculate that there are more APIs to be loaded
                if (maxPaginationLimit == totalLength) {
                    // performance hit
                    --totalLength; // Remove the additional 1 we added earlier when setting max pagination limit
                }
                int tempLength = 0;
                for (GenericArtifact artifact : genericArtifacts) {

                    API api = APIUtil.getAPI(artifact);

                    if (api != null) {
                        apiSortedList.add(api);
                    }
                    tempLength++;
                    if (tempLength >= totalLength) {
                        break;
                    }
                }
                Collections.sort(apiSortedList, new APINameComparator());
            }

        } catch (RegistryException e) {
            handleException("Failed to get all APIs", e);
        } catch (UserStoreException e) {
            handleException("Failed to get all APIs", e);
        } finally {
            PaginationContext.destroy();
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        result.put("apis", apiSortedList);
        result.put("totalLength", totalLength);
        return result;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    /**
     * Deploy policy to global CEP and persist the policy object
     *
     * @param policy policy object
     */
    public void addPolicy(Policy policy) throws APIManagementException {

        ThrottlePolicyDeploymentManager manager = ThrottlePolicyDeploymentManager.getInstance();
        ThrottlePolicyTemplateBuilder policyBuilder = new ThrottlePolicyTemplateBuilder();
        Map<String, String> executionFlows = new HashMap<String, String>();
        String policyLevel = null;

        try {
            if (policy instanceof APIPolicy) {
                APIPolicy apiPolicy = (APIPolicy) policy;
                apiPolicy.setUserLevel(PolicyConstants.ACROSS_ALL);
                apiPolicy = apiMgtDAO.addAPIPolicy(apiPolicy);
                executionFlows = policyBuilder.getThrottlePolicyForAPILevel(apiPolicy);
                String defaultPolicy = policyBuilder.getThrottlePolicyForAPILevelDefault(apiPolicy);
                String policyFile = apiPolicy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + apiPolicy.getPolicyName();
                String defaultPolicyName = policyFile + "_default";
                executionFlows.put(defaultPolicyName, defaultPolicy);
                policyLevel = PolicyConstants.POLICY_LEVEL_API;
            } else if (policy instanceof ApplicationPolicy) {
                ApplicationPolicy appPolicy = (ApplicationPolicy) policy;
                String policyString = policyBuilder.getThrottlePolicyForAppLevel(appPolicy);
                String policyFile = appPolicy.getTenantDomain() + "_" +PolicyConstants.POLICY_LEVEL_APP + "_" + appPolicy.getPolicyName();
                executionFlows.put(policyFile, policyString);
                apiMgtDAO.addApplicationPolicy(appPolicy);
                policyLevel = PolicyConstants.POLICY_LEVEL_APP;
            } else if (policy instanceof SubscriptionPolicy) {
                SubscriptionPolicy subPolicy = (SubscriptionPolicy) policy;
                String policyString = policyBuilder.getThrottlePolicyForSubscriptionLevel(subPolicy);
                String policyFile = subPolicy.getTenantDomain() + "_" +PolicyConstants.POLICY_LEVEL_APP + "_" + subPolicy.getPolicyName();
                executionFlows.put(policyFile, policyString);
                apiMgtDAO.addSubscriptionPolicy(subPolicy);
                policyLevel = PolicyConstants.POLICY_LEVEL_SUB;
            } else if (policy instanceof GlobalPolicy) {
                GlobalPolicy globalPolicy = (GlobalPolicy) policy;
                String policyString = policyBuilder.getThrottlePolicyForGlobalLevel(globalPolicy);

                // validating custom execution plan
                if (!manager.validateExecutionPlan(policyString)) {
                    throw new APIManagementException("Invalid Execution Plan");
                }

                // checking if keytemplate already exist
                if (apiMgtDAO.isKeyTemplatesExist(globalPolicy)) {
                    throw new APIManagementException("Key Template Already Exist");
                }

                String policyFile = PolicyConstants.POLICY_LEVEL_GLOBAL + "_" + globalPolicy.getPolicyName();
                executionFlows.put(policyFile, policyString);

                apiMgtDAO.addGlobalPolicy(globalPolicy);

                publishKeyTemplateEvent(globalPolicy.getKeyTemplate(),"add");
                policyLevel = PolicyConstants.POLICY_LEVEL_GLOBAL;
            } else {
                String msg = "Policy type " + policy.getClass().getName() + " is not supported";
                log.error(msg);
                throw new UnsupportedPolicyTypeException(msg);
            }
        } catch (APITemplateException e) {
            handleException("Error while generating policy", e);
        }

        // deploy in global cep and gateway manager
        try {
            Iterator iterator = executionFlows.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                String policyName  = entry.getKey();
                String flowString  = entry.getValue();
                manager.deployPolicyToGlobalCEP(flowString);
            }
            apiMgtDAO.setPolicyDeploymentStatus(policyLevel, policy.getPolicyName(), policy.getTenantId(), true);
            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, policy.getPolicyName());
            apiLogObject.put("Policy Level ", policyLevel);
            apiLogObject.put("Tenant ID", policy.getTenantId());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.POLICY, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.CREATED, this.username);
        } catch (APIManagementException e) {
            String msg = "Error while deploying policy";
            // Add deployment fail flag to database and throw the exception
            apiMgtDAO.setPolicyDeploymentStatus(policyLevel, policy.getPolicyName(), policy.getTenantId(), false);
            throw new PolicyDeploymentFailureException(msg, e);
        }
    }

    public void updatePolicy(Policy policy) throws APIManagementException {

        ThrottlePolicyDeploymentManager deploymentManager = ThrottlePolicyDeploymentManager.getInstance();
        ThrottlePolicyTemplateBuilder policyBuilder = new ThrottlePolicyTemplateBuilder();
        Map<String, String> executionFlows = new HashMap<String, String>();
        String policyLevel = null;
        String oldKeyTemplate = null;
        String newKeyTemplate = null;
        String policyName = policy.getPolicyName();
        List<String> policiesToUndeploy = new ArrayList<String>();
        try {
            if (policy instanceof APIPolicy) {
                APIPolicy apiPolicy = (APIPolicy) policy;
                apiPolicy.setUserLevel(PolicyConstants.ACROSS_ALL);
                //TODO this has done due to update policy method not deleting the second level entries when delete on cascade
                //TODO Need to fix appropriately
                List<Pipeline> pipelineList = apiPolicy.getPipelines();
                if (pipelineList != null && pipelineList.size() != 0) {
                    Iterator<Pipeline> pipelineIterator = pipelineList.iterator();
                    while (pipelineIterator.hasNext()) {
                        Pipeline pipeline = pipelineIterator.next();
                        if (!pipeline.isEnabled()) {
                            pipelineIterator.remove();
                        } else {
                            if (pipeline.getConditions() != null && pipeline.getConditions().size() != 0) {
                                Iterator<Condition> conditionIterator = pipeline.getConditions().iterator();
                                while (conditionIterator.hasNext()) {
                                    Condition condition = conditionIterator.next();
                                    if (JavaUtils.isFalseExplicitly(condition.getConditionEnabled())) {
                                        conditionIterator.remove();
                                    }
                                }
                            } else {
                                pipelineIterator.remove();
                            }
                        }
                    }
                }
                APIPolicy existingPolicy = apiMgtDAO.getAPIPolicy(policy.getPolicyName(), policy.getTenantId());
                apiPolicy = apiMgtDAO.updateAPIPolicy(apiPolicy);
                executionFlows = policyBuilder.getThrottlePolicyForAPILevel(apiPolicy);
                String defaultPolicy = policyBuilder.getThrottlePolicyForAPILevelDefault(apiPolicy);
                //TODO rename level to  resource or appropriate name
                String policyFile = apiPolicy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + policyName;
                String defaultPolicyName = policyFile + "_default";
                executionFlows.put(defaultPolicyName, defaultPolicy);
                //add default policy file name
                policiesToUndeploy.add(defaultPolicyName);
                for (int i = 0; i < existingPolicy.getPipelines().size(); i++) {
                    policiesToUndeploy.add(policyFile + "_condition_" +  existingPolicy.getPipelines().get(i).getId());
                }
                policyLevel = PolicyConstants.POLICY_LEVEL_API;
            } else if (policy instanceof ApplicationPolicy) {
                ApplicationPolicy appPolicy = (ApplicationPolicy) policy;
                String policyString = policyBuilder.getThrottlePolicyForAppLevel(appPolicy);
                apiMgtDAO.updateApplicationPolicy(appPolicy);
                String policyFile = appPolicy.getTenantDomain() + "_" +PolicyConstants.POLICY_LEVEL_APP + "_" + policyName;
                executionFlows.put(policyFile, policyString);
                policiesToUndeploy.add(policyFile);
                policyLevel = PolicyConstants.POLICY_LEVEL_APP;
            } else if (policy instanceof SubscriptionPolicy) {
                SubscriptionPolicy subPolicy = (SubscriptionPolicy) policy;
                String policyString = policyBuilder.getThrottlePolicyForSubscriptionLevel(subPolicy);
                apiMgtDAO.updateSubscriptionPolicy(subPolicy);
                String policyFile = subPolicy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" + policyName;
                policiesToUndeploy.add(policyFile);
                executionFlows.put(policyFile, policyString);
                policyLevel = PolicyConstants.POLICY_LEVEL_SUB;
            } else if (policy instanceof GlobalPolicy) {
                GlobalPolicy globalPolicy = (GlobalPolicy) policy;
                String policyString = policyBuilder.getThrottlePolicyForGlobalLevel(globalPolicy);

                // validating custom execution plan
                if(!deploymentManager.validateExecutionPlan(policyString)){
                    throw new APIManagementException("Invalid Execution Plan");
                }
                // checking if keytemplate already exist for another policy
                if(apiMgtDAO.isKeyTemplatesExist(globalPolicy)){
                    throw new APIManagementException("Key Template Already Exist");
                }

                // getting key templates before updating database
                GlobalPolicy oldGlobalPolicy = apiMgtDAO.getGlobalPolicy(policy.getPolicyName());
                oldKeyTemplate = oldGlobalPolicy.getKeyTemplate();
                newKeyTemplate = globalPolicy.getKeyTemplate();

                apiMgtDAO.updateGlobalPolicy(globalPolicy);
                String policyFile = PolicyConstants.POLICY_LEVEL_GLOBAL + "_" + policyName;
                executionFlows.put(policyFile, policyString);
                policiesToUndeploy.add(policyFile);
                policyLevel = PolicyConstants.POLICY_LEVEL_GLOBAL;
            } else {
                String msg = "Policy type " + policy.getClass().getName() + " is not supported";
                log.error(msg);
                throw new UnsupportedPolicyTypeException(msg);
            }
        } catch (APITemplateException e) {
            handleException("Error while generating policy for update");
        }
        // Deploy in global cep and gateway manager
        try {
            /* If single pipeline fails to deploy then whole deployment should fail.
             * Therefore for loop is wrapped inside a try catch block
             */
            if(PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyLevel)) {
                for (String flowName : policiesToUndeploy) {
                    deploymentManager.undeployPolicyFromGlobalCEP(flowName);
                }
            }

            Iterator iterator = executionFlows.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                String policyPlanName  = pair.getKey();
                String flowString  = pair.getValue();
                deploymentManager.updatePolicyToGlobalCEP(policyPlanName, flowString);

                //publishing keytemplate after update
                if (oldKeyTemplate != null && newKeyTemplate != null) {
                    publishKeyTemplateEvent(oldKeyTemplate, "remove");
                    publishKeyTemplateEvent(newKeyTemplate, "add");
                }
            }

            apiMgtDAO.setPolicyDeploymentStatus(policyLevel, policy.getPolicyName(), policy.getTenantId(), true);
            JSONObject apiLogObject = new JSONObject();
            apiLogObject.put(APIConstants.AuditLogConstants.NAME, policy.getPolicyName());
            apiLogObject.put("Policy Level ", policyLevel);
            apiLogObject.put("Tenant ID", policy.getTenantId());

            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.POLICY, apiLogObject.toString(),
                    APIConstants.AuditLogConstants.UPDATED, this.username);
        } catch (APIManagementException e) {
            String msg = "Error while deploying policy to gateway";
            // Add deployment fail flag to database and throw the exception
            apiMgtDAO.setPolicyDeploymentStatus(policyLevel, policy.getPolicyName(), policy.getTenantId(), false);
            throw new PolicyDeploymentFailureException(msg, e);
        }
    }

    /**
     *
     * @param username username to recognize tenant
     * @param level policy level to be applied
     * @return
     * @throws APIManagementException
     */
    public String[] getPolicyNames(String username, String level) throws APIManagementException {
        String[] policyNames = apiMgtDAO.getPolicyNames(level, username);
        return policyNames;
    }

    /**
     * @param username    username to recognize the tenant
     * @param policyLevel policy level
     * @param policyName  name of the policy to be deleted
     * @throws APIManagementException
     */
    public void deletePolicy(String username, String policyLevel, String policyName) throws APIManagementException {
        int tenantID = APIUtil.getTenantId(username);
        List<String> policyFileNames = new ArrayList<String>();
        String policyFile = null;

        if (PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            //need to load whole policy object to get the pipelines
            APIPolicy policy = apiMgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId(username));

            //add default policy file name
            if (policy.isDeployed()) {
                policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" + policyName;
                policyFileNames.add(policyFile + "_default");
                for (Pipeline pipeline : policy.getPipelines()) {
                    policyFileNames.add(policyFile + "_condition_" + pipeline.getId());
                }
            }

        } else if (PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            ApplicationPolicy appPolicy = apiMgtDAO.getApplicationPolicy(policyName, tenantID);
            if (appPolicy.isDeployed()) {
                policyFile = appPolicy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_APP + "_" + policyName;
                policyFileNames.add(policyFile);
            }
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            SubscriptionPolicy subscriptionPolicy = apiMgtDAO.getSubscriptionPolicy(policyName, tenantID);
            if (subscriptionPolicy.isDeployed()) {
                policyFile = subscriptionPolicy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" +
                             policyName;
                policyFileNames.add(policyFile);
            }
        } else if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            GlobalPolicy globalPolicy = apiMgtDAO.getGlobalPolicy(policyName);
            if (globalPolicy.isDeployed()) {
                policyFile = PolicyConstants.POLICY_LEVEL_GLOBAL + "_" + policyName;
                policyFileNames.add(policyFile);
            }
        }

        ThrottlePolicyDeploymentManager manager = ThrottlePolicyDeploymentManager.getInstance();
        try {
            manager.undeployPolicyFromGatewayManager(policyFileNames.toArray(new String[policyFileNames.size()]));

        } catch (Exception e) {
            String msg = "Error while undeploying policy: ";
            log.error(msg, e);
            throw new APIManagementException(msg);
        }

        GlobalPolicy globalPolicy = null;
        if (PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            globalPolicy = apiMgtDAO.getGlobalPolicy(policyName);
        }
        //remove from database
        apiMgtDAO.removeThrottlePolicy(policyLevel, policyName, tenantID);

        JSONObject apiLogObject = new JSONObject();
        apiLogObject.put(APIConstants.AuditLogConstants.NAME, policyName);
        apiLogObject.put("Policy Level ", policyLevel);
        apiLogObject.put("Tenant ID", tenantID);

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.POLICY, apiLogObject.toString(),
                APIConstants.AuditLogConstants.DELETED, this.username);

        if (globalPolicy != null) {
            publishKeyTemplateEvent(globalPolicy.getKeyTemplate(), "remove");
        }
    }

    /**
     * Returns true if key template given by the global policy already exists.
     * But this check will exclude the policy represented by the policy name
     *
     * @param policy Global policy
     * @return true if Global policy key template already exists
     */
    public boolean isGlobalPolicyKeyTemplateExists(GlobalPolicy policy) throws APIManagementException {
        return apiMgtDAO.isKeyTemplatesExist(policy);
    }

    public boolean hasAttachments(String username, String policyName, String policyType)throws APIManagementException{
    	int tenantID = APIUtil.getTenantId(username);
    	String tenantDomain = MultitenantUtils.getTenantDomain(username);
    	String tenantDomainWithAt = username;
        if(APIUtil.getSuperTenantId() != tenantID){
        	tenantDomainWithAt = "@"+tenantDomain;
        }

        boolean hasSubscription = apiMgtDAO.hasSubscription(policyName, tenantDomainWithAt, policyType);
        return hasSubscription;
    }

    @Override
    public List<BlockConditionsDTO> getBlockConditions() throws APIManagementException {
        return apiMgtDAO.getBlockConditions(tenantDomain);
    }

    @Override
    public BlockConditionsDTO getBlockCondition(int conditionId) throws APIManagementException {
        return apiMgtDAO.getBlockCondition(conditionId);
    }

    @Override
    public BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException {
        BlockConditionsDTO blockCondition = apiMgtDAO.getBlockConditionByUUID(uuid);
        if (blockCondition == null) {
            handleBlockConditionNotFoundException("Block condition: " + uuid + " was not found.");
        }
        return blockCondition;
    }

    @Override
    public boolean updateBlockCondition(int conditionId, String state) throws APIManagementException {
        boolean updateState = apiMgtDAO.updateBlockConditionState(conditionId,state);
        BlockConditionsDTO blockConditionsDTO = apiMgtDAO.getBlockCondition(conditionId);
        if(updateState){
            publishBlockingEventUpdate(blockConditionsDTO);
        }
        return updateState;
    }

    @Override
    public boolean updateBlockConditionByUUID(String uuid, String state) throws APIManagementException {

        boolean updateState = apiMgtDAO.updateBlockConditionStateByUUID(uuid, state);
        BlockConditionsDTO blockConditionsDTO = apiMgtDAO.getBlockConditionByUUID(uuid);
        if (updateState && blockConditionsDTO != null) {
            publishBlockingEventUpdate(blockConditionsDTO);
        }
        return updateState;
    }

    @Override
    public String addBlockCondition(String conditionType, String conditionValue) throws APIManagementException {

        if (APIConstants.BLOCKING_CONDITIONS_IP.equals(conditionType)) {
            conditionValue = tenantDomain + ":" + conditionValue.trim();
        }
        String uuid = apiMgtDAO.addBlockConditions(conditionType, conditionValue, tenantDomain);

        if (uuid != null) {
            if (APIConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
                conditionValue = MultitenantUtils.getTenantAwareUsername(conditionValue);
                conditionValue = conditionValue + "@" + tenantDomain;
            }
            publishBlockingEvent(conditionType, conditionValue, "true");
        }

        return uuid;
    }

    @Override
    public boolean deleteBlockCondition(int conditionId) throws APIManagementException {

        BlockConditionsDTO blockCondition = apiMgtDAO.getBlockCondition(conditionId);
        boolean deleteState = apiMgtDAO.deleteBlockCondition(conditionId);
        if (deleteState && blockCondition != null) {
            unpublishBlockCondition(blockCondition);
        }
        return deleteState;
    }

    @Override
    public boolean deleteBlockConditionByUUID(String uuid) throws APIManagementException {
        boolean deleteState = false;
        BlockConditionsDTO blockCondition = apiMgtDAO.getBlockConditionByUUID(uuid);
        if (blockCondition != null) {
            deleteState = apiMgtDAO.deleteBlockCondition(blockCondition.getConditionId());
            if (deleteState) {
                unpublishBlockCondition(blockCondition);
            }
        }
        return deleteState;
    }

    /**
     * Unpublish a blocking condition.
     *
     * @param blockCondition Block Condition object
     */
    private void unpublishBlockCondition (BlockConditionsDTO blockCondition) {
        String blockingConditionType = blockCondition.getConditionType();
        String blockingConditionValue = blockCondition.getConditionValue();
        if(APIConstants.BLOCKING_CONDITIONS_USER.equalsIgnoreCase(blockingConditionType)) {
            blockingConditionValue = MultitenantUtils.getTenantAwareUsername(blockingConditionValue);
            blockingConditionValue = blockingConditionValue + "@" + tenantDomain;
        }
        publishBlockingEvent(blockingConditionType, blockingConditionValue, "delete");
    }

    @Override
    public APIPolicy getAPIPolicy(String username, String policyName) throws APIManagementException {
        return apiMgtDAO.getAPIPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException {
        APIPolicy policy = apiMgtDAO.getAPIPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Advanced Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String username, String policyName) throws APIManagementException {
        return apiMgtDAO.getApplicationPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException {
        ApplicationPolicy policy = apiMgtDAO.getApplicationPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Application Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String username, String policyName) throws APIManagementException {
        return apiMgtDAO.getSubscriptionPolicy(policyName, APIUtil.getTenantId(username));
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException {
        SubscriptionPolicy policy = apiMgtDAO.getSubscriptionPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Subscription Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    @Override
    public GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException {
        return apiMgtDAO.getGlobalPolicy(policyName);
    }

    @Override
    public GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException {
        GlobalPolicy policy = apiMgtDAO.getGlobalPolicyByUUID(uuid);
        if (policy == null) {
            handlePolicyNotFoundException("Global Policy: " + uuid + " was not found.");
        }
        return policy;
    }

    /**
     * Publishes the changes on blocking conditions.
     *
     * @param blockCondition Block Condition object
     * @throws APIManagementException
     */
    private void publishBlockingEventUpdate(BlockConditionsDTO blockCondition) throws APIManagementException {
        if (blockCondition != null) {
            String blockingConditionType = blockCondition.getConditionType();
            String blockingConditionValue = blockCondition.getConditionValue();
            if (APIConstants.BLOCKING_CONDITIONS_USER.equalsIgnoreCase(blockingConditionType)) {
                blockingConditionValue = MultitenantUtils.getTenantAwareUsername(blockingConditionValue);
                blockingConditionValue = blockingConditionValue + "@" + tenantDomain;
            }

            publishBlockingEvent(blockingConditionType, blockingConditionValue, Boolean.toString(blockCondition
                    .isEnabled()));
        }
    }

    /**
     * Publishes the changes on blocking conditions.
     * @param conditionType -
     * @param conditionValue
     */
    private void publishBlockingEvent(String conditionType, String conditionValue, String state) {
        OutputEventAdapterService eventAdapterService = ServiceReferenceHolder.getInstance().getOutputEventAdapterService();

        // Encoding the message into a map.
        HashMap<String, String> blockingMessage = new HashMap<String, String>();
        blockingMessage.put(APIConstants.BLOCKING_CONDITION_KEY, conditionType);
        blockingMessage.put(APIConstants.BLOCKING_CONDITION_VALUE, conditionValue);
        blockingMessage.put(APIConstants.BLOCKING_CONDITION_STATE, state);
        blockingMessage.put(APIConstants.BLOCKING_CONDITION_DOMAIN, tenantDomain);
        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getThrottleProperties();

        // Checking whether EventPublisherName is provided.  An empty HashMap is set so that it can be used to keep transport.jms
        // .Header value.
        if (throttleProperties.getJmsPublisherParameters() != null && !throttleProperties.getJmsPublisherParameters()
                .isEmpty()) {
            eventAdapterService.publish(APIConstants.BLOCKING_EVENT_PUBLISHER, new HashMap<String, String>()
                    , blockingMessage);
        }
    }

    private void publishKeyTemplateEvent(String templateValue, String state) {

        //Publishing an event to notify that a policy has been added.
        HashMap<String, String> keyTemplateMap = new HashMap<String, String>();
        keyTemplateMap.put(APIConstants.POLICY_TEMPLATE_KEY, templateValue);
        keyTemplateMap.put(APIConstants.TEMPLATE_KEY_STATE, state);

        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getThrottleProperties();

        // Getting JMS Publisher name from config. An empty HashMap is set so that it can be used to keep transport.jms
        // .Header value.
        if (throttleProperties.getJmsPublisherParameters() != null && !throttleProperties.getJmsPublisherParameters()
                .isEmpty()) {
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService().publish(APIConstants.BLOCKING_EVENT_PUBLISHER,
                                                                                        new HashMap<String, String>()
                    , keyTemplateMap);
        }
    }

    public String getLifecycleConfiguration(String tenantDomain) throws APIManagementException {
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIUtil utils = new APIUtil();
            return utils.getFullLifeCycleData(configRegistry);
        } catch (XMLStreamException e) {
            handleException("Parsing error while getting the lifecycle configuration content.", e);
            return null;
        } catch (RegistryException e) {
            handleException("Registry error while getting the lifecycle configuration content.", e);
            return null;
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    public String getExternalWorkflowReferenceId(int subscriptionId) throws APIManagementException {
        return apiMgtDAO.getExternalWorkflowReferenceForSubscription(subscriptionId);
    }

    /**
     * Get the workflow status information for the given api for the given workflow type
     * 
     * @param apiIdentifier Api identifier
     * @param workflowType workflow type
     * @return WorkflowDTO
     * @throws APIManagementException
     */
    public WorkflowDTO getAPIWorkflowStatus(APIIdentifier apiIdentifier, String workflowType)
            throws APIManagementException {
        int apiId = apiMgtDAO.getAPIID(apiIdentifier, null);        
        
        WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                WorkflowConstants.WF_TYPE_AM_API_STATE);
        
        return wfDTO;
    }

    public void deleteWorkflowTask(APIIdentifier apiIdentifier) throws APIManagementException {
        int apiId;
        try {
            apiId = apiMgtDAO.getAPIID(apiIdentifier, null);
            cleanUpPendingAPIStateChangeTask(apiId);
        } catch (APIManagementException e) {
            handleException("Error while deleting the workflow task.", e);
        } catch (WorkflowException e) {
            handleException("Error while deleting the workflow task.", e);
        }     
        
    }
    
    private void cleanUpPendingAPIStateChangeTask(int apiId) throws WorkflowException, APIManagementException{
        //Run cleanup task for workflow
        WorkflowExecutor apiStateChangeWFExecutor = WorkflowExecutorFactory.getInstance().
                getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_API_STATE);

        WorkflowDTO wfDTO = apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId),
                WorkflowConstants.WF_TYPE_AM_API_STATE);
        if(wfDTO != null && WorkflowStatus.CREATED == wfDTO.getStatus()){
            apiStateChangeWFExecutor.cleanUpPendingTask(wfDTO.getExternalWorkflowReference());
        }
    }
}
