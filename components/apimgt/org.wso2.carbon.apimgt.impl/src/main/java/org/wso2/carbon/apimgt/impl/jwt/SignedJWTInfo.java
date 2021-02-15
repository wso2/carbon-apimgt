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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.clients.Util;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;

import java.io.Serializable;
import java.text.ParseException;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

/**
 * JWT internal Representation
 */
public class SignedJWTInfo implements Serializable {

    private String token;
    private SignedJWT signedJWT;
    private JWTClaimsSet jwtClaimsSet;
    private ValidationStatus validationStatus = ValidationStatus.NOT_VALIDATED;
    private String certificateThumbprint; //holder of key certificate bound access token
    private X509Certificate X509ClientCertificate; //holder of key certificate cnf
    private String x509ClientCertificateHash; //holder of key certificate cnf
    private static final Log log = LogFactory.getLog(JWTValidator.class);

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

    public void setX509ClientCertificate(X509Certificate x509ClientCertificate) {

        X509ClientCertificate = x509ClientCertificate;
        if (x509ClientCertificate != null) {
            byte[] encoded = new byte[0];
            try {
                encoded = org.apache.commons.codec.binary.Base64.encodeBase64(x509ClientCertificate.getEncoded());
            } catch (CertificateEncodingException e) {
                log.error("Error while encoding client certificate. ");
            }
            x509ClientCertificateHash = GatewayUtils.hashString(encoded);
        }
    }


    public String getCertificateThumbprint() {

        String thumbprint = null;
        if (null != jwtClaimsSet) {
            try {
                thumbprint = jwtClaimsSet.getStringClaim(APIConstants.CNF);
                JSONObject thumbprintJson = new JSONObject(thumbprint);
                return thumbprintJson.getString(APIConstants.DIGEST);

            } catch (ParseException e) {
                log.error("Error while paring JWT claims. ");
            }
        }
        return null;
    }

    public String getX509ClientCertificateHash() {

        return x509ClientCertificateHash;
    }

    public boolean isValidCertificateBoundAccessToken() { //Holder of Key token
        if (X509ClientCertificate == null || StringUtils.isEmpty(getX509ClientCertificateHash()))
            return false;
        if (isCertificateBoundAccessTokenEnabled()) {
            if (getX509ClientCertificateHash().equals(getCertificateThumbprint())) {
                return true;
            }
            return false;
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
