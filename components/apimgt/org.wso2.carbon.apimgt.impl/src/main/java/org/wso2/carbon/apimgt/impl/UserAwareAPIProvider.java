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
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.util.List;
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
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException {
        checkAccessControlPermission(username, api.getAccessControl(), api.getAccessControlRoles());
        return super.updateAPIsInExternalAPIStores(api, apiStoreSet, apiOlderVersionExist);
    }

    @Override
    public API getLightweightAPIByUUID(String uuid, String organization) throws APIManagementException {
        return super.getLightweightAPIByUUID(uuid, organization);
    }

    @Override
    public Documentation updateDocumentation(String apiId, Documentation documentation, String organization) throws APIManagementException {
        //checkAccessControlPermission(apiId);
        return super.updateDocumentation(apiId, documentation, organization);
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

    @Override
    public int addClientCertificate(String userName, ApiTypeWrapper apiTypeWrapper, String certificate, String alias,
                                    String tierName, String keyType, String organization) throws APIManagementException {
        return super.addClientCertificate(userName, apiTypeWrapper, certificate, alias, tierName, keyType,
                organization);
    }

    @Override
    public int deleteClientCertificate(String userName, ApiTypeWrapper apiTypeWrapper, String alias, String keyType)
            throws APIManagementException {
        return super.deleteClientCertificate(userName, apiTypeWrapper, alias, keyType);
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
    public ClientCertificateDTO getClientCertificate(String alias, String keyType, ApiTypeWrapper apiTypeWrapper,
                                                     String organization) throws APIManagementException {
        return super.getClientCertificate(alias, keyType, apiTypeWrapper, organization);
    }

    @Override
    public CertificateInformationDTO getCertificateStatus(String tenantDomain, String alias) throws APIManagementException {
        return super.getCertificateStatus(tenantDomain, alias);
    }

    @Override
    public int updateCertificate(String certificateString, String alias) throws APIManagementException {
        return super.updateCertificate(certificateString, alias);
    }

    @Override
    public int updateClientCertificate(String certificate, String alias, ApiTypeWrapper apiIdentifier,
                                       String tier, String keyType, int tenantId, String organization)
            throws APIManagementException {
        return super.updateClientCertificate(certificate, alias, apiIdentifier, tier, keyType,
                tenantId, organization);
    }

    @Override
    public ByteArrayInputStream getCertificateContent(String tenantDomain, String alias) throws APIManagementException {
        return super.getCertificateContent(tenantDomain, alias);
    }

    @Override
    public void deleteWorkflowTask(Identifier identifier) throws APIManagementException {
        super.deleteWorkflowTask(identifier);
    }
}
