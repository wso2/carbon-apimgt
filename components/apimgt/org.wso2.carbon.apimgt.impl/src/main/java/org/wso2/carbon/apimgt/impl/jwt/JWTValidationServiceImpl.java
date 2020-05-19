package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.CarbonContext;

import java.text.ParseException;

public class JWTValidationServiceImpl implements JWTValidationService {

    private static final Log log = LogFactory.getLog(JWTValidationServiceImpl.class);

    @Override
    public JWTValidationInfo validateJWTToken(SignedJWT signedJWT) throws APIManagementException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        try {
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            if (StringUtils.isNotEmpty(issuer)) {
                JWTValidator jwtValidator = KeyManagerHolder.getJWTValidator(tenantDomain, issuer);
                if (jwtValidator != null) {
                    return jwtValidator.validateToken(signedJWT);
                }
            }
            jwtValidationInfo.setValid(false);
            jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_GENERAL_ERROR);
            return jwtValidationInfo;
        } catch (ParseException e) {
            log.error("Error while parsing JWT Token", e);
            jwtValidationInfo.setValid(false);
            jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_GENERAL_ERROR);
            return jwtValidationInfo;
        }
    }

    @Override
    public String getKeyManagerNameIfJwtValidatorExist(SignedJWT signedJWT) throws APIManagementException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        try {
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            return KeyManagerHolder.getKeyManagerNameByIssuer(tenantDomain, issuer);
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing JWT", e);
        }
    }

}
