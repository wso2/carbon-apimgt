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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.InputStream;
import java.util.*;

import static org.wso2.carbon.apimgt.impl.APIConstants.UN_AUTHORIZED_ERROR_MESSAGE;

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
    protected String username;
    private static final Log log = LogFactory.getLog(UserAwareAPIProvider.class);

    UserAwareAPIProvider(String username) throws APIManagementException {
        super(username);
        this.username = username;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        isAccessControlRestrictionEnabled = Boolean
                .parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS));
    }

    @Override
    public void addAPI(API api) throws APIManagementException {
        checkCreatePermission();
        super.addAPI(api);
    }

    @Override
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException, APIManagementException {
        checkCreatePermission();
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        super.createNewAPIVersion(api, newVersion);
    }

    @Override
    public List<String> getCustomInSequences(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getCustomInSequences(apiIdentifier);
    }

    @Override
    public void updateAPI(API api) throws APIManagementException, FaultGatewaysException {
        checkCreatePermission();
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        super.updateAPI(api);
    }

    @Override
    public String getWsdl(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getWsdl(apiId);
    }

    @Override
    public boolean updateAPIStatus(APIIdentifier identifier, String status, boolean publishToGateway,
            boolean deprecateOldVersions, boolean makeKeysForwardCompatible)
            throws APIManagementException, FaultGatewaysException {
        checkAccessControlPermission(identifier);
        return super
                .updateAPIStatus(identifier, status, publishToGateway, deprecateOldVersions, makeKeysForwardCompatible);
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
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        super.updateAPI(api);
    }

    @Override
    public void deleteAPI(APIIdentifier identifier) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(identifier);
        super.deleteAPI(identifier);
    }

    @Override
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        return super.updateAPIsInExternalAPIStores(api, apiStoreSet, apiOlderVersionExist);
    }

    @Override
    public List<String> getCustomOutSequences(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getCustomOutSequences(apiIdentifier);
    }

    @Override
    public List<String> getCustomApiFaultSequences(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getCustomApiFaultSequences(apiIdentifier);
    }

    @Override
    public List<String> getCustomFaultSequences(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getCustomFaultSequences(apiIdentifier);
    }

    @Override
    public void changeAPIStatus(API api, APIStatus status, String userId,
                                boolean updateGatewayConfig) throws APIManagementException, FaultGatewaysException {
        checkPublishPermission();
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        super.changeAPIStatus(api, status, userId, updateGatewayConfig);
    }

    @Override
    public Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier identifier, APIStatus newStatus)
            throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.propergateAPIStatusChangeToGateways(identifier, newStatus);
    }

    @Override
    public boolean updateAPIforStateChange(APIIdentifier identifier, APIStatus newStatus,
            Map<String, String> failedGatewaysMap) throws APIManagementException, FaultGatewaysException {
        checkAccessControlPermission(identifier);
        return super.updateAPIforStateChange(identifier, newStatus, failedGatewaysMap);
    }

    @Override
    public void addFileToDocumentation(APIIdentifier apiId, Documentation documentation, String filename,
            InputStream content, String contentType) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.addFileToDocumentation(apiId, documentation, filename, content, contentType);
    }

    @Override
    public void addDocumentation(APIIdentifier apiId,
                                 Documentation documentation) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(apiId);
        super.addDocumentation(apiId, documentation);
    }

    @Override
    public API getLightweightAPIByUUID(String uuid, String requestedTenantDomain) throws APIManagementException {
        API api = super.getLightweightAPIByUUID(uuid, requestedTenantDomain);
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        return api;
    }

    @Override
    public API getLightweightAPI(APIIdentifier identifier) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getLightweightAPI(identifier);
    }

    @Override
    public String getSwagger20Definition(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getSwagger20Definition(apiId);
    }

    @Override
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(apiId);
        super.removeDocumentation(apiId, docName, docType);
    }

    @Override
    public void removeDocumentation(APIIdentifier apiId, String docId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.removeDocumentation(apiId, docId);
    }

    @Override
    public boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException {
        return super.checkIfAPIExists(apiId);
    }

    @Override
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(apiId);
        super.updateDocumentation(apiId, documentation);
    }
   
    @Override
    public void addDocumentationContent(API api, String documentationName,
                                        String text) throws APIManagementException {
        checkCreatePermission();
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        super.addDocumentationContent(api, documentationName, text);
    }

    @Override
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(apiId);
        super.copyAllDocumentation(apiId, toVersion);
    }

    @Override
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getLifeCycleEvents(apiId);
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
        checkAccessControlPermission(apiIdentifier);
        return super.changeLifeCycleStatus(apiIdentifier, targetStatus);
    }

    @Override
    public boolean checkAndChangeAPILCCheckListItem(APIIdentifier apiIdentifier, String checkItemName,
            boolean checkItemValue) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.checkAndChangeAPILCCheckListItem(apiIdentifier, checkItemName, checkItemValue);
    }

    public boolean changeAPILCCheckListItems(APIIdentifier apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException {
        checkPublishPermission();
        checkAccessControlPermission(apiIdentifier);
        return super.changeAPILCCheckListItems(apiIdentifier, checkItem, checkItemValue);
    }

    @Override
    public Map<String, Object> getAPILifeCycleData(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getAPILifeCycleData(apiId);
    }

    @Override
    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getAPI(identifier);
    }

    @Override
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getSubscribersOfAPI(identifier);
    }

    @Override
    public String getAPILifeCycleStatus(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getAPILifeCycleStatus(apiIdentifier);
    }

    @Override
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getAPISubscriptionCountByAPI(identifier);
    }

    @Override
    public String getDefaultVersion(APIIdentifier apiid) throws APIManagementException {
        checkAccessControlPermission(apiid);
        return super.getDefaultVersion(apiid);
    }

    public String[] getConsumerKeys(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getConsumerKeys(apiIdentifier);
    }

    @Override
    public void saveSwagger20Definition(APIIdentifier apiId, String jsonText) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.saveSwagger20Definition(apiId, jsonText);
    }

    @Override
    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getAllDocumentation(apiId);
    }

    @Override
    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getDocumentationContent(identifier, documentationName);
    }

    @Override
    public List<Mediation> getAllApiSpecificMediationPolicies(APIIdentifier apiIdentifier)
            throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getAllApiSpecificMediationPolicies(apiIdentifier);
    }

    @Override
    public boolean isAPIUpdateValid(API api) throws APIManagementException {
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        return super.isAPIUpdateValid(api);
    }

    @Override
    public String addResourceFile(String resourcePath, ResourceFile resourceFile) throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifier(resourcePath);
        checkAccessControlPermission(apiIdentifier);
        return super.addResourceFile(resourcePath, resourceFile);
    }

    @Override
    protected API getAPI(GenericArtifact apiArtifact) throws APIManagementException {
        API api = APIUtil.getAPI(apiArtifact, registry);
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        return api;
    }

    @Override
    public ResourceFile getIcon(APIIdentifier identifier) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getIcon(identifier);
    }

    @Override
    protected GenericArtifact getAPIArtifact(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getAPIArtifact(apiIdentifier);
    }

    @Override
    public Map<Documentation, API> searchAPIDoc(Registry registry, int tenantID, String username, String searchTerm)
            throws APIManagementException {
        Map<Documentation, API> apiByDocumentation = APIUtil
                .searchAPIsByDoc(registry, tenantId, username, searchTerm, APIConstants.PUBLISHER_CLIENT);
        Map<Documentation, API> filteredAPIDocumentation = new HashMap<Documentation, API>();
        if (apiByDocumentation != null) {
            for (Map.Entry<Documentation, API> entry : apiByDocumentation.entrySet()) {
                API api = entry.getValue();
                if (api != null) {
                    checkAccessControlPermission(api.getId());
                    filteredAPIDocumentation.put(entry.getKey(), api);
                }
            }
            return filteredAPIDocumentation;
        }
        return null;
    }

    @Override
    public Boolean deleteApiSpecificMediationPolicy(String apiResourcePath, String mediationPolicyId)
            throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifier(apiResourcePath);
        checkAccessControlPermission(apiIdentifier);
        return super.deleteApiSpecificMediationPolicy(apiResourcePath, mediationPolicyId);
    }

    @Override
    public Mediation getApiSpecificMediationPolicy(String apiResourcePath, String mediationPolicyId)
            throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifier(apiResourcePath);
        checkAccessControlPermission(apiIdentifier);
        return super.getApiSpecificMediationPolicy(apiResourcePath, mediationPolicyId);
    }

    @Override
    public Resource getApiSpecificMediationResourceFromUuid(String uuid, String resourcePath)
            throws APIManagementException {
        APIIdentifier apiIdentifier = APIUtil.getAPIIdentifier(resourcePath);
        checkAccessControlPermission(apiIdentifier);
        return super.getApiSpecificMediationResourceFromUuid(uuid, resourcePath);
    }
}
