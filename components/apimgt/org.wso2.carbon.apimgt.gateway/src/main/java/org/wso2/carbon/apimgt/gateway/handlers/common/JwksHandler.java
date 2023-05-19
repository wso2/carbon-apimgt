/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.common;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.util.JWTUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for JWKS endpoint.
 */
public class JwksHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(JwksHandler.class);
    private static final String KEY_USE = "sig";
    private static final String KEYS = "keys";
    private final Map<String, Set<Certificate>> certificateMap = new HashMap<>();
    ExtendedJWTConfigurationDto jwtConfigurationDto;

    public boolean handleRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        try {
            String payload = getJwksEndpointResponse();
            JsonUtil.removeJsonPayload(axis2MsgContext);
            JsonUtil.getNewJsonPayload(axis2MsgContext, payload, true, true);
            axis2MsgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MsgContext.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MsgContext.removeProperty(APIConstants.NO_ENTITY_BODY);
        } catch (ParseException | AxisFault | APIManagementException e) {
            log.error("Error while generating payload " + axis2MsgContext.getLogIDString(), e);
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    /**
     * This method is used to get the response for the JWKS endpoint using the certificates in the keystore.
     *
     * @return JWKS response
     */
    public String getJwksEndpointResponse() throws ParseException, APIManagementException {
        this.jwtConfigurationDto = org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration().getJwtConfigurationDto();
        boolean isTenantBasedSigningEnabled = jwtConfigurationDto.isTenantBasedSigningEnabled();
        String certMapKey;
        if (isTenantBasedSigningEnabled) {
            certMapKey = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            certMapKey = APIConstants.SUPER_TENANT_DOMAIN;
        }

        Set<Certificate> certificateSet = null;
        if (certificateMap.containsKey(certMapKey)) {
            certificateSet = certificateMap.get(certMapKey);
        } else {
            synchronized (this) {
                if (!certificateMap.containsKey(certMapKey)) {
                    certificateSet = getCertificates(certMapKey);
                    certificateMap.put(certMapKey, certificateSet);
                }
            }
        }
        if (certificateSet != null) {
            return buildResponse(certificateSet);
        }
        return null;
    }

    /**
     * JWKS response is formed by considering the set of certificates provided
     *
     * @param certificates Set of certificates
     * @return JWKS response as a JSON string
     */
    private String buildResponse(Set<Certificate> certificates) throws ParseException, APIManagementException {

        JSONArray jwksArray = new JSONArray();
        JSONObject jwksJson = new JSONObject();
        JWSAlgorithm accessTokenSignAlgorithm = mapSignatureAlgorithmForJWSAlgorithm(
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getSignatureAlgorithm());
        List<JWSAlgorithm> diffAlgorithms = findDifferentAlgorithms(accessTokenSignAlgorithm);

        for (Certificate certificate : certificates) {
            for (JWSAlgorithm algorithm : diffAlgorithms) {
                RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
                RSAKey.Builder jwk = new RSAKey.Builder(publicKey);

                X509Certificate x509Certificate = (X509Certificate) certificate;
                jwk.keyID(JWTUtil.getKID(x509Certificate));
                jwk.algorithm(algorithm);
                jwk.keyUse(KeyUse.parse(KEY_USE));
                jwksArray.put(jwk.build().toJSONObject());
            }
        }

        jwksJson.put(KEYS, jwksArray);
        return jwksJson.toString();
    }

    /**
     * This method read identity.xml and find different signing algorithms
     *
     * @param accessTokenSignAlgorithm Access token signing algorithm
     * @return List of different signing algorithms
     */
    private List<JWSAlgorithm> findDifferentAlgorithms(
            JWSAlgorithm accessTokenSignAlgorithm) throws APIManagementException {

        List<JWSAlgorithm> diffAlgorithms = new ArrayList<>();
        diffAlgorithms.add(accessTokenSignAlgorithm);
        JWSAlgorithm idTokenSignAlgorithm = mapSignatureAlgorithmForJWSAlgorithm(
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getIdTokenSignatureAlgorithm());
        if (!accessTokenSignAlgorithm.equals(idTokenSignAlgorithm)) {
            diffAlgorithms.add(idTokenSignAlgorithm);
        }
        JWSAlgorithm userInfoSignAlgorithm = mapSignatureAlgorithmForJWSAlgorithm(
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getUserInfoJWTSignatureAlgorithm());
        if (!accessTokenSignAlgorithm.equals(userInfoSignAlgorithm)
                && !idTokenSignAlgorithm.equals(userInfoSignAlgorithm)) {
            diffAlgorithms.add(userInfoSignAlgorithm);
        }
        return diffAlgorithms;
    }

    /**
     * This method generates the key store file name from the Domain Name.
     *
     * @return key store file name
     */
    private String generateKSNameFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + APIConstants.KeyStoreManagement.KEY_STORE_EXTENSION_JKS);
    }

    /**
     * This method returns a set of certificates depending on the provided tenant domain.
     *
     * @param tenantDomain tenant domain which is used to get the relevant key store and extract certificates
     * @return set of certificates
     */
    private Set<Certificate> getCertificates(String tenantDomain) {
        Set<Certificate> certificates = new HashSet<>();
        KeyStore keyStore;
        try {
            if (!APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                // get tenant keyStore
                int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                keyStore = keyStoreManager.getKeyStore(generateKSNameFromDomainName(tenantDomain));
            } else {
                // get super tenant keyStore
                boolean tenantFlowStarted = false;
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                    tenantFlowStarted = true;
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(
                            APIUtil.getTenantIdFromTenantDomain(APIConstants.SUPER_TENANT_DOMAIN));
                    keyStore = keyStoreManager.getPrimaryKeyStore();
                } finally {
                    if (tenantFlowStarted) {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
            certificates.addAll(getCertificatesFromKeyStore(keyStore));
        } catch (Exception e) {
            log.error("Encountered an error while retrieving certificates", e);
        }
        return certificates;
    }

    /**
     * This method retrieves the certificates from the key store.
     *
     * @param keyStore Key store
     * @return Set of certificates from the key store
     */
    private Set<Certificate> getCertificatesFromKeyStore(KeyStore keyStore) throws KeyStoreException {
        Set<Certificate> certs = new HashSet<>();
        Enumeration<String> enumeration = keyStore.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                Certificate publicCert = keyStore.getCertificate(alias);
                certs.add(publicCert);
            }
        }
        return certs;
    }

    /**
     * Method to map signature algorithm for JWSAlgorithm
     */
    private static JWSAlgorithm mapSignatureAlgorithmForJWSAlgorithm(String signatureAlgorithm)
            throws APIManagementException {
        if ("NONE".equalsIgnoreCase(signatureAlgorithm)) {
            return new JWSAlgorithm(JWSAlgorithm.NONE.getName());
        } else if ("SHA256withRSA".equals(signatureAlgorithm)) {
            return JWSAlgorithm.RS256;
        } else if ("SHA384withRSA".equals(signatureAlgorithm)) {
            return JWSAlgorithm.RS384;
        } else if ("SHA512withRSA".equals(signatureAlgorithm)) {
            return JWSAlgorithm.RS512;
        } else if ("SHA256withHMAC".equals(signatureAlgorithm)) {
            return JWSAlgorithm.HS256;
        } else if ("SHA384withHMAC".equals(signatureAlgorithm)) {
            return JWSAlgorithm.HS384;
        } else if ("SHA512withHMAC".equals(signatureAlgorithm)) {
            return JWSAlgorithm.HS512;
        } else if ("SHA256withEC".equals(signatureAlgorithm)) {
            return JWSAlgorithm.ES256;
        } else if ("SHA384withEC".equals(signatureAlgorithm)) {
            return JWSAlgorithm.ES384;
        } else if ("SHA512withEC".equals(signatureAlgorithm)) {
            return JWSAlgorithm.ES512;
        } else if (!"SHA256withPS".equals(signatureAlgorithm) && !"PS256".equals(signatureAlgorithm)) {
            log.error("Unsupported Signature Algorithm in identity.xml");
            throw new APIManagementException("Unsupported Signature Algorithm in identity.xml");
        } else {
            return JWSAlgorithm.PS256;
        }
    }
}
