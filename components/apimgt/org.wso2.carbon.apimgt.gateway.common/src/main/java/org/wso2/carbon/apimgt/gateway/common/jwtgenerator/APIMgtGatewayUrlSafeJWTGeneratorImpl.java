package org.wso2.carbon.apimgt.gateway.common.jwtgenerator;


import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.gateway.common.exception.JWTGeneratorException;

public class APIMgtGatewayUrlSafeJWTGeneratorImpl extends APIMgtGatewayJWTGeneratorImpl {

    @Override
    public String encode(byte[] stringToBeEncoded) throws JWTGeneratorException {
        return Base64.encodeBase64URLSafeString(stringToBeEncoded);

    }
}
