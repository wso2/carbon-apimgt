package org.wso2.carbon.apimgt.common.gateway.jwtgenerator;


import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

/**
 * Implementation of url safe jwt generator impl.
 */
public class APIMgtGatewayUrlSafeJWTGeneratorImpl extends APIMgtGatewayJWTGeneratorImpl {

    @Override
    public String encode(byte[] stringToBeEncoded) throws JWTGeneratorException {
        return Base64.encodeBase64URLSafeString(stringToBeEncoded);

    }
}
