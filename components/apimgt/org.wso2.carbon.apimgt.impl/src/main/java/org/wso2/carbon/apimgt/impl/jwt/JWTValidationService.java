package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;

public interface JWTValidationService {

    public JWTValidationInfo validateJWTToken(SignedJWT signedJWT) throws APIManagementException;
    public String getKeyManagerNameIfJwtValidatorExist(SignedJWT signedJWT) throws APIManagementException;
}
