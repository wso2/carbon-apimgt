/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the JSON Web Token generator.
 * By default the following properties are encoded to each authenticated API request:
 * subscriber, applicationName, apiContext, version, tier, and endUserName
 * Additional properties can be encoded by engaging the ClaimsRetrieverImplClass callback-handler.
 * The JWT header and body are base64 encoded separately and concatenated with a dot.
 * Finally the token is signed using SHA256 with RSA algorithm.
 */
public class JWTGenerator {

    private static final Log log = LogFactory.getLog(JWTGenerator.class);

    private static final String JWT_HEADER = "{\"typ\":\"JWT\", \"alg\":\"[2]\", \"x5t\":\"[1]\"}";

    private static final String JWT_INITIAL_BODY = "{\"iss\":\"[1]\", \"exp\":[2]" +
            ", \"[0]/subscriber\":\"[3]\"" +
            ", \"[0]/applicationname\":\"[4]\"" +
            ", \"[0]/apicontext\":\"[5]\"" +
            ", \"[0]/version\":\"[6]\"" +
            ", \"[0]/tier\":\"[7]\"" +
            ", \"[0]/enduser\":\"[8]\"";

    private static final String API_GATEWAY_ID = "wso2.org/products/am";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    private static final String NONE = "NONE";

    private static volatile long ttl = -1L;

    private ClaimsRetriever claimsRetriever;

    private String dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;

    private String signatureAlgorithm = SHA256_WITH_RSA;

    private static final String SIGNATURE_ALGORITHM = "APIConsumerAuthentication.SignatureAlgorithm";

    private boolean includeClaims = true;

    private boolean enableSigning = true;

    private static ConcurrentHashMap<Integer, Key> privateKeys = new ConcurrentHashMap<Integer, Key>();
    private static ConcurrentHashMap<Integer, Certificate> publicCerts = new ConcurrentHashMap<Integer, Certificate>();

    //constructor for testing purposes
    public JWTGenerator(boolean includeClaims, boolean enableSigning) {
        this.includeClaims = includeClaims;
        this.enableSigning = enableSigning;
        signatureAlgorithm = NONE;
    }

    /**
     * Reads the ClaimsRetrieverImplClass from api-manager.xml ->
     * APIConsumerAuthentication -> ClaimsRetrieverImplClass.
     *
     * @throws APIManagementException
     */
    public JWTGenerator(){
        if (includeClaims && enableSigning) {
            String claimsRetrieverImplClass =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                            getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CLAIMS_RETRIEVER_IMPL_CLASS);
            dialectURI = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                            getAPIManagerConfiguration().getFirstProperty(ClaimsRetriever.CONSUMER_DIALECT_URI);
            if(dialectURI == null){
                dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
            }
            signatureAlgorithm =  ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                            getAPIManagerConfiguration().getFirstProperty(SIGNATURE_ALGORITHM);

            if(signatureAlgorithm == null){
                signatureAlgorithm = NONE;
            }
            else if(!NONE.equals(signatureAlgorithm) || SHA256_WITH_RSA.equals(signatureAlgorithm)){
                signatureAlgorithm = SHA256_WITH_RSA;
            }

            if(claimsRetrieverImplClass != null){
                try{
                    claimsRetriever = (ClaimsRetriever)Class.forName(claimsRetrieverImplClass).newInstance();
                    claimsRetriever.init();
                }catch (ClassNotFoundException e){
                    log.error("Cannot find class: " + claimsRetrieverImplClass,e);
                } catch (InstantiationException e) {
                    log.error("Error instantiating " + claimsRetrieverImplClass);
                } catch (IllegalAccessException e) {
                    log.error("Illegal access to " + claimsRetrieverImplClass);
                } catch (APIManagementException e){
                    log.error("Error while initializing " + claimsRetrieverImplClass);
                }
            }
        }
    }

  /**
     * Method that generates the JWT.
     *
     * @param keyValidationInfoDTO
     * @param apiContext
     * @param version
     * @param includeEndUserName
     * @return signed JWT token
     * @throws APIManagementException
     */
    public String generateToken(APIKeyValidationInfoDTO keyValidationInfoDTO, String apiContext,
                     String version, boolean includeEndUserName) throws APIManagementException{

        //generating expiring timestamp
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireIn = currentTime + 1000 * 60 * getTTL();

        String jwtBody;
        String dialect;
        if(claimsRetriever != null){
            //jwtBody = JWT_INITIAL_BODY.replaceAll("\\[0\\]", claimsRetriever.getDialectURI(endUserName));
            dialect = claimsRetriever.getDialectURI(keyValidationInfoDTO.getEndUserName());
        }else{
            //jwtBody = JWT_INITIAL_BODY.replaceAll("\\[0\\]", dialectURI);
            dialect = dialectURI;
        }
        
        String subscriber = keyValidationInfoDTO.getSubscriber();
        String applicationName = keyValidationInfoDTO.getApplicationName();
        String applicationId = keyValidationInfoDTO.getApplicationId();
        String tier = keyValidationInfoDTO.getTier();
        String endUserName = includeEndUserName ? keyValidationInfoDTO.getEndUserName() : null;
        String keyType = keyValidationInfoDTO.getType();
        String userType = keyValidationInfoDTO.getUserType();
        String applicationTier = keyValidationInfoDTO.getApplicationTier();
        String enduserTenantId = includeEndUserName ? String.valueOf(getTenantId(endUserName)) : null;
        
//        jwtBody = jwtBody.replaceAll("\\[1\\]", API_GATEWAY_ID);
//        jwtBody = jwtBody.replaceAll("\\[2\\]", String.valueOf(expireIn));
//        jwtBody = jwtBody.replaceAll("\\[3\\]", subscriber);
//        jwtBody = jwtBody.replaceAll("\\[4\\]", applicationName);
//        jwtBody = jwtBody.replaceAll("\\[5\\]", apiContext);
//        jwtBody = jwtBody.replaceAll("\\[6\\]", version);
//        jwtBody = jwtBody.replaceAll("\\[7\\]", tier);
//        jwtBody = jwtBody.replaceAll("\\[8\\]", endUserName);


        //Sample JWT body
        //{"iss":"wso2.org/products/am","exp":1349267862304,"http://wso2.org/claims/subscriber":"nirodhasub",
        // "http://wso2.org/claims/applicationname":"App1","http://wso2.org/claims/apicontext":"/echo",
        // "http://wso2.org/claims/version":"1.2.0","http://wso2.org/claims/tier":"Gold",
        // "http://wso2.org/claims/enduser":"null"}

        StringBuilder jwtBuilder = new StringBuilder();
        jwtBuilder.append("{");
        jwtBuilder.append("\"iss\":\"");
        jwtBuilder.append(API_GATEWAY_ID);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"exp\":");
        jwtBuilder.append(String.valueOf(expireIn));
        jwtBuilder.append(",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/subscriber\":\"");
        jwtBuilder.append(subscriber);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/applicationid\":\"");
        jwtBuilder.append(applicationId);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/applicationname\":\"");
        jwtBuilder.append(applicationName);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/applicationtier\":\"");
        jwtBuilder.append(applicationTier);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/apicontext\":\"");
        jwtBuilder.append(apiContext);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/version\":\"");
        jwtBuilder.append(version);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/tier\":\"");
        jwtBuilder.append(tier);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/keytype\":\"");
        jwtBuilder.append(keyType);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/usertype\":\"");
        jwtBuilder.append(userType);
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/enduser\":\"");
        jwtBuilder.append(getUserNameWithTenantPrefix(endUserName));
        jwtBuilder.append("\",");

        jwtBuilder.append("\"");
        jwtBuilder.append(dialect);
        jwtBuilder.append("/enduserTenantId\":\"");
        jwtBuilder.append(enduserTenantId);
        jwtBuilder.append("\"");

        if(claimsRetriever != null){
        	String  tenantAwareUserName = endUserName;
            if (endUserName != null) {
                tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(endUserName);
            }
            SortedMap<String,String> claimValues = claimsRetriever.getClaims(tenantAwareUserName);
            Iterator<String> it = new TreeSet(claimValues.keySet()).iterator();
            while(it.hasNext()){
                String claimURI = it.next();
                jwtBuilder.append(", \"");
                jwtBuilder.append(claimURI);
                jwtBuilder.append("\":\"");
                jwtBuilder.append(claimValues.get(claimURI));
                jwtBuilder.append("\"");
            }
        }

        jwtBuilder.append("}");
        jwtBody = jwtBuilder.toString();

      String jwtHeader = null;

      //if signature algo==NONE, header without cert
      if(NONE.equals(signatureAlgorithm)){
          StringBuilder jwtHeaderBuilder = new StringBuilder();
          jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
          jwtHeaderBuilder.append("\"alg\":\"");
          jwtHeaderBuilder.append(JWTSignatureAlg.NONE.getJwsCompliantCode());
          jwtHeaderBuilder.append("\"");
          jwtHeaderBuilder.append("}");
          
          jwtHeader = jwtHeaderBuilder.toString();

      } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)){
          jwtHeader = addCertToHeader(endUserName);
      }

      /*//add cert thumbprint to header
      String headerWithCertThumb = addCertToHeader(endUserName);*/

      String base64EncodedHeader = Base64Utils.encode(jwtHeader.getBytes());
      String base64EncodedBody = Base64Utils.encode(jwtBody.getBytes());
      if(signatureAlgorithm.equals(SHA256_WITH_RSA)){
          String assertion = base64EncodedHeader + "." + base64EncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion, endUserName);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion));
            }
            String base64EncodedAssertion = Base64Utils.encode(signedAssertion);

            return base64EncodedHeader + "." + base64EncodedBody + "." + base64EncodedAssertion;
        } else {
            return base64EncodedHeader + "." + base64EncodedBody + ".";
        }
    }

  /**
     * Helper method to sign the JWT
     *
     * @param assertion
     * @param endUserName
     * @return signed assertion
     * @throws APIManagementException
     */
    private byte[] signJWT(String assertion, String endUserName)
            throws APIManagementException {

        try {
            //get tenant domain
            String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = getTenantId(endUserName);

            Key privateKey = null;

            if (!(privateKeys.containsKey(tenantId))) {
                APIUtil.loadTenantRegistry(tenantId);
                //get tenant's key store manager
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
                    //derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    //obtain private key
                    //TODO: maintain a hash map with tenants' private keys after first initialization
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);
                }else{
                    try{
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    }catch (Exception e){
                        log.error("Error while obtaining private key for super tenant",e);
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
            byte[] signedInfo = signature.sign();
            return signedInfo;

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
        } catch (APIManagementException e) {
            //do not log
            throw new APIManagementException(e.getMessage());
        }
    }

  /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param endUserName
     * @throws APIManagementException
     */
    private String addCertToHeader(String endUserName) throws APIManagementException {

        try {
            //get tenant domain
            String tenantDomain = MultitenantUtils.getTenantDomain(endUserName);
            //get tenantId
            int tenantId = getTenantId(endUserName);
            Certificate publicCert = null;

            if (!(publicCerts.containsKey(tenantId))) {
                //get tenant's key store manager
                APIUtil.loadTenantRegistry(tenantId);
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                KeyStore keyStore = null;
                if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
                    //derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + ".jks";
                    keyStore = tenantKSM.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                }else{
                    keyStore = tenantKSM.getPrimaryKeyStore();
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
            String base64EncodedThumbPrint = Base64Utils.encode(publicCertThumbprint.getBytes());
            //String headerWithCertThumb = JWT_HEADER.replaceAll("\\[1\\]", base64EncodedThumbPrint);
            //headerWithCertThumb = headerWithCertThumb.replaceAll("\\[2\\]", signatureAlgorithm);
            //return headerWithCertThumb;

            StringBuilder jwtHeader = new StringBuilder();
            //Sample header
            //{"typ":"JWT", "alg":"SHA256withRSA", "x5t":"NmJmOGUxMzZlYjM2ZDRhNTZlYTA1YzdhZTRiOWE0NWI2M2JmOTc1ZA=="}
            //{"typ":"JWT", "alg":"[2]", "x5t":"[1]"}
            jwtHeader.append("{\"typ\":\"JWT\",");
            jwtHeader.append("\"alg\":\"");
            jwtHeader.append(SHA256_WITH_RSA.equals(signatureAlgorithm) ?
                             JWTSignatureAlg.SHA256_WITH_RSA.getJwsCompliantCode() : signatureAlgorithm);
            jwtHeader.append("\",");

            jwtHeader.append("\"x5t\":\"");
            jwtHeader.append(base64EncodedThumbPrint);
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

    private long getTTL() {
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
     * Helper method to get tenantId from userName
     *
     * @param userName
     * @return tenantId
     * @throws APIManagementException
     */
    static int getTenantId(String userName) throws APIManagementException {
        //get tenant domain from user name
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if(realmService == null){
            return MultitenantConstants.SUPER_TENANT_ID;
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            return tenantId;
        } catch (UserStoreException e) {
            String error = "Error in obtaining tenantId from Domain";
            //do not log
            throw new APIManagementException(error);
        }
    }

  /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes
     * @return  hexadecimal representation
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
    * Helper method to get username with tenant domain. 
    *      
    * @param userName
    * @return  userName with tenant domain
    */
    public static String getUserNameWithTenantPrefix(String userName) {
    	String userNameWithTenantPrefix = userName;
    	String tenantDomain = MultitenantUtils.getTenantDomain(userName);
    	if (userName != null && !userName.contains("@") 
    			&& MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
    		userNameWithTenantPrefix = userName + "@" + tenantDomain;
    	}
    	return userNameWithTenantPrefix;
    	
    }


}
