package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.common.gateway.jwt.JWTValidator;

public class KeyManagerDto {

    private String name;
    private String issuer;
    private KeyManager keyManager;
    private JWTValidator jwtValidator;

    private org.wso2.carbon.apimgt.impl.jwt.JWTValidator deprecatedJWTValidator;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIssuer() {

        return issuer;
    }

    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    public KeyManager getKeyManager() {

        return keyManager;
    }

    public void setKeyManager(KeyManager keyManager) {

        this.keyManager = keyManager;
    }

    public JWTValidator getJwtValidator() {

        return jwtValidator;
    }

    public void setJwtValidator(JWTValidator jwtValidator) {

        this.jwtValidator = jwtValidator;
    }

    public org.wso2.carbon.apimgt.impl.jwt.JWTValidator getDeprecatedJWTValidator() {
        return deprecatedJWTValidator;
    }

    public void setDeprecatedJWTValidator(org.wso2.carbon.apimgt.impl.jwt.JWTValidator deprecatedJWTValidator) {
        this.deprecatedJWTValidator = deprecatedJWTValidator;
    }
}
