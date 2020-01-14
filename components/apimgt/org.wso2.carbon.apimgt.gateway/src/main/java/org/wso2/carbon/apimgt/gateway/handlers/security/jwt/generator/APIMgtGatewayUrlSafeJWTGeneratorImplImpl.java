package org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator;

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.api.APIManagementException;

public class APIMgtGatewayUrlSafeJWTGeneratorImplImpl extends APIMgtGatewayJWTGeneratorImpl {

    @Override
    public String encode(byte[] stringToBeEncoded) throws APIManagementException {
        return Base64.encodeBase64URLSafeString(stringToBeEncoded);

    }
}
