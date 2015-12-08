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

package org.wso2.carbon.apimgt.impl.token;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.codehaus.jettison.json.JSONException;
//import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the JSON Web Token generator.
 * By default the following properties are encoded to each authenticated API request:
 * subscriber, applicationName, apiContext, version, tier, and endUserName
 * Additional properties can be encoded by engaging the ClaimsRetrieverImplClass callback-handler.
 * The JWT header and body are base64 encoded separately and concatenated with a dot.
 * Finally the token is signed using SHA256 with RSA algorithm.
 */
public abstract class AbstractJWTGenerator implements TokenGenerator {

    private static final Log log = LogFactory.getLog(JWTGenerator.class);

    public static final String API_GATEWAY_ID = "wso2.org/products/am";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private static final String SIGNATURE_ALGORITHM = "APIConsumerAuthentication.SignatureAlgorithm";

    private static ConcurrentHashMap<Integer, Key> privateKeys = new ConcurrentHashMap<Integer, Key>();
    private static ConcurrentHashMap<Integer, Certificate> publicCerts = new ConcurrentHashMap<Integer, Certificate>();


    public AbstractJWTGenerator() {


        dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CONSUMER_DIALECT_URI);
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        signatureAlgorithm = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(SIGNATURE_ALGORITHM);
        if (signatureAlgorithm == null || !(signatureAlgorithm.equals(NONE) || signatureAlgorithm.equals(SHA256_WITH_RSA))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }


        String claimsRetrieverImplClass =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CLAIMS_RETRIEVER_IMPL_CLASS);

        if (claimsRetrieverImplClass != null) {
            try {
                claimsRetriever = (ClaimsRetriever) APIUtil.getClassForName(claimsRetrieverImplClass).newInstance();
                claimsRetriever.init();
            } catch (ClassNotFoundException e) {
                log.error("Cannot find class: " + claimsRetrieverImplClass, e);
            } catch (InstantiationException e) {
                log.error("Error instantiating " + claimsRetrieverImplClass);
            } catch (IllegalAccessException e) {
                log.error("Illegal access to " + claimsRetrieverImplClass);
            } catch (APIManagementException e) {
                log.error("Error while initializing " + claimsRetrieverImplClass);
            }
        }
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public ClaimsRetriever getClaimsRetriever() {
        return claimsRetriever;
    }

    public abstract Map<String, String> populateStandardClaims(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext, String version)
            throws APIManagementException;

    public abstract Map<String, String> populateCustomClaims(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext,
            String version, String accessToken) throws APIManagementException;
    public String generateToken(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext, String version)
            throws APIManagementException {
        //To have backward compatibility with implementations done based on TokenGenerator interface
        return generateToken(keyValidationInfoDTO, apiContext, version,null);
    }

    public String encode(String stringToBeEncoded) throws APIManagementException{
        try {
            return Base64Utils.encode(stringToBeEncoded.getBytes("UTF-8"));

        } catch(UnsupportedEncodingException e){
            String error = "Unsupported encoding : " + e;
            //do not log
            throw new APIManagementException(error);
        }
    }

    public String generateToken(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext, String version,
            String accessToken) throws APIManagementException {

        String jwtHeader = buildHeader(keyValidationInfoDTO);

        /*//add cert thumbprint to header
     String headerWithCertThumb = addCertToHeader(endUserName);*/

        try {

            String base64UrlEncodedHeader = "";
            if (jwtHeader != null) {
                base64UrlEncodedHeader = encode(jwtHeader);
            }

            String jwtBody = buildBody(keyValidationInfoDTO, apiContext, version, accessToken);
            String base64UrlEncodedBody = "";
            if (jwtBody != null) {
                base64UrlEncodedBody = encode(jwtBody);
            }

            if (signatureAlgorithm.equals(SHA256_WITH_RSA)) {
                String assertion = base64UrlEncodedHeader + "." + base64UrlEncodedBody;

                //get the assertion signed
                byte[] signedAssertion = signJWT(assertion, keyValidationInfoDTO.getEndUserName());

                if (log.isDebugEnabled()) {
                    log.debug("signed assertion value : " + new String(signedAssertion, "UTF-8"));
                }
                String base64UrlEncodedAssertion = encode(new String(signedAssertion, "UTF-8"));

                return base64UrlEncodedHeader + "." + base64UrlEncodedBody + "." + base64UrlEncodedAssertion;
            } else {
                return base64UrlEncodedHeader + "." + base64UrlEncodedBody + ".";
            }
        } catch (UnsupportedEncodingException e) {
            String error = "Unsupported encoding : " + e;
            //do not log
            throw new APIManagementException(error);
        }
    }



    public String buildHeader(APIKeyValidationInfoDTO keyValidationInfoDTO) throws APIManagementException {
        String jwtHeader = null;

        //if signature algo==NONE, header without cert
        if (NONE.equals(signatureAlgorithm)) {
            StringBuilder jwtHeaderBuilder = new StringBuilder();
            jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
            jwtHeaderBuilder.append("\"alg\":\"");
            jwtHeaderBuilder.append(getJWSCompliantAlgorithmCode(NONE));
            jwtHeaderBuilder.append("\"");
            jwtHeaderBuilder.append("}");

            jwtHeader = jwtHeaderBuilder.toString();

        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeader = addCertToHeader(keyValidationInfoDTO.getEndUserName());
        }
        return jwtHeader;
    }

    public String buildBody(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext, String version, String accessToken) throws APIManagementException {
        Map<String, String> standardClaims = populateStandardClaims(keyValidationInfoDTO, apiContext, version);
        Map<String, String> customClaims = populateCustomClaims(keyValidationInfoDTO, apiContext, version, accessToken);

        if (standardClaims != null) {
            if (customClaims != null) {
                standardClaims.putAll(customClaims);
            }

            StringBuilder body = new StringBuilder();
            body.append("{");

            Iterator<Map.Entry<String, String>> entryIterator = standardClaims.entrySet().iterator();
            while (entryIterator.hasNext()) {
                 Map.Entry<String, String> entry = entryIterator.next();
                String key = entry.getKey();
                if("exp".equals(key) || "nbf".equals(key) || "iat".equals(key)){
                    //These values should be numbers.
                    body.append("\"" + key + "\":" + entry.getValue() + ",");
                }
                else{
                    body.append("\"" + key + "\":\"" + entry.getValue() + "\",");
                }

            }

            if (body.length() > 1) {
                body.delete(body.length() - 1, body.length());
            }

            body.append("}");
            return body.toString();

        }

        return null;
    }


    private byte[] signJWT(String assertion, String endUserName)
            throws APIManagementException {

        String tenantDomain = null;

        try {
            //get tenant domain
            tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = APIUtil.getTenantId(endUserName);

            Key privateKey = null;

            if (!(privateKeys.containsKey(tenantId))) {
                APIUtil.loadTenantRegistry(tenantId);
                //get tenant's key store manager
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    //derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    //obtain private key
                    //TODO: maintain a hash map with tenants' private keys after first initialization
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);
                } else {
                    try {
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    } catch (Exception e) {
                        log.error("Error while obtaining private key for super tenant", e);
                    }
                }
                if (privateKey != null) {
                    privateKeys.put(tenantId, privateKey);
                }
            } else {
                privateKey = privateKeys.get(tenantId);
            }

            //initialize signature with private key and algorithm
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign((PrivateKey) privateKey);

            //update signature with data to be signed
            byte[] dataInBytes = assertion.getBytes();
            signature.update(dataInBytes);

            //sign the assertion and return the signature
            return signature.sign();

        } catch (NoSuchAlgorithmException e) {
            String error = "Signature algorithm not found.";
            //do not log
            throw new APIManagementException(error);
        } catch (InvalidKeyException e) {
            String error = "Invalid private key provided for the signature";
            //do not log
            throw new APIManagementException(error);
        } catch (SignatureException e) {
            String error = "Error in signature";
            //do not log
            throw new APIManagementException(error);
        } catch (RegistryException e) {
            String error = "Error in loading tenant registry for " + tenantDomain;
            //do not log
            throw new APIManagementException(error);
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
            String ttlValue = config.getFirstProperty(APIConstants.API_KEY_SECURITY_CONTEXT_TTL);
            if (ttlValue != null) {
                ttl = Long.parseLong(ttlValue);
            } else {
                ttl = 15L;
            }
            return ttl;
        }
    }

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param endUserName - The end user name
     * @throws APIManagementException
     */
    private String addCertToHeader(String endUserName) throws APIManagementException {

        try {
            //get tenant domain
            String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = APIUtil.getTenantId(endUserName);
            Certificate publicCert;

            if (!(publicCerts.containsKey(tenantId))) {
                //get tenant's key store manager
                APIUtil.loadTenantRegistry(tenantId);
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                KeyStore keyStore;
                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    //derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    keyStore = tenantKSM.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                } else {
                    //keyStore = tenantKSM.getPrimaryKeyStore();
                    publicCert = tenantKSM.getDefaultPrimaryCertificate();
                }
                if (publicCert != null) {
                    publicCerts.put(tenantId, publicCert);
                }
            } else {
                publicCert = publicCerts.get(tenantId);
            }

            //generate the SHA-1 thumbprint of the certificate
            //TODO: maintain a hashmap with tenants' pubkey thumbprints after first initialization
            MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
            byte[] der = publicCert.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();

            String publicCertThumbprint = hexify(digestInBytes);
            String base64UrlEncodedThumbPrint = encode(publicCertThumbprint);
            //String headerWithCertThumb = JWT_HEADER.replaceAll("\\[1\\]", base64UrlEncodedThumbPrint);
            //headerWithCertThumb = headerWithCertThumb.replaceAll("\\[2\\]", signatureAlgorithm);
            //return headerWithCertThumb;

            StringBuilder jwtHeader = new StringBuilder();
            //Sample header
            //{"typ":"JWT", "alg":"SHA256withRSA", "x5t":"NmJmOGUxMzZlYjM2ZDRhNTZlYTA1YzdhZTRiOWE0NWI2M2JmOTc1ZA=="}
            //{"typ":"JWT", "alg":"[2]", "x5t":"[1]"}
            jwtHeader.append("{\"typ\":\"JWT\",");
            jwtHeader.append("\"alg\":\"");
            jwtHeader.append(getJWSCompliantAlgorithmCode(signatureAlgorithm));
            jwtHeader.append("\",");

            jwtHeader.append("\"x5t\":\"");
            jwtHeader.append(base64UrlEncodedThumbPrint);
            jwtHeader.append("\"");

            jwtHeader.append("}");
            return jwtHeader.toString();

        } catch (KeyStoreException e) {
            String error = "Error in obtaining tenant's keystore";
            throw new APIManagementException(error);
        } catch (CertificateEncodingException e) {
            String error = "Error in generating public cert thumbprint";
            throw new APIManagementException(error);
        } catch (NoSuchAlgorithmException e) {
            String error = "Error in generating public cert thumbprint";
            throw new APIManagementException(error);
        } catch (Exception e) {
            String error = "Error in obtaining tenant's keystore";
            throw new APIManagementException(error);
        }
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

    /**
     * Get the JWS compliant signature algorithm code of the algorithm used to sign the JWT.
     * @param signatureAlgorithm - The algorithm used to sign the JWT. If signing is disabled, the value will be NONE.
     * @return - The JWS Compliant algorithm code of the signature algorithm.
     */
    public String getJWSCompliantAlgorithmCode(String signatureAlgorithm){

        if (signatureAlgorithm == null || NONE.equals(signatureAlgorithm)){
            return JWTSignatureAlg.NONE.getJwsCompliantCode();
        }
        else if(SHA256_WITH_RSA.equals(signatureAlgorithm)){
            return JWTSignatureAlg.SHA256_WITH_RSA.getJwsCompliantCode();
        }
        else{
            return signatureAlgorithm;
        }
    }
}
