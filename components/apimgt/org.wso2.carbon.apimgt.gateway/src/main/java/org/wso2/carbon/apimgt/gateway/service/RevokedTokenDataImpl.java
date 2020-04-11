package org.wso2.carbon.apimgt.gateway.service;

import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.impl.token.RevokedTokenService;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class RevokedTokenDataImpl implements RevokedTokenService {

    public void addRevokedJWTIntoMap(String revokedToken, Long expiryTime) {
        RevokedJWTDataHolder.getInstance().addRevokedJWTToMap(revokedToken, expiryTime);
        // Add revoked token to

    }

    @Override
    public void revokedTokenFromGatewayCache(String accessToken,boolean isJwtToken) {
        String cachedTenantDomain;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            cachedTenantDomain = Utils.getCachedTenantDomain(accessToken);
            if (cachedTenantDomain == null) { //the token is not in cache
                return;
            }
            Utils.removeCacheEntryFromGatewayCache(accessToken);
            Utils.putInvalidTokenEntryIntoInvalidTokenCache(accessToken, cachedTenantDomain);
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
