/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.auth.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures common attributes used in an OAuth Application.
 */
public final class DCRClientInfo {

    @SerializedName("client_id")
    private String clientId;
    @SerializedName("registration_client_uri")
    private String registrationClientUri;
    @SerializedName("registration_access_token")
    private String registrationAccessToken;
    @SerializedName("client_id_issued_at")
    private String clientIdIssuedAt;
    @SerializedName("client_secret")
    private String clientSecret;
    @SerializedName("client_secret_expires_at")
    private String clientSecretExpiresAt;
    @SerializedName("client_name")
    private String clientName;
    @SerializedName("redirect_uris")
    private List<String> redirectURIs;
    @SerializedName("grant_types")
    private List<String> grantTypes;
    @SerializedName("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;
    @SerializedName("logo_uri")
    private String logoUri;
    @SerializedName("jwks_uri")
    private String jwksUri;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Set client Name of OAuthApplication.
     *
     * @param clientName Name of the application.
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Set grant types of OAuth Application.
     *
     * @param grantTypes grant types
     */
    public void setGrantTypes(List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public String getClientName() {
        return clientName;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public String getRegistrationClientUri() {
        return registrationClientUri;
    }

    public void setRegistrationClientUri(String registrationClientUri) {
        this.registrationClientUri = registrationClientUri;
    }

    public String getRegistrationAccessToken() {
        return registrationAccessToken;
    }

    public void setRegistrationAccessToken(String registrationAccessToken) {
        this.registrationAccessToken = registrationAccessToken;
    }

    public String getClientIdIssuedAt() {
        return clientIdIssuedAt;
    }

    public void setClientIdIssuedAt(String clientIdIssuedAt) {
        this.clientIdIssuedAt = clientIdIssuedAt;
    }

    public String getClientSecretExpiresAt() {
        return clientSecretExpiresAt;
    }

    public void setClientSecretExpiresAt(String clientSecretExpiresAt) {
        this.clientSecretExpiresAt = clientSecretExpiresAt;
    }

    public List<String> getRedirectURIs() {
        return redirectURIs;
    }

    public void setRedirectURIs(List<String> redirectURIs) {
        this.redirectURIs = redirectURIs;
    }

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public void addGrantType(String grantType) {
        if (grantType == null) {
            return;
        }
        if (grantTypes == null) {
            grantTypes = new ArrayList<>();
        }
        grantTypes.add(grantType);
    }

    public void addCallbackUrl(String callback) {
        if (callback == null) {
            return;
        }
        if (redirectURIs == null) {
            redirectURIs = new ArrayList<>();
        }
        redirectURIs.add(callback);
    }
}
