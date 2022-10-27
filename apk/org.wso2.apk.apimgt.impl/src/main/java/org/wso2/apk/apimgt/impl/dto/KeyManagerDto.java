package org.wso2.apk.apimgt.impl.dto;


import org.wso2.apk.apimgt.api.model.KeyManager;

public class KeyManagerDto {

    private String name;
    private String issuer;
    private KeyManager keyManager;

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
}
