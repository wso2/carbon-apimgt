package org.wso2.carbon.apimgt.gateway.handlers.security.jwt;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;

import javax.cache.Cache;

public class JWTValidatorWrapper extends JWTValidator {

    private Cache gatewayTokenCache;
    private Cache invalidTokenCache;
    private Cache gatewayKeyCache;
    private Cache gatewayJWTTokenCache;
    private APIManagerConfiguration apiManagerConfiguration;
    public JWTValidatorWrapper(APIKeyValidator apiKeyValidator) throws APIManagementException {
        super(apiKeyValidator, "carbon.super");
    }

    public JWTValidatorWrapper(String apiLevelPolicy, boolean isGatewayTokenCacheEnabled,
                               APIKeyValidator apiKeyValidator, boolean jwtGenerationEnabled,
                               AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator,
                               JWTConfigurationDto jwtConfigurationDto,
                               JWTValidationService jwtValidationService, Cache invalidTokenCache,
                               Cache gatewayTokenCache, Cache gatewayKeyCache, Cache gatewayJWTTokenCache) {

        super(apiLevelPolicy, isGatewayTokenCacheEnabled, apiKeyValidator, jwtGenerationEnabled,
                apiMgtGatewayJWTGenerator, jwtConfigurationDto, jwtValidationService);
        this.gatewayJWTTokenCache = gatewayJWTTokenCache;
        this.gatewayKeyCache = gatewayKeyCache;
        this.gatewayTokenCache = gatewayTokenCache;
        this.invalidTokenCache = invalidTokenCache;
    }

    @Override
    protected Cache getGatewayTokenCache() {

        return gatewayTokenCache;
    }

    @Override
    protected Cache getInvalidTokenCache() {

        return invalidTokenCache;
    }

    @Override
    protected Cache getGatewayKeyCache() {

        return gatewayKeyCache;
    }

    @Override
    protected Cache getGatewayJWTTokenCache() {

        return gatewayJWTTokenCache;
    }

    public void setApiManagerConfiguration(APIManagerConfiguration apiManagerConfiguration) {

        this.apiManagerConfiguration = apiManagerConfiguration;
    }

    @Override
    protected long getTimeStampSkewInSeconds() {

        return 0l;
    }
}
