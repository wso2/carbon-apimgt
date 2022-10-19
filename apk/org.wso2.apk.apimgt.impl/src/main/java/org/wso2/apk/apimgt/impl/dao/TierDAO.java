package org.wso2.apk.apimgt.impl.dao;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.impl.dto.TierPermissionDTO;

import java.util.Set;

public interface TierDAO {

    /**
     * Update Tier Permissions.
     *
     * @param tierName       Tier Name
     * @param permissionType Permission Type
     * @param roles          Roles
     * @param organization   Organization
     * @throws APIManagementException if fails to update Tier permissions
     */
    void updateTierPermissions(String tierName, String permissionType, String roles, String organization)
            throws APIManagementException;

    /**
     * Delete Tier Permissions.
     *
     * @param tierName     Tier Name
     * @param organization Organization
     * @throws APIManagementException if fails to delete Tier permissions
     */
    void deleteThrottlingPermissions(String tierName, String organization) throws APIManagementException;

    /**
     * Retrieve Tier Permissions.
     *
     * @param organization Organization
     * @throws APIManagementException if fails to retrieve Tier permissions
     */
    Set<TierPermissionDTO> getTierPermissions(String organization) throws APIManagementException;

    /**
     * Retrieve Tier Permission by Tier Name.
     *
     * @param tierName     Tier Name
     * @param organization Organization
     * @throws APIManagementException if fails to retrieve Tier permission
     */
    TierPermissionDTO getThrottleTierPermission(String tierName, String organization) throws APIManagementException;

    /**
     * Update Tier Permissions.
     *
     * @param tierName       Tier Name
     * @param permissionType Permission Type
     * @param roles          Roles
     * @param organization   Organization
     * @throws APIManagementException if fails to update Tier permissions
     */
    void updateThrottleTierPermissions(String tierName, String permissionType, String roles, String organization)
            throws APIManagementException;

    /**
     * Retrieve Tier Permissions by Organization.
     *
     * @param organization Organization
     * @throws APIManagementException if fails to retrieve Tier permissions
     */
    Set<TierPermissionDTO> getThrottleTierPermissions(String organization) throws APIManagementException;

}
