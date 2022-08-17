package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.PersistenceFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.NotificationExecutor;
import org.wso2.carbon.apimgt.impl.notification.NotifierConstants;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderDetailsExtractor;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderEventPublisher;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.impl.lifecycle.LCManagerFactory;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProduct;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.APIProductMapper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import java.io.IOException;
import java.util.*;

/**
 * This class used to LifeCycleUtils.
 */
public class LifeCycleUtils {
    private static final Log log = LogFactory.getLog(LifeCycleUtils.class);
    private static final ApiMgtDAO apiMgtDAO;
    private static final APIPersistence apiPersistence;

    static {
        apiMgtDAO = ApiMgtDAO.getInstance();
        apiPersistence = PersistenceFactory.getAPIPersistenceInstance();
    }

    public static void changeLifecycle(String user, APIProvider apiProvider, String orgId,
                                       ApiTypeWrapper apiTypeWrapper, String action, Map<String,
            Boolean> checklist) throws PersistenceException, APIPersistenceException, APIManagementException, IOException, ParseException {
        String targetStatus;
        String apiName = apiTypeWrapper.getName();
        String apiType = apiTypeWrapper.geType();
        String apiContext = apiTypeWrapper.getContext();
        String uuid = apiTypeWrapper.getUuid();
        String currentStatus = apiTypeWrapper.getStatus();
        targetStatus = LCManagerFactory.getInstance().getLCManager().getStateForTransition(action);
        apiPersistence.changeAPILifeCycle(new Organization(orgId), apiTypeWrapper.getUuid(), targetStatus);
        if (!apiTypeWrapper.isAPIProduct()) {
            API api = apiTypeWrapper.getApi();
            api.setOrganization(orgId);
            changeLifeCycle(apiProvider, api, currentStatus, targetStatus, checklist, orgId);
            //Sending Notifications to existing subscribers
            if (APIConstants.PUBLISHED.equals(targetStatus)) {
                sendEmailNotification(api, orgId);
            }
        } else {
            APIProduct apiProduct = apiTypeWrapper.getApiProduct();
            apiProduct.setOrganization(orgId);
            changeLifecycle(apiProvider, apiProduct, currentStatus, targetStatus);
        }
        addLCStateChangeInDatabase(user, apiTypeWrapper, currentStatus, targetStatus, uuid);
        // Event need to be sent after database status update.
        sendLCStateChangeNotification(apiName, apiType, apiContext, apiTypeWrapper.getId().getVersion(), targetStatus, apiTypeWrapper.getId().getProviderName(),
                apiTypeWrapper.getId().getId(), uuid, orgId);

        // Remove revisions and subscriptions after API retire
        if (!apiTypeWrapper.isAPIProduct()) {
            String newStatus = (targetStatus != null) ? targetStatus.toUpperCase() : targetStatus;
            if (APIConstants.RETIRED.equals(newStatus)) {
                API api = apiTypeWrapper.getApi();
                api.setOrganization(orgId);
                cleanUpPendingSubscriptionCreationProcessesByAPI(api.getUuid());
                apiMgtDAO.removeAllSubscriptions(api.getUuid());
                apiProvider.deleteAPIRevisions(api.getUuid(), orgId);
            }
        }
        if (log.isDebugEnabled()) {
            String logMessage = "LC Status changed successfully for artifact with name: " + apiName
                    + ", version " + apiTypeWrapper.getId().getVersion() + ", New Status : " + targetStatus;
            log.debug(logMessage);
        }
        extractRecommendationDetails(apiTypeWrapper);
    }

    /**
     * Update the lifecycle of API Product in registry
     *
     * @param apiProduct   API Product object
     * @param currentState Current state of the API Product
     * @param targetState  Target state of the API Product
     * @throws APIManagementException Exception when updating the lc state of API Product
     */
    private static void changeLifecycle(APIProvider apiProvider, APIProduct apiProduct, String currentState, String targetState)
            throws APIManagementException {

        if (targetState != null) {
            String newStatus = targetState.toUpperCase();
            if (log.isDebugEnabled()) {
                String logMessage = "Publish changed status to the Gateway. API Name: " + apiProduct.getId().getName()
                        + ", API Version " + apiProduct.getId().getVersion() + ", API Context: "
                        + apiProduct.getContext() + ", New Status : " + newStatus;
                log.debug(logMessage);
            }
            // update api product related information for state change
            updateAPIProductForStateChange(apiProvider, LifeCycleUtils.apiPersistence, apiProduct, currentState, newStatus);
        } else {
            throw new APIManagementException("Invalid Lifecycle status provided for default APIExecutor");
        }
    }

    private static void changeLifeCycle(APIProvider apiProvider, API api, String currentState, String targetState,
                                        Map<String, Boolean> checklist, String organization)
            throws APIManagementException {

        String oldStatus = currentState.toUpperCase();
        String newStatus = (null != targetState) ? targetState.toUpperCase() : targetState;

        boolean isCurrentCreatedOrPrototyped = APIConstants.CREATED.equals(oldStatus)
                || APIConstants.PROTOTYPED.equals(oldStatus);
        boolean isStateTransitionToPublished = isCurrentCreatedOrPrototyped && APIConstants.PUBLISHED.equals(newStatus);
        if (newStatus != null) { // only allow the executor to be used with default LC states transition
            // check only the newStatus so this executor can be used for LC state change from
            // custom state to default api state
            if (isStateTransitionToPublished) {
                Set<Tier> tiers = api.getAvailableTiers();
                String endPoint = api.getEndpointConfig();
                String apiSecurity = api.getApiSecurity();
                boolean isOauthProtected = apiSecurity == null
                        || apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
                if (APIConstants.API_TYPE_WEBSUB.equals(api.getType())
                        || endPoint != null && endPoint.trim().length() > 0
                        || api.isAdvertiseOnly() && (api.getApiExternalProductionEndpoint() != null
                        || api.getApiExternalSandboxEndpoint() != null)) {
                    if ((isOauthProtected && (tiers == null || tiers.size() == 0)) && !api.isAdvertiseOnly()) {
                        throw new APIManagementException("Failed to publish service to API store. No Tiers selected");
                    }
                } else {
                    throw new APIManagementException("Failed to publish service to API store. No endpoint selected");
                }
            }

            // push the state change to gateway
            Map<String, String> failedGateways = propergateAPIStatusChangeToGateways(apiProvider, newStatus, api);

            if (APIConstants.PUBLISHED.equals(newStatus) || !oldStatus.equals(newStatus)) { //TODO has registry access
                //if the API is websocket and if default version is selected, update the other versions
                if (APIConstants.APITransportType.WS.toString().equals(api.getType()) && api.isDefaultVersion()) {
                    Set<String> versions = apiProvider.getAPIVersions(api.getId().getProviderName(), api.getId().getName(),
                            api.getOrganization());
                    for (String version : versions) {
                        if (version.equals(api.getId().getVersion())) {
                            continue;
                        }
                        String uuid = APIUtil.getUUIDFromIdentifier(
                                new APIIdentifier(api.getId().getProviderName(), api.getId().getName(), version),
                                api.getOrganization());
                        API otherApi = apiProvider.getLightweightAPIByUUID(uuid, api.getOrganization());
                        APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                                APIConstants.EventType.API_UPDATE.name(), APIUtil.getInternalOrganizationId(organization),
                                organization,
                                otherApi.getId().getApiName(), otherApi.getId().getId(), otherApi.getUuid(), version,
                                api.getType(), otherApi.getContext(), otherApi.getId().getProviderName(),
                                otherApi.getStatus());
                        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
                    }
                }
            }


            if (log.isDebugEnabled()) {
                String logMessage = "Publish changed status to the Gateway. API Name: " + api.getId().getApiName()
                        + ", API Version " + api.getId().getVersion() + ", API Context: " + api.getContext()
                        + ", New Status : " + newStatus;
                log.debug(logMessage);
            }

            // update api related information for state change
            updateAPIforStateChange(LifeCycleUtils.apiPersistence, api, currentState, newStatus);


            if (log.isDebugEnabled()) {
                String logMessage = "API related information successfully updated. API Name: "
                        + api.getId().getApiName() + ", API Version " + api.getId().getVersion() + ", API Context: "
                        + api.getContext() + ", New Status : " + newStatus;
                log.debug(logMessage);
            }
        } else {
            throw new APIManagementException("Invalid Lifecycle status for default APIExecutor :" + targetState);
        }

        boolean deprecateOldVersions = false;
        boolean makeKeysForwardCompatible = true;
        // If the API status is CREATED/PROTOTYPED ,check for check list items of lifecycle
        if (isCurrentCreatedOrPrototyped) {
            if (checklist != null) {
                if (checklist.containsKey(APIConstants.DEPRECATE_CHECK_LIST_ITEM)) {
                    deprecateOldVersions = checklist.get(APIConstants.DEPRECATE_CHECK_LIST_ITEM);
                }
                if (checklist.containsKey(APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM)) {
                    makeKeysForwardCompatible = !checklist.get(APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM);
                }
            }
        }

        if (isStateTransitionToPublished) {
            if (makeKeysForwardCompatible) {
                makeAPIKeysForwardCompatible(apiProvider, api);
            }
            if (deprecateOldVersions) {
                String provider = APIUtil.replaceEmailDomain(api.getId().getProviderName());
                String apiName = api.getId().getName();
                List<API> apiList = getAPIVersionsByProviderAndName(provider, apiName, api.getOrganization());
                APIVersionComparator versionComparator = new APIVersionComparator();
                for (API oldAPI : apiList) {
                    if (oldAPI.getId().getApiName().equals(api.getId().getApiName())
                            && versionComparator.compare(oldAPI, api) < 0
                            && (APIConstants.PUBLISHED.equals(oldAPI.getStatus()))) {
                        apiProvider.changeLifeCycleStatus(organization, new ApiTypeWrapper(oldAPI),
                                APIConstants.API_LC_ACTION_DEPRECATE, null);

                    }
                }
            }
        }
    }

    /**
     * This method used to send notifications to the previous subscribers of older versions of a given API
     *
     * @param api API object.
     * @throws APIManagementException
     */
    private static void sendEmailNotification(API api, String organization) throws APIManagementException {

        try {
            JSONObject tenantConfig = APIUtil.getTenantConfig(organization);
            int tenantId = APIUtil.getInternalOrganizationId(organization);
            String isNotificationEnabled = "false";

            if (tenantConfig.containsKey(NotifierConstants.NOTIFICATIONS_ENABLED)) {
                isNotificationEnabled = (String) tenantConfig.get(NotifierConstants.NOTIFICATIONS_ENABLED);
            }
            if (JavaUtils.isTrueExplicitly(isNotificationEnabled)) {
                List<APIIdentifier> apiIdentifiers = getOldPublishedAPIList(api);
                for (APIIdentifier oldAPI : apiIdentifiers) {
                    Properties prop = new Properties();
                    prop.put(NotifierConstants.API_KEY, oldAPI);
                    prop.put(NotifierConstants.NEW_API_KEY, api.getId());

                    Set<Subscriber> subscribersOfAPI = apiMgtDAO.getSubscribersOfAPI(oldAPI);
                    prop.put(NotifierConstants.SUBSCRIBERS_PER_API, subscribersOfAPI);

                    NotificationDTO notificationDTO = new NotificationDTO(prop,
                            NotifierConstants.NOTIFICATION_TYPE_NEW_VERSION);
                    notificationDTO.setTenantID(tenantId);
                    notificationDTO.setTenantDomain(organization);
                    new NotificationExecutor().sendAsyncNotifications(notificationDTO);
                }
            }
        } catch (NotificationException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Record LC state change to database
     *
     * @param currentStatus Current state of the artifact
     * @param targetStatus  Target state of the artifact
     * @param uuid          Unique UUID of the artifact
     * @throws APIManagementException Exception if there are any errors when updating LC state change in database
     */
    private static void addLCStateChangeInDatabase(String user, ApiTypeWrapper apiTypeWrapper, String currentStatus,
                                                   String targetStatus, String uuid)
            throws APIManagementException {
        int tenantId = APIUtil.getInternalOrganizationId(apiTypeWrapper.getOrganization());
        if (!currentStatus.equalsIgnoreCase(targetStatus)) {
            apiMgtDAO.recordAPILifeCycleEvent(uuid, currentStatus.toUpperCase(),
                    targetStatus.toUpperCase(), user, tenantId);
        }
    }

    /**
     * @param apiName           Name of the API
     * @param apiType           API Type
     * @param apiContext        API or Product context
     * @param apiVersion        API or Product version
     * @param targetStatus      Target Lifecycle status
     * @param provider          Provider of the API or Product
     * @param apiOrApiProductId unique ID of API or API product
     * @param uuid              unique UUID of API or API Product
     */
    private static void sendLCStateChangeNotification(String apiName, String apiType, String apiContext, String apiVersion,
                                                      String targetStatus, String provider, int apiOrApiProductId,
                                                      String uuid, String organization) throws APIManagementException {

        APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.API_LIFECYCLE_CHANGE.name(), APIUtil.getInternalOrganizationId(organization), organization, apiName, apiOrApiProductId,
                uuid, apiVersion, apiType, apiContext, APIUtil.replaceEmailDomainBack(provider), targetStatus);
        APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
    }

    private static void extractRecommendationDetails(ApiTypeWrapper apiTypeWrapper) {
        RecommendationEnvironment recommendationEnvironment = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getApiRecommendationEnvironment();

        // Extracting API or API Product details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher
                    extractor = new RecommenderDetailsExtractor(apiTypeWrapper, apiTypeWrapper.getOrganization(), APIConstants.ADD_API);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
    }

    /**
     * This method returns a list of previous versions of a given API
     *
     * @param api
     * @return oldPublishedAPIList
     * @throws APIManagementException
     */
    private static List<APIIdentifier> getOldPublishedAPIList(API api) throws APIManagementException {
        List<APIIdentifier> oldPublishedAPIList = new ArrayList<APIIdentifier>();
        List<API> apiList = getAPIVersionsByProviderAndName(api.getId().getProviderName(), api.getId().getName(),
                api.getOrganization());
        APIVersionComparator versionComparator = new APIVersionComparator();
        for (API oldAPI : apiList) {
            if (oldAPI.getId().getApiName().equals(api.getId().getApiName()) &&
                    versionComparator.compare(oldAPI, api) < 0 &&
                    (oldAPI.getStatus().equals(APIConstants.PUBLISHED))) {
                oldPublishedAPIList.add(oldAPI.getId());
            }
        }

        return oldPublishedAPIList;
    }

    private static List<API> getAPIVersionsByProviderAndName(String provider, String apiName, String organization)
            throws APIManagementException {
        return apiMgtDAO.getAllAPIVersions(apiName, provider);
    }

    private static void makeAPIKeysForwardCompatible(APIProvider apiProvider, API api) throws APIManagementException {
        String provider = api.getId().getProviderName();
        String apiName = api.getId().getApiName();
        Set<String> versions = apiProvider.getAPIVersions(provider, apiName, api.getOrganization());
        APIVersionComparator comparator = new APIVersionComparator();
        List<API> sortedAPIs = new ArrayList<API>();
        for (String version : versions) {
            if (version.equals(api.getId().getVersion())) {
                continue;
            }
            API otherApi = new API(new APIIdentifier(provider, apiName, version));//getAPI(new APIIdentifier(provider, apiName, version));
            if (comparator.compare(otherApi, api) < 0 && !APIConstants.RETIRED.equals(otherApi.getStatus())) {
                sortedAPIs.add(otherApi);
            }
        }

        // Get the subscriptions from the latest api version first
        Collections.sort(sortedAPIs, comparator);
        List<SubscribedAPI> subscribedAPIS = apiMgtDAO.makeKeysForwardCompatible(new ApiTypeWrapper(api), sortedAPIs);
        for (SubscribedAPI subscribedAPI : subscribedAPIS) {
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(), subscribedAPI, APIUtil.getInternalOrganizationId(api.getOrganization()), api.getOrganization());
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        }
    }

    private static Map<String, String> propergateAPIStatusChangeToGateways(APIProvider apiProvider, String newStatus, API api)
            throws APIManagementException {
        Map<String, String> failedGateways = new HashMap<String, String>();
        APIIdentifier identifier = api.getId();
        String providerTenantMode = identifier.getProviderName();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            String currentStatus = api.getStatus();

            if (APIConstants.PUBLISHED.equals(newStatus) || !currentStatus.equals(newStatus)) {
                api.setStatus(newStatus);

                api.setAsPublishedDefaultVersion(api.getId().getVersion()
                        .equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));

                apiProvider.loadMediationPoliciesToAPI(api, tenantDomain);

            }

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return failedGateways;
    }

    /**
     * Update API Product in registry for lifecycle state change
     *
     * @param apiProduct    API Product Object
     * @param currentStatus Current state of API Product
     * @param newStatus     New state of API Product
     * @return boolean indicates success or failure
     * @throws APIManagementException if there is an error when updating API Product for lifecycle state
     */
    private static void updateAPIProductForStateChange(APIProvider apiProvider, APIPersistence apiPersistence,
                                                      APIProduct apiProduct, String currentStatus, String newStatus)
            throws APIManagementException {

        String provider = apiProduct.getId().getProviderName();
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(provider));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (!currentStatus.equals(newStatus)) {
                apiProduct.setState(newStatus);
                // If API status changed to publish we should add it to recently added APIs list
                // this should happen in store-publisher cluster domain if deployment is distributed
                // IF new API published we will add it to recently added APIs
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants
                        .RECENTLY_ADDED_API_CACHE_NAME).removeAll();
                if (APIConstants.RETIRED.equals(newStatus)) {
                    cleanUpPendingSubscriptionCreationProcessesByAPI(apiProduct.getUuid());
                    apiMgtDAO.removeAllSubscriptions(apiProduct.getUuid());
                    apiProvider.deleteAPIProductRevisions(apiProduct.getUuid(), tenantDomain);
                }
                PublisherAPIProduct publisherAPIProduct = APIProductMapper.INSTANCE.toPublisherApiProduct(apiProduct);
                try {
                    apiPersistence.updateAPIProduct(new Organization(apiProduct.getOrganization()),
                            publisherAPIProduct);
                } catch (APIPersistenceException e) {
                    handleException("Error while persisting the updated API Product", e);
                }
            }
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Clean-up pending subscriptions of a given API
     *
     * @param uuid API uuid
     * @throws APIManagementException
     */
    private static void cleanUpPendingSubscriptionCreationProcessesByAPI(String uuid) throws APIManagementException {

        WorkflowExecutor createSubscriptionWFExecutor = null;
        try {
            createSubscriptionWFExecutor = WorkflowExecutorFactory.getInstance().getWorkflowExecutor(
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        } catch (WorkflowException e) {
            throw new APIManagementException("Error while obtaining WorkflowExecutor instance for workflow type :" +
                    WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        }
        Set<Integer> pendingSubscriptions = apiMgtDAO.getPendingSubscriptionsByAPIId(uuid);
        String workflowExtRef = null;

        for (int subscription : pendingSubscriptions) {
            try {
                workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceForSubscription(subscription);
                createSubscriptionWFExecutor.cleanUpPendingTask(workflowExtRef);
            } catch (APIManagementException ex) {
                // failed clean-up processes are ignored to prevent failures in API state change flow
                log.warn("Failed to retrieve external workflow reference for subscription for subscription ID: "
                        + subscription);
            } catch (WorkflowException ex) {
                // failed clean-up processes are ignored to prevent failures in API state change flow
                log.warn("Failed to clean-up pending subscription approval task for subscription ID: " + subscription);
            }
        }
    }

    private static boolean updateAPIforStateChange(APIPersistence apiPersistence, API api, String currentStatus, String newStatus)
            throws APIManagementException {

        boolean isSuccess;
        String providerTenantMode = api.getId().getProviderName();

        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerTenantMode));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (!currentStatus.equals(newStatus)) {
                api.setStatus(newStatus);

                // If API status changed to publish we should add it to recently added APIs list
                // this should happen in store-publisher cluster domain if deployment is distributed
                // IF new API published we will add it to recently added APIs
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(APIConstants.RECENTLY_ADDED_API_CACHE_NAME).removeAll();


                api.setAsPublishedDefaultVersion(api.getId().getVersion()
                        .equals(apiMgtDAO.getPublishedDefaultVersion(api.getId())));
                if (APIConstants.RETIRED.equals(newStatus)) {
                    cleanUpPendingSubscriptionCreationProcessesByAPI(api.getUuid());
                }

                //updateApiArtifactNew(api, false, false);

                // For Choreo-Connect gateway, gateway vendor type in the DB will be "wso2/choreo-connect".
                // This value is determined considering the gateway type comes with the request.
                api.setGatewayVendor(APIUtil.setGatewayVendorBeforeInsertion(
                        api.getGatewayVendor(), api.getGatewayType()));
                PublisherAPI publisherAPI = APIMapper.INSTANCE.toPublisherApi(api);
                try {
                    apiPersistence.updateAPI(new Organization(api.getOrganization()), publisherAPI);
                } catch (APIPersistenceException e) {
                    handleException("Error while persisting the updated API ", e);
                }

            }
            isSuccess = true;

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return true;
    }

    private static void handleException(String msg, Exception e) throws APIManagementException {

        throw new APIManagementException(msg, e);
    }
}
