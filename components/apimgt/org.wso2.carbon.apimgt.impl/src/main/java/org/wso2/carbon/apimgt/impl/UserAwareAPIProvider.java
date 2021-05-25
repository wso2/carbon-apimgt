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
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
@MethodStats
public class UserAwareAPIProvider extends APIProviderImpl {
    protected String username;
    private static final Log log = LogFactory.getLog(UserAwareAPIProvider.class);

    UserAwareAPIProvider(String username) throws APIManagementException {
        super(username);
        this.username = username;
        this.tenantDomain = MultitenantUtils.getTenantDomain(username);
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
    public ResourceFile getWSDL(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getWSDL(apiId);
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
    public void deleteAPI(APIIdentifier identifier, String apiUUid) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(identifier);
        super.deleteAPI(identifier, apiUUid);
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
    public void changeAPIStatus(API api, String status, String userId,
                                boolean updateGatewayConfig) throws APIManagementException, FaultGatewaysException {
        checkPublishPermission();
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        super.changeAPIStatus(api, status, userId, updateGatewayConfig);
    }

    @Override
    public void changeAPIStatus(API api, APIStatus status, String userId,
            boolean updateGatewayConfig) throws APIManagementException, FaultGatewaysException {
        changeAPIStatus(api, status.getStatus(), userId, updateGatewayConfig);
    }

    @Override
    public Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier identifier, String newStatus)
            throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.propergateAPIStatusChangeToGateways(identifier, newStatus);
    }

    @Override
    public Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier identifier, APIStatus newStatus)
            throws APIManagementException {
        return propergateAPIStatusChangeToGateways(identifier, newStatus.getStatus());
    }

    @Override
    public boolean updateAPIforStateChange(APIIdentifier identifier, String newStatus,
            Map<String, String> failedGatewaysMap) throws APIManagementException, FaultGatewaysException {
        checkAccessControlPermission(identifier);
        return super.updateAPIforStateChange(identifier, newStatus, failedGatewaysMap);
    }

    @Override
    public boolean updateAPIforStateChange(APIIdentifier identifier, APIStatus newStatus,
            Map<String, String> failedGatewaysMap) throws APIManagementException, FaultGatewaysException {
        return updateAPIforStateChange(identifier, newStatus.getStatus(), failedGatewaysMap);
    }

    @Override
    public void addFileToDocumentation(APIIdentifier apiId, Documentation documentation, String filename,
            InputStream content, String contentType) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.addFileToDocumentation(apiId, documentation, filename, content, contentType);
    }

    @Override
    public void addDocumentation(Identifier id,
                                 Documentation documentation) throws APIManagementException {

        if (!checkCreateOrPublishPermission()) {
            throw new APIManagementException("User '" + username + "' has neither '" + APIConstants.Permissions.API_CREATE
                    + "' nor the '" + APIConstants.Permissions.API_PUBLISH + "' permission to add API documentation");
        }
        //todo : implement access control check for api products too
        if (id instanceof APIIdentifier) {
            checkAccessControlPermission((APIIdentifier) id);
        }
        super.addDocumentation(id, documentation);
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
    public String getOpenAPIDefinition(Identifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getOpenAPIDefinition(apiId);
    }

    @Override
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType) throws APIManagementException {
        checkCreatePermission();
        checkAccessControlPermission(apiId);
        super.removeDocumentation(apiId, docName, docType);
    }

    @Override
    public void removeDocumentation(Identifier id, String docId) throws APIManagementException {
        checkAccessControlPermission(id);
        super.removeDocumentation(id, docId);
    }

    @Override
    public boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException {
        return super.checkIfAPIExists(apiId);
    }

    @Override
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws APIManagementException {
        if (!checkCreateOrPublishPermission()) {
            throw new APIManagementException("User '" + username + "' has neither '" +
                    APIConstants.Permissions.API_CREATE + "' nor the '" + APIConstants.Permissions.API_PUBLISH +
                    "' permission to update API documentation");
        }
        checkAccessControlPermission(apiId);
        super.updateDocumentation(apiId, documentation);
    }

    @Override
    public void addDocumentationContent(API api, String documentationName,
                                        String text) throws APIManagementException {

        if (!checkCreateOrPublishPermission()) {
            throw new APIManagementException("User '" + username + "' has neither '" +
                    APIConstants.Permissions.API_CREATE + "' nor the '" + APIConstants.Permissions.API_PUBLISH +
                    "' permission to add content for API documentation");
        }
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
    public void updateSubscription(APIIdentifier apiId, String subStatus, int appId) throws APIManagementException {
        checkPublishPermission();
        apiMgtDAO.updateSubscription(apiId, subStatus, appId);
    }

    @Override
    public void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException {
        super.updateSubscription(subscribedAPI);
    }

    @Override
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        if (checkCreateOrPublishPermission()) {
            return super.getSubscriptionByUUID(uuid);
        }
        return null;
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

    public boolean checkCreateOrPublishPermission() throws APIManagementException {
        return APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_CREATE) ||
                APIUtil.checkPermissionQuietly(username, APIConstants.Permissions.API_PUBLISH);
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
    public int addClientCertificate(String userName, APIIdentifier apiIdentifier, String certificate, String alias,
            String tierName) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.addClientCertificate(userName, apiIdentifier, certificate, alias, tierName);
    }

    @Override
    public int deleteClientCertificate(String userName, APIIdentifier apiIdentifier, String alias)
            throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.deleteClientCertificate(userName, apiIdentifier, alias);
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
    public List<Documentation> getAllDocumentation(Identifier id) throws APIManagementException {
        checkAccessControlPermission(id);
        return super.getAllDocumentation(id);
    }

    @Override
    public String getDocumentationContent(Identifier identifier, String documentationName)
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
    public String addResourceFile(Identifier identifier, String resourcePath, ResourceFile resourceFile)
            throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.addResourceFile(identifier, resourcePath, resourceFile);
    }

    @Override
    protected API getAPI(GenericArtifact apiArtifact) throws APIManagementException {
        API api = APIUtil.getAPI(apiArtifact, registry);
        if (api != null) {
            APIUtil.updateAPIProductDependencies(api, registry);
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
    public Boolean deleteApiSpecificMediationPolicy(Identifier identifier, String apiResourcePath,
            String mediationPolicyId) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.deleteApiSpecificMediationPolicy(identifier, apiResourcePath, mediationPolicyId);
    }

    @Override
    public Mediation getApiSpecificMediationPolicy(Identifier identifier, String apiResourcePath,
            String mediationPolicyId) throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getApiSpecificMediationPolicy(identifier, apiResourcePath, mediationPolicyId);
    }

    @Override
    public Resource getApiSpecificMediationResourceFromUuid(Identifier identifier, String uuid, String resourcePath)
            throws APIManagementException {
        checkAccessControlPermission(identifier);
        return super.getApiSpecificMediationResourceFromUuid(identifier, uuid, resourcePath);
    }

    @Override
    public String getSequenceFileContent(APIIdentifier apiIdentifier, String sequenceType, String sequenceName)
            throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getSequenceFileContent(apiIdentifier, sequenceType, sequenceName);
    }

    @Override
    public int addCertificate(String userName, String certificate, String alias, String endpoint) throws APIManagementException {
        checkCreatePermission();
        return super.addCertificate(userName, certificate, alias, endpoint);
    }

    @Override
    public int deleteCertificate(String userName, String alias, String endpoint) throws APIManagementException {
        checkCreatePermission();
        return super.deleteCertificate(userName, alias, endpoint);
    }

    @Override
    public List<CertificateMetadataDTO> getCertificates(String userName) throws APIManagementException {
        checkCreatePermission();
        return super.getCertificates(userName);
    }

    @Override
    public List<CertificateMetadataDTO> searchCertificates(int tenantId, String alias, String endpoint)
            throws APIManagementException {
        checkCreatePermission();
        return super.searchCertificates(tenantId, alias, endpoint);
    }

    @Override
    public boolean isCertificatePresent(int tenantId, String alias) throws APIManagementException {
        checkCreatePermission();
        return super.isCertificatePresent(tenantId, alias);
    }

    @Override
    public ClientCertificateDTO getClientCertificate(int tenantId, String alias) throws APIManagementException {
        ClientCertificateDTO clientCertificateDTO = super.getClientCertificate(tenantId, alias);
        if (clientCertificateDTO != null) {
            checkAccessControlPermission(clientCertificateDTO.getApiIdentifier());
        }
        return clientCertificateDTO;
    }

    @Override
    public ClientCertificateDTO getClientCertificate(int tenantId, String alias, APIIdentifier apiIdentifier)
            throws APIManagementException {
        ClientCertificateDTO clientCertificateDTO = super.getClientCertificate(tenantId, alias);
        if (clientCertificateDTO != null) {
            checkAccessControlPermission(clientCertificateDTO.getApiIdentifier());
        }
        return clientCertificateDTO;
    }

    @Override
    public CertificateInformationDTO getCertificateStatus(String alias) throws APIManagementException {
        checkCreatePermission();
        return super.getCertificateStatus(alias);
    }

    @Override
    public int updateCertificate(String certificateString, String alias) throws APIManagementException {
        checkCreatePermission();
        return super.updateCertificate(certificateString, alias);
    }

    @Override
    public int updateClientCertificate(String certificate, String alias, APIIdentifier apiIdentifier,
            String tier, int tenantId) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.updateClientCertificate(certificate, alias, apiIdentifier, tier, tenantId);
    }

    @Override
    public ByteArrayInputStream getCertificateContent(String alias) throws APIManagementException {
        checkCreatePermission();
        return super.getCertificateContent(alias);
    }

    @Override
    public void deleteWorkflowTask(APIIdentifier apiIdentifier) throws APIManagementException {
        checkPublishPermission();
        super.deleteWorkflowTask(apiIdentifier);
    }
}
