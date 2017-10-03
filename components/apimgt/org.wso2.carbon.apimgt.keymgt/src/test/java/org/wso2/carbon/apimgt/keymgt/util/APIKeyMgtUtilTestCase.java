/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.keymgt.util;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;

import java.util.Map;

public class APIKeyMgtUtilTestCase {

    @Test
    public void testConstructParameterMap() throws Exception {

        OAuth2TokenValidationRequestDTO.TokenValidationContextParam param1 = new OAuth2TokenValidationRequestDTO()
                .new TokenValidationContextParam();
        param1.setKey("Key1");
        param1.setValue("Value1");
        OAuth2TokenValidationRequestDTO.TokenValidationContextParam param2 = new OAuth2TokenValidationRequestDTO()
                .new TokenValidationContextParam();
        param2.setKey("Key2");
        param2.setValue("Value2");
        OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] params = {param1, param2};

        Map<String, String> paramMap = APIKeyMgtUtil.constructParameterMap(params);
        Assert.assertEquals(2, paramMap.size());
        Assert.assertEquals("Value1", paramMap.get("Key1"));
        Assert.assertEquals("Value2", paramMap.get("Key2"));
    }

    @Test
    public void testConstructParameterMapForNull() throws Exception {

        Map<String, String> paramMap = APIKeyMgtUtil.constructParameterMap(null);
        Assert.assertNull(paramMap);
    }


}