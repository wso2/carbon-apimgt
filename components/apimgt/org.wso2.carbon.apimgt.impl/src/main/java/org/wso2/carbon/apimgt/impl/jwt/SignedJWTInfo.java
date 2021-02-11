/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.jwt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.clients.Util;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.Serializable;

/**
 * JWT internal Representation
 */
public class SignedJWTInfo implements Serializable {

    private String token;
    private SignedJWT signedJWT;
    private JWTClaimsSet jwtClaimsSet;
    private ValidationStatus validationStatus = ValidationStatus.NOT_VALIDATED;
    private String certificateThumbprint; //holder of key certificate bound access token
    private String encodedClientCertificate; //holder of key certificate cnf

    public enum ValidationStatus {
        NOT_VALIDATED, INVALID, VALID
    }

    public SignedJWTInfo(String token, SignedJWT signedJWT, JWTClaimsSet jwtClaimsSet) {

        this.token = token;
        this.signedJWT = signedJWT;
        this.jwtClaimsSet = jwtClaimsSet;
    }

    public SignedJWTInfo() {

    }

    public SignedJWT getSignedJWT() {

        return signedJWT;
    }

    public void setSignedJWT(SignedJWT signedJWT) {

        this.signedJWT = signedJWT;
    }

    public JWTClaimsSet getJwtClaimsSet() {

        return jwtClaimsSet;
    }

    public void setJwtClaimsSet(JWTClaimsSet jwtClaimsSet) {

        this.jwtClaimsSet = jwtClaimsSet;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public void setEncodedClientCertificate(String encodedClientCertificate) {

        this.encodedClientCertificate = encodedClientCertificate;
    }

    public String getCertificateThumbprint() {

        return certificateThumbprint;
    }

    public String getEncodedClientCertificate() {

        return encodedClientCertificate;
    }

    public void setCertificateThumbprint(String certificateThumbprint) {

        this.certificateThumbprint = certificateThumbprint;
    }

    public boolean isValidHoKToken() { //certificate bound access token

        if (isCertificateBoundAccessTokenEnabled()) {
            if (StringUtils.isNotEmpty(getEncodedClientCertificate()) && StringUtils.isNotEmpty(getCertificateThumbprint())) {
                JsonObject jsonElem = new JsonParser().parse(getCertificateThumbprint()).getAsJsonObject();
                if (null != jsonElem) {
                    return jsonElem.get(APIConstants.DIGEST) != null && jsonElem.get(APIConstants.DIGEST).toString().
                            equalsIgnoreCase(getEncodedClientCertificate());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isCertificateBoundAccessTokenEnabled() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            String firstProperty = config
                    .getFirstProperty(APIConstants.ENABLE_CERTIFICATE_BOUND_ACCESS_TOKEN);
            return Boolean.parseBoolean(firstProperty);
        }
        return false;
    }
}
