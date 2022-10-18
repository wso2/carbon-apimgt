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

package org.wso2.apk.apimgt.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIInfo;
import org.wso2.apk.apimgt.api.model.APIKey;
import org.wso2.apk.apimgt.api.model.APIProductIdentifier;
import org.wso2.apk.apimgt.api.model.APIProductResource;
import org.wso2.apk.apimgt.api.model.AccessTokenInfo;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.Documentation;
import org.wso2.apk.apimgt.api.model.ResourceFile;
import org.wso2.apk.apimgt.api.model.DocumentationContent;
import org.wso2.apk.apimgt.api.model.KeyManager;
import org.wso2.apk.apimgt.api.model.Identifier;
import org.wso2.apk.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.api.model.Subscriber;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.impl.dao.dto.DocumentContent;
import org.wso2.apk.apimgt.impl.dao.dto.DocumentSearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.Organization;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherAPIInfo;
import org.wso2.apk.apimgt.impl.dao.dto.PublisherAPISearchResult;
import org.wso2.apk.apimgt.impl.dao.dto.UserContext;
import org.wso2.apk.apimgt.impl.dao.exceptions.AsyncSpecPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.DocumentationPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.GraphQLPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.OASPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.ThumbnailPersistenceException;
import org.wso2.apk.apimgt.impl.dao.exceptions.WSDLPersistenceException;
import org.wso2.apk.apimgt.impl.dao.mapper.APIMapper;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIManager;
import org.wso2.apk.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.ApplicationNameWhiteSpaceValidationException;
import org.wso2.apk.apimgt.api.ApplicationNameWithInvalidCharactersException;
import org.wso2.apk.apimgt.api.BlockConditionNotFoundException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.PolicyNotFoundException;
import org.wso2.apk.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.apk.apimgt.api.model.policy.Policy;
import org.wso2.apk.apimgt.api.model.policy.PolicyConstants;
import org.wso2.apk.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.apk.apimgt.impl.dao.ScopesDAO;
import org.wso2.apk.apimgt.impl.dao.impl.*;
import org.wso2.apk.apimgt.impl.dao.mapper.DocumentMapper;
import org.wso2.apk.apimgt.impl.dto.ThrottleProperties;
import org.wso2.apk.apimgt.impl.utils.APINameComparator;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.impl.utils.TierNameComparator;
import org.wso2.apk.apimgt.user.exceptions.UserException;
import org.wso2.apk.apimgt.user.mgt.internal.UserManagerHolder;
import org.wso2.apk.apimgt.impl.dao.EnvironmentSpecificAPIPropertyDAO;

import java.util.*;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {

    // API definitions from swagger v2.0
    protected Log log = LogFactory.getLog(getClass());
    protected ApiMgtDAO apiMgtDAO;
    protected ApiDAOImpl apiDAOImpl;
    protected EnvironmentDAOImpl environmentDAO;
    protected ApplicationDAOImpl applicationDAOImpl;
    protected TierDAOImpl tierDAOImpl;
    protected WorkflowDAOImpl workflowDAOImpl;
    protected PolicyDAOImpl policyDAOImpl;
    protected BlockConditionDAOImpl blockConditionDAOImpl;
    protected ScopeDAOImpl scopeDAOImpl;
    protected CommentDAOImpl commentDAOImpl;
    protected ConsumerDAOImpl consumerDAOImpl;
    protected KeyManagerDAOImpl keyManagerDAOImpl;
    protected EnvironmentSpecificAPIPropertyDAO environmentSpecificAPIPropertyDAO;
    protected ScopesDAO scopesDAO;
    protected int tenantId = -1; //-1 the issue does not occur.;
    protected String tenantDomain;
    protected String organization;
    protected String username;
    // Property to indicate whether access control restriction feature is enabled.
    protected boolean isAccessControlRestrictionEnabled = false;
    private static final String REGISTRY_ANONNYMOUS_USERNAME = "wso2.anonymous.user";

    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";

    public AbstractAPIManager() throws APIManagementException {

    }

    public AbstractAPIManager(String username) throws APIManagementException {
        this(username, StringUtils.isNoneBlank(username) ? getTenantDomain(username)
                : APIConstants.SUPER_TENANT_DOMAIN);
    }

    public AbstractAPIManager(String username, String organization) throws APIManagementException {
        apiDAOImpl = ApiDAOImpl.getInstance();
        apiMgtDAO = ApiMgtDAO.getInstance();
        scopesDAO = ScopesDAO.getInstance();
        environmentSpecificAPIPropertyDAO = EnvironmentSpecificAPIPropertyDAO.getInstance();
        environmentDAO = EnvironmentDAOImpl.getInstance();
        applicationDAOImpl = ApplicationDAOImpl.getInstance();
        tierDAOImpl = TierDAOImpl.getInstance();
        workflowDAOImpl = WorkflowDAOImpl.getInstance();
        policyDAOImpl = PolicyDAOImpl.getInstance();
        blockConditionDAOImpl = BlockConditionDAOImpl.getInstance();
        scopeDAOImpl = ScopeDAOImpl.getInstance();
        commentDAOImpl = CommentDAOImpl.getInstance();
        consumerDAOImpl = ConsumerDAOImpl.getInstance();
        keyManagerDAOImpl = KeyManagerDAOImpl.getInstance();

        try {
            if (username == null) {
                this.username = REGISTRY_ANONNYMOUS_USERNAME;
            } else {
                String tenantDomainName = APIUtil.getInternalOrganizationDomain(organization);
                String tenantUserName = getTenantAwareUsername(username);
                int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomainName);
                this.tenantId = tenantId;
                this.tenantDomain = tenantDomainName;
                this.organization = organization;
                this.username = tenantUserName;

            }
        } catch (UserException e) {
            String msg = "Error while getting user registry for user:" + username;
            throw new APIManagementException(msg, e,
                    ExceptionCodes.from(ExceptionCodes.USERSTORE_INITIALIZATION_FAILED));
        }
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
            PublisherAPISearchResult searchAPIs = apiDAOImpl.searchAPIsForPublisher(org, "", 0,
                    Integer.MAX_VALUE, userCtx, null, null);

            if (searchAPIs != null) {
                List<PublisherAPIInfo> list = searchAPIs.getPublisherAPIInfoList();
                for (PublisherAPIInfo publisherAPIInfo : list) {
                    API mappedAPI = APIMapper.INSTANCE.toApi(publisherAPIInfo);
                    apiSortedList.add(mappedAPI);
                }
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while searching the api ", e);
        }

        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
    }

    protected String getTenantAwareUsername(String username) throws APIManagementException {

        return APIUtil.getTenantAwareUsername(username);
    }

    protected String getTenantDomain(Identifier identifier) throws APIManagementException {

        return APIUtil.getTenantDomain(
                APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
    }

    protected void populateDefaultVersion(API api) throws APIManagementException {

        apiMgtDAO.setDefaultVersion(api);
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
            org.wso2.apk.apimgt.impl.dao.dto.ResourceFile resource =
                    apiDAOImpl.getWSDL(new Organization(organization), apiId);
            if (resource != null) {
                ResourceFile resourceFile = new ResourceFile(resource.getContent(), resource.getContentType());
                resourceFile.setName(resource.getName());
                return resourceFile;
            } else {
                String msg = "Failed to get WSDL. Artifact corresponding to artifactId " + apiId + " does not exist";
                throw new APIMgtResourceNotFoundException(msg, ExceptionCodes.RESOURCE_NOT_FOUND);
            }
        } catch (WSDLPersistenceException e) {
            String errorMessage = "Error while retrieving wsdl resource for api " + apiId;
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    @Override
    public String getGraphqlSchemaDefinition(String apiId, String tenantDomain) throws APIManagementException {

        String definition;
        try {
            definition = apiDAOImpl.getGraphQLSchema(new Organization(tenantDomain), apiId);
        } catch (GraphQLPersistenceException e) {
            throw new APIManagementException("Error while retrieving graphql definition from the persistence location",
                    e, ExceptionCodes.INTERNAL_ERROR);
        }
        return definition;
    }

    @Override
    public String getOpenAPIDefinition(String apiId, String organization) throws APIManagementException {

        String definition = null;
        try {
            definition = apiDAOImpl.getOASDefinition(new Organization(organization), apiId);
        } catch (OASPersistenceException e) {
            throw new APIManagementException("Error while retrieving OAS definition from the persistence location", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return definition;
    }

    @Override
    public String getAsyncAPIDefinition(String apiId, String organization) throws APIManagementException {

        String definition = null;
        try {
            definition = apiDAOImpl.getAsyncDefinition(new Organization(organization), apiId);
        } catch (AsyncSpecPersistenceException e) {
            throw new APIManagementException("Error while retrieving Async definition from the persistence location", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return definition;
    }

    public List<Documentation> getAllDocumentation(String uuid, String organization) throws APIManagementException {

        String username = org.wso2.apk.apimgt.user.ctx.UserContext.getThreadLocalUserContext().getUsername();

        Organization org = new Organization(organization);
        UserContext ctx = new UserContext(username, org, null, null);
        List<Documentation> convertedList = null;
        try {
            DocumentSearchResult list =
                    apiDAOImpl.searchDocumentation(org, uuid, 0, 0, null, ctx);
            if (list != null) {
                convertedList = new ArrayList<Documentation>();
                List<org.wso2.apk.apimgt.impl.dao.dto.Documentation> docList = list.getDocumentationList();
                if (docList != null) {
                    for (int i = 0; i < docList.size(); i++) {
                        convertedList.add(DocumentMapper.INSTANCE.toDocumentation(docList.get(i)));
                    }
                }
            } else {
                convertedList = new ArrayList<Documentation>();
            }
        } catch (DocumentationPersistenceException e) {
            String msg = "Failed to get documentations for api/product " + uuid;
            throw new APIManagementException(msg, e, ExceptionCodes.INTERNAL_ERROR);
        }
        return convertedList;
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
            org.wso2.apk.apimgt.impl.dao.dto.Documentation doc = apiDAOImpl
                    .getDocumentation(new Organization(organization), apiId, docId);
            if (doc != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved doc: " + doc);
                }
                documentation = DocumentMapper.INSTANCE.toDocumentation(doc);
            } else {
                String msg = "Failed to get the document. Artifact corresponding to document id " + docId
                        + " does not exist";
                throw new APIManagementException(msg, ExceptionCodes.DOCUMENT_NOT_FOUND);
            }
        } catch (DocumentationPersistenceException e) {
            throw new APIManagementException("Error while retrieving document for id " + docId, e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return documentation;
    }

    @Override
    public DocumentationContent getDocumentationContent(String apiId, String docId, String organization)
            throws APIManagementException {

        try {
            DocumentContent content = apiDAOImpl
                    .getDocumentationContent(new Organization(organization), apiId, docId);
            DocumentationContent docContent = null;
            if (content != null) {
                docContent = DocumentMapper.INSTANCE.toDocumentationContent(content);
            } else {
                String msg = "Failed to get the document content. Artifact corresponding to document id " + docId
                        + " does not exist";
                throw new APIManagementException(msg, ExceptionCodes.DOCUMENT_CONTENT_NOT_FOUND);
            }
            return docContent;
        } catch (DocumentationPersistenceException e) {
            throw new APIManagementException("Error while retrieving document content ", e,
                    ExceptionCodes.INTERNAL_ERROR);
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
        if (tenantDomain != null && !SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            context = "/t/" + tenantDomain + context;
        }
        return apiMgtDAO.isContextExist(context, organization);
    }

    protected String getTenantDomainFromUrl(String url) {

        return APIUtil.getTenantDomainFromUrl(url);
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

        String tenantName = SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameExist(apiName, tenantName, organization);
    }

    public boolean isApiNameWithDifferentCaseExist(String apiName, String organization) throws APIManagementException {

        String tenantName = SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            tenantName = tenantDomain;
        }
        return apiMgtDAO.isApiNameWithDifferentCaseExist(apiName, tenantName, organization);
    }

    public void addSubscriber(String username, String groupingId)
            throws APIManagementException {

        Subscriber subscriber = new Subscriber(username);
        subscriber.setSubscribedDate(new Date());
        try {
            int tenantId = UserManagerHolder.getUserManager().getTenantId(getTenantDomain(username));
            subscriber.setEmail(StringUtils.EMPTY);
            subscriber.setTenantId(tenantId);
            apiMgtDAO.addSubscriber(subscriber, groupingId);
            if (APIUtil.isDefaultApplicationCreationEnabled() &&
                    !APIUtil.isDefaultApplicationCreationDisabledForTenant(getTenantDomain(username))) {
                // Add a default application once subscriber is added
                addDefaultApplicationForSubscriber(subscriber);
            }
        } catch (APIManagementException | UserException e) {
            String msg = "Error while adding the subscriber " + subscriber.getName();
            throw new APIManagementException(msg, e);
        }
    }

    protected static String getTenantDomain(String username) throws APIManagementException {

        return APIUtil.getTenantDomain(username);
    }

    /**
     * Add default application on the first time a subscriber is added to the database
     *
     * @param subscriber Subscriber
     * @throws APIManagementException if an error occurs while adding default application
     */
    private void addDefaultApplicationForSubscriber(Subscriber subscriber) throws APIManagementException, UserException {

        Application defaultApp = new Application(APIConstants.DEFAULT_APPLICATION_NAME, subscriber);
        if (APIUtil.isEnabledUnlimitedTier()) {
            defaultApp.setTier(APIConstants.UNLIMITED_TIER);
        } else {
            Map<String, Tier> throttlingTiers = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE,
                    getTenantDomain(subscriber.getName()));
            Set<Tier> tierValueList = new HashSet<Tier>(throttlingTiers.values());
            List<Tier> sortedTierList = APIUtil.sortTiers(tierValueList);
            defaultApp.setTier(sortedTierList.get(0).getName());
        }
        //application will not be shared within the group
        defaultApp.setGroupId("");
        defaultApp.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        defaultApp.setUUID(UUID.randomUUID().toString());
        defaultApp.setDescription(APIConstants.DEFAULT_APPLICATION_DESCRIPTION);
        int applicationId = apiMgtDAO.addApplication(defaultApp, subscriber.getName(), tenantDomain);

        // TODO: send application Event
//        ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
//                System.currentTimeMillis(), APIConstants.EventType.APPLICATION_CREATE.name(), tenantId,
//                tenantDomain, applicationId, defaultApp.getUUID(), defaultApp.getName(),
//                defaultApp.getTokenType(),
//                defaultApp.getTier(), defaultApp.getGroupId(), defaultApp.getApplicationAttributes(),
//                subscriber.getName());
//        APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
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

    protected final void handleExceptionWithCode(String msg, Exception e, ErrorHandler code) throws APIManagementException {

        throw new APIManagementException(msg, e, code);
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

        if (tenantId == -1) {
            tierMap = APIUtil.getAllTiers();
        } else {
            tierMap = APIUtil.getAllTiers(tenantId);
        }

        tiers.addAll(tierMap.values());
        return tiers;
    }

    public Set<Tier> getAllTiers(String tenantDomain) throws APIManagementException {

        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Map<String, Tier> tierMap;

        int requestedTenantId = org.wso2.apk.apimgt.user.ctx.UserContext.getThreadLocalUserContext()
                .getOrganizationId();
        if (requestedTenantId == -1234 || requestedTenantId == -1) {
            tierMap = APIUtil.getAllTiers();
        } else {
            tierMap = APIUtil.getAllTiers(requestedTenantId);
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

        // TODO : // Read Configurations
//        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
//        ThrottleProperties throttleProperties = apiManagerConfiguration.getThrottleProperties();
        ThrottleProperties throttleProperties = null;
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
            if (tenantId == -1234) {
                xWso2Tenant = APIConstants.SUPER_TENANT_DOMAIN;
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
            if (tenantDomain != null && !tenantDomain.equalsIgnoreCase(
                    keyManagerConfigurationDTO.getOrganization())) {
                continue;
            }
            KeyManager keyManager = null;

            // TODO : check keyManagerConfigurationDTO.isEnabled()
//            if (keyManagerConfigurationDTO.isEnabled()) {
//                keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain, keyManagerName);
//            } else {
//                continue;
//            }
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
            resourceConfigsString = apiDAOImpl.getOASDefinition(org, uuid);
        }
        api.setSwaggerDefinition(resourceConfigsString);

        if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
            api.setGraphQLSchema(getGraphqlSchemaDefinition(uuid, organization));
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject paths = null;
        if (resourceConfigsString != null) {
            JSONObject resourceConfigsJSON = (JSONObject) jsonParser.parse(resourceConfigsString);
            paths = (JSONObject) resourceConfigsJSON.get(APIConstants.SWAGGER_PATHS);
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
                JSONObject path = (JSONObject) paths.get(uTemplate);
                if (path != null) {
                    JSONObject operation = (JSONObject) path.get(method.toLowerCase());
                    if (operation != null) {
                        if (operation.containsKey(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME)) {
                            uriTemplate.setAmznResourceName((String)
                                    operation.get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME));
                        }
                        if (operation.containsKey(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)) {
                            uriTemplate.setAmznResourceTimeout(((Long)
                                    operation.get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)).intValue());
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

    @Override
    public ResourceFile getIcon(String apiId, String organization) throws APIManagementException {

        try {
            org.wso2.apk.apimgt.impl.dao.dto.ResourceFile resource = apiDAOImpl.getThumbnail(new Organization(organization), apiId);
            if (resource != null) {
                ResourceFile thumbnail = new ResourceFile(resource.getContent(), resource.getContentType());
                return thumbnail;
            }
        } catch (ThumbnailPersistenceException e) {
            throw new APIManagementException("Error while accessing thumbnail resource ", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    protected boolean isOauthAppValidation() {

        // TODO : Read from  config
//        String oauthAppValidation = null;
//        String oauthAppValidation = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
//                .getAPIManagerConfiguration()
//                .getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENABLE_PROVISION_APP_VALIDATION);
        String oauthAppValidation = null;
        if (StringUtils.isNotEmpty(oauthAppValidation)) {
            return Boolean.parseBoolean(oauthAppValidation);
        }
        return true;
    }
}
