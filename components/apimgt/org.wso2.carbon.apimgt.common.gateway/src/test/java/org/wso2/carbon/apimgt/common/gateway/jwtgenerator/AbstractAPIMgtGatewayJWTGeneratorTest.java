/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.common.gateway.jwtgenerator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

public class AbstractAPIMgtGatewayJWTGeneratorTest {
    protected JWTConfigurationDto jwtConfigurationDto;

    @Before
    public void setup() {
        jwtConfigurationDto = new JWTConfigurationDto();
        jwtConfigurationDto.setEnableBase64Padding(false);
    }

    @Test
    public void testEncode() {
        // Test whether the encode method is base64 encoding.
        AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator = new APIMgtGatewayJWTGeneratorImpl();
        apiMgtGatewayJWTGenerator.setJWTConfigurationDto(jwtConfigurationDto);
        String stringToBeEncoded = "<<???>>";
        String expectedEncodedString = "PDw/Pz8+Pg";
        try {
            String actualEncodedString = apiMgtGatewayJWTGenerator.encode(stringToBeEncoded.getBytes());
            Assert.assertEquals(expectedEncodedString, actualEncodedString);
        } catch (JWTGeneratorException e) {
            Assert.fail("JWTGeneratorException thrown");
        }
    }
}
