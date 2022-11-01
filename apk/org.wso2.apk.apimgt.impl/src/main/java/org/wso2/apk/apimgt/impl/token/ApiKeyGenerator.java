/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.apk.apimgt.impl.token;


import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.impl.dto.JwtTokenInfoDTO;

/**
 * This interface encapsulates an apikey generator
 * Anyone trying to customize the apikey generation or signing can either implement the interface or extend the default
 * implementation given in org.wso2.carbon.apimgt.impl.token.DefaultApiKeyGenerator and mention the fully qualified class name
 * in deployment.toml
 * [apim.devportal]
 * api_key_generator_impl = <fully qualified class name>
 *
 */
public interface ApiKeyGenerator {
    /**
     * Generates and signs the apikey, which can be used for the authorization of APIs deployed at gateways
     * @param jwtTokenInfoDTO JWT Token Info DTO
     * @return the signed apikey as a string
     * @throws APIManagementException API Manager Exception
     */
    String generateToken(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException;
}
