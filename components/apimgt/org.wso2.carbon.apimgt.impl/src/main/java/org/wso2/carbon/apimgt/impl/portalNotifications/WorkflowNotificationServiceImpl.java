package org.wso2.carbon.apimgt.impl.portalNotifications;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;

public class WorkflowNotificationServiceImpl implements PortalNotificationService<WorkflowDTO> {
    public void sendPortalNotifications(WorkflowDTO workflowDTO, String tenantDomainOfUser) {
        PortalNotificationDTO portalNotificationsDTO = new PortalNotificationDTO();

        portalNotificationsDTO.setNotificationType(getNotificationType(workflowDTO.getWorkflowType()));
        portalNotificationsDTO.setCreatedTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        portalNotificationsDTO.setNotificationMetadata(getNotificationMetaData(workflowDTO));
        portalNotificationsDTO.setEndUsers(getDestinationUser(workflowDTO, tenantDomainOfUser));

        boolean result = PortalNotificationDAO.getInstance().addNotification(portalNotificationsDTO);

        if (!result) {
            System.out.println("Error while adding publisher developer notification - sedPubDevNotification()");
        }
    }

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

    private List<PortalNotificationEndUserDTO> getDestinationUser(
            org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, String tenantDomainOfUser) {
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
            if (workflowDTO.getMetadata("Action").equals("Block") || workflowDTO.getMetadata("Action")
                    .equals("Deprecate") || workflowDTO.getMetadata("Action").equals("Retire")) {
                String apiUUID = null;
                String apiName = workflowDTO.getProperties("apiName");
                String apiContext = workflowDTO.getMetadata("ApiContext");
                String apiVersion = workflowDTO.getProperties("apiVersion");
                String provider = workflowDTO.getMetadata("ApiProvider");
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
                        endUser.setPortalToDisplay("developer");
                        destinationUserList.add(endUser);
                    }
                } catch (APIManagementException e) {
                    System.out.println("Error while getting subscribers of API - getDestinationUser()");
                }
            }
        }

        if(APIUtil.isMultiGroupAppSharingEnabled()){
            try {
                List<User> users = getAllUsersBelongToGroup(workflowDTO, tenantDomainOfUser);
                for (User user : users) {
                    PortalNotificationEndUserDTO endUser = new PortalNotificationEndUserDTO();
                    if(user.getUsername().equals(destinationUser)){
                        continue;
                    }
                    endUser.setDestinationUser(user.getUsername());
                    endUser.setOrganization(user.getTenantDomain());
                    endUser.setPortalToDisplay("developer");
                    destinationUserList.add(endUser);
                }
            } catch (APIManagementException e) {
                System.out.println("Error while getting users belong to group - getDestinationUser()");
            } catch (UserStoreException e) {
                throw new RuntimeException(e);
            }
        }


        return destinationUserList;
    }

    private PortalNotificationMetaData getNotificationMetaData(
            org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO) {
        PortalNotificationMetaData portalNotificationMetaData = new PortalNotificationMetaData();

        portalNotificationMetaData.setApi(workflowDTO.getProperties("apiName"));
        portalNotificationMetaData.setApiVersion(workflowDTO.getProperties("apiVersion"));
        portalNotificationMetaData.setAction(workflowDTO.getProperties("action"));
        portalNotificationMetaData.setApplicationName(workflowDTO.getProperties("applicationName"));
        portalNotificationMetaData.setRequestedTier(workflowDTO.getProperties("requestedTier"));
        portalNotificationMetaData.setRevisionId(workflowDTO.getProperties("revisionId"));
        portalNotificationMetaData.setComment(workflowDTO.getComments());

        if (WorkflowConstants.WF_TYPE_AM_API_STATE.equals(
                workflowDTO.getWorkflowType()) || WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE.equals(
                workflowDTO.getWorkflowType())) {
            portalNotificationMetaData.setApiContext(workflowDTO.getMetadata("ApiContext"));
        }

        return portalNotificationMetaData;
    }

    private String setPortalToDisplay(String workflowType) {
        switch (workflowType) {
        case WorkflowConstants.WF_TYPE_AM_API_STATE:
        case WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE:
        case WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT:
            return "publisher";
        default:
            return "developer";
        }
    }

    private String getAPIUUIDUsingNameContextVersion(String apiName, String apiContext, String apiVersion,
            String organization) throws APIManagementException {
        return PortalNotificationDAO.getInstance()
                .getAPIUUIDUsingNameContextVersion(apiName, apiContext, apiVersion, organization);
    }

    private List<SubscribedAPI> getSubscribersOfAPI(String apiUUID, String organization,
            APIIdentifier apiIdEmailReplaced) throws APIManagementException {
        return getAPIUsageByAPIId(apiUUID, organization, apiIdEmailReplaced);
    }

    private List<SubscribedAPI> getAPIUsageByAPIId(String uuid, String organization, APIIdentifier apiIdEmailReplaced)
            throws APIManagementException {
        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();
        Set<String> uniqueUserSet = new HashSet<>();
        try{
            UserApplicationAPIUsage[] allApiResult = ApiMgtDAO.getInstance().getAllAPIUsageByProviderAndApiId(uuid, organization);
            for (UserApplicationAPIUsage usage : allApiResult) {
                for (SubscribedAPI apiSubscription : usage.getApiSubscriptions()) {
                    APIIdentifier subsApiId = apiSubscription.getAPIIdentifier();
                    APIIdentifier subsApiIdEmailReplaced = new APIIdentifier(
                            APIUtil.replaceEmailDomain(subsApiId.getProviderName()), subsApiId.getApiName(),
                            subsApiId.getVersion());
                    if (subsApiIdEmailReplaced.equals(apiIdEmailReplaced)) {
                        String userIdentifier = apiSubscription.getSubscriber().getName()+ "-" + apiSubscription.getOrganization();
                        if (uniqueUserSet.add(userIdentifier)){
                            subscribedAPIs.add(apiSubscription);
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            System.out.println("Error while getting API usage by API ID - getAPIUsageByAPIId()");
        }

        return subscribedAPIs;
    }

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
        } else if (workflowDTO.getWorkflowType().equals(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION)
                || workflowDTO.getWorkflowType().equals(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX)) {
            groupId = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getApplication().getGroupId();
        }

        if (groupId != null) {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainOfUser);
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            if(realm != null){
                org.wso2.carbon.user.core.UserStoreManager manager = realm.getUserStoreManager();
                AbstractUserStoreManager abstractManager = (AbstractUserStoreManager) manager;
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String claim = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI);
                if (StringUtils.isBlank(claim)) {
                    claim = "http://wso2.org/claims/organization";
                }
                users = abstractManager.getUserListWithID(claim , groupId, "default");
            }

        }
        return users;
    }

}
