package org.apache.synapse.securevault.keystore;

import java.security.KeyStore;


public class PKCS12KeyStoreLoader extends AbstractKeyStoreLoader {

    private String keyStorePath;
    private String keyStorePassword;

    /**
     * constructs an instance of KeyStoreLoader
     *
     * @param keystorePath     - path to KeyStore file.  KeyStore must be in pkcs12 format.
     * @param keyStorePassword - password to access keyStore
     */
    public PKCS12KeyStoreLoader(String keystorePath, String keyStorePassword) {
        this.keyStorePath = keystorePath;
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * returns KeyStore to be used
     */
    public KeyStore getKeyStore() {
        return getKeyStore(keyStorePath, keyStorePassword, "PKCS12", "SunJSSE");
    }
}
