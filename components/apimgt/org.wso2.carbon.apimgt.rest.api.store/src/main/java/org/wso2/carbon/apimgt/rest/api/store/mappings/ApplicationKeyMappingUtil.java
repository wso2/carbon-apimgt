/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.util.ArrayStack;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.APIKey;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.ApplicationConstants;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TokenDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApplicationKeyMappingUtil {

    private static final Logger log = LoggerFactory.getLogger(ApplicationKeyMappingUtil.class);

    public static ApplicationKeyDTO fromApplicationKeyToDTO(APIKey apiKey) {
        ApplicationKeyDTO applicationKeyDTO = new ApplicationKeyDTO();
        applicationKeyDTO.setKeyType(ApplicationKeyDTO.KeyTypeEnum.valueOf(apiKey.getType()));
        applicationKeyDTO.setConsumerKey(apiKey.getConsumerKey());
        applicationKeyDTO.setConsumerSecret(apiKey.getConsumerSecret());
        applicationKeyDTO.setKeyState(apiKey.getState());
        applicationKeyDTO.setSupportedGrantTypes(null); //this is not supported by impl yet

        TokenDTO tokenDTO = new TokenDTO();
        if (apiKey.getTokenScope() != null) {
            tokenDTO.setTokenScopes(Arrays.asList(apiKey.getTokenScope().split(" ")));
        }
        tokenDTO.setAccessToken(apiKey.getAccessToken());
        tokenDTO.setValidityTime(apiKey.getValidityPeriod());
        applicationKeyDTO.setToken(tokenDTO);
        return applicationKeyDTO;
    }

    public static ApplicationKeyDTO fromApplicationKeyToDTO(Map<String, Object> keyDetails, String applicationKeyType) {
        ApplicationKeyDTO applicationKeyDTO = new ApplicationKeyDTO();
        applicationKeyDTO.setConsumerKey((String) keyDetails.get(KeyManagerConstants.KeyDetails.CONSUMER_KEY));
        applicationKeyDTO.setConsumerSecret((String) keyDetails.get(KeyManagerConstants.KeyDetails.CONSUMER_SECRET));
        applicationKeyDTO.setKeyState((String) keyDetails.get(APIConstants.FrontEndParameterNames.KEY_STATE));
        applicationKeyDTO.setSupportedGrantTypes(
                ((List<String>) keyDetails.get(KeyManagerConstants.KeyDetails.SUPPORTED_GRANT_TYPES)));
        String appDetailsString = (String) keyDetails.get(KeyManagerConstants.KeyDetails.APP_DETAILS);
        if (appDetailsString != null) {
            JsonObject appDetailsJsonObj = (JsonObject) new JsonParser().parse(appDetailsString);
            if (appDetailsJsonObj != null) {
                applicationKeyDTO.setKeyType(ApplicationKeyDTO.KeyTypeEnum.valueOf(applicationKeyType));
            }
        }

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setValidityTime((Long) keyDetails.get(KeyManagerConstants.KeyDetails.VALIDITY_TIME));
        tokenDTO.setAccessToken((String) keyDetails.get(KeyManagerConstants.KeyDetails.ACCESS_TOKEN));
        String[] tokenScopes = (String[]) keyDetails.get(APIConstants.AccessTokenConstants.TOKEN_SCOPES);
        if (tokenScopes != null) {
            tokenDTO.setTokenScopes(Arrays.asList(tokenScopes));
        }

        applicationKeyDTO.setToken(tokenDTO);
        return applicationKeyDTO;
    }
}
