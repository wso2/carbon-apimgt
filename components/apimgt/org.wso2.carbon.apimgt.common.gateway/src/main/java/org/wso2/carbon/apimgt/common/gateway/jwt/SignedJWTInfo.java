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

package org.wso2.carbon.apimgt.common.gateway.jwt;

import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.gateway.constants.APIConstants;
import org.wso2.carbon.apimgt.common.gateway.util.CertificateMgtUtils;

import java.security.cert.Certificate;

/**
 * JWT internal Representation
 */
public class SignedJWTInfo {

    private String token;
    private SignedJWT signedJWT;
    private JWTClaimsSet jwtClaimsSet;
    private ValidationStatus validationStatus = ValidationStatus.NOT_VALIDATED;
    private Certificate clientCertificate; //holder of key certificate cnf
    private String clientCertificateHash;
    private static final Log log = LogFactory.getLog(JWTValidator.class);

    /**
     * ValidationStatus represents the possible validation states available for the signedJWTInfo.
     */
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

    public void setClientCertificate(Certificate clientCertificate) {

        this.clientCertificate = clientCertificate;
        if (clientCertificate != null) {
            CertificateMgtUtils.convert(clientCertificate).ifPresent(x509Certificate ->
                    clientCertificateHash = X509CertUtils.computeSHA256Thumbprint(x509Certificate).toString());
        }
    }


    public String getCertificateThumbprint() {

        if (null != jwtClaimsSet) {
            Object thumbprint = jwtClaimsSet.getClaim(APIConstants.CNF);
            net.minidev.json.JSONObject thumbprintJson = (net.minidev.json.JSONObject) thumbprint;
            return thumbprintJson.getAsString(APIConstants.DIGEST);
        }
        return null;
    }

    public String getClientCertificateHash() {

        return clientCertificateHash;
    }

    public Certificate getClientCertificate() {

        return clientCertificate;
    }
}
