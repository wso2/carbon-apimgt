/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.token;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class APIMJWTGenerator extends JWTGenerator {

    private static final Log log = LogFactory.getLog(APIMJWTGenerator.class);
    private static final String SHA256_WITH_RSA = "SHA256withRSA";
    private String signatureAlgorithm = SHA256_WITH_RSA;
    private static Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private String userAttributeSeparator = APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;
    private static final String NONE = "NONE";

    public String generateJWT(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException {

        String jwtHeader = buildHeader();

        String base64UrlEncodedHeader = "";
        if (jwtHeader != null) {
            base64UrlEncodedHeader = encoder.encodeToString(jwtHeader.getBytes(Charset.defaultCharset()));
        }

        String jwtBody = buildBody(jwtTokenInfoDTO);
        String base64UrlEncodedBody = "";
        if (jwtBody != null) {
            base64UrlEncodedBody = encoder.encodeToString(jwtBody.getBytes());
        }

        if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            String assertion = base64UrlEncodedHeader + '.' + base64UrlEncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
            }
            String base64UrlEncodedAssertion = encoder.encodeToString(signedAssertion);

            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.';
        }
    }

    public String buildBody(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException {

        Map<String, Object> standardClaims = populateStandardClaims(jwtTokenInfoDTO);

        //get tenantId
        int tenantId = APIUtil.getTenantId(jwtTokenInfoDTO.getEndUserName());

        String claimSeparator = getMultiAttributeSeparator(tenantId);
        if (StringUtils.isNotBlank(claimSeparator)) {
            userAttributeSeparator = claimSeparator;
        }

        if (standardClaims != null) {
            JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();

            Iterator<String> it = new TreeSet(standardClaims.keySet()).iterator();
            while (it.hasNext()) {
                String claimURI = it.next();

                Object claimValObj = standardClaims.get(claimURI);
                if (claimValObj instanceof String) {
                    String claimVal = (String) claimValObj;
                    List<String> claimList = new ArrayList<String>();
                    if (userAttributeSeparator != null && claimVal
                            .contains(userAttributeSeparator)) {
                        StringTokenizer st = new StringTokenizer(claimVal, userAttributeSeparator);
                        while (st.hasMoreElements()) {
                            String attValue = st.nextElement().toString();
                            if (StringUtils.isNotBlank(attValue)) {
                                claimList.add(attValue);
                            }
                        }
                        jwtClaimsSetBuilder.claim(claimURI, claimList.toArray(new String[claimList.size()]));
                    } else if (APIConstants.EXP.equals(claimURI)) {
                        jwtClaimsSetBuilder
                                .claim(APIConstants.EXP, new Date(Long.valueOf((String) standardClaims.get(claimURI))));
                    } else {
                        jwtClaimsSetBuilder.claim(claimURI, claimVal);
                    }
                } else {
                    if (claimValObj != null) {
                        jwtClaimsSetBuilder.claim(claimURI, claimValObj);
                    }
                }
            }

            return jwtClaimsSetBuilder.build().toJSONObject().toJSONString();
        }
        return null;
    }

    public Map<String, Object> populateStandardClaims(JwtTokenInfoDTO jwtTokenInfoDTO) throws APIManagementException {

        //generating expiring timestamp
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // jwtTokenInfoDTO.getExpirationTime() gives the token validity time given when the token is generated.
        long expireIn = currentTime + jwtTokenInfoDTO.getExpirationTime();

        String endUserName = jwtTokenInfoDTO.getEndUserName();

        Map<String, Object> claims = new LinkedHashMap<String, Object>(20);

        String issuerIdentifier = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenIssuerIdentifier();
        claims.put("sub", endUserName);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iss", issuerIdentifier);
        claims.put("aud", jwtTokenInfoDTO.getAudience());
        claims.put("iat", currentTime);
        claims.put("exp", expireIn);
        claims.put("scope", jwtTokenInfoDTO.getScopes());
        claims.put("subscribedAPIs", jwtTokenInfoDTO.getSubscribedApiDTOList());
        claims.put("tierInfo", jwtTokenInfoDTO.getSubscriptionPolicyDTOList());
        claims.put("application", jwtTokenInfoDTO.getApplication());
        claims.put("keytype", jwtTokenInfoDTO.getKeyType());
        claims.put("consumerKey", jwtTokenInfoDTO.getConsumerKey());

        return claims;
    }

    public byte[] signJWT(String assertion) throws APIManagementException {

        PrivateKey privateKey;
        KeyStoreManager superTenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
        try {
            privateKey = superTenantKSM.getDefaultPrivateKey();
        } catch (Exception e) {
            String msg = "Error while obtaining private key for super tenant";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return APIUtil.signJwt(assertion, privateKey, signatureAlgorithm);
    }

    public String buildHeader() throws APIManagementException {

        String jwtHeader = null;

        //if signature algo==NONE, header without cert
        if (NONE.equals(signatureAlgorithm)) {

            StringBuilder jwtHeaderBuilder = new StringBuilder();
            jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
            jwtHeaderBuilder.append("\"alg\":\"");
            jwtHeaderBuilder.append(APIUtil.getJWSCompliantAlgorithmCode(NONE));
            jwtHeaderBuilder.append('\"');
            jwtHeaderBuilder.append('}');

            jwtHeader = jwtHeaderBuilder.toString();

        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeader = addCertificateThumbPrintToHeader();
        }
        return jwtHeader;
    }

    /**
     * Helper method to add public certificate thumbprint to JWT_HEADER to signature verification.
     *
     * @throws APIManagementException
     */
    protected String addCertificateThumbPrintToHeader() throws APIManagementException {

        try {
            KeyStoreManager superTenantKSM = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
            Certificate publicCert = superTenantKSM.getDefaultPrimaryCertificate();
            return APIUtil.generateHeader(publicCert, signatureAlgorithm);
        } catch (Exception e) {
            String error = "Error obtaining keystore";
            throw new APIManagementException(error, e);
        }
    }
}
