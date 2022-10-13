package org.wso2.apk.apimgt.impl.dao;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.impl.dto.TierPermissionDTO;

import java.util.Set;

public interface TierDAO {

    /**
     * Update Tier Permissions.
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles
     * @param tenantId
     * @throws APIManagementException if fails to update Tier permissions
     */
    void updateTierPermissions(String tierName, String permissionType, String roles, int tenantId)
            throws APIManagementException;

    /**
     * Delete Tier Permissions.
     *
     * @param tierName Tier Name
     * @param tenantId
     * @throws APIManagementException if fails to delete Tier permissions
     */
    void deleteThrottlingPermissions(String tierName, int tenantId) throws APIManagementException;

    /**
     * Retrieve Tier Permissions.
     *
     * @param tenantId
     * @throws APIManagementException if fails to retrieve Tier permissions
     */
    Set<TierPermissionDTO> getTierPermissions(int tenantId) throws APIManagementException;

    /**
     * Retrieve Tier Permission by Tier Name.
     *
     * @param tierName Tier Name
     * @param tenantId Organization
     * @throws APIManagementException if fails to retrieve Tier permission
     */
    TierPermissionDTO getThrottleTierPermission(String tierName, int tenantId) throws APIManagementException;

    /**
     * Update Tier Permissions.
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles
     * @param tenantId Organization
     * @throws APIManagementException if fails to update Tier permissions
     */
    void updateThrottleTierPermissions(String tierName, String permissionType, String roles, int tenantId)
            throws APIManagementException;

    /**
     * Retrieve Tier Permissions by Organization.
     *
     * @param tenantId Organization
     * @throws APIManagementException if fails to retrieve Tier permissions
     */
    Set<TierPermissionDTO> getThrottleTierPermissions(int tenantId) throws APIManagementException;

}
