/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.portalNotifications;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.PortalNotificationDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.portalNotifications.PortalNotificationDTO;
import org.wso2.carbon.apimgt.impl.dto.portalNotifications.PortalNotificationEndUserDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This class used to insert workflow related notifications to the database,
 * whenever admin approves or rejects a workflow request
 */
public class WorkflowNotificationServiceImpl implements PortalNotificationService<WorkflowDTO> {

    private static final Log log = LogFactory.getLog(WorkflowNotificationServiceImpl.class);

    /**
     * This method will insert workflow related notifications to the database
     *
     * @param workflowDTO  WorkflowDTO object that contains the workflow details
     * @param tenantDomainOfUser Tenant domain of the user
     */
    public void sendPortalNotifications(WorkflowDTO workflowDTO, String tenantDomainOfUser) {
        try {
            PortalNotificationDTO portalNotificationsDTO = new PortalNotificationDTO();
            portalNotificationsDTO.setNotificationType(getNotificationType(workflowDTO.getWorkflowType()));
            portalNotificationsDTO.setCreatedTime(new java.sql.Timestamp(new java.util.Date().getTime()));
            portalNotificationsDTO.setNotificationMetadata(getNotificationMetaData(workflowDTO));
            portalNotificationsDTO.setEndUsers(getDestinationUser(workflowDTO, tenantDomainOfUser));

            boolean result = PortalNotificationDAO.getInstance().addNotification(portalNotificationsDTO);

            if (!result) {
                log.error("Error while adding publisher developer notification - sendPortalNotifications()");
            }
        } catch (APIManagementException e) {
            log.error("Error while sending portal notifications");
        }
    }

    /**
     * This method will return the type of the notification
     *
     * @param workflowType  workflow type
     * @return PortalNotificationType type of the notification
     */
    private PortalNotificationType getNotificationType(String workflowType) {
        switch (workflowType) {
        case WorkflowConstants.WF_TYPE_AM_API_STATE:
            return PortalNotificationType.API_STATE_CHANGE;
        case WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE:
            return PortalNotificationType.API_PRODUCT_STATE_CHANGE;
        case WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION:
            return PortalNotificationType.APPLICATION_CREATION;
        case WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT:
            return PortalNotificationType.API_REVISION_DEPLOYMENT;
        case WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION:
            return PortalNotificationType.APPLICATION_REGISTRATION_PRODUCTION;
        case WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX:
            return PortalNotificationType.APPLICATION_REGISTRATION_SANDBOX;
        case WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION:
            return PortalNotificationType.SUBSCRIPTION_CREATION;
        case WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE:
            return PortalNotificationType.SUBSCRIPTION_UPDATE;
        case WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION:
            return PortalNotificationType.SUBSCRIPTION_DELETION;
        }
        return null;
    }

    /**
     * This method will return a list of users that need to send notifications based on the workflow type
     *
     * @param workflowDTO  WorkflowDTO object that contains the workflow details
     * @param tenantDomainOfUser Tenant domain of the user
     * @return list of PortalNotificationEndUserDTO objects
     */
    private List<PortalNotificationEndUserDTO> getDestinationUser(WorkflowDTO workflowDTO, String tenantDomainOfUser)
            throws APIManagementException {
        List<PortalNotificationEndUserDTO> destinationUserList = new ArrayList<>();
        String destinationUser = null;

        switch (workflowDTO.getWorkflowType()) {
        case WorkflowConstants.WF_TYPE_AM_API_STATE:
        case WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE:
            destinationUser = workflowDTO.getMetadata("Invoker");
            break;

        case WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION:
        case WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT:
        case WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION:
        case WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX:
            destinationUser = workflowDTO.getProperties("userName");
            break;

        case WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION:
        case WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE:
        case WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION:
            destinationUser = workflowDTO.getProperties("subscriber");
            break;

        case WorkflowConstants.WF_TYPE_AM_USER_SIGNUP:
            destinationUser = workflowDTO.getMetadata("userName");
            break;
        }

        if (destinationUser != null) {
            PortalNotificationEndUserDTO endUser = new PortalNotificationEndUserDTO();
            endUser.setDestinationUser(destinationUser);
            endUser.setOrganization(workflowDTO.getTenantDomain());
            endUser.setPortalToDisplay(setPortalToDisplay(workflowDTO.getWorkflowType()));
            destinationUserList.add(endUser);
        }

        if (workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_API_STATE) || workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE)) {
            if (workflowDTO.getMetadata(APIConstants.PortalNotifications.ACTION_META)
                    .equals(APIConstants.BLOCK) || workflowDTO.getMetadata(APIConstants.PortalNotifications.ACTION_META)
                    .equals(APIConstants.DEPRECATE) || workflowDTO.getMetadata(
                    APIConstants.PortalNotifications.ACTION_META).equals(APIConstants.RETIRE)) {
                String apiUUID = null;
                String apiName = workflowDTO.getProperties(APIConstants.PortalNotifications.API_NAME);
                String apiContext = workflowDTO.getMetadata(APIConstants.PortalNotifications.API_CONTEXT_META);
                String apiVersion = workflowDTO.getProperties(APIConstants.PortalNotifications.API_VERSION);
                String provider = workflowDTO.getMetadata(APIConstants.PortalNotifications.API_PROVIDER);
                try {
                    apiUUID = getAPIUUIDUsingNameContextVersion(apiName, apiContext, apiVersion,
                            workflowDTO.getTenantDomain());
                    APIIdentifier apiIdEmailReplaced = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName,
                            apiVersion);
                    List<SubscribedAPI> subscribers = getSubscribersOfAPI(apiUUID, workflowDTO.getTenantDomain(),
                            apiIdEmailReplaced);
                    for (SubscribedAPI subscriber : subscribers) {
                        PortalNotificationEndUserDTO endUser = new PortalNotificationEndUserDTO();
                        endUser.setDestinationUser(subscriber.getSubscriber().getName());
                        endUser.setOrganization(subscriber.getOrganization());
                        endUser.setPortalToDisplay(APIConstants.PortalNotifications.DEV_PORTAL);
                        destinationUserList.add(endUser);
                    }
                } catch (APIManagementException e) {
                    APIUtil.handleException("Error while getting subscribers of API - getDestinationUser()", e);
                }
            }
        }

        if (APIUtil.isMultiGroupAppSharingEnabled()) {
            try {
                List<User> users = getAllUsersBelongToGroup(workflowDTO, tenantDomainOfUser);
                for (User user : users) {
                    PortalNotificationEndUserDTO endUser = new PortalNotificationEndUserDTO();
                    if (user.getUsername().equals(destinationUser)) {
                        continue;
                    }
                    endUser.setDestinationUser(user.getUsername());
                    endUser.setOrganization(user.getTenantDomain());
                    endUser.setPortalToDisplay(APIConstants.PortalNotifications.DEV_PORTAL);
                    destinationUserList.add(endUser);
                }
            } catch (APIManagementException e) {
                APIUtil.handleException("Error while getting users belong to group - getDestinationUser()", e);
            } catch (UserStoreException e) {
                APIUtil.handleException("User-Store Exception ", e);
            }
        }

        return destinationUserList;
    }

    /**
     * This method will return a PortalNotificationMetaData object containing metadata related to the notification
     *
     * @param workflowDTO  WorkflowDTO object that contains the workflow details
     * @return PortalNotificationMetaData object containing metadata related to the notification
     */
    private PortalNotificationMetaData getNotificationMetaData(WorkflowDTO workflowDTO) {
        PortalNotificationMetaData portalNotificationMetaData = new PortalNotificationMetaData();

        portalNotificationMetaData.setApi(workflowDTO.getProperties(APIConstants.PortalNotifications.API_NAME));
        portalNotificationMetaData.setApiVersion(
                workflowDTO.getProperties(APIConstants.PortalNotifications.API_VERSION));
        portalNotificationMetaData.setAction(workflowDTO.getProperties(APIConstants.PortalNotifications.ACTION));
        portalNotificationMetaData.setApplicationName(
                workflowDTO.getProperties(APIConstants.PortalNotifications.APPLICATION_NAME));
        portalNotificationMetaData.setRequestedTier(
                workflowDTO.getProperties(APIConstants.PortalNotifications.REQUESTED_TIER));
        portalNotificationMetaData.setRevisionId(
                workflowDTO.getProperties(APIConstants.PortalNotifications.REVISION_ID));
        portalNotificationMetaData.setComment(workflowDTO.getComments());

        if (WorkflowConstants.WF_TYPE_AM_API_STATE.equals(
                workflowDTO.getWorkflowType()) || WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE.equals(
                workflowDTO.getWorkflowType())) {
            portalNotificationMetaData.setApiContext(workflowDTO.getMetadata(APIConstants.PortalNotifications.API_CONTEXT_META));
        }

        return portalNotificationMetaData;
    }

    /**
     * This method will return the portal that the notifications should display
     *
     * @param workflowType  workflow type
     * @return the portal that the notifications should display
     */
    private String setPortalToDisplay(String workflowType) {
        switch (workflowType) {
        case WorkflowConstants.WF_TYPE_AM_API_STATE:
        case WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE:
        case WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT:
            return APIConstants.PortalNotifications.PUBLISHER_PORTAL;
        default:
            return APIConstants.PortalNotifications.DEV_PORTAL;
        }
    }

    /**
     * This method will return the UUID of a given API
     *
     * @param apiName Name of the API
     * @param apiContext Context of the API
     * @param apiVersion Version of the API
     * @return the UUID of the API
     */
    private String getAPIUUIDUsingNameContextVersion(String apiName, String apiContext, String apiVersion,
            String organization) throws APIManagementException {
        return PortalNotificationDAO.getInstance()
                .getAPIUUIDUsingNameContextVersion(apiName, apiContext, apiVersion, organization);
    }

    /**
     * This method will return the list of subscribers of a given API
     *
     * @param apiUUID UUID of the API
     * @param organization Organization of the API
     * @param apiIdEmailReplaced APIIdentifier object with email replaced provider name
     * @return the subscribers of the API
     */
    private List<SubscribedAPI> getSubscribersOfAPI(String apiUUID, String organization,
            APIIdentifier apiIdEmailReplaced) throws APIManagementException {
        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();
        Set<String> uniqueUserSet = new HashSet<>();
        try {
            UserApplicationAPIUsage[] allApiResult = ApiMgtDAO.getInstance()
                    .getAllAPIUsageByProviderAndApiId(apiUUID, organization);
            for (UserApplicationAPIUsage usage : allApiResult) {
                for (SubscribedAPI apiSubscription : usage.getApiSubscriptions()) {
                    APIIdentifier subsApiId = apiSubscription.getAPIIdentifier();
                    APIIdentifier subsApiIdEmailReplaced = new APIIdentifier(
                            APIUtil.replaceEmailDomain(subsApiId.getProviderName()), subsApiId.getApiName(),
                            subsApiId.getVersion());
                    if (subsApiIdEmailReplaced.equals(apiIdEmailReplaced)) {
                        String userIdentifier = apiSubscription.getSubscriber()
                                .getName() + "-" + apiSubscription.getOrganization();
                        if (uniqueUserSet.add(userIdentifier)) {
                            subscribedAPIs.add(apiSubscription);
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            APIUtil.handleException("Error while getting API usage by API ID - getAPIUsageByAPIId()", e);
        }
        return subscribedAPIs;
    }

    /**
     * This method will return all the users belongs to a given group
     *
     * @param workflowDTO WorkflowDTO object that contains the workflow details
     * @param tenantDomainOfUser Tenant domain of the user
     * @return a list of users belongs to a given group
     */
    private List<User> getAllUsersBelongToGroup(WorkflowDTO workflowDTO, String tenantDomainOfUser)
            throws APIManagementException, UserStoreException {
        List<User> users = new ArrayList<>();
        String groupId = null;
        if (workflowDTO.getWorkflowType().equals(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION)) {
            String applicationId = workflowDTO.getWorkflowReference();
            int appId = Integer.parseInt(applicationId);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Application application = apiMgtDAO.getApplicationById(appId);
            groupId = application.getGroupId();
        } else if (workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION) || workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE) || workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION)) {
            SubscribedAPI sub = ApiMgtDAO.getInstance()
                    .getSubscriptionById(Integer.parseInt(workflowDTO.getWorkflowReference()));
            groupId = sub.getApplication().getGroupId();
        } else if (workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION) || workflowDTO.getWorkflowType()
                .equals(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX)) {
            groupId = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getApplication().getGroupId();
        }

        if (groupId != null) {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainOfUser);
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            if (realm != null) {
                UserStoreManager manager = realm.getUserStoreManager();
                AbstractUserStoreManager abstractManager = (AbstractUserStoreManager) manager;
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String claim = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI);
                if (StringUtils.isBlank(claim)) {
                    claim = APIConstants.PortalNotifications.DEFAULT_CLAIM;
                }
                users = abstractManager.getUserListWithID(claim, groupId, "default");
            }

        }
        return users;
    }

}
