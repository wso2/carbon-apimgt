/*
*Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.util.JWTUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SigningUtil;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;

/**
 * This class represents the JSON Web Token generator.
 * By default the following properties are encoded to each authenticated API request:
 * subscriber, applicationName, apiContext, version, tier, and endUserName
 * Additional properties can be encoded by engaging the ClaimsRetrieverImplClass callback-handler.
 * The JWT header and body are base64 encoded separately and concatenated with a dot.
 * Finally the token is signed using SHA256 with RSA algorithm.
 */
public abstract class AbstractJWTGenerator implements TokenGenerator {

    private static final Log log = LogFactory.getLog(AbstractJWTGenerator.class);

    public static final String API_GATEWAY_ID = "wso2.org/products/am";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private String userAttributeSeparator = APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;
    private boolean tenantBasedSigningEnabled;
    private boolean useKid;

    public AbstractJWTGenerator() {

        ExtendedJWTConfigurationDto jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getJwtConfigurationDto();

        dialectURI = jwtConfigurationDto.getConsumerDialectUri();
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        signatureAlgorithm = jwtConfigurationDto.getSignatureAlgorithm();
        if (signatureAlgorithm == null || !(NONE.equals(signatureAlgorithm)
                                            || SHA256_WITH_RSA.equals(signatureAlgorithm))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }

        String claimsRetrieverImplClass = jwtConfigurationDto.getClaimRetrieverImplClass();

        if (claimsRetrieverImplClass != null) {
            try {
                claimsRetriever = (ClaimsRetriever) APIUtil.getClassInstance(claimsRetrieverImplClass);
                claimsRetriever.init();
            } catch (ClassNotFoundException e) {
                log.error("Cannot find class: " + claimsRetrieverImplClass, e);
            } catch (InstantiationException e) {
                log.error("Error instantiating " + claimsRetrieverImplClass, e);
            } catch (IllegalAccessException e) {
                log.error("Illegal access to " + claimsRetrieverImplClass, e);
            } catch (APIManagementException e) {
                log.error("Error while initializing " + claimsRetrieverImplClass, e);
            }
        }
        tenantBasedSigningEnabled = jwtConfigurationDto.isTenantBasedSigningEnabled();
        useKid = jwtConfigurationDto.useKid();
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public ClaimsRetriever getClaimsRetriever() {
        return claimsRetriever;
    }

    public abstract Map<String, String> populateStandardClaims(TokenValidationContext validationContext)
            throws APIManagementException;

    public abstract Map<String, String> populateCustomClaims(TokenValidationContext validationContext) throws APIManagementException;

    public String encode(byte[] stringToBeEncoded) throws APIManagementException {
        return java.util.Base64.getEncoder().withoutPadding().encodeToString(stringToBeEncoded);
    }

    public String generateToken(TokenValidationContext validationContext) throws APIManagementException{

        String jwtHeader = buildHeader(validationContext.getTenantDomain());

        String base64UrlEncodedHeader = "";
        if (jwtHeader != null) {
            base64UrlEncodedHeader = encode(jwtHeader.getBytes(Charset.defaultCharset()));
        }

        String jwtBody = buildBody(validationContext);
        String base64UrlEncodedBody = "";
        if (jwtBody != null) {
            base64UrlEncodedBody = encode(jwtBody.getBytes());
        }

        if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            String assertion = base64UrlEncodedHeader + '.' + base64UrlEncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion, validationContext.getTenantDomain());

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
            }
            String base64UrlEncodedAssertion = encode(signedAssertion);

            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.';
        }
    }

    @Deprecated
    public String buildHeader() throws APIManagementException {

        return buildHeader(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    public String buildHeader(String tenantDomain) throws APIManagementException {
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
            jwtHeader = addCertToHeader(tenantDomain);
        }
        return jwtHeader;
    }

    public String buildBody(TokenValidationContext validationContext) throws APIManagementException {

        Map<String, String> standardClaims = populateStandardClaims(validationContext);
        Map<String, String> customClaims = populateCustomClaims(validationContext);

        //get tenantId
        int tenantId = APIUtil.getTenantId(validationContext.getTenantDomain());

        String claimSeparator = getMultiAttributeSeparator(tenantId);
        if (StringUtils.isNotBlank(claimSeparator)) {
            userAttributeSeparator = claimSeparator;
        }

        if (standardClaims != null) {
            if (customClaims != null) {
                for (Map.Entry<String, String> entry : customClaims.entrySet()) {
                    if (standardClaims.containsKey(entry.getKey())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skip already existing claim '" + entry.getKey() + "'");
                        }
                    } else {
                        standardClaims.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();

            if (standardClaims != null) {
                Iterator<String> it = new TreeSet(standardClaims.keySet()).iterator();
                while (it.hasNext()) {
                    String claimURI = it.next();
                    String claimVal = standardClaims.get(claimURI);
                    List<String> claimList = new ArrayList<String>();
                    if (claimVal != null && ((claimVal.startsWith("[") && claimVal.endsWith("]"))
                            || claimVal.contains("{"))) {
                        JSONParser jsonParser = new JSONParser(JSONParser.ACCEPT_SIMPLE_QUOTE);
                        try {
                            Object jsonObj = jsonParser.parse(claimVal);
                            jwtClaimsSetBuilder.claim(claimURI, jsonObj);
                        } catch (ParseException e) {
                            // Exception isn't thrown in order to generate jwt without claim, even if an error is
                            // occurred during the retrieving claims.
                            log.error(String.format("Error while reading claim values for %s", claimVal), e);
                        }
                    } else if (userAttributeSeparator != null && claimVal != null &&
                            claimVal.contains(userAttributeSeparator)) {
                        StringTokenizer st = new StringTokenizer(claimVal, userAttributeSeparator);
                        while (st.hasMoreElements()) {
                            String attValue = st.nextElement().toString();
                            if (StringUtils.isNotBlank(attValue)) {
                                claimList.add(attValue);
                            }
                        }
                        jwtClaimsSetBuilder.claim(claimURI, claimList);
                    } else if ("exp".equals(claimURI)) {
                        jwtClaimsSetBuilder.expirationTime(new Date(Long.valueOf(standardClaims.get(claimURI))));
                    } else {
                        jwtClaimsSetBuilder.claim(claimURI, claimVal);
                    }
                }
                //Adding JTI standard claim
                jwtClaimsSetBuilder.jwtID(UUID.randomUUID().toString());
            }
            return jwtClaimsSetBuilder.build().toJSONObject().toJSONString();
        }
        return null;
    }

    public byte[] signJWT(String assertion, String tenantDomain) throws APIManagementException {

        try {
            PrivateKey privateKey;
            if (tenantBasedSigningEnabled) {
                privateKey = SigningUtil.getSigningKey(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
            } else {
                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
                privateKey = keyStoreManager.getDefaultPrivateKey();
            }
            return APIUtil.signJwt(assertion, privateKey, signatureAlgorithm);
        } catch (Exception e) {
            throw new APIManagementException(e);
        }
    }

    protected long getTTL() {
        if (ttl != -1) {
            return ttl;
        }

        synchronized (JWTGenerator.class) {
            if (ttl != -1) {
                return ttl;
            }
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            String gwTokenCacheConfig = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            boolean isGWTokenCacheEnabled = Boolean.parseBoolean(gwTokenCacheConfig);

            String kmTokenCacheConfig = config.getFirstProperty(APIConstants.KEY_MANAGER_TOKEN_CACHE);
            boolean isKMTokenCacheEnabled = Boolean.parseBoolean(kmTokenCacheConfig);

            if (isGWTokenCacheEnabled || isKMTokenCacheEnabled) {
                String apimKeyCacheExpiry = config.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);

                if (apimKeyCacheExpiry != null) {
                    //added one minute buffer to the expiry time to avoid inconsistencies happen during cache expiry
                    //task time
                    ttl = Long.parseLong(apimKeyCacheExpiry) + Long.valueOf(60);
                } else {
                    ttl = Long.valueOf(960);
                }
            } else {
                String ttlValue = config.getFirstProperty(APIConstants.JWT_EXPIRY_TIME);
                if (ttlValue != null) {
                    ttl = Long.parseLong(ttlValue) + Long.valueOf(60);
                } else {
                    //15 * 60 (convert 15 minutes to seconds)
                    ttl = Long.valueOf(960);
                }
            }
            return ttl;
        }
    }

    protected String addCertToHeader() throws APIManagementException {

        return addCertToHeader(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }
        /**
         * Helper method to add public certificate to JWT_HEADER to signature verification.
         *
         * @throws APIManagementException
         * @param tenantDomain
         */
    protected String addCertToHeader(String tenantDomain) throws APIManagementException {

        try {
            Certificate publicCert;
            if (tenantBasedSigningEnabled) {
                publicCert = SigningUtil.getPublicCertificate(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
            } else {
                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
                publicCert = keyStoreManager.getDefaultPrimaryCertificate();
            }
            return generateHeader(publicCert, signatureAlgorithm, useKid);
        } catch (Exception e) {
            String error = "Error in obtaining keystore";
            throw new APIManagementException(error, e);
        }
    }

    protected String getMultiAttributeSeparator(int tenantId) {
        try {
            RealmConfiguration realmConfiguration = null;
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

            if (realmService != null && tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                UserStoreManager userStoreManager = (UserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager();


                realmConfiguration = userStoreManager.getRealmConfiguration();
            }

            if (realmConfiguration != null) {
                String claimSeparator = realmConfiguration.getUserStoreProperty(APIConstants.MULTI_ATTRIBUTE_SEPARATOR);
                if (claimSeparator != null && !claimSeparator.trim().isEmpty()) {
                    return claimSeparator;
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while getting the realm configuration, User store properties might not be " +
                      "returned", e);
        }
        return null;
    }

    protected Application getApplicationById(String tenantDomain, int applicationId) {

        SubscriptionDataStore datastore = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        return datastore.getApplicationById(applicationId);
    }

    /**
     * Utility method to generate JWT header with public certificate thumbprint for signature verification.
     *
     * @param publicCert         The public certificate which needs to include in the header as thumbprint
     * @param signatureAlgorithm Signature algorithm which needs to include in the header
     * @param useKid             Boolean to indicate whether to include kid property in the header
     */
    public static String generateHeader(Certificate publicCert, String signatureAlgorithm, boolean useKid)
            throws APIManagementException {
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
            java.security.cert.X509Certificate x509Certificate = (java.security.cert.X509Certificate) publicCert;
            StringBuilder jwtHeader = new StringBuilder();
            /*
             * Sample header
             * {"typ":"JWT", "alg":"SHA256withRSA", "x5t":"a_jhNus21KVuoFx65LmkW2O_l10",
             * "kid":"a_jhNus21KVuoFx65LmkW2O_l10_RS256"}
             * {"typ":"JWT", "alg":"[2]", "x5t":"[1]", "x5t":"[1]"}
             * */
            jwtHeader.append("{\"typ\":\"JWT\",");
            jwtHeader.append("\"alg\":\"");
            jwtHeader.append(APIUtil.getJWSCompliantAlgorithmCode(signatureAlgorithm));
            jwtHeader.append("\",");

            jwtHeader.append("\"x5t\":\"");
            jwtHeader.append(base64UrlEncodedThumbPrint);
            jwtHeader.append("\"");
            if (useKid) {
                jwtHeader.append(",\"kid\":\"");
                jwtHeader.append(JWTUtil.getKID(x509Certificate));
                jwtHeader.append("\"");
            }
            jwtHeader.append("}");
            return jwtHeader.toString();

        } catch (Exception e) {
            throw new APIManagementException("Error in generating public certificate thumbprint", e);
        }
    }
}
