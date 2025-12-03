package org.wso2.carbon.apimgt.solace;

import org.junit.Test;
import org.wso2.carbon.apimgt.solace.parser.SolaceApiParser;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for SolaceApiParser class.
 */
public class SolaceApiParserTest {

    private final SolaceApiParser parser = new SolaceApiParser();

    @Test
    public void testGetVendorFromExtension_ReturnsSolace_WhenVendorIsSolaceLowercase() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"solace\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertEquals("Vendor must be recognized as Solace", SolaceConstants.SOLACE_ENVIRONMENT, vendor);
    }

    @Test
    public void testGetVendorFromExtension_ReturnsSolace_WhenVendorIsSolaceUppercase() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"SOLACE\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertEquals("Vendor matching should be case-insensitive", SolaceConstants.SOLACE_ENVIRONMENT, vendor);
    }

    @Test
    public void testGetVendorFromExtension_ReturnsNull_WhenVendorNotPresent() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertNull("When no x-origin extension exists, vendor should be null", vendor);
    }

    @Test
    public void testGetVendorFromExtension_ReturnsNull_WhenOriginHasDifferentVendor() throws Exception {
        String definition = "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"t\",\"version\":\"1.0.0\"}," +
                "\"x-origin\":{\"vendor\":\"other-vendor\"}}";
        String vendor = parser.getVendorFromExtensionWithError(definition);
        assertNull("When vendor is not Solace, method should return null", vendor);
    }

}
