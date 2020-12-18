/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.service;

import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.token.RevokedTokenService;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;

public class RevokedTokenDataImpl implements RevokedTokenService {

    public void addRevokedJWTIntoMap(String revokedToken, Long expiryTime) {
        RevokedJWTDataHolder.getInstance().addRevokedJWTToMap(revokedToken, expiryTime);
        // Add revoked token to the Map
    }

    @Override
    public void removeTokenFromGatewayCache(String accessToken, boolean isJwtToken) {
        String cachedTenantDomain;
        String apiKeyCachedTenantDomain;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            cachedTenantDomain = Utils.getCachedTenantDomain(accessToken);
            apiKeyCachedTenantDomain = Utils.getApiKeyCachedTenantDomain(accessToken);
            if (cachedTenantDomain == null && apiKeyCachedTenantDomain == null) { //the token is not in cache
                return;
            }
            if (cachedTenantDomain != null) {
                Utils.removeCacheEntryFromGatewayCache(accessToken);
                Utils.putInvalidTokenEntryIntoInvalidTokenCache(accessToken, cachedTenantDomain);
            }
            //Clear the API Key cache if revoked token is in the JWT format
            if (isJwtToken) {
                Utils.removeCacheEntryFromGatewayAPiKeyCache(accessToken);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        //Remove token from the token's own tenant's cache.
        Utils.removeTokenFromTenantTokenCache(accessToken, cachedTenantDomain);
        Utils.putInvalidTokenIntoTenantInvalidTokenCache(accessToken, cachedTenantDomain);
    }
}
