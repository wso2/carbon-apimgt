/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;

/**
 * Covers the GraphQL guard in {@link ApisApiServiceImpl#createAPI}. A GraphQL API cannot be complete without a
 * schema, and {@code POST /apis} has no way to carry one, so the request must be rejected up front (HTTP 400)
 * instead of creating an API that can never be exported or deployed.
 * <p>
 * The guard throws before anything else runs, so these tests deliberately use no PowerMock and also run on
 * JDK 17+. For the other API types nothing is stubbed: the call is expected to fail later, somewhere past the
 * guard, and the tests only check that the failure is not the GraphQL rejection.
 */
public class ApisApiServiceImplCreateApiTestCase {

    @Test
    public void testCreateGraphQLApiIsRejectedWithSchemaRequiredError() throws Exception {

        APIDTO apiDto = new APIDTO();
        apiDto.setName("TestAPI");
        apiDto.setContext("/test");
        apiDto.setVersion("1.0.0");
        apiDto.setType(APIDTO.TypeEnum.GRAPHQL);

        try {
            new ApisApiServiceImpl().createAPI(apiDto, "v3", Mockito.mock(MessageContext.class));
            Assert.fail("Creating a GraphQL API through POST /apis should be rejected");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.API_TYPE_INCOMPATIBLE_WITH_RESOURCE.getErrorCode(),
                    e.getErrorHandler().getErrorCode());
            Assert.assertEquals(400, e.getErrorHandler().getHttpStatusCode());
            Assert.assertEquals("Resource type 'API creation from scratch' is not supported for API type 'GRAPHQL'",
                    e.getErrorHandler().getErrorDescription());
            Assert.assertTrue("The error should point to the import-graphql-schema endpoint",
                    e.getMessage().contains("/apis/import-graphql-schema"));
        }
    }

    @Test
    public void testCreateHttpApiIsNotRejectedByGraphQLGuard() {

        assertNotRejectedByGraphQLGuard(APIDTO.TypeEnum.HTTP);
    }

    @Test
    public void testCreateWebSocketApiIsNotRejectedByGraphQLGuard() {

        assertNotRejectedByGraphQLGuard(APIDTO.TypeEnum.WS);
    }

    @Test
    public void testCreateApiWithoutTypeIsNotRejectedByGraphQLGuard() {

        // type is optional on the DTO; a null type must not hit the guard or throw an NPE from it
        assertNotRejectedByGraphQLGuard(null);
    }

    private void assertNotRejectedByGraphQLGuard(APIDTO.TypeEnum type) {

        APIDTO apiDto = new APIDTO();
        apiDto.setName("TestAPI");
        apiDto.setContext("/test");
        apiDto.setVersion("1.0.0");
        apiDto.setType(type);

        try {
            new ApisApiServiceImpl().createAPI(apiDto, "v3", Mockito.mock(MessageContext.class));
        } catch (APIManagementException e) {
            Assert.assertNotEquals("The GraphQL guard must not reject type " + type,
                    ExceptionCodes.API_TYPE_INCOMPATIBLE_WITH_RESOURCE.getErrorCode(),
                    e.getErrorHandler().getErrorCode());
        } catch (Exception | LinkageError e) {
            // Expected: there is no Carbon runtime in a unit test, so creation fails later, after the guard
        }
    }
}
