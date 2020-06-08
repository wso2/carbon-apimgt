package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;

public class KeyManagerDto {

    private String name;
    private String issuer;
    private KeyManager keyManager;
    private JWTValidator jwtValidator;

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
}
