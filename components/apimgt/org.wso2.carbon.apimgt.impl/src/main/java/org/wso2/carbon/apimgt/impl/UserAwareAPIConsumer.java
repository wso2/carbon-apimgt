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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
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
public class UserAwareAPIConsumer extends APIConsumerImpl {

    private String username;

    UserAwareAPIConsumer() throws APIManagementException {
        super();
    }

    UserAwareAPIConsumer(String username) throws APIManagementException {
        super(username);
        this.username = username;
    }

    @Override
    public String addSubscription(String providerName, String apiName, String version, String tier, int applicationId, String userId) throws APIManagementException {
        checkSubscribePermission();
        return super.addSubscription(providerName, apiName, version, tier, applicationId, userId);
    }

    @Override
    public void removeSubscription(APIIdentifier identifier, String userId,
                                   int applicationId) throws APIManagementException {
        checkSubscribePermission();
        super.removeSubscription(identifier, userId, applicationId);
    }

    @Override
    public String addApplication(String appName, String userName, String tier, String callbackUrl, String description) throws APIManagementException {
        checkSubscribePermission();
        return super.addApplication(appName, userName, tier, callbackUrl, description);
    }

    @Override
    public void updateApplication(Application application) throws APIManagementException {
        checkSubscribePermission();
        super.updateApplication(application);
    }

    @Override
    public void removeApplication(Application application) throws APIManagementException {
        checkSubscribePermission();
        super.removeApplication(application);
    }

    @Override
    public JSONObject getApplicationKey(String username, String applicationName, String tokenType,
                                  String scopes, String validityPeriod, String callbackUrl,
                                  JSONArray accessAllowDomainsArr) throws APIManagementException {
        checkSubscribePermission();
        return super.getApplicationKey(username,applicationName,tokenType,scopes,validityPeriod,callbackUrl,accessAllowDomainsArr);
    }

    @Override
    public void addComment(APIIdentifier identifier, String s, String user) throws APIManagementException {
        checkSubscribePermission();
        super.addComment(identifier, s, user);
    }

    public void checkSubscribePermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.API_SUBSCRIBE);
    }
}
