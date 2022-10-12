package org.wso2.apk.apimgt.impl.dao;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.BlockConditionsDTO;

import java.util.List;

public interface BlockConditionDAO {

    /**
     * Get Block Conditions by Organization.
     *
     * @param organization Organization
     * @return List of Block Conditions
     * @throws APIManagementException
     */
    List<BlockConditionsDTO> getBlockConditions(String organization) throws APIManagementException;

    /**
     * Get details of a block condition by Id
     *
     * @param conditionId id of the condition
     * @return Block conditoin represented by the UUID
     * @throws APIManagementException
     */
    BlockConditionsDTO getBlockCondition(int conditionId) throws APIManagementException;

    /**
     * Get details of a block condition by UUID
     *
     * @param uuid uuid of the block condition
     * @return Block condition represented by the UUID
     * @throws APIManagementException
     */
    BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException;

    /**
     * Update the block condition state true (Enabled) /false (Disabled) given the UUID
     *
     * @param conditionId id of the block condition
     * @param state       blocking state
     * @return true if the operation was success
     * @throws APIManagementException
     */
    boolean updateBlockConditionState(int conditionId, String state) throws APIManagementException;

    /**
     * Update the block condition state true (Enabled) /false (Disabled) given the UUID
     *
     * @param uuid  UUID of the block condition
     * @param state blocking state
     * @return true if the operation was success
     * @throws APIManagementException
     */
    boolean updateBlockConditionStateByUUID(String uuid, String state) throws APIManagementException;

    /**
     * Add a block condition
     *
     * @return uuid of the block condition if successfully added
     * @throws APIManagementException
     */
    BlockConditionsDTO addBlockConditions(BlockConditionsDTO blockConditionsDTO) throws
            APIManagementException;

    /**
     * Delete the block condition given the id
     *
     * @param conditionId id of the condition
     * @return true if successfully deleted
     * @throws APIManagementException
     */
    boolean deleteBlockCondition(int conditionId) throws APIManagementException;
}
