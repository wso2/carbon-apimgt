package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;

import java.util.List;

public interface KeyManagerDAO {

    /**
     * Get Key Manager Configuration By Organization.
     *
     * @param organization Organization Name
     * @return List of Key Manager Configurations
     * @throws APIManagementException if failed to Configurations
     */
    List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization)
            throws APIManagementException;

    /**
     * Get All Key Manager Configurations.
     *
     * @return List of Key Manager Configurations
     * @throws APIManagementException if failed to Configurations
     */
    List<KeyManagerConfigurationDTO> getKeyManagerConfigurations() throws APIManagementException;

    /**
     * Get Key Manager Configuration By Organization and UUID.
     *
     * @param organization Organization Name
     * @param uuid KeyManager UUID
     * @return List of Key Manager Configurations
     * @throws APIManagementException if failed to Configurations
     */
    KeyManagerConfigurationDTO getKeyManagerConfigurationByID(String organization, String uuid)
            throws APIManagementException;

    /**
     * Checks Key Manager exists in given organization.
     *
     * @param organization Organization Name
     * @param resourceId KeyManager UUID
     * @return boolean
     * @throws APIManagementException
     */
    boolean isIDPExistInOrg(String organization, String resourceId) throws APIManagementException;
}
