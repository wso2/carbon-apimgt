package org.wso2.carbon.apimgt.impl.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.impl.dto.JWKSConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerConfigurationsDto;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JWTValidatorHolder {
    private static final Log log = LogFactory.getLog(JWTValidatorHolder.class);
    private static Map<String, Map<String, JWTValidator>> tenantWiseJWTValidatorMap = new ConcurrentHashMap<>();
    private static Map<String, Map<String, String>> tenantWiseKeyManagerNameIssuerMap = new ConcurrentHashMap<>();
    private JWTValidatorHolder() {
    }

    public static void addJWTValidator(String tenantDomain, String name, String type,
                                       KeyManagerConfiguration keyManagerConfiguration) throws APIManagementException {
        Object selfValidateJWT = keyManagerConfiguration.getParameter(APIConstants.KeyManager.SELF_VALIDATE_JWT);
        if (selfValidateJWT != null && (Boolean) selfValidateJWT) {
            Map<String, JWTValidator> tokenIssuerWiseMap = tenantWiseJWTValidatorMap.getOrDefault(tenantDomain,
                    new HashMap<>());
            Map<String, String> keyManagerNameIssuerMapping =
                    tenantWiseKeyManagerNameIssuerMap.getOrDefault(tenantDomain, new HashMap<>());
            Object issuer = keyManagerConfiguration.getParameter(APIConstants.KeyManager.ISSUER);
            if (issuer != null) {
                TokenIssuerDto tokenIssuerDto = new TokenIssuerDto((String) issuer);
                Object claimMappings = keyManagerConfiguration.getParameter(APIConstants.KeyManager.CLAIM_MAPPING);
                if (claimMappings instanceof List) {
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.toJsonTree(claimMappings);
                    ClaimMappingDto[] claimMappingDto = gson.fromJson(jsonElement,ClaimMappingDto[].class);
                        tokenIssuerDto.addClaimMappings(claimMappingDto);
                }
                Object jwksEndpoint = keyManagerConfiguration.getParameter(APIConstants.KeyManager.JWKS_ENDPOINT);
                if (jwksEndpoint != null){
                    if (StringUtils.isNotEmpty((String) jwksEndpoint)){
                        JWKSConfigurationDTO jwksConfigurationDTO  = new JWKSConfigurationDTO();
                        jwksConfigurationDTO.setEnabled(true);
                        jwksConfigurationDTO.setUrl((String) jwksEndpoint);
                        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
                    }
                }
                KeyManagerConfigurationsDto keyManagerConfigurationsDto =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                .getAPIManagerConfiguration().getKeyManagerConfigurationsDto();
                JWTValidator jwtValidator = null;
                if (keyManagerConfigurationsDto != null){
                    KeyManagerConfigurationsDto.KeyManagerConfigurationDto keyManagerConfigurationDto =
                            keyManagerConfigurationsDto.getKeyManagerConfiguration().get(type);
                    if (keyManagerConfigurationDto != null &&
                            StringUtils.isNotEmpty(keyManagerConfigurationDto.getJwtValidatorImplementationClass())){
                        try {
                            jwtValidator  =
                                    (JWTValidator) Class
                                            .forName(keyManagerConfigurationDto.getJwtValidatorImplementationClass())
                                            .newInstance();
                        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                            log.error("Error while initializing JWT Validator",e);
                            throw new APIManagementException("Error while initializing JWT Validator",e);
                        }
                    }
                }
                if (jwtValidator == null){
                    jwtValidator = new JWTValidatorImpl();
                }
                jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);
                tokenIssuerWiseMap.put((String) issuer, jwtValidator);
                keyManagerNameIssuerMapping.put(name, (String) issuer);
            }
            tenantWiseJWTValidatorMap.put(tenantDomain, tokenIssuerWiseMap);
            tenantWiseKeyManagerNameIssuerMap.put(tenantDomain, keyManagerNameIssuerMapping);
        }
    }

    public static void deleteJWTValidator(String tenantDomain, String name) {

        Map<String, String> tokenIssuerWiseMap = tenantWiseKeyManagerNameIssuerMap.get(tenantDomain);
        if (tokenIssuerWiseMap != null) {
            String issuer = tokenIssuerWiseMap.get(name);
            if (StringUtils.isNotEmpty(issuer)) {
                tenantWiseJWTValidatorMap.getOrDefault(tenantDomain, new HashMap<>()).remove(issuer);
                tokenIssuerWiseMap.remove(issuer);
            }
        }
    }

    public static JWTValidator getJWTValidator(String tenantDomain, String issuer) {

        Map<String, JWTValidator> issuerWiseJWTValidator =
                tenantWiseJWTValidatorMap.getOrDefault(tenantDomain, new HashMap<>());
        JWTValidator jwtValidator = issuerWiseJWTValidator.get(issuer);
        if (jwtValidator != null) {
            return jwtValidator;
        }
        return null;
    }
}
