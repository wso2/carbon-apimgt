package org.wso2.carbon.apimgt.impl.token;

public interface RevokedTokenService {

    public void addRevokedJWTIntoMap(String revokedToken, Long expiryTime);

    public void revokedTokenFromGatewayCache(String accessToken, boolean isJwtToken);
}
