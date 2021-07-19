package org.wso2.carbon.apimgt.cleanup.service.organizationPurge;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;

import java.util.List;

public class IdpKeyMangerPurge implements OrganizationPurge {
    protected String username;

    public IdpKeyMangerPurge(String username) {
        this.username = username;
    }

    public void deleteOrganization(String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerList =
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization);

        for (KeyManagerConfigurationDTO keyManager : keyManagerList) {
            apiAdmin.deleteKeyManagerConfigurationById(organization, keyManager.getUuid(), username);
        }
    }
}
