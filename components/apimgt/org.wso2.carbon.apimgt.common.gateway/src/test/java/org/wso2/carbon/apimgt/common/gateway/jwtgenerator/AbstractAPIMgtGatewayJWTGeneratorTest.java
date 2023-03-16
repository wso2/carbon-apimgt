package org.wso2.carbon.apimgt.common.gateway.jwtgenerator;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

public class AbstractAPIMgtGatewayJWTGeneratorTest extends TestCase {

    @Test
    public void testEncode() {
        // Test whether the encode method is base64 encoding.
        APIMgtGatewayJWTGeneratorImpl apiMgtGatewayJWTGenerator = new APIMgtGatewayJWTGeneratorImpl();
        String stringToBeEncoded = "<<???>>";
        String expectedEncodedString = "PDw/Pz8+PiA=";
        try {
            String actualEncodedString = apiMgtGatewayJWTGenerator.encode(stringToBeEncoded.getBytes());
            Assert.assertEquals(expectedEncodedString, actualEncodedString);
        } catch (JWTGeneratorException e) {
            Assert.fail("JWTGeneratorException thrown");
        }
    }
}
