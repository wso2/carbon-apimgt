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
            fail("a blocked namespace URL must propagate UNTRUSTED_URL, not fetch");
        } catch (InvocationTargetException ite) {
            assertTrue(ite.getCause() instanceof APIManagementException);
            APIManagementException cause = (APIManagementException) ite.getCause();
            assertEquals("must carry UNTRUSTED_URL (900405)",
                    ExceptionCodes.UNTRUSTED_URL.getErrorCode(), cause.getErrorHandler().getErrorCode());
        }

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.validateRemoteURL("http://169.254.169.254/latest/meta-data/.xsd", "carbon.super");
    }
}
