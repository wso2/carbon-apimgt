package org.wso2.carbon.apimgt.keymgt.token;

import junit.framework.TestCase;
import org.junit.Assert;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.APIMgtGatewayJWTGeneratorImpl;

public class AbstractJWTGeneratorTest extends TestCase {

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
