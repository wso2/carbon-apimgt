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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors;

import org.apache.cxf.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtBadRequestException;
import org.wso2.carbon.apimgt.api.OrganizationResolver;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;

import java.util.HashMap;

/**
 * Regression coverage for the E04-012 fix: {@link OrganizationInterceptor} must map an
 * {@link APIMgtAuthorizationFailedException} raised by the organization resolver to an HTTP 403,
 * the same way it already maps {@link APIMgtBadRequestException} to an HTTP 400.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class OrganizationInterceptorTest {

    private Message mockMessage() {
        Message message = Mockito.mock(Message.class);
        // Default for every other key read by handleMessage (in particular
        // RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, so the platform-gateway-api-key
        // early-return is not taken). Declared first so the specific stubs below take precedence.
        Mockito.when(message.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(new HashMap<String, Object>());
        Mockito.when(message.get(Message.QUERY_STRING)).thenReturn(null);
        return message;
    }

    @Test(expected = ForbiddenException.class)
    public void testAuthorizationFailureIsMappedToForbidden() throws Exception {
        OrganizationResolver resolver = Mockito.mock(OrganizationResolver.class);
        Mockito.when(resolver.resolve(Mockito.anyMap()))
                .thenThrow(new APIMgtAuthorizationFailedException("caller not permitted on this tenant"));

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getOrganizationResolver()).thenReturn(resolver);

        new OrganizationInterceptor().handleMessage(mockMessage());
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestIsStillHandledUnchanged() throws Exception {
        OrganizationResolver resolver = Mockito.mock(OrganizationResolver.class);
        Mockito.when(resolver.resolve(Mockito.anyMap()))
                .thenThrow(new APIMgtBadRequestException("invalid tenant"));

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getOrganizationResolver()).thenReturn(resolver);

        new OrganizationInterceptor().handleMessage(mockMessage());
    }
}
