package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class for managing private keys and public certs of tenants used for signing purposes.
 */
public class SigningUtil {

    private static final ConcurrentHashMap<Integer, Key> privateKeys = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Certificate> publicCerts = new ConcurrentHashMap<>();

    private SigningUtil() {

        throw new IllegalStateException("Utility class");
    }

    /**
     * Util method to get signing key for the tenant.
     *
     * @param tenantId Tenant Id
     * @return Private key to sign
     * @throws APIManagementException If an error occurs
     */
    public static PrivateKey getSigningKey(int tenantId) throws APIManagementException {

        //get tenant domain of the key to sign from
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        Key privateKey;
        try {
            if (!(privateKeys.containsKey(tenantId))) {
                APIUtil.loadTenantRegistry(tenantId);
                //get tenant's key store manager
                KeyStoreManager tenantKeyStoreManager = KeyStoreManager.getInstance(tenantId);
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //derive key store name
                    String ksName = tenantDomain.trim().replace('.', '-');
                    String jksName = ksName + APIConstants.KeyStoreManagement.KEY_STORE_EXTENSION_JKS;
                    //obtain private key
                    privateKey = tenantKeyStoreManager.getPrivateKey(jksName, tenantDomain);
                } else {
                    privateKey = tenantKeyStoreManager.getDefaultPrivateKey();
                }
                if (privateKey != null) {
                    privateKeys.put(tenantId, privateKey);
                }
            } else {
                privateKey = privateKeys.get(tenantId);
            }
            if (privateKey == null) {
                throw new APIManagementException("Error while obtaining private key for tenant: " + tenantDomain);
            }
            return (PrivateKey) privateKey;
        } catch (RegistryException e) {
            throw new APIManagementException("Error while loading tenant registry for " + tenantDomain, e);
        } catch (Exception e) {
            throw new APIManagementException("Error while obtaining private key for tenant: " + tenantDomain, e);
        }
    }

    /**
     * Util method to get public certificate.
     *
     * @param tenantId Tenant domain
     * @return public cert
     * @throws APIManagementException If an error occurs
     */
    public static Certificate getPublicCertificate(int tenantId) throws APIManagementException {
        //get tenant domain of the key to add the certificate from
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        try {
            Certificate publicCert;
            if (!(publicCerts.containsKey(tenantId))) {
                //get tenant's key store manager
                APIUtil.loadTenantRegistry(tenantId);
                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                KeyStore keyStore;
                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //derive key store name
                    String ksName = tenantDomain.trim().replace('.', '-');
                    String jksName = ksName + APIConstants.KeyStoreManagement.KEY_STORE_EXTENSION_JKS;
                    keyStore = keyStoreManager.getKeyStore(jksName);
                    publicCert = keyStore.getCertificate(tenantDomain);
                } else {
                    publicCert = keyStoreManager.getDefaultPrimaryCertificate();
                }
                if (publicCert != null) {
                    publicCerts.put(tenantId, publicCert);
                }
            } else {
                publicCert = publicCerts.get(tenantId);
            }
            if (publicCert == null) {
                throw new APIManagementException(
                        "Error while obtaining public certificate from keystore for tenant: " + tenantDomain);
            } else {
                return publicCert;
            }
        } catch (RegistryException e) {
            throw new APIManagementException("Error while loading tenant registry for " + tenantDomain, e);
        } catch (Exception e) {
            throw new APIManagementException(
                    "Error while obtaining public certificate from keystore for tenant: " + tenantDomain, e);
        }
    }
}
