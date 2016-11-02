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
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Map;

/**
 * User aware APIProvider implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations. Users can use this class as an
 * entry point to accessing the core API provider functionality. In order to ensure
 * proper initialization and cleanup of these objects, the constructors of the class
 * has been hidden. Users should use the APIManagerFactory class to obtain an instance
 * of this class. This implementation also allows anonymous access to some of the
 * available operations. However if the user attempts to execute a privileged operation
 * when the object had been created in the anonymous mode, an exception will be thrown.
 */
public class UserAwareAPIProvider extends APIProviderImpl {

    private String username;

    UserAwareAPIProvider(String username) throws APIManagementException {
        super(username);
        this.username = username;
    }

    @Override
    public void addAPI(API api) throws APIManagementException {
        checkCreatePermission();
        super.addAPI(api);
    }

    @Override
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException, APIManagementException {
        checkCreatePermission();
        super.createNewAPIVersion(api, newVersion);
    }

    @Override
    public void updateAPI(API api) throws APIManagementException, FaultGatewaysException {
        checkCreatePermission();
        super.updateAPI(api);
    }

    @Override
    public void manageAPI(API api) throws APIManagementException,FaultGatewaysException {
        boolean permitted = APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_CREATE) ||
                APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_PUBLISH);
        if(!permitted){
            String permission = APIConstants.Permissions.API_CREATE + " or " + APIConstants.Permissions.API_PUBLISH;
            throw new APIManagementException("User '" + username + "' does not have the " +
                    "required permission: " + permission);
        }
        super.updateAPI(api);
    }

    @Override
    public void deleteAPI(APIIdentifier identifier) throws APIManagementException {
        checkCreatePermission();
        super.deleteAPI(identifier);
    }

    @Override
    public void changeAPIStatus(API api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws APIManagementException, FaultGatewaysException {
        checkPublishPermission();
        super.changeAPIStatus(api, status, userId, updateGatewayConfig);
    }

    @Override
    public void addDocumentation(APIIdentifier apiId,
                                 Documentation documentation) throws APIManagementException {
        checkCreatePermission();
        super.addDocumentation(apiId, documentation);
    }

    @Override
    public void removeDocumentation(APIIdentifier apiId, String docName,
                                    String docType) throws APIManagementException {
        checkCreatePermission();
        super.removeDocumentation(apiId, docName, docType);
    }

    @Override
    public boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException {
        return super.checkIfAPIExists(apiId);
    }

    @Override
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws APIManagementException {
        checkCreatePermission();
        super.updateDocumentation(apiId, documentation);
    }
   
    @Override
    public void addDocumentationContent(API api, String documentationName,
                                        String text) throws APIManagementException {
        checkCreatePermission();
        super.addDocumentationContent(api, documentationName, text);
    }

    @Override
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {
        checkCreatePermission();
        super.copyAllDocumentation(apiId, toVersion);
    }

    @Override
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        checkCreatePermission();
        return super.getSubscriptionByUUID(uuid);
    }

    public void checkCreatePermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.API_CREATE);
    }
    
    public void checkManageTiersPermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.MANAGE_TIERS);
    }

    public void checkPublishPermission() throws APIManagementException {
        APIUtil.checkPermission(username, APIConstants.Permissions.API_PUBLISH);
    }

    public APIStateChangeResponse changeLifeCycleStatus(APIIdentifier apiIdentifier, String targetStatus)
            throws APIManagementException, FaultGatewaysException {
        checkPublishPermission();
        return super.changeLifeCycleStatus(apiIdentifier, targetStatus);
    }

    public boolean changeAPILCCheckListItems(APIIdentifier apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException {
        checkPublishPermission();
        return super.changeAPILCCheckListItems(apiIdentifier, checkItem, checkItemValue);
    }

    public Map<String, Object> getAPILifeCycleData(APIIdentifier apiId) throws APIManagementException {
        return super.getAPILifeCycleData(apiId);
    }
}
