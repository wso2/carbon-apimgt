/**
 *
 */
package org.apache.synapse.securevault.keystore;

import java.security.KeyStore;

/**
 * Loads KeyStore from a JKS file
 */
public class JKSKeyStoreLoader extends AbstractKeyStoreLoader {

    private String keyStorePath;
    private String keyStorePassword;

    /**
     * constructs an instance of KeyStoreLoader
     *
     * @param keyStorePath     - path to KeyStore file.  KeyStore must be in JKS format.
     * @param keyStorePassword - password to access keyStore
     */
    public JKSKeyStoreLoader(String keyStorePath, String keyStorePassword) {
        super();
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Returns KeyStore to be used
     *
     * @return KeyStore instance
     */
    public KeyStore getKeyStore() {
        return getKeyStore(keyStorePath, keyStorePassword, "JKS", null);
    }

}
