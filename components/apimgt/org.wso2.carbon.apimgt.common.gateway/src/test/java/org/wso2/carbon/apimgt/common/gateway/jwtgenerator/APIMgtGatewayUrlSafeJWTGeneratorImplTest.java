package org.wso2.carbon.apimgt.common.gateway.jwtgenerator;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

public class APIMgtGatewayUrlSafeJWTGeneratorImplTest extends TestCase {

    @Test
    public void testEncode() {
        // Test whether the encode method is base64url encoding.
        APIMgtGatewayJWTGeneratorImpl apiMgtGatewayJWTGenerator = new APIMgtGatewayJWTGeneratorImpl();
        String stringToBeEncoded = "<<???>>";
        String expectedEncodedString = "PDw_Pz8-PiA=";
        try {
            String actualEncodedString = apiMgtGatewayJWTGenerator.encode(stringToBeEncoded.getBytes());
            Assert.assertEquals(expectedEncodedString, actualEncodedString);
        } catch (JWTGeneratorException e) {
            Assert.fail("JWTGeneratorException thrown");
        }
    }
}
