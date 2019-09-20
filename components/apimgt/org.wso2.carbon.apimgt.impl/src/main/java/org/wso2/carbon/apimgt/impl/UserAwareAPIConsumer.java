/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * User aware APIConsumer implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations. Users can use this class as an
 * entry point to accessing the core API provider functionality. In order to ensure
 * proper initialization and cleanup of these objects, the constructors of the class
 * has been hidden. Users should use the APIManagerFactory class to obtain an instance
 * of this class. This implementation also allows anonymous access to some of the
 * available operations. However if the user attempts to execute a privileged operation
 * when the object had been created in the anonymous mode, an exception will be thrown.
 */
@MethodStats
public class UserAwareAPIConsumer extends APIConsumerImpl {

    private String username;

    UserAwareAPIConsumer() throws APIManagementException {
        super();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        isAccessControlRestrictionEnabled = Boolean
                .parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS));
    }

    UserAwareAPIConsumer(String username, APIMRegistryService registryService) throws APIManagementException {
        super(username, registryService);
        this.username = username;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        isAccessControlRestrictionEnabled = Boolean
                .parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS));
    }

    @Override
    public SubscriptionResponse addSubscription(ApiTypeWrapper apiTypeWrapper,
                                                String userId, int applicationId) throws APIManagementException {
        checkSubscribePermission();
        return super.addSubscription(apiTypeWrapper, userId, applicationId);
    }

    @Override
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        SubscribedAPI subscribedAPI = super.getSubscriptionByUUID(uuid);
        checkSubscribePermission();
        return subscribedAPI;
    }

    @Override
    public void removeSubscription(Identifier identifier, String userId,
                                   int applicationId) throws APIManagementException {
        checkSubscribePermission();
        super.removeSubscription(identifier, userId, applicationId);
    }

    @Override
    public void removeSubscription(SubscribedAPI subscription) throws APIManagementException {
        checkSubscribePermission();
        super.removeSubscription(subscription);
    }

    @Override
    public int addApplication(Application application, String userId) throws APIManagementException {
        checkSubscribePermission();
        return super.addApplication(application, userId);
    }

    @Override
    public void updateApplication(Application application) throws APIManagementException {
        checkSubscribePermission();
        super.updateApplication(application);
    }

    @Override
    public void removeApplication(Application application, String username) throws APIManagementException {
        checkSubscribePermission();
        super.removeApplication(application, username);
    }
     @Override
    public void removeSubscription(APIIdentifier identifier, String userId, int applicationId, String groupId) throws
             APIManagementException {
        checkSubscribePermission();
        super.removeSubscription(identifier, userId, applicationId, groupId);
    }

    /**
     * @deprecated
     * This method needs to be removed once the Jaggery web apps are removed.
     *
     */
    @Override
    public void addComment(APIIdentifier identifier, String s, String user) throws APIManagementException {
        checkSubscribePermission();
        super.addComment(identifier, s, user);
    }

    @Override
    public String addComment(APIIdentifier identifier, Comment comment, String user) throws APIManagementException {
        checkSubscribePermission();
        return super.addComment(identifier, comment, user);
    }

    @Override
    public Comment getComment(APIIdentifier identifier, String commentId) throws APIManagementException {
        checkSubscribePermission();
        return super.getComment(identifier, commentId);
    }

    @Override
    public void deleteComment(APIIdentifier identifier, String commentId) throws APIManagementException {
        checkSubscribePermission();
        super.deleteComment(identifier,commentId);
    }

    @Override
    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getAPI(identifier);
    }

    public void checkSubscribePermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.API_SUBSCRIBE);
    }
}
