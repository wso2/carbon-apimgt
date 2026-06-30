/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import java.util.concurrent.atomic.AtomicReference;

public class OASParserOptionsTest {

    @Test
    public void testDefaults() {
        OASParserOptions o = new OASParserOptions();
        Assert.assertTrue(o.isExplicitStyleAndExplode());
        Assert.assertNull(o.getYamlCodePointLimit());
        Assert.assertNull(o.getRefValidationTenantDomain());
        Assert.assertNull(o.getRefValidator());
    }

    @Test
    public void testCopyConstructorCopiesAllFields() {
        OASParserOptions base = new OASParserOptions();
        base.setExplicitStyleAndExplode("false");
        base.setYamlCodePointLimit("5");
        base.setRefValidationTenantDomain("carbon.super");
        base.setRefValidator((url, t) -> { });

        OASParserOptions copy = new OASParserOptions(base);
        Assert.assertFalse(copy.isExplicitStyleAndExplode());
        Assert.assertEquals(base.getYamlCodePointLimit(), copy.getYamlCodePointLimit());
        Assert.assertEquals("carbon.super", copy.getRefValidationTenantDomain());
        Assert.assertNotNull(copy.getRefValidator());
        Assert.assertSame(base.getRefValidator(), copy.getRefValidator());
    }

    @Test
    public void testCopyConstructorNullSafe() {
        OASParserOptions copy = new OASParserOptions(null);
        Assert.assertTrue(copy.isExplicitStyleAndExplode());
        Assert.assertNull(copy.getYamlCodePointLimit());
        Assert.assertNull(copy.getRefValidationTenantDomain());
        Assert.assertNull(copy.getRefValidator());
    }

    @Test
    public void httpClientProviderIsCarriedAndCopied() {
        OASParserOptions opts = new OASParserOptions();
        OASParserOptions.HttpClientProvider provider = (protocol, port) -> null;
        opts.setHttpClientProvider(provider);
        Assert.assertSame(provider, opts.getHttpClientProvider());

        OASParserOptions copy = new OASParserOptions(opts);
        Assert.assertSame(provider, copy.getHttpClientProvider());
    }

    @Test
    public void testRefValidatorPropagatesCheckedException() {
        OASParserOptions o = new OASParserOptions();
        o.setRefValidator((url, t) -> {
            throw new APIManagementException("blocked " + url, ExceptionCodes.UNTRUSTED_URL);
        });
        AtomicReference<APIManagementException> caught = new AtomicReference<>();
        try {
            o.getRefValidator().validate("http://127.0.0.1/x", "carbon.super");
        } catch (APIManagementException e) {
            caught.set(e);
        }
        Assert.assertNotNull(caught.get());
        Assert.assertEquals(ExceptionCodes.UNTRUSTED_URL.getErrorCode(), caught.get().getErrorHandler().getErrorCode());
    }
}
