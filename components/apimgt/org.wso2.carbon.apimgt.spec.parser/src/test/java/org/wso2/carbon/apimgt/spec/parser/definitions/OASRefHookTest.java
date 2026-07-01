/*
 *   Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

public class OASRefHookTest {

    private OASParserOptions optsBlockingLoopback() {
        OASParserOptions o = new OASParserOptions();
        o.setRefValidationTenantDomain("carbon.super");
        o.setRefValidator((url, t) -> {
            if (url.contains("127.0.0.1")) {
                throw new APIManagementException("blocked " + url, ExceptionCodes.UNTRUSTED_URL);
            }
        });
        return o;
    }

    @Test
    public void testValidateAPIDefinitionRunsHook() {
        try {
            OASParserUtil.validateAPIDefinition("{\"$ref\":\"http://127.0.0.1/x.yaml\"}", false, optsBlockingLoopback());
            Assert.fail("expected UNTRUSTED_URL from Layer-1 hook");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.UNTRUSTED_URL.getErrorCode(), e.getErrorHandler().getErrorCode());
        }
    }

    @Test
    public void testNoHookNoLayer1() throws Exception {
        OASParserOptions o = new OASParserOptions(); // no validator => Layer-1 skipped
        OASParserUtil.validateAPIDefinition("openapi: 3.0.0\ninfo: {title: t, version: '1.0'}\npaths: {}\n", false, o);
    }
}
