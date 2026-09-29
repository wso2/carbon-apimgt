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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, PrivilegedCarbonContext.class})
public class WSDL11SOAPOperationExtractorAccessControlTest {

    @Test
    public void blockedNamespaceUrlIsValidatedAndPropagatesUntrustedUrl() throws Exception {
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext ctx = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(ctx);
        Mockito.when(ctx.getTenantDomain()).thenReturn("carbon.super");

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doThrow(new APIManagementException("URL is not trusted", ExceptionCodes.UNTRUSTED_URL))
                .when(APIUtil.class);
        APIUtil.validateRemoteURL("http://169.254.169.254/latest/meta-data/.xsd", "carbon.super");

        WSDL11SOAPOperationExtractor extractor = new WSDL11SOAPOperationExtractor();
        Method getBasedXSDofWSDL =
                WSDL11SOAPOperationExtractor.class.getDeclaredMethod("getBasedXSDofWSDL", String.class);
        getBasedXSDofWSDL.setAccessible(true);

        try {
            getBasedXSDofWSDL.invoke(extractor, "http://169.254.169.254/latest/meta-data/");
            fail("a blocked namespace URL must propagate UNTRUSTED_URL_IN_DEFINITION, not fetch");
        } catch (InvocationTargetException ite) {
            assertTrue(ite.getCause() instanceof APIManagementException);
            APIManagementException cause = (APIManagementException) ite.getCause();
            assertEquals("must carry UNTRUSTED_URL_IN_DEFINITION (900407)",
                    ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), cause.getErrorHandler().getErrorCode());
        }

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.validateRemoteURL("http://169.254.169.254/latest/meta-data/.xsd", "carbon.super");
    }
}
