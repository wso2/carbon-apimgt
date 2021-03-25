package org.wso2.carbon.apimgt.impl.token;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InternalAPIKeyGenerator implements ApiKeyGenerator {

    private static final Log log = LogFactory.getLog(DefaultApiKeyGenerator.class);

    public String generateToken(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException {

        JWSHeader jwtHeader = buildHeader();
        JWTClaimsSet jwtBody = buildBody(jwtTokenInfoDTO);

        SignedJWT signedJWT = new SignedJWT(jwtHeader,jwtBody);
        //get the assertion signed
        buildSignature(signedJWT);
        if (log.isDebugEnabled()) {
            log.debug("signed assertion value : " + signedJWT.getParsedString());
        }

        return signedJWT.serialize();
    }

    protected JWTClaimsSet buildBody(JwtTokenInfoDTO jwtTokenInfoDTO) {

        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        long expireIn;
        if (jwtTokenInfoDTO.getExpirationTime() == -1 ||
                jwtTokenInfoDTO.getExpirationTime() > (Integer.MAX_VALUE - currentTime)) {
            expireIn = -1;
        } else {
            expireIn = currentTime + jwtTokenInfoDTO.getExpirationTime();
        }
        String issuerIdentifier = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenIssuerIdentifier();
        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.END_USERNAME,
                APIUtil.getUserNameWithTenantSuffix(jwtTokenInfoDTO.getEndUserName()));
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.JWT_ID, UUID.randomUUID().toString());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.ISSUER_IDENTIFIER, issuerIdentifier);
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.ISSUED_TIME, currentTime);
        if (expireIn != -1) {
            jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.EXPIRY_TIME, expireIn);
        }
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS,
                jwtTokenInfoDTO.getSubscribedApiDTOList());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.KEY_TYPE, jwtTokenInfoDTO.getKeyType());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.TOKEN_TYPE,
                APIConstants.JwtTokenConstants.INTERNAL_KEY_TOKEN_TYPE);
        return jwtClaimsSetBuilder.build();
    }

    protected JWSHeader buildHeader() throws APIManagementException {

        return new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(APIUtil.getInternalApiKeyAlias()).build();
    }

    protected void buildSignature(SignedJWT assertion) throws APIManagementException {

        //get super tenant's key store manager
        KeyStoreManager tenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
        try {
            PrivateKey privateKey = tenantKSM.getDefaultPrivateKey();
            JWSSigner jwsSigner = new RSASSASigner(privateKey) ;
            assertion.sign(jwsSigner);
        } catch (Exception e) {
            throw new APIManagementException("Error while signing Api Key", e);
        }
    }

}
