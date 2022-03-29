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
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public API addAPI(API api) throws APIManagementException {
        return super.addAPI(api);
    }

    @Override
    public List<String> getCustomInSequences(APIIdentifier apiIdentifier) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.getCustomInSequences(apiIdentifier);
    }

    @Override
    public void updateAPI(API api) throws APIManagementException, FaultGatewaysException {
        checkAccessControlPermission(api.getId());
        super.updateAPI(api);
    }

    @Override
    public ResourceFile getWSDL(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getWSDL(apiId);
    }

    @Override
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {
        checkAccessControlPermission(api.getId());
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
    public API getLightweightAPIByUUID(String uuid, String organization) throws APIManagementException {
        API api = super.getLightweightAPIByUUID(uuid, organization);
        if (api != null) {
            checkAccessControlPermission(api.getId());
        }
        return api;
    }

    @Override
    public String getOpenAPIDefinition(Identifier apiId, String organization) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getOpenAPIDefinition(apiId, organization);
    }

    @Override

    public void removeDocumentation(APIIdentifier apiId, String docName, String docType, String orgId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.removeDocumentation(apiId, docName, docType, orgId);
    }

    @Override
    public void removeDocumentation(Identifier id, String docId, String orgId) throws APIManagementException {
        checkAccessControlPermission(id);
        super.removeDocumentation(id, docId, orgId);
    }

    @Override
    public boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException {
        return super.checkIfAPIExists(apiId);
    }

    @Override
    public Documentation updateDocumentation(String apiId, Documentation documentation, String organization) throws APIManagementException {
        //checkAccessControlPermission(apiId);
        return super.updateDocumentation(apiId, documentation, organization);
    }

    @Override
    public void addDocumentationContent(API api, String documentationName,
                                        String text) throws APIManagementException {
        checkAccessControlPermission(api.getId());
        super.addDocumentationContent(api, documentationName, text);
    }

    @Override
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.copyAllDocumentation(apiId, toVersion);
    }

    @Override
    public List<LifeCycleEvent> getLifeCycleEvents(String uuid) throws APIManagementException {

        return super.getLifeCycleEvents(uuid);
    }

    @Override
    public void updateSubscription(APIIdentifier apiId, String subStatus, int appId, String organization)
            throws APIManagementException {
        apiMgtDAO.updateSubscription(apiId, subStatus, appId, organization);
    }

    @Override
    public void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException {
        super.updateSubscription(subscribedAPI);
    }

    @Override
    public SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException {
        return super.getSubscriptionByUUID(uuid);
    }

    public APIStateChangeResponse changeLifeCycleStatus(APIIdentifier apiIdentifier, String targetStatus,
            String organization) throws APIManagementException, FaultGatewaysException {
        checkAccessControlPermission(apiIdentifier);
        return super.changeLifeCycleStatus(apiIdentifier, targetStatus, organization);
    }

    @Override
    public boolean checkAndChangeAPILCCheckListItem(APIIdentifier apiIdentifier, String checkItemName,
            boolean checkItemValue) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.checkAndChangeAPILCCheckListItem(apiIdentifier, checkItemName, checkItemValue);
    }

    public boolean changeAPILCCheckListItems(APIIdentifier apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.changeAPILCCheckListItems(apiIdentifier, checkItem, checkItemValue);
    }

    @Override
    public Map<String, Object> getAPILifeCycleData(APIIdentifier apiId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        return super.getAPILifeCycleData(apiId);
    }

    @Override
    public int addClientCertificate(String userName, APIIdentifier apiIdentifier, String certificate, String alias,
            String tierName, String organization) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.addClientCertificate(userName, apiIdentifier, certificate, alias, tierName, organization);
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

    @Override
    public void saveSwagger20Definition(APIIdentifier apiId, String jsonText, String orgId) throws APIManagementException {
        checkAccessControlPermission(apiId);
        super.saveSwagger20Definition(apiId, jsonText, orgId);
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
        checkAccessControlPermission(api.getId());
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
        Map<Documentation, API> filteredAPIDocumentation = new HashMap<>();
        for (Map.Entry<Documentation, API> entry : apiByDocumentation.entrySet()) {
            API api = entry.getValue();
            if (api != null) {
                checkAccessControlPermission(api.getId());
                filteredAPIDocumentation.put(entry.getKey(), api);
            }
        }
        return filteredAPIDocumentation;
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
    public int addCertificate(String userName, String certificate, String alias, String endpoint) throws APIManagementException {
        return super.addCertificate(userName, certificate, alias, endpoint);
    }

    @Override
    public int deleteCertificate(String userName, String alias, String endpoint) throws APIManagementException {
        return super.deleteCertificate(userName, alias, endpoint);
    }

    @Override
    public List<CertificateMetadataDTO> getCertificates(String userName) throws APIManagementException {
        return super.getCertificates(userName);
    }

    @Override
    public List<CertificateMetadataDTO> searchCertificates(int tenantId, String alias, String endpoint)
            throws APIManagementException {
        return super.searchCertificates(tenantId, alias, endpoint);
    }

    @Override
    public boolean isCertificatePresent(int tenantId, String alias) throws APIManagementException {
        return super.isCertificatePresent(tenantId, alias);
    }

    @Override
    public ClientCertificateDTO getClientCertificate(int tenantId, String alias, String organization) throws APIManagementException {
        ClientCertificateDTO clientCertificateDTO = super.getClientCertificate(tenantId, alias, organization);
        if (clientCertificateDTO != null) {
            checkAccessControlPermission(clientCertificateDTO.getApiIdentifier());
        }
        return clientCertificateDTO;
    }

    @Override
    public ClientCertificateDTO getClientCertificate(int tenantId, String alias, APIIdentifier apiIdentifier, String organization)
            throws APIManagementException {
        ClientCertificateDTO clientCertificateDTO = super.getClientCertificate(tenantId, alias, organization);
        if (clientCertificateDTO != null) {
            checkAccessControlPermission(clientCertificateDTO.getApiIdentifier());
        }
        return clientCertificateDTO;
    }

    @Override
    public CertificateInformationDTO getCertificateStatus(String alias) throws APIManagementException {
        return super.getCertificateStatus(alias);
    }

    @Override
    public int updateCertificate(String certificateString, String alias) throws APIManagementException {
        return super.updateCertificate(certificateString, alias);
    }

    @Override
    public int updateClientCertificate(String certificate, String alias, APIIdentifier apiIdentifier,
            String tier, int tenantId, String organization) throws APIManagementException {
        checkAccessControlPermission(apiIdentifier);
        return super.updateClientCertificate(certificate, alias, apiIdentifier, tier, tenantId, organization);
    }

    @Override
    public ByteArrayInputStream getCertificateContent(String alias) throws APIManagementException {
        return super.getCertificateContent(alias);
    }

    @Override
    public void deleteWorkflowTask(Identifier identifier) throws APIManagementException {
        super.deleteWorkflowTask(identifier);
    }
}
