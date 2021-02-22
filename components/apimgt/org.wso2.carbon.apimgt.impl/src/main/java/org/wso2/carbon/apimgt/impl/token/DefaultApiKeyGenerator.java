
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

package org.wso2.carbon.apimgt.impl.token;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
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
import java.security.*;
import java.security.cert.Certificate;
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
                jwtTokenInfoDTO.getExpirationTime() > (Integer.MAX_VALUE - currentTime)) {
            expireIn = -1;
        } else {
            expireIn = currentTime + jwtTokenInfoDTO.getExpirationTime();
        }
        String issuerIdentifier = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenIssuerIdentifier();
        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.claim("sub", APIUtil.getUserNameWithTenantSuffix(jwtTokenInfoDTO.getEndUserName()));
        jwtClaimsSetBuilder.claim("jti", UUID.randomUUID().toString());
        jwtClaimsSetBuilder.claim("iss", issuerIdentifier);
        jwtClaimsSetBuilder.claim("iat", currentTime);
        if (expireIn != -1) {
            jwtClaimsSetBuilder.claim("exp", expireIn);
        }
        jwtClaimsSetBuilder.claim("subscribedAPIs", jwtTokenInfoDTO.getSubscribedApiDTOList());
        jwtClaimsSetBuilder.claim("tierInfo", jwtTokenInfoDTO.getSubscriptionPolicyDTOList());
        jwtClaimsSetBuilder.claim("application", jwtTokenInfoDTO.getApplication());
        jwtClaimsSetBuilder.claim("keytype", jwtTokenInfoDTO.getKeyType());

        if (jwtTokenInfoDTO.getPermittedIP() != null) {
            jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.PERMITTED_IP, jwtTokenInfoDTO.getPermittedIP());
        }
        if (jwtTokenInfoDTO.getPermittedReferer() != null) {
            jwtClaimsSetBuilder.claim(APIConstants.JwtTokenConstants.PERMITTED_REFERER, jwtTokenInfoDTO.getPermittedReferer());
        }

        return jwtClaimsSetBuilder.build().toJSONObject().toJSONString();
    }

    protected String buildHeader() throws APIManagementException {
        Certificate publicCert;
        JSONObject headerWithKid;
        try {
            KeyStoreManager tenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
            publicCert = tenantKSM.getDefaultPrimaryCertificate();
            String headerWithoutKid = APIUtil.generateHeader(publicCert, APIConstants.SIGNATURE_ALGORITHM_SHA256_WITH_RSA);
            headerWithKid = new JSONObject(headerWithoutKid);
            headerWithKid.put("kid", APIUtil.getApiKeyAlias());

        } catch (Exception e) {
            throw new APIManagementException("Error while building Api key header", e);
        }
        return headerWithKid.toString();
    }

    protected byte[] buildSignature(String assertion) throws APIManagementException {
        PrivateKey privateKey = null;
        //get super tenant's key store manager
        KeyStoreManager tenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
        try {

            ServerConfigurationService config = tenantKSM.getServerConfigService();
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
            throw new APIManagementException("Error while encoding the Api Key ", e);
        }
    }

}
