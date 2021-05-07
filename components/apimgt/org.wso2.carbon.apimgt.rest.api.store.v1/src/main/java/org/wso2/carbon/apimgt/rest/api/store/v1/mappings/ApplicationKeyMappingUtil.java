/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for application key generation related operations
 */
public class ApplicationKeyMappingUtil {

    private static final Log log = LogFactory.getLog(ApplicationKeyMappingUtil.class);

    /**
     * Insert the application related details to a DTO Object
     *
     * @param keyDetails Application related details map
     * @param applicationKeyType Key type of the application
     * @return DTO object with application related details
     */
    @SuppressWarnings("unchecked")
    public static ApplicationKeyDTO fromApplicationKeyToDTO(Map<String, Object> keyDetails, String applicationKeyType) {
        ApplicationKeyDTO applicationKeyDTO = new ApplicationKeyDTO();
        applicationKeyDTO.setConsumerKey((String) keyDetails.get(APIConstants.FrontEndParameterNames.CONSUMER_KEY));
        applicationKeyDTO.setKeyMappingId((String) keyDetails.get(APIConstants.FrontEndParameterNames.KEY_MAPPING_ID));
        applicationKeyDTO
                .setConsumerSecret((String) keyDetails.get(APIConstants.FrontEndParameterNames.CONSUMER_SECRET));
        applicationKeyDTO.setKeyState((String) keyDetails.get(APIConstants.FrontEndParameterNames.KEY_STATE));
        Object mode = keyDetails.get(APIConstants.FrontEndParameterNames.MODE);
        if (mode != null) {
            applicationKeyDTO.setMode(ApplicationKeyDTO.ModeEnum.valueOf((String) mode));
        }
        try {
            String appDetailsString = (String) keyDetails.get(ApplicationConstants.OAUTH_APP_DETAILS);
            if (appDetailsString != null) {
                JSONObject appDetailsJsonObj = (JSONObject) new JSONParser().parse(appDetailsString);
                if (appDetailsJsonObj != null) {
                    applicationKeyDTO.setKeyType(ApplicationKeyDTO.KeyTypeEnum.valueOf(applicationKeyType));
                    String supportedGrantTypes = (String) appDetailsJsonObj
                            .get(ApplicationConstants.OAUTH_CLIENT_GRANT);
                    if (supportedGrantTypes != null) {
                        applicationKeyDTO.setSupportedGrantTypes(Arrays.asList(supportedGrantTypes.split(" ")));
                    }
                    String callbackUrl = (String) appDetailsJsonObj.get(ApplicationConstants.OAUTH_REDIRECT_URIS);
                    applicationKeyDTO.setCallbackUrl(callbackUrl);

                    Object additionalPropertiesObj = appDetailsJsonObj.get(APIConstants.JSON_ADDITIONAL_PROPERTIES);
                    if (additionalPropertiesObj != null) {
                        if (additionalPropertiesObj instanceof JSONObject){
                            Map additionalPropertiesMap = new HashMap();
                            additionalPropertiesMap.putAll((Map) additionalPropertiesObj);
                            applicationKeyDTO.setAdditionalProperties(additionalPropertiesMap);
                        }else if (additionalPropertiesObj instanceof String){
                            applicationKeyDTO.setAdditionalProperties(additionalPropertiesObj);
                        }

                    }
                    
                }
            }

            ApplicationTokenDTO tokenDTO = new ApplicationTokenDTO();
            tokenDTO.setValidityTime((Long) keyDetails.get(APIConstants.AccessTokenConstants.VALIDITY_TIME));
            tokenDTO.setAccessToken((String) keyDetails.get(APIConstants.AccessTokenConstants.ACCESS_TOKEN));
            String[] tokenScopes = (String[]) keyDetails.get(APIConstants.AccessTokenConstants.TOKEN_SCOPES);
            if (tokenScopes != null) {
                tokenDTO.setTokenScopes(Arrays.asList(tokenScopes));
            }

            applicationKeyDTO.setToken(tokenDTO);
        } catch (ParseException e) {
            String errorMsg = "Error while parsing application details string";
            log.error(errorMsg, e);
            throw new InternalServerErrorException(errorMsg, e);
        }
        return applicationKeyDTO;
    }

    /**
     * Insert the application related details to a DTO Object using api key
     *
     * @param apiKey Object that contains details needed during token request
     * @return DTO object with application related details
     */
    public static ApplicationKeyDTO fromApplicationKeyToDTO(APIKey apiKey) {
        ApplicationKeyDTO applicationKeyDTO = new ApplicationKeyDTO();
        applicationKeyDTO.setKeyType(ApplicationKeyDTO.KeyTypeEnum.valueOf(apiKey.getType()));
        applicationKeyDTO.setConsumerKey(apiKey.getConsumerKey());
        applicationKeyDTO.setConsumerSecret(apiKey.getConsumerSecret());
        applicationKeyDTO.setKeyState(apiKey.getState());
        applicationKeyDTO.setMode(ApplicationKeyDTO.ModeEnum.valueOf(apiKey.getCreateMode()));
        applicationKeyDTO.setKeyManager(apiKey.getKeyManager());
        applicationKeyDTO.setKeyMappingId(apiKey.getMappingId());
        if (apiKey.getGrantTypes() != null) {
            applicationKeyDTO.setSupportedGrantTypes(Arrays.asList(apiKey.getGrantTypes().split(" ")));
        } else {
            applicationKeyDTO.setSupportedGrantTypes(null);
        }
        applicationKeyDTO.setCallbackUrl(apiKey.getCallbackUrl());

        ApplicationTokenDTO tokenDTO = new ApplicationTokenDTO();
        if (apiKey.getTokenScope() != null) {
            tokenDTO.setTokenScopes(Arrays.asList(apiKey.getTokenScope().split(" ")));
        }
        tokenDTO.setAccessToken(apiKey.getAccessToken());
        tokenDTO.setValidityTime(apiKey.getValidityPeriod());
        applicationKeyDTO.setToken(tokenDTO);
        applicationKeyDTO.setAdditionalProperties(apiKey.getAdditionalProperties());
        return applicationKeyDTO;
    }

    public static APIKeyDTO formApiKeyToDTO(String apiKey, int validityTime){
        APIKeyDTO apiKeyDto = new APIKeyDTO();
        apiKeyDto.setApikey(apiKey);
        apiKeyDto.setValidityTime(validityTime);
        return apiKeyDto;
    }
}
