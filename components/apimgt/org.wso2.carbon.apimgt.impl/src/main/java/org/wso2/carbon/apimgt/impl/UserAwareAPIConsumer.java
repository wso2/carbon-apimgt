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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

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
        readAccessControlConfig();
    }

    UserAwareAPIConsumer(String username) throws APIManagementException {
        super(username);
        this.username = username;
        readAccessControlConfig();
    }

    UserAwareAPIConsumer(String username, String organization) throws APIManagementException {
        super(username, organization);
        this.username = username;
        readAccessControlConfig();
    }

    private void readAccessControlConfig() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        isAccessControlRestrictionEnabled = Boolean
                .parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS));
    }

    @Override
    public SubscriptionResponse addSubscription(ApiTypeWrapper apiTypeWrapper,
                                                String userId, Application application) throws APIManagementException {
        return super.addSubscription(apiTypeWrapper, userId, application);
    }

    @Override
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        return super.getSubscriptionByUUID(uuid);
    }

    @Override
    public void removeSubscription(Identifier identifier, String userId, int applicationId,
                                   String organization) throws APIManagementException {
        super.removeSubscription(identifier, userId, applicationId, organization);
    }

    @Override
    public void removeSubscription(SubscribedAPI subscription, String organization) throws APIManagementException {
        super.removeSubscription(subscription, organization);
    }

    @Override
    public int addApplication(Application application, String userId, String organization) throws APIManagementException {
        return super.addApplication(application, userId, organization);
    }

    @Override
    public void updateApplication(Application application) throws APIManagementException {
        super.updateApplication(application);
    }

    @Override
    public void removeApplication(Application application, String username) throws APIManagementException {
        super.removeApplication(application, username);
    }
     @Override
    public void removeSubscription(APIIdentifier identifier, String userId, int applicationId, String groupId,
                                   String organization) throws APIManagementException {
        super.removeSubscription(identifier, userId, applicationId, groupId, organization);
    }

    /**
     * @deprecated
     * This method needs to be removed once the Jaggery web apps are removed.
     *
     */
    @Override
    public void addComment(APIIdentifier identifier, String s, String user) throws APIManagementException {
        super.addComment(identifier, s, user);
    }

    @Override
    public String addComment(String uuid, Comment comment, String user) throws APIManagementException {
        return super.addComment(uuid, comment, user);
    }

    @Override
    public void deleteComment(String uuid, String commentId) throws APIManagementException {
        super.deleteComment(uuid, commentId);
    }

    @Override
    public ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String organization) throws APIManagementException {
        ApiTypeWrapper apiTypeWrapper = super.getAPIorAPIProductByUUID(uuid, organization);
        return apiTypeWrapper;
    }

    @Override
    public API getLightweightAPI(APIIdentifier identifier, String orgId) throws APIManagementException {
        API api = super.getLightweightAPI(identifier, orgId);
        checkVisibilityPermission(userNameWithoutChange, api.getVisibility(),
                api.getVisibleRoles());
        return api;
    }
}
