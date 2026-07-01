/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.AsyncApiParserImplUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;

/**
 * Tests that the service-catalog import-by-URL path gates the user-supplied top-level definition
 * URL through {@link APIUtil#validateRemoteURL(String, String)} (the network access-control gate)
 * BEFORE the URL is fetched, for both the OpenAPI and AsyncAPI branches. Mirrors the publisher
 * import-by-URL gating.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, RestApiCommonUtil.class, OASParserUtil.class, AsyncApiParserUtil.class,
        CommonUtil.class, AsyncApiParserImplUtil.class})
@SuppressStaticInitializationFor("org.wso2.carbon.apimgt.impl.utils.APIUtil")
public class ServicesApiServiceImplTest {

    private static final String BLOCKED_URL = "http://169.254.169.254/latest/meta-data";
    private static final String TENANT_DOMAIN = "carbon.super";

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.mockStatic(OASParserUtil.class);
        PowerMockito.mockStatic(AsyncApiParserUtil.class);
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.mockStatic(AsyncApiParserImplUtil.class);

        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(TENANT_DOMAIN);
        PowerMockito.when(APIUtil.getHttpClient(anyInt(), anyString()))
                .thenReturn(Mockito.mock(HttpClient.class));
        PowerMockito.when(CommonUtil.getOasParserOptions()).thenReturn(new OASParserOptions());
        PowerMockito.when(APIUtil.buildRefAwareOASParserOptions(any(), anyString()))
                .thenReturn(new OASParserOptions());
    }

    /**
     * The disallowed URL must be rejected by the access-control gate (APIUtil.validateRemoteURL)
     * BEFORE the OAS definition is fetched from the URL. Asserts the by-URL fetch is never reached
     * and the resulting validation response is invalid (so the caller surfaces a 400).
     */
    @Test
    public void testValidateOpenAPIDefinitionByUrlBlockedBeforeFetch() throws Exception {
        PowerMockito.doThrow(new APIManagementException(
                "The URL " + BLOCKED_URL + " is not trusted", ExceptionCodes.UNTRUSTED_URL))
                .when(APIUtil.class);
        APIUtil.validateRemoteURL(eq(BLOCKED_URL), nullable(String.class));

        ServicesApiServiceImpl serviceImpl = new ServicesApiServiceImpl();
        APIDefinitionValidationResponse response =
                Whitebox.invokeMethod(serviceImpl, "validateOpenAPIDefinition", BLOCKED_URL, null);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.validateRemoteURL(eq(BLOCKED_URL), nullable(String.class));
        PowerMockito.verifyStatic(OASParserUtil.class, never());
        OASParserUtil.validateAPIDefinitionByURL(anyString(), any(), anyBoolean(), any(), anyString());
        assertFalse("Blocked URL must yield an invalid definition response", response.isValid());
    }

    /**
     * Same contract for the AsyncAPI by-URL branch: the access-control gate must reject the
     * disallowed URL before AsyncApiParserUtil.validateAsyncAPISpecificationByURL fetches it.
     */
    @Test
    public void testValidateAsyncAPISpecificationByUrlBlockedBeforeFetch() throws Exception {
        PowerMockito.when(AsyncApiParserImplUtil.getParserOptionsFromConfig()).thenReturn(null);
        PowerMockito.doThrow(new APIManagementException(
                "The URL " + BLOCKED_URL + " is not trusted", ExceptionCodes.UNTRUSTED_URL))
                .when(APIUtil.class);
        APIUtil.validateRemoteURL(eq(BLOCKED_URL), nullable(String.class));

        ServicesApiServiceImpl serviceImpl = new ServicesApiServiceImpl();
        APIDefinitionValidationResponse response =
                Whitebox.invokeMethod(serviceImpl, "validateAsyncAPISpecification", BLOCKED_URL, null);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.validateRemoteURL(eq(BLOCKED_URL), nullable(String.class));
        PowerMockito.verifyStatic(AsyncApiParserUtil.class, never());
        AsyncApiParserUtil.validateAsyncAPISpecificationByURL(anyString(), any(), anyBoolean(), any(), anyString());
        assertFalse("Blocked URL must yield an invalid definition response", response.isValid());
    }
}
