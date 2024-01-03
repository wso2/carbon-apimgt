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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.apimgt.api.PolicyNotFoundException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.EnvironmentSpecificAPIPropertyDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.factory.PersistenceFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.utils.*;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.DocumentMapper;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants.WF_TYPE_AM_API_STATE;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {

    // API definitions from swagger v2.0
    protected Log log = LogFactory.getLog(getClass());
    protected ApiMgtDAO apiMgtDAO;
    protected EnvironmentSpecificAPIPropertyDAO environmentSpecificAPIPropertyDAO;
    protected ScopesDAO scopesDAO;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected String tenantDomain;
    protected String organization;
    protected String username;
    // Property to indicate whether access control restriction feature is enabled.
    protected boolean isAccessControlRestrictionEnabled = false;
    APIPersistence apiPersistenceInstance;
    String migrationEnabled = System.getProperty(APIConstants.MIGRATE);

    public AbstractAPIManager() throws APIManagementException {

    }

    public AbstractAPIManager(String username) throws APIManagementException {
        this(username, StringUtils.isNoneBlank(username) ? MultitenantUtils.getTenantDomain(username)
                : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    public AbstractAPIManager(String username, String organization) throws APIManagementException {

        apiMgtDAO = ApiMgtDAO.getInstance();
        scopesDAO = ScopesDAO.getInstance();
        environmentSpecificAPIPropertyDAO = EnvironmentSpecificAPIPropertyDAO.getInstance();

        try {
            if (username == null) {
                this.username = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
            } else {
                String tenantDomainName = APIUtil.getInternalOrganizationDomain(organization);
                String tenantUserName = getTenantAwareUsername(username);
                int tenantId = getTenantManager().getTenantId(tenantDomainName);
                this.tenantId = tenantId;
                this.tenantDomain = tenantDomainName;
                this.organization = organization;
                this.username = tenantUserName;

            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while getting user registry for user:" + username;
            throw new APIManagementException(msg, e);
        }
        apiPersistenceInstance = PersistenceFactory.getAPIPersistenceInstance();
    }

    public void cleanup() {

    }

    public List<API> getAllAPIs() throws APIManagementException {

        List<API> apiSortedList = new ArrayList<API>();

        Organization org = new Organization(tenantDomain);
        String[] roles = APIUtil.getFilteredUserRoles(username);
        Map<String, Object> properties = APIUtil.getUserProperties(username);
        UserContext userCtx = new UserContext(username, org, properties, roles);
        try {
            PublisherAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForPublisher(org, "", 0,
                    Integer.MAX_VALUE, userCtx, null, null);

            if (searchAPIs != null) {
                List<PublisherAPIInfo> list = searchAPIs.getPublisherAPIInfoList();
                for (PublisherAPIInfo publisherAPIInfo : list) {
                    API mappedAPI = APIMapper.INSTANCE.toApi(publisherAPIInfo);
                    apiSortedList.add(mappedAPI);
                }
            }
        } catch (APIPersistenceException e) {
            throw new APIManagementException("Error while searching the api ", e);
        }

        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
    }

    protected String getTenantAwareUsername(String username) {

        return MultitenantUtils.getTenantAwareUsername(username);
    }

    protected String getTenantDomain(Identifier identifier) {

        return MultitenantUtils.getTenantDomain(
                APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
    }

    protected void populateDefaultVersion(API api) throws APIManagementException {
        apiMgtDAO.setDefaultVersion(api);
    }
    protected void populateDefaultVersion(APIProduct apiProduct) throws APIManagementException {
        apiMgtDAO.setDefaultVersion(apiProduct);
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {

        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }


    /**
     * Returns the minimalistic information about the API given the UUID. This will only query from AM database AM_API
     * table.
     *
     * @param id UUID of the API
     * @return basic information about the API
     * @throws APIManagementException error while getting the API information from AM_API
     */
    public APIInfo getAPIInfoByUUID(String id) throws APIManagementException {
        return apiMgtDAO.getAPIInfoByUUID(id);
    }


    protected TenantManager getTenantManager() {

        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }

    protected void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    protected void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    public boolean isAPIAvailable(APIIdentifier identifier, String organization) throws APIManagementException {

        String uuid = apiMgtDAO.getUUIDFromIdentifier(identifier, organization);
        if (uuid == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isAPIProductAvailable(APIProductIdentifier identifier, String organization)
            throws APIManagementException {

        String uuid = apiMgtDAO.getUUIDFromIdentifier(identifier, organization);
        if (uuid == null) {
            return false;
        } else {
            return true;
        }
    }

    public Set<String> getAPIVersions(String providerName, String apiName, String organization)
            throws APIManagementException {

        return apiMgtDAO.getAPIVersions(apiName, providerName, organization);
    }


    @Override
    public ResourceFile getWSDL(String apiId, String organization) throws APIManagementException {

        try {
            org.wso2.carbon.apimgt.persistence.dto.ResourceFile resource =
                    apiPersistenceInstance.getWSDL(new Organization(organization), apiId);
            if (resource != null) {
                ResourceFile resourceFile = new ResourceFile(resource.getContent(), resource.getContentType());
                resourceFile.setName(resource.getName());
                return resourceFile;
            } else {
                String msg = "Failed to get WSDL. Artifact corresponding to artifactId " + apiId + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (WSDLPersistenceException e) {
            throw new APIManagementException("Error while retrieving wsdl resource for api " + apiId, e);
        }
    }

    @Override
    public String getGraphqlSchemaDefinition(String apiId, String tenantDomain) throws APIManagementException {

        String definition;
        try {
            definition = apiPersistenceInstance.getGraphQLSchema(new Organization(tenantDomain), apiId);
        } catch (GraphQLPersistenceException e) {
            throw new APIManagementException("Error while retrieving graphql definition from the persistance location",
                    e);
        }
        return definition;
    }

    @Override
    public String getOpenAPIDefinition(String apiId, String organization) throws APIManagementException {

        String definition = null;
        try {
            definition = apiPersistenceInstance.getOASDefinition(new Organization(organization), apiId);
        } catch (OASPersistenceException e) {
            throw new APIManagementException("Error while retrieving OAS definition from the persistance location", e);
        }
        return definition;
    }

    @Override
    public String getAsyncAPIDefinition(String apiId, String organization) throws APIManagementException {

        String definition = null;
        try {
            definition = apiPersistenceInstance.getAsyncDefinition(new Organization(organization), apiId);
        } catch (AsyncSpecPersistenceException e) {
            throw new APIManagementException("Error while retrieving Async definition from the persistance location", e);
        }
        return definition;
    }

    public List<Documentation> getAllDocumentation(String uuid, String organization) throws APIManagementException {

        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        Organization org = new Organization(organization);
        UserContext ctx = new UserContext(username, org, null, null);
        List<Documentation> convertedList = null;
        boolean isDocVisibilityEnabled = Boolean.parseBoolean(
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS));
        try {
            DocumentSearchResult list =
                    apiPersistenceInstance.searchDocumentation(org, uuid, 0, 0, null, ctx);
            if (list != null) {
                convertedList = new ArrayList<Documentation>();
                List<Documentation> privateDocs = new ArrayList<Documentation>();
                List<org.wso2.carbon.apimgt.persistence.dto.Documentation> docList = list.getDocumentationList();
                if (docList != null) {
                    for (int i = 0; i < docList.size(); i++) {
                        if (!isDocVisibilityEnabled) {
                            convertedList.add(DocumentMapper.INSTANCE.toDocumentation(docList.get(i)));
                        } else {
                            org.wso2.carbon.apimgt.persistence.dto.Documentation doc = docList.get(i);
                            if (APIConstants.DOC_API_BASED_VISIBILITY.equals(String.valueOf(doc.getVisibility()))) {
                                convertedList.add(DocumentMapper.INSTANCE.toDocumentation(docList.get(i)));
                            }
                            if (APIConstants.DOC_OWNER_VISIBILITY.equals(String.valueOf(doc.getVisibility()))) {
                                if (APIConstants.WSO2_ANONYMOUS_USER != username
                                        && !isTenantDomainNotMatching(organization)) {
                                    convertedList.add(DocumentMapper.INSTANCE.toDocumentation(docList.get(i)));
                                }
                            }
                            if (APIConstants.DOC_SHARED_VISIBILITY.equals(String.valueOf(doc.getVisibility()))) {
                                if (APIConstants.WSO2_ANONYMOUS_USER != username
                                        && !isTenantDomainNotMatching(organization)){
                                    privateDocs.add(DocumentMapper.INSTANCE.toDocumentation(docList.get(i)));
                                }
                            }

                        }
                    }
                    if (isDocVisibilityEnabled && privateDocs.size() > 0) {
                        String loggedInTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .getTenantDomain();
                        if (validatePrivateScopes(username, loggedInTenantDomain)) {
                            convertedList.addAll(privateDocs);
                        }
                    }
                }
            } else {
                convertedList = new ArrayList<Documentation>();
            }
        } catch (DocumentationPersistenceException | org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get documentations for api/product " + uuid;
            throw new APIManagementException(msg, e);
        }
        return convertedList;
    }

    /**
     * Validates whether the user has creator or publisher scopes for the documentation visibility control.
     *
     * @param username              Username
     * @param loggedInTenantDomain  Logged in Tenant domain
     * @return true if user has creator or publisher scopes
     * @throws UserStoreException if user store is not found.
     */
    private boolean validatePrivateScopes(String username, String loggedInTenantDomain)
            throws org.wso2.carbon.user.api.UserStoreException {
        int tenantId = APIUtil.getTenantIdFromTenantDomain(loggedInTenantDomain);

        String[] roleList = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                .getUserStoreManager().getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
        Map<String, String> restAPIScopes = APIUtil.getRESTAPIScopesForTenant(loggedInTenantDomain);

        Set<String> roles = new HashSet();
        roles.addAll(Arrays.asList(restAPIScopes.get(APIConstants.APIM_CREATOR_SCOPE).split(",")));
        roles.addAll(Arrays.asList(restAPIScopes.get(APIConstants.APIM_PUBLISHER_SCOPE).split(",")));

        for (String userRole : roleList) {
            if (roles.contains(userRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a documentation by artifact Id
     *
     * @param apiId                 artifact id of the api
     * @param docId                 artifact id of the document
     * @param organization          identifier of the organization
     * @return Document object which represents the artifact id
     * @throws APIManagementException
     */
    public Documentation getDocumentation(String apiId, String docId, String organization)
            throws APIManagementException {

        Documentation documentation = null;
        try {
            org.wso2.carbon.apimgt.persistence.dto.Documentation doc = apiPersistenceInstance
                    .getDocumentation(new Organization(organization), apiId, docId);
            if (doc != null && isDocVisible(doc, organization)) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved doc: " + doc);
                }
                documentation = DocumentMapper.INSTANCE.toDocumentation(doc);
            } else {
                String msg = "Failed to get the document. Artifact corresponding to document id " + docId
                        + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (DocumentationPersistenceException  | APIManagementException e) {
            throw new APIManagementException("Error while retrieving document for id " + docId, e);
        }
        return documentation;
    }

/**
     * Validate the document for doc visibility
     *
     * @param doc         Document ID
     * @return False      if user is not authorized to view the document
     */
    public boolean isDocVisible(org.wso2.carbon.apimgt.persistence.dto.Documentation doc,
                             String requestedTenantDomain) throws APIManagementException {
        boolean isDocVisibilityEnabled = Boolean.parseBoolean(
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(
                                APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS));

        if (!isDocVisibilityEnabled) {
            return true;
        }

        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String loggedInTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        boolean validDoc = false;
        if (APIConstants.DOC_API_BASED_VISIBILITY.equals(String.valueOf(doc.getVisibility()))) {
            validDoc = true;
        } else if (APIConstants.DOC_OWNER_VISIBILITY.equals(String.valueOf(doc.getVisibility()))) {
            if (APIConstants.WSO2_ANONYMOUS_USER != username && !isTenantDomainNotMatching(requestedTenantDomain)) {
                validDoc = true;
            }
        } else if (APIConstants.DOC_SHARED_VISIBILITY.equals(String.valueOf(doc.getVisibility()))) {
            if (APIConstants.WSO2_ANONYMOUS_USER != username && !isTenantDomainNotMatching(requestedTenantDomain)) {
                try {
                    if (validatePrivateScopes(username, loggedInTenantDomain)) {
                        validDoc = true;
                    }
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new APIManagementException(e);
                }
            }
        }

        if(!validDoc) {
            if (log.isDebugEnabled()) {
                log.debug("User " + username + " cannot view the requested document " + doc.getId());
            }
        }
        return validDoc;
    }
    
    @Override
    public DocumentationContent getDocumentationContent(String apiId, String docId, String organization)
            throws APIManagementException {

        try {
            DocumentContent content = apiPersistenceInstance
                    .getDocumentationContent(new Organization(organization), apiId, docId);
            DocumentationContent docContent = null;
            if (content != null) {
                docContent = DocumentMapper.INSTANCE.toDocumentationContent(content);
            }
            return docContent;
        } catch (DocumentationPersistenceException e) {
            throw new APIManagementException("Error while retrieving document content ", e);
        }
    }

    public GraphqlComplexityInfo getComplexityDetails(String uuid) throws APIManagementException {

        return apiMgtDAO.getComplexityDetails(uuid);
    }

    public void addOrUpdateComplexityDetails(String uuid, GraphqlComplexityInfo graphqlComplexityInfo) throws APIManagementException {

        apiMgtDAO.addOrUpdateComplexityDetails(uuid, graphqlComplexityInfo);
    }

    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {

        return null;
    }

    public boolean isContextExist(String context, String organization) throws APIManagementException {
        // Since we don't have tenant in the APIM table, we do the filtering using this hack
        if (context != null && context.startsWith("/t/"))
            context = context.replace("/t/" + getTenantDomainFromUrl(context), ""); //removing prefix
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            context = "/t/" + tenantDomain + context;
        }
        return apiMgtDAO.isContextExist(context, organization);
    }

    public boolean isContextExistForAPIProducts(String context, String contextWithVersion, String organization)
            throws APIManagementException {

        return apiMgtDAO.isContextExistForAPIProducts(context, contextWithVersion, organization);
    }

    protected String getTenantDomainFromUrl(String url) {

        return MultitenantUtils.getTenantDomainFromUrl(url);
    }

    /**
     * Check the scope exist in Tenant.
     *
     * @param scopeKey candidate scope key
     * @param tenantid tenant id
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
     * @param apiName API name
     * @param scopeKey      candidate scope key
     * @param organization   organization
     * @return true if the scope key is already attached as a local scope in any API
     * @throws APIManagementException if failed to check the local scope availability
     */
    public boolean isScopeKeyAssignedLocally(String apiName, String scopeKey, String organization)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Checking whether scope: " + scopeKey + " is assigned to another API as a local scope"
                    + " in organization: " + organization);
        }
        int tenantId = APIUtil.getInternalOrganizationId(organization);
        return apiMgtDAO.isScopeKeyAssignedLocally(apiName, scopeKey, tenantId, organization);
    }

    public boolean isApiNameExist(String apiName, String organization) throws APIManagementException {

        String tenantName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameExist(apiName, tenantName, organization);
    }

    public boolean isApiNameWithDifferentCaseExist(String apiName, String organization) throws APIManagementException {

        String tenantName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameWithDifferentCaseExist(apiName, tenantName, organization);
    }

    public void addSubscriber(String username, String groupingId)
            throws APIManagementException {

        Subscriber subscriber = new Subscriber(username);
        subscriber.setSubscribedDate(new Date());
        try {
            int tenantId = getTenantManager()
                    .getTenantId(getTenantDomain(username));
                subscriber.setEmail(StringUtils.EMPTY);
            subscriber.setTenantId(tenantId);
            apiMgtDAO.addSubscriber(subscriber, groupingId);
            if (APIUtil.isDefaultApplicationCreationEnabled() &&
                    !APIUtil.isDefaultApplicationCreationDisabledForTenant(getTenantDomain(username))) {
                // Add a default application once subscriber is added
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
        defaultApp.setTier(APIUtil.getDefaultApplicationLevelPolicy(subscriber.getTenantId()));
        //application will not be shared within the group
        defaultApp.setGroupId("");
        defaultApp.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        defaultApp.setUUID(UUID.randomUUID().toString());
        defaultApp.setDescription(APIConstants.DEFAULT_APPLICATION_DESCRIPTION);
        int applicationId = apiMgtDAO.addApplication(defaultApp, subscriber.getName(), tenantDomain);

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
     * @param uuid         uuid of the Application.
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
            int subscriptionCount = apiMgtDAO.getSubscriptionCountByApplicationId(application, tenantDomain);
            application.setSubscriptionCount(subscriptionCount);

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
            ApplicationNameWithInvalidCharactersException {

        throw new ApplicationNameWithInvalidCharactersException(msg);
    }

    public Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException {

        return Collections.emptySet();
    }

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

    public boolean isDuplicateContextTemplateMatchingOrganization(String contextTemplate, String organization) throws APIManagementException {
        return apiMgtDAO.isDuplicateContextTemplateMatchesOrganization(contextTemplate, organization);
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
        List<Policy> policiesWithoutUnlimitedTier = new ArrayList<Policy>();

        if (policies != null) {
            for (Policy policy : policies) {
                if (APIConstants.UNLIMITED_TIER.equals(policy.getPolicyName())) {
                    if (throttleProperties.isEnableUnlimitedTier()) {
                        policiesWithoutUnlimitedTier.add(policy);
                    }
                } else if (!APIConstants.UNAUTHENTICATED_TIER.equals(policy.getPolicyName())) {
                    policiesWithoutUnlimitedTier.add(policy);
                }
            }
        }
        policies = policiesWithoutUnlimitedTier.toArray(new Policy[0]);
        return policies;
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

    @Override
    public List<String> getApiVersionsMatchingApiNameAndOrganization(String apiName, String username,
            String organization) throws APIManagementException {
        return apiMgtDAO.getAPIVersionsMatchingApiNameAndOrganization(apiName, username, organization);
    }

    @Override
    public String getAPIProviderByNameAndOrganization(String apiName, String organization)
            throws APIManagementException {
        return apiMgtDAO.getAPIProviderByNameAndOrganization(apiName, organization);
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
        
        if (StringUtils.isNotEmpty(xWso2Tenant)) {
            int tenantId = APIUtil.getInternalOrganizationId(xWso2Tenant);
            // To handle choreo scenario. due to keymanagers are not per organization atm. using ST
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                xWso2Tenant = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
        }
        Set<APIKey> resultantApiKeyList = new HashSet<>();
        for (APIKey apiKey : apiKeyList) {
            String keyManagerName = apiKey.getKeyManager();
            String consumerKey = apiKey.getConsumerKey();
            String tenantDomain = this.tenantDomain;
            if (StringUtils.isNotEmpty(xWso2Tenant)) {
                tenantDomain = xWso2Tenant;
            }
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    apiMgtDAO.getKeyManagerConfigurationByName(tenantDomain, keyManagerName);
            if (keyManagerConfigurationDTO == null) {
                keyManagerConfigurationDTO = apiMgtDAO.getKeyManagerConfigurationByUUID(keyManagerName);
                if (keyManagerConfigurationDTO != null) {
                    keyManagerName = keyManagerConfigurationDTO.getName();
                } else {
                    log.error("Key Manager: " + keyManagerName + " not found in database.");
                    continue;
                }
            }
            String kmTenantDomain = keyManagerConfigurationDTO.getOrganization();
            if (tenantDomain != null && !tenantDomain.equalsIgnoreCase(kmTenantDomain)
                    && !APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN.equals(kmTenantDomain)) {
                continue;
            }
            KeyManager keyManager = null;
            if (keyManagerConfigurationDTO.isEnabled()) {
                keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
            } else {
                continue;
            }
            apiKey.setKeyManager(keyManagerConfigurationDTO.getName());
            if (StringUtils.isNotEmpty(consumerKey)) {
                if (keyManager != null) {
                    if (APIConstants.OAuthAppMode.MAPPED.name().equalsIgnoreCase(apiKey.getCreateMode())
                            && !isOauthAppValidation()) {
                        resultantApiKeyList.add(apiKey);
                    } else {
                        OAuthApplicationInfo oAuthApplicationInfo = null;
                        try {
                            oAuthApplicationInfo = keyManager.retrieveApplication(consumerKey);
                        } catch (APIManagementException e) {
                            log.error("Error while retrieving Application Information", e);
                            continue;
                        }
                        if (StringUtils.isNotEmpty(apiKey.getAppMetaData())) {
                            OAuthApplicationInfo storedOAuthApplicationInfo =
                                    new Gson().fromJson(apiKey.getAppMetaData()
                                            , OAuthApplicationInfo.class);
                            if (oAuthApplicationInfo == null) {
                                oAuthApplicationInfo = storedOAuthApplicationInfo;
                            } else {

                                if (StringUtils.isEmpty(oAuthApplicationInfo.getCallBackURL())) {
                                    oAuthApplicationInfo.setCallBackURL(storedOAuthApplicationInfo.getCallBackURL());
                                }
                                if ("null".equalsIgnoreCase(oAuthApplicationInfo.getCallBackURL())) {
                                    oAuthApplicationInfo.setCallBackURL("");
                                }
                                if (oAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES) == null &&
                                        storedOAuthApplicationInfo.getParameter(APIConstants.JSON_GRANT_TYPES) != null) {
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
     * @param subscriberId    subscriberId of the Application
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


    public List<APIProductResource> getResourcesOfAPIProduct(APIProductIdentifier productIdentifier)
            throws APIManagementException {

        return apiMgtDAO.getAPIProductResourceMappings(productIdentifier);
    }

    protected void populateAPIInformation(String uuid, String organization, API api)
            throws APIManagementException, OASPersistenceException, ParseException, AsyncSpecPersistenceException {
        //UUID
        if (api.getUuid() == null) {
            api.setUuid(uuid);
        }
        if (organization == null) {
            APIIdentifier identifier = api.getId();
            String tenantDomain = getTenantDomain(identifier);
            organization = tenantDomain;
        }
        Organization org = new Organization(organization);
        api.setOrganization(organization);
        // environment
        String environmentString = null;
        if (api.getEnvironments() != null) {
            environmentString = String.join(",", api.getEnvironments());
        }
        api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environmentString, organization));
        // workflow status
        APIIdentifier apiId = api.getId();
        WorkflowDTO workflow;
        String currentApiUuid = uuid;
        if (api.isRevision() && api.getRevisionedApiId() != null) {
            currentApiUuid = api.getRevisionedApiId();
        }
        workflow = APIUtil.getAPIWorkflowStatus(currentApiUuid, WF_TYPE_AM_API_STATE);
        if (workflow != null) {
            WorkflowStatus status = workflow.getStatus();
            api.setWorkflowStatus(status.toString());
        }
        // TODO try to use a single query to get info from db
        int internalId = apiMgtDAO.getAPIID(currentApiUuid);
        apiId.setId(internalId);
        apiMgtDAO.setServiceStatusInfoToAPI(api, internalId);
        String gatewayVendor = apiMgtDAO.getGatewayVendorByAPIUUID(uuid);
        api.setGatewayVendor(APIUtil.handleGatewayVendorRetrieval(gatewayVendor));
        api.setGatewayType(APIUtil.getGatewayType(gatewayVendor));
        // api level tier
        String apiLevelTier;
        if (api.isRevision()) {
            apiLevelTier = apiMgtDAO.getAPILevelTier(api.getRevisionedApiId(), api.getUuid());
        } else {
            apiLevelTier = apiMgtDAO.getAPILevelTier(internalId);
        }
        api.setApiLevelPolicy(apiLevelTier);
        // available tier
        String tiers = null;
        Set<Tier> tiersSet = api.getAvailableTiers();
        Set<String> tierNameSet = new HashSet<String>();
        for (Tier t : tiersSet) {
            tierNameSet.add(t.getName());
        }
        if (api.getAvailableTiers() != null) {
            tiers = String.join("||", tierNameSet);
        }
        Map<String, Tier> definedTiers = APIUtil.getTiers(APIUtil.getInternalOrganizationId(organization));
        Set<Tier> availableTier = APIUtil.getAvailableTiers(definedTiers, tiers, api.getId().getApiName());
        api.removeAllTiers();
        api.setAvailableTiers(availableTier);

        //Scopes
        Map<String, Scope> scopeToKeyMapping = APIUtil.getAPIScopes(currentApiUuid, organization);
        api.setScopes(new LinkedHashSet<>(scopeToKeyMapping.values()));

        //templates
        String resourceConfigsString;
        if (api.getSwaggerDefinition() != null) {
            resourceConfigsString = api.getSwaggerDefinition();
        } else {
            resourceConfigsString = apiPersistenceInstance.getOASDefinition(org, uuid);
        }
        api.setSwaggerDefinition(resourceConfigsString);

        if (resourceConfigsString == null) {
            if (api.getAsyncApiDefinition() != null) {
                resourceConfigsString = api.getAsyncApiDefinition();
            } else {
                resourceConfigsString = apiPersistenceInstance.getAsyncDefinition(org, uuid);
            }
            api.setAsyncApiDefinition(resourceConfigsString);
        }

        if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
            api.setGraphQLSchema(getGraphqlSchemaDefinition(uuid, organization));
        }

        JsonElement paths = null;
        if (resourceConfigsString != null) {
            JsonObject resourceConfigsJSON = new Gson().fromJson(resourceConfigsString, JsonObject.class);
            paths = resourceConfigsJSON.get(APIConstants.SWAGGER_PATHS);
        }
        Set<URITemplate> uriTemplates = apiMgtDAO.getURITemplatesOfAPI(api.getUuid());
        for (URITemplate uriTemplate : uriTemplates) {
            String uTemplate = uriTemplate.getUriTemplate();
            String method = uriTemplate.getHTTPVerb();
            List<Scope> oldTemplateScopes = uriTemplate.retrieveAllScopes();
            List<Scope> newTemplateScopes = new ArrayList<>();
            if (!oldTemplateScopes.isEmpty()) {
                for (Scope templateScope : oldTemplateScopes) {
                    Scope scope = scopeToKeyMapping.get(templateScope.getKey());
                    newTemplateScopes.add(scope);
                }
            }
            uriTemplate.addAllScopes(newTemplateScopes);
            uriTemplate.setResourceURI(api.getUrl());
            uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
            // AWS Lambda: set arn & timeout to URI template
            if (paths != null) {
                JsonElement path = paths.getAsJsonObject().get(uTemplate);
                if (path != null) {
                    JsonElement operation = path.getAsJsonObject().get(method.toLowerCase());
                    if (operation != null) {
                        if (operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME) != null) {
                            uriTemplate.setAmznResourceName(
                                    operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME)
                                            .toString());
                        }
                        if (operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT) != null) {
                            uriTemplate.setAmznResourceTimeout(
                                    operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)
                                            .getAsInt());
                        }
                        if (operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED) != null) {
                            uriTemplate.setAmznResourceContentEncoded(operation.getAsJsonObject().
                                    get(APIConstants.SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED).getAsBoolean());
                        }
                    }
                }
            }
        }

        if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
            for (URITemplate template : uriTemplates) {
                template.setMediationScript(template.getAggregatedMediationScript());
            }
        }
        api.setUriTemplates(uriTemplates);
        //CORS . if null is returned, set default config from the configuration
        if (api.getCorsConfiguration() == null) {
            api.setCorsConfiguration(APIUtil.getDefaultCorsConfiguration());
        }

        // set category
        List<APICategory> categories = api.getApiCategories();
        if (categories != null) {
            List<String> categoriesOfAPI = new ArrayList<String>();
            for (APICategory apiCategory : categories) {
                categoriesOfAPI.add(apiCategory.getName());
            }
            List<APICategory> categoryList = new ArrayList<>();

            if (!categoriesOfAPI.isEmpty()) {
                // category array retrieved from artifact has only the category name, therefore we need to fetch
                // categories
                // and fill out missing attributes before attaching the list to the api
                List<APICategory> allCategories = new ArrayList<>();
                if (migrationEnabled == null) {
                    allCategories = APIUtil.getAllAPICategoriesOfOrganization(organization);
                }
                // todo-category: optimize this loop with breaks
                for (String categoryName : categoriesOfAPI) {
                    for (APICategory category : allCategories) {
                        if (categoryName.equals(category.getName())) {
                            categoryList.add(category);
                            break;
                        }
                    }
                }
            }
            api.setApiCategories(categoryList);
        }
    }

    protected void populateDevPortalAPIInformation(String uuid, String organization, API api)
            throws APIManagementException, OASPersistenceException, ParseException {
        Organization org = new Organization(organization);
        //UUID
        if (api.getUuid() == null) {
            api.setUuid(uuid);
        }
        api.setOrganization(organization);
        // environment
        String environmentString = null;
        if (api.getEnvironments() != null) {
            environmentString = String.join(",", api.getEnvironments());
        }
        api.setEnvironments(APIUtil.extractEnvironmentsForAPI(environmentString, organization));
        // workflow status
        APIIdentifier apiId = api.getId();
        String currentApiUuid = uuid;
        if (api.isRevision() && api.getRevisionedApiId() != null) {
            currentApiUuid = api.getRevisionedApiId();
        }
        // TODO try to use a single query to get info from db
        // Ratings
        int internalId = apiMgtDAO.getAPIID(currentApiUuid);
        api.setRating(APIUtil.getAverageRating(internalId));
        apiId.setId(internalId);
        // api level tier
        String apiLevelTier = apiMgtDAO.getAPILevelTier(internalId);
        api.setApiLevelPolicy(apiLevelTier);

        // available tier
        String tiers = null;
        Set<Tier> tiersSet = api.getAvailableTiers();
        Set<String> tierNameSet = new HashSet<String>();
        for (Tier t : tiersSet) {
            tierNameSet.add(t.getName());
        }
        if (api.getAvailableTiers() != null) {
            tiers = String.join("||", tierNameSet);
        }
        Map<String, Tier> definedTiers = APIUtil.getTiers(APIUtil.getInternalOrganizationId(organization));
        Set<Tier> availableTier = APIUtil.getAvailableTiers(definedTiers, tiers, api.getId().getApiName());
        api.setAvailableTiers(availableTier);

        //Scopes
        Map<String, Scope> scopeToKeyMapping = APIUtil.getAPIScopes(currentApiUuid, organization);
        api.setScopes(new LinkedHashSet<>(scopeToKeyMapping.values()));

        //templates
        String resourceConfigsString = null;
        if (api.getSwaggerDefinition() != null) {
            resourceConfigsString = api.getSwaggerDefinition();
        } else {
            resourceConfigsString = apiPersistenceInstance.getOASDefinition(org, uuid);
        }
        api.setSwaggerDefinition(resourceConfigsString);

        if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
            api.setGraphQLSchema(getGraphqlSchemaDefinition(uuid, organization));
        }

        JsonElement paths = null;
        if (resourceConfigsString != null) {
            JsonObject resourceConfigsJSON = new Gson().fromJson(resourceConfigsString, JsonObject.class);
            paths = resourceConfigsJSON.get(APIConstants.SWAGGER_PATHS);
        }
        Set<URITemplate> uriTemplates = apiMgtDAO.getURITemplatesOfAPI(api.getUuid());
        for (URITemplate uriTemplate : uriTemplates) {
            String uTemplate = uriTemplate.getUriTemplate();
            String method = uriTemplate.getHTTPVerb();
            List<Scope> oldTemplateScopes = uriTemplate.retrieveAllScopes();
            List<Scope> newTemplateScopes = new ArrayList<>();
            if (!oldTemplateScopes.isEmpty()) {
                for (Scope templateScope : oldTemplateScopes) {
                    Scope scope = scopeToKeyMapping.get(templateScope.getKey());
                    newTemplateScopes.add(scope);
                }
            }
            uriTemplate.addAllScopes(newTemplateScopes);
            uriTemplate.setResourceURI(api.getUrl());
            uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
            // AWS Lambda: set arn & timeout to URI template
            if (paths != null) {
                JsonElement path = paths.getAsJsonObject().get(uTemplate);
                if (path != null) {
                    JsonElement operation = path.getAsJsonObject().get(method.toLowerCase());
                    if (operation != null) {
                        if (operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME) != null) {
                            uriTemplate.setAmznResourceName(
                                    operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME)
                                            .toString());
                        }
                        if (operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT) != null) {
                            uriTemplate.setAmznResourceTimeout(
                                    operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)
                                            .getAsInt());
                        }
                        if (operation.getAsJsonObject().get(APIConstants.SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED) != null) {
                            uriTemplate.setAmznResourceContentEncoded(operation.getAsJsonObject().
                                    get(APIConstants.SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED).getAsBoolean());
                        }
                    }
                }
            }
        }

        if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
            for (URITemplate template : uriTemplates) {
                template.setMediationScript(template.getAggregatedMediationScript());
            }
        }
        api.setUriTemplates(uriTemplates);
        //CORS . if null is returned, set default config from the configuration
        if (api.getCorsConfiguration() == null) {
            api.setCorsConfiguration(APIUtil.getDefaultCorsConfiguration());
        }

        // set category
        List<APICategory> categories = api.getApiCategories();
        if (categories != null) {
            List<String> categoriesOfAPI = new ArrayList<String>();
            for (APICategory apiCategory : categories) {
                categoriesOfAPI.add(apiCategory.getName());
            }
            List<APICategory> categoryList = new ArrayList<>();

            if (!categoriesOfAPI.isEmpty() && migrationEnabled == null) {
                // category array retrieved from artifact has only the category name, therefore we need to fetch
                // categories
                // and fill out missing attributes before attaching the list to the api
                List<APICategory> allCategories = APIUtil.getAllAPICategoriesOfOrganization(organization);

                for (String categoryName : categoriesOfAPI) {
                    for (APICategory category : allCategories) {
                        if (categoryName.equals(category.getName())) {
                            categoryList.add(category);
                            break;
                        }
                    }
                }
            }
            api.setApiCategories(categoryList);
        }
    }

    protected void populateAPIProductInformation(String uuid, String organization, APIProduct apiProduct)
            throws APIManagementException, OASPersistenceException, ParseException {
        Organization org = new Organization(organization);
        apiProduct.setOrganization(organization);
        ApiMgtDAO.getInstance().setAPIProductFromDB(apiProduct);
        apiProduct.setRating(Float.toString(APIUtil.getAverageRating(apiProduct.getProductId())));

        List<APIProductResource> resources = ApiMgtDAO.getInstance().
                getAPIProductResourceMappings(apiProduct.getId());

        Map<String, Scope> uniqueAPIProductScopeKeyMappings = new LinkedHashMap<>();
        for (APIProductResource resource : resources) {
            List<Scope> resourceScopes = resource.getUriTemplate().retrieveAllScopes();
            ListIterator it = resourceScopes.listIterator();
            while (it.hasNext()) {
                Scope resourceScope = (Scope) it.next();
                String scopeKey = resourceScope.getKey();
                if (!uniqueAPIProductScopeKeyMappings.containsKey(scopeKey)) {
                    resourceScope = APIUtil.getScopeByName(scopeKey, organization);
                    uniqueAPIProductScopeKeyMappings.put(scopeKey, resourceScope);
                } else {
                    resourceScope = uniqueAPIProductScopeKeyMappings.get(scopeKey);
                }
                it.set(resourceScope);
            }
        }

        for (APIProductResource resource : resources) {
            String resourceAPIUUID = resource.getApiIdentifier().getUUID();
            resource.setApiId(resourceAPIUUID);
            try {
                PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(org, resourceAPIUUID);
                API api = APIMapper.INSTANCE.toApi(publisherAPI);
                if (api.isAdvertiseOnly()) {
                    resource.setEndpointConfig(APIUtil.generateEndpointConfigForAdvertiseOnlyApi(api));
                } else {
                    resource.setEndpointConfig(api.getEndpointConfig());
                }
                resource.setEndpointSecurityMap(APIUtil.setEndpointSecurityForAPIProduct(api));
            } catch (APIPersistenceException e) {
                throw new APIManagementException("Error while retrieving the api for api product " + e);
            }

        }
        apiProduct.setProductResources(resources);

        //UUID
        if (apiProduct.getUuid() == null) {
            apiProduct.setUuid(uuid);
        }
        // environment
        String environmentString = null;
        if (apiProduct.getEnvironments() != null) {
            environmentString = String.join(",", apiProduct.getEnvironments());
        }
        apiProduct.setEnvironments(APIUtil.extractEnvironmentsForAPI(environmentString, organization));

        // workflow status
        APIProductIdentifier productIdentifier = apiProduct.getId();
        WorkflowDTO workflow;
        String currentApiProductUuid = uuid;
        if (apiProduct.isRevision() && apiProduct.getRevisionedApiProductId() != null) {
            currentApiProductUuid = apiProduct.getRevisionedApiProductId();
        }
        workflow = APIUtil.getAPIWorkflowStatus(currentApiProductUuid, WF_TYPE_AM_API_PRODUCT_STATE);
        if (workflow != null) {
            WorkflowStatus status = workflow.getStatus();
            apiProduct.setWorkflowStatus(status.toString());
        }

        // available tier
        String tiers = null;
        Set<Tier> tiersSet = apiProduct.getAvailableTiers();
        Set<String> tierNameSet = new HashSet<String>();
        for (Tier t : tiersSet) {
            tierNameSet.add(t.getName());
        }
        if (apiProduct.getAvailableTiers() != null) {
            tiers = String.join("||", tierNameSet);
        }
        Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
        Set<Tier> availableTier = APIUtil.getAvailableTiers(definedTiers, tiers, apiProduct.getId().getName());
        apiProduct.setAvailableTiers(availableTier);

        //Scopes
        /*
        Map<String, Scope> scopeToKeyMapping = APIUtil.getAPIScopes(api.getId(), requestedTenantDomain);
        apiProduct.setScopes(new LinkedHashSet<>(scopeToKeyMapping.values()));
        */
        //templates
        String resourceConfigsString = null;
        if (apiProduct.getDefinition() != null) {
            resourceConfigsString = apiProduct.getDefinition();
        } else {
            resourceConfigsString = apiPersistenceInstance.getOASDefinition(org, uuid);
            apiProduct.setDefinition(resourceConfigsString);
        }
        //CORS . if null is returned, set default config from the configuration
        if (apiProduct.getCorsConfiguration() == null) {
            apiProduct.setCorsConfiguration(APIUtil.getDefaultCorsConfiguration());
        }

        // set category
        List<APICategory> categories = apiProduct.getApiCategories();
        if (categories != null) {
            List<String> categoriesOfAPI = new ArrayList<String>();
            for (APICategory apiCategory : categories) {
                categoriesOfAPI.add(apiCategory.getName());
            }
            List<APICategory> categoryList = new ArrayList<>();

            if (!categoriesOfAPI.isEmpty()) {
                // category array retrieved from artifact has only the category name, therefore we need to fetch
                // categories
                // and fill out missing attributes before attaching the list to the api
                List<APICategory> allCategories = APIUtil.getAllAPICategoriesOfOrganization(organization);

                // todo-category: optimize this loop with breaks
                for (String categoryName : categoriesOfAPI) {
                    for (APICategory category : allCategories) {
                        if (categoryName.equals(category.getName())) {
                            categoryList.add(category);
                            break;
                        }
                    }
                }
            }
            apiProduct.setApiCategories(categoryList);
        }
    }

    @Override
    public ResourceFile getIcon(String apiId, String organization) throws APIManagementException {

        try {
            org.wso2.carbon.apimgt.persistence.dto.ResourceFile resource = apiPersistenceInstance
                    .getThumbnail(new Organization(organization), apiId);
            if (resource != null) {
                ResourceFile thumbnail = new ResourceFile(resource.getContent(), resource.getContentType());
                return thumbnail;
            }
        } catch (ThumbnailPersistenceException e) {
            throw new APIManagementException("Error while accessing thumbnail resource ", e);
        }
        return null;
    }

    protected boolean isOauthAppValidation() {

        String oauthAppValidation = getAPIManagerConfiguration()
                .getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENABLE_PROVISION_APP_VALIDATION);
        if (StringUtils.isNotEmpty(oauthAppValidation)) {
            return Boolean.parseBoolean(oauthAppValidation);
        }
        return true;
    }
}
