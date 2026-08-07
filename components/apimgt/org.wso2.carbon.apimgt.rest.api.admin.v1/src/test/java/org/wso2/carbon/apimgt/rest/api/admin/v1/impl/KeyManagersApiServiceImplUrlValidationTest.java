/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Unit tests for {@link KeyManagersApiServiceImpl}'s Key Manager URL validation. Tests exercise the pre-check (which
 * skips non-URL values before any outbound validation) and the error mapping directly, so no outbound-validation
 * infrastructure is loaded.
 */
public class KeyManagersApiServiceImplUrlValidationTest {

    private final KeyManagersApiServiceImpl keyManagersApiService = new KeyManagersApiServiceImpl();

    private void validateKeyManagerURL(String url, String fieldName) throws Throwable {
        Method method = KeyManagersApiServiceImpl.class.getDeclaredMethod(
                "validateKeyManagerURL", String.class, String.class);
        method.setAccessible(true);
        try {
            method.invoke(keyManagersApiService, url, fieldName);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private APIManagementException toKeyManagerUrlError(APIManagementException e, String fieldName) throws Exception {
        Method method = KeyManagersApiServiceImpl.class.getDeclaredMethod(
                "toKeyManagerUrlError", APIManagementException.class, String.class);
        method.setAccessible(true);
        return (APIManagementException) method.invoke(keyManagersApiService, e, fieldName);
    }

    @Test
    public void testNonUrlAndBlankValuesAreSkipped() throws Throwable {
        // Non-absolute values (sentinels like "none", relative paths) and blanks must be skipped by the pre-check
        // before any outbound validation - they never reach validateRemoteURL. If the pre-check let them through they
        // would be rejected as malformed, breaking backward-compatible Key Manager configurations.
        validateKeyManagerURL("none", "token endpoint");
        validateKeyManagerURL("disabled", "revoke endpoint");
        validateKeyManagerURL("some/relative/path", "userinfo endpoint");
        validateKeyManagerURL("", "introspection endpoint");
        validateKeyManagerURL(null, "JWKS endpoint");
    }

    @Test
    public void testUntrustedErrorIsMappedToFieldSpecificMessage() throws Exception {
        APIManagementException blocked = new APIManagementException("Outbound request blocked",
                ExceptionCodes.UNTRUSTED_URL);

        APIManagementException mapped = toKeyManagerUrlError(blocked, "token endpoint");

        Assert.assertEquals("An untrusted URL must keep the UNTRUSTED_URL code",
                ExceptionCodes.UNTRUSTED_URL.getErrorCode(), mapped.getErrorHandler().getErrorCode());
        Assert.assertTrue("An untrusted URL must surface the field-specific 'not trusted' message",
                mapped.getMessage().contains("token endpoint") && mapped.getMessage().contains("URL is not trusted"));
    }

    @Test
    public void testMalformedErrorIsPropagatedUnchanged() throws Exception {
        // A non-untrusted 400 (e.g. MALFORMED_URL) must not be re-labelled as "not trusted".
        APIManagementException malformed = new APIManagementException("Malformed URL", ExceptionCodes.MALFORMED_URL);

        APIManagementException mapped = toKeyManagerUrlError(malformed, "token endpoint");

        Assert.assertSame("A non-untrusted error must be propagated unchanged", malformed, mapped);
        Assert.assertFalse("A malformed URL must not be re-labelled as 'not trusted'",
                mapped.getMessage().contains("URL is not trusted"));
    }
}
