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

package org.wso2.carbon.apimgt.impl.systemNotifications;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.NotificationDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationEndUserDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractWFNotifier extends BaseNotifier {

    private static final Log log = LogFactory.getLog(AbstractWFNotifier.class);

    public abstract NotificationDTO prepareNotification(WorkflowDTO workflowDTO) throws APIManagementException;

    public abstract NotificationMetaData getNotificationMetaData(WorkflowDTO workflowDTO);

    public abstract List<NotificationEndUserDTO> getEndUsers(WorkflowDTO workflowDTO) throws APIManagementException;

    /**
     * This method will return a NotificationEndUserDTO object contains the primary user details
     *
     * @param primaryUser     username of the primary user who make the workflow request
     * @param organization    organization of the primary user
     * @param portalToDisplay portal to display the notification
     * @return a NotificationEndUserDTO object
     */
    protected NotificationEndUserDTO getPrimaryUser(String primaryUser, String organization, String portalToDisplay) {
        NotificationEndUserDTO endUser = new NotificationEndUserDTO();
        if (primaryUser != null) {
            endUser.setDestinationUser(primaryUser);
            endUser.setOrganization(organization);
            endUser.setPortalToDisplay(portalToDisplay);
        }
        return endUser;
    }

    /**
     * This method will return the UUID of a given API
     *
     * @param apiName    Name of the API
     * @param apiContext Context of the API
     * @param apiVersion Version of the API
     * @return the UUID of the API
     */
    protected String getAPIUUIDUsingNameContextVersion(String apiName, String apiContext, String apiVersion,
            String organization) throws APIManagementException {
        return NotificationDAO.getInstance()
                .getAPIUUIDUsingNameContextVersion(apiName, apiContext, apiVersion, organization);
    }

    /**
     * This method will return the list of subscribers of a given API
     *
     * @param apiUUID            UUID of the API
     * @param organization       Organization of the API
     * @param apiIdEmailReplaced APIIdentifier object with email replaced provider name
     * @return the subscribers of the API
     */
    protected List<SubscribedAPI> getSubscribersOfAPI(String apiUUID, String organization,
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

    protected List<NotificationEndUserDTO> getSubscriberListOfAPI(String apiAction, String apiName, String apiContext,
            String apiVersion, String provider, String tenantDomain) throws APIManagementException {
        List<NotificationEndUserDTO> subscriberList = new ArrayList<>();

        String apiUUID = null;
        try {
            apiUUID = getAPIUUIDUsingNameContextVersion(apiName, apiContext, apiVersion, tenantDomain);
            APIIdentifier apiIdEmailReplaced = new APIIdentifier(APIUtil.replaceEmailDomain(provider), apiName,
                    apiVersion);
            List<SubscribedAPI> subscribers = getSubscribersOfAPI(apiUUID, tenantDomain, apiIdEmailReplaced);
            for (SubscribedAPI subscriber : subscribers) {
                NotificationEndUserDTO endUser = new NotificationEndUserDTO();
                endUser.setDestinationUser(subscriber.getSubscriber().getName());
                endUser.setOrganization(subscriber.getOrganization());
                endUser.setPortalToDisplay(APIConstants.PortalNotifications.DEV_PORTAL);
                subscriberList.add(endUser);
            }
        } catch (APIManagementException e) {
            APIUtil.handleException("Error while getting subscribers of API - getDestinationUser()", e);
        }

        return subscriberList;
    }

    /**
     * This method will return all the users belongs to a given group
     *
     * @param workflowDTO WorkflowDTO object that contains the workflow details
     * @return a list of users belongs to a given group
     */
    protected List<User> getAllUsersBelongToGroup(WorkflowDTO workflowDTO, String groupId) throws UserStoreException {
        String tenantDomainOfUser = workflowDTO.getTenantDomain();
        List<User> users = new ArrayList<>();

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

    /**
     * This method will return a NotificationEndUserDTO list containing all the users of a given group
     *
     * @param primaryUser primary user of the workflow
     * @param users       a list of users belongs to the group
     * @return a NotificationEndUserDTO list
     */
    protected List<NotificationEndUserDTO> getAllUsersBelongToGroupList(String primaryUser, List<User> users) {
        List<NotificationEndUserDTO> allUsersOfTheGroup = new ArrayList<>();

        for (User user : users) {
            NotificationEndUserDTO endUser = new NotificationEndUserDTO();
            if (user.getUsername().equals(primaryUser)) {
                continue;
            }
            endUser.setDestinationUser(user.getUsername());
            endUser.setOrganization(user.getTenantDomain());
            endUser.setPortalToDisplay(APIConstants.PortalNotifications.DEV_PORTAL);
            allUsersOfTheGroup.add(endUser);
        }

        return allUsersOfTheGroup;
    }

}
