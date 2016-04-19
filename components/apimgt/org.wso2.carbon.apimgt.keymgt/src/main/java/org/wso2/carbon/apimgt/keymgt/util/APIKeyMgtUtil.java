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

package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class APIKeyMgtUtil {

    private static final Log log = LogFactory.getLog(APIKeyMgtUtil.class);

    private  static boolean isKeyCacheInistialized = false;

    public static String getTenantDomainFromTenantId(int tenantId) throws APIKeyMgtException {
        try {
            TenantManager tenantManager = APIKeyMgtDataHolder.getRealmService().getTenantManager();
            return tenantManager.getDomain(tenantId);
        } catch (UserStoreException e) {
            String errorMsg = "Error when getting the Tenant domain name for the given Tenant Id";
            log.error(errorMsg, e);
            throw new APIKeyMgtException(errorMsg, e);
        }
    }

    public static Map<String,String> constructParameterMap(OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] params){
        Map<String,String> paramMap = null;
        if(params != null){
            paramMap = new HashMap<String, String>();
            for(OAuth2TokenValidationRequestDTO.TokenValidationContextParam param : params){
                paramMap.put(param.getKey(),param.getValue());
            }
        }

        return paramMap;
    }
    /**
     * Get a database connection instance from the Identity Persistence Manager
     * @return Database Connection
     */
    public static Connection getDBConnection(){
        return IdentityDatabaseUtil.getDBConnection();
    }

    /**
     * Get the KeyValidationInfo object from cache, for a given cache-Key
     *
     * @param cacheKey Key for the Cache Entry
     * @return APIKeyValidationInfoDTO
     * @throws APIKeyMgtException
     */
    public static APIKeyValidationInfoDTO getFromKeyManagerCache(String cacheKey) {

        APIKeyValidationInfoDTO info = null;

        boolean cacheEnabledKeyMgt = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        Cache cache = getKeyManagerCache();

        //We only fetch from cache if KeyMgtValidationInfoCache is enabled.
        if (cacheEnabledKeyMgt) {
            info = (APIKeyValidationInfoDTO) cache.get(cacheKey);
            //If key validation information is not null then only we proceed with cached object
            if (info != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found cached access token for : " + cacheKey + ".");
                }
            }
        }

        return info;
    }


    /**
     * Store KeyValidationInfoDTO in Key Manager Cache
     *
     * @param cacheKey          Key for the Cache Entry to be stored
     * @param validationInfoDTO KeyValidationInfoDTO object
     */
    public static void writeToKeyManagerCache(String cacheKey, APIKeyValidationInfoDTO validationInfoDTO) {

        boolean cacheEnabledKeyMgt = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        if (cacheKey != null) {
            if (log.isDebugEnabled()) {
                log.debug("Storing KeyValidationDTO for key: " + cacheKey + ".");
            }
        }

        if (validationInfoDTO != null) {
            if (cacheEnabledKeyMgt) {
                Cache cache = getKeyManagerCache();
                cache.put(cacheKey, validationInfoDTO);
            }
        }
    }

    /**
     * Remove APIKeyValidationInfoDTO from Key Manager Cache
     *
     * @param cacheKey Key for the Cache Entry to be removed
     */
    public static void removeFromKeyManagerCache(String cacheKey) {

        boolean cacheEnabledKeyMgt = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        if (cacheKey != null && cacheEnabledKeyMgt) {

            Cache cache = getKeyManagerCache();
            cache.remove(cacheKey);
            log.debug("KeyValidationInfoDTO removed for key : " + cacheKey);
        }
    }

    private static Cache getKeyManagerCache(){
        String apimKeyCacheExpiry = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.KEY_MANAGER_TOKEN_CACHE_EXPIRY);
        if(!isKeyCacheInistialized && apimKeyCacheExpiry != null ) {
            isKeyCacheInistialized = true;
            return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    createCacheBuilder(APIConstants.KEY_CACHE_NAME)
                    .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                            Long.parseLong(apimKeyCacheExpiry)))
                    .setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                            Long.parseLong(apimKeyCacheExpiry))).setStoreByValue(false).build();
        } else{
          return  Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    getCache(APIConstants.KEY_CACHE_NAME);
        }

    }

    /**
     * This returns API object for given APIIdentifier. Reads from registry entry for given APIIdentifier
     * creates API object
     *
     * @param identifier APIIdentifier object for the API
     * @return API object for given identifier
     * @throws APIManagementException on error in getting API artifact
     */
    public static API getAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);

        try {
            Registry registry = APIKeyMgtDataHolder.getRegistryService().getGovernanceSystemRegistry();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact, registry);

        } catch (RegistryException e) {
            return null;
        }
    }

}
