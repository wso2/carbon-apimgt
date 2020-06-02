package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWTValidationInfo implements Serializable {

    private String user;
    private String issuer;
    private long expiryTime;
    private long issuedTime;
    private String consumerKey;
    private boolean valid;
    private List<String> scopes = new ArrayList<>();
    private Map<String,Object> claims = new HashMap<>();
    private String jti;
    private int validationCode;
    private String rawPayload;
    private String keyManager;
    public String getUser() {

        return user;
    }

    public void setUser(String user) {

        this.user = user;
    }

    public String getIssuer() {

        return issuer;
    }

    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    public long getExpiryTime() {

        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {

        this.expiryTime = expiryTime;
    }

    public long getIssuedTime() {

        return issuedTime;
    }

    public void setIssuedTime(long issuedTime) {

        this.issuedTime = issuedTime;
    }

    public boolean isValid() {

        return valid;
    }

    public void setValid(boolean valid) {

        this.valid = valid;
    }

    public List<String> getScopes() {

        return scopes;
    }

    public void setScopes(List<String> scopes) {

        this.scopes = scopes;
    }

    public Map<String, Object> getClaims() {

        return claims;
    }

    public void setClaims(Map<String, Object> claims) {

        this.claims = claims;
    }

    public String getJti() {

        return jti;
    }

    public void setJti(String jti) {

        this.jti = jti;
    }

    public String getConsumerKey() {

        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {

        this.consumerKey = consumerKey;
    }

    public int getValidationCode() {

        return validationCode;
    }

    public void setValidationCode(int validationCode) {

        this.validationCode = validationCode;
    }

    public String getRawPayload() {

        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {

        this.rawPayload = rawPayload;
    }

    public String getKeyManager() {

        return keyManager;
    }

    public void setKeyManager(String keyManager) {

        this.keyManager = keyManager;
    }
}
