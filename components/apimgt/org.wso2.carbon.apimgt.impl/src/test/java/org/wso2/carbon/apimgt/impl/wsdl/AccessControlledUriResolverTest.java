package org.wso2.carbon.apimgt.impl.wsdl;

import org.apache.woden.resolver.URIResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class AccessControlledUriResolverTest {

    /** delegate that never maps anything (simulates a URI not in Woden's catalog). */
    private URIResolver passThroughDelegate() {
        return uri -> null;
    }

    @Test
    public void blockedRemoteUriIsRedirectedToLocalStub() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doThrow(new APIManagementException("URL is not trusted", ExceptionCodes.UNTRUSTED_URL))
                .when(APIUtil.class);
        APIUtil.validateRemoteURL(Mockito.anyString(), Mockito.anyString());

        AccessControlledUriResolver resolver =
                new AccessControlledUriResolver(passThroughDelegate(), "carbon.super");

        URI out = resolver.resolveURI(URI.create("http://169.254.169.254/latest/meta.xsd"));

        // Blocked reference must resolve to a non-remote stub or Woden would fetch the raw URL; scheme is
        // file:/jar: depending on packaging, so assert "not remote" rather than a specific scheme.
        assertNotNull("blocked reference must resolve to a non-null stub", out);
        assertFalse("blocked reference must NOT resolve to a remote URL",
                "http".equalsIgnoreCase(out.getScheme()) || "https".equalsIgnoreCase(out.getScheme()));
        assertNotEquals("169.254.169.254", out.getHost());
        assertTrue("blocked URL must be recorded for user feedback",
                resolver.getBlockedReferences().contains("http://169.254.169.254/latest/meta.xsd"));
    }

    @Test
    public void allowedRemoteUriPassesThroughAfterValidation() throws Exception {
        PowerMockito.mockStatic(APIUtil.class); // validateRemoteURL is a no-op (allowed)

        AccessControlledUriResolver resolver =
                new AccessControlledUriResolver(passThroughDelegate(), "carbon.super");

        // delegate returns null -> resolver returns null -> Woden opens the original (allowed) URI
        URI out = resolver.resolveURI(URI.create("http://api.github.com/schema.xsd"));
        assertNull(out);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.validateRemoteURL("http://api.github.com/schema.xsd", "carbon.super");
    }

    @Test
    public void localCatalogUriIsReturnedWithoutPolicyCheck() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        URI local = URI.create("jar:file:/woden.jar!/org/apache/woden/resolver/XMLSchema.xsd");
        URIResolver catalogDelegate = uri -> local; // simulate a catalog hit

        AccessControlledUriResolver resolver =
                new AccessControlledUriResolver(catalogDelegate, "carbon.super");

        URI out = resolver.resolveURI(URI.create("http://www.w3.org/2001/XMLSchema.xsd"));
        assertEquals(local, out);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.never());
        APIUtil.validateRemoteURL(Mockito.anyString(), Mockito.anyString());
    }
}
