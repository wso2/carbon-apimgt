/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.token;

import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultApiKeyGenerator implements ApiKeyGenerator {

    private static final Log log = LogFactory.getLog(DefaultApiKeyGenerator.class);

    public DefaultApiKeyGenerator() {
    }

    public String generateToken(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException {

        String jwtHeader = buildHeader();
        String base64UrlEncodedHeader = "";
        if (jwtHeader != null) {
            base64UrlEncodedHeader = encode(jwtHeader.getBytes(Charset.defaultCharset()));
        }

        String jwtBody = buildBody(jwtTokenInfoDTO);
        String base64UrlEncodedBody = "";
        if (jwtBody != null) {
            base64UrlEncodedBody = encode(jwtBody.getBytes());
        }

        String assertion = base64UrlEncodedHeader + '.' + base64UrlEncodedBody;
        //get the assertion signed
        byte[] signedAssertion = buildSignature(assertion);
        if (log.isDebugEnabled()) {
            log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
        }
        String base64UrlEncodedAssertion = encode(signedAssertion);

        return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
    }

    protected String buildBody(JwtTokenInfoDTO jwtTokenInfoDTO) {
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        long expireIn;
        if (jwtTokenInfoDTO.getExpirationTime() == -1 ||
                jwtTokenInfoDTO.getExpirationTime() > (Integer.MAX_VALUE-currentTime)) {
            expireIn = -1;
        } else {
            expireIn = currentTime + jwtTokenInfoDTO.getExpirationTime();
        }
        String issuerIdentifier = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenIssuerIdentifier();
        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.END_USERNAME, APIUtil.getUserNameWithTenantSuffix(jwtTokenInfoDTO.getEndUserName()));
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.JWT_ID, UUID.randomUUID().toString());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.ISSUER_IDENTIFIER, issuerIdentifier);
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.ISSUED_TIME, currentTime);
        if (expireIn != -1) {
            jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.EXPIRY_TIME, expireIn);
        }
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS, jwtTokenInfoDTO.getSubscribedApiDTOList());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.TIER_INFO, jwtTokenInfoDTO.getSubscriptionPolicyDTOList());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.APPLICATION, jwtTokenInfoDTO.getApplication());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.KEY_TYPE, jwtTokenInfoDTO.getKeyType());
        jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.TOKEN_TYPE,
                APIConstants.JwtTokenConstants.API_KEY_TOKEN_TYPE);

        if (jwtTokenInfoDTO.getPermittedIP() != null) {
            jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.PERMITTED_IP, jwtTokenInfoDTO.getPermittedIP());
        }
        if (jwtTokenInfoDTO.getPermittedReferer() != null) {
            jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.PERMITTED_REFERER, jwtTokenInfoDTO.getPermittedReferer());
        }
        Map<String, Object> claimSet = jwtClaimsSetBuilder.build().toJSONObject();

        return new JSONObject(claimSet).toJSONString();
    }

    protected String buildHeader() throws APIManagementException {
        Certificate publicCert;
        JSONObject headerWithKid;
        try {
            KeyStoreManager tenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
            publicCert = tenantKSM.getDefaultPrimaryCertificate();
            headerWithKid = generateHeader(publicCert, APIConstants.SIGNATURE_ALGORITHM_SHA256_WITH_RSA);
            headerWithKid.put("kid", APIUtil.getApiKeyAlias());
        } catch (Exception e) {
            throw new APIManagementException("Error while building Api key header", e);
        }
        return headerWithKid.toString();
    }

    /**
     * Utility method to generate JWT header with public certificate thumbprint for signature verification.
     *
     * @param publicCert         The public certificate which needs to include in the header as thumbprint
     * @param signatureAlgorithm signature algorithm which needs to include in the header
     */
    private JSONObject generateHeader(Certificate publicCert, String signatureAlgorithm) throws APIManagementException {
        try {
            //generate the SHA-1 thumbprint of the certificate
            MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
            byte[] der = publicCert.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();
            String publicCertThumbprint = APIUtil.hexify(digestInBytes);
            String base64UrlEncodedThumbPrint;
            base64UrlEncodedThumbPrint = java.util.Base64.getUrlEncoder()
                    .encodeToString(publicCertThumbprint.getBytes(StandardCharsets.UTF_8));

            /*
             * Sample header
             * {"typ":"JWT", "alg":"SHA256withRSA", "x5t":"a_jhNus21KVuoFx65LmkW2O_l10",
             * "kid":"a_jhNus21KVuoFx65LmkW2O_l10_RS256"}
             * {"typ":"JWT", "alg":"[2]", "x5t":"[1]", "x5t":"[1]"}
             * */
            JSONObject jwtHeader = new JSONObject();
            jwtHeader.put("typ", "JWT");
            jwtHeader.put("alg", APIUtil.getJWSCompliantAlgorithmCode(signatureAlgorithm));
            jwtHeader.put("x5t", base64UrlEncodedThumbPrint);
            return jwtHeader;

        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            throw new APIManagementException("Error in generating public certificate thumbprint", e);
        }
    }

    protected byte[] buildSignature(String assertion) throws APIManagementException {
        PrivateKey privateKey = null;
        //get super tenant's key store manager
        KeyStoreManager tenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
        try {

            ServerConfigurationService config =  tenantKSM.getServerConfigService();
            String apiKeySignKeyStoreName = APIUtil.getApiKeySignKeyStoreName();
            String keyStorePassword = config.getFirstProperty(APIConstants.KeyStoreManagement
                    .SERVER_APIKEYSIGN_PRIVATE_KEY_PASSWORD.replaceFirst(APIConstants.KeyStoreManagement.KeyStoreName,
                            apiKeySignKeyStoreName));
            String apiKeySignAlias = config.getFirstProperty(APIConstants.KeyStoreManagement
                    .SERVER_APIKEYSIGN_KEYSTORE_KEY_ALIAS.replaceFirst(APIConstants.KeyStoreManagement.KeyStoreName,
                            apiKeySignKeyStoreName));
            KeyStore apiKeySignKeyStore = getApiKeySignKeyStore(tenantKSM);
            if (apiKeySignKeyStore != null) {
                privateKey = (PrivateKey) apiKeySignKeyStore.getKey(apiKeySignAlias,
                        keyStorePassword.toCharArray());
            }
        } catch (Exception e) {
            throw new APIManagementException("Error while signing Api Key", e);
        }
        return APIUtil.signJwt(assertion, privateKey, "SHA256withRSA");
    }

    private KeyStore getApiKeySignKeyStore(KeyStoreManager keyStoreManager) throws Exception {
        KeyStore apiKeySignKeyStore;
        ServerConfigurationService config = keyStoreManager.getServerConfigService();
        String apiKeySignKeyStoreName = APIUtil.getApiKeySignKeyStoreName();
        if (config.
                getFirstProperty(APIConstants.KeyStoreManagement.SERVER_APIKEYSIGN_KEYSTORE_FILE.
                        replaceFirst(APIConstants.KeyStoreManagement.KeyStoreName, apiKeySignKeyStoreName)) == null) {
            return null;
        }

        String file = new File(config
                .getFirstProperty(APIConstants.KeyStoreManagement.SERVER_APIKEYSIGN_KEYSTORE_FILE
                        .replaceFirst(APIConstants.KeyStoreManagement.KeyStoreName, apiKeySignKeyStoreName)))
                .getAbsolutePath();
        KeyStore store = KeyStore.getInstance(config
                .getFirstProperty(APIConstants.KeyStoreManagement.SERVER_APIKEYSIGN_KEYSTORE_TYPE
                        .replaceFirst(APIConstants.KeyStoreManagement.KeyStoreName, apiKeySignKeyStoreName)));
        String password = config
                .getFirstProperty(APIConstants.KeyStoreManagement.SERVER_APIKEYSIGN_KEYSTORE_PASSWORD
                        .replaceFirst(APIConstants.KeyStoreManagement.KeyStoreName, apiKeySignKeyStoreName));

        try (FileInputStream in = new FileInputStream(file)) {
            store.load(in, password.toCharArray());
            apiKeySignKeyStore = store;
        }
        return apiKeySignKeyStore;
    }

    private static String encode(byte[] stringToBeEncoded) throws APIManagementException {
        try {
            return java.util.Base64.getUrlEncoder().encodeToString(stringToBeEncoded);
        } catch (Exception e) {
            throw new APIManagementException("Error while encoding the Api Key ",e);
        }
    }
}
