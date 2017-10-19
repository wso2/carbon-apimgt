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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.ApplicationToken;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenDTO;

import java.util.ArrayList;
import java.util.List;

public class ApplicationKeyMappingUtil {

    private static final Logger log = LoggerFactory.getLogger(ApplicationKeyMappingUtil.class);

    public static ApplicationKeysDTO fromApplicationKeysToDTO(OAuthApplicationInfo applicationKeys) {
        ApplicationKeysDTO applicationKeyDTO = new ApplicationKeysDTO();
        applicationKeyDTO.setKeyType(ApplicationKeysDTO.KeyTypeEnum.fromValue(applicationKeys.getKeyType()));
        applicationKeyDTO.setConsumerKey(applicationKeys.getClientId());
        applicationKeyDTO.setConsumerSecret(applicationKeys.getClientSecret());
        applicationKeyDTO.setSupportedGrantTypes(applicationKeys.getGrantTypes());
        return applicationKeyDTO;
    }

    public static List<ApplicationKeysDTO> fromApplicationKeyListToDTOList(List<OAuthApplicationInfo> keysList) {
        List<ApplicationKeysDTO> applicationKeysDTOList = new ArrayList<>();
        for (OAuthApplicationInfo oAuthApplicationInfo : keysList) {
            applicationKeysDTOList.add(fromApplicationKeysToDTO(oAuthApplicationInfo));
        }
        return applicationKeysDTOList;
    }

    public static ApplicationTokenDTO fromApplicationTokenToDTO(ApplicationToken applicationToken){
        if (applicationToken == null) {
            return null;
        }
        ApplicationTokenDTO applicationTokenDTO = new ApplicationTokenDTO();
        applicationTokenDTO.setAccessToken(applicationToken.getAccessToken());
        applicationTokenDTO.setTokenScopes(applicationToken.getScopes());
        applicationTokenDTO.setValidityTime(applicationToken.getValidityPeriod());
        return applicationTokenDTO;
    }
}
