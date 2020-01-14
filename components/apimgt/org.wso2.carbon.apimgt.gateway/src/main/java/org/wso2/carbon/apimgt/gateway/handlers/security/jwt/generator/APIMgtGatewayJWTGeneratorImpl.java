package org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class APIMgtGatewayJWTGeneratorImpl extends AbstractAPIMgtGatewayJWTGenerator {

    @Override
    public Map<String, Object> populateStandardClaims(JWTInfoDto jwtInfoDto) {

        long currentTime = System.currentTimeMillis();
        long expireIn = currentTime + getTTL() * 1000;
        String dialect = getDialectURI();
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", API_GATEWAY_ID);
        claims.put("exp", String.valueOf(expireIn));
        if (StringUtils.isNotEmpty(jwtInfoDto.getSubscriber())) {
            claims.put(dialect + "/subscriber", jwtInfoDto.getSubscriber());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getApplicationid())) {
            claims.put(dialect + "/applicationid", jwtInfoDto.getApplicationid());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getApplicationname())) {
            claims.put(dialect + "/applicationname", jwtInfoDto.getApplicationname());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getApplicationtier())) {
            claims.put(dialect + "/applicationtier", jwtInfoDto.getApplicationtier());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getApicontext())) {
            claims.put(dialect + "/apicontext", jwtInfoDto.getApicontext());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getVersion())) {
            claims.put(dialect + "/version", jwtInfoDto.getVersion());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getSubscriptionTier())) {
            claims.put(dialect + "/tier", jwtInfoDto.getSubscriptionTier());
        }
        if (StringUtils.isNotEmpty(jwtInfoDto.getKeytype())) {
            claims.put(dialect + "/keytype", jwtInfoDto.getKeytype());
        } else {
            claims.put(dialect + "/keytype", "PRODUCTION");
        }
        claims.put(dialect + "/usertype", APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN);
        claims.put(dialect + "/enduser", jwtInfoDto.getEnduser());
        claims.put(dialect + "/enduserTenantId", String.valueOf(jwtInfoDto.getEndusertenantid()));
        return claims;
    }

    @Override
    public Map<String, Object> populateCustomClaims(JWTInfoDto jwtInfoDto) {

        JWTConfigurationDto jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto();
        Map<String, Object> claims = new HashMap<>();
        Set<String> claimConfigurations = jwtConfigurationDto.getClaimConfigurations();
        JSONObject jwtToken = jwtInfoDto.getJwtToken();
        if (jwtToken != null) {
            for (String claim : claimConfigurations) {
                if (jwtToken.get(claim) != null) {
                    claims.put(claim, jwtToken.get(claim));
                }
            }
        }
        return claims;
    }
}
