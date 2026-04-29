/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link OIDCErrorConstants}.
 */
public class OIDCErrorConstantsTest {

    @Test
    public void testAudClaimValidationFailed() {

        OIDCErrorConstants.ErrorMessages error =
                OIDCErrorConstants.ErrorMessages.JWT_TOKEN_AUD_CLAIM_VALIDATION_FAILED;

        Assert.assertEquals("OID-60018", error.getCode());
        Assert.assertNotNull(error.getMessage());
        Assert.assertTrue(error.getMessage().contains("audience"));
    }

    @Test
    public void testIssClaimValidationFailed() {

        OIDCErrorConstants.ErrorMessages error =
                OIDCErrorConstants.ErrorMessages.JWT_TOKEN_ISS_CLAIM_VALIDATION_FAILED;

        Assert.assertEquals("OID-65016", error.getCode());
        Assert.assertNotNull(error.getMessage());
        Assert.assertTrue(error.getMessage().contains("iss"));
    }

    @Test
    public void testSignatureValidationFailed() {

        OIDCErrorConstants.ErrorMessages error =
                OIDCErrorConstants.ErrorMessages.JWT_TOKEN_SIGNATURE_VALIDATION_FAILED;

        Assert.assertEquals("OID-65017", error.getCode());
        Assert.assertNotNull(error.getMessage());
        Assert.assertTrue(error.getMessage().contains("signature"));
    }

    @Test
    public void testToStringFormat() {

        OIDCErrorConstants.ErrorMessages error =
                OIDCErrorConstants.ErrorMessages.JWT_TOKEN_SIGNATURE_VALIDATION_FAILED;

        String result = error.toString();
        Assert.assertTrue(result.contains(error.getCode()));
        Assert.assertTrue(result.contains(error.getMessage()));
    }
}
