/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.apk.apimgt.api.APIManagementException;

public class BackofficeAPIUtils {

    public static <T> String getJsonFromDTO(T dto) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error");
        }
    }

    public static <T> T getDTOFromJson(String json, Class<T> clazz)
            throws APIManagementException{
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error");
        }
    }

}
