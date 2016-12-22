package org.wso2.carbon.apimgt.gateway.models;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Details about an Access Token.
 */
public class AccessTokenInfo {

    private boolean isTokenValid;

    private boolean isApplicationToken;

    private String consumerKey;

    private String consumerSecret;

    private String[] scopes;

    private String tokenState;

    private String accessToken;

    private long issuedTime;

    private long validityPeriod;

    private int errorcode;

    private String endUserName;

    public String[] getScopes() {
        if (scopes != null) {
            return scopes.clone();
        } else {
            return new String[0];
        }
    }

    public int getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes.clone();
    }

    public String getTokenState() {
        return tokenState;
    }

    public void setTokenState(String tokenState) {
        this.tokenState = tokenState;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getIssuedTime() {
        return issuedTime;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    private HashMap<String, Object> parameters = new HashMap<String, Object>();

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * Get consumer secret corresponding to the access token
     *
     * @return consumer secret
     */
    public String getConsumerSecret() {
        return consumerSecret;
    }

    /**
     * Set consumer secret corresponding to the access token
     *
     * @param consumerSecret consumer secret to set
     */
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setIssuedTime(long issuedTime) {
        this.issuedTime = issuedTime;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public void addParameter(String paramName, Object paramValue) {
        parameters.put(paramName, paramValue);
    }

    public Object getParameter(String paramName) {
        return parameters.get(paramName);
    }

    public boolean isTokenValid() {
        return isTokenValid;
    }

    public void setTokenValid(boolean isTokenValid) {
        this.isTokenValid = isTokenValid;
    }

    public boolean isApplicationToken() {
        return isApplicationToken;
    }

    public void setApplicationToken(boolean isApplicationToken) {
        this.isApplicationToken = isApplicationToken;
    }

    /**
     * Sending additional properties as a JSON String.
     */
    public String getJSONString() {

        // TODO:Need to add other parameters into the param Map.
        if (!parameters.containsKey("scopes") && scopes != null) {
            parameters.put("scopes", Arrays.toString(scopes));
        }

        if (!parameters.containsKey("tokenState")) {
            parameters.put("tokenState", tokenState);
        }
        Gson gson = new Gson();
        return gson.toJson(parameters);

    }

    public String getEndUserName() {
        return endUserName;
    }

    public void setEndUserName(String endUserName) {
        this.endUserName = endUserName;
    }
}

