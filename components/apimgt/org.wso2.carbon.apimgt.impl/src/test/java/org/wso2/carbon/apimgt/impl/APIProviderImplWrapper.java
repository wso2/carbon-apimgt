/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class APIProviderImplWrapper extends APIProviderImpl {
    
    private API api;
    private Map<String, Map<String,String>> failedGateways;
    private List<Documentation> documentationList;

    public APIProviderImplWrapper(ApiMgtDAO apiMgtDAO, List<Documentation> documentationList,
            Map<String, Map<String,String>> failedGateways) throws APIManagementException {
        super(null);
        this.apiMgtDAO = apiMgtDAO;
        this.documentationList = documentationList;
        this.failedGateways = failedGateways;
    }
    
    @Override
    protected void registerCustomQueries(UserRegistry registry, String username)
            throws RegistryException, APIManagementException {
     // do nothing
    }
    
    @Override
    protected void createAPI(API api) {
        this.api = api;
    }
    
    @Override
    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        return api;
        
    }
    
    @Override
    public void makeAPIKeysForwardCompatible(API api) throws APIManagementException {
        //do nothing
    }
    
    public void changeAPIStatus(API api, APIStatus status, String userId, boolean updateGatewayConfig)
            throws APIManagementException, FaultGatewaysException {
        if (failedGateways != null && failedGateways.size() >0) {
            throw new FaultGatewaysException(failedGateways);
        }
    }
    
    @Override
    protected Map<String, String> publishToGateway(API api, String tenantDomain) {
        if (failedGateways.containsKey("PUBLISHED")) {
            return failedGateways.get("PUBLISHED");
        } 
        return new HashMap<String, String>();
    }
    
    @Override
    protected Map<String, String> removeFromGateway(API api, String tenantDomain) {
        if (failedGateways.containsKey("UNPUBLISHED")) {
            return failedGateways.get("UNPUBLISHED");
        }
        return new HashMap<String, String>();
    }
    
    @Override
    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        return documentationList;
    }
    
    @Override
    protected int getTenantId(String tenantDomain) {
        return -1234;
    }
    
    @Override
    public String addResourceFile(String resourcePath, ResourceFile resourceFile) throws APIManagementException{
        return null;
    }
    
    @Override
    protected void sendAsncNotification(NotificationDTO notificationDTO) throws NotificationException {
        //do nothing
    }
    
    /*protected String getTenantConfigContent() throws RegistryException, UserStoreException {
        return "{\"EnableMonetization\":false,\"IsUnlimitedTierPaid\":false,\"ExtensionHandlerPosition\":\"bottom\","
                + "\"RESTAPIScopes\":{\"Scope\":[{\"Name\":\"apim:api_publish\",\"Roles\":\"admin,Internal/publisher\"},"
                + "{\"Name\":\"apim:api_create\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_view\","
                + "\"Roles\":\"admin,Internal/publisher,Internal/creator\"},{\"Name\":\"apim:subscribe\",\"Roles\":"
                + "\"admin,Internal/subscriber\"},{\"Name\":\"apim:tier_view\",\"Roles\":\"admin,Internal/publisher,"
                + "Internal/creator\"},{\"Name\":\"apim:tier_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:bl_view\","
                + "\"Roles\":\"admin\"},{\"Name\":\"apim:bl_manage\",\"Roles\":\"admin\"},{\"Name\":"
                + "\"apim:subscription_view\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":"
                + "\"apim:subscription_block\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":"
                + "\"apim:mediation_policy_view\",\"Roles\":\"admin\"},{\"Name\":\"apim:mediation_policy_create\","
                + "\"Roles\":\"admin\"},{\"Name\":\"apim:api_workflow\",\"Roles\":\"admin\"}]},\"NotificationsEnabled\":"
                + "\"false\",\"Notifications\":[{\"Type\":\"new_api_version\",\"Notifiers\":[{\"Class\":"
                + "\"org.wso2.carbon.apimgt.impl.notification.NewAPIVersionEmailNotifier\",\"ClaimsRetrieverImplClass\":"
                + "\"org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever\",\"Title\":\"Version $2 of $1 Released\","
                + "\"Template\":\" <html> <body> <h3 style=\\\"color:Black;\\\">We’re happy to announce the arrival of"
                + " the next major version $2 of $1 API which is now available in Our API Store.</h3><a href=\\\"https:"
                + "//localhost:9443/store\\\">Click here to Visit WSO2 API Store</a></body></html>\"}]}],"
                + "\"DefaultRoles\":{\"PublisherRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":"
                + "\"Internal/publisher\"},\"CreatorRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":"
                + "\"Internal/creator\"},\"SubscriberRole\":{\"CreateOnTenantLoad\":true}}}";
    }*/

}
