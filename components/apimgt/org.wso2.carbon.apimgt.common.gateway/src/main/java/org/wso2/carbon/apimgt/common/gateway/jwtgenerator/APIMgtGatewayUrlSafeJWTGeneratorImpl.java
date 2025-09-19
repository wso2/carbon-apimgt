package org.wso2.carbon.apimgt.common.gateway.jwtgenerator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

/**
 * Implementation of url safe jwt generator impl.
 */
public class APIMgtGatewayUrlSafeJWTGeneratorImpl extends APIMgtGatewayJWTGeneratorImpl {
    private static final Log log = LogFactory.getLog(APIMgtGatewayUrlSafeJWTGeneratorImpl.class);

    @Override
    public String encode(byte[] stringToBeEncoded) throws JWTGeneratorException {
        if (log.isDebugEnabled()) {
            log.debug("Encoding JWT with URL-safe Base64 encoding");
        }
        return Base64.encodeBase64URLSafeString(stringToBeEncoded);

    }
}
