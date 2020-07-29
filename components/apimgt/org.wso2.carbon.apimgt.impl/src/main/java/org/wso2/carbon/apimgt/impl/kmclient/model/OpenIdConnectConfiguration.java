/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.apimgt.impl.kmclient.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Openid Connect Discovery Endpoint metadata.
 */
public class OpenIdConnectConfiguration {

    @SerializedName("issuer")
    private String issuer;
    @SerializedName("authorization_endpoint")
    private String authorizeEndpoint;
    @SerializedName("token_endpoint")
    private String tokenEndpoint;
    @SerializedName("userinfo_endpoint")
    private String userInfoEndpoint;
    @SerializedName("jwks_uri")
    private String jwksEndpoint;
    @SerializedName("registration_endpoint")
    private String registrationEndpoint;
    @SerializedName("scopes_supported")
    private List<String> scopesSupported = new ArrayList();
    @SerializedName("response_types_supported")
    private List<String> responseTypesSupported = new ArrayList<>();
    @SerializedName("response_modes_supported")
    private List<String> responseModesSupported = new ArrayList<>();
    @SerializedName("grant_types_supported")
    private List<String> grantTypesSupported = new ArrayList<>();
    @SerializedName("acr_values_supported")
    private List<String> acrValuesSupported = new ArrayList<>();
    @SerializedName("subject_types_supported")
    private List<String> subjectTypesSupported = new ArrayList<>();
    @SerializedName("id_token_signing_alg_values_supported")
    private List<String> idTokenSigningAlgSupported = new ArrayList<>();
    @SerializedName("id_token_encryption_alg_values_supported")
    private List<String> idTokenEncryptionAlgValuesSupported = new ArrayList<>();
    @SerializedName("id_token_encryption_enc_values_supported")
    private List<String> idTokenEncryptionValuesSupported = new ArrayList<>();
    @SerializedName("userinfo_signing_alg_values_supported")
    private List<String> userInfoSigningAlgValuesSupported = new ArrayList<>();
    @SerializedName("userinfo_encryption_alg_values_supported")
    private List<String> userInfoEncryptionAlgValuesSupported = new ArrayList<>();
    @SerializedName("userinfo_encryption_enc_values_supported")
    private List<String> userInfoEncryptionEncValuesSupported = new ArrayList<>();
    @SerializedName("request_object_signing_alg_values_supported")
    private List<String> requestObjectSigningAlgValuesSupported = new ArrayList<>();
    @SerializedName("request_object_encryption_alg_values_supported")
    private List<String> requestObjectEncryptionAlgValuesSupported = new ArrayList<>();
    @SerializedName("request_object_encryption_enc_values_supported")
    private List<String> requestObjectEncryptionEncValuesSupported = new ArrayList<>();
    @SerializedName("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported = new ArrayList<>();
    @SerializedName("token_endpoint_auth_signing_alg_values_supported")
    private List<String> tokenEndpointAuthSigningAlgSupported = new ArrayList<>();
    @SerializedName("display_values_supported")
    private List<String> displayValuesSupported = new ArrayList<>();
    @SerializedName("claim_types_supported")
    private List<String> claimTypesSupported = new ArrayList<>();
    @SerializedName("claims_supported")
    private List<String> claimsSupported = new ArrayList<>();
    @SerializedName("service_documentation")
    private String serviceDocumentation;
    @SerializedName("claims_locales_supported")
    private List<String> claimLocalesSupported = new ArrayList<>();
    @SerializedName("ui_locales_supported")
    private List<String> uiLocalesSupported = new ArrayList<>();
    @SerializedName("claims_parameter_supported")
    private boolean claimParametersSupported;
    @SerializedName("request_parameter_supported")
    private boolean requestParametersSupported;
    @SerializedName("request_uri_parameter_supported")
    private boolean requestURiParametersSupported ;
    @SerializedName("require_request_uri_registration")
    private boolean requireRequestURIRegistration;
    @SerializedName("op_policy_uri")
    private String opPolicyURI;
    @SerializedName("op_tos_uri")
    private String opTOCUri;
    @SerializedName("introspection_endpoint")
    private String introspectionEndpoint;
    @SerializedName("revocation_endpoint")
    private String revokeEndpoint;

    public String getIssuer() {

        return issuer;
    }

    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    public String getAuthorizeEndpoint() {

        return authorizeEndpoint;
    }

    public void setAuthorizeEndpoint(String authorizeEndpoint) {

        this.authorizeEndpoint = authorizeEndpoint;
    }

    public String getTokenEndpoint() {

        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {

        this.tokenEndpoint = tokenEndpoint;
    }

    public String getUserInfoEndpoint() {

        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {

        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String getJwksEndpoint() {

        return jwksEndpoint;
    }

    public void setJwksEndpoint(String jwksEndpoint) {

        this.jwksEndpoint = jwksEndpoint;
    }

    public String getRegistrationEndpoint() {

        return registrationEndpoint;
    }

    public void setRegistrationEndpoint(String registrationEndpoint) {

        this.registrationEndpoint = registrationEndpoint;
    }

    public List<String> getScopesSupported() {

        return scopesSupported;
    }

    public void setScopesSupported(List<String> scopesSupported) {

        this.scopesSupported = scopesSupported;
    }

    public List<String> getResponseTypesSupported() {

        return responseTypesSupported;
    }

    public void setResponseTypesSupported(List<String> responseTypesSupported) {

        this.responseTypesSupported = responseTypesSupported;
    }

    public List<String> getResponseModesSupported() {

        return responseModesSupported;
    }

    public void setResponseModesSupported(List<String> responseModesSupported) {

        this.responseModesSupported = responseModesSupported;
    }

    public List<String> getGrantTypesSupported() {

        return grantTypesSupported;
    }

    public void setGrantTypesSupported(List<String> grantTypesSupported) {

        this.grantTypesSupported = grantTypesSupported;
    }

    public List<String> getAcrValuesSupported() {

        return acrValuesSupported;
    }

    public void setAcrValuesSupported(List<String> acrValuesSupported) {

        this.acrValuesSupported = acrValuesSupported;
    }

    public List<String> getSubjectTypesSupported() {

        return subjectTypesSupported;
    }

    public void setSubjectTypesSupported(List<String> subjectTypesSupported) {

        this.subjectTypesSupported = subjectTypesSupported;
    }

    public List<String> getIdTokenSigningAlgSupported() {

        return idTokenSigningAlgSupported;
    }

    public void setIdTokenSigningAlgSupported(List<String> idTokenSigningAlgSupported) {

        this.idTokenSigningAlgSupported = idTokenSigningAlgSupported;
    }

    public List<String> getIdTokenEncryptionAlgValuesSupported() {

        return idTokenEncryptionAlgValuesSupported;
    }

    public void setIdTokenEncryptionAlgValuesSupported(List<String> idTokenEncryptionAlgValuesSupported) {

        this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
    }

    public List<String> getIdTokenEncryptionValuesSupported() {

        return idTokenEncryptionValuesSupported;
    }

    public void setIdTokenEncryptionValuesSupported(List<String> idTokenEncryptionValuesSupported) {

        this.idTokenEncryptionValuesSupported = idTokenEncryptionValuesSupported;
    }

    public List<String> getUserInfoSigningAlgValuesSupported() {

        return userInfoSigningAlgValuesSupported;
    }

    public void setUserInfoSigningAlgValuesSupported(List<String> userInfoSigningAlgValuesSupported) {

        this.userInfoSigningAlgValuesSupported = userInfoSigningAlgValuesSupported;
    }

    public List<String> getUserInfoEncryptionAlgValuesSupported() {

        return userInfoEncryptionAlgValuesSupported;
    }

    public void setUserInfoEncryptionAlgValuesSupported(List<String> userInfoEncryptionAlgValuesSupported) {

        this.userInfoEncryptionAlgValuesSupported = userInfoEncryptionAlgValuesSupported;
    }

    public List<String> getUserInfoEncryptionEncValuesSupported() {

        return userInfoEncryptionEncValuesSupported;
    }

    public void setUserInfoEncryptionEncValuesSupported(List<String> userInfoEncryptionEncValuesSupported) {

        this.userInfoEncryptionEncValuesSupported = userInfoEncryptionEncValuesSupported;
    }

    public List<String> getRequestObjectSigningAlgValuesSupported() {

        return requestObjectSigningAlgValuesSupported;
    }

    public void setRequestObjectSigningAlgValuesSupported(
            List<String> requestObjectSigningAlgValuesSupported) {

        this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
    }

    public List<String> getRequestObjectEncryptionAlgValuesSupported() {

        return requestObjectEncryptionAlgValuesSupported;
    }

    public void setRequestObjectEncryptionAlgValuesSupported(
            List<String> requestObjectEncryptionAlgValuesSupported) {

        this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
    }

    public List<String> getRequestObjectEncryptionEncValuesSupported() {

        return requestObjectEncryptionEncValuesSupported;
    }

    public void setRequestObjectEncryptionEncValuesSupported(
            List<String> requestObjectEncryptionEncValuesSupported) {

        this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
    }

    public List<String> getTokenEndpointAuthMethodsSupported() {

        return tokenEndpointAuthMethodsSupported;
    }

    public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {

        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
    }

    public List<String> getTokenEndpointAuthSigningAlgSupported() {

        return tokenEndpointAuthSigningAlgSupported;
    }

    public void setTokenEndpointAuthSigningAlgSupported(List<String> tokenEndpointAuthSigningAlgSupported) {

        this.tokenEndpointAuthSigningAlgSupported = tokenEndpointAuthSigningAlgSupported;
    }

    public List<String> getDisplayValuesSupported() {

        return displayValuesSupported;
    }

    public void setDisplayValuesSupported(List<String> displayValuesSupported) {

        this.displayValuesSupported = displayValuesSupported;
    }

    public List<String> getClaimTypesSupported() {

        return claimTypesSupported;
    }

    public void setClaimTypesSupported(List<String> claimTypesSupported) {

        this.claimTypesSupported = claimTypesSupported;
    }

    public List<String> getClaimsSupported() {

        return claimsSupported;
    }

    public void setClaimsSupported(List<String> claimsSupported) {

        this.claimsSupported = claimsSupported;
    }

    public String getServiceDocumentation() {

        return serviceDocumentation;
    }

    public void setServiceDocumentation(String serviceDocumentation) {

        this.serviceDocumentation = serviceDocumentation;
    }

    public List<String> getClaimLocalesSupported() {

        return claimLocalesSupported;
    }

    public void setClaimLocalesSupported(List<String> claimLocalesSupported) {

        this.claimLocalesSupported = claimLocalesSupported;
    }

    public List<String> getUiLocalesSupported() {

        return uiLocalesSupported;
    }

    public void setUiLocalesSupported(List<String> uiLocalesSupported) {

        this.uiLocalesSupported = uiLocalesSupported;
    }

    public boolean isClaimParametersSupported() {

        return claimParametersSupported;
    }

    public void setClaimParametersSupported(boolean claimParametersSupported) {

        this.claimParametersSupported = claimParametersSupported;
    }

    public boolean isRequestParametersSupported() {

        return requestParametersSupported;
    }

    public void setRequestParametersSupported(boolean requestParametersSupported) {

        this.requestParametersSupported = requestParametersSupported;
    }

    public boolean isRequestURiParametersSupported() {

        return requestURiParametersSupported;
    }

    public void setRequestURiParametersSupported(boolean requestURiParametersSupported) {

        this.requestURiParametersSupported = requestURiParametersSupported;
    }

    public boolean isRequireRequestURIRegistration() {

        return requireRequestURIRegistration;
    }

    public void setRequireRequestURIRegistration(boolean requireRequestURIRegistration) {

        this.requireRequestURIRegistration = requireRequestURIRegistration;
    }

    public String getOpPolicyURI() {

        return opPolicyURI;
    }

    public void setOpPolicyURI(String opPolicyURI) {

        this.opPolicyURI = opPolicyURI;
    }

    public String getOpTOCUri() {

        return opTOCUri;
    }

    public void setOpTOCUri(String opTOCUri) {

        this.opTOCUri = opTOCUri;
    }

    public String getIntrospectionEndpoint() {

        return introspectionEndpoint;
    }

    public void setIntrospectionEndpoint(String introspectionEndpoint) {

        this.introspectionEndpoint = introspectionEndpoint;
    }

    public String getRevokeEndpoint() {

        return revokeEndpoint;
    }

    public void setRevokeEndpoint(String revokeEndpoint) {

        this.revokeEndpoint = revokeEndpoint;
    }
}
