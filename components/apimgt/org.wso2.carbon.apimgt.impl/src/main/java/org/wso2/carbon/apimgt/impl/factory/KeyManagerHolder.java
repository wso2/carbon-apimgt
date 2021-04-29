/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.factory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.impl.dto.JWKSConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.dto.TenantKeyManagerDto;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;
import org.wso2.carbon.apimgt.impl.loader.KeyManagerConfigurationDataRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.cert.X509Certificate;

/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class KeyManagerHolder {

    private static Log log = LogFactory.getLog(KeyManagerHolder.class);
    private static Map<String, TenantKeyManagerDto> tenantWiseMap = new HashMap<>();
    private static Map<String, KeyManagerDto> globalJWTValidatorMap = new HashMap<>();
    public static void addKeyManagerConfiguration(String tenantDomain, String name, String type,
                                                  KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

        String issuer = (String) keyManagerConfiguration.getParameter(APIConstants.KeyManager.ISSUER);

        TenantKeyManagerDto tenantKeyManagerDto = tenantWiseMap.get(tenantDomain);
        if (tenantKeyManagerDto == null) {
            tenantKeyManagerDto = new TenantKeyManagerDto();
        }
        if (tenantKeyManagerDto.getKeyManagerByName(name) != null) {
            log.error("Key Manager " + name + " already initialized in tenant " + tenantDomain);
        }
        if (keyManagerConfiguration.isEnabled()) {
            KeyManager keyManager = null;
            JWTValidator jwtValidator = null;
            APIManagerConfiguration apiManagerConfiguration =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration();
            String defaultKeyManagerType =
                    apiManagerConfiguration.getFirstProperty(APIConstants.DEFAULT_KEY_MANAGER_TYPE);
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration =
                    ServiceReferenceHolder.getInstance().getKeyManagerConnectorConfiguration(type);
            if (keyManagerConnectorConfiguration != null) {
                if (StringUtils.isNotEmpty(keyManagerConnectorConfiguration.getImplementation())) {
                    try {
                        keyManager = (KeyManager) Class
                                .forName(keyManagerConnectorConfiguration.getImplementation()).newInstance();
                        keyManager.setTenantDomain(tenantDomain);
                        if (StringUtils.isNotEmpty(defaultKeyManagerType) && defaultKeyManagerType.equals(type)){
                            keyManagerConfiguration.addParameter(APIConstants.KEY_MANAGER_USERNAME,
                                    apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME));
                            keyManagerConfiguration.addParameter(APIConstants.KEY_MANAGER_PASSWORD,
                                    apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD));
                        }
                        keyManagerConfiguration.setName(name);
                        keyManagerConfiguration.setTenantDomain(tenantDomain);
                        keyManagerConfiguration.setType(type);
                        keyManager.loadConfiguration(keyManagerConfiguration);
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        throw new APIManagementException("Error while loading keyManager configuration", e);
                    }
                }
                jwtValidator =
                        getJWTValidator(keyManagerConfiguration, keyManagerConnectorConfiguration.getJWTValidator());
            } else {
                if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(type)) {
                    keyManager = new AMDefaultKeyManagerImpl();
                    keyManager.setTenantDomain(tenantDomain);
                    keyManager.loadConfiguration(keyManagerConfiguration);
                    jwtValidator = getJWTValidator(keyManagerConfiguration, null);
                }
            }
            KeyManagerDto keyManagerDto = new KeyManagerDto();
            keyManagerDto.setName(name);
            keyManagerDto.setIssuer(issuer);
            keyManagerDto.setJwtValidator(jwtValidator);
            keyManagerDto.setKeyManager(keyManager);
            tenantKeyManagerDto.putKeyManagerDto(keyManagerDto);
            tenantWiseMap.put(tenantDomain, tenantKeyManagerDto);
        }

    }


    public static Map<String, KeyManagerDto> getTenantKeyManagers(String tenantDomain) {

        TenantKeyManagerDto tenantKeyManagerDto = getTenantKeyManagerDto(tenantDomain);
        if (tenantKeyManagerDto != null) {
            return tenantKeyManagerDto.getKeyManagerMap();
        } else {
            return Collections.emptyMap();
        }
    }

    private KeyManagerHolder() {

    }

    public static void updateKeyManagerConfiguration(String tenantDomain, String name, String type,
                                                     KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

        removeKeyManagerConfiguration(tenantDomain, name);
        if (keyManagerConfiguration.isEnabled()) {
            addKeyManagerConfiguration(tenantDomain, name, type, keyManagerConfiguration);
        }
    }

    public static void removeKeyManagerConfiguration(String tenantDomain, String name) {

        TenantKeyManagerDto tenantKeyManagerDto = tenantWiseMap.get(tenantDomain);
        if (tenantKeyManagerDto != null) {
            tenantKeyManagerDto.removeKeyManagerDtoByName(name);
        }
    }

    private static JWTValidator getJWTValidator(KeyManagerConfiguration keyManagerConfiguration,
                                                String jwtValidatorImplementation)
            throws APIManagementException {

        Object selfValidateJWT = keyManagerConfiguration.getParameter(APIConstants.KeyManager.SELF_VALIDATE_JWT);
        if (selfValidateJWT != null && (Boolean) selfValidateJWT) {
            Object issuer = keyManagerConfiguration.getParameter(APIConstants.KeyManager.ISSUER);
            if (issuer != null) {
                TokenIssuerDto tokenIssuerDto = new TokenIssuerDto((String) issuer);
                Object claimMappings = keyManagerConfiguration.getParameter(APIConstants.KeyManager.CLAIM_MAPPING);
                if (claimMappings instanceof List) {
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.toJsonTree(claimMappings);
                    ClaimMappingDto[] claimMappingDto = gson.fromJson(jsonElement, ClaimMappingDto[].class);
                    tokenIssuerDto.addClaimMappings(claimMappingDto);
                }
                Object consumerKeyClaim =
                        keyManagerConfiguration.getParameter(APIConstants.KeyManager.CONSUMER_KEY_CLAIM);
                if (consumerKeyClaim instanceof String && StringUtils.isNotEmpty((String) consumerKeyClaim)) {
                    tokenIssuerDto.setConsumerKeyClaim((String) consumerKeyClaim);
                }
                Object scopeClaim =
                        keyManagerConfiguration.getParameter(APIConstants.KeyManager.SCOPES_CLAIM);
                if (scopeClaim instanceof String && StringUtils.isNotEmpty((String) scopeClaim)) {
                    tokenIssuerDto.setScopesClaim((String) scopeClaim);
                }
                Object jwksEndpoint = keyManagerConfiguration.getParameter(APIConstants.KeyManager.JWKS_ENDPOINT);
                if (jwksEndpoint != null) {
                    if (StringUtils.isNotEmpty((String) jwksEndpoint)) {
                        JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
                        jwksConfigurationDTO.setEnabled(true);
                        jwksConfigurationDTO.setUrl((String) jwksEndpoint);
                        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
                    }
                }
                Object certificateType = keyManagerConfiguration.getParameter(APIConstants.KeyManager.CERTIFICATE_TYPE);
                Object certificateValue =
                        keyManagerConfiguration.getParameter(APIConstants.KeyManager.CERTIFICATE_VALUE);
                if (certificateType != null && StringUtils.isNotEmpty((String) certificateType) &&
                        certificateValue != null && StringUtils.isNotEmpty((String) certificateValue)) {
                    if (APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT.equals(certificateType)) {
                        JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
                        jwksConfigurationDTO.setEnabled(true);
                        jwksConfigurationDTO.setUrl((String) certificateValue);
                        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
                    } else {
                        X509Certificate x509Certificate =
                                APIUtil.retrieveCertificateFromContent((String) certificateValue);
                        if (x509Certificate != null) {
                            tokenIssuerDto.setCertificate(x509Certificate);
                        }
                    }
                }
                JWTValidator jwtValidator;
                if (StringUtils.isEmpty(jwtValidatorImplementation)) {
                    jwtValidator = new JWTValidatorImpl();
                } else {
                    try {
                        jwtValidator = (JWTValidator) Class.forName(jwtValidatorImplementation).newInstance();
                    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                        log.error("Error while initializing JWT Validator", e);
                        throw new APIManagementException("Error while initializing JWT Validator", e);
                    }
                }
                jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);
                return jwtValidator;
            }
        }
        return null;
    }

    public static KeyManager getKeyManagerInstance(String tenantDomain, String keyManagerName) {

        TenantKeyManagerDto tenantKeyManagerDto = getTenantKeyManagerDto(tenantDomain);
        if (tenantKeyManagerDto != null) {
            KeyManagerDto keyManagerDto = tenantKeyManagerDto.getKeyManagerByName(keyManagerName);
            if (keyManagerDto == null) {
                return null;
            }
            return keyManagerDto.getKeyManager();
        }
        return null;
    }

    public static KeyManagerDto getKeyManagerByIssuer(String tenantDomain, String issuer) {

        if (globalJWTValidatorMap.containsKey(issuer)) {
            return globalJWTValidatorMap.get(issuer);
        }
        TenantKeyManagerDto tenantKeyManagerDto = getTenantKeyManagerDto(tenantDomain);
        if (tenantKeyManagerDto != null) {
            return tenantKeyManagerDto.getKeyManagerDtoByIssuer(issuer);
        }
        return null;
    }

    private static TenantKeyManagerDto getTenantKeyManagerDto(String tenantDomain) {

        TenantKeyManagerDto tenantKeyManagerDto = tenantWiseMap.get(tenantDomain);
        if (tenantKeyManagerDto == null) {
            synchronized ("KeyManagerHolder".concat(tenantDomain)) {
                if (tenantKeyManagerDto == null) {
                    new KeyManagerConfigurationDataRetriever(tenantDomain).run();
                    tenantKeyManagerDto = tenantWiseMap.get(tenantDomain);
                }
            }
        }
        return tenantKeyManagerDto;
    }
    public static void addGlobalJWTValidators(TokenIssuerDto tokenIssuerDto) {

        KeyManagerDto keyManagerDto = new KeyManagerDto();
        keyManagerDto.setIssuer(tokenIssuerDto.getIssuer());
        keyManagerDto.setName(APIConstants.KeyManager.DEFAULT_KEY_MANAGER);
        JWTValidator jwtValidator = new JWTValidatorImpl();
        jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);
        keyManagerDto.setJwtValidator(jwtValidator);
        globalJWTValidatorMap.put(tokenIssuerDto.getIssuer(), keyManagerDto);
    }
}
