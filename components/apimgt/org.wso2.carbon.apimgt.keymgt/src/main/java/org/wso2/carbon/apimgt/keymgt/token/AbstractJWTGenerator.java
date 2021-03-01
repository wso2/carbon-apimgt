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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SigningUtil;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.nio.charset.Charset;
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

    public static final String FORMAT_JSON_ARRAY_PROPERTY = "formatJWTJsonArray";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private String userAttributeSeparator = APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;

    private boolean tenantBasedSigningEnabled;

    public AbstractJWTGenerator() {

        JWTConfigurationDto jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getJwtConfigurationDto();

        dialectURI = jwtConfigurationDto.getConsumerDialectUri();
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }

        String claimsRetrieverImplClass = jwtConfigurationDto.getClaimRetrieverImplClass();

        if (claimsRetrieverImplClass != null) {
            try {
                claimsRetriever = (ClaimsRetriever) APIUtil.getClassForName(claimsRetrieverImplClass).newInstance();
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
        return java.util.Base64.getUrlEncoder().encodeToString(stringToBeEncoded);
    }

    public String generateToken(TokenValidationContext validationContext) throws APIManagementException {

        String jwtHeader = buildHeader(validationContext.getValidationInfoDTO().getEndUserName());

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
            byte[] signedAssertion = signJWT(assertion, validationContext.getValidationInfoDTO().getEndUserName());

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
            }
            String base64UrlEncodedAssertion = encode(signedAssertion);

            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.';
        }
    }

    public String buildHeader(String endUserName) throws APIManagementException {
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
            jwtHeader = addCertToHeader(endUserName);
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
            ObjectMapper mapper = new ObjectMapper();

            if (standardClaims != null) {
                Iterator<String> it = new TreeSet(standardClaims.keySet()).iterator();
                while (it.hasNext()) {
                    String claimURI = it.next();
                    String claimVal = standardClaims.get(claimURI);
                    List<String> claimList = new ArrayList<String>();
                    if (claimVal != null && claimVal.contains("{")) {
                        try {
                            Map<String, String> map = mapper.readValue(claimVal, Map.class);
                            jwtClaimsSetBuilder.claim(claimURI, map);
                        } catch (IOException e) {
                            // Exception isn't thrown in order to generate jwt without claim, even if an error is
                            // occurred during the retrieving claims.
                            log.error(String.format("Error while reading claim values for %s", claimVal), e);
                        }
                    } else if (Boolean.parseBoolean(System.getProperty(FORMAT_JSON_ARRAY_PROPERTY)) && claimVal != null
                            && claimVal.contains("[\"") && claimVal.contains("\"]")) {

                        try {
                            List<String> arrayList = mapper.readValue(claimVal, List.class);
                            jwtClaimsSetBuilder.claim(claimURI, arrayList);
                        } catch (IOException e) {
                            // Exception isn't thrown in order to generate jwt without claim, even if an error is
                            // occurred during the retrieving claims.
                            log.error("Error while reading claim values", e);
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
                    } else if ("iat".equals(claimURI)) {
                        jwtClaimsSetBuilder.issueTime(new Date(Long.valueOf(standardClaims.get(claimURI))));
                    } else {
                        jwtClaimsSetBuilder.claim(claimURI, claimVal);
                    }
                }
                //Adding JTI standard claim to the backend JWT
                jwtClaimsSetBuilder.jwtID(UUID.randomUUID().toString());
            }
            return jwtClaimsSetBuilder.build().toJSONObject().toJSONString();
        }
        return null;
    }

    public byte[] signJWT(String assertion, String endUserName) throws APIManagementException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantBasedSigningEnabled ?
                MultitenantUtils.getTenantDomain(endUserName) : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        return APIUtil.signJwt(assertion, SigningUtil.getSigningKey(tenantId), signatureAlgorithm);
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

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param endUserName - The end user name
     * @throws APIManagementException if an error occurs
     */
    protected String addCertToHeader(String endUserName) throws APIManagementException {

        //get tenant domain
        int tenantId = APIUtil.getTenantIdFromTenantDomain(
                tenantBasedSigningEnabled ? MultitenantUtils.getTenantDomain(endUserName) :
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        return APIUtil.generateHeader(SigningUtil.getPublicCertificate(tenantId), signatureAlgorithm);
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
}
