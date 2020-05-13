package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;

public interface JWTValidator {

    JWTValidationInfo validateToken(SignedJWT jwtToken) throws APIManagementException;
    void loadTokenIssuerConfiguration(TokenIssuerDto tokenIssuerConfigurations);
 }
