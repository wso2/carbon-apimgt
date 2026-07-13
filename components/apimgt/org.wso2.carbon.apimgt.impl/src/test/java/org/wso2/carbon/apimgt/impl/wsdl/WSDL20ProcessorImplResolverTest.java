/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.wsdl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, PrivilegedCarbonContext.class})
public class WSDL20ProcessorImplResolverTest {

    private static final String WSDL20_WITH_BLOCKED_IMPORT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<description xmlns=\"http://www.w3.org/ns/wsdl\""
          + "  xmlns:tns=\"http://example.org/svc\""
          + "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
          + "  targetNamespace=\"http://example.org/svc\">"
          + "  <import namespace=\"http://example.org/imported\""
          + "          location=\"http://169.254.169.254/meta/import.wsdl\"/>"
          + "  <types><xs:schema targetNamespace=\"http://example.org/svc\">"
          + "      <xs:element name=\"Ping\" type=\"xs:string\"/></xs:schema></types>"
          + "  <interface name=\"I\"/>"
          + "  <service name=\"S\" interface=\"tns:I\"/>"
          + "</description>";

    @Test
    public void blockedNestedImportSurfacesUntrustedUrlError() throws Exception {
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext ctx = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(ctx);
        Mockito.when(ctx.getTenantDomain()).thenReturn("carbon.super");

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doThrow(new APIManagementException("URL is not trusted", ExceptionCodes.UNTRUSTED_URL))
                .when(APIUtil.class);
        APIUtil.validateRemoteURL(Mockito.anyString(), Mockito.anyString());

        WSDL20ProcessorImpl processor = new WSDL20ProcessorImpl();
        processor.init(WSDL20_WITH_BLOCKED_IMPORT.getBytes(StandardCharsets.UTF_8));

        // blocked import redirected to a local stub (no outbound fetch) AND reported to the user
        assertTrue("a blocked nested import must be reported as an error", processor.hasError());
        assertEquals("must report UNTRUSTED_URL (900405)",
                ExceptionCodes.UNTRUSTED_URL.getErrorCode(), processor.getError().getErrorCode());

        // the policy gate was consulted for the nested import URL
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.validateRemoteURL("http://169.254.169.254/meta/import.wsdl", "carbon.super");
    }
}
