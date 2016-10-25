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

import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.apimgt.api.model.policy.Policy;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {

    protected Log log = LogFactory.getLog(getClass());


    // API definitions from swagger v2.0
    //protected static final APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

    public AbstractAPIManager() throws APIManagementException {
    }

    public AbstractAPIManager(String username) throws APIManagementException {
    }

    public void cleanup() {

    }

    public List<API> getAllAPIs() throws APIManagementException {
       return null;
    }

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        return null;
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
        return null;
    }

    private Registry getRegistry(APIIdentifier identifier, String apiPath)
            throws APIManagementException {
        return null;    
    }

    public API getAPI(String apiPath) throws APIManagementException {
        return null;
    }

    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException {
        return false;
    }

    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException {

        return null;
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
        
                return null;
    }

    public String addResourceFile(String resourcePath, ResourceFile resourceFile) throws APIManagementException {
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
        return false;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        return null;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId,String loggedUsername) throws APIManagementException {
        return null;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
        
        return false;
    }

    public Documentation getDocumentation(APIIdentifier apiId, DocumentationType docType,
                                          String docName) throws APIManagementException {
        return null;
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
        return null;
    }

    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        return null;
    }

    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        return null;
    }

    public boolean isContextExist(String context) throws APIManagementException {
        return false;
    }
    
    public boolean isScopeKeyExist(String scopeKey, int tenantid) throws APIManagementException {
        return false;
    }

    public boolean isScopeKeyAssigned(APIIdentifier identifier, String scopeKey, int tenantid)
            throws APIManagementException {
        return false;
    }      


    public boolean isApiNameExist(String apiName) throws APIManagementException {
        return false;
    }

    public void addSubscriber(String username, String groupingId)
            throws APIManagementException {

        
    }

    /**
     * Add default application on the first time a subscriber is added to the database
     * @param subscriber Subscriber
     *
     * @throws APIManagementException if an error occurs while adding default application
     */
    private void addDefaultApplicationForSubscriber (Subscriber subscriber) throws APIManagementException {
        
    }

    public void updateSubscriber(Subscriber subscriber)
            throws APIManagementException {
       
    }

    public Subscriber getSubscriber(int subscriberId)
            throws APIManagementException {
        return null;
    }

    public ResourceFile getIcon(APIIdentifier identifier) throws APIManagementException {
        return null;
    }

    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        return null;
    }

    /**
     * Returns the corresponding application given the uuid
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    public Application getApplicationByUUID(String uuid) throws APIManagementException {
        return null;
    }

    /** returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Subscription
     * @return SubscribedAPI object which is related to the UUID
     * @throws APIManagementException
     */
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        return null;
    }

    protected final void handleException(String msg, Exception e) throws APIManagementException {
        
    }

    protected final void handleException(String msg) throws APIManagementException {
        
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
        return false;
    }

    public boolean isApplicationTokenRevoked(String accessToken) throws APIManagementException {
        return false;
    }


    public APIKey getAccessTokenData(String accessToken) throws APIManagementException {
        return null;
    }

    public Map<Integer, APIKey> searchAccessToken(String searchType, String searchTerm, String loggedInUser)
            throws APIManagementException {
        return null;
    }
    
    public Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException {
        return null;
    }

    public API getAPI(APIIdentifier identifier,APIIdentifier oldIdentifier, String oldContext) { 
        return null;
    }

    @Override
    public Set<Tier> getAllTiers() throws APIManagementException {
        return null;
    }

    @Override
    public Set<Tier> getAllTiers(String tenantDomain) throws APIManagementException {
        return null;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers() throws APIManagementException {
        return null;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers(String tenantDomain) throws APIManagementException {

        return null;
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
        return null;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Map<String, String>
     */
    public Map<String,String> getTenantDomainMappings(String tenantDomain, String apiType) throws APIManagementException {
        return null;
    }


    public boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException{

        return false;
    }
    
    public Policy[] getPolicies(String username, String level) throws APIManagementException {
        return null;
    }
    
    @Override
    public Map<String,Object> searchPaginatedAPIs(String searchQuery, String requestedTenantDomain,
                                                  int start,int end, boolean isLazyLoad) throws APIManagementException {
        return null;
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
        return null;
    }
}
