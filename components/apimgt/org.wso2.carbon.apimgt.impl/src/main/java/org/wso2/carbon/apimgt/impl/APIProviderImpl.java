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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.observers.APIStatusObserverList;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.impl.utils.*;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.apimgt.impl.publishers.WSO2APIPublisher;

import javax.cache.Cache;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.*;
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
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException {

        List<API> apiSortedList = new ArrayList<API>();

        try {
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
        String providerPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                              APIConstants.PROVIDERS_PATH + RegistryConstants.PATH_SEPARATOR + providerName;
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
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(
            String providerName) throws APIManagementException {
        return apiMgtDAO.getAllAPIUsageByProvider(providerName);
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

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws APIManagementException {

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
    public void addAPI(API api) throws APIManagementException {
        try {           
            createAPI(api);
            apiMgtDAO.addAPI(api);
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
        } catch (APIManagementException e) {          
            throw new APIManagementException("Error in adding API :"+api.getId().getApiName(),e);
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

    /**
     * Updates an existing API
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update API
     */
    public void updateAPI(API api) throws APIManagementException {
        API oldApi = getAPI(api.getId());
        if (oldApi.getStatus().equals(api.getStatus())) {
            try {
               
                boolean updatePermissions = false;
                if(!oldApi.getVisibility().equals(api.getVisibility()) || (oldApi.getVisibility().equals(APIConstants.API_RESTRICTED_VISIBILITY) && !api.getVisibleRoles().equals(oldApi.getVisibleRoles()))){
                    updatePermissions = true;
                }
                updateApiArtifact(api, true,updatePermissions);
                if (!oldApi.getContext().equals(api.getContext())) {
                    api.setApiHeaderChanged(true);
                }

                apiMgtDAO.updateAPI(api);

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
                            apiPublished.setOldInSequence(oldApi.getInSequence());
                            apiPublished.setOldOutSequence(oldApi.getOutSequence());
                            publishToGateway(apiPublished);
                        }
                    } else {
                        log.debug("Gateway is not existed for the current API Provider");
                    }
                }
               
                //If gateway(s) exist, remove resource paths saved on the cache.
                if (gatewayExists) {
                    if (isAPIPublished && !oldApi.getUriTemplates().equals(api.getUriTemplates())) {
                        Set<URITemplate> resourceVerbs = api.getUriTemplates();

                        List<Environment> gatewayEnvs = config.getApiGatewayEnvironments();
                        for(Environment environment : gatewayEnvs){
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
                        }

                    }
                }
                /* Update API Definition for Swagger 
                createUpdateAPIDefinition(api);*/

                //update apiContext cache
                if (APIUtil.isAPIManagementEnabled()) {
                    Cache contextCache = APIUtil.getAPIContextCache();
                    contextCache.remove(oldApi.getContext());
                    contextCache.put(api.getContext(), true);
                }

            } catch (APIManagementException e) {
            	handleException("Error while updating the API :" +api.getId().getApiName(),e);
            }  catch (AxisFault axisFault) {
                handleException("Error while invalidating API resource cache", axisFault);
            }

        } else {
            // We don't allow API status updates via this method.
            // Use changeAPIStatus for that kind of updates.
            throw new APIManagementException("Invalid API update operation involving API status changes");
        }
    }



    private void updateApiArtifact(API api, boolean updateMetadata,boolean updatePermissions) throws APIManagementException {

        //Validate Transports
        validateAndSetTransports(api);

        try {
        	registry.beginTransaction();
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
          

            if (updateMetadata) {
            	
                if (api.getWsdlUrl() != null && !"".equals(api.getWsdlUrl())) {
                    String path = APIUtil.createWSDL(registry, api);
                    if (path != null) {
                        registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
                        updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl()); //reset the wsdl path to permlink                      
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
            if(updatePermissions){
                clearResourcePermissions(artifactPath, api.getId());
                String visibleRolesList = api.getVisibleRoles();
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,artifactPath);
            }
            registry.commitTransaction();
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
    
    
    
    public void changeAPIStatus(API api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws APIManagementException {
        APIStatus currentStatus = api.getStatus();
        if (!currentStatus.equals(status)) {
            api.setStatus(status);
            try {                
                updateApiArtifact(api, false,false);
                apiMgtDAO.recordAPILifeCycleEvent(api.getId(), currentStatus, status, userId);

                APIStatusObserverList observerList = APIStatusObserverList.getInstance();
                observerList.notifyObservers(currentStatus, status, api);
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

                if (gatewayType.equalsIgnoreCase(APIConstants.API_GATEWAY_TYPE_SYNAPSE) && updateGatewayConfig) {
                    if (status.equals(APIStatus.PUBLISHED) || status.equals(APIStatus.DEPRECATED) ||
                        status.equals(APIStatus.BLOCKED)) {
                        publishToGateway(api);
                    } else {
                        removeFromGateway(api);
                    }
                }
               
            } catch (APIManagementException e) {
            	handleException("Error occured in the status change : " + api.getId().getApiName() , e);
            }
        }
    }

    public void makeAPIKeysForwardCompatible(API api) throws APIManagementException {
        String provider = api.getId().getProviderName();
        String apiName = api.getId().getApiName();
        Set<String> versions = getAPIVersions(provider, apiName);
        APIVersionComparator comparator = new APIVersionComparator();
        for (String version : versions) {
            API otherApi = getAPI(new APIIdentifier(provider, apiName, version));
            if (comparator.compare(otherApi, api) < 0 && otherApi.getStatus().equals(APIStatus.PUBLISHED)) {
                apiMgtDAO.makeKeysForwardCompatible(provider, apiName, version,
                                                    api.getId().getVersion(), api.getContext());
            }
        }
    }

    private void publishToGateway(API api) throws APIManagementException {
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
        try {
            gatewayManager.publishToGateway(api, builder, tenantDomain);
        } catch (Exception e) {
            handleException("Error while publishing to Gateway ", e);
        }
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
            handleException("Unsupported Transport ["+transport+"]");
        }
    }

    private void removeFromGateway(API api) throws APIManagementException {
        String tenantDomain = null;
        if (api.getId().getProviderName().contains("AT")) {
            String provider = api.getId().getProviderName().replace("-AT-", "@");
            tenantDomain = MultitenantUtils.getTenantDomain( provider);
        }

        APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
        try {
            gatewayManager.removeFromGateway(api, tenantDomain);
        } catch (Exception e) {
            handleException("Error while removing API from Gateway ", e);
        }
    }

    private boolean isAPIPublished(API api) throws APIManagementException {
        try {
            String tenantDomain = null;
			if (api.getId().getProviderName().contains("AT")) {
				String provider = api.getId().getProviderName().replace("-AT-", "@");
				tenantDomain = MultitenantUtils.getTenantDomain( provider);			
			}
            APIGatewayManager gatewayManager = APIGatewayManager.getInstance();
            return gatewayManager.isAPIPublished(api, tenantDomain);
        } catch (Exception e) {
            handleException("Error while checking API status", e);
        }
		return false;
    }

    private APITemplateBuilder getAPITemplateBuilder(API api){
        APITemplateBuilderImpl vtb = new APITemplateBuilderImpl(api);

        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler", Collections.EMPTY_MAP);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("id","A");
        properties.put("policyKey","gov:" + APIConstants.API_TIER_LOCATION);
        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleHandler",properties);

        vtb.addHandler("org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler",Collections.EMPTY_MAP);

        vtb.addHandler("org.wso2.carbon.apimgt.usage.publisher.APIMgtGoogleAnalyticsTrackingHandler",Collections.EMPTY_MAP);

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String extensionHandlerPosition = config.getFirstProperty(APIConstants.EXTENSION_HANDLER_POSITION);
        if(extensionHandlerPosition != null && "top".equalsIgnoreCase(extensionHandlerPosition)){
            vtb.addHandlerPriority("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",Collections.EMPTY_MAP,0);
        }
        else{
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",Collections.EMPTY_MAP);
        }
        return vtb;
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
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException,
                                                                       APIManagementException {
        String apiSourcePath = APIUtil.getAPIPath(api.getId());

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                            api.getId().getProviderName() +
                            RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                            RegistryConstants.PATH_SEPARATOR + newVersion +
                            APIConstants.API_RESOURCE_NAME;
        try {
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
            API newAPI = getAPI(newId,api.getId());
            for (Documentation doc : docs) {
            	/* If the document is API Definition for swagger */
            	if (doc.getName().equals(APIConstants.API_DEFINITION_DOC_NAME)) {
            		/* Create the JSON Content again for API with new definition */
            		String content = APIUtil.createSwaggerJSONContent(newAPI);
            		addAPIDefinitionContent(newId, doc.getName(), content);
            	} else {
	                addDocumentation(newId, doc);
	                String content = getDocumentationContent(api.getId(), doc.getName());
	                if (content != null) {
	                    addDocumentationContent(newId, doc.getName(), content);
	                }
            	}
            }

            // Make sure to unset the isLatest flag on the old version
            GenericArtifact oldArtifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());
            oldArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "false");
            artifactManager.updateGenericArtifact(oldArtifact);

            apiMgtDAO.addAPI(newAPI);

        } catch (RegistryException e) {
            String msg = "Failed to create new version : " + newVersion + " of : "
                         + api.getId().getApiName();
            handleException(msg, e);
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
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType)
            throws APIManagementException {
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;

        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
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
    public void addDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        createDocumentation(apiId, documentation);
    }

    /**
     * This method used to save the documentation content
     *
     * @param identifier,        API identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(APIIdentifier identifier, String documentationName, String text)
            throws APIManagementException {

        String documentationPath = APIUtil.getAPIDocPath(identifier) + documentationName;
        String contentPath = APIUtil.getAPIDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
                             RegistryConstants.PATH_SEPARATOR + documentationName;
        try {
            Resource docContent = registry.newResource();
            docContent.setContent(text);
            registry.put(contentPath, docContent);
            registry.addAssociation(documentationPath, contentPath,
                                    APIConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
            String[] authorizedRoles = getAuthorizedRoles(documentationPath);
            String apiPath = APIUtil.getAPIPath(identifier);
            APIUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(), 
            		getAPI(apiPath).getVisibility(),authorizedRoles,contentPath);
        } catch (RegistryException e) {
            String msg = "Failed to add the documentation content of : "
                         + documentationName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to add the documentation content of : "
                         + documentationName + " of API :" + identifier.getApiName();
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
            registry.put(contentPath, docContent);
            
            String apiPath = APIUtil.getAPIPath(identifier);
            API api = getAPI(apiPath);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
    		APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, contentPath);
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

        String docPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                         apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                         RegistryConstants.PATH_SEPARATOR + apiId.getVersion() + RegistryConstants.PATH_SEPARATOR +
                         APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR + documentation.getName();
        try {
            String apiArtifactId = registry.get(docPath).getUUID();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
            String apiPath = APIUtil.getAPIPath(apiId);
            GenericArtifact updateApiArtifact = APIUtil.createDocArtifactContent(artifact, apiId, documentation);
            artifactManager.updateGenericArtifact(updateApiArtifact);
            clearResourcePermissions(docPath, apiId);

            API api=getAPI(apiPath);
            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),visibleRoles,artifact.getPath());
            
            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if(docFilePath != null && !docFilePath.equals("")) {
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                APIUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(),
                        getAPI(apiPath).getVisibility(), visibleRoles, filePath);
            }

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
     * @throws APIManagementException if failed to create API
     */
    private void createAPI(API api) throws APIManagementException {
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                            APIConstants.API_KEY);

        //Validate Transports
        validateAndSetTransports(api);
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = APIUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null && tagSet.size() > 0) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }           
            if (api.getWsdlUrl() != null && !"".equals(api.getWsdlUrl())) {
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

            /* Generate API Definition for Swagger */
            createUpdateAPIDefinition(api);

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
     * This function is to set resource permissions based on its visibility
     *
     * @param artifactPath API resource path
     * @throws APIManagementException Throwing exception
     */
    private void clearResourcePermissions(String artifactPath, APIIdentifier apiId)
            throws APIManagementException {
        try {
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                            + artifactPath);
            String tenantDomain = MultitenantUtils.getTenantDomain(
                    APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.
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
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    private void createDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        try {
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
            String[] authorizedRoles=getAuthorizedRoles(apiPath);
            APIUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(), 
            		getAPI(apiPath).getVisibility(), authorizedRoles, artifact.getPath());
            
            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if(docFilePath != null && !docFilePath.equals("")){
                //The docFilePatch comes as /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                //We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set permissions.
                int startIndex = docFilePath.indexOf("governance") + "governance".length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                APIUtil.setResourcePermissions(getAPI(apiPath).getId().getProviderName(),
                        getAPI(apiPath).getVisibility(), authorizedRoles, filePath);
            }
        } catch (RegistryException e) {
            handleException("Failed to add documentation", e);
        } catch (UserStoreException e) {
            handleException("Failed to add documentation", e);
        }
    }

    private String[] getAuthorizedRoles(String artifactPath) throws UserStoreException {
        String  resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                             RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                                                             + artifactPath);
        RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                (ServiceReferenceHolder.getUserRealm());
        return authorizationManager.getAllowedRolesForResource(resourcePath,ActionConstants.GET);
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
     * @return int value with subscription id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    public void updateSubscription(APIIdentifier apiId,String subStatus,int appId) throws APIManagementException {
        apiMgtDAO.updateSubscription(apiId,subStatus,appId);
    }

    public void deleteAPI(APIIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                      identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                      identifier.getApiName()+RegistryConstants.PATH_SEPARATOR+identifier.getVersion();
        
        String apiArtifactPath = APIUtil.getAPIPath(identifier);
      
        try {

            long subsCount = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
            if(subsCount > 0){
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

            artifactManager.removeGenericArtifact(artifactId);

            String thumbPath = APIUtil.getIconPath(identifier);
            if (registry.resourceExists(thumbPath)) {
                registry.delete(thumbPath);
            }
            
            /*Remove API Definition Resource - swagger*/
            String apiDefinitionFilePath = APIUtil.getAPIDefinitionFilePath(identifier.getApiName(), identifier.getVersion(),identifier.getProviderName());
            if (registry.resourceExists(apiDefinitionFilePath)) {
            	registry.delete(apiDefinitionFilePath);
            }

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
            String gatewayType = config.getFirstProperty(APIConstants.API_GATEWAY_TYPE);

            API api = new API(identifier);
            // gatewayType check is required when API Management is deployed on other servers to avoid synapse
            if (gatewayExists && gatewayType.equals("Synapse")) {
                //if (isAPIPublished(api)) {
            		api.setInSequence(inSequence); //need to remove the custom sequences
            		api.setOutSequence(outSequence);
                    removeFromGateway(api);
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
            if (APIUtil.isAPIManagementEnabled()) {
            	 Cache contextCache = APIUtil.getAPIContextCache();
                contextCache.remove(api.getContext());
                contextCache.put(api.getContext(), false);
            }

        } catch (RegistryException e) {
            handleException("Failed to remove the API from : " + path, e);
        }
    }
  

    public List<API> searchAPIs(String searchTerm, String searchType, String providerId) throws APIManagementException {
        List<API> apiSortedList = new ArrayList<API>();
        String regex = "(?i)[\\w.|-]*" + searchTerm.trim() + "[\\w.|-]*";
		      
        Pattern pattern;
        Matcher matcher;
        try {
            List<API> apiList;
            if(providerId!=null){
                apiList= getAPIsByProvider(providerId);
            }else{
                apiList= getAllAPIs();
            }
            if (apiList == null || apiList.size() == 0) {
                return apiSortedList;
            }
            pattern = Pattern.compile(regex);
            for (API api : apiList) {

                if (searchType.equalsIgnoreCase("Name")) {
                    String api1 = api.getId().getApiName();
                    matcher = pattern.matcher(api1);
                }else if (searchType.equalsIgnoreCase("Provider")) {
                    String api1 = api.getId().getProviderName();
                    matcher = pattern.matcher(api1);
                } else if (searchType.equalsIgnoreCase("Version")) {
                    String api1 = api.getId().getVersion();
                    matcher = pattern.matcher(api1);
                } else if (searchType.equalsIgnoreCase("Context")) {
                    String api1 = api.getContext();
                    matcher = pattern.matcher(api1);
                } else {
                    String apiName = api.getId().getApiName();
                    matcher = pattern.matcher(apiName);
                }
                
                if (matcher.find()) {                 	
                    apiSortedList.add(api);
                }

            }
        } catch (APIManagementException e) {
            handleException("Failed to search APIs with type", e);
        }
        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
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
                if (APIConstants.WSO2_API_STORE_TYPE.equals(store.getType())) {
                    boolean published = new WSO2APIPublisher().publishToStore(api, store); //First trying to publish the API to external APIStore
                    if (published) { //If published,then save to database.
                        publishedStores.add(store);
                    }
                } else { //When the external APIStore is not a WSO2 APIStore
                    log.warn("The configured external APIStore type is currently not supported.Hence ignoring publishing the API to - " + store.getDisplayName());
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
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet)
            throws APIManagementException {
        boolean updated=false;
        Set<APIStore> publishedStores=getPublishedExternalAPIStores(api.getId());
        Set<APIStore> notPublishedAPIStores = new HashSet<APIStore>();
        Set<APIStore> modifiedPublishedApiStores = new HashSet<APIStore>();
        Set<APIStore> updateApiStores = new HashSet<APIStore>();
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
        publishToExternalAPIStores(api, notPublishedAPIStores);
        //Update the APIs which are already exist in the external APIStore
        updateAPIInExternalAPIStores(api,updateApiStores);
        updateExternalAPIStoresDetails(api.getId(),modifiedPublishedApiStores); //Update database saved published APIStore details,if there are any
        //modifications in api-manager.xml
        updated=true;
        return updated;
    }
    
	private boolean isAPIAvailableInExternalAPIStore(API api, APIStore store) throws APIManagementException {
		// Check external APIStore type is wso2 or not
		if (APIConstants.WSO2_API_STORE_TYPE.equals(store.getType())) {
			return new WSO2APIPublisher().isAPIAvailable(api, store);
		} else {
			// When the external APIStore is not a WSO2 APIStore
			log.warn("The configured external APIStore type is currently not supported. Hence ignoring checking the API availabiltiy in - "
					+ store.getDisplayName());
		}
		return false;
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
                if (APIConstants.WSO2_API_STORE_TYPE.equals(store.getType())) {//Check external APIStore type is wso2 or not
                    new WSO2APIPublisher().updateToStore(api, store);
                } else { //When the external APIStore is not a WSO2 APIStore
                    log.warn("The configured external APIStore type is currently not supported.Hence ignoring updating the API in - " + store.getDisplayName());
                }
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
        if (APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            return apiMgtDAO.getExternalAPIStoresDetails(apiId);

        } else {
            return null;
        }
    }

	/**
	 * Get stored custom inSequences from governanceSystem registry
	 * 
	 * @throws APIManagementException
	 */

	public List<String> getCustomInSequences() throws APIManagementException {

		List<String> sequenceList = new ArrayList<String>();
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
		                sequenceList.add(seqElment.getAttributeValue(new QName("name")));		               
	                }
                }
            }

		} catch (Exception e) {
			handleException("Issue is in getting custom InSequences from the Registry", e);
		}
		return sequenceList;
	}

	/**
	 * Get stored custom outSequences from governanceSystem registry
	 * 
	 * @throws APIManagementException
	 */

	public List<String> getCustomOutSequences() throws APIManagementException {

		List<String> sequenceList = new ArrayList<String>();
		try {
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if (registry.resourceExists(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION)) {
	            org.wso2.carbon.registry.api.Collection outSeqCollection =
	                                                                       (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
	            if (outSeqCollection !=null) {
	                String[] outSeqChildPaths = outSeqCollection.getChildren();
	                for (int i = 0; i < outSeqChildPaths.length; i++) {
		                Resource outSequence = registry.get(outSeqChildPaths[i]);
		                OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());
		         
		                sequenceList.add(seqElment.getAttributeValue(new QName("name")));		               
	                }
                }
            }

		} catch (Exception e) {
			handleException("Issue is in getting custom OutSequences from the Registry", e);
		}
		return sequenceList;
	}

    /**
     * Get stored custom fault sequences from governanceSystem registry
     *
     * @throws APIManagementException
     */

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
                    for (int i = 0; i < faultSeqChildPaths.length; i++) {
                        Resource outSequence = registry.get(faultSeqChildPaths[i]);
                        OMElement seqElment = APIUtil.buildOMElement(outSequence.getContentStream());

                        sequenceList.add(seqElment.getAttributeValue(new QName("name")));
                    }
                }
            }

        } catch (Exception e) {
            handleException("Issue is in getting custom Fault Sequences from the Registry", e);
        }
        return sequenceList;
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
}


