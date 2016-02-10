/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an API for the required for implementing the functionality required by
 * the API providers. This include getting the list of keys issued for a given API, get the list of APIs
 * provided by him where a user has subscribed and perform different actions on issued keys like activating,
 * revoking, etc.
 */
public class APIKeyMgtProviderService extends AbstractAdmin {

    /**
     * Get the issued keys for a given API. This method returns the set of users who have subscribed
     * for the given API and the status of the Key, whether it is ACTIVE, BLOCKED OR REVOKED.
     *
     * @param apiInfoDTO Information about the API. Provider Name, API Name and Version uniquely identifies an API.
     * @return An array of APIKeyInfoDTO. Each APIKeyInfoDTO contains the user id and the status of the key.
     * @throws APIKeyMgtException Error has occurred when processing reading API Key Info from the database.
     */
    public APIKeyInfoDTO[] getIssuedKeyInfo(APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
            APIManagementException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getSubscribedUsersForAPI(apiInfoDTO);
    }

    /**
     * Get the list of APIs a user has subscribed for a given provider.
     *
     * @param userId     User Id
     * @param providerId Provider Id
     * @return Array of APIInfoDTO objects for each API that the user has subscribed for a given provider.
     * @throws APIKeyMgtException Error has occurred when processing reading API Info from the database.
     */
    public APIInfoDTO[] getAPIsOfUser(String userId, String providerId) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        APIInfoDTO[] apiInfoDTOs = apiMgtDAO.getSubscribedAPIsOfUser(userId);
        // Filter by Provider
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<APIInfoDTO>();
        for (APIInfoDTO apiInfoDTO : apiInfoDTOs) {
            if (apiInfoDTO.getProviderId().equalsIgnoreCase(providerId)) {
                apiInfoDTOList.add(apiInfoDTO);
            }
        }
        return apiInfoDTOList.toArray(new APIInfoDTO[apiInfoDTOList.size()]);
    }

    /**
     * Activate the keys of the set of users subscribed for the given API
     * @param users Subscribed Users whose keys will be activated
     * @param apiInfoDTO API Information
     * @throws APIKeyMgtException Error has occurred when processing updating the key Info from the database.
     */
    public void activateAccessTokens(String[] users, APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        for (String userId : users) {
            apiMgtDAO.changeAccessTokenStatus(userId, apiInfoDTO, APIConstants.TokenStatus.ACTIVE);
        }
    }

    /**
     * Block the keys of the set of users subscribed for the given API
     * @param users Subscribed Users whose keys will be blocked
     * @param apiInfoDTO API Information
     * @throws APIKeyMgtException Error has occurred when processing updating the key Info from the database.
     */
    public void blockAccessTokens(String[] users, APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        for (String userId : users) {
            apiMgtDAO.changeAccessTokenStatus(userId, apiInfoDTO, APIConstants.TokenStatus.BLOCKED);
        }
    }

    /**
     * Revoke the keys of the set of users subscribed for the given API
     * @param users Subscribed Users whose keys will be revoked.
     * @param apiInfoDTO API Information
     * @throws APIKeyMgtException Error has occurred when processing updating the key Info from the database.
     */
    public void revokeAccessTokens(String[] users, APIInfoDTO apiInfoDTO) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        for (String userId : users) {
            apiMgtDAO.changeAccessTokenStatus(userId, apiInfoDTO, APIConstants.TokenStatus.REVOKED);
        }
    }

    /**
     * Removes passed consumer keys from scope cache
     *
     * @param consumerKeys
     */
    public void removeScopeCache(String[] consumerKeys) {

        if (consumerKeys != null && consumerKeys.length != 0) {

            Cache appCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants
                                                                                                              .APP_SCOPE_CACHE);
            for (String consumerKey : consumerKeys) {
                // removing from app scope cache
                appCache.remove(consumerKey);
            }
        }
    }

}
